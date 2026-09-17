package io.virinchi.dhammanature.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.dto.ParticipantView;
import io.virinchi.dhammanature.model.User;
import io.virinchi.dhammanature.service.DiscussionService;
import io.virinchi.dhammanature.service.NudgeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** FR-06: Community Discussion Forum - the platform's main topic is "general"; any page can spin up its own topic slug. */
@Controller
@RequiredArgsConstructor
public class DiscussionController {

    private static final String DEFAULT_SLUG = "general";

    private final DiscussionService discussionService;
    private final NudgeService nudgeService;
    private final SessionUserResolver sessionUserResolver;
    private final ObjectMapper objectMapper;

    @GetMapping("/discuss")
    public String discuss(@RequestParam(defaultValue = DEFAULT_SLUG) String topic, Model model) {
        discussionService.topicFor(topic, "Community Discussion");
        model.addAttribute("slug", topic);
        model.addAttribute("comments", discussionService.commentTree(topic));
        model.addAttribute("totalPosts", discussionService.totalPosts());
        model.addAttribute("topicsCount", discussionService.topicCount());
        model.addAttribute("participants", discussionService.participants());
        List<ParticipantView> participantList = discussionService.participantList();
        model.addAttribute("participantList", participantList);
        try {
            model.addAttribute("participantsJson", objectMapper.writeValueAsString(participantList));
        } catch (JsonProcessingException e) {
            model.addAttribute("participantsJson", "[]");
        }
        return "discuss";
    }

    @PostMapping("/discuss")
    public String post(@RequestParam(defaultValue = DEFAULT_SLUG) String topic,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) String title,
                        @RequestParam(required = false) Integer parentId,
                        @RequestParam String content,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        var user = sessionUserResolver.resolve(session).orElse(null);
        if ((user == null) && (name == null || name.isBlank())) {
            redirectAttributes.addFlashAttribute("error", "Please enter your name, or log in, to post.");
            return "redirect:/discuss?topic=" + topic;
        }
        discussionService.post(topic, "Community Discussion", user, name, email, title, content, parentId);
        return "redirect:/discuss?topic=" + topic;
    }

    // ===== Edit / delete a comment (author-only) =====

    @PostMapping("/discuss/comment/{id}/delete")
    public String deleteComment(@PathVariable Integer id,
                                @RequestParam(defaultValue = DEFAULT_SLUG) String topic,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        var user = sessionUserResolver.resolve(session).orElse(null);
        try {
            discussionService.deleteOwned(id, user);
            redirectAttributes.addFlashAttribute("success", "Your comment was deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/discuss?topic=" + topic;
    }

    @PostMapping("/discuss/comment/{id}/edit")
    public String editComment(@PathVariable Integer id,
                              @RequestParam(defaultValue = DEFAULT_SLUG) String topic,
                              @RequestParam String content,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        var user = sessionUserResolver.resolve(session).orElse(null);
        try {
            discussionService.updateOwned(id, content, user);
            redirectAttributes.addFlashAttribute("success", "Your comment was updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/discuss?topic=" + topic;
    }

    // ===== Hover-on-participant nudges =====

    @PostMapping("/discuss/message")
    public String messageParticipant(@RequestParam(defaultValue = DEFAULT_SLUG) String topic,
                                     @RequestParam Integer participantId,
                                     @RequestParam String message,
                                     HttpSession session, RedirectAttributes redirectAttributes) {
        var sender = sessionUserResolver.resolve(session).orElse(null);
        if (sender == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to send a personal message.");
            return "redirect:/discuss?topic=" + topic;
        }
        if (message == null || message.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Write something before sending a personal message.");
            return "redirect:/discuss?topic=" + topic;
        }
        nudgeService.sendMessage(sender, participantId, message);
        redirectAttributes.addFlashAttribute("success", "Your message was sent.");
        return "redirect:/discuss?topic=" + topic;
    }

    @PostMapping("/discuss/quote")
    public String sendQuote(@RequestParam(defaultValue = DEFAULT_SLUG) String topic,
                            @RequestParam Integer participantId,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        var sender = sessionUserResolver.resolve(session).orElse(null);
        if (sender == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to send a Dhamma quote.");
            return "redirect:/discuss?topic=" + topic;
        }
        nudgeService.sendQuote(sender, participantId);
        redirectAttributes.addFlashAttribute("success", "A Dhamma quote was sent.");
        return "redirect:/discuss?topic=" + topic;
    }

    @PostMapping("/discuss/nudge")
    public String nudgeParticipant(@RequestParam(defaultValue = DEFAULT_SLUG) String topic,
                                   @RequestParam Integer participantId,
                                   HttpSession session, RedirectAttributes redirectAttributes) {
        var sender = sessionUserResolver.resolve(session).orElse(null);
        if (sender == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to nudge a participant.");
            return "redirect:/discuss?topic=" + topic;
        }
        nudgeService.sendAfkNudge(sender, participantId);
        redirectAttributes.addFlashAttribute("success", "Your warm nudge was sent — thank you for caring.");
        return "redirect:/discuss?topic=" + topic;
    }
}