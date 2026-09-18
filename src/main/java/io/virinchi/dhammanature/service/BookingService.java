package io.virinchi.dhammanature.service;

import io.virinchi.dhammanature.model.Booking;
import io.virinchi.dhammanature.model.Event;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.model.enums.BookingStatus;
import io.virinchi.dhammanature.model.enums.NotificationType;
import io.virinchi.dhammanature.model.enums.PaymentMethod;
import io.virinchi.dhammanature.model.enums.SessionMode;
import io.virinchi.dhammanature.repository.BookingRepository;
import io.virinchi.dhammanature.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;
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
    public Booking book(User user, Integer eventId, SessionMode attendanceMode, int attendees,
                        PaymentMethod paymentMethod, int pointsToUse) {
        return book(user, eventId, attendanceMode, attendees, paymentMethod, pointsToUse,
                null, null, false);
    }

    /** Full booking: requires a credential and acceptance of the etiquette &amp; rules agreement. */
    @Transactional
    public Booking book(User user, Integer eventId, SessionMode attendanceMode, int attendees,
                        PaymentMethod paymentMethod, int pointsToUse,
                        String itemsToBring, String credential, boolean etiquetteAgreed) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found"));

        if (credential == null || credential.isBlank()) {
            throw new IllegalStateException("Please provide a proper credential (e.g. student or member ID) before booking.");
        }
        if (!etiquetteAgreed) {
            throw new IllegalStateException("You must accept the etiquette and rules agreement to attend this event.");
        }

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

        BigDecimal cost = event.getPrice() == null ? BigDecimal.ZERO : event.getPrice();
        BigDecimal total = cost.multiply(BigDecimal.valueOf(attendees));
        BigDecimal pointsWorth = PointValue.moneyValue(pointsToUse);
        if (pointsToUse > 0 && pointsWorth.compareTo(total) > 0) {
            pointsToUse = PointValue.pointsFor(total); // never over-redeem
            pointsWorth = PointValue.moneyValue(pointsToUse);
        }
        if (pointsToUse > 0) {
            rewardService.spendPoints(user, pointsToUse, "Booking \"" + event.getTitle() + "\"");
        }
        BigDecimal remaining = total.subtract(pointsWorth);
        if (remaining.signum() > 0 && paymentMethod == null) {
            throw new IllegalStateException("Please choose a payment method for this session.");
        }
        if (remaining.signum() <= 0) {
            paymentMethod = PaymentMethod.REDEEMED_POINTS;
        }

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user).event(event).attendanceMode(attendanceMode)
                .numberOfAttendees(attendees).status(BookingStatus.CONFIRMED)
                .paymentMethod(paymentMethod).pointsUsed(pointsToUse)
                .itemsToBring(itemsToBring).credential(credential.trim())
                .etiquetteAgreed(etiquetteAgreed).build());

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
