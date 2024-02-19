package com.s8.demoservice.controller;

import com.s8.demoservice.service.ProductService;
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
@RequestMapping(value = "/product-version")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private RequestServiceAsync requestServiceAsync;

    @Hidden
    @GetMapping(value = "/details")
    public CompletableFuture<ResponseEntity<?>> getTransactionDetail(
            @RequestParam String account_number
    ){
        Map<String, Object> queryParamMap = new HashMap<>();
        queryParamMap.put("account_number", account_number);
        return requestServiceAsync.processRequest(() -> productService.getProductDetail(queryParamMap));
    }
}
