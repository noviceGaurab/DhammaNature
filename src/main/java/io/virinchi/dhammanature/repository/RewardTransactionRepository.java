package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.RewardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Integer> {
    List<RewardTransaction> findByUser_IdOrderByCreatedAtDesc(Integer userId);
}
