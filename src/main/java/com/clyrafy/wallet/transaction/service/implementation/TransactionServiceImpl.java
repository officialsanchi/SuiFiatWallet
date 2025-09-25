package com.clyrafy.wallet.transaction.service.implementation;

import com.clyrafy.wallet.enduser.data.models.EndUser;
import com.clyrafy.wallet.enduser.data.repositories.EndUserRepository;
import com.clyrafy.wallet.org.data.models.User;
import com.clyrafy.wallet.org.enums.Role;
import com.clyrafy.wallet.transaction.data.models.Transaction;
import com.clyrafy.wallet.transaction.data.repositories.TransactionRepository;
import com.clyrafy.wallet.transaction.dtos.request.*;
import com.clyrafy.wallet.transaction.dtos.responses.BalanceResponse;
import com.clyrafy.wallet.transaction.dtos.responses.PaystackDepositResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionSummaryResponse;
import com.clyrafy.wallet.transaction.enums.TransactionStatus;
import com.clyrafy.wallet.transaction.enums.TransactionType;
import com.clyrafy.wallet.transaction.service.interfaces.TransactionService;
import com.clyrafy.wallet.transaction.service.interfaces.WithdrawalService;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import com.clyrafy.wallet.wallet.data.models.WalletBalance;
import com.clyrafy.wallet.wallet.enums.WalletType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
@Transactional
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    @Value("${central.sui.wallet.address}")
    private String centralPoolAddress;

    private final EndUserRepository endUserRepository;
    private final WithdrawalService withdrawalService;
    private final TransactionRepository transactionRepository;
    private final WalletResolverService walletResolver;
    private final TransactionMapper transactionMapper;
    private final PaystackService paystackService;

    public TransactionServiceImpl( EndUserRepository endUserRepository,
                                   WithdrawalService withdrawalService,
                                   TransactionRepository transactionRepository,
                                   WalletResolverService walletResolver,
                                   TransactionMapper transactionMapper,
                                   PaystackService paystackService) {
        this.endUserRepository = endUserRepository;
        this.withdrawalService = withdrawalService;
        this.transactionRepository = transactionRepository;
        this.walletResolver = walletResolver;
        this.transactionMapper = transactionMapper;
        this.paystackService = paystackService;
    }

    private String generateTransactionHash(P2PDepositRequest request) {
        try {
            String data = request.getSenderWalletValue() + request.getReceiverWalletValue() + request.getAmount() + System.nanoTime();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 not available, fallback to UUID");
            return UUID.randomUUID().toString();
        }
    }

    private Transaction buildTransaction(String reference, String txHash, BigDecimal amount,
                                         TransactionType type, TransactionStatus status,
                                         Wallet wallet, String senderWallet, String receiverWallet) {
        log.info("Wallet type = {}", wallet.getWalletType());
        Transaction txn = new Transaction();
        txn.setReference(reference);
        txn.setTxHash(txHash);
        txn.setAmount(amount);
        txn.setType(type);
        txn.setStatus(status);
        txn.setWallet(wallet);
        txn.setWalletType(wallet.getWalletType());
        txn.setSenderWallet(senderWallet);
        txn.setReceiverWallet(receiverWallet);
        return txn;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public BalanceResponse checkFiatBalance() {
        Wallet wallet = walletResolver.getWalletByCurrentUser(WalletType.NGN);
        BigDecimal balance = walletResolver.getWalletBalance(wallet, WalletType.NGN);
        return new BalanceResponse(wallet.getVirtualAccountNum(), balance);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public BalanceResponse suiCheckBalance() {
        Wallet wallet = walletResolver.getWalletByCurrentUser(WalletType.SUI);
        BigDecimal balance = walletResolver.getWalletBalance(wallet, WalletType.SUI);
        return new BalanceResponse(wallet.getVirtualAccountNum(), balance);
    }


    @PreAuthorize("isAuthenticated()")
    @Override
    public TransactionResponse fiatDeposit(FiatDepositRequest request) {
        log.info("fiatDeposit called with request: {}", request);

        if (request == null || request.getRecipientIdentifier() == null || request.getAmount() == null) {
            log.error("Invalid request parameters: {}", request);
            throw new IllegalArgumentException("Invalid request parameters");
        }
        log.info("Request parameters validated");

        if (!WalletType.NGN.equals(request.getWalletType())) {
            log.error("Unsupported wallet type: {}", request.getWalletType());
            throw new IllegalArgumentException("This method only supports NGN deposits");
        }
        log.info("Wallet type is NGN");

        Object currentUser = walletResolver.getCurrentUser();
        log.info("Current user resolved: {}", currentUser);

        Wallet wallet = walletResolver.resolveWallet(request.getRecipientIdentifier(), null, null, WalletType.NGN);
        log.info("Wallet resolved: {}", wallet);
        if (wallet == null) {
            throw new IllegalStateException("Wallet not found for recipient: " + request.getRecipientIdentifier());
        }

        walletResolver.validateWalletAccess(wallet, currentUser);
        log.info("Wallet access validated for user: {}", currentUser);

        String reference = "clyrafi_" + UUID.randomUUID();
        log.info("Generated reference: {}", reference);

        String userId = currentUser instanceof User ? ((User) currentUser).getId().toString()
                : ((EndUser) currentUser).getId().toString();
        log.info("User ID determined: {}", userId);

        String businessId = currentUser instanceof User ? ((User) currentUser).getOrganization().getId().toString() : null;
        log.info("Business ID determined: {}", businessId);

        String emailForPaystack = walletResolver.getWalletOwnerEmail(wallet);
        log.info("Wallet owner email for Paystack: {}", emailForPaystack);

        PaystackDepositResponse paystackDepositResponse = paystackService.deposit(
                request.getAmount(),
                emailForPaystack,
                businessId,
                userId,
                wallet.getId().toString(),
                reference,
                request.getCallbackUrl()
        );
        log.info("Paystack deposit response: {}", paystackDepositResponse);

        Transaction transaction = buildTransaction(
                reference,
                null,
                request.getAmount(),
                TransactionType.DEPOSIT,
                paystackDepositResponse.isSuccess() ? TransactionStatus.PENDING : TransactionStatus.FAILED,
                wallet,
                null,
                wallet.getVirtualAccountNum()
        );
        log.info("Transaction built: {}", transaction);

        transactionRepository.save(transaction);
        log.info("Transaction saved to repository");

        TransactionResponse response = transactionMapper.toResponse(transaction);
        log.info("TransactionResponse mapped: {}", response);

        response.setRecipientBalance(walletResolver.getWalletBalance(wallet, WalletType.NGN));
        log.info("Recipient balance set in response: {}", response.getRecipientBalance());

        return response;
    }


    private String resolveEmailForPaystack(String email, String value, String username) {
        if (email != null) return email;

        if (username != null) {
            EndUser endUser = endUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("No user found for username: " + username));
            return endUser.getEmail();
        }

        if (value != null) {
            EndUser endUser = endUserRepository.findByEmail(value)
                    .orElseThrow(() -> new UsernameNotFoundException("No user found for email: " + value));
            return endUser.getEmail();
        }

        throw new IllegalArgumentException("Email or username must be provided for Paystack deposit");
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public TransactionResponse p2pFiatDeposit(P2PDepositRequest request) {
        Wallet sender = walletResolver.getWalletByVirtualAccount(request.getSenderWalletValue(), WalletType.NGN);
        Wallet receiver = walletResolver.getWalletByVirtualAccount(request.getReceiverWalletValue(), WalletType.NGN);
        BigDecimal amount = request.getAmount();

        BigDecimal senderBalance = walletResolver.getWalletBalance(sender, WalletType.NGN);
        if (senderBalance.compareTo(amount) < 0) throw new RuntimeException("Insufficient balance");

        walletResolver.updateWalletBalance(sender, WalletType.NGN, amount.negate());
        walletResolver.updateWalletBalance(receiver, WalletType.NGN, amount);

        BigDecimal newSenderBalance = walletResolver.getWalletBalance(sender, WalletType.NGN);
        BigDecimal newReceiverBalance = walletResolver.getWalletBalance(receiver, WalletType.NGN);

        String transactionReference = "P2P_FD_" + UUID.randomUUID();
        String transactionHash = generateTransactionHash(request);

        Transaction transaction = buildTransaction(transactionReference, transactionHash, amount,
                TransactionType.DEPOSIT, TransactionStatus.SUCCESS, receiver,
                request.getSenderWalletValue(), request.getReceiverWalletValue());
        transaction.setSenderBalanceSnapshot(newSenderBalance);
        transaction.setRecipientBalanceSnapshot(newReceiverBalance);
        transactionRepository.save(transaction);

        return transactionMapper.toResponse(transaction)
                .toBuilder()
                .status("SUCCESS")
                .amount(amount)
                .sender(request.getSenderWalletValue())
                .recipient(request.getReceiverWalletValue())
                .senderBalance(newSenderBalance)
                .recipientBalance(newReceiverBalance)
                .message("P2P Fiat deposit successful")
                .txHash(transactionHash)
                .build();
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public TransactionResponse p2pSuiDeposit(P2PDepositRequest request) {
        Wallet sender = walletResolver.getWalletByVirtualAccount(request.getSenderWalletValue(), WalletType.SUI);
        Wallet receiver = walletResolver.getWalletByVirtualAccount(request.getReceiverWalletValue(), WalletType.SUI);
        BigDecimal amount = request.getAmount();

        BigDecimal senderBalance = walletResolver.getWalletBalance(sender, WalletType.SUI);
        if (senderBalance.compareTo(amount) < 0) throw new RuntimeException("Insufficient balance");

        walletResolver.updateWalletBalance(sender, WalletType.SUI, amount.negate());
        walletResolver.updateWalletBalance(receiver, WalletType.SUI, amount);

        String transactionReference = "P2P_SD_" + UUID.randomUUID();
        String transactionHash = generateTransactionHash(request);

        Transaction txn = buildTransaction(transactionReference, transactionHash, amount,
                TransactionType.DEPOSIT, TransactionStatus.SUCCESS, receiver,
                request.getSenderWalletValue(), request.getReceiverWalletValue());
        transactionRepository.save(txn);

        return transactionMapper.toResponse(txn)
                .toBuilder()
                .status("SUCCESS")
                .amount(amount)
                .sender(request.getSenderWalletValue())
                .recipient(request.getReceiverWalletValue())
                .senderBalance(walletResolver.getWalletBalance(sender, WalletType.SUI))
                .recipientBalance(walletResolver.getWalletBalance(receiver, WalletType.SUI))
                .message("P2P SUI deposit successful")
                .txHash(transactionHash)
                .build();
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public TransactionResponse fiatWithdraw(FiatWithdrawRequest request) {
        Wallet wallet = walletResolver.getWalletByCurrentUser(WalletType.NGN);
        BigDecimal amount = request.getAmount();
        BigDecimal balance = walletResolver.getWalletBalance(wallet, WalletType.NGN);

        if (balance.compareTo(amount) < 0) throw new RuntimeException("Insufficient balance");

        walletResolver.updateWalletBalance(wallet, WalletType.NGN, amount.negate());

        Object currentUser = walletResolver.getCurrentUser();
        TransactionResponse response = withdrawalService.withdrawToFiat(
                request,
                wallet.getId(),
                currentUser instanceof User user ? user.getId() : ((EndUser) currentUser).getId(),
                currentUser instanceof User ? "ORG_USER" : "END_USER"
        );

        response.setSender(wallet.getVirtualAccountNum());
        response.setSenderBalance(walletResolver.getWalletBalance(wallet, WalletType.NGN));

        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public TransactionResponse suiWithdraw(SuiWithdrawRequest request) {
        Wallet wallet = walletResolver.getWalletByCurrentUser(WalletType.SUI);
        BigDecimal amount = request.getAmount();
        BigDecimal balance = walletResolver.getWalletBalance(wallet, WalletType.SUI);

        if (balance.compareTo(amount) < 0) throw new RuntimeException("Insufficient balance");

        walletResolver.updateWalletBalance(wallet, WalletType.SUI, amount.negate());

        Object currentUser = walletResolver.getCurrentUser();
        TransactionResponse response = withdrawalService.withdrawToSui(
                request,
                wallet.getId(),
                currentUser instanceof User u ? u.getId() : ((EndUser) currentUser).getId(),
                currentUser instanceof User ? "ORG_USER" : "END_USER"
        );

        response.setSender(wallet.getVirtualAccountNum());
        response.setSenderBalance(walletResolver.getWalletBalance(wallet, WalletType.SUI));

        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<TransactionResponse> getTransactionHistory() {
        Wallet fiatWallet = walletResolver.getWalletByCurrentUser(WalletType.NGN);
        Wallet suiWallet = walletResolver.getWalletByCurrentUser(WalletType.SUI);

        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(transactionRepository.findAllByWallet(fiatWallet));
        allTransactions.addAll(transactionRepository.findAllByWallet(suiWallet));

        List<TransactionResponse> responses = new ArrayList<>();
        for (Transaction txn : allTransactions) {
            responses.add(transactionMapper.toResponse(txn)
                    .toBuilder()
                    .status(txn.getStatus().name())
                    .txHash(txn.getTxHash())
                    .recipient(txn.getReceiverWallet())
                    .sender(txn.getWallet().getVirtualAccountNum())
                    .build());
        }
        return responses;
    }

    @PreAuthorize("hasRole('ORG_ADMIN')")
    @Override
    public List<TransactionSummaryResponse> getOrganizationTransactionSummaries() {
        Object currentUser = walletResolver.getCurrentUser();
        if (!(currentUser instanceof User user) || user.getRole() != Role.ORG_ADMIN) {
            throw new SecurityException("Only organization admins can view transaction summaries");
        }

        UUID organizationId = ((User) currentUser).getOrganization().getId();
        return transactionRepository.findTransactionSummariesByOrganizationId(organizationId);
    }

    @Override
    public List<TransactionResponse> getTransactionHistoryForWallets(List<Wallet> wallets) {
        List<TransactionResponse> responses = new ArrayList<>();
        for (Wallet wallet : wallets) {
            List<Transaction> transactions = transactionRepository.findAllByWallet(wallet);
            for (Transaction txn : transactions) {
                responses.add(transactionMapper.toResponse(txn)
                        .toBuilder()
                        .status(txn.getStatus().name())
                        .txHash(txn.getTxHash())
                        .sender(wallet.getVirtualAccountNum())
                        .recipient(txn.getReceiverWallet())
                        .amount(txn.getAmount())
                        .build());
            }
        }
        return responses;
    }

    @Override
    public void handlePaystackWebhook(String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);

            String event = root.path("event").asText();
            JsonNode data = root.path("data");
            String reference = data.path("reference").asText();

            Optional<Transaction> txnOpt = transactionRepository.findByReference(reference);
            if (txnOpt.isEmpty()) {
                log.warn("Transaction not found for reference: {}", reference);
                return;
            }

            Transaction txn = txnOpt.get();
            Wallet wallet = txn.getWallet();

            switch (event) {
                case "charge.success":
                    txn.setStatus(TransactionStatus.SUCCESS);
                    walletResolver.updateWalletBalance(wallet, txn.getWalletType(), txn.getAmount());

                    BigDecimal newBalance = walletResolver.getWalletBalance(wallet, txn.getWalletType());

                    txn.setSenderBalanceSnapshot(null);
                    txn.setRecipientBalanceSnapshot(newBalance);
                    break;
                case "charge.failed":
                    txn.setStatus(TransactionStatus.FAILED);
                    break;
                case "transfer.success":
                    txn.setStatus(TransactionStatus.SUCCESS);
                    break;
                case "transfer.failed":
                    txn.setStatus(TransactionStatus.FAILED);
                    break;
                default:
                    log.info("Unhandled Paystack webhook event: {}", event);
                    return;
            }

            transactionRepository.save(txn);
            log.info("Transaction {} updated to {}", reference, txn.getStatus());

        } catch (Exception e) {
            log.error("Failed to process Paystack webhook: {}", e.getMessage(), e);
        }
    }


}