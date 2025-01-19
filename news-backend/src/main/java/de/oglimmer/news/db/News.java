package de.oglimmer.news.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity(name = "news")
@EqualsAndHashCode(of = "id")
@ToString(exclude = "feed")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Feed feed;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private FeedItemToProcess originalFeedItem;

    @Column(nullable = false)
//    @NotNull
    private String url;

    @Column(nullable = false)
//    @NotNull
    private String title;

    @Lob
    @Column(nullable = false, length = 2_000_000)
    @NotNull
    private String text;

    @Column(nullable = false)
    @NotNull
    private Instant createdOn;

}
