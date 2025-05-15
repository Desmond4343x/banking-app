package net.desmond.bankingApp.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    @Query("SELECT t FROM Transaction t WHERE t.senderId = :senderId")
    List<Transaction> findAllSent(@Param("senderId") Long senderId);

    @Query("SELECT t FROM Transaction t WHERE t.receiverId = :receiverId")
    List<Transaction> findAllReceived(@Param("receiverId") Long receiverId);

    @Query("SELECT t FROM Transaction t WHERE t.senderId = :userId OR t.receiverId = :userId")
    List<Transaction> findTransactionById(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'pending'")
    List<Transaction> findAllPending();

    @Query("SELECT t FROM Transaction t WHERE t.status = 'pending' AND (t.senderId = :userId OR t.receiverId = :userId)")
    List<Transaction> findAllPendingByUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'pending' AND t.senderId = :senderId")
    List<Transaction> findAllPendingSentByUser(@Param("senderId") Long senderId);

    @Query("SELECT t FROM Transaction t WHERE t.status = 'pending' AND t.receiverId = :receiverId")
    List<Transaction> findAllPendingReceivedByUser(@Param("receiverId") Long receiverId);
}
