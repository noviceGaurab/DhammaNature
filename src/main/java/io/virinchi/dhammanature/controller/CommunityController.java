package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.repository.CommentRepository;
import io.virinchi.dhammanature.repository.DiscussionTopicRepository;
import io.virinchi.dhammanature.repository.UserRepository;
import io.virinchi.dhammanature.service.CharityCampaignService;
import io.virinchi.dhammanature.service.EventService;
import io.virinchi.dhammanature.service.QuizService;
import io.virinchi.dhammanature.service.VolunteerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Community hub: aggregates member, discussion and participation stats plus recent topics. */
@Controller
@RequiredArgsConstructor
public class CommunityController {

    private final UserRepository userRepository;
    private final DiscussionTopicRepository discussionTopicRepository;
    private final CommentRepository commentRepository;
    private final EventService eventService;
    private final VolunteerService volunteerService;
    private final CharityCampaignService charityCampaignService;
    private final QuizService quizService;

    @GetMapping("/community")
    public String community(Model model) {
        var upcomingEvents = eventService.upcoming();
        var opportunities = volunteerService.upcoming();
        var campaigns = charityCampaignService.active();
        model.addAttribute("memberCount", userRepository.count());
        model.addAttribute("topicCount", discussionTopicRepository.count());
        model.addAttribute("commentCount", commentRepository.countByHiddenFalse());
        model.addAttribute("participantCount", commentRepository.countDistinctParticipants());
        model.addAttribute("recentTopics", discussionTopicRepository.findTop5ByOrderByCreatedAtDesc());
        model.addAttribute("upcomingEvents", upcomingEvents.stream().limit(3).toList());
        model.addAttribute("eventCount", upcomingEvents.size());
        model.addAttribute("volunteerOpportunities", opportunities.stream().limit(3).toList());
        model.addAttribute("volunteerCount", opportunities.size());
        model.addAttribute("charityCampaigns", campaigns);
        model.addAttribute("charityCount", campaigns.size());
        model.addAttribute("quizzes", quizService.all());
        return "community";
    }
}