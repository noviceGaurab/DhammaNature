package io.virinchi.dhammanature.restapi;

import io.virinchi.dhammanature.model.*;
import io.virinchi.dhammanature.model.enums.PaymentMethod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds plain JSON-safe maps from JPA entities. Hibernate lazy proxies cannot
 * be serialized directly (the "hibernateLazyInitializer" Jackson error), so the
 * REST controllers expose only these explicit views.
 */
final class ApiViews {

    private ApiViews() {
    }

    static Map<String, Object> user(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("phoneNumber", u.getPhoneNumber());
        m.put("address", u.getAddress());
        m.put("genderIdentity", u.getGenderIdentity());
        m.put("profileImage", u.getProfileImageData() == null ? null : "/users/" + u.getId() + "/image");
        m.put("bio", u.getBio());
        m.put("role", u.getRole() == null ? null : u.getRole().name());
        m.put("rewardPoints", u.getRewardPoints());
        m.put("createdAt", u.getCreatedAt());
        return m;
    }

    static Map<String, Object> product(Product p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("productName", p.getProductName());
        m.put("description", p.getDescription());
        m.put("genre", p.getGenre());
        m.put("price", p.getPrice());
        m.put("stockQuantity", p.getStockQuantity());
        m.put("imageUrl", p.getImageUrl());
        m.put("category", p.getCategory() == null ? null : p.getCategory().name());
        Vendor vendor = p.getVendor();
        m.put("vendorId", vendor == null ? null : vendor.getId());
        m.put("vendorName", vendor == null ? null : vendor.getVendorName());
        return m;
    }

    static Map<String, Object> event(Event e) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("title", e.getTitle());
        m.put("description", e.getDescription());
        m.put("eventDate", e.getEventDate());
        m.put("venue", e.getVenue());
        m.put("mode", e.getMode() == null ? null : e.getMode().name());
        m.put("capacity", e.getCapacity());
        m.put("price", e.getPrice());
        MeditationCenter c = e.getMeditationCenter();
        m.put("centerId", c == null ? null : c.getId());
        m.put("centerName", c == null ? null : c.getName());
        return m;
    }

    static Map<String, Object> center(MeditationCenter c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("location", c.getLocation());
        m.put("description", c.getDescription());
        m.put("contactNumber", c.getContactNumber());
        m.put("contactEmail", c.getContactEmail());
        m.put("website", c.getWebsite());
        m.put("facebookUrl", c.getFacebookUrl());
        m.put("instagramUrl", c.getInstagramUrl());
        m.put("coverImageUrl", c.getCoverImageUrl());
        m.put("supportsOnlineSessions", c.isSupportsOnlineSessions());
        m.put("supportsPhysicalSessions", c.isSupportsPhysicalSessions());
        m.put("verified", c.isVerified());
        return m;
    }

    static Map<String, Object> blogPost(BlogPost p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("title", p.getTitle());
        m.put("category", p.getCategory());
        m.put("content", p.getContent());
        m.put("imageUrl", p.getImageUrl());
        m.put("authorId", p.getAuthor() == null ? null : p.getAuthor().getId());
        m.put("authorName", p.getAuthorName());
        m.put("likeCount", p.getLikeCount());
        m.put("status", p.getStatus() == null ? null : p.getStatus().name());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    static Map<String, Object> gallery(Gallery g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("title", g.getTitle());
        m.put("imageUrl", g.getImageUrl());
        m.put("description", g.getDescription());
        MeditationCenter c = g.getMeditationCenter();
        m.put("centerId", c == null ? null : c.getId());
        m.put("centerName", c == null ? null : c.getName());
        return m;
    }

    static Map<String, Object> quiz(Quiz q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId());
        m.put("title", q.getTitle());
        m.put("description", q.getDescription());
        m.put("rewardPoints", q.getRewardPoints());
        m.put("questionCount", q.getQuestions().size());
        return m;
    }

    static Map<String, Object> quizQuestion(QuizQuestion q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId());
        m.put("questionText", q.getQuestionText());
        m.put("optionA", q.getOptionA());
        m.put("optionB", q.getOptionB());
        m.put("optionC", q.getOptionC());
        m.put("optionD", q.getOptionD());
        m.put("correctOption", q.getCorrectOption());
        return m;
    }

    static Map<String, Object> booking(Booking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        Event e = b.getEvent();
        m.put("eventId", e == null ? null : e.getId());
        m.put("eventTitle", e == null ? null : e.getTitle());
        m.put("eventDate", e == null ? null : e.getEventDate());
        m.put("attendanceMode", b.getAttendanceMode() == null ? null : b.getAttendanceMode().name());
        m.put("numberOfAttendees", b.getNumberOfAttendees());
        m.put("status", b.getStatus() == null ? null : b.getStatus().name());
        m.put("paymentMethod", b.getPaymentMethod() == null ? null : b.getPaymentMethod().getDisplayName());
        m.put("pointsUsed", b.getPointsUsed());
        m.put("credential", b.getCredential());
        m.put("bookingDate", b.getBookingDate());
        return m;
    }

    static Map<String, Object> order(ProductOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        Product p = o.getProduct();
        m.put("productId", p == null ? null : p.getId());
        m.put("productName", p == null ? null : p.getProductName());
        m.put("quantity", o.getQuantity());
        m.put("totalPrice", o.getTotalPrice());
        m.put("status", o.getStatus() == null ? null : o.getStatus().name());
        m.put("paymentMethod", o.getPaymentMethod() == null ? null : o.getPaymentMethod().getDisplayName());
        m.put("pointsUsed", o.getPointsUsed());
        m.put("orderDate", o.getOrderDate());
        return m;
    }

    static Map<String, Object> donation(Donation d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("receiptNumber", d.getReceiptNumber());
        m.put("firstName", d.getFirstName());
        m.put("lastName", d.getLastName());
        m.put("email", d.getEmail());
        m.put("amount", d.getAmount());
        m.put("donationType", d.getDonationType() == null ? null : d.getDonationType().name());
        m.put("paymentMethod", d.getPaymentMethod());
        m.put("donationDate", d.getDonationDate());
        CharityCampaign c = d.getCampaign();
        m.put("campaignId", c == null ? null : c.getId());
        m.put("campaignTitle", c == null ? null : c.getTitle());
        return m;
    }

    static Map<String, Object> rewardTransaction(RewardTransaction t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("type", t.getType() == null ? null : t.getType().name());
        m.put("points", t.getPoints());
        m.put("reason", t.getReason());
        m.put("createdAt", t.getCreatedAt());
        m.put("redeemedItemName",
                t.getRedeemedItem() == null ? null : t.getRedeemedItem().getName());
        return m;
    }

    static Map<String, Object> rewardCatalogItem(RewardCatalogItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", item.getId());
        m.put("name", item.getName());
        m.put("description", item.getDescription());
        m.put("pointsCost", item.getPointsCost());
        m.put("category", item.getCategory() == null ? null : item.getCategory().name());
        m.put("active", item.isActive());
        MeditationCenter c = item.getMeditationCenter();
        m.put("centerId", c == null ? null : c.getId());
        m.put("centerName", c == null ? null : c.getName());
        return m;
    }

    static Map<String, Object> campaign(CharityCampaign c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("title", c.getTitle());
        m.put("description", c.getDescription());
        m.put("goalAmount", c.getGoalAmount());
        m.put("raisedAmount", c.getRaisedAmount());
        m.put("startDate", c.getStartDate());
        m.put("endDate", c.getEndDate());
        m.put("status", c.getStatus() == null ? null : c.getStatus().name());
        m.put("progressPercent", c.getProgressPercent());
        m.put("daysRemaining", c.getDaysRemaining());
        MeditationCenter center = c.getMeditationCenter();
        m.put("centerId", center == null ? null : center.getId());
        m.put("centerName", center == null ? null : center.getName());
        return m;
    }

    static Map<String, Object> volunteerOpportunity(VolunteerOpportunity o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("title", o.getTitle());
        m.put("description", o.getDescription());
        m.put("opportunityDate", o.getOpportunityDate());
        m.put("location", o.getLocation());
        MeditationCenter c = o.getMeditationCenter();
        m.put("centerId", c == null ? null : c.getId());
        m.put("centerName", c == null ? null : c.getName());
        return m;
    }

    static Map<String, Object> notification(Notification n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId());
        m.put("title", n.getTitle());
        m.put("message", n.getMessage());
        m.put("type", n.getType() == null ? null : n.getType().name());
        m.put("read", n.isRead());
        m.put("createdAt", n.getCreatedAt());
        return m;
    }

    static Map<String, Object> volunteerRegistration(VolunteerRegistration r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("status", r.getStatus() == null ? null : r.getStatus().name());
        m.put("studentIdNumber", r.getStudentIdNumber());
        m.put("collegeName", r.getCollegeName());
        m.put("studentIdImage", r.getStudentIdImageData() == null ? null : "/volunteer-registrations/" + r.getId() + "/student-id/image");
        m.put("collegeApprovalImage", r.getCollegeApprovalImageData() == null ? null : "/volunteer-registrations/" + r.getId() + "/college-approval/image");
        m.put("warningsAccepted", r.isWarningsAccepted());
        m.put("registeredAt", r.getRegisteredAt());
        VolunteerOpportunity o = r.getOpportunity();
        m.put("opportunityId", o == null ? null : o.getId());
        m.put("opportunityTitle", o == null ? null : o.getTitle());
        m.put("opportunityDate", o == null ? null : o.getOpportunityDate());
        m.put("location", o == null ? null : o.getLocation());
        return m;
    }

    static Map<String, Object> quizAttempt(QuizAttempt a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("score", a.getScore());
        m.put("totalQuestions", a.getTotalQuestions());
        m.put("attemptedAt", a.getAttemptedAt());
        Quiz q = a.getQuiz();
        m.put("quizId", q == null ? null : q.getId());
        m.put("quizTitle", q == null ? null : q.getTitle());
        return m;
    }

    static Map<String, Object> review(ProductReview r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("rating", r.getRating());
        m.put("reviewText", r.getReviewText());
        m.put("reviewerId", r.getUser() == null ? null : r.getUser().getId());
        m.put("reviewerName", r.getUser() == null ? null : r.getUser().getFullName());
        m.put("createdAt", r.getCreatedAt());
        return m;
    }

    static String paymentName(PaymentMethod p) {
        return p == null ? null : p.getDisplayName();
    }
}