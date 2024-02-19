package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.s8.demoservice.AppConfiguration;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionExpensesDTO {
    private AccountTransactionDTO transactionList;
    private List<TransactionDTO> transactionDTOS;
    private Double amountExpenses;
    private String description;

    public TransactionExpensesDTO(AccountTransactionDTO transactionResponse, String description, int maxPool) {
        this.description = description;
        if(!Objects.equals(transactionResponse.getPosting_instruction_batches(), null)){
            this.transactionList = transactionResponse;
            this.sumTransaction(this.transactionList, maxPool);
        }else {
            this.transactionList = new AccountTransactionDTO(Collections.emptyList(), "","");
            this.amountExpenses = 0.00;
        }
    }

    public TransactionExpensesDTO(List<TransactionDTO> transactionDTOS, String description, int maxPool){
        this.description = description;
        if(!Objects.equals(transactionDTOS, null)){
            this.transactionDTOS = transactionDTOS;
            this.sumTransaction(new AccountTransactionDTO(this.transactionDTOS), maxPool);
        }else {
            this.transactionList = new AccountTransactionDTO(Collections.emptyList(), "","");
            this.amountExpenses = 0.00;
        }
    }

    public void sumTransaction(AccountTransactionDTO transactionList, int maxPool) {
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(15, transactionList.getPosting_instruction_batches().size()));

        List<CompletableFuture<Double>> futures = transactionList.getPosting_instruction_batches()
                .parallelStream()
                .map(batch -> CompletableFuture.supplyAsync(() -> getAmountFromBatch(batch.getPosting_instructions(), maxPool), executorService))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        allFutures.join();

        Double totalAmount = futures.parallelStream()
                .map(CompletableFuture::join)
                .reduce(0.0, Double::sum);

        executorService.shutdown();

        this.amountExpenses = totalAmount;
    }

    private Double getAmountFromBatch(List<PostingInstructionDTO> batch, int maxPool) {
        ForkJoinPool forkJoinPool = new ForkJoinPool(maxPool + 1);

        return forkJoinPool.submit(() ->
                batch.parallelStream() // Process transactions in the batch concurrently
                        .mapToDouble(this::getAmountFromTransaction)
                        .sum()
        ).join();
    }

    private Double getAmountFromTransaction(PostingInstructionDTO transaction) {
        if (transaction.getInbound_hard_settlement() != null) {
            return transaction.getInbound_hard_settlement().getAmount();
        } else if (transaction.getOutbound_hard_settlement() != null) {
            return transaction.getOutbound_hard_settlement().getAmount();
        } else if (transaction.getTransfer() != null) {
            return transaction.getTransfer().getAmount();
        } else if (transaction.getCustom_instruction() != null) {
            return transaction.getCustom_instruction().getPostings().stream()
                    .mapToDouble(CommittedPostingDTO::getAmount)
                    .sum();
        }

        return 0.0;
    }

}
