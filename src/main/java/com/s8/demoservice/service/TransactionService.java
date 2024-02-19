package com.s8.demoservice.service;

import com.s8.demoservice.AppConfiguration;
import com.s8.demoservice.dto.*;
import com.s8.demoservice.exception.CustomerErrorException;
import com.s8.demoservice.exception.CustomerNotFound;
import com.s8.demoservice.model.Account;
import com.s8.demoservice.model.CustomerKYC;
import com.s8.demoservice.model.enums.AccountType;
import com.s8.demoservice.model.enums.Endpoint;
import com.s8.demoservice.repository.AccountRepository;
import com.s8.demoservice.repository.CustomerKYCRepository;
import com.s8.demoservice.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import javax.transaction.Transactional;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@CacheConfig(cacheNames = {"balances", "transaction"})
public class TransactionService {
    Logger logger = LoggerFactory.getLogger(TransactionService.class);
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private CustomerKYCRepository customerKYCRepository;
    @Autowired
    private ThoughtMachineApiClient thoughtMachineApiClient;
    @Autowired
    private AppConfiguration configuration;

    @Autowired
    private CacheManager cacheManager;

    public TransactionExpensesDTO getTransactionExpensesDetail(Map<String, Object> queryParamMap) {
        AccountTransactionDTO transactionResponse = new AccountTransactionDTO();
        Account account = accountRepository.findByAccountNumber(queryParamMap.get("account_number").toString());

        queryParamMap.put("account_ids", account.getStakeholderIds());
        queryParamMap.remove("account_number");

        while(queryParamMap.get("page_token") != ""){
            String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

            ResponseEntity<AccountTransactionDTO> response = thoughtMachineApiClient.get(getTMTransactionApiUrl(), queryString, AccountTransactionDTO.class);

            List<TransactionDTO> transactiondtoFilter = Objects.requireNonNull(response.getBody()).getPosting_instruction_batches()
                            .parallelStream()
                            .filter(transactionDTO ->
                                    transactionDTO.getPosting_instructions()
                                            .stream()
                                            .anyMatch(postingInstructionDTO -> postingInstructionDTO.getOutbound_hard_settlement() != null
                                                    || (postingInstructionDTO.getTransfer() != null &&
                                                        postingInstructionDTO.getTransfer().getDebtor_target_account_id().equalsIgnoreCase(account.getStakeholderIds()))
                                            )
                            )
                            .filter(transactionDTO -> transactionDTO.getStatus().equalsIgnoreCase("POSTING_INSTRUCTION_BATCH_STATUS_ACCEPTED"))
                            .collect(Collectors.toList());

            if(!transactiondtoFilter.isEmpty()){
                transactionResponse.setPosting_instruction_batches(transactiondtoFilter);
                transactionResponse.setNext_page_token(response.getBody().getNext_page_token());
                transactionResponse.setPrevious_page_token(response.getBody().getPrevious_page_token());
            }
            queryParamMap.put("page_token", Objects.equals(transactionResponse.getNext_page_token(), null) ? "" : transactionResponse.getNext_page_token());
        }

        return new TransactionExpensesDTO(
                transactionResponse,
                "Balance Expenses and List Transaction",
                configuration.getMaximum_Pool_Size()
        );
    }

    @Cacheable(value = "transaction", key = "{#queryParamMap['id_customer'], #queryParamMap['GROUP_BY']}")
    public TransactionTotalExpensesDTO getTransactionTotalExpensesDetail(Map<String, Object> queryParamMap){
        Optional<CustomerKYC> customer = customerKYCRepository.findById(queryParamMap.get("id_customer").toString());
        Map<String, TransactionExpensesDTO> mapTransaction = new HashMap<>();
        Map<Instant, TransactionDTO> instantTransactionDTOMap = new HashMap<>();
        Map<YearMonth, List<TransactionDTO>> transactionsByMonth = new HashMap<>();

        if(customer.isEmpty()){
            throw new CustomerNotFound("customer not found");
        }

        if(queryParamMap.get("GROUP_BY").toString().equalsIgnoreCase("ACCOUNT_NUMBER")){
            customer.stream().parallel()
                    .flatMap(customerKYC ->
                            customerKYC.getAccounts()
                                    .stream()
                                    .filter(account -> account.getType() == AccountType.SAVING)
                    )
                    .forEach(accountId -> {
                        Map<String, Object> queryParams = new HashMap<>(queryParamMap);
                        queryParams.put("account_number", accountId.getAccountNumber());
                        mapTransaction.put(accountId.getAccountNumber(), this.getTransactionExpensesDetail(queryParams));
                    });
            mapTransaction.forEach((key, transactions) -> {
                List<TransactionDTO> sortedTransactions = transactions.getTransactionList().getPosting_instruction_batches().parallelStream()
                        .sorted(Comparator.comparing(TransactionDTO::getInsertion_timestamp).reversed())
                        .collect(Collectors.toList());
                transactions.getTransactionList().setPosting_instruction_batches(sortedTransactions);
                mapTransaction.put(key, transactions);
            });
        } else if (queryParamMap.get("GROUP_BY").toString().equalsIgnoreCase("MONTHS")) {
            List<TransactionExpensesDTO> transactionExpensesDTO= customer.stream().parallel()
                    .flatMap(customerKYC ->
                            customerKYC.getAccounts()
                                    .stream()
                                    .filter(account -> account.getType() == AccountType.SAVING)
                    )
                    .map(accountId -> {
                        Map<String, Object> queryParams = new HashMap<>(queryParamMap);
                        queryParams.put("account_number", accountId.getAccountNumber());
                        return this.getTransactionExpensesDetail(queryParams);
                    }).toList();

            transactionExpensesDTO.parallelStream()
                    .flatMap(
                            transactionExpensesDTOs ->
                                    transactionExpensesDTOs.getTransactionList().getPosting_instruction_batches().parallelStream()
                    )
                    .forEach(
                            transactionDTO -> {
                                instantTransactionDTOMap.put(transactionDTO.getInsertion_timestamp(), transactionDTO);
                            }
                    );
            instantTransactionDTOMap.forEach((timestamp, transactionDTO) -> {

                LocalDateTime localDateTime = timestamp.atZone(ZoneId.of("Asia/Jakarta")).toLocalDateTime();
                YearMonth yearMonth = YearMonth.from(localDateTime);
                transactionsByMonth.computeIfAbsent(yearMonth, key -> new ArrayList<>()).add(transactionDTO);
            });

            transactionsByMonth.forEach((key, transactions) -> {
                mapTransaction.put(key.toString(), new TransactionExpensesDTO(
                        transactions,
                        "Balance Expenses and List Transaction",
                        configuration.getMaximum_Pool_Size()
                ));
            });

            mapTransaction.forEach((key, transactions) -> {
                List<TransactionDTO> sortedTransactions = transactions.getTransactionDTOS().parallelStream()
                        .sorted(Comparator.comparing(TransactionDTO::getInsertion_timestamp).reversed())
                        .collect(Collectors.toList());
                transactions.setTransactionDTOS(sortedTransactions);
                mapTransaction.put(key, transactions);
            });
        }

        return new TransactionTotalExpensesDTO(
                mapTransaction,
                "Total Balance Expenses and List Transaction"
        );
    }


    public AccountTransactionDTO getTransactionIncomesDetail(Map<String, Object> queryParamMap) {
        AccountTransactionDTO transactionResponse = new AccountTransactionDTO();
        Account account = accountRepository.findByAccountNumber(queryParamMap.get("account_number").toString());

        queryParamMap.put("account_ids", account.getStakeholderIds());
        queryParamMap.remove("account_number");

        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

        ResponseEntity<AccountTransactionDTO> response = thoughtMachineApiClient.get(getTMTransactionApiUrl(), queryString, AccountTransactionDTO.class);

        List<TransactionDTO> transactiondtoFilter = Objects.requireNonNull(response.getBody()).getPosting_instruction_batches()
                .stream()
                .filter(transactionDTO ->
                        transactionDTO.getPosting_instructions()
                                .stream()
                                .anyMatch(postingInstructionDTO -> postingInstructionDTO.getInbound_hard_settlement() != null)
                ).collect(Collectors.toList());
        transactionResponse.setPosting_instruction_batches(transactiondtoFilter);
        return transactionResponse;
    }

    public AccountTransactionDTO getTransactionMutationDetail(Map<String, Object> queryParamMap) {
        AccountTransactionDTO transactionResponse = new AccountTransactionDTO();
        Account account = accountRepository.findByAccountNumber(queryParamMap.get("account_number").toString());

        queryParamMap.put("account_ids", account.getStakeholderIds());
        queryParamMap.remove("account_number");

        while(queryParamMap.get("page_token") != ""){
            String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

            ResponseEntity<AccountTransactionDTO> response = thoughtMachineApiClient.get(getTMTransactionApiUrl(), queryString, AccountTransactionDTO.class);

            List<TransactionDTO> transactiondtoFilter = Objects.requireNonNull(response.getBody())
                    .getPosting_instruction_batches()
                    .stream()
                    .filter(transactionDTO -> {
                        return transactionDTO.getPosting_instructions()
                                .stream()
                                .anyMatch(postingInstructionDTO -> {
                                    return postingInstructionDTO.getInbound_hard_settlement() != null
                                            || postingInstructionDTO.getOutbound_hard_settlement() != null
                                            || postingInstructionDTO.getTransfer() != null
                                            || (postingInstructionDTO.getCustom_instruction() != null &&
                                            postingInstructionDTO.getCustom_instruction().getPostings()
                                                    .stream()
                                                    .anyMatch(committedPostingDTO -> committedPostingDTO.getCredit()
                                                            && committedPostingDTO.getAccount_id().equalsIgnoreCase(account.getStakeholderIds())
                                                            && committedPostingDTO.getAccount_address().equalsIgnoreCase("default")));
                                });
                    })
                    .filter(transactionDTO -> transactionDTO.getStatus().equalsIgnoreCase("POSTING_INSTRUCTION_BATCH_STATUS_ACCEPTED"))
                    .sorted(Comparator.comparing(TransactionDTO::getInsertion_timestamp).reversed())
                    .collect(Collectors.toList());
            transactionResponse.setPosting_instruction_batches(transactiondtoFilter);
            transactionResponse.setNext_page_token(response.getBody().getNext_page_token());
            transactionResponse.setPrevious_page_token(response.getBody().getPrevious_page_token());
            queryParamMap.put("page_token", transactionResponse.getNext_page_token());
        }
        return transactionResponse;
    }

    public AccountTransactionDTO getTransactionInstruction(Map<String, Object> queryParamMap){
        Account account = accountRepository.findByAccountNumber(queryParamMap.get("account_number").toString());

        queryParamMap.put("account_ids", account.getStakeholderIds());
        queryParamMap.remove("account_number");

        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

        ResponseEntity<AccountTransactionDTO> response = thoughtMachineApiClient.get(getTMTransactionApiUrl(), queryString, AccountTransactionDTO.class);

        return response.getBody();
    }

    public <T> T getLedger(Map<String, Object> queryParamMap){
        Optional<CustomerKYC> customer = customerKYCRepository.findById(queryParamMap.get("id_customer").toString());
        HashMap<String, List<TransactionDTO>> mapTransaction = new HashMap<>();

        if(customer.isEmpty()){
            throw new CustomerNotFound("customer not found");
        }

        if(queryParamMap.containsKey("grouping") && (boolean) queryParamMap.get("grouping")){
            customer.stream().parallel()
                .flatMap(customerKYC ->
                        customerKYC.getAccounts()
                                .stream()
                                .filter(account -> account.getType() == AccountType.valueOf(queryParamMap.get("type").toString().toUpperCase()))
                )
                .forEach(accountId -> {
                    queryParamMap.put("account_ids", accountId.getStakeholderIds());
                    String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);
                    ResponseEntity<AccountTransactionDTO> response = thoughtMachineApiClient.get(getTMTransactionApiUrl(), queryString, AccountTransactionDTO.class);
                    List<TransactionDTO> transactions = Objects.requireNonNull(response.getBody()).getPosting_instruction_batches();
                    mapTransaction.put(accountId.getAccountNumber(), transactions);
                });

            // Sorting the transactions in the map by insertion_timestamp (descending order)
            mapTransaction.forEach((accountId, transactions) -> {
                List<TransactionDTO> sortedTransactions = transactions.stream()
                        .sorted(Comparator.comparing(TransactionDTO::getInsertion_timestamp).reversed())
                        .collect(Collectors.toList());
                mapTransaction.put(accountId, sortedTransactions);
            });

            return (T) mapTransaction;
        }else{
            List<TransactionDTO> transactionList =  customer.stream().parallel()
                    .flatMap(customerKYC ->
                            customerKYC.getAccounts()
                                    .stream()
                                    .filter(
                                            account -> account.getType() == AccountType.SAVING)
                                    .map(Account::getStakeholderIds)
                    )
                    .flatMap(accountId -> {
                        queryParamMap.put("account_ids", accountId);

                        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

                        ResponseEntity<AccountTransactionDTO> response = thoughtMachineApiClient.get(getTMTransactionApiUrl(), queryString, AccountTransactionDTO.class);

                        return Objects.requireNonNull(response.getBody()).getPosting_instruction_batches().parallelStream();
                    })
                    .sorted(Comparator.comparing(TransactionDTO::getInsertion_timestamp).reversed())
                    .collect(Collectors.toList());

            return (T) transactionList;
        }
    }

    public TransactionResponseDTO postingTransaction(String path, TransactionRequestDTO transactionRequest) {
        UUID client_batch_ids = UUID.randomUUID();
        UUID client_transaction_ids = UUID.randomUUID();
        String instruction;
        String message = null;
        Account account = accountRepository.findByAccountNumber(transactionRequest.getAccountNumber());
        String customerId = account.getCustomer().getId();

        // Clear the cache balances entry associated with the customerId
        Cache balancesCache = cacheManager.getCache("balances");
        assert balancesCache != null;
        balancesCache.evict(customerId);

        if (path.equalsIgnoreCase("deposit")) {
            instruction = "inbound_hard_settlement";
        } else if (path.equalsIgnoreCase("withdraw")) {
            instruction = "outbound_hard_settlement";

            // Clear the cache transaction entry associated with the customerId
            Cache balancesCacheTransaction = cacheManager.getCache("transaction");
            assert balancesCacheTransaction != null;
            balancesCacheTransaction.clear();
        } else {
            try {
                throw new Exception("no path found");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

//        check account
        if (account.getId() == null) {
            throw new CustomerNotFound("account not found");
        }

//        post instruction
        String requestId = "posting-transaction-" + System.currentTimeMillis();
        String body = "{\n" +
                "     \"request_id\": \"" + requestId + "\",\n" +
                "     \"posting_instruction_batch\": {\n" +
                "         \"client_id\": \"" + "AsyncCreatePostingInstructionBatch" + "\",\n" +
                "         \"client_batch_id\": \"" + client_batch_ids.toString() + "\",\n" +
                "         \"posting_instructions\": [{\n" +
                "               \"client_transaction_id\": \"" + client_transaction_ids.toString() + "\",\n" +
                "               \"" + instruction + "\": {\n" +
                "                       \"amount\": \"" + transactionRequest.getAmount() + "\",\n" +
                "                       \"denomination\": \"" + transactionRequest.getDenomination().toUpperCase() + "\",\n" +
                "                       \"target_account\": {\n" +
                "                           \"account_id\": \"" + account.getStakeholderIds() + "\"\n" +
                "                       },\n" +
                "                       \"internal_account_id\": \"" + configuration.getInternal_account() + "\"\n" +
                "               }\n" +
                "         }]\n" +
                "     }\n" +
                "}";

        ResponseEntity<String> responseEntity = thoughtMachineApiClient.post(getTMTransactionAsyncApiUrl(), body, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CustomerErrorException("create account failed");
        }

        boolean isPostingCompleted = false;
        TransactionDTO transactiondtoFilter = null;

//        memastikan bahwa posting transaction sudah complete
        try {
            Thread.sleep(1500); // Wait for 1.5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        while (!isPostingCompleted) {
            Map<String, Object> queryParamMapForGetTransactionInstruction = new HashMap<>();
            queryParamMapForGetTransactionInstruction.put("account_number", account.getAccountNumber());
            queryParamMapForGetTransactionInstruction.put("page_size", "100");
            queryParamMapForGetTransactionInstruction.put("order_by_direction", "ORDER_BY_DESC");
            queryParamMapForGetTransactionInstruction.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of("Asia/Jakarta"))).withDayOfMonth(1).toInstant());
            queryParamMapForGetTransactionInstruction.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of("Asia/Jakarta"))).toInstant());

//        check instruction post
            AccountTransactionDTO transactionResponse = getTransactionInstruction(queryParamMapForGetTransactionInstruction);

            transactiondtoFilter = Objects.requireNonNull(transactionResponse).getPosting_instruction_batches()
                    .stream()
                    .filter(transactionDTO ->
                            transactionDTO.getClient_batch_id().equalsIgnoreCase(client_batch_ids.toString())
                    )
                    .filter(transactionDTO ->
                            transactionDTO.getPosting_instructions()
                                    .stream()
                                    .anyMatch(postingInstructionDTO -> postingInstructionDTO.getClient_transaction_id().equalsIgnoreCase(client_transaction_ids.toString()))
                    )
                    .findFirst()
                    .orElse(null);

            if (!transactiondtoFilter.getStatus().isEmpty() && transactiondtoFilter.getStatus().equalsIgnoreCase("POSTING_INSTRUCTION_BATCH_STATUS_REJECTED")) {
                message = "Rejected";
                isPostingCompleted = true;
            } else if (!transactiondtoFilter.getStatus().isEmpty() && transactiondtoFilter.getStatus().equalsIgnoreCase("POSTING_INSTRUCTION_BATCH_STATUS_ACCEPTED")) {
                message = "Accepted";
                isPostingCompleted = true;
            }
        }

        return new TransactionResponseDTO(
                account.getId(),
                account.getAccountNumber(),
                message,
                transactiondtoFilter.getPosting_instructions()
                                .stream()
                                        .flatMap(postingInstructionDTO ->
                                                postingInstructionDTO.getContract_violations()
                                                .stream()
                                                .map(
                                                        ContractViolationDTO::getReason
                                                )
                                        )
                                                .findFirst()
                        .orElse("Posting " +  path.toUpperCase().charAt(0) + path.substring(1 , path.length()).toLowerCase() + " is " + message),
                Double.parseDouble(transactionRequest.getAmount()),
                transactiondtoFilter.getInsertion_timestamp()
        );
    }

    private String getTMTransactionApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.TRANSACTIONS.endPoint;
    }

    private String getTMTransactionAsyncApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.TRANSACTIONS_ASYNC.endPoint;
    }
}
