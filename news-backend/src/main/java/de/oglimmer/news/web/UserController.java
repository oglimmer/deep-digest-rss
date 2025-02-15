package de.oglimmer.news.web;

import de.oglimmer.news.service.NewsService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class UserController {

    private final NewsService newsService;

    @GetMapping("/{id}/voted-news")
    public List<String> voteNews(@PathVariable String id, @RequestParam(required = false, defaultValue = "") String date, Authentication authentication) {
        String email = authentication.getName();
        return newsService.userNews(id, date, email);
    }
}
