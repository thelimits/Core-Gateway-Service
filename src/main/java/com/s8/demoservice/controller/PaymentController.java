package com.s8.demoservice.controller;

import com.s8.demoservice.dto.TransactionRequestDTO;
import com.s8.demoservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Hidden
    @PostMapping(value="/repayment/loan")
    public ResponseEntity<String> repayment(@RequestBody TransactionRequestDTO transactionRequestDTO) {
        paymentService.repayment(transactionRequestDTO);
        return new ResponseEntity<>("payment successful", HttpStatus.OK);
    }
}
