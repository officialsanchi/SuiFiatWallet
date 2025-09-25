package com.clyrafy.wallet.transaction.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class DepositDto {

    private BigDecimal amount;
    private String recipientEmail;
}
