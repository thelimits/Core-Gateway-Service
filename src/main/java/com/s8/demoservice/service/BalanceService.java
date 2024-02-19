package com.s8.demoservice.service;

import com.s8.demoservice.AppConfiguration;
import com.s8.demoservice.dto.*;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Transactional
@CacheConfig(cacheNames = {"balances"})
public class BalanceService {
    Logger logger = LoggerFactory.getLogger(BalanceService.class);
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CustomerKYCRepository customerKYCRepository;
    @Autowired
    private ThoughtMachineApiClient thoughtMachineApiClient;
    @Autowired
    private AppConfiguration configuration;

    public BalanceResponseDTO getBalance(Map<String, Object> queryParamMap) {
        ArrayList<String> denomList = new ArrayList<>();
        String account_id = null;
        String denom = null;
        Double totalDebit;
        Double totalCredit;
        Double totalAmount;

        Account account = accountRepository.findByAccountNumber(queryParamMap.get("account_number").toString());

        queryParamMap.put("account_ids", account.getStakeholderIds());
        queryParamMap.remove("account_number");

        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

        ResponseEntity<AccountBalanceDTO> response = thoughtMachineApiClient.get(getTMBalanceApiUrl(), queryString, AccountBalanceDTO.class);

        Map<String, Double> balanceMap = new HashMap<>();
        Map<String, Double> debitMap = new HashMap<>();
        Map<String, Double> creditMap = new HashMap<>();

        for (BalanceDTO balance : Objects.requireNonNull(response.getBody()).getBalances()) {
            if (!(balance.getPhase().equals(configuration.getPhase()) && balance.getAsset().equals(configuration.getAsset_saving_account()))) {
                continue;
            }

            String accountAddress = balance.getAccount_address();
            account_id = balance.getAccount_id();
            denomList.add(balance.getDenomination());
            Double debit = balance.getTotal_debit();
            Double credit = balance.getTotal_credit();
            Double amount = balance.getAmount();

            if (balanceMap.containsKey(accountAddress) || debitMap.containsKey(accountAddress) || creditMap.containsKey(accountAddress)) {
                Double currentAmount = balanceMap.get(accountAddress);
                Double currentDebit = debitMap.get(accountAddress);
                Double currentCredit = creditMap.get(accountAddress);
                balanceMap.put(accountAddress, currentAmount + amount);
                debitMap.put(accountAddress, currentDebit + debit);
                creditMap.put(accountAddress, currentCredit + credit);
            }else {
                balanceMap.put(accountAddress, amount);
                debitMap.put(accountAddress, debit);
                creditMap.put(accountAddress, credit);
            }

        }
        totalAmount = balanceMap.get(queryParamMap.get("account_addresses").toString());
        totalDebit = debitMap.get(queryParamMap.get("account_addresses").toString());
        totalCredit = creditMap.get(queryParamMap.get("account_addresses").toString());
        return new BalanceResponseDTO(
                account_id,
                configuration.getPhase(),
                denomList,
                totalAmount,
                totalDebit,
                totalCredit
        );
    }

    private Double getAmountFromBatch(Set<Account> batch, Map<String, Object> queryParamMap) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(configuration.getMaximum_Pool_Size() + 1);

        return forkJoinPool.submit(() ->
                batch.parallelStream()
                        .mapToDouble(account -> getAmountFromTransaction(account, queryParamMap))
                        .sum()
        ).join();
    }

    private Double getAmountFromTransaction(Account account, Map<String, Object> queryParamMap) {

        if(account.getAccountNumber() != null){
            queryParamMap.put("account_number", account.getAccountNumber());
            return this.getBalance(queryParamMap).getTotalAmount();
        }

        return 0.0;
    }

    @Cacheable(value = "balances", key = "#queryParamMap['id_customer']")
    public TotalBalanceAccountDTO getTotalBalance(Map<String, Object> queryParamMap){
        Optional<CustomerKYC> customer = customerKYCRepository.findById(queryParamMap.get("id_customer").toString());

        if(customer.isEmpty()){
            throw new CustomerNotFound("customer not found");
        }

        int retryCount = 0;
        while (retryCount < 3){
            try {
                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

                List<CompletableFuture<Double>> futures = customer
                        .stream().parallel()
                        .filter(customerKYC -> customerKYC.getAccounts().stream()
                                .anyMatch(account -> account.getType() == AccountType.SAVING))
                        .map(batch -> CompletableFuture.supplyAsync(() -> getAmountFromBatch(batch.getAccounts(), queryParamMap), executor))
                        .toList();

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
                allFutures.join();

                Double totalAmount = futures.parallelStream()
                        .map(CompletableFuture::join)
                        .reduce(0.0, Double::sum);

                executor.shutdown();

                return new TotalBalanceAccountDTO(
                        customer.stream().parallel()
                                .flatMap(customerKYC -> customerKYC.getAccounts().stream())
                                .filter(account -> account.getType() == AccountType.SAVING)
                                .collect(Collectors.toList()),
                        totalAmount
                );
            }catch (RuntimeException ex) {
                retryCount++;
                System.out.println("Failed to retrieve account data. Retrying... (Retry count: " + retryCount + ")");
            }
        }
        throw new RuntimeException("Failed to retrieve account data after maximum retries.");
    }

    public AccountBalanceDTO getBalanceDetail(Map<String, Object> queryParamMap) {
        Account account = accountRepository.findByAccountNumber(queryParamMap.get("account_number").toString());

        queryParamMap.put("account_ids", account.getStakeholderIds());
        queryParamMap.remove("account_number");

        String queryString = StringUtil.convertMapToQueryParamString(queryParamMap);

        ResponseEntity<AccountBalanceDTO> response = thoughtMachineApiClient.get(getTMBalanceApiUrl(), queryString, AccountBalanceDTO.class);

        return response.getBody();
    }

    private String getTMBalanceApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.BALANCES.endPoint;
    }
}
