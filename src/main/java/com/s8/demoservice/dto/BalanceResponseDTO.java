package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceResponseDTO {
    private String account_id;
    private String phase;
    private List<String> denomination;
    private Double totalAmount;
    private Double total_debit;
    private Double total_credit;
    private String timestamps;

    protected String time(){
        LocalDateTime todayJakarta = LocalDateTime.now(ZoneId.of("Asia/Jakarta"));
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return dateFormat.format(todayJakarta);
    }

    public BalanceResponseDTO(String account_id, String phase, List<String> denomination, Double totalAmount, Double total_debit, Double total_credit){
        this.account_id = account_id;
        this.phase = phase;
        this.denomination = denomination;
        this.totalAmount = totalAmount;
        this.total_debit = total_debit;
        this.total_credit = total_credit;
        this.timestamps = time();
    }
}
