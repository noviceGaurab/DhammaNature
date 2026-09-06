package io.virinchi.dhammanature.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * These pages were mostly static (Lorem-ipsum) content in the original JSP
 * site with no server-side data, so they are served as-is after the JSP ->
 * Thymeleaf conversion (see /docs or the accompanying README for the mapping
 * table). Pages that needed real backend data (events, donations, discussion,
 * gallery, rewards...) have their own dedicated controllers instead.
 */
@Controller
public class StaticPagesController {

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/blog")
    public String blog() {
        return "blog";
    }

    @GetMapping("/blog/detail")
    public String blogDetail() {
        return "blog-detail";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/community")
    public String community() {
        return "community";
    }

    @GetMapping("/teachings")
    public String teachings() {
        return "teachings";
    }

    @GetMapping("/teachings/detail")
    public String teachingDetail() {
        return "teaching-detail";
    }

    @GetMapping("/upcoming-sermons")
    public String upcomingSermons() {
        return "upcoming-sermons";
    }

    @GetMapping("/upcoming-sermons/detail")
    public String upcomingSermonDetail() {
        return "upcoming-sermon-detail";
    }
}
