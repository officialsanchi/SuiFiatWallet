package com.clyrafy.wallet.enduser.services.impl;

import com.clyrafy.wallet.commons.exceptions.WrongWalletException;
import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
import com.clyrafy.wallet.enduser.dtos.requests.UpdateEndUserDetailsRequest;
import com.clyrafy.wallet.enduser.dtos.responses.UpdateEndUserDetailsResponse;
import com.clyrafy.wallet.enduser.services.EndUserService;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.data.repositories.OrganizationRespository;
import com.clyrafy.wallet.transaction.dtos.request.*;
import com.clyrafy.wallet.transaction.dtos.responses.BalanceResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import com.clyrafy.wallet.wallet.data.models.WalletBalance;
import com.clyrafy.wallet.wallet.data.repositories.WalletBalanceRepository;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.transaction.service.interfaces.TransactionService;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
import com.clyrafy.wallet.wallet.exceptions.WalletNotFoundException;
import com.clyrafy.wallet.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EndUserServiceImpl implements EndUserService {

    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final OrganizationRespository organizationRepository;
    private final EndUserRepository endUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletBalanceRepository walletBalanceRepository;

    @Override
    public TransactionResponse endUserFiatDeposit(FiatDepositRequest request) {
        if (request.getWalletType() == WalletType.SUI) {
            throw new WrongWalletException("Use endUserSuiDeposit for SUI deposits");
        }
        log.info("Processing fiat deposit for endUserId: {}", request.getRecipientIdentifier());
        return transactionService.fiatDeposit(request);
    }

//    @Override
//    public TransactionResponse endUserSuiDeposit(SuiDepositRequest request) {
//        if (request.getWalletType() == WalletType.NGN) {
//            throw new WrongWalletException("Use endUserFiatDeposit for NGN deposits");
//        }
//        log.info("Processing SUI deposit for endUserId: {}", request.getUserId());
//        return transactionService.suiDeposit(request);
//    }

    @Override
    public TransactionResponse withdraw(WithdrawRequest request) {
        log.info("Processing withdrawal for endUserId: {}, walletType: {}", request.getValue(), request.getWalletType());
        return switch (request.getWalletType()) {
            case GHS -> null;
            case KES -> null;
            case ZAR -> null;
            case XAF -> null;
            case XOF -> null;
            case EGP -> null;
            case MAD -> null;
            case TZS -> null;
            case UGX -> null;
            case SDG -> null;
            case DZD -> null;
            case ETB -> null;
            case RWF -> null;
            case MWK -> null;
            case ZMW -> null;
            case BWP -> null;
            case MZN -> null;
            case LRD -> null;
            case SLL -> null;
            case GMD -> null;
            case MRU -> null;
            case SCR -> null;
            case USD -> null;
            case EUR -> null;
            case GBP -> null;
            case JPY -> null;
            case CNY -> null;
            case INR -> null;
            case CAD -> null;
            case AUD -> null;
            case BRL -> null;
            case MXN -> null;
            case CHF -> null;
            case SEK -> null;
            case NOK -> null;
            case AED -> null;
            case SAR -> null;
            case TRY -> null;
            case KRW -> null;
            case SUI -> transactionService.suiWithdraw((SuiWithdrawRequest) request);
            case NGN -> transactionService.fiatWithdraw((FiatWithdrawRequest) request);
            case BTC -> null;
            case ETH -> null;
            case USDT -> null;
            case USDC -> null;
            case BNB -> null;
            case SOL -> null;
            case MATIC -> null;
            case XRP -> null;
            case TRX -> null;
        };
    }

    @Override
    public TransactionResponse endUserP2PFiatDeposit(P2PDepositRequest request) {
        return transactionService.p2pFiatDeposit(request);
    }

//    @Override
//    public TransactionResponse endUserP2PSuiDeposit(P2PDepositRequest request) {
//        return transactionService.p2pSuiDeposit(request);
//    }

    @Override
    public BalanceResponse checkBalance(UUID endUserId, WalletType type) {
        Wallet wallet = walletBalanceRepository.findWalletByEndUserIdAndCurrency(endUserId, type)
                .orElseThrow(() -> new WalletNotFoundException(
                        type + " wallet not found for end user: " + endUserId));

        WalletBalance balance = walletBalanceRepository.findByWalletIdAndCurrencyType(wallet.getId(), type)
                .orElseThrow(() -> new WalletNotFoundException(
                        type + " balance not found in wallet: " + wallet.getId()));

        log.info("Checked balance for endUserId: {}, walletType: {}, balance: {}",
                endUserId, type, balance.getBalance());

        return new BalanceResponse(wallet.getVirtualAccountNum(), balance.getBalance());
    }


    @Override
    public List<TransactionResponse> getTransactionHistory(UUID endUserId) {
        List<Wallet> wallets = walletRepository.findAllByEndUserId(endUserId);
        if (wallets.isEmpty()) {
            throw new WalletNotFoundException("No wallets found for end user: " + endUserId);
        }
        return transactionService.getTransactionHistoryForWallets(wallets);
    }

    @Override
    @Transactional
    public Wallet linkWalletToOrganization(UUID endUserId, UUID organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found: " + organizationId));

        Wallet wallet = walletRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new WalletNotFoundException("NGN wallet not found for end user: " + endUserId));

        wallet.setOrganization(organization);
        Wallet saved = walletRepository.save(wallet);
        log.info("Linked wallet [{}] to organization [{}]", wallet.getId(), organizationId);
        return saved;
    }

    @Transactional
    @Override
    public UpdateEndUserDetailsResponse updateEndUserDetails(UpdateEndUserDetailsRequest request) {
        if (request.getPin() != null && !request.getPin().equals(request.getConfirmPin())) {
            throw new IllegalArgumentException("PIN and confirm PIN do not match");
        }

        EndUser endUser = endUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("End user not found with email: " + request.getEmail()));

        if (request.getFullName() != null) {
            endUser.setFullName(request.getFullName());
        }

        if (request.getPin() != null) {
            endUser.setPasswordHash(passwordEncoder.encode(request.getConfirmPin()));
        }

        endUser.setUpdatedAt(LocalDateTime.now());
        EndUser saved = endUserRepository.save(endUser);

        return new UpdateEndUserDetailsResponse(
                saved.getId().toString(),
                saved.getFullName(),
                saved.getEmail(),
                "End user details updated successfully"
        );
    }

}
