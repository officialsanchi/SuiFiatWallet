package com.clyrafy.wallet.transaction.data.repositories;

import com.clyrafy.wallet.transaction.data.models.Transaction;
import com.clyrafy.wallet.transaction.dtos.responses.TransactionSummaryResponse;
import com.clyrafy.wallet.wallet.data.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllByWallet(Wallet wallet);

    @Query("SELECT new com.clyrafy.wallet.transaction.dtos.responses.TransactionSummaryResponse(" +
            "t.reference, t.txHash, t.amount, t.status, t.senderWallet, t.receiverWallet, t.createdAt) " +
            "FROM Transaction t " +
            "JOIN t.wallet w " +
            "WHERE w.organization.id = :organizationId")
    List<TransactionSummaryResponse> findTransactionSummariesByOrganizationId(@Param("organizationId") UUID organizationId);

    Optional<Transaction> findByTxHash(String transactionHash);

    List<Transaction> findAllByWalletIn(List<Wallet> wallets);

    Optional<Transaction> findByReference(String reference);
}