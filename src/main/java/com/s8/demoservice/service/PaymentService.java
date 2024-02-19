package com.s8.demoservice.service;

import com.s8.demoservice.AppConfiguration;
import com.s8.demoservice.dto.BalanceResponseDTO;
import com.s8.demoservice.dto.TransactionRequestDTO;
import com.s8.demoservice.exception.CustomerErrorException;
import com.s8.demoservice.exception.CustomerNotFound;
import com.s8.demoservice.model.Account;
import com.s8.demoservice.model.enums.Endpoint;
import com.s8.demoservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    Logger logger = LoggerFactory.getLogger(PaymentService.class);
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AppConfiguration configuration;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private ThoughtMachineApiClient thoughtMachineApiClient;

    public void repayment(TransactionRequestDTO transactionRequest){
        UUID client_batch_ids = UUID.randomUUID();
        UUID client_transaction_ids = UUID.randomUUID();
        Account accountDebtor = accountRepository.findByAccountNumber(transactionRequest.getAccountNumber());
        Account accountCreditor = accountRepository.findByAccountNumber(transactionRequest.getAccountNumberCreditor());

//        check account
        if (accountDebtor.getId() == null) {
            throw new CustomerNotFound("account not found");
        }

        Map<String, Object> queryParamMapForGetBalance = new HashMap<>();
        queryParamMapForGetBalance.put("account_number", accountDebtor.getAccountNumber());
        queryParamMapForGetBalance.put("page_size", "10");
        queryParamMapForGetBalance.put("account_addresses", "DEFAULT");

//        get denomination
        BalanceResponseDTO denom = balanceService.getBalance(queryParamMapForGetBalance);

//        post instruction
        String requestId = "posting-transaction-repayment" + System.currentTimeMillis();
        String body = "{\n" +
                "     \"request_id\": \"" + requestId + "\",\n" +
                "     \"posting_instruction_batch\": {\n" +
                "         \"client_id\": \"" + "AsyncCreatePostingInstructionBatch" + "\",\n" +
                "         \"client_batch_id\": \"" + client_batch_ids + "\",\n" +
                "         \"posting_instructions\": [{\n" +
                "               \"client_transaction_id\": \"" + client_transaction_ids + "\",\n" +
                "               \"transfer\": {\n" +
                "                   \"amount\": \"" + transactionRequest.getAmount() + "\",\n" +
                "                   \"denomination\": \"" + denom.getDenomination() + "\",\n" +
                "                   \"debtor_target_account\": {\n" +
                "                       \"account_id\": \"" + accountDebtor.getStakeholderIds() + "\"\n" +
                "                   },\n" +
                "                   \"creditor_target_account\": {\n" +
                "                       \"account_id\": \"" + accountCreditor.getStakeholderIds() + "\"\n" +
                "                   }\n" +
                "               },\n" +
                "               \"pics\": [],\n" +
                "               \"instruction_details\": {\n" +
                "                   \"transaction_code\": \"" + "Repayment_Transaction_" + System.currentTimeMillis() + "\",\n" +
                "                   \"event\": \"" + "REPAYMENT" + "\",\n" +  // Added comma here
                "                   \"description\": \"" + transactionRequest.getDescription() + "\",\n" +  // Added comma here
                "                   \"payment_date\": \"" + LocalDate.now() + "\"\n" +
                "               }\n" +
                "           }],\n" +
                "           \"batch_details\": {\n" +
                "               \"event\": \"" + "REPAYMENT" + "\"\n" +
                "           }\n" +
                "     }\n" +
                "}";

        ResponseEntity<String> responseEntity = thoughtMachineApiClient.post(getTMTransactionAsyncApiUrl(), body, String.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CustomerErrorException("payment failed");
        }
    }

    private String getTMTransactionAsyncApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.TRANSACTIONS_ASYNC.endPoint;
    }
}
