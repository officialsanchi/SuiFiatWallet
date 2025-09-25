package com.clyrafy.wallet.transaction.dtos.responses;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class WithdrawalResponse {
    private Double amount;
    private String recipientCode;
    private String reason;
}
