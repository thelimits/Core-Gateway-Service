package com.s8.demoservice.controller;

import com.s8.demoservice.dto.*;
import com.s8.demoservice.model.Account;
import com.s8.demoservice.model.AccountNote;
import com.s8.demoservice.service.AccountService;
import com.s8.demoservice.service.RequestServiceAsync;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@CrossOrigin
@RequestMapping(value = "/account")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private RequestServiceAsync requestServiceAsync;

    @Hidden
    @PostMapping(value = "/create/saving-account/{id}")
    public ResponseEntity<Account> createSavingAccount(@PathVariable String id, @RequestBody CreateAccountRequestDTO body) {
        return new ResponseEntity<>(accountService.createSavingoanAccount(id, body), HttpStatus.OK);
    }

    @Hidden
    @PostMapping(value = "/create/loan-account/{id}")
    public ResponseEntity<Account> createLoanAccount(@PathVariable String id, @RequestBody CreateAccountRequestDTO body){
        return new ResponseEntity<>(accountService.createLoanAccount(id, body), HttpStatus.OK);
    }

    @Hidden
    @PostMapping(value = "/create/bnpl-account/{id}")
    public ResponseEntity<Account> createBnplAccount(@PathVariable String id, @RequestBody CreateAccountRequestDTO body) {
        return new ResponseEntity<>(accountService.createBNPLAccount(id, body), HttpStatus.OK);
    }

    @Hidden
    @PostMapping(value = "/update/loan-instance-parameter/{account_number}")
    public ResponseEntity<Boolean> updateLoanInstanceParameter(@PathVariable String account_number, @RequestBody InstanceParamLoanDTO body) {
        return new ResponseEntity<>(accountService.updateInstanceParam(account_number, body), HttpStatus.OK);
    }

    @Hidden
    @PutMapping(value = "/update/loan-status/{account_number}")
    public ResponseEntity<Boolean> updateLoanStatus(@PathVariable String account_number, @RequestParam String status) {
        return new ResponseEntity<>(accountService.updateAccountStatus(account_number, status), HttpStatus.OK);
    }

//    @Hidden
//    @PutMapping(value = "/update/bnpl-status/{account_number}")
//    public ResponseEntity<Boolean> updateBnplStatus(@PathVariable String account_number, @RequestParam String status) {
//        return new ResponseEntity<>(accountService.updateAccountStatus(account_number, status), HttpStatus.OK);
//    }

    @GetMapping("/customers")
    public CompletableFuture<ResponseEntity<?>> getCustomersAndAccount(
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "15") int size) {
        return requestServiceAsync.processRequest(() -> accountService.getAllCustomerandAccounts(type, status, page, size));
    }

    @GetMapping("/customers/search/name")
    public CompletableFuture<ResponseEntity<?>> searchCustomerNameAndAccountType(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "15") int size) {
        return requestServiceAsync.processRequest(() -> accountService.searchCustomerNameAndAccountType(q, type, status, page, size));
    }

    @Hidden
    @GetMapping("/customers/search/nik")
    public CompletableFuture<ResponseEntity<?>> searchCustomerNikAndAccountType(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "15") int size) {
        return requestServiceAsync.processRequest(() -> accountService.searchCustomerNikAndAccountType(q, type, status, page, size));
    }

    @Hidden
    @GetMapping("/customers/search/account-number")
    public CompletableFuture<ResponseEntity<?>> searchCustomerAccountNumberAndAccountType(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "15") int size) {
        return requestServiceAsync.processRequest(() -> accountService.searchCustomerAccountNumberAndAccountType(q, type, status, page, size));
    }

    @Hidden
    @GetMapping(value = "/customer/{id}")
    public CompletableFuture<ResponseEntity<?>> getAccountCustomersById(@PathVariable String id) {
        return requestServiceAsync.processRequest(() -> accountService.getAccountByCustomerId(id));
    }

    @Hidden
    @GetMapping(value = "/customer/account-number/{number}")
    public CompletableFuture<ResponseEntity<?>> getAccountCustomersByAccountNumber(@PathVariable String number) {
        return requestServiceAsync.processRequest(() -> accountService.getAccountByAccountNumber(number));
    }

    @Hidden
    @GetMapping(value = "/details")
    public CompletableFuture<ResponseEntity<?>> getAccountDetail(@RequestParam String account_number
            , @RequestParam(required = false, defaultValue = "10") int page_size
            , @RequestParam(required = false, defaultValue = "DEFAULT") String account_addresses) {
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        queryParamMap.put("page_size", page_size);
        queryParamMap.put("account_addresses", account_addresses);
        return requestServiceAsync.processRequest(() -> accountService.getTMAccountDetail(queryParamMap));
    }

    @Hidden
    @GetMapping(value = "/schedule")
    public ResponseEntity<ScheduleResponseDTO> getAccountSchedule(@RequestParam(required = false, defaultValue = "null") String account_number
            , @RequestParam(required = false, defaultValue = "100") int page_size) throws ExecutionException, InterruptedException {
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        queryParamMap.put("page_size", page_size);
        return new ResponseEntity<>(accountService.getTMAccountSchedule(queryParamMap), HttpStatus.OK);
    }

    @PostMapping(value = "/create/note/{accountNumber}")
    public ResponseEntity<AccountNote> createNote(@PathVariable String accountNumber, @RequestBody NoteRequestDTO Notes) {
        return new ResponseEntity<>(accountService.createNote(accountNumber, Notes), HttpStatus.OK);
    }

    @Hidden
    @GetMapping(value = "/note")
    public CompletableFuture<ResponseEntity<?>> getNote(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size,
            @RequestParam(required = false, defaultValue = "") String account_number) {
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("page", page);
        queryParamMap.put("page_size", size);
        queryParamMap.put("account_number", account_number);
        return requestServiceAsync.processRequest(() -> accountService.getNote(queryParamMap));
    }

    @Hidden
    @PatchMapping(value = "/note")
    public ResponseEntity<NoteUpdateDTO> updateNote(@RequestBody NoteUpdateDTO noteUpdate) {
        return new ResponseEntity<>(accountService.updateNote(noteUpdate), HttpStatus.OK);
    }

    @Hidden
    @DeleteMapping(value= "/note")
    public ResponseEntity<Boolean> deleteNote(
            @RequestParam(required = false, defaultValue = "") String id) {
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("id", id);
        return new ResponseEntity<>(accountService.deleteNote(queryParamMap), HttpStatus.OK);
    }

    @Hidden
    @GetMapping(value = "check/account-status")
    public ResponseEntity<Boolean> checkAccountStatus(
            @RequestParam(required = false, defaultValue = "") String id, @RequestParam(required = false, defaultValue = "") String type){
        return new ResponseEntity<>(accountService.checkPendingStatus(id, type), HttpStatus.OK);
    }
}
