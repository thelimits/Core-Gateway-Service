package com.s8.demoservice.controller;

import com.s8.demoservice.service.BalanceService;
import com.s8.demoservice.service.RequestServiceAsync;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Hidden
@RestController
@CrossOrigin
@RequestMapping(value = "/balance")
public class BalanceController {
    @Autowired
    private BalanceService balanceService;

    @Autowired
    private RequestServiceAsync requestServiceAsync;

    @Hidden
    @GetMapping(value = "/live")
    public CompletableFuture<ResponseEntity<?>> getBalances(@RequestParam String account_number
            , @RequestParam(required = false, defaultValue = "15") int page_size
            , @RequestParam(required = false, defaultValue = "DEFAULT") String account_addresses){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("account_addresses", account_addresses);
        return requestServiceAsync.processRequest(() -> balanceService.getBalance(queryParamMap));
    }

    @Hidden
    @GetMapping(value = "/live/detail")
    public CompletableFuture<ResponseEntity<?>> getBalancesDetail(@RequestParam String account_number
            , @RequestParam(required = false, defaultValue = "15") int page_size
            , @RequestParam(required = false, defaultValue = "DEFAULT") String account_addresses){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("account_addresses", account_addresses);
        return requestServiceAsync.processRequest(() -> balanceService.getBalanceDetail(queryParamMap));
    }

    @Hidden
    @GetMapping(value = "/live/total-balance")
    public CompletableFuture<ResponseEntity<?>> getTotalBalances(@RequestParam String id_customer
            , @RequestParam(required = false, defaultValue = "15") int page_size
            , @RequestParam(required = false, defaultValue = "DEFAULT") String account_addresses){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("id_customer", id_customer);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("account_addresses", account_addresses);
        return requestServiceAsync.processRequest(() -> balanceService.getTotalBalance(queryParamMap));
    }
}
