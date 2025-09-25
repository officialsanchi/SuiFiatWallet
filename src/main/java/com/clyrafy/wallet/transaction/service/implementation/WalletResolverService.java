package com.clyrafy.wallet.transaction.service.implementation;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.repositories.UserRepository;
import com.clyrafy.wallet.org.enums.Role;
import com.clyrafy.wallet.transaction.exceptions.UnauthorizedUserException;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletAccess;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletId;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.data.models.WalletBalance;
import com.clyrafy.wallet.wallet.data.repositories.OrgUserWalletAccessRepository;
import com.clyrafy.wallet.wallet.data.repositories.WalletBalanceRepository;
import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.exceptions.WalletNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletResolverService {

    private final WalletRepository walletRepository;
    private final WalletBalanceRepository walletBalanceRepository;
    private final OrgUserWalletAccessRepository orgUserWalletAccessRepository;
    private final EndUserRepository endUserRepository;
    private final UserRepository userRepository;

    public Object getCurrentUser() {
        log.info("Fetching current authenticated user...");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.warn("User not authenticated");
            throw new SecurityException("User not authenticated");
        }

        UUID userId = UUID.fromString(auth.getName());
        log.info("Authenticated userId: {}", userId);

        return userRepository.findById(userId)
                .<Object>map(orgUser -> {
                    log.info("Found OrgUser: {}", orgUser.getEmail());
                    return orgUser;
                })
                .or(() -> endUserRepository.findById(userId).map(endUser -> {
                    log.info("Found EndUser: {}", endUser.getEmail());
                    return endUser;
                }))
                .orElseThrow(() -> {
                    log.error("User not found for id: {}", userId);
                    return new EntityNotFoundException("User not found: " + userId);
                });
    }

    public void validateWalletAccess(Wallet wallet, Object currentUser) {
        log.info("Validating wallet access for walletId: {}", wallet.getId());

        if (currentUser instanceof EndUser endUser) {
            boolean ownsWallet = endUser.getWallets().stream()
                    .anyMatch(w -> w.getId().equals(wallet.getId()));
            log.info("EndUser owns wallet: {}", ownsWallet);

            if (!endUser.isActive() || !endUser.isEmailVerified() || !ownsWallet) {
                log.warn("EndUser not authorized for wallet {}", wallet.getId());
                throw new UnauthorizedUserException("EndUser not authorized for this wallet");
            }

        } else if (currentUser instanceof User user) {
            boolean hasAccess = orgUserWalletAccessRepository.existsById(
                    new OrgUserWalletId(user.getId(), wallet.getId())
            );
            log.info("OrgUser has access: {}", hasAccess);

            if (!user.isLoggedIn() || !user.isEmailVerified() || !hasAccess) {
                log.warn("OrgUser not authorized for wallet {}", wallet.getId());
                throw new UnauthorizedUserException("OrgUser not authorized for this wallet");
            }

        } else {
            log.warn("Unknown user type: {}", currentUser.getClass().getSimpleName());
            throw new UnauthorizedUserException("Unknown user type");
        }

        log.info("Wallet access validated successfully");
    }

    @Transactional
    public Wallet resolveWallet(String identifier, String email, String username, WalletType requestedType) {
        log.info("Resolving wallet with identifier={}, email={}, username={}, type={}",
                identifier, email, username, requestedType);

        Optional<Wallet> walletOpt;

        if (identifier != null) {

            walletOpt = walletRepository.findByVirtualAccountNum(identifier);
        }
        else if (email != null) {
            walletOpt = walletRepository.findByOrganizationEmailAndType(email, requestedType);
        }
        else if (username != null) {
            walletOpt = walletRepository.findByOrganizationUsernameAndType(username, requestedType);
        }
        else {
            throw new WalletNotFoundException("Cannot resolve wallet: no valid identifier provided");
        }

        Wallet wallet = walletOpt.orElseThrow(() ->
                new WalletNotFoundException("Wallet not found for identifier: " + identifier));

        boolean hasCurrency = wallet.getBalances().stream()
                .anyMatch(balance -> balance.getCurrencyType().equals(requestedType));

        if (!hasCurrency) {
            throw new WalletNotFoundException(
                    "No wallet of type " + requestedType +
                            " found for organization " +
                            (wallet.getOrganization() != null ? wallet.getOrganization().getId() : "unknown")
            );
        }

        log.info("Resolved wallet {} for org {} with currency {}",
                wallet.getId(),
                wallet.getOrganization() != null ? wallet.getOrganization().getId() : "N/A",
                requestedType);

        return wallet;
    }

//    public BigDecimal getWalletBalance(Wallet wallet, WalletType type) {
//        return walletBalanceRepository.findByWalletAndCurrencyType(wallet, type)
//                .map(WalletBalance::getBalance)
//                .orElse(BigDecimal.ZERO);
//    }

    public List<Wallet> getAccessibleWallets(Object currentUser, WalletType type) {
        log.info("Fetching accessible wallets for user={} and type={}", currentUser, type);

        if (currentUser instanceof EndUser endUser) {
            return walletRepository.findAllByEndUserId(endUser.getId()).stream()
                    .filter(w -> walletBalanceRepository.findByWalletIdAndCurrencyType(w.getId(), type).isPresent())
                    .toList();
        } else if (currentUser instanceof User user) {
            return orgUserWalletAccessRepository.findByUserId(user.getId()).stream()
                    .map(OrgUserWalletAccess::getWallet)
                    .filter(w -> walletBalanceRepository.findByWalletIdAndCurrencyType(w.getId(), type).isPresent())
                    .toList();
        }

        return List.of();
    }

    public Wallet getWalletByVirtualAccount(String virtualAccount, WalletType type) {
        log.info("Resolving wallet by virtual account: {}", virtualAccount);

        Wallet wallet = walletRepository.findByVirtualAccountNum(virtualAccount)
                .orElseThrow(() -> {
                    log.error("Wallet not found for virtual account: {}", virtualAccount);
                    return new WalletNotFoundException("Wallet not found: " + virtualAccount);
                });

        boolean hasCurrency = wallet.getBalances().stream()
                .anyMatch(b -> b.getCurrencyType() == type);

        log.info("Wallet has currency {}: {}", type, hasCurrency);
        if (!hasCurrency) throw new WalletNotFoundException("Currency not enabled for wallet: " + virtualAccount);

        validateWalletAccess(wallet, getCurrentUser());
        return wallet;
    }

    public Wallet getWalletByCurrentUser(WalletType type) {
        Object currentUser = getCurrentUser();
        return getAccessibleWallets(currentUser, type).stream()
                .findFirst()
                .orElseThrow(() -> new WalletNotFoundException("No wallet found for type: " + type));
    }

    public BigDecimal getWalletBalance(Wallet wallet, WalletType walletType) {
        return walletBalanceRepository
                .findByWalletIdAndCurrencyType(wallet.getId(), walletType)
                .map(WalletBalance::getBalance)
                .orElse(BigDecimal.ZERO);
    }

//    public BigDecimal getWalletBalance(Wallet wallet, WalletType type) {
//        log.info("Fetching balance for walletId={} and type={}", wallet.getId(), type);
//
//        return walletBalanceRepository.findByWalletIdAndCurrencyType(wallet.getId(), type)
//                .map(balance -> {
//                    log.info("Found balance: {}", balance.getBalance());
//                    return balance.getBalance();
//                })
//                .orElse(BigDecimal.ZERO);
//    }

    public void updateWalletBalance(Wallet wallet, WalletType type, BigDecimal amount) {
        log.info("Updating walletId={} balance by {}", wallet.getId(), amount);

        WalletBalance balance = walletBalanceRepository.findByWalletIdAndCurrencyType(wallet.getId(), type)
                .orElse(WalletBalance.builder()
                        .wallet(wallet)
                        .currencyType(type)
                        .balance(BigDecimal.ZERO)
                        .build());

        balance.setBalance(balance.getBalance().add(amount));
        walletBalanceRepository.save(balance);

        log.info("Updated balance for walletId={} new balance={}", wallet.getId(), balance.getBalance());
    }

    public String getWalletOwnerEmail(Wallet wallet) {
        log.debug("Fetching wallet owner email for walletId: {}", wallet.getId());

        if (wallet.getEndUser() != null) {
            log.debug("Wallet owned by EndUser: {}", wallet.getEndUser().getEmail());
            return wallet.getEndUser().getEmail();
        }

        if (wallet.getOrganization() != null) {
            List<OrgUserWalletAccess> accesses = orgUserWalletAccessRepository.findByWallet_Id(wallet.getId());
            if (!accesses.isEmpty()) {
                OrgUserWalletAccess access = accesses.get(0);
                log.debug("Wallet owned by OrgUser: {}", access.getUser().getEmail());
                return access.getUser().getEmail();
            }

            Optional<User> orgAdmin = userRepository.findFirstByOrganizationIdAndRole(wallet.getOrganization().getId(), Role.ORG_ADMIN);
            if (orgAdmin.isPresent()) {
                log.debug("Wallet owned by OrgAdmin (fallback): {}", orgAdmin.get().getEmail());
                return orgAdmin.get().getEmail();
            }
        }

        log.error("Wallet has no associated owner: {}", wallet.getId());
        throw new IllegalStateException("Wallet has no associated owner");
    }


}
