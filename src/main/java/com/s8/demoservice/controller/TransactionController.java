package com.s8.demoservice.controller;

import com.s8.demoservice.dto.TransactionRequestDTO;
import com.s8.demoservice.service.RequestServiceAsync;
import com.s8.demoservice.service.TransactionService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Hidden
@RestController
@CrossOrigin
@RequestMapping(value = "/transaction")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private RequestServiceAsync requestServiceAsync;

    @Hidden
    @GetMapping(value = "/detail/expenses")
    public CompletableFuture<ResponseEntity<?>> getTransactionExpensesDetail(@RequestParam String account_number
            , @RequestParam(required = false, defaultValue = "100") int page_size
            , @RequestParam(required = false, defaultValue = "ORDER_BY_DESC") String order_by_direction
            , @RequestParam(required = false, defaultValue = "") Instant start_time
            , @RequestParam(required = false, defaultValue = "") Instant end_time
            , @RequestParam(required = false, defaultValue = "live") String date_time_range
            , @RequestParam(required = false, defaultValue = "") String page_token){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("order_by_direction", order_by_direction);
        queryParamMap.put("page_token", page_token);

        if(date_time_range.equalsIgnoreCase("live")){
            queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfMonth( 1 ).toInstant());
            queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        } else if (date_time_range.equalsIgnoreCase("annualy")) {
            queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfYear( 1 ).toInstant());
            queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        }else {
            queryParamMap.put("start_time", start_time);
            queryParamMap.put("end_time", end_time);
        }

        return requestServiceAsync.processRequest(() -> transactionService.getTransactionExpensesDetail(queryParamMap));
    }

    @Hidden
    @GetMapping(value = "/detail/total-expenses/{id_customer}")
    public CompletableFuture<ResponseEntity<?>> getTransactionTotalExpensesDetail(@PathVariable String id_customer
            , @RequestParam(required = false, defaultValue = "100") int page_size
            , @RequestParam(required = false, defaultValue = "ORDER_BY_DESC") String order_by_direction
            , @RequestParam(required = false, defaultValue = "ACCOUNT_NUMBER") String GROUP_BY
            , @RequestParam(required = false, defaultValue = "") Instant start_time
            , @RequestParam(required = false, defaultValue = "") Instant end_time
            , @RequestParam(required = false, defaultValue = "annualy") String date_time_range
            , @RequestParam(required = false, defaultValue = "") String page_token){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("id_customer", id_customer);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("order_by_direction", order_by_direction);
        queryParamMap.put("GROUP_BY", GROUP_BY);
        queryParamMap.put("page_token", page_token);

        if(date_time_range.equalsIgnoreCase("live")){
            queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfMonth( 1 ).toInstant());
            queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        } else if (date_time_range.equalsIgnoreCase("annualy")) {
            queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfYear( 1 ).toInstant());
            queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        }else {
            queryParamMap.put("start_time", start_time);
            queryParamMap.put("end_time", end_time);
        }

        return requestServiceAsync.processRequest(() -> transactionService.getTransactionTotalExpensesDetail(queryParamMap));
    }

    @Hidden
    @GetMapping(value = "/detail/incomes")
    public CompletableFuture<ResponseEntity<?>> getTransactionIncomesDetail(@RequestParam String account_number
            , @RequestParam(required = false, defaultValue = "100") int page_size
            , @RequestParam(required = false, defaultValue = "ORDER_BY_DESC") String order_by_direction){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("order_by_direction", order_by_direction);
        queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfMonth( 1 ).toInstant());
        queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        return requestServiceAsync.processRequest(() -> transactionService.getTransactionIncomesDetail(queryParamMap));
    }

    @Hidden
    @GetMapping(value = "/detail/mutation")
    public CompletableFuture<ResponseEntity<?>> getTransactionMutationsDetail(@RequestParam String account_number
            , @RequestParam(required = false, defaultValue = "100") int page_size
            , @RequestParam(required = false, defaultValue = "ORDER_BY_DESC") String order_by_direction
            , @RequestParam(required = false, defaultValue = "") String page_token){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("page_token", page_token);
        queryParamMap.put("order_by_direction", order_by_direction);
        queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfMonth( 1 ).toInstant());
        queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        return requestServiceAsync.processRequest(() -> transactionService.getTransactionMutationDetail(queryParamMap));
    }

    @Hidden
    @GetMapping(value = "/ledger-transaction/{id_customer}")
    public CompletableFuture<ResponseEntity<?>> getLedgerTransaction(@PathVariable String id_customer
            , @RequestParam(required = false, defaultValue = "15") int page_size
            , @RequestParam(required = false, defaultValue = "ORDER_BY_DESC") String order_by_direction
            , @RequestParam(required = false, defaultValue = "") String page_token
            , @RequestParam(required = false, defaultValue = "false") boolean grouping
            , @RequestParam(required = false, defaultValue = "saving") String type
            , @RequestParam(required = false, defaultValue = "live") String date_time_range
            , @RequestParam(required = false, defaultValue = "") Instant start_time
            , @RequestParam(required = false, defaultValue = "") Instant end_time){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("id_customer", id_customer);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("page_token", page_token);
        queryParamMap.put("order_by_direction", order_by_direction);
        queryParamMap.put("grouping", grouping);
        queryParamMap.put("type", type);
        if(date_time_range.equalsIgnoreCase("live")){
            queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfMonth( 1 ).toInstant());
            queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        } else if (date_time_range.equalsIgnoreCase("annualy")) {
            queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfYear( 1 ).toInstant());
            queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        }else if(date_time_range.isEmpty()){
            queryParamMap.put("start_time", start_time);
            queryParamMap.put("end_time", end_time);
        }else{
            queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfMonth( 1 ).toInstant());
            queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());
        }
        return requestServiceAsync.processRequest(() -> transactionService.getLedger(queryParamMap));
    }

    @Hidden
    @GetMapping(value = "/instruction-details")
    public CompletableFuture<ResponseEntity<?>> getTransactionInstructionDetails(@RequestParam String account_number
            , @RequestParam(required = false, defaultValue = "100") int page_size
            , @RequestParam(required = false, defaultValue = "ORDER_BY_DESC") String order_by_direction){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("order_by_direction", order_by_direction);
        queryParamMap.put("start_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).withDayOfMonth(1).toInstant());
        queryParamMap.put("end_time", ZonedDateTime.from(Instant.now().atZone(ZoneId.of( "Asia/Jakarta" ))).toInstant());

        return requestServiceAsync.processRequest(() -> transactionService.getTransactionInstruction(queryParamMap));
    }

    @Hidden
    @PostMapping(value = "/{method}")
    public CompletableFuture<ResponseEntity<?>> postTransaction(@RequestBody TransactionRequestDTO body, @PathVariable String method){
        return requestServiceAsync.processRequest(() -> transactionService.postingTransaction(method, body));
    }
}
