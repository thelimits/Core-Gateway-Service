package com.s8.demoservice;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppConfiguration {
    @Value("${tm.api.server}")
    private String thoughtMachineApiServer;

    @Value("${tm.auth.token}")
    private String thoughtMachineAuthToken;

    @Value("${account.base.currency}")
    private String baseCurrency;

    @Value("${tm.emi-loan-account.product-id}")
    private String emiLoanAccountProductId;

    @Value(("${tm.personal-loan-account.product-id}"))
    private String personalLoanAccountProductId;

    @Value(("${restTemplate.timeout}"))
    private int TIMEOUT;

    @Value(("${spring.timeout-client}"))
    private int TIMEOUT_CLIENT;

    @Value(("${spring.datasource.hikari.maximumPoolSize}"))
    private int Maximum_Pool_Size;

    @Value(("${tm.balance.phase}"))
    private String phase;

    @Value(("${tm.balance.saving-account.asset}"))
    private String asset_saving_account;

    @Value(("${account.internal-account}"))
    private String internal_account;

    public Double PERIODIC_INTEREST_RATE = 1.5 / 12;
}
