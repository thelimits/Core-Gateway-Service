package com.s8.demoservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostingInstructionDTO {
    private String id;
    private String client_transaction_id;
    private InboundTransactionDTO inbound_hard_settlement;
    private OutboundTransactionDTO outbound_hard_settlement;
    private TransferTransactionDTO transfer;
    private CustomInstructionDTO custom_instruction;
    @JsonIgnore
    private List<CommittedPostingDTO> committed_postings;
    private Map<String, String> instruction_details;
    private List<ContractViolationDTO> contract_violations;

    public PostingInstructionDTO(List<CommittedPostingDTO> committed_postings) {
        this.committed_postings = committed_postings;
    }
}
