package com.clyrafy.wallet.wallet.service.impl;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.repositories.UserRepository;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletAccessService {

    private final UserRepository userRepository;
    private final EndUserRepository endUserRepository;
    private final WalletRepository walletRepository;

    public Object getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("User not authenticated");
        }

        String principal = authentication.getName();

        UUID userId;
        try {
            userId = UUID.fromString(principal);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Invalid authentication principal: " + principal);
        }

        return userRepository.findById(userId)
                .map(user -> (Object) user)
                .orElseGet(() -> endUserRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId)));
    }


    public void validateWalletAccess(Wallet wallet, Object currentUser) {
        if (currentUser instanceof EndUser endUser) {
            if (!endUser.isEmailVerified() || !endUser.isActive() || !wallet.getEndUser().getId().equals(endUser.getId())) {
                throw new SecurityException("Unauthorized access to wallet");
            }
        } else if (currentUser instanceof User user) {
            if (!user.isEmailVerified() || !user.isLoggedIn() || !wallet.getOrganization().getId().equals(user.getOrganization().getId())) {
                throw new SecurityException("Unauthorized access to organization wallet");
            }
        } else {
            throw new SecurityException("Invalid user type");
        }
    }


    public Wallet getWalletByCurrentUser(WalletType type) {
        Object currentUser = getCurrentUser();
        Wallet wallet;

        if (currentUser instanceof EndUser endUser) {
            wallet = walletRepository.findByEndUserId(endUser.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Wallet not found for EndUser: " + endUser.getId()));
        } else if (currentUser instanceof User user) {
            wallet = walletRepository.findByOrganizationId(user.getOrganization().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Wallet not found for Org: " + user.getOrganization().getId()));
        } else {
            throw new SecurityException("Invalid user type");
        }
        boolean hasCurrency = wallet.getBalances().stream()
                .anyMatch(balance -> balance.getCurrencyType() == type);
        if (!hasCurrency) {
            throw new EntityNotFoundException(type + " not activated for wallet: " + wallet.getId());
        }
        validateWalletAccess(wallet, currentUser);
        return wallet;
    }

}
