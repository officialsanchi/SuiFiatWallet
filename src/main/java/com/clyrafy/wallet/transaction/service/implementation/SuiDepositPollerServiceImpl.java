//package com.clyrafy.wallet.transaction.service.implementation;
//
//import com.clyrafy.wallet.transaction.data.models.Transaction;
//import com.clyrafy.wallet.transaction.dtos.request.SuiDepositRequest;
//import com.clyrafy.wallet.transaction.enums.TransactionStatus;
//import com.clyrafy.wallet.wallet.enums.WalletType;
//import com.clyrafy.wallet.transaction.data.repositories.TransactionRepository;
//import com.clyrafy.wallet.wallet.data.models.Wallet;
//import com.clyrafy.wallet.wallet.data.repositories.WalletRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//
//
//@Service
//public class SuiDepositPollerServiceImpl {
//    private static final Logger logger = LoggerFactory.getLogger(SuiDepositPollerServiceImpl.class);
//    private final RestTemplate restTemplate;
//    private final TransactionRepository transactionRepository;
//    private final WalletRepository walletRepository;
//
//    @Value("${central.sui.wallet.address}")
//    private String centralPoolAddress;
//
//    @Value("${sui.rpc.url:https://fullnode.mainnet.sui.io:443}")
//    private String suiRpcUrl;
//
//    public SuiDepositPollerServiceImpl(RestTemplate restTemplate,
//                                       TransactionRepository transactionRepository,
//                                       WalletRepository walletRepository) {
//        this.restTemplate = restTemplate;
//        this.transactionRepository = transactionRepository;
//        this.walletRepository = walletRepository;
//    }
//
//    @Scheduled(fixedRate = 60000)
//    public void pollSuiDeposits() {
//        try {
//            String request = """
//            {
//              "jsonrpc": "2.0",
//              "id": 1,
//              "method": "suix_queryTransactionBlocks",
//              "params": {
//                "filter": { "ToAddress": "%s" },
//                "options": { "showInput": true, "showEffects": true, "showEvents": true, "showObjectChanges": true },
//                "limit": 50
//              }
//            }
//            """.formatted(centralPoolAddress);
//
//            Map response = restTemplate.postForObject(suiRpcUrl, request, Map.class);
//            if (response == null || !response.containsKey("result")) {
//                logger.error("Failed to fetch transactions from Sui RPC");
//                return;
//            }
//
//            List<Map> transactions = (List<Map>) ((Map) response.get("result")).get("data");
//            if (transactions == null) {
//                logger.warn("No transactions found for pool address: {}", centralPoolAddress);
//                return;
//            }
//
//            for (Map tx : transactions) {
//                processTransaction(tx);
//            }
//        } catch (Exception e) {
//            logger.error("Error polling SUI deposits: {}", e.getMessage(), e);
//        }
//    }
//
//    public void confirmSuiDeposit(SuiDepositRequest request) {
//        String txHash = request.getTxHash();
//
//        if (transactionRepository.findByTxHash(txHash).isPresent()) {
//            logger.info("ℹ️ Transaction {} already confirmed", txHash);
//            return;
//        }
//
//        try {
//            String rpcRequest = """
//            {
//              "jsonrpc": "2.0",
//              "id": 1,
//              "method": "sui_getTransactionBlock",
//              "params": ["%s", { "showInput": true, "showEffects": true, "showEvents": true, "showObjectChanges": true }]
//            }
//            """.formatted(txHash);
//
//            Map response = restTemplate.postForObject(suiRpcUrl, rpcRequest, Map.class);
//            if (response == null || !response.containsKey("result")) {
//                throw new IllegalStateException("Transaction not found on Sui RPC: " + txHash);
//            }
//
//            Map tx = (Map) response.get("result");
//            processTransaction(tx);
//
//        } catch (Exception e) {
//            logger.error("Failed to confirm SUI deposit {}: {}", txHash, e.getMessage(), e);
//            throw new RuntimeException("Confirmation failed: " + e.getMessage());
//        }
//    }
//
//    private void processTransaction(Map txn) {
//        String digest = (String) txn.get("digest");
//        if (transactionRepository.findByTxHash(digest).isPresent()) {
//            return;
//        }
//
//        try {
//            String memo = extractMemo(txn);
//            String sender = extractSender(txn);
//            BigDecimal amount = extractAmount(txn);
//
//            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//                logger.warn("Invalid or zero amount for transaction: {}", digest);
//                return;
//            }
//
//            Wallet wallet = findWallet(memo, sender);
//            if (wallet == null) {
//                logger.error("Wallet not found for memo: {} or sender: {}", memo, sender);
//                return;
//            }
//
//            updateWalletBalance(wallet, amount);
//            saveTransaction(wallet, digest, amount, sender);
//
//            logger.info("Processed SUI deposit: wallet={}, amount={}, txHash={}",
//                    wallet.getId(), amount, digest);
//
//        } catch (Exception e) {
//            logger.error("Error processing txn {}: {}", txn.get("digest"), e.getMessage(), e);
//        }
//    }
//
//    private String extractMemo(Map txn) {
//        try {
//            Map transaction = (Map) txn.get("transaction");
//            Map data = (Map) transaction.get("data");
//            return (String) data.get("memo");
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private String extractSender(Map txn) {
//        try {
//            Map transaction = (Map) txn.get("transaction");
//            return (String) transaction.get("sender");
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private BigDecimal extractAmount(Map txn) {
//        try {
//            List<Map> objectChanges = (List<Map>) txn.get("objectChanges");
//            if (objectChanges == null) return BigDecimal.ZERO;
//
//            for (Map change : objectChanges) {
//                if ("modified".equals(change.get("type")) && centralPoolAddress.equals(change.get("owner"))) {
//                    Map balance = (Map) change.get("objectType");
//                    if (balance != null && "0x2::coin::Coin<0x2::sui::SUI>".equals(balance.get("type"))) {
//                        String rawValue = (String) balance.get("value"); // amount in MIST
//                        return new BigDecimal(rawValue)
//                                .divide(new BigDecimal("1000000000"), 9, BigDecimal.ROUND_DOWN);
//                    }
//                }
//            }
//            return BigDecimal.ZERO;
//        } catch (Exception e) {
//            return BigDecimal.ZERO;
//        }
//    }
//
//    private Wallet findWallet(String memo, String sender) {
//        if (memo != null && !memo.isEmpty()) {
//            try {
//                String userIdStr = memo.split("_")[0];
//                UUID userId = UUID.fromString(userIdStr);
//
//                Optional<Wallet> walletOpt = walletRepository.findByUserIdAndWalletType(userId, WalletType.SUI);
//                if (walletOpt.isPresent()) return walletOpt.get();
//
//                walletOpt = walletRepository.findByEndUserIdAndWalletType(userId, WalletType.SUI);
//                if (walletOpt.isPresent()) return walletOpt.get();
//
//            } catch (IllegalArgumentException e) {
//                logger.warn("Invalid UUID in memo '{}', skipping OrgUser/EndUser lookup", memo);
//            }
//        }
//
//        try {
//            UUID orgId = UUID.fromString(sender);
//            Optional<Wallet> walletOpt = walletRepository.findByOrganizationIdAndWalletType(orgId, WalletType.SUI);
//            if (walletOpt.isPresent()) return walletOpt.get();
//        } catch (IllegalArgumentException e) {
//            logger.warn("Sender '{}' is not a valid UUID, skipping Organization lookup", sender);
//        }
//
//        return null;
//    }
//
//
//    private void updateWalletBalance(Wallet wallet, BigDecimal amount) {
//        wallet.setSuiBalance(wallet.getSuiBalance().add(amount));
//        wallet.setUpdatedAt(LocalDateTime.now());
//        walletRepository.save(wallet);
//    }
//
//    private void saveTransaction(Wallet wallet, String digest, BigDecimal amount, String sender) {
//        Transaction transaction = new Transaction();
//        transaction.setReference(UUID.randomUUID().toString());
//        transaction.setTxHash(digest);
//        transaction.setAmount(amount);
//        transaction.setType(WalletType.SUI);
//        transaction.setStatus(TransactionStatus.SUCCESS);
//        transaction.setSenderWallet(sender);
//        transaction.setReceiverWallet(centralPoolAddress);
//        transaction.setCreatedAt(LocalDateTime.now());
//        transaction.setWallet(wallet);
//        transactionRepository.save(transaction);
//    }
//}