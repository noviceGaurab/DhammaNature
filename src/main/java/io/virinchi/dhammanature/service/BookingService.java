package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Booking;
import io.virinchi.dhammanature.model.Event;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.BookingStatus;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.model.enums.SessionMode;
import io.virinchi.dhammanature.repository.BookingRepository;
import io.virinchi.dhammanature.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/** FR-03 / FR-04: booking a meditation session or event; supports hybrid mode (NFR-02). */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final RewardService rewardService;
    private final NotificationService notificationService;

    @Transactional
    public Booking book(User user, Integer eventId, SessionMode attendanceMode, int attendees) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));

        if (event.getCapacity() != null) {
            long already = bookingRepository.countByEvent_Id(eventId);
            if (already + attendees > event.getCapacity()) {
                throw new IllegalStateException("Sorry, this session is fully booked.");
            }
        }
        if (attendanceMode == SessionMode.ONLINE && !event.getMeditationCenter().isSupportsOnlineSessions()) {
            throw new IllegalStateException("This center does not offer online sessions for this event.");
        }
        if (attendanceMode == SessionMode.PHYSICAL && !event.getMeditationCenter().isSupportsPhysicalSessions()) {
            throw new IllegalStateException("This center does not offer physical sessions for this event.");
        }

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user).event(event).attendanceMode(attendanceMode)
                .numberOfAttendees(attendees).status(BookingStatus.CONFIRMED).build());

        rewardService.awardPoints(user, 5, "Booked \"" + event.getTitle() + "\"");
        notificationService.notifyUser(user, "Booking confirmed: " + event.getTitle(),
                "See you " + (attendanceMode == SessionMode.ONLINE ? "online" : "at " + event.getVenue()) + ".",
                NotificationType.EVENT);
        return booking;
    }

    public Booking get(Integer id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
    }

    public List<Booking> forUser(Integer userId) {
        return bookingRepository.findByUser_IdOrderByBookingDateDesc(userId);
    }

    public List<Booking> forEvent(Integer eventId) {
        return bookingRepository.findByEvent_Id(eventId);
    }

    @Transactional
    public void cancel(User user, Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You do not have permission to modify this booking.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("This booking has already been cancelled.");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("This booking has already been completed and cannot be cancelled.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
    }
}
