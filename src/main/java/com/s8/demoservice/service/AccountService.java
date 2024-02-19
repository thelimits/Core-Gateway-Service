package com.s8.demoservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s8.demoservice.AppConfiguration;
import com.s8.demoservice.dto.*;
import com.s8.demoservice.exception.CustomerErrorException;
import com.s8.demoservice.model.Account;
import com.s8.demoservice.model.AccountAdditionalDetails;
import com.s8.demoservice.model.AccountNote;
import com.s8.demoservice.model.CustomerKYC;
import com.s8.demoservice.model.enums.AccountStatusType;
import com.s8.demoservice.model.enums.AccountType;
import com.s8.demoservice.model.enums.CustomerStatusType;
import com.s8.demoservice.model.enums.Endpoint;
import com.s8.demoservice.repository.AccountNoteRepository;
import com.s8.demoservice.repository.AccountRepository;
import com.s8.demoservice.repository.CustomerKYCRepository;
import com.s8.demoservice.util.GenerateAccountNumber;
import com.s8.demoservice.util.StringUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Status;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountService {
    Logger logger = LoggerFactory.getLogger(AccountService.class);
    private ModelMapper modelMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CustomerKYCRepository customerKYCRepository;
    @Autowired
    private AccountNoteRepository accountNoteRepository;
    @Autowired
    private AccountRepository accountRepository;
    private TransactionService transactionService;
    private ProductService productService;
    @Autowired
    private RequestServiceAsync requestServiceAsync;
    @Autowired
    private AppConfiguration configuration;
    @Autowired
    private ThoughtMachineApiClient thoughtMachineApiClient;
    private static final int MAX_RETRIES = 3;

    public Account createSavingoanAccount(String id, CreateAccountRequestDTO request) {
        Map<String, String> innerMap = new HashMap<>();
        List<String> innerList = Collections.singletonList(request.getInstanceParameterDTO().getAccount_tier_names());
        innerMap.put("ATM", request.getInstanceParameterDTO().getDaily_withdrawal_limit_by_transaction_type());
        String jsonEscapedList = null;
        String jsonEscapedMap = null;

        UUID uuid = UUID.randomUUID();
        GenerateAccountNumber acId = new GenerateAccountNumber();
        Account account = new Account();
        AccountAdditionalDetails details = new AccountAdditionalDetails();
        Optional<CustomerKYC> user = customerKYCRepository.findById(id);

        if (!user.isPresent()) {
            throw new CustomerErrorException("customer id is not already exists");
        } else if (user.get().getStatus() != CustomerStatusType.ACTIVE) {
            throw new CustomerErrorException("customer status not active");
        }

        try {
            jsonEscapedList = objectMapper.writeValueAsString(innerList);
            jsonEscapedMap = objectMapper.writeValueAsString(innerMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String requestId = "create-account-" + System.currentTimeMillis();
        String body = "{\n" +
                "     \"request_id\": \"" + requestId + "\",\n" +
                "     \"account\": {\n" +
                "         \"id\": \"" + uuid.toString() + "\",\n" +
                "         \"product_version_id\": \"" + request.getProductVersionId() + "\",\n" +
                "         \"permitted_denominations\": [\n" +
                "               \"" + "IDR" + "\",\n" +
                "               \"" + "USD" + "\",\n" +
                "               \"" + "EUR" + "\",\n" +
                "               \"" + "SGD" + "\"\n" +
                "         ],\n" +
                "         \"status\": \"" + "ACCOUNT_STATUS_" + AccountStatusType.OPEN.toString() + "\",\n" +
                "         \"stakeholder_ids\": [\n" +
                "               \"" + user.get().getId() + "\"\n" +
                "         ],\n" +
                "         \"instance_param_vals\": {\n" +
                "               \"account_tier_names\": \"" + jsonEscapedList.replace("\"", "\\\"") + "\",\n" +
                "               \"daily_withdrawal_limit_by_transaction_type\": \"" + jsonEscapedMap.replace("\"", "\\\"") + "\",\n" +
                "               \"interest_application_day\": \"" + request.getInstanceParameterDTO().getInterest_application_day() + "\",\n" +
                "               \"inactivity_fee_application_day\": \"" + request.getInstanceParameterDTO().getInactivity_fee_application_day() + "\",\n" +
                "               \"maintenance_fee_application_day\": \"" + request.getInstanceParameterDTO().getMaintenance_fee_application_day() + "\"\n" +
                "         }\n" +
                "     }\n" +
                "}";

        ResponseEntity<String> responseEntity = thoughtMachineApiClient.post(getTMAccountApiUrl(), body, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CustomerErrorException("create account failed");
        }
        details.setId(UUID.randomUUID().toString());
        details.setSourceOfFunds(request.getAccountAdditionalDetails().getSourceOfFunds());

        account.setAccountName(request.getAccountName());
        account.setAccountNumber(acId.Generate());
        account.setStakeholderIds(uuid.toString());
        account.setStatus(AccountStatusType.OPEN);
        account.setType(AccountType.SAVING);
        account.setOpenandCloseTime(responseEntity.getBody());
        account.setAdditionalDetails(details);
        account.setCustomer(user.get());

        Set<Account> accounts = new HashSet<>();
        accounts.add(account);

        user.get().setAccounts(accounts);
        customerKYCRepository.save(user.get());
        return account;
    }

    public boolean checkPendingStatus(String id, String type){
        AccountDTO accounts = this.getAccountByCustomerId(id);

        return accounts.getAccount().stream()
                .filter(account -> account.getType() == AccountType.valueOf(type.toUpperCase()))
                .anyMatch(account -> account.getStatus() == AccountStatusType.PENDING);
    }

    public Account createLoanAccount(String id, CreateAccountRequestDTO request){
        UUID uuid = UUID.randomUUID();
        LocalDate loanStartDate = LocalDate.now();
        GenerateAccountNumber acId = new GenerateAccountNumber();
        Account account = new Account();
        AccountAdditionalDetails details = new AccountAdditionalDetails();
        Optional<CustomerKYC> user = customerKYCRepository.findById(id);
        Double fixed_interest_rate = request.getInstanceParameterLoan().getFixed_interest_rate() == null ? configuration.getPERIODIC_INTEREST_RATE() : request.getInstanceParameterLoan().getFixed_interest_rate();
        Double balloon_payment_amount = request.getInstanceParameterLoan().getBalloon_payment_amount() == null ? null : request.getInstanceParameterLoan().getBalloon_payment_amount();
        Integer balloon_payment_days_delta = request.getInstanceParameterLoan().getBalloon_payment_days_delta() == null ? null : request.getInstanceParameterLoan().getBalloon_payment_days_delta();
        String balloon_payment;

        if (user.isEmpty()) {
            throw new CustomerErrorException("customer id is not already exists");
        } else if (user.get().getStatus() != CustomerStatusType.ACTIVE) {
            throw new CustomerErrorException("customer status not active");
        }

        if(balloon_payment_days_delta != null
                && balloon_payment_amount != null
        ){
            balloon_payment =
                    "               \"amortisation_method\": \"" + request.getInstanceParameterLoan().getAmortisation_method_with_balloon() + "\",\n" +
                    "               \"balloon_payment_amount\": \"" + request.getInstanceParameterLoan().getBalloon_payment_amount() + "\",\n" +
                    "               \"balloon_payment_days_delta\": \"" + request.getInstanceParameterLoan().getBalloon_payment_days_delta() + "\",\n";
        }else{
            balloon_payment =
                    "               \"amortisation_method\": \"" + request.getInstanceParameterLoan().getAmortisation_method_no_balloon() + "\",\n";
        }

        String requestId = "loan_account_creation-" + System.currentTimeMillis();
        String body = "{\n" +
                "     \"request_id\": \"" + requestId + "\",\n" +
                "     \"account\": {\n" +
                "         \"id\": \"" + uuid.toString() + "\",\n" +
                "         \"product_version_id\": \"" + request.getProductVersionId() + "\",\n" +
                "         \"permitted_denominations\": [\n" +
                "               \"" + request.getBaseCurrency() + "\"\n" +
                "         ],\n" +
                "         \"status\": \"" + "ACCOUNT_STATUS_" + AccountStatusType.PENDING.toString() + "\",\n" +
                "         \"stakeholder_ids\": [\n" +
                "               \"" + user.get().getId() + "\"\n" +
                "         ],\n" +
                "         \"instance_param_vals\": {\n" +
                "               \"fixed_interest_rate\": \"" + fixed_interest_rate + "\",\n" +
                "               \"upfront_fee\": \"" + request.getInstanceParameterLoan().getUpfront_fee() + "\",\n" +
                "               \"amortise_upfront_fee\": \"" + request.getInstanceParameterLoan().getAmortise_upfront_fee() + "\",\n" +
                "               \"fixed_interest_loan\": \"" + request.getInstanceParameterLoan().getFixed_interest_loan() + "\",\n" +
                "               \"total_term\": \"" + request.getInstanceParameterLoan().getTotal_term() + "\",\n" +
                "               \"principal\": \"" + request.getInstanceParameterLoan().getPrincipal() + "\",\n" +
                "               \"repayment_day\": \"" + request.getInstanceParameterLoan().getRepayment_day() + "\",\n" +
                "               \"deposit_account\": \"" + request.getInstanceParameterLoan().getDeposit_account() + "\",\n" +
                "               \"variable_rate_adjustment\": \"" + request.getInstanceParameterLoan().getVariable_rate_adjustment() + "\",\n" +
                "               \"loan_start_date\": \"" + loanStartDate + "\",\n" +
                "               \"interest_accrual_rest_type\": \"" + request.getInstanceParameterLoan().getInterest_accrual_rest_type() + "\",\n" +
                "               \"repayment_holiday_impact_preference\": \"" + request.getInstanceParameterLoan().getRepayment_holiday_impact_preference() + "\",\n" +
                                balloon_payment +
                "               \"capitalise_late_repayment_fee\": \"" + request.getInstanceParameterLoan().getCapitalise_late_repayment_fee() + "\"\n" +
                "         }\n" +
                "     }\n" +
                "}";

        ResponseEntity<String> responseEntity = thoughtMachineApiClient.post(getTMAccountApiUrl(), body, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CustomerErrorException("create loan account failed");
        }
        details.setId(UUID.randomUUID().toString());
        details.setSourceOfFunds(request.getAccountAdditionalDetails().getSourceOfFunds());
        details.setWorkPlace(request.getAccountAdditionalDetails().getWorkPlace());
        details.setRangeSalaries(request.getAccountAdditionalDetails().getRangeSalaries());
        details.setPurpose(request.getAccountAdditionalDetails().getPurpose());

        account.setAccountName(request.getAccountName());
        account.setAccountNumber(acId.Generate());
        account.setStakeholderIds(uuid.toString());
        account.setStatus(AccountStatusType.PENDING);
        account.setType(AccountType.LOAN);
        account.setOpenandCloseTime(responseEntity.getBody());
        account.setAdditionalDetails(details);
        account.setCustomer(user.get());

        Set<Account> accounts = new HashSet<>();
        accounts.add(account);

        user.get().setAccounts(accounts);
        customerKYCRepository.save(user.get());
        return account;
    }

    public Account createBNPLAccount(String id, CreateAccountRequestDTO request) {
        UUID uuid = UUID.randomUUID();
        LocalDate loanStartDate = LocalDate.now();
        GenerateAccountNumber acId = new GenerateAccountNumber();
        Account account = new Account();

        Optional<CustomerKYC> user = customerKYCRepository.findById(id);
        if (user.isEmpty()) {
            throw new CustomerErrorException("customer id is not already exists");
        } else if (user.get().getStatus() != CustomerStatusType.ACTIVE) {
            throw new CustomerErrorException("customer status not active");
        }

        String requestId = "bnpl_account_creation-" + System.currentTimeMillis();
        String body = "{\n" +
                "     \"request_id\": \"" + requestId + "\",\n" +
                "     \"account\": {\n" +
                "         \"id\": \"" + uuid.toString() + "\",\n" +
                "         \"product_version_id\": \"" + request.getProductVersionId() + "\",\n" +
                "         \"permitted_denominations\": [\n" +
                "               \"" + request.getBaseCurrency() + "\"\n" +
                "         ],\n" +
                "         \"status\": \"" + "ACCOUNT_STATUS_" + AccountStatusType.OPEN.toString() + "\",\n" +
                "         \"stakeholder_ids\": [\n" +
                "               \"" + user.get().getId() + "\"\n" +
                "         ],\n" +
                "         \"instance_param_vals\": {\n" +
                "               \"loan_start_date\": \"" + loanStartDate + "\",\n" +
                "               \"principal\": \"" + request.getInstanceParameterBnpl().getPrincipal() + "\",\n" +
                "               \"deposit_account\": \"" + request.getInstanceParameterBnpl().getDeposit_account() + "\",\n" +
                "               \"total_repayment_count\": \"" + request.getInstanceParameterBnpl().getTotal_repayment_count() + "\",\n" +
                "               \"repayment_frequency\": \"" + request.getInstanceParameterBnpl().getRepayment_frequency() + "\"\n" +
                "         }\n" +
                "     }\n" +
                "}";

        ResponseEntity<String> responseEntity = thoughtMachineApiClient.post(getTMAccountApiUrl(), body, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CustomerErrorException("create bnpl account failed");
        }

        account.setAccountName(request.getAccountName());
        account.setAccountNumber(acId.Generate());
        account.setStakeholderIds(uuid.toString());
        account.setStatus(AccountStatusType.OPEN);
        account.setType(AccountType.BNPL);
        account.setOpenandCloseTime(responseEntity.getBody());
        account.setCustomer(user.get());

        Set<Account> accounts = new HashSet<>();
        accounts.add(account);

        user.get().setAccounts(accounts);
        customerKYCRepository.save(user.get());
        return account;
    }

    public Boolean updateInstanceParam(String account_number, InstanceParamLoanDTO request){
        UUID uuid = UUID.randomUUID();
        LocalDate loanStartDate = LocalDate.now();
        Double fixed_interest_rate = request.getFixed_interest_rate() == null ? configuration.getPERIODIC_INTEREST_RATE() : request.getFixed_interest_rate();
        Account account = accountRepository.findByAccountNumber(account_number);

        if (account == null) {
            throw new CustomerErrorException("account id is not already exists");
        } else if (account.getType() != AccountType.LOAN) {
            throw new CustomerErrorException("account type is not loan account");
        }

        String requestId = "update_instance_param-" + System.currentTimeMillis();
        String body = "{\n" +
                "     \"request_id\": \"" + requestId + "\",\n" +
                "     \"account_update\": {\n" +
                "         \"id\": \"" + "update-instance_" + uuid.toString() + "\",\n" +
                "         \"account_id\": \"" + account.getStakeholderIds() + "\",\n" +
                "         \"instance_param_vals_update\": {\n" +
                "               \"instance_param_vals\": {\n" +
                "                       \"fixed_interest_rate\": \"" + fixed_interest_rate + "\",\n" +
                "                       \"fixed_interest_loan\": \"" + request.getFixed_interest_loan() + "\",\n" +
                "                       \"total_term\": \"" + request.getTotal_term() + "\",\n" +
                "                       \"principal\": \"" + request.getPrincipal() + "\",\n" +
                "                       \"repayment_day\": \"" + request.getRepayment_day() + "\",\n" +
                "                       \"variable_rate_adjustment\": \"" + request.getVariable_rate_adjustment() + "\",\n" +
                "                       \"loan_start_date\": \"" + loanStartDate + "\",\n" +
                "                       \"capitalise_late_repayment_fee\": \"" + request.getCapitalise_late_repayment_fee() + "\",\n" +
                "                       \"repayment_holiday_impact_preference\": \"" + request.getRepayment_holiday_impact_preference() + "\"\n" +
                "               }\n" +
                "          }\n" +
                "     }\n" +
                "}";
        ResponseEntity<String> responseEntity = thoughtMachineApiClient.post(getTMAccountUpdateInstanceParam(), body, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CustomerErrorException("create loan account failed");
        }

        return true;
    }

    public Boolean updateAccountStatus(String account_number, String status){
        Account account = accountRepository.findByAccountNumber(account_number);

        if (account == null) {
            throw new CustomerErrorException("account is not already exists");
        }

        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account.getAccountNumber());
        queryParamMap.put("page_size", "10");
        queryParamMap.put("account_addresses", "DEFAULT");
        AccountResponseDTO accountDetail = this.getTMAccountDetail(queryParamMap);

        String requestId = "update_status_loan-account-" + System.currentTimeMillis();
        String body = "{\n" +
                "     \"request_id\": \"" + requestId + "\",\n" +
                "     \"account\": {\n" +
                "         \"name\": \"" + "loan" + "\",\n" +
                "         \"product_version_id\": \"" + accountDetail.getProduct_version_id() + "\",\n" +
                "         \"status\": \"" + "ACCOUNT_STATUS_" + AccountStatusType.valueOf(status.toUpperCase()) + "\",\n" +
                "         \"permitted_denominations\": [\n" +
                "               \"" + accountDetail.getPermitted_denominations().get(0) + "\"\n" +
                "          ],\n" +
                "         \"stakeholder_ids\": [\n" +
                "               \"" + account.getCustomer().getId() + "\"\n" +
                "          ]\n" +
                "     },\n" +
                "     \"update_mask\": {\n" +
                "         \"paths\": [\n" +
                "               \"" + "status" + "\"\n" +
                "          ]\n" +
                "     }\n" +
                "}";

        ResponseEntity<String> responseEntity = thoughtMachineApiClient.put(getTMAccountApiUrl() + "/" + account.getStakeholderIds(), body, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CustomerErrorException("create loan account failed");
        }

        account.setStatus(AccountStatusType.valueOf(status.toUpperCase()));
        account.setOpenandCloseTime(responseEntity.getBody());
        accountRepository.save(account);
        return true;
    }

    public AccountNote createNote(String accountNumber, NoteRequestDTO notes) {
        AccountNote note = new AccountNote();
        Account account = accountRepository.findByAccountNumber(accountNumber);

        note.setId(UUID.randomUUID().toString());
        note.setNote(notes.getNote());
        note.setAccount(account);
        accountNoteRepository.save(note);
        return note;
    }

    public GetAccountNoteResponseDTO getNote(Map<String, Object> queryParamMap) {
        Page<AccountNote> accountsNotesPage;
        Pageable pageable = PageRequest.of(
                Integer.parseInt(queryParamMap.get("page").toString()),
                Integer.parseInt(queryParamMap.get("page_size").toString())
        );

        if (queryParamMap.containsKey("account_number") && queryParamMap.get("account_number") != "") {
            accountsNotesPage = accountNoteRepository.findNotesByAccountId(queryParamMap.get("account_number").toString(), pageable);
        } else {
            accountsNotesPage = accountNoteRepository.findAlls(pageable);
        }
        return new GetAccountNoteResponseDTO(accountsNotesPage);

    }

    public NoteUpdateDTO updateNote(NoteUpdateDTO updateNote) {

        final Optional<AccountNote> note = accountNoteRepository.findById(updateNote.getId());

        if (!note.isPresent()) {
            throw new CustomerErrorException("Note id does not exist");
        }

        note.get().setNote(updateNote.getNote());
        accountNoteRepository.save(note.get());

        return updateNote;
    }

    public boolean deleteNote(Map<String, Object> queryParamMap) {

        final Optional<AccountNote> note = accountNoteRepository.findById(queryParamMap.get("id").toString());

        if (!note.isPresent()) {
            throw new CustomerErrorException("Note id does not exist");
        }

        accountNoteRepository.deleteAccountNote(queryParamMap.get("id").toString());

        return true;
    }

    public boolean checkStatus(String statusString) {
        boolean found = false;
        for (AccountStatusType statusType : AccountStatusType.values()) {
            if (statusString.equalsIgnoreCase(statusType.toString())) {
                found = true;
                break;
            }
        }
        return found;
    }

    public AccountCustomerResponseDTO getAllCustomerandAccounts(List<String> types, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerKYC> customer = customerKYCRepository.findCustomerByTypeAccounts(
                types.stream()
                .map(String::toUpperCase)
                .map(AccountType::valueOf)
                .collect(Collectors.toList())
                ,AccountStatusType.valueOf(status.toUpperCase())
                ,pageable
        );

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                List<String> customerIds = customer.getContent().stream()
                        .map(CustomerKYC::getId)
                        .collect(Collectors.toList());

                List<Account> accounts = accountRepository.findAllByIdCustomerInAndType(
                        customerIds
                        ,types.stream()
                            .map(String::toUpperCase)
                            .map(AccountType::valueOf)
                            .collect(Collectors.toList())
                        ,AccountStatusType.valueOf(status.toUpperCase())
                );

                List<CompletableFuture<AccountDTO>> futures = customer.getContent().stream()
                        .map(customerKYC -> CompletableFuture.supplyAsync(() -> new AccountDTO(
                                customerKYC,
                                accounts.stream()
                                        .filter(account -> account.getCustomer().getId().equals(customerKYC.getId()))
                                        .toList()
                        ), executor))
                        .toList();

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

                try {
                    allFutures.get(150, TimeUnit.MILLISECONDS);
                } catch (TimeoutException ex) {
                    throw new RuntimeException("Timeout while retrieving account data.", ex);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to retrieve account data.", ex);
                }

                List<AccountDTO> content = futures.stream()
                        .map(CompletableFuture::join)
                        .distinct()
                        .collect(Collectors.toList());

                executor.shutdown();

                return new AccountCustomerResponseDTO(customer, content);
            } catch (RuntimeException ex) {
                retryCount++;
                System.out.println("Failed to retrieve account data. Retrying... (Retry count: " + retryCount + ")");
            }
        }

        throw new RuntimeException("Failed to retrieve account data after maximum retries.");
    }

    public AccountCustomerResponseDTO searchCustomerNameAndAccountType(String name, String type, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerKYC> customer = customerKYCRepository.searchCustomerByNameAndAccountType(name
                ,AccountType.valueOf(type.toUpperCase())
                ,AccountStatusType.valueOf(status.toUpperCase())
                ,pageable);

        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                List<AccountDTO> content =  customer.getContent().stream()
                        .map(customerKYC -> CompletableFuture.supplyAsync(() -> {
                            List<Account> account = accountRepository.findAccountByIdCustomer(customerKYC.getId());
                            return new AccountDTO(
                                    customerKYC,
                                    account.stream()
                                            .filter(account1 -> account1.getType() == AccountType.valueOf(type.toUpperCase()))
                                            .filter(account1 -> account1.getStatus() == AccountStatusType.valueOf(status.toUpperCase()))
                                            .toList()
                            );
                        }))
                        .map(CompletableFuture::join)
                        .distinct()
                        .collect(Collectors.toList());

                return new AccountCustomerResponseDTO(customer, content);
            } catch (RuntimeException ex) {
                retryCount++;
                System.out.println("Failed to retrieve account data. Retrying... (Retry count: " + retryCount + ")");
            }
        }

        throw new RuntimeException("Failed to retrieve account data after maximum retries.");
    }

    public AccountCustomerResponseDTO searchCustomerNikAndAccountType(String nik, String type, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerKYC> customer = customerKYCRepository.searchCustomerByNikAndAccountType(
                nik
                ,AccountType.valueOf(type.toUpperCase())
                ,AccountStatusType.valueOf(status.toUpperCase())
                ,pageable);

        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                List<AccountDTO> content =  customer.getContent().stream()
                        .map(customerKYC -> CompletableFuture.supplyAsync(() -> {
                            List<Account> account = accountRepository.findAccountByIdCustomer(customerKYC.getId());
                            return new AccountDTO(
                                    customerKYC,
                                    account.stream()
                                            .filter(account1 -> account1.getType() == AccountType.valueOf(type.toUpperCase()))
                                            .filter(account1 -> account1.getStatus() == AccountStatusType.valueOf(status.toUpperCase()))
                                            .toList()
                            );
                        }))
                        .map(CompletableFuture::join)
                        .distinct()
                        .collect(Collectors.toList());

                return new AccountCustomerResponseDTO(customer, content);
            } catch (RuntimeException ex) {
                retryCount++;
                System.out.println("Failed to retrieve account data. Retrying... (Retry count: " + retryCount + ")");
            }
        }

        throw new RuntimeException("Failed to retrieve account data after maximum retries.");
    }

    public AccountCustomerResponseDTO searchCustomerAccountNumberAndAccountType(String accountNumber, String type, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerKYC> customer = customerKYCRepository.searchCustomerByAccountNumberAndAccountType(
                accountNumber
                ,AccountType.valueOf(type.toUpperCase())
                ,AccountStatusType.valueOf(status.toUpperCase())
                ,pageable);

        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                List<AccountDTO> content =  customer.getContent().stream()
                        .map(customerKYC -> CompletableFuture.supplyAsync(() -> {
                            List<Account> account = accountRepository.findAccountByIdCustomer(customerKYC.getId());
                            return new AccountDTO(
                                    customerKYC,
                                    account.stream()
                                            .filter(account1 -> account1.getType() == AccountType.valueOf(type.toUpperCase()))
                                            .filter(account1 -> account1.getStatus() == AccountStatusType.valueOf(status.toUpperCase()))
                                            .toList()
                            );
                        }))
                        .map(CompletableFuture::join)
                        .distinct()
                        .collect(Collectors.toList());

                return new AccountCustomerResponseDTO(customer, content);
            } catch (RuntimeException ex) {
                retryCount++;
                System.out.println("Failed to retrieve account data. Retrying... (Retry count: " + retryCount + ")");
            }
        }

        throw new RuntimeException("Failed to retrieve account data after maximum retries.");
    }

    public AccountDTO getAccountByCustomerId(String id) {
        Optional<CustomerKYC> customer = customerKYCRepository.findById(id);

        if (customer.isEmpty()) {
            throw new CustomerErrorException("customer id is not already exists");
        } else if (customer.get().getStatus() != CustomerStatusType.ACTIVE) {
            throw new CustomerErrorException("customer status not active");
        }

        List<Account> accounts = accountRepository.findAccountByIdCustomer(customer.get().getId());

        return new AccountDTO(customer.get(), accounts);
    }

    public AccountDTO getAccountByAccountNumber(String number) {
        CustomerKYC customer = accountRepository.findCustomerByAccountNumber(number);
        List<Account> accountList = new ArrayList<>();

        if (customer == null) {
            throw new CustomerErrorException("customer account number is not already exists");
        } else if (customer.getStatus() != CustomerStatusType.ACTIVE) {
            throw new CustomerErrorException("customer status not active");
        }

        accountList.add(accountRepository.findByAccountNumber(number));

        return new AccountDTO(customer, accountList);
    }

    public AccountResponseDTO getTMAccountDetail(Map<String, Object> queryParamMap) {
        Account account = accountRepository.findByAccountNumber(queryParamMap.get("account_number").toString());

        if(account.getType() == AccountType.LOAN || account.getType() == AccountType.BNPL){
            queryParamMap.put("fields_to_include", "INCLUDE_FIELD_DERIVED_INSTANCE_PARAM_VALS");
        }

        queryParamMap.put("account_ids", account.getStakeholderIds());
        queryParamMap.remove("account_number");

        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

        ResponseEntity<AccountResponseDTO> response = thoughtMachineApiClient.get(
                configuration.getThoughtMachineApiServer() + Endpoint.ACCOUNTS.endPoint + "/" + account.getStakeholderIds()
                , queryString, AccountResponseDTO.class
        );

        return response.getBody();
    }

    public ScheduleAssocDTO getTMAccountScheduleAssoc(Map<String, Object> queryParamMap) {
        Account account = accountRepository.findByAccountNumber(queryParamMap.get("account_number").toString());

        queryParamMap.put("account_id", account.getStakeholderIds());
        queryParamMap.remove("account_number");
        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

        ResponseEntity<ScheduleAssocDTO> response = thoughtMachineApiClient.get(getTMScheduleAssocApiUrl(), queryString, ScheduleAssocDTO.class);

        return response.getBody();
    }

    public JobsDTO getTMScheduleJob(Map<String, Object> queryParamMap) {
        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

        ResponseEntity<JobResponseDTO> response = thoughtMachineApiClient.get(getTMScheduleJobApiUrl(), queryString, JobResponseDTO.class);

        return Objects.requireNonNull(response.getBody()).getJobs()
                .stream().min((p2, p1) -> p1.getPublish_timestamp().compareTo(p2.getPublish_timestamp()))
                .orElse(null);
    }

    public ScheduleResponseDTO getTMAccountSchedule(Map<String, Object> queryParamMap) throws ExecutionException, InterruptedException {
        ScheduleAssocDTO scheduleList = getTMAccountScheduleAssoc(queryParamMap);
        List<String> ids = scheduleList.getAccount_schedule_assocs().stream()
                .map(AssocDTO::getSchedule_id)
                .toList();

        String queryParam = ids.stream()
                .map(id -> "ids=" + id)
                .collect(Collectors.joining("&"));

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        int retryCount = 0;
        while (retryCount < MAX_RETRIES){
            try {
                CompletableFuture<ResponseEntity<ScheduleResponseDTO>> scheduleResponseDTO = CompletableFuture.supplyAsync(() -> {
                    ResponseEntity<ScheduleResponseDTO> response = thoughtMachineApiClient.get(getTMScheduleApiUrl(), queryParam, ScheduleResponseDTO.class);

                    List<CompletableFuture<Void>> jobFutures = Objects.requireNonNull(response.getBody()).getSchedules().values().stream()
                            .map(scheduleDTO -> CompletableFuture.supplyAsync(() -> {
                                        queryParamMap.put("schedule_id", scheduleDTO.getId());
                                        return this.getTMScheduleJob(queryParamMap);
                                    }, executor)
                                    .thenAccept(scheduleDTO::setJobsDTO))
                            .toList();

                    CompletableFuture<Void> allJobsFuture = CompletableFuture.allOf(jobFutures.toArray(new CompletableFuture[0]));

                    allJobsFuture.join();

                    return response;
                }, executor);

                ScheduleResponseDTO schedule = scheduleResponseDTO.get().getBody();

                executor.shutdown();

                return schedule;
            }catch (RuntimeException ex) {
                retryCount++;
                System.out.println("Failed to retrieve account data. Retrying... (Retry count: " + retryCount + ")");
            }
        }

        throw new RuntimeException("Failed to retrieve account data after maximum retries.");
    }

    private String getTMAccountApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.ACCOUNTS.endPoint;
    }

    private String getTMScheduleAssocApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.SCHEDULE_ASSOC.endPoint;
    }

    private String getTMScheduleApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.SCHEDULE.endPoint;
    }

    private String getTMScheduleJobApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.JOBS.endPoint;
    }

    private String getTMAccountUpdateInstanceParam() {
        return configuration.getThoughtMachineApiServer() + Endpoint.ACCOUNT_UPDATE_INSTANCE_PARAMETER.endPoint;
    }
}
