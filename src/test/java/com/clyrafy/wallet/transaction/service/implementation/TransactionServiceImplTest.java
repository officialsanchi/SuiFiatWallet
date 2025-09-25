//package com.clyrafy.wallet.transaction.service.implementation;
//
//import com.clyrafy.wallet.enduser.data.models.EndUser;
//import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
//import com.clyrafy.wallet.org.data.models.Organization;
//import com.clyrafy.wallet.org.data.models.User;
//import com.clyrafy.wallet.org.data.repositories.UserRepository;
//import com.clyrafy.wallet.org.enums.OrgStatus;
//import com.clyrafy.wallet.org.enums.Role;
//import com.clyrafy.wallet.transaction.data.models.Transaction;
//import com.clyrafy.wallet.transaction.data.repositories.TransactionRepository;
//import com.clyrafy.wallet.transaction.dtos.request.*;
//import com.clyrafy.wallet.transaction.dtos.responses.BalanceResponse;
//import com.clyrafy.wallet.transaction.dtos.responses.PaystackDepositResponse;
//import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
//import com.clyrafy.wallet.transaction.enums.TransactionStatus;
//import com.clyrafy.wallet.wallet.enums.WalletType;
//import com.clyrafy.wallet.transaction.service.interfaces.BulkTransactionService;
//import com.clyrafy.wallet.transaction.service.interfaces.WithdrawalService;
//import com.clyrafy.wallet.wallet.data.models.Wallet;
//import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.math.BigDecimal;
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class TransactionServiceImplTest {
//
//    @Mock
//    private WalletRepository walletRepository;
//
//    @Mock
//    private TransactionRepository transactionRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private EndUserRepository endUserRepository;
//
//    @Mock
//    private WithdrawalService withdrawalService;
//
//    @Mock
//    private BulkTransactionService bulkTransactionService;
//
//    @Mock
//    private PaystackService fiatService;
//
//    @Mock
//    private SuiDepositPollerServiceImpl suiDepositPollerService;
//
//    @InjectMocks private TransactionServiceImpl transactionService;
//
//    private User orgAdmin;
//    private EndUser endUser;
//    private Wallet userWallet;
//    private Wallet endUserWallet;
//
//    @BeforeEach
//    void setup() {
//        Organization org = new Organization();
//        org.setId(UUID.randomUUID());
//
//        orgAdmin = new User();
//        orgAdmin.setId(UUID.randomUUID());
//        orgAdmin.setEmail("admin@example.com");
//        orgAdmin.setRole(Role.ORG_ADMIN);
//        orgAdmin.setLoggedIn(true);
//        orgAdmin.setEmailVerificationStatus(OrgStatus.ACTIVE);
//        orgAdmin.setOrganization(org);
//
//        endUser = new EndUser();
//        endUser.setId(UUID.randomUUID());
//        endUser.setEmail("user@example.com");
//        endUser.setEmailVerified(true);
//        endUser.setActive(true);
//
//        userWallet = new Wallet();
//        userWallet.setId(UUID.randomUUID());
//        userWallet.setOrganization(org);
//        userWallet.setFiatBalance(BigDecimal.valueOf(1000));
//        userWallet.setSuiBalance(BigDecimal.valueOf(500));
//
//        endUserWallet = new Wallet();
//        endUserWallet.setId(UUID.randomUUID());
//        endUserWallet.setEndUser(endUser);
//        endUserWallet.setFiatBalance(BigDecimal.valueOf(200));
//        endUserWallet.setSuiBalance(BigDecimal.valueOf(100));
//
//        mockAuthentication(orgAdmin.getEmail(), true);
//    }
//
//    private void mockAuthentication(String email, boolean authenticated) {
//        Authentication auth = mock(Authentication.class);
//        when(auth.isAuthenticated()).thenReturn(authenticated);
//        when(auth.getPrincipal()).thenReturn(email);
//        when(auth.getName()).thenReturn(email);
//
//        SecurityContext securityContext = mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(auth);
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    @Test
//    void fiatDeposit_success() {
//        FiatDepositRequest request = new FiatDepositRequest();
//        request.setValue("wallet1");
//        request.setAmount(BigDecimal.valueOf(500));
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier(any())).thenReturn(Optional.of(userWallet));
//        PaystackDepositResponse mockResponse = PaystackDepositResponse.builder()
//                .success(true)
//                .authorizationUrl("https://paystack.com/authorize")
//                .accessCode("AC_123")
//                .reference("REF_123")
//                .build();
//
//        when(fiatService.deposit(any(BigDecimal.class), anyString(), anyString()))
//                .thenReturn(mockResponse);
//
//        TransactionResponse response = transactionService.fiatDeposit(request);
//
//        assertThat(response.getStatus()).isEqualTo("SUCCESS");
//        assertThat(userWallet.getFiatBalance()).isEqualByComparingTo(BigDecimal.valueOf(1500));
//    }
//
//    @Test
//    void suiDeposit_success() {
//        SuiDepositRequest request = new SuiDepositRequest();
//        request.setWalletType(WalletType.SUI);
//        request.setAmount(BigDecimal.valueOf(100));
//        request.setUserId(orgAdmin.getId().toString());
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOrgUserIdAndWalletType(orgAdmin.getId(), WalletType.SUI)).thenReturn(Optional.of(userWallet));
//
//        TransactionResponse response = transactionService.suiDeposit(request);
//
//        assertThat(response.getStatus()).isEqualTo("PENDING");
//    }
//
//    @Test
//    void checkFiatBalance_success() {
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier(any())).thenReturn(Optional.of(userWallet));
//
//        BalanceResponse balance = transactionService.checkFiatBalance();
//        assertThat(balance.getBalance()).isEqualByComparingTo(userWallet.getFiatBalance());
//    }
//
//    @Test
//    void getOrganizationTransactionSummaries_success() {
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(transactionRepository.findTransactionSummariesByOrganizationId(orgAdmin.getOrganization().getId()))
//                .thenReturn(List.of());
//
//        var summaries = transactionService.getOrganizationTransactionSummaries();
//        assertThat(summaries).isEmpty();
//    }
//
//    @Test
//    void getOrganizationTransactionSummaries_notVerified_throws() {
//        orgAdmin.setEmailVerificationStatus(OrgStatus.PENDING_VERIFICATION);
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//
//        assertThrows(IllegalStateException.class, () -> transactionService.getOrganizationTransactionSummaries());
//    }
//
//    @Test
//    void p2pFiatDeposit_success() {
//        P2PDepositRequest request = new P2PDepositRequest();
//        request.setSenderWalletValue("wallet1");
//        request.setReceiverWalletValue("wallet2");
//        request.setAmount(BigDecimal.valueOf(100));
//
//        Wallet receiverWallet = new Wallet();
//        receiverWallet.setFiatBalance(BigDecimal.valueOf(50));
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier("wallet1")).thenReturn(Optional.of(userWallet));
//        when(walletRepository.findByOwnerIdentifier("wallet2")).thenReturn(Optional.of(receiverWallet));
//
//        TransactionResponse response = transactionService.p2pFiatDeposit(request);
//
//        assertThat(response.getStatus()).isEqualTo("SUCCESS");
//        assertThat(userWallet.getFiatBalance()).isEqualByComparingTo(BigDecimal.valueOf(900));
//        assertThat(receiverWallet.getFiatBalance()).isEqualByComparingTo(BigDecimal.valueOf(150));
//    }
//
//    @Test
//    void p2pFiatDeposit_insufficientBalance_throws() {
//        P2PDepositRequest request = new P2PDepositRequest();
//        request.setSenderWalletValue("wallet1");
//        request.setReceiverWalletValue("wallet2");
//        request.setAmount(BigDecimal.valueOf(2000));
//
//        Wallet receiverWallet = new Wallet();
//        receiverWallet.setFiatBalance(BigDecimal.valueOf(50));
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier("wallet1")).thenReturn(Optional.of(userWallet));
//        when(walletRepository.findByOwnerIdentifier("wallet2")).thenReturn(Optional.of(receiverWallet));
//
//        assertThrows(RuntimeException.class, () -> transactionService.p2pFiatDeposit(request));
//    }
//
//    @Test
//    void fiatWithdraw_success() {
//        FiatWithdrawRequest request = new FiatWithdrawRequest();
//        request.setValue("wallet1");
//        request.setAmount(BigDecimal.valueOf(300));
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier("wallet1")).thenReturn(Optional.of(userWallet));
//        when(withdrawalService.withdrawToFiat(eq(request), eq(userWallet.getId()), any(), anyString()))
//                .thenReturn(TransactionResponse.builder()
//                        .reference("REF_WD_123")
//                        .status("SUCCESS")
//                        .amount(BigDecimal.valueOf(300))
//                        .balance(BigDecimal.valueOf(700))
//                        .build());
//
//        TransactionResponse response = transactionService.fiatWithdraw(request);
//
//        assertThat(response.getStatus()).isEqualTo("SUCCESS");
//    }
//
//    @Test
//    void bulkFiatDeposit_success() {
//        BulkFiatDepositRequest request = new BulkFiatDepositRequest();
//        request.setSenderWalletValue("wallet1");
//
//        FiatDepositRequest fd1 = new FiatDepositRequest();
//        fd1.setValue("wallet1");
//        fd1.setAmount(BigDecimal.valueOf(500));
//
//        FiatDepositRequest fd2 = new FiatDepositRequest();
//        fd2.setValue("wallet2");
//        fd2.setAmount(BigDecimal.valueOf(100));
//
//        FiatDepositRequest fd3 = new FiatDepositRequest();
//        fd3.setValue("wallet3");
//        fd3.setAmount(BigDecimal.valueOf(200));
//
//        request.setRequests(List.of(fd2, fd3));
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier("wallet1")).thenReturn(Optional.of(userWallet));
//        when(bulkTransactionService.disburseBulkFiatDeposit(eq(userWallet), anyList()))
//                .thenReturn(List.of(
//                        TransactionResponse.builder().amount(BigDecimal.valueOf(100)).build(),
//                        TransactionResponse.builder().amount(BigDecimal.valueOf(200)).build()
//                ));
//
//        List<TransactionResponse> responses = transactionService.bulkFiatDeposit(request);
//
//        assertThat(responses).hasSize(2);
//        assertThat(userWallet.getFiatBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
//    }
//
//
//    @Test
//    void bulkFiatWithdraw_insufficientBalance_throws() {
//        BulkFiatWithdrawRequest request = new BulkFiatWithdrawRequest();
//        request.setSenderWalletValue("wallet1");
//
//        FiatWithdrawRequest request1 = new FiatWithdrawRequest();
//        request1.setValue("wallet1");
//        request1.setAmount(BigDecimal.valueOf(200));
//
//        FiatWithdrawRequest request2 = new FiatWithdrawRequest();
//        request2.setValue("wallet2");
//        request2.setAmount(BigDecimal.valueOf(1200));
//
//        request.setRequests(List.of(request1, request2));
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier("wallet1")).thenReturn(Optional.of(userWallet));
//
//        assertThrows(IllegalStateException.class, () -> transactionService.bulkFiatWithdraw(request));
//    }
//
//
//    @Test
//    void p2pSuiDeposit_success() {
//        P2PDepositRequest request = new P2PDepositRequest();
//        request.setSenderWalletValue("wallet1");
//        request.setReceiverWalletValue("wallet2");
//        request.setAmount(BigDecimal.valueOf(50));
//
//        Wallet receiverWallet = new Wallet();
//        receiverWallet.setSuiBalance(BigDecimal.valueOf(20));
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier("wallet1")).thenReturn(Optional.of(userWallet));
//        when(walletRepository.findByOwnerIdentifier("wallet2")).thenReturn(Optional.of(receiverWallet));
//
//        TransactionResponse response = transactionService.p2pSuiDeposit(request);
//
//        assertThat(response.getStatus()).isEqualTo("SUCCESS");
//        assertThat(userWallet.getSuiBalance()).isEqualByComparingTo(BigDecimal.valueOf(450));
//        assertThat(receiverWallet.getSuiBalance()).isEqualByComparingTo(BigDecimal.valueOf(70));
//    }
//
//    @Test
//    void getTransactionHistory_success() {
//        Transaction txn1 = new Transaction();
//        txn1.setReference("TXN1");
//        txn1.setStatus(TransactionStatus.SUCCESS);
//        txn1.setAmount(BigDecimal.valueOf(100));
//        txn1.setWallet(userWallet);
//
//        Transaction txn2 = new Transaction();
//        txn2.setReference("TXN2");
//        txn2.setStatus(TransactionStatus.PENDING);
//        txn2.setAmount(BigDecimal.valueOf(50));
//        txn2.setWallet(userWallet);
//
//        when(userRepository.findUserByEmail(orgAdmin.getEmail())).thenReturn(Optional.of(orgAdmin));
//        when(walletRepository.findByOwnerIdentifier("wallet1")).thenReturn(Optional.of(userWallet));
//        when(transactionRepository.findAllByWallet(userWallet)).thenReturn(List.of(txn1, txn2));
//
//        List<TransactionResponse> responses = transactionService.getTransactionHistory();
//
//        assertThat(responses).hasSize(2);
//        assertThat(responses.get(0).getReference()).isEqualTo("TXN1");
//        assertThat(responses.get(1).getReference()).isEqualTo("TXN2");
//    }
//
//}
