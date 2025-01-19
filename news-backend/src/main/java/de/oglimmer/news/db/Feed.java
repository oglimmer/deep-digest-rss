package de.oglimmer.news.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity(name = "feed")
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"feedItems", "news"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private String url;

    @Column(nullable = false)
    @NotNull
    private String title;

    @Column(nullable = false)
    @NotNull
    private Instant createdOn;

    @OneToMany(mappedBy = "feed", fetch = FetchType.LAZY)
    private List<FeedItemToProcess> feedItems;

    @OneToMany(mappedBy = "feed", fetch = FetchType.LAZY)
    private List<News> news;

}
