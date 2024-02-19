package com.s8.demoservice.service;

import com.s8.demoservice.AppConfiguration;
import com.s8.demoservice.dto.*;
import com.s8.demoservice.exception.BadRequest;
import com.s8.demoservice.exception.CustomerAuthError;
import com.s8.demoservice.exception.CustomerErrorException;
import com.s8.demoservice.exception.CustomerNotFound;
import com.s8.demoservice.model.CustomerKYC;
import com.s8.demoservice.model.enums.CustomerStatusType;
import com.s8.demoservice.model.enums.Endpoint;
import com.s8.demoservice.repository.CustomerKYCRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@CacheConfig(cacheNames = {"customers", "customerStatus"})
public class CustomerService {
    Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private ModelMapper modelMapper;
    @Autowired
    private CustomerKYCRepository customerKYCRepository;
    @Autowired
    private AppConfiguration configuration;
    @Autowired
    private ThoughtMachineApiClient thoughtMachineApiClient;

    @CacheEvict(value = "customers", allEntries = true)
    public CreateCustomerRequestDTO createCustomer(CustomerKYC customer) {
        UUID uuid = UUID.randomUUID();

        Optional<CustomerKYC> user = customerKYCRepository.findByNik(customer.getNik());
        if (user.isPresent()) {
            throw new CustomerErrorException("customer id already exists");
        }

        customer.setId(uuid.toString());
        customer.setStatus(CustomerStatusType.PENDING);
        customerKYCRepository.save(customer);
        CreateCustomerRequestDTO results = new CreateCustomerRequestDTO(customer);
        return results;
    }

    @Caching(
            put = {
                    @CachePut(value = "customers", key = "#customerStatus.id")
            },
            evict = {
                    @CacheEvict(value = "customers", key = "#customerStatus.id", allEntries = true)
            }
    )
    public UpdateStatusRequestDTO updateStatus(UpdateStatusRequestDTO customerStatus) {
        if (!(customerStatus.getStatus().equalsIgnoreCase(CustomerStatusType.ACTIVE.toString()) ||
                customerStatus.getStatus().equalsIgnoreCase(CustomerStatusType.REJECTED.toString()))) {
            throw new CustomerErrorException("status body out of bounds");
        }

        final Optional<CustomerKYC> user = customerKYCRepository.findById(customerStatus.getId());

        if (!user.isPresent()) {
            throw new CustomerErrorException("customer id does not exist");
        }

        if (Objects.equals(customerStatus.getStatus(), CustomerStatusType.ACTIVE.toString())) {
            String requestId = "create-customer-" + System.currentTimeMillis();
            String body = "{\n" +
                    "     \"request_id\": \"" + requestId + "\",\n" +
                    "     \"customer\": {\n" +
                    "         \"id\": \"" + user.get().getId() + "\",\n" +
                    "         \"status\": \"" + "CUSTOMER_STATUS_" + CustomerStatusType.ACTIVE.toString() + "\",\n" +
                    "         \"customer_details\": {\n" +
                    "             \"first_name\": \"" + user.get().getFirstName() + "\",\n" +
                    "             \"last_name\": \"" + user.get().getLastName() + "\"\n" +
                    "         }\n" +
                    "     }\n" +
                    "}";

            ResponseEntity<String> responseEntity = thoughtMachineApiClient.post(getTMTransactionAsyncApiUrl(), body, String.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new CustomerErrorException("update customer status failed");
            }

            user.get().setStatus(CustomerStatusType.ACTIVE);
            customerKYCRepository.save(user.get());

            return customerStatus;
        }
        user.get().setStatus(CustomerStatusType.REJECTED);
        customerKYCRepository.save(user.get());

        return customerStatus;
    }

    @Caching(
            put = {
                    @CachePut(value = "customers", key = "#id")
            },
            evict = {
                    @CacheEvict(value = "customers", key = "#id", allEntries = true)
            }
    )
    public CustomerDetailsRequestDTO updateCustomerDetails(CustomerDetailsRequestDTO customerDetails, String id) {

        final Optional<CustomerKYC> user = customerKYCRepository.findById(id);

        if (!user.isPresent()) {
            throw new CustomerErrorException("customer id does not exist");
        }

        user.get().setFirstName(customerDetails.getFirstName());
        user.get().setLastName(customerDetails.getLastName());
        user.get().setAddress(customerDetails.getAddress());
        user.get().setDob(customerDetails.getDob());
        user.get().setMotherMaidenName(customerDetails.getMotherMaidenName());
        user.get().setEmail(customerDetails.getEmail());
        user.get().setPin(customerDetails.getPin());
        user.get().setPhoneNumber(customerDetails.getPhoneNumber());

        customerKYCRepository.save(user.get());

        return customerDetails;
    }

    @Cacheable(value = "customers", key = "{#authRequest.phoneNumber, #authRequest.pin}")
    public AuthResponseDTO authenticateCustomer(AuthenticateCustomerDTO authRequest) {
        Optional<CustomerKYC> customer = customerKYCRepository.findByPhoneNumber(authRequest.getPhoneNumber());
        if (!customer.isPresent()) {
            throw new CustomerAuthError("authentication failed");
        }
        return new AuthResponseDTO(true);
    }

    @Cacheable(value = "customers", key = "{#id}")
    public CustomerAllResponseDTO findById(String id) {
        modelMapper = new ModelMapper();
        Optional<CustomerKYC> customer = customerKYCRepository.findById(id);
        if (!customer.isPresent()) {
            throw new CustomerNotFound("customer not found by id");
        }
        CustomerKYC customerResponse = customer.get();
        return modelMapper.map(customerResponse, CustomerAllResponseDTO.class);
    }

    @Cacheable(value = "customers", key = "{#numberPhone}")
    public CustomerAllResponseDTO getByPhoneNumber(String numberPhone) {
        modelMapper = new ModelMapper();
        Optional<CustomerKYC> customer = customerKYCRepository.findByPhoneNumber(numberPhone);
        if (!customer.isPresent()) {
            throw new CustomerNotFound("get customer not found by phone number");
        }
        CustomerKYC customerResponse = customer.get();
        return modelMapper.map(customerResponse, CustomerAllResponseDTO.class);
    }

    @Cacheable(value = "customers", key = "{#numberPhoneStatus}")
    public CustomerPhoneNumberResponseDTO getStatusByPhoneNumber(String numberPhoneStatus) {
        modelMapper = new ModelMapper();
        Optional<CustomerKYC> customer = customerKYCRepository.findByPhoneNumber(numberPhoneStatus);
        if (!customer.isPresent()) {
            throw new CustomerNotFound("customer not found by phone number");
        }
        CustomerKYC customerResponse = customer.get();
        return modelMapper.map(customerResponse, CustomerPhoneNumberResponseDTO.class);
    }

    public boolean checkStatus(String statusString){
        boolean found = false;
        for (CustomerStatusType statusType : CustomerStatusType.values()) {
            if (statusString.equalsIgnoreCase(statusType.toString())) {
                found = true;
                break;
            }
        }
        return found;
    }

   @Cacheable(value = "customers", key = "{#status, #page, #size, #orderBy}")
    public GetCustomerResponseDTO getAllCustomer(Object status, int page, int size, String orderBy) {
        Page<CustomerKYC> customerPage;
        Sort sort = orderBy.split(",")[0].equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, orderBy.split(",")[1])
                : Sort.by(Sort.Direction.ASC, orderBy.split(",")[1]);
        Pageable pageable =  PageRequest.of(
                page,
                size,
                sort
        );

        if (status != null && !checkStatus(status.toString().toUpperCase())) {
            throw new BadRequest("Unexpected Status");
        }

        if (status != null) {
            customerPage = customerKYCRepository.findByStatus(CustomerStatusType.valueOf(status.toString().toUpperCase()), pageable);
        } else {
            customerPage = customerKYCRepository.findAll(pageable);
        }

        return new GetCustomerResponseDTO(customerPage);
    }

    @Cacheable(value = "customers", key = "{#status, #query, #page, #size}")
    public GetCustomerResponseDTO searchByName(Object status, String query, int page, int size) {
        Page<CustomerKYC> customerPage;
        Pageable pageable =  PageRequest.of(page, size, Sort.by("createdAt").ascending());

        if (status != null && !checkStatus(status.toString().toUpperCase())) {
            throw new BadRequest("Unexpected Status");
        }

        if (status != null) {
            customerPage = customerKYCRepository.findByNameAndStatusContainingIgnoreCase(query, CustomerStatusType.valueOf(status.toString().toUpperCase()), pageable);
        } else {
            customerPage = customerKYCRepository.findByNameAndStatusContainingIgnoreCase(query, null, pageable);
        }

        return new GetCustomerResponseDTO(customerPage);
    }

    @Cacheable(value = "customers", key = "{#status, #query, #page, #size}")
    public GetCustomerResponseDTO searchByNik(Object status, String query, int page, int size) {
        Page<CustomerKYC> customerPage;
        Pageable pageable =  PageRequest.of(page, size, Sort.by("createdAt").ascending());

        if (status != null && !checkStatus(status.toString().toUpperCase())) {
            throw new BadRequest("Unexpected Status");
        }

        if (status != null) {
            customerPage = customerKYCRepository.searchByNikAndStatus(query, CustomerStatusType.valueOf(status.toString().toUpperCase()), pageable);
        } else {
            customerPage = customerKYCRepository.searchByNikAndStatus(query, null, pageable);
        }

        return new GetCustomerResponseDTO(customerPage);
    }

    @Cacheable(value = "customers", key = "{#status, #query, #page, #size}")
    public GetCustomerResponseDTO searchByPhoneNumber(Object status, String query, int page, int size) {
        Page<CustomerKYC> customerPage;
        Pageable pageable =  PageRequest.of(page, size, Sort.by("createdAt").ascending());

        if (status != null && !checkStatus(status.toString().toUpperCase())) {
            throw new BadRequest("Unexpected Status");
        }

        if (status != null) {
            customerPage = customerKYCRepository.searchByPhoneNumberAndStatus(query, CustomerStatusType.valueOf(status.toString().toUpperCase()), pageable);
        } else {
            customerPage = customerKYCRepository.searchByPhoneNumberAndStatus(query, null, pageable);
        }

        return new GetCustomerResponseDTO(customerPage);
    }



    private String getTMTransactionAsyncApiUrl() {
        return configuration.getThoughtMachineApiServer() + Endpoint.CUSTOMERS.endPoint;
    }
}
