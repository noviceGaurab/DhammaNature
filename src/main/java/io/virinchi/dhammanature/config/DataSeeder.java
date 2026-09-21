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
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final BlogPostRepository blogPostRepository;
    private final DiscussionTopicRepository discussionTopicRepository;
    private final CommentRepository commentRepository;

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
        if (seedDemoData) {
            if (meditationCenterRepository.count() == 0) {
                seedDemoData(admin);
            }
            ensureUpcomingDemoContent();
            ensureCenterCoverImages();
            seedMarketplace();
            seedBlog(admin);
            seedBlogComments();
        }
    }

    /** Keeps upcoming events / volunteer opportunities populated on long-lived databases
     * where the original "now()+days" demo rows have aged into the past. */
    private void ensureUpcomingDemoContent() {
        List<MeditationCenter> centers = meditationCenterRepository.findAll();
        if (centers.isEmpty()) {
            return;
        }
        MeditationCenter center = centers.get(0);

        long upcomingEvents = eventRepository.findAll().stream()
                .filter(e -> e.getEventDate().isAfter(LocalDateTime.now()))
                .count();
        if (upcomingEvents < 3) {
            seedUpcomingEvents(center);
        }

        long upcomingOpportunities = volunteerOpportunityRepository.findAll().stream()
                .filter(o -> !o.getOpportunityDate().isBefore(LocalDate.now()))
                .count();
        if (upcomingOpportunities < 2) {
            seedUpcomingOpportunities(center);
        }
    }

    /** Assigns a distinct cover image to each seeded center so the directory
     * never shows the same fallback thumbnail on multiple cards. */
    private void ensureCenterCoverImages() {
        setCoverImage("Mokshya Yoga Retreat Center", "pexels-eky-rima-nurya-ganda-174710171-15147310");
        setCoverImage("Transcendental Meditation Center", "pexels-2152214764-32117889");
    }

    private void setCoverImage(String centerName, String imageName) {
        meditationCenterRepository.findByNameContainingIgnoreCase(centerName).stream()
                .filter(c -> c.getName().equalsIgnoreCase(centerName))
                .findFirst()
                .ifPresent(c -> {
                    if (!imageName.equals(c.getCoverImageUrl())) {
                        c.setCoverImageUrl(imageName);
                        meditationCenterRepository.save(c);
                    }
                });
    }

    private void seedUpcomingEvents(MeditationCenter center) {
        eventRepository.save(Event.builder()
                .title("Morning Guided Meditation")
                .description("A gentle guided session suitable for beginners and experienced practitioners alike.")
                .eventDate(LocalDateTime.now().plusDays(3).withHour(7).withMinute(0))
                .venue("Mokshya Retreat Hall")
                .mode(SessionMode.HYBRID)
                .capacity(30)
                .meditationCenter(center)
                .build());

        eventRepository.save(Event.builder()
                .title("Weekend Yoga & Sound Healing Retreat")
                .description("A weekend retreat combining yoga, sound healing and silent meditation.")
                .eventDate(LocalDateTime.now().plusDays(10).withHour(9).withMinute(0))
                .venue("Mokshya Retreat Hall")
                .mode(SessionMode.PHYSICAL)
                .capacity(20)
                .meditationCenter(center)
                .build());

        eventRepository.save(Event.builder()
                .title("Evening TM Practice Session")
                .description("A community transcendental meditation practice session, open to all levels.")
                .eventDate(LocalDateTime.now().plusDays(5).withHour(18).withMinute(30))
                .venue("TM Center Hall")
                .mode(SessionMode.ONLINE)
                .capacity(50)
                .meditationCenter(center)
                .build());

        eventRepository.save(Event.builder()
                .title("Dhamma Discussion Circle")
                .description("An open-floor discussion on the weekly teaching, guided by senior practitioners.")
                .eventDate(LocalDateTime.now().plusDays(7).withHour(17).withMinute(30))
                .venue("Dhamma Nature Hall")
                .mode(SessionMode.HYBRID)
                .capacity(40)
                .meditationCenter(center)
                .build());

        eventRepository.save(Event.builder()
                .title("Full-Moon Puja & Silent Sitting")
                .description("A tranquil evening of chanting, candlelight puja and a long silent sit.")
                .eventDate(LocalDateTime.now().plusDays(12).withHour(19).withMinute(0))
                .venue("Dhamma Nature Hall")
                .mode(SessionMode.PHYSICAL)
                .capacity(35)
                .meditationCenter(center)
                .build());

        eventRepository.save(Event.builder()
                .title("Online Metta Meditation Session")
                .description("A guided loving-kindness meditation over video call, open to participants worldwide.")
                .eventDate(LocalDateTime.now().plusDays(8).withHour(20).withMinute(0))
                .venue("Online - Zoom")
                .mode(SessionMode.ONLINE)
                .capacity(100)
                .meditationCenter(center)
                .build());
    }

    private void seedUpcomingOpportunities(MeditationCenter center) {
        volunteerOpportunityRepository.save(VolunteerOpportunity.builder()
                .title("Temple Grounds Cleaning Day")
                .description("Help clean and prepare the grounds ahead of the festival season.")
                .opportunityDate(LocalDate.now().plusDays(14))
                .location("Ashok Stupa")
                .meditationCenter(center)
                .build());

        volunteerOpportunityRepository.save(VolunteerOpportunity.builder()
                .title("Tree Plantation Drive")
                .description("Join fellow volunteers in planting trees around the retreat center.")
                .opportunityDate(LocalDate.now().plusDays(21))
                .location("Mokshya Retreat Center")
                .meditationCenter(center)
                .build());

        volunteerOpportunityRepository.save(VolunteerOpportunity.builder()
                .title("Community Meal Distribution")
                .description("Package and serve warm meals to visitors and pilgrims at Ashok Stupa.")
                .opportunityDate(LocalDate.now().plusDays(9))
                .location("Ashok Stupa")
                .meditationCenter(center)
                .build());

        volunteerOpportunityRepository.save(VolunteerOpportunity.builder()
                .title("Library & Archive Helpers")
                .description("Help catalogue dhamma books and digital resources at the retreat library.")
                .opportunityDate(LocalDate.now().plusDays(28))
                .location("Mokshya Retreat Center")
                .meditationCenter(center)
                .build());
    }

    private void seedBlog(User admin) {
        if (blogPostRepository.count() > 0) {
            return;
        }
        blogPostRepository.save(BlogPost.builder()
                .title("Dhamma Will Uplift Your Mentality")
                .category("meditation")
                .content("Every morning we begin again. This simple truth is the heart of the practice - the mind, like a lake, can be disturbed, yet it always returns to stillness.\n\n"
                        + "Read on for five ways to bring the Dhamma into ordinary life: sit for five minutes before the day's noise begins, speak one fewer unkind word, offer a silent blessing to someone you find difficult, walk slowly enough to notice your breath, and return to the present moment whenever the past or future pulls you away.\n\n"
                        + "A steady mind, like a mountain, cannot be swayed. Practice is not about becoming someone new; it is about remembering who you already are.")
                .imageUrl("pexels-tkirkgoz-13047288.jpg")
                .author(admin)
                .authorName(admin.getFullName())
                .status(BlogStatus.APPROVED)
                .build());

        blogPostRepository.save(BlogPost.builder()
                .title("Mindful Living Practices")
                .category("meditation")
                .content("Mindful living is not another item on the to-do list. It is a way of meeting ordinary moments with full attention - the taste of tea, the weight of a cup, the sound of rain on the roof.\n\n"
                        + "Start with the one thing you can keep. Keep one wholesome action, repeat it daily, and let it grow roots before adding another. Morning breath, one mindful meal, an evening walk, a gratitude note, a weekly sutta reading - each is enough on its own.")
                .imageUrl("pexels-pixabay-220650.jpg")
                .author(admin)
                .authorName(admin.getFullName())
                .status(BlogStatus.APPROVED)
                .build());

        blogPostRepository.save(BlogPost.builder()
                .title("Building a Healthy Lifestyle, One Mindful Step at a Time")
                .category("lifestyle")
                .content("We often plan grand routines and give up within a week. The Buddha's way is smaller: keep one wholesome action, repeat it daily, and let it grow roots before adding another.\n\n"
                        + "When you build a life around these small, repeating wholes, the body and the mind settle. Health becomes less a goal you chase and more a rhythm you keep - waking, eating, working, resting, all in its own turn.")
                .imageUrl("pexels-ajaybhargavguduru-939700.jpg")
                .author(admin)
                .authorName(admin.getFullName())
                .status(BlogStatus.APPROVED)
                .build());

        blogPostRepository.save(BlogPost.builder()
                .title("Community and Connection")
                .category("community")
                .content("Sangha is not a building - it is the hands that offer dana, the ears that listen, the hearts that practice together.\n\n"
                        + "Connection grows in small gatherings: a shared meal, a working bee, a circle of silence. Join a local event, bring a friend, and let the group carry what the solitary mind cannot.")
                .imageUrl("pexels-cryschanxanhy-29547000.jpg")
                .author(admin)
                .authorName(admin.getFullName())
                .status(BlogStatus.APPROVED)
                .build());

        blogPostRepository.save(BlogPost.builder()
                .title("About to leave on a ten-day retreat")
                .category("general")
                .content("This is a sample pending article, waiting for an administrator to review and approve it. In the admin panel, navigate to the Blog Posts page and use the Approve or Reject buttons.\n\n"
                        + "Once approved, the article will appear on the public blog.")
                .imageUrl("pexels-ron-lach-10461522.jpg")
                .author(admin)
                .authorName(admin.getFullName())
.status(BlogStatus.PENDING)
            .build());
    }

    /** Attaches the classic demonstration comments (John Doe / Jane Smith) to each approved blog post
     * as real discussion comments so blog replies render through the nested tree (IDEMPOTENT - only runs
     * per topic when that topic has no comments yet). */
    private void seedBlogComments() {
        List<BlogPost> posts = blogPostRepository.findAll().stream()
                .filter(p -> p.getStatus() == BlogStatus.APPROVED)
                .toList();
        for (BlogPost post : posts) {
            String slug = "blog-" + post.getId();
            DiscussionTopic topic = discussionTopicRepository.findBySlug(slug).orElseGet(() ->
                    discussionTopicRepository.save(DiscussionTopic.builder()
                            .slug(slug)
                            .title("Comments on \"" + post.getTitle() + "\"")
                            .category("blog")
                            .build()));
            boolean hasComments = !commentRepository.findByTopic_IdAndHiddenFalseOrderByCreatedAtAsc(topic.getId()).isEmpty();
            if (hasComments) {
                continue;
            }
            Comment john = commentRepository.save(Comment.builder()
                    .topic(topic)
                    .name("John Doe")
                    .email("john.doe@dhammanature.org")
                    .title("Wise words")
                    .content("This really resonated with me. The quiet practice of returning to the breath has been transformative lately - thank you for writing it.")
                    .build());
            commentRepository.save(Comment.builder()
                    .topic(topic)
                    .name("Jane Smith")
                    .email("jane.smith@dhammanature.org")
                    .title("Another perspective")
                    .content("Lovely article! I found the section on letting the mind settle slowly especially helpful. I'd love to read more on this topic.")
                    .parent(john)
                    .build());
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
                .coverImageUrl("pexels-eky-rima-nurya-ganda-174710171-15147310")
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
                .coverImageUrl("pexels-2152214764-32117889")
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

    private void seedMarketplace() {
        Vendor himalayan = ensureVendor(
                "vendor@dhammanature.org",
                "Himalayan Sacred Crafts",
                "Patan, Lalitpur, Nepal");
        Vendor lotus = ensureVendor(
                "lotus@dhammanature.org",
                "Lotus Valley Arts",
                "Boudha, Kathmandu, Nepal");
        Vendor stupa = ensureVendor(
                "stupa@dhammanature.org",
                "Ashok Stupa Collective",
                "Ashok Stupa, Lalitpur, Nepal");

        seedProduct(himalayan, "Sandalwood Mala",
                "108-bead sandalwood prayer mala for japa and mindfulness practice.",
                "18.00", 40, ProductCategory.PRAYER_BEADS, "beads.png");
        seedProduct(himalayan, "Rudraksha Prayer Beads",
                "Hand-strung rudraksha beads blessed for daily meditation.",
                "24.00", 25, ProductCategory.PRAYER_BEADS, "beads 2nd.png");
        seedProduct(lotus, "Bodhi Seed Mala",
                "Classic bodhi seed mala with guru bead, ideal for longer sittings.",
                "27.00", 22, ProductCategory.PRAYER_BEADS, "3rd beads.png");
        seedProduct(stupa, "Rosewood Wrist Mala",
                "Compact 21-bead wrist mala for on-the-go mindfulness.",
                "12.50", 55, ProductCategory.PRAYER_BEADS, "pexels-ron-lach-10461522.jpg");
        seedProduct(lotus, "Crystal Quartz Mala",
                "Clear quartz beads for focus and clarity during meditation.",
                "32.00", 18, ProductCategory.PRAYER_BEADS, "pexels-sibi-ar-3290250-4940194.jpg");

        seedProduct(himalayan, "Himalayan Incense Bundle",
                "Naturally scented incense sticks crafted with Himalayan herbs.",
                "9.50", 80, ProductCategory.INCENSE, "incense bundle.png");
        seedProduct(himalayan, "Temple Resin Incense",
                "Traditional resin incense for shrine and home practice.",
                "12.00", 50, ProductCategory.INCENSE, "pexels-tkirkgoz-15277853.jpg");
        seedProduct(lotus, "Nag Champa Gift Pack",
                "A fragrant multipack of nag champa sticks for daily offerings.",
                "8.00", 90, ProductCategory.INCENSE, "bundle.png");
        seedProduct(stupa, "Cedarwood Cone Incense",
                "Slow-burning cedar cones with a grounding forest aroma.",
                "7.50", 70, ProductCategory.INCENSE, "pexels-tkirkgoz-13047288.jpg");
        seedProduct(lotus, "Sandalwood Incense Rolls",
                "Premium sandalwood rolls packaged for temple and home use.",
                "11.00", 60, ProductCategory.INCENSE, "pexels-eky-rima-nurya-ganda-174710171-15147310.jpg");

        seedProduct(himalayan, "Introduction to the Dhamma",
                "A clear beginner guide to the Buddha's teachings and daily practice.",
                "15.00", 30, ProductCategory.BOOKS, "intro to dhamma.png");
        seedProduct(himalayan, "Mindfulness Journal",
                "Lined journal with gentle prompts for reflection after meditation.",
                "11.00", 45, ProductCategory.BOOKS, "book.png");
        seedProduct(lotus, "Four Noble Truths Handbook",
                "A concise study companion with notes for discussion groups.",
                "13.50", 28, ProductCategory.BOOKS, "pexels-pexels-user-1493533273-27021625.jpg");
        seedProduct(stupa, "Loving-Kindness Practice Guide",
                "Step-by-step metta instructions for beginners and returners.",
                "10.00", 40, ProductCategory.BOOKS, "pexels-2152214764-32117889.jpg");
        seedProduct(lotus, "Children's Dhamma Stories",
                "Illustrated stories introducing kindness and mindfulness to kids.",
                "14.00", 35, ProductCategory.BOOKS, "pexels-nishantaneja-2385606.jpg");

        seedProduct(himalayan, "Brass Buddha Statue",
                "Compact brass Buddha figure suitable for a home shrine.",
                "42.00", 15, ProductCategory.STATUES, "carved buddha.png");
        seedProduct(lotus, "Meditating Buddha Figurine",
                "Serene seated Buddha cast for altar or meditation corner.",
                "38.00", 20, ProductCategory.STATUES, "carved 2nd buddh.png");
        seedProduct(stupa, "Standing Blessing Buddha",
                "Standing Buddha in blessing mudra, finished in warm bronze tone.",
                "55.00", 10, ProductCategory.STATUES, "pexels-pixabay-45178.jpg");
        seedProduct(lotus, "Mini Shrine Buddha Set",
                "Small statue with offering dish — perfect for travel altars.",
                "29.00", 24, ProductCategory.STATUES, "pexels-pixabay-50709.jpg");

        seedProduct(himalayan, "Meditation Cushion",
                "Firm cotton zafu cushion for seated meditation.",
                "28.00", 20, ProductCategory.ACCESSORIES, "pexels-rdne-8710873.jpg");
        seedProduct(himalayan, "Singing Bowl Set",
                "Hand-hammered singing bowl with wooden striker for sound practice.",
                "36.00", 18, ProductCategory.ACCESSORIES, "pexels-koithyr-1360255.jpg");
        seedProduct(lotus, "Yoga Mat & Strap Kit",
                "Non-slip mat with cotton strap for home practice sessions.",
                "31.00", 26, ProductCategory.ACCESSORIES, "pouch for meditation.png");
        seedProduct(stupa, "Eye Pillow Set",
                "Lavender-filled eye pillows for savasana and rest.",
                "16.00", 40, ProductCategory.ACCESSORIES, "pexels-sibi-ar-3290250-4940194.jpg");
        seedProduct(lotus, "Temple Bell",
                "Clear-toned hand bell for marking the start and end of sits.",
                "19.50", 30, ProductCategory.ACCESSORIES, "pexels-caleboquendo-3030090.jpg");
        seedProduct(stupa, "Meditation Timer",
                "Simple analog timer with a soft chime — no screens needed.",
                "21.00", 22, ProductCategory.ACCESSORIES, "pexels-rdne-8710873.jpg");

        seedProduct(himalayan, "Handwoven Prayer Flag Set",
                "Five-color prayer flags handwoven by local artisans.",
                "14.00", 35, ProductCategory.HANDICRAFTS, "pexels-nishantaneja-2385606.jpg");
        seedProduct(himalayan, "Thangka-Inspired Wall Art",
                "Small cloth print inspired by traditional thangka motifs.",
                "22.00", 12, ProductCategory.HANDICRAFTS, "Wall art.jpg");
        seedProduct(lotus, "Handmade Offering Bowls",
                "Set of seven brass offering bowls for shrine arrangements.",
                "33.00", 16, ProductCategory.HANDICRAFTS, "pexels-tkirkgoz-15277853.jpg");
        seedProduct(stupa, "Wool Meditation Shawl",
                "Soft handwoven shawl for cool morning sits.",
                "26.00", 20, ProductCategory.HANDICRAFTS, "pexels-cryschanxanhy-29547000.jpg");
        seedProduct(lotus, "Carved Wooden Altar Shelf",
                "Compact wall shelf for statues, candles, and incense.",
                "48.00", 8, ProductCategory.HANDICRAFTS, "carved table.png");
        seedProduct(stupa, "Lokta Paper Notebook",
                "Eco lokta paper notebook for retreat notes and reflections.",
                "9.00", 50, ProductCategory.HANDICRAFTS, "pexels-ajaybhargavguduru-939700.jpg");
    }

    private Vendor ensureVendor(String email, String vendorName, String address) {
        User vendorUser = userRepository.findByEmailIgnoreCase(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .fullName(vendorName)
                        .email(email)
                        .passwordHash(passwordEncoder.encode("Vendor123!"))
                        .role(Role.VENDOR)
                        .acceptedTerms(true)
                        .build()));

        return vendorRepository.findByUser_Id(vendorUser.getId()).orElseGet(() ->
                vendorRepository.save(Vendor.builder()
                        .vendorName(vendorName)
                        .contactDetails(email)
                        .address(address)
                        .verified(true)
                        .user(vendorUser)
                        .build()));
    }

    private void seedProduct(Vendor vendor, String name, String description, String price,
                             int stock, ProductCategory category, String imageUrl) {
        productRepository.findByProductNameContainingIgnoreCase(name).stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresentOrElse(p -> {
                    if (!imageUrl.equals(p.getImageUrl())) {
                        p.setImageUrl(imageUrl);
                        productRepository.save(p);
                    }
                }, () -> productRepository.save(Product.builder()
                        .vendor(vendor)
                        .productName(name)
                        .description(description)
                        .price(new BigDecimal(price))
                        .stockQuantity(stock)
                        .category(category)
                        .imageUrl(imageUrl)
                        .build()));
    }
}
