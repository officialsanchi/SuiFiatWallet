package com.clyrafy.wallet.wallet.service.impl;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
import com.clyrafy.wallet.enduser.dtos.requests.CreateWalletForEndUserRequest;
import com.clyrafy.wallet.org.data.models.Organization;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.data.repositories.OrganizationRespository;
import com.clyrafy.wallet.org.data.repositories.UserRepository;
import com.clyrafy.wallet.wallet.data.models.OrgUserWalletAccess;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.data.models.WalletBalance;
import com.clyrafy.wallet.wallet.data.repositories.OrgUserWalletAccessRepository;
import com.clyrafy.wallet.wallet.data.repositories.WalletBalanceRepository;
import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
import com.clyrafy.wallet.wallet.enums.WalletType;
import com.clyrafy.wallet.wallet.exceptions.WalletNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class WalletServiceImplTest {

    @Mock private WalletRepository walletRepository;
    @Mock private EndUserRepository endUserRepository;
    @Mock private OrganizationRespository organizationRespository;
    @Mock private UserRepository userRepository;
    @Mock private OrgUserWalletAccessRepository orgUserWalletAccessRepository;
    @Mock private WalletBalanceRepository walletBalanceRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createWalletForOrganization_shouldInitializeNgnAndSuiBalances() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Organization org = new Organization();
        org.setId(orgId);
        org.setOrgId("acme123");

        User user = new User();
        user.setId(userId);
        user.setPhoneNumber("08012345678");

        when(walletRepository.findByOrganizationId(orgId)).thenReturn(Optional.empty());
        when(organizationRespository.findOrganizationById(orgId)).thenReturn(org);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.existsByVirtualAccountNum(anyString())).thenReturn(false);

        Wallet savedWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .organization(org)
                .virtualAccountNum("ACME12345678")
                .createdAt(LocalDateTime.now())
                .build();

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);
        when(walletRepository.findById(savedWallet.getId())).thenReturn(Optional.of(savedWallet));

        when(orgUserWalletAccessRepository.existsById(any())).thenReturn(false);
        when(orgUserWalletAccessRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        when(walletBalanceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Wallet wallet = walletService.createWalletForOrganization(orgId, userId, WalletType.NGN);

        verify(walletBalanceRepository).save(argThat(b -> b.getCurrencyType() == WalletType.NGN));
        verify(walletBalanceRepository).save(argThat(b -> b.getCurrencyType() == WalletType.SUI));
        assertEquals(savedWallet, wallet);

        verify(orgUserWalletAccessRepository).save(any(OrgUserWalletAccess.class));
    }



    @Test
    void createWalletForEndUser_shouldInitializeNgnAndSuiBalances() {
        UUID endUserId = UUID.randomUUID();
        EndUser endUser = new EndUser();
        endUser.setId(endUserId);
        endUser.setVirtualAccountNumber("V123");

        when(endUserRepository.findById(endUserId)).thenReturn(Optional.of(endUser));
        when(walletRepository.findByEndUserId(endUserId)).thenReturn(Optional.empty());

        Wallet savedWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .endUser(endUser)
                .virtualAccountNum("V123")
                .createdAt(LocalDateTime.now())
                .build();
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        CreateWalletForEndUserRequest request = CreateWalletForEndUserRequest.builder()
                .endUser(endUser)
                .email("test@example.com")
                .userName("tester")
                .organization(new Organization())
                .build();

        walletService.createWalletsForEndUser(request);

        verify(walletBalanceRepository).save(argThat(balance -> balance.getCurrencyType() == WalletType.NGN));
        verify(walletBalanceRepository).save(argThat(balance -> balance.getCurrencyType() == WalletType.SUI));
    }


    @Test
    void createWalletForOrganization_shouldCreateWallet_whenNoneExists() {
        UUID orgId = UUID.randomUUID();
        UUID creatorUserId = UUID.randomUUID();

        Organization organization = Organization.builder()
                .id(orgId)
                .orgId("ORG-001")
                .build();

        User creator = User.builder()
                .id(creatorUserId)
                .phoneNumber("08012345678")
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .organization(organization)
                .virtualAccountNum("ORG0018012345678")
                .build();

        when(walletRepository.findByOrganizationId(orgId)).thenReturn(Optional.empty());
        when(organizationRespository.findOrganizationById(orgId)).thenReturn(organization);
        when(userRepository.findById(creatorUserId)).thenReturn(Optional.of(creator));
        when(walletRepository.existsByVirtualAccountNum(anyString())).thenReturn(false);

        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        // ✅ FIXED: add WalletType
        Wallet created = walletService.createWalletForOrganization(orgId, creatorUserId, WalletType.NGN);

        assertNotNull(created);
        assertEquals(wallet.getId(), created.getId());
        assertEquals(organization, created.getOrganization());
        verify(walletRepository).save(any(Wallet.class));
        verify(orgUserWalletAccessRepository).save(any(OrgUserWalletAccess.class));
    }



    @Test
    void createWalletsForEndUser_shouldReturnExistingWallet_ifAlreadyExists() {
        UUID endUserId = UUID.randomUUID();
        EndUser endUser = new EndUser();
        endUser.setId(endUserId);
        endUser.setVirtualAccountNumber("V123");

        Wallet existingWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .endUser(endUser)
                .virtualAccountNum("V123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setName("Test Org");

        when(endUserRepository.findById(endUserId)).thenReturn(Optional.of(endUser));
        when(walletRepository.findByEndUserId(endUserId)).thenReturn(Optional.of(existingWallet));

        CreateWalletForEndUserRequest request = CreateWalletForEndUserRequest.builder()
                .endUser(endUser)
                .email("test@example.com")
                .userName("tester")
                .organization(organization)
                .build();

        Wallet result = walletService.createWalletsForEndUser(request);

        assertThat(result).isEqualTo(existingWallet);
    }

    @Test
    void activateCurrency_shouldNotReinitializeNgnOrSui() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder().id(walletId).build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletBalanceRepository.findByWalletIdAndCurrencyType(walletId, WalletType.NGN))
                .thenReturn(Optional.of(WalletBalance.builder()
                        .wallet(wallet)
                        .currencyType(WalletType.NGN)
                        .balance(BigDecimal.ZERO)
                        .build()));

        WalletBalance result = walletService.activateCurrency(walletId, WalletType.NGN);

        // It should just return the existing balance, not create a new one
        assertThat(result.getCurrencyType()).isEqualTo(WalletType.NGN);
        verify(walletBalanceRepository, never()).save(argThat(b -> b.getCurrencyType() == WalletType.NGN));
    }

    @Test
    void activateCurrency_shouldAddNewCurrencyWhenExtraCurrency() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder().id(walletId).build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletBalanceRepository.findByWalletIdAndCurrencyType(walletId, WalletType.USD))
                .thenReturn(Optional.empty());

        WalletBalance savedBalance = WalletBalance.builder()
                .wallet(wallet)
                .currencyType(WalletType.USD)
                .balance(BigDecimal.ZERO)
                .build();
        when(walletBalanceRepository.save(any(WalletBalance.class))).thenReturn(savedBalance);

        WalletBalance result = walletService.activateCurrency(walletId, WalletType.USD);

        assertThat(result.getCurrencyType()).isEqualTo(WalletType.USD);
        verify(walletBalanceRepository).save(any(WalletBalance.class));
    }

    @Test
    void activateCurrency_shouldCreateBalance_whenNotExists() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder().id(walletId).build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletBalanceRepository.findByWalletIdAndCurrencyType(walletId, WalletType.NGN))
                .thenReturn(Optional.empty());

        WalletBalance savedBalance = WalletBalance.builder()
                .wallet(wallet)
                .currencyType(WalletType.NGN)
                .balance(BigDecimal.ZERO)
                .build();
        when(walletBalanceRepository.save(any(WalletBalance.class))).thenReturn(savedBalance);

        WalletBalance result = walletService.activateCurrency(walletId, WalletType.NGN);

        assertThat(result.getCurrencyType()).isEqualTo(WalletType.NGN);
        verify(walletBalanceRepository).save(any(WalletBalance.class));
    }

    @Test
    void updateWalletBalance_shouldIncreaseBalance() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder().id(walletId).build();

        WalletBalance balance = WalletBalance.builder()
                .wallet(wallet)
                .currencyType(WalletType.NGN)
                .balance(BigDecimal.TEN)
                .build();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletBalanceRepository.findByWalletIdAndCurrencyType(walletId, WalletType.NGN))
                .thenReturn(Optional.of(balance));
        when(walletBalanceRepository.save(any(WalletBalance.class))).thenReturn(balance);

        WalletBalance result = walletService.updateWalletBalance(walletId, WalletType.NGN, BigDecimal.ONE);

        assertThat(result.getBalance()).isEqualTo(BigDecimal.valueOf(11));
    }

    @Test
    void getBalance_shouldThrow_ifCurrencyNotActivated() {
        UUID walletId = UUID.randomUUID();
        when(walletBalanceRepository.findByWalletIdAndCurrencyType(walletId, WalletType.SUI))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getBalance(walletId, WalletType.SUI))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Currency SUI not activated");
    }

    @Test
    void revokeOrgUserAccess_shouldThrow_ifAccessNotExists() {
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(orgUserWalletAccessRepository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> walletService.revokeOrgUserAccess(walletId, userId))
                .isInstanceOf(WalletNotFoundException.class);
    }
}
