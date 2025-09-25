package com.clyrafy.wallet.wallet.service.impl;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
import com.clyrafy.wallet.enduser.dtos.requests.CreateWalletForEndUserRequest;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.repositories.OrganizationRespository;
import com.clyrafy.wallet.org.data.repositories.UserRepository;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletAccess;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletId;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.data.models.WalletBalance;
import com.clyrafy.wallet.wallet.data.repositories.OrgUserWalletAccessRepository;
import com.clyrafy.wallet.wallet.data.repositories.WalletBalanceRepository;
import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
import com.clyrafy.wallet.wallet.enums.WalletOwnerType;
import com.clyrafy.wallet.wallet.exceptions.WalletNotFoundException;
import com.clyrafy.wallet.wallet.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final EndUserRepository endUserRepository;
    private final OrganizationRespository organizationRespository;
    private final UserRepository userRepository;
    private final OrgUserWalletAccessRepository orgUserWalletAccessRepository;
    private final WalletBalanceRepository walletBalanceRepository;


//    @Transactional
//    @Override
//    public Wallet createWalletForOrganization(UUID organizationId, UUID creatorOrgUserId) {
//        log.info("Starting wallet creation for organization {}", organizationId);
//
//        var existingWalletOpt = walletRepository.findByOrganizationId(organizationId);
//        if (existingWalletOpt.isPresent()) {
//            log.info("Organization {} already has a wallet: {}", organizationId, existingWalletOpt.get().getId());
//            return existingWalletOpt.get();
//        }
//
//        Organization organization = organizationRespository.findOrganizationById(organizationId);
//        if (organization == null) {
//            throw new IllegalArgumentException("Organization not found: " + organizationId);
//        }
//
//        User creatorUser = userRepository.findById(creatorOrgUserId)
//                .orElseThrow(() -> new IllegalArgumentException("Creator user not found: " + creatorOrgUserId));
//
//        Wallet wallet = Wallet.builder()
//                .organization(organization)
//                .virtualAccountNum(generateVirtualAccountNumber(organization, creatorUser))
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        Set<WalletBalance> balances = initializeBalances(wallet, WalletType.NGN, WalletType.SUI);
////        wallet.setBalances(initializeBalances(wallet, WalletType.NGN, WalletType.SUI));
//        balances.forEach(balance -> balance.setWallet(wallet));
//        wallet.setBalances(balances);
//
//        Wallet savedWallet = walletRepository.save(wallet);
//        log.info("Created wallet {} for organization {}", savedWallet.getId(), organizationId);
//
//        grantOrgUserAccess(savedWallet.getId(), creatorOrgUserId, true);
//
//        if (organization.getWallets() == null) {
//            organization.setWallets(new HashSet<>());
//        }
//        organization.getWallets().add(savedWallet);
//        organizationRespository.saveAndFlush(organization);
//
//        log.info("Wallet {} linked to organization {}", savedWallet.getId(), organization.getId());
//
//        return savedWallet;
//    }
    @Transactional
    @Override
    public Wallet createWalletForOrganization(UUID organizationId, UUID creatorOrgUserId, WalletType walletType) {
        log.info("Starting wallet creation for organization {}", organizationId);

        var existingWalletOpt = walletRepository.findByOrganizationId(organizationId);
        if (existingWalletOpt.isPresent()) {
            log.info("Organization {} already has a wallet: {}", organizationId, existingWalletOpt.get().getId());
            return existingWalletOpt.get();
        }

        Organization organization = organizationRespository.findOrganizationById(organizationId);
        if (organization == null) {
            throw new IllegalArgumentException("Organization not found: " + organizationId);
        }

        User creatorUser = userRepository.findById(creatorOrgUserId)
                .orElseThrow(() -> new IllegalArgumentException("Creator user not found: " + creatorOrgUserId));

        Wallet wallet = Wallet.builder()
                .organization(organization)
                .virtualAccountNum(generateVirtualAccountNumber(organization, creatorUser))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .walletType(walletType)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Created wallet {} for organization {}", savedWallet.getId(), organizationId);

        Set<WalletBalance> balances = initializeBalances(savedWallet, WalletType.NGN, WalletType.SUI);
        balances.forEach(balance -> {
            balance.setWallet(savedWallet);
            walletBalanceRepository.save(balance);
        });
        savedWallet.setBalances(balances);

        grantOrgUserAccess(savedWallet.getId(), creatorOrgUserId, true);

        if (organization.getWallets() == null) {
            organization.setWallets(new HashSet<>());
        }
        organization.getWallets().add(savedWallet);
        organizationRespository.saveAndFlush(organization);

        log.info("Wallet {} linked to organization {}", savedWallet.getId(), organization.getId());

        return savedWallet;
    }



    private String generateVirtualAccountNumber(Organization org, User creator) {
        String phone = creator.getPhoneNumber();
        String base = phone.substring(Math.max(phone.length() - 10, 0));

        String prefix = org.getOrgId().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (prefix.length() > 6) {
            prefix = prefix.substring(0, 6);
        }

        String candidate = prefix + base;

        if (walletRepository.existsByVirtualAccountNum(candidate)) {
            throw new IllegalStateException(
                    "Virtual account number already exists. Please use a different phone number."
            );
        }

        return candidate;
    }

//    @Transactional
//    @Override
//    public Wallet createWalletsForEndUser(CreateWalletForEndUserRequest request) {
//        EndUser endUser = endUserRepository.findById(request.getEndUser().getId())
//                .orElseThrow(() -> new IllegalArgumentException("End user not found: " + request.getEndUser().getId()));
//
//        Optional<Wallet> existingWallet = walletRepository.findByEndUserId(endUser.getId());
//        if (existingWallet.isPresent()) {
//            log.warn("EndUser {} already has a wallet. Skipping creation.", endUser.getId());
//            return existingWallet.get();
//        }
//
//        Wallet wallet = Wallet.builder()
//                .endUser(endUser)
//                .virtualAccountNum(endUser.getVirtualAccountNumber())
//                .createdAt(endUser.getCreatedAt())
//                .updatedAt(endUser.getUpdatedAt())
//                .build();
//
//        wallet.setBalances(initializeBalances(wallet, WalletType.NGN, WalletType.SUI));
//
//        return walletRepository.save(wallet);
//    }

    @Transactional
    @Override
    public Wallet createWalletsForEndUser(CreateWalletForEndUserRequest request) {
        EndUser endUser = endUserRepository.findById(request.getEndUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("End user not found: " + request.getEndUser().getId()));

        Optional<Wallet> existingWallet = walletRepository.findByEndUserId(endUser.getId());
        if (existingWallet.isPresent()) {
            log.warn("EndUser {} already has a wallet. Skipping creation.", endUser.getId());
            return existingWallet.get();
        }

        Wallet wallet = Wallet.builder()
                .endUser(endUser)
                .virtualAccountNum(endUser.getVirtualAccountNumber())
                .createdAt(endUser.getCreatedAt())
                .updatedAt(endUser.getUpdatedAt())
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        Arrays.asList(WalletType.NGN, WalletType.SUI).forEach(type -> {
            WalletBalance balance = WalletBalance.builder()
                    .wallet(savedWallet)
                    .currencyType(type)
                    .balance(BigDecimal.ZERO)
                    .build();

            walletBalanceRepository.save(balance);
        });

        return savedWallet;
    }

//    @Transactional
//    @Override
//    public Wallet createWalletsForEndUser(CreateWalletForEndUserRequest request) {
//        EndUser endUser = endUserRepository.findById(request.getEndUser().getId())
//                .orElseThrow(() -> new IllegalArgumentException("End user not found: " + request.getEndUser().getId()));
//
//        Optional<Wallet> existingWallet = walletRepository.findByEndUserId(endUser.getId());
//        if (existingWallet.isPresent()) {
//            log.warn("EndUser {} already has a wallet. Skipping creation.", endUser.getId());
//            return existingWallet.get();
//        }
//
//        Wallet wallet = Wallet.builder()
//                .endUser(endUser)
//                .virtualAccountNum(endUser.getVirtualAccountNumber())
//                .createdAt(endUser.getCreatedAt())
//                .updatedAt(endUser.getUpdatedAt())
//                .build();
//
//        Set<WalletBalance> balances = initializeBalances(wallet, WalletType.NGN, WalletType.SUI);
//        balances.forEach(balance -> balance.setWallet(wallet));
//        wallet.setBalances(balances);
//
//        return walletRepository.save(wallet);
//    }


    @Transactional
    public WalletBalance activateCurrency(UUID walletId, WalletType currency) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        Optional<WalletBalance> existingBalance =
                walletBalanceRepository.findByWalletIdAndCurrencyType(walletId, currency);

        if (existingBalance.isPresent()) {
            return existingBalance.get();
        }

        WalletBalance newBalance = WalletBalance.builder()
                .wallet(wallet)
                .currencyType(currency)
                .balance(BigDecimal.ZERO)
                .build();

        return walletBalanceRepository.save(newBalance);
    }

    public Set<WalletType> getActiveCurrencies(UUID walletId) {
        return walletBalanceRepository.findAllByWalletId(walletId).stream()
                .map(WalletBalance::getCurrencyType)
                .collect(Collectors.toSet());
    }

    private Set<WalletBalance> initializeBalances(Wallet wallet, WalletType... types) {
        return Arrays.stream(types)
                .map(type -> WalletBalance.builder()
                        .wallet(wallet)
                        .currencyType(type)
                        .balance(BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toSet());
    }


    @Transactional
    @Override
    public OrgUserWalletAccess grantOrgUserAccess(UUID walletId, UUID orgUserId, boolean isAdmin) {
        log.info("Granting access to wallet {} for OrgUser {}", walletId, orgUserId);

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        User orgUser = userRepository.findById(orgUserId)
                .orElseThrow(() -> new IllegalArgumentException("Org user not found: " + orgUserId));

        OrgUserWalletId id = new OrgUserWalletId(orgUser.getId(), wallet.getId());
        if (orgUserWalletAccessRepository.existsById(id)) {
            log.warn("OrgUser {} already has access to wallet {}", orgUserId, walletId);
            return orgUserWalletAccessRepository.findById(id).get();
        }

        OrgUserWalletAccess access = new OrgUserWalletAccess();
        access.setId(id);
        access.setWallet(wallet);
        access.setUser(orgUser);
        access.setAdmin(isAdmin);

        OrgUserWalletAccess savedAccess = orgUserWalletAccessRepository.save(access);
        log.info("Access granted to OrgUser {} for wallet {} (isAdmin={})", orgUserId, walletId, isAdmin);

        return savedAccess;
    }

    @Transactional
    @Override
    public void revokeOrgUserAccess(UUID walletId, UUID orgUserId) {
        OrgUserWalletId id = new OrgUserWalletId(orgUserId, walletId);

        if (!orgUserWalletAccessRepository.existsById(id)) {
            throw new WalletNotFoundException("Access not found for orgUser " + orgUserId + " and wallet " + walletId);
        }

        orgUserWalletAccessRepository.deleteById(id);
        log.info("Revoked wallet {} access from orgUser {}", walletId, orgUserId);
    }

    @Override
    public List<Wallet> getOrganizationWalletsForOrganizationStaff(UUID orgUserId) {
        return orgUserWalletAccessRepository.findByUserId(orgUserId)
                .stream()
                .map(OrgUserWalletAccess::getWallet)
                .filter(wallet -> wallet.getOrganization() != null)
                .toList();
    }

    @Transactional
    @Override
    public WalletBalance updateWalletBalance(UUID walletId, WalletType currency, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));

        WalletBalance balance = walletBalanceRepository
                .findByWalletIdAndCurrencyType(walletId, currency)
                .orElseThrow(() -> new IllegalStateException(
                        "Currency " + currency + " not activated for wallet " + walletId));

        balance.setBalance(balance.getBalance().add(amount));
        return walletBalanceRepository.save(balance);
    }

    @Override
    public BigDecimal getBalance(UUID walletId, WalletType walletType) {
        return walletBalanceRepository.findByWalletIdAndCurrencyType(walletId, walletType)
                .map(WalletBalance::getBalance)
                .orElseThrow(() -> new IllegalStateException(
                        "Currency " + walletType + " not activated for wallet " + walletId));
    }

    @Override
    public Optional<Wallet> getWalletById(UUID walletId) {
        return walletRepository.findById(walletId);
    }

    @Override
    public Optional<Wallet> getWalletByOwner(UUID ownerId, WalletOwnerType ownerType) {
        return switch (ownerType) {
            case END_USER -> walletRepository.findByEndUserId(ownerId);
            case ORGANIZATION -> walletRepository.findByOrganizationId(ownerId);
        };
    }

    @Override
    public List<Wallet> getWalletByOrganizationId(UUID organizationId) {
        return walletRepository.findAllByOrganizationId(organizationId);
    }
}


