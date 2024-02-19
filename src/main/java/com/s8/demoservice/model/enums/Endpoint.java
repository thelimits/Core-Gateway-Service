package com.s8.demoservice.model.enums;

public enum Endpoint {
    ACCOUNTS("/v1/accounts")
    , BALANCES("/v1/balances/live")
    , TRANSACTIONS("/v1/posting-instruction-batches")
    , TRANSACTIONS_ASYNC("/v1/posting-instruction-batches:asyncCreate")
    , CUSTOMERS("/v1/customers")
    , PRODUCT("/v1/product-versions")
    , PRODUCTBATCH("/v1/product-versions:batchGet")
    , SCHEDULE_ASSOC("/v1/account-schedule-assocs")
    , SCHEDULE ("/v1/schedules:batchGet")
    , JOBS ("/v1/jobs")
    , ACCOUNT_UPDATE_INSTANCE_PARAMETER("/v1/account-updates")
    ;

    public final String endPoint;

    private Endpoint(String endPoint){
        this.endPoint = endPoint;
    }
}
