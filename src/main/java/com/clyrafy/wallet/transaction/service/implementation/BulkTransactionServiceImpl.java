//package com.clyrafy.wallet.transaction.service.implementation;
//
//import com.clyrafy.wallet.ledger.dtos.requests.LedgerEntryDto;
//import com.clyrafy.wallet.ledger.service.LedgerService;
//import com.clyrafy.wallet.transaction.dtos.request.*;
//import com.clyrafy.wallet.transaction.dtos.responses.TransactionResponse;
//import com.clyrafy.wallet.transaction.enums.TransactionStatus;
//import com.clyrafy.wallet.transaction.exceptions.InsufficientBalanceException;
//import com.clyrafy.wallet.transaction.exceptions.WalletBalanceNotFound;
//import com.clyrafy.wallet.wallet.data.models.WalletBalance;
//import com.clyrafy.wallet.wallet.data.repositories.WalletBalanceRepository;
//import com.clyrafy.wallet.wallet.enums.WalletType;
//import com.clyrafy.wallet.transaction.data.models.Transaction;
//import com.clyrafy.wallet.transaction.data.repositories.TransactionRepository;
//import com.clyrafy.wallet.transaction.service.interfaces.BulkTransactionService;
//import com.clyrafy.wallet.wallet.data.models.Wallet;
//import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
//import com.clyrafy.wallet.wallet.exceptions.WalletNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class BulkTransactionServiceImpl implements BulkTransactionService {
//
//    private final WalletRepository walletRepository;
//    private final TransactionRepository transactionRepository;
//    private final LedgerService ledgerService;
//    private final WalletBalanceRepository walletBalanceRepository;
//
//    @Override
//    public List<TransactionResponse> disburseBulkFiatDeposit(Wallet senderWallet, List<FiatDepositRequest> requests) {
//        return disburseBulk(requests, WalletType.NGN, senderWallet);
//    }
//
//    @Override
//    public List<TransactionResponse> disburseBulkSuiDeposit(Wallet senderWallet, List<SuiDepositRequest> requests) {
//        return disburseBulk(requests, WalletType.SUI, senderWallet);
//    }
//
//    @Override
//    public List<TransactionResponse> disburseBulkFiatWithdraw(Wallet senderWallet, List<FiatWithdrawRequest> requests) {
//        return disburseBulk(requests, WalletType.NGN, senderWallet);
//    }
//
//    @Override
//    public List<TransactionResponse> disburseBulkSuiWithdraw(Wallet senderWallet, List<SuiWithdrawRequest> requests) {
//        return disburseBulk(requests, WalletType.SUI, senderWallet);
//    }
//
//    private <T> List<TransactionResponse> disburseBulk(List<T> requests, WalletType type, Wallet senderWallet) {
//        List<TransactionResponse> responses = new ArrayList<>();
//        String bulkReference = "BULK_" + UUID.randomUUID();
//
//        for (T transactionRequest : requests) {
//            String recipientIdentifier;
//            BigDecimal amount;
//
//            if (transactionRequest instanceof FiatDepositRequest fiatDepositRequest) {
//                recipientIdentifier = fiatDepositRequest.getValue();
//                amount = fiatDepositRequest.getAmount();
//            } else if (transactionRequest instanceof SuiDepositRequest suiDepositRequest) {
//                recipientIdentifier = suiDepositRequest.getUserId();
//                amount = suiDepositRequest.getAmount();
//            } else if (transactionRequest instanceof FiatWithdrawRequest fiatWithdrawRequest) {
//                recipientIdentifier = fiatWithdrawRequest.getValue();
//                amount = fiatWithdrawRequest.getAmount();
//            } else if (transactionRequest instanceof SuiWithdrawRequest suiWithdrawRequest) {
//                recipientIdentifier = suiWithdrawRequest.getValue();
//                amount = suiWithdrawRequest.getAmount();
//            } else {
//                throw new IllegalArgumentException("Unsupported request type for bulk disbursal");
//            }
//
////            debitSenderWallet(senderWallet, type, amount);
//            Wallet recipientWallet = getAndCreditRecipientWallet(type, recipientIdentifier, amount);
//            Transaction txn = createTransactionRecord(type, senderWallet, bulkReference, amount, recipientWallet, recipientIdentifier);
//            createLedgerEntry(senderWallet, recipientWallet, amount, type, txn.getReference(), bulkReference);
//
//            buildTransactionResponse(type, responses, txn, amount, recipientWallet, senderWallet, recipientIdentifier);
//        }
//
//        return responses;
//    }
//
////    private void debitSenderWallet(Wallet senderWallet, WalletType type, BigDecimal amount){
////        WalletBalance senderBalance = walletBalanceRepository.findByWalletIdAndCurrencyType(senderWallet.getId(), type)
////                .orElseThrow(() -> new WalletBalanceNotFound("Sender wallet balance not found for type: " + type));
////        if (senderBalance.getBalance().compareTo(amount) < 0) {
////            throw new InsufficientBalanceException("Insufficient balance in sender wallet");
////        }
////        senderBalance.setBalance(senderBalance.getBalance().subtract(amount));
////        walletBalanceRepository.save(senderBalance);
////    }
//
//    private Wallet getAndCreditRecipientWallet(WalletType type, String recipientIdentifier, BigDecimal amount) {
//        Wallet recipientWallet = walletRepository.findByVirtualAccountNum(recipientIdentifier)
//                .orElseThrow(() -> new WalletNotFoundException("Recipient wallet not found: " + recipientIdentifier));
//
//        WalletBalance balance = walletBalanceRepository.findByWalletIdAndCurrencyType(recipientWallet.getId(), type)
//                        .orElseThrow(() -> new WalletBalanceNotFound("Wallet balance not found for type: " + type));
//        balance.setBalance(balance.getBalance().add(amount));
//        walletBalanceRepository.save(balance);
//        return recipientWallet;
//    }
//
//    private static void buildTransactionResponse(WalletType type, List<TransactionResponse> responses, Transaction txn, BigDecimal amount, Wallet senderWallet, Wallet recipientWallet, String recipientIdentifier) {
//
//        BigDecimal recipientBalance = recipientWallet.getBalances().stream()
//                .filter(walletBalance -> walletBalance.getCurrencyType() == type)
//                .findFirst()
//                .map(WalletBalance::getBalance)
//                .orElse(BigDecimal.ZERO);
//
//        BigDecimal senderBalance = senderWallet.getBalances().stream()
//                .filter(b -> b.getCurrencyType() == type)
//                .findFirst()
//                .map(WalletBalance::getBalance)
//                .orElse(BigDecimal.ZERO);
//
//        responses.add(TransactionResponse.builder()
//                .reference(txn.getReference())
//                .status(txn.getStatus().name())
//                .amount(amount)
//                .senderBalance(senderBalance)
//                .recipientBalance(recipientBalance)
//                .recipient(recipientIdentifier)
//                .type(type)
//                .build());
//    }
//
//    private Transaction createTransactionRecord(WalletType type, Wallet senderWallet, String bulkReference, BigDecimal amount, Wallet recipientWallet, String recipientIdentifier) {
//        Transaction txn = new Transaction();
//        txn.setReference(UUID.randomUUID().toString());
//        txn.setBulkReference(bulkReference);
//        txn.setAmount(amount);
//        txn.setType(type);
//        txn.setStatus(TransactionStatus.SUCCESS);
//        txn.setWallet(recipientWallet);
//        txn.setSenderWallet(senderWallet.getOrganization() != null ? senderWallet.getOrganization().getName() : senderWallet.getEndUser().getId().toString());
//        txn.setReceiverWallet(recipientIdentifier);
//        txn.setCreatedAt(LocalDateTime.now());
//        transactionRepository.save(txn);
//        return txn;
//    }
//
//    private void createLedgerEntry(Wallet sender, Wallet recipient, BigDecimal amount, WalletType type, String txnRef, String bulkRef) {
//       LedgerEntryDto ledger = LedgerEntryDto.builder()
//                        .amount(amount)
//                        .walletType(type)
//                        .channel(type.name())
//                        .receiverAddress(recipient.getVirtualAccountNum())
//                        .txHash(txnRef)
//                        .ownerType(sender.getVirtualAccountNum())
//                        .walletId(sender.getId())
//                        .build();
//
//        ledgerService.recordWithdrawal(ledger);
//        System.out.println("Ledger Entry -> Sender: " + sender.getVirtualAccountNum()
//                + ", Recipient: " + recipient.getVirtualAccountNum()
//                + ", Amount: " + amount
//                + ", Type: " + type
//                + ", TxRef: " + txnRef
//                + ", BulkRef: " + bulkRef
//                + ", Timestamp: " + LocalDateTime.now());
//    }
//}
