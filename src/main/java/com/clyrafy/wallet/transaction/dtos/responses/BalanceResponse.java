package com.clyrafy.wallet.transaction.dtos.responses;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BalanceResponse {
    private String value;
    private BigDecimal balance;

}
