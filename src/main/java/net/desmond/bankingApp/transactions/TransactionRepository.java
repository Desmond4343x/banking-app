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
}
