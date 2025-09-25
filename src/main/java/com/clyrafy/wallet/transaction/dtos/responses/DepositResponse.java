package com.clyrafy.wallet.transaction.dtos.responses;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DepositResponse {
    private Double amount;
    private String recipientCode;
    private String reason;
}
