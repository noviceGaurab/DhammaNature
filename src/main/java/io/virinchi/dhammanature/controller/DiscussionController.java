package io.virinchi.dhammanature.controller;

import io.virinchi.dhammanature.config.SessionUserResolver;
import io.virinchi.dhammanature.service.DiscussionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** FR-06: Community Discussion Forum - the platform's main topic is "general"; any page can spin up its own topic slug. */
@Controller
@RequiredArgsConstructor
public class DiscussionController {

    private static final String DEFAULT_SLUG = "general";

    private final DiscussionService discussionService;
    private final SessionUserResolver sessionUserResolver;

    @GetMapping("/discuss")
    public String discuss(@RequestParam(defaultValue = DEFAULT_SLUG) String topic, Model model) {
        discussionService.topicFor(topic, "Community Discussion");
        model.addAttribute("slug", topic);
        model.addAttribute("comments", discussionService.commentsFor(topic));
        return "discuss";
    }

    @PostMapping("/discuss")
    public String post(@RequestParam(defaultValue = DEFAULT_SLUG) String topic,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) String email,
                        @RequestParam String content,
                        HttpSession session, RedirectAttributes redirectAttributes) {
        var user = sessionUserResolver.resolve(session).orElse(null);
        if ((user == null) && (name == null || name.isBlank())) {
            redirectAttributes.addFlashAttribute("error", "Please enter your name, or log in, to post.");
            return "redirect:/discuss?topic=" + topic;
        }
        discussionService.post(topic, "Community Discussion", user, name, email, content);
        return "redirect:/discuss?topic=" + topic;
    }
}
