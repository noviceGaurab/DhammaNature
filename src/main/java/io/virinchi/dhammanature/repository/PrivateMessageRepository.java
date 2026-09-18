package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Integer> {

    @Query("select m from PrivateMessage m where m.sender.id = :uid or m.recipient.id = :uid order by m.createdAt desc")
    List<PrivateMessage> threadFor(@Param("uid") Integer userId);

    long countByRecipient_IdAndIsReadFalse(Integer recipientId);

    @Query("select m from PrivateMessage m where m.recipient.id = :uid and m.isRead = false order by m.createdAt desc")
    List<PrivateMessage> unreadFor(@Param("uid") Integer userId);
}