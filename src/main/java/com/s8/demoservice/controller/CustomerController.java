package com.s8.demoservice.controller;

import com.s8.demoservice.AppConfiguration;
import com.s8.demoservice.dto.*;
import com.s8.demoservice.model.CustomerKYC;
import com.s8.demoservice.service.CustomerService;
import com.s8.demoservice.service.RequestServiceAsync;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin
@RequestMapping(value = "/customers")
@Api(tags = "Customer Controller", description = "Endpoint untuk mengelola data pelanggan")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private RequestServiceAsync requestServiceAsync;

    @Autowired
    private AppConfiguration configure;

    @PostMapping()
    @ApiOperation(value = "Registrasi data pelanggan", notes = "Endpoint untuk meregistrasi data pelanggan.")
    public ResponseEntity<CreateCustomerRequestDTO> createCustomer(
            @RequestBody CustomerKYC customerRequest
    ) {
        return new ResponseEntity<>(customerService.createCustomer(customerRequest), HttpStatus.OK);
    }

    @PatchMapping()
    @ApiOperation(value = "Update status pelanggan", notes = "Endpoint untuk mengupdate status pelanggan.")
    public ResponseEntity<UpdateStatusRequestDTO> updateCustomer(
            @RequestBody UpdateStatusRequestDTO customerRequest
    ) {
        return new ResponseEntity<>(customerService.updateStatus(customerRequest), HttpStatus.OK);
    }

    @PatchMapping("/details/{id}")
    @ApiOperation(value = "Update detail pelanggan", notes = "Endpoint untuk mengupdate detail pelanggan.")
    public ResponseEntity<CustomerDetailsRequestDTO> updateCustomerDetails(
            @RequestBody CustomerDetailsRequestDTO customerRequest,
            @PathVariable String id
    ) {
        return new ResponseEntity<>(customerService.updateCustomerDetails(customerRequest, id), HttpStatus.OK);
    }

    @PostMapping(value = "/auth")
    @ApiOperation(value = "Authentication", notes = "Endpoint untuk auth pelanggan.")
    public ResponseEntity<AuthResponseDTO> authenticate(
            @RequestBody AuthenticateCustomerDTO customerRequest
    ) {
        return new ResponseEntity<>(customerService.authenticateCustomer(customerRequest), HttpStatus.OK);
    }

    @GetMapping()
    @ApiOperation(value = "Mendapatkan daftar pelanggan", notes = "Endpoint untuk mendapatkan daftar pelanggan dengan berbagai opsi filter dan paginasi.")
    public CompletableFuture<ResponseEntity<?>> getCustomers(
            @ApiParam(value = "Status pelanggan value: active, pending, rejected")
            @RequestParam(required = false) String status,
            @ApiParam(value = "Nomor halaman saat melakukan paginasi (default: 0)", defaultValue = "0")
            @RequestParam(required = false, defaultValue = "0") int page,
            @ApiParam(value = "Jumlah data per halaman saat melakukan paginasi (default: 15)", defaultValue = "15")
            @RequestParam(required = false, defaultValue = "15") int size,
            @ApiParam(value = "Urutan hasil (default: desc,createdAt) value: asc,name. Format: direction,column_name (misalnya: desc,name)", defaultValue = "desc,createdAt")
            @RequestParam(required = false, defaultValue = "desc,createdAt") String order_by
    ) {
        return requestServiceAsync.processRequest(() -> customerService.getAllCustomer(status, page, size, order_by));
    }

    @GetMapping(value = "/search/name")
    @ApiOperation(value = "Mencari data pelanggan berdasarkan nama", notes = "Endpoint untuk mencari data pelanggan berdasarkan nama")
    public CompletableFuture<ResponseEntity<?>>  searchCustomersByName(
            @ApiParam(value = "Status pelanggan value: active, pending, rejected")
            @RequestParam(required = false) String status,
            @ApiParam(value = "query parameter value: nama dari pelanggan yang ingin dicari")
            @RequestParam(required = false) String q,
            @ApiParam(value = "Nomor halaman saat melakukan paginasi (default: 0)", defaultValue = "0")
            @RequestParam(required = false, defaultValue = "0") int page,
            @ApiParam(value = "Jumlah data per halaman saat melakukan paginasi (default: 15)", defaultValue = "15")
            @RequestParam(required = false, defaultValue = "15") int size
    ) {

        return requestServiceAsync.processRequest(() -> customerService.searchByName(status, q, page, size));
    }

    @GetMapping(value = "/search/nik")
    @ApiOperation(value = "Mencari data pelanggan berdasarkan nik", notes = "Endpoint untuk mencari data pelanggan berdasarkan nik")
    public CompletableFuture<ResponseEntity<?>>  searchCustomerByNik(
            @ApiParam(value = "Status pelanggan value: active, pending, rejected")
            @RequestParam(required = false) String status,
            @ApiParam(value = "query parameter value: nik dari pelanggan yang ingin dicari")
            @RequestParam(required = false) String q,
            @ApiParam(value = "Nomor halaman saat melakukan paginasi (default: 0)", defaultValue = "0")
            @RequestParam(required = false, defaultValue = "0") int page,
            @ApiParam(value = "Jumlah data per halaman saat melakukan paginasi (default: 15)", defaultValue = "15")
            @RequestParam(required = false, defaultValue = "15") int size
    ) {

        return requestServiceAsync.processRequest(() -> customerService.searchByNik(status, q, page, size));
    }

    @GetMapping(value = "/search/phone")
    @ApiOperation(value = "Mencari data pelanggan berdasarkan nomor telepon", notes = "Endpoint untuk mencari data pelanggan berdasarkan nomor telepon")
    public CompletableFuture<ResponseEntity<?>>  searchCustomerByPhoneNumber(
            @ApiParam(value = "Status pelanggan value: active, pending, rejected")
            @RequestParam(required = false) String status,
            @ApiParam(value = "query parameter value: nomor telepon dari pelanggan yang ingin dicari")
            @RequestParam(required = false) String q,
            @ApiParam(value = "Nomor halaman saat melakukan paginasi (default: 0)", defaultValue = "0")
            @RequestParam(required = false, defaultValue = "0") int page,
            @ApiParam(value = "Jumlah data per halaman saat melakukan paginasi (default: 15)", defaultValue = "15")
            @RequestParam(required = false, defaultValue = "15") int size
    ) {

        return requestServiceAsync.processRequest(() -> customerService.searchByPhoneNumber(status, q, page, size));
    }

    @GetMapping(value = "/{id}")
    @ApiOperation(value = "Mengambil data pelanggan berdasarkan id pelanggan", notes = "Endpoint untuk mengambil data pelanggan berdasarkan id pelanggan")
    public ResponseEntity<CustomerAllResponseDTO> getCustomerById(
            @ApiParam(value = "id dari pelanggan (misalnya: f077a38a-e5e9-4cec-aacd-6f72d4352a5e)")
            @PathVariable String id
    ) {
        return new ResponseEntity<>(customerService.findById(id), HttpStatus.OK);
    }

    @GetMapping(value = "/phone/{phoneNumber}")
    @ApiOperation(value = "Mengambil data pelanggan berdasarkan nomor telepon pelanggan", notes = "Endpoint untuk mengambil data pelanggan berdasarkan nomor telepon pelanggan")
    public ResponseEntity<CustomerAllResponseDTO> getCustomerByPhone(
            @ApiParam(value = "nomor telepon dari pelanggan (misalnya: 080723155639)")
            @PathVariable String phoneNumber
    ) {
        return new ResponseEntity<>(customerService.getByPhoneNumber(phoneNumber), HttpStatus.OK);
    }

    @GetMapping(value = "/phone/{phone}/status")
    @ApiOperation(value = "Mengambil status pelanggan berdasarkan nomor telepon pelanggan", notes = "Endpoint untuk mengambil status pelanggan berdasarkan nomor telepon pelanggan")
    public ResponseEntity<CustomerPhoneNumberResponseDTO> getStatusByPhone(
            @ApiParam(value = "nomor telepon dari pelanggan (misalnya: 080723155639)")
            @PathVariable String phone
    ) {
        return new ResponseEntity<>(customerService.getStatusByPhoneNumber(phone), HttpStatus.OK);
    }
}