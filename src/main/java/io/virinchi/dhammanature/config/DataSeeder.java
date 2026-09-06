package io.virinchi.dhammanature.config;

import io.virinchi.dhammanature.model.*;
import io.virinchi.dhammanature.model.enums.*;
import io.virinchi.dhammanature.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bootstraps the admin account (Business Rule 3/8 depend on an admin existing)
 * and, when app.seed-demo-data=true, a small set of demo meditation centers /
 * events / reward catalog / quiz so the app is browsable immediately after
 * `mvn spring-boot:run` without any manual data entry.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MeditationCenterRepository meditationCenterRepository;
    private final EventRepository eventRepository;
    private final RewardCatalogItemRepository rewardCatalogItemRepository;
    private final CharityCampaignRepository charityCampaignRepository;
    private final VolunteerOpportunityRepository volunteerOpportunityRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.seed-demo-data:true}")
    private boolean seedDemoData;

    @Override
    @Transactional
    public void run(String... args) {
        User admin = ensureAdmin();
        if (seedDemoData && meditationCenterRepository.count() == 0) {
            seedDemoData(admin);
        }
    }

    private User ensureAdmin() {
        return userRepository.findByEmailIgnoreCase(adminEmail).orElseGet(() -> userRepository.save(User.builder()
                .fullName("Administrator")
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .acceptedTerms(true)
                .build()));
    }

    private void seedDemoData(User admin) {
        MeditationCenter mokshya = meditationCenterRepository.save(MeditationCenter.builder()
                .name("Mokshya Yoga Retreat Center")
                .location("Kathmandu, Nepal")
                .description("A meditation and wellness center serving both domestic and international participants, offering hybrid online and physical guidance.")
                .contactEmail("info@themokshya.com")
                .website("https://themokshya.com")
                .supportsOnlineSessions(true)
                .supportsPhysicalSessions(true)
                .verified(true)
                .build());

        MeditationCenter tmCenter = meditationCenterRepository.save(MeditationCenter.builder()
                .name("Transcendental Meditation Center")
                .location("Kathmandu, Nepal")
                .description("An established meditation institution that communicates mainly through Facebook, with a website in development.")
                .contactEmail("info@tm-center.org")
                .facebookUrl("https://facebook.com")
                .supportsOnlineSessions(true)
                .supportsPhysicalSessions(true)
                .verified(true)
                .build());

        eventRepository.save(Event.builder()
                .title("Morning Guided Meditation")
                .description("A gentle guided session suitable for beginners and experienced practitioners alike.")
                .eventDate(LocalDateTime.now().plusDays(3).withHour(7).withMinute(0))
                .venue("Mokshya Retreat Hall")
                .mode(SessionMode.HYBRID)
                .capacity(30)
                .meditationCenter(mokshya)
                .build());

        eventRepository.save(Event.builder()
                .title("Weekend Yoga & Sound Healing Retreat")
                .description("A weekend retreat combining yoga, sound healing and silent meditation.")
                .eventDate(LocalDateTime.now().plusDays(10).withHour(9).withMinute(0))
                .venue("Mokshya Retreat Hall")
                .mode(SessionMode.PHYSICAL)
                .capacity(20)
                .meditationCenter(mokshya)
                .build());

        eventRepository.save(Event.builder()
                .title("Evening TM Practice Session")
                .description("A community Transcendental Meditation practice session, open to all levels.")
                .eventDate(LocalDateTime.now().plusDays(5).withHour(18).withMinute(30))
                .venue("TM Center Hall")
                .mode(SessionMode.ONLINE)
                .capacity(50)
                .meditationCenter(tmCenter)
                .build());

        charityCampaignRepository.save(CharityCampaign.builder()
                .title("Food Sponsorship for Ashok Stupa Visitors")
                .description("Sponsor a meal for pilgrims and volunteers during festival season - directly requested by the visitors interviewed at Ashok Stupa.")
                .goalAmount(new BigDecimal("2000"))
                .raisedAmount(new BigDecimal("650"))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(50))
                .status(CampaignStatus.ACTIVE)
                .meditationCenter(mokshya)
                .build());

        volunteerOpportunityRepository.save(VolunteerOpportunity.builder()
                .title("Temple Grounds Cleaning Day")
                .description("Help clean and prepare the grounds ahead of the festival season.")
                .opportunityDate(LocalDate.now().plusDays(14))
                .location("Ashok Stupa")
                .meditationCenter(mokshya)
                .build());

        volunteerOpportunityRepository.save(VolunteerOpportunity.builder()
                .title("Tree Plantation Drive")
                .description("Join fellow volunteers in planting trees around the retreat center.")
                .opportunityDate(LocalDate.now().plusDays(21))
                .location("Mokshya Retreat Center")
                .meditationCenter(mokshya)
                .build());

        rewardCatalogItemRepository.save(RewardCatalogItem.builder()
                .name("Incense Coupon")
                .description("Redeem for a free box of incense at any participating center.")
                .pointsCost(20)
                .category(RewardCategory.INCENSE_COUPON)
                .active(true)
                .build());

        rewardCatalogItemRepository.save(RewardCatalogItem.builder()
                .name("10% Meditation Program Discount")
                .description("10% off your next booked meditation program.")
                .pointsCost(50)
                .category(RewardCategory.MEDITATION_DISCOUNT)
                .active(true)
                .build());

        rewardCatalogItemRepository.save(RewardCatalogItem.builder()
                .name("Sponsor a Meal (Charity)")
                .description("Convert your points into a meal sponsorship for someone in need.")
                .pointsCost(30)
                .category(RewardCategory.CHARITY_SPONSORSHIP)
                .active(true)
                .build());

        rewardCatalogItemRepository.save(RewardCatalogItem.builder()
                .name("Certificate of Participation")
                .description("A printable certificate recognizing your continued participation.")
                .pointsCost(15)
                .category(RewardCategory.CERTIFICATE)
                .active(true)
                .build());

        Quiz quiz = quizRepository.save(Quiz.builder()
                .title("Basics of Dhamma")
                .description("A short quiz on the fundamentals of Buddhist teachings.")
                .rewardPoints(10)
                .build());

        quizQuestionRepository.saveAll(List.of(
                QuizQuestion.builder().quiz(quiz)
                        .questionText("What are the Four Noble Truths part of?")
                        .optionA("Buddhist teachings").optionB("Hindu rituals")
                        .optionC("Taoist philosophy").optionD("Confucian ethics")
                        .correctOption("A").build(),
                QuizQuestion.builder().quiz(quiz)
                        .questionText("What does 'Dhamma' commonly refer to?")
                        .optionA("A type of temple").optionB("The teaching / natural law of the Buddha")
                        .optionC("A meditation posture").optionD("A festival")
                        .correctOption("B").build(),
                QuizQuestion.builder().quiz(quiz)
                        .questionText("Which practice is central to most meditation centers featured on this platform?")
                        .optionA("Sound healing only").optionB("Silent meditation only")
                        .optionC("A mix of meditation, yoga and mindfulness practices").optionD("None of the above")
                        .correctOption("C").build()
        ));
    }
}
