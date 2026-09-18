package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBlockRepository extends JpaRepository<UserBlock, Integer> {

    boolean existsByBlocker_IdAndBlocked_Id(Integer blockerId, Integer blockedId);

    void deleteByBlocker_IdAndBlocked_Id(Integer blockerId, Integer blockedId);

    @Query("select b.blocked.id from UserBlock b where b.blocker.id = :uid")
    List<Integer> blockedUserIdsBy(@Param("uid") Integer blockerId);
}