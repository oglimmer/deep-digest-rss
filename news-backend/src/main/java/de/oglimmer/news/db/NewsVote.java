package de.oglimmer.news.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity(name = "news_vote")
@EqualsAndHashCode(of = "id")
@ToString()
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private News news;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    private LocalDate voteDate;

}
