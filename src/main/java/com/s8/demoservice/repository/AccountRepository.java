package com.s8.demoservice.repository;

import com.s8.demoservice.model.Account;
import com.s8.demoservice.model.CustomerKYC;
import com.s8.demoservice.model.enums.AccountStatusType;
import com.s8.demoservice.model.enums.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    @EntityGraph(attributePaths = "customer")
    Page<Account> findByStatus(AccountStatusType status, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Account a JOIN FETCH a.customer c" +
            " WHERE c.id = :customerId")
    List<Account> findAccountByIdCustomer(@Param("customerId") String customerId);

    @Query("SELECT DISTINCT a FROM Account a JOIN FETCH a.customer c" +
            " WHERE c.id IN (:customerId) AND a.type IN (:type) AND (:status IS NULL OR a.status = :status)")
    List<Account> findAllByIdCustomerInAndType(@Param("customerId") List<String> customerId, @Param("type") List<AccountType> type, @Param("status") AccountStatusType status);

    @Query("SELECT c FROM Account a JOIN a.customer c" +
            " WHERE a.accountNumber = :accountNumber")
    CustomerKYC findCustomerByAccountNumber(@Param("accountNumber") String accountNumber);

    Account findByAccountNumber(String accountNumber);
}
