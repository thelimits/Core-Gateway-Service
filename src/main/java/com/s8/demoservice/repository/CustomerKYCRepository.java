package com.s8.demoservice.repository;

import com.s8.demoservice.model.CustomerKYC;
import com.s8.demoservice.model.enums.AccountStatusType;
import com.s8.demoservice.model.enums.AccountType;
import com.s8.demoservice.model.enums.CustomerStatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerKYCRepository extends JpaRepository<CustomerKYC, String> {
    Optional<CustomerKYC> findByNik(String nik);

    Optional<CustomerKYC> findByPhoneNumber(String phoneNumber);

    Optional<CustomerKYC> findByIdAndPhoneNumber(String id, String phoneNumber);

    Page<CustomerKYC> findByStatus(CustomerStatusType status, Pageable pageable);

    @Query("SELECT c FROM CustomerKYC c WHERE (:status IS NULL OR c.status = :status) " +
            "AND CONCAT(UPPER(c.firstName) , ' ' , UPPER(c.lastName)) LIKE CONCAT('%', UPPER(:name), '%')")
    Page<CustomerKYC> findByNameAndStatusContainingIgnoreCase(
            @Param("name") String name,
            @Param("status") CustomerStatusType status,
            Pageable pageable
    );

    @Query("SELECT c FROM CustomerKYC c WHERE (:status IS NULL OR c.status = :status) " +
            "AND c.nik LIKE CONCAT('%', :nik, '%')")
    Page<CustomerKYC> searchByNikAndStatus(
            @Param("nik") String nik,
            @Param("status") CustomerStatusType status,
            Pageable pageable
    );

    @Query("SELECT c FROM CustomerKYC c WHERE (:status IS NULL OR c.status = :status) " +
            "AND c.phoneNumber LIKE CONCAT('%', :phoneNumber, '%')")
    Page<CustomerKYC> searchByPhoneNumberAndStatus(
            @Param("phoneNumber") String phoneNumber,
            @Param("status") CustomerStatusType status,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c FROM CustomerKYC c JOIN c.accounts a WHERE " +
            "a.type IN (:type) " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<CustomerKYC> findCustomerByTypeAccounts(
            @Param("type") List<AccountType> type,
            @Param("status") AccountStatusType status,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c FROM CustomerKYC c JOIN c.accounts a WHERE " +
            "a.type = :type " +
            "AND CONCAT(UPPER(c.firstName) , ' ' , UPPER(c.lastName)) LIKE CONCAT('%', UPPER(:name), '%') " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<CustomerKYC> searchCustomerByNameAndAccountType(
            @Param("name") String name,
            @Param("type") AccountType type,
            @Param("status") AccountStatusType status,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c FROM CustomerKYC c JOIN c.accounts a WHERE " +
            "a.type = :type " +
            "AND c.nik LIKE CONCAT('%', :nik, '%') " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<CustomerKYC> searchCustomerByNikAndAccountType(
            @Param("nik") String nik,
            @Param("type") AccountType type,
            @Param("status") AccountStatusType status,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c FROM CustomerKYC c JOIN c.accounts a WHERE " +
            "a.type = :type " +
            "AND a.accountNumber LIKE CONCAT('%', :accountNumber, '%') " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<CustomerKYC> searchCustomerByAccountNumberAndAccountType(
            @Param("accountNumber") String accountNumber,
            @Param("type") AccountType type,
            @Param("status") AccountStatusType status,
            Pageable pageable
    );
}
