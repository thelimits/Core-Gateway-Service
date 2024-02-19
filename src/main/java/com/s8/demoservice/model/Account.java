package com.s8.demoservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s8.demoservice.model.enums.AccountStatusType;
import com.s8.demoservice.model.enums.AccountType;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name="Account")
public class Account {
    @Id
    @GeneratedValue(generator = "uuid4")
    @GenericGenerator(name = "uuid4", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "VARCHAR(255)")
    @JsonIgnore
    private String id;

    private String accountName;

    @Column(unique = true)
    private String accountNumber;

    private String stakeholderIds;

    @Enumerated(EnumType.STRING)
    private AccountStatusType status;

    @Enumerated(EnumType.STRING)
    private AccountType type;

    private String openingTimestamp;

    private String closingTimestamp;

    @OneToOne(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    @JoinColumn(name = "additional_id")
    private AccountAdditionalDetails additionalDetails;

    @ManyToOne(
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private CustomerKYC customer;

    private String convertTime(String time){
        OffsetDateTime utcDateTime = OffsetDateTime.parse(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        ZonedDateTime jakartaDateTime = utcDateTime.atZoneSameInstant(ZoneId.of("Asia/Jakarta"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String jakartaTime = formatter.format(jakartaDateTime);

        return jakartaTime;
    }

    public void setOpenandCloseTime(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(body);

            String openingTimestamp = !Objects.equals(jsonNode.get("opening_timestamp").asText(), "null")
                    ? this.convertTime(jsonNode.get("opening_timestamp").asText())
                    : null;

            String closingTimestamp = !Objects.equals(jsonNode.get("closing_timestamp").asText(), "null")
                    ? this.convertTime(jsonNode.get("closing_timestamp").asText())
                    : null;

            this.openingTimestamp = openingTimestamp;
            this.closingTimestamp = closingTimestamp;
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
    }
}
