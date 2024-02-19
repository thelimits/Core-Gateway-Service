package com.s8.demoservice.repository;

import com.s8.demoservice.model.AccountAdditionalDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountAdditionalDetailsRepository extends JpaRepository<AccountAdditionalDetails, UUID> {
}
