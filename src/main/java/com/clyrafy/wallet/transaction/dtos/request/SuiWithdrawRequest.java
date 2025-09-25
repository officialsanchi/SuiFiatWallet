package com.clyrafy.wallet.transaction.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuiWithdrawRequest extends WithdrawRequest {
    private String suiAddress;
}

