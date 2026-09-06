package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByUser_IdOrderByBookingDateDesc(Integer userId);
    List<Booking> findByEvent_Id(Integer eventId);
    long countByEvent_Id(Integer eventId);
}
