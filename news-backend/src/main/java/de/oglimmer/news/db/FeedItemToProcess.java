package de.oglimmer.news.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity(name = "feed_item_to_process")
@EqualsAndHashCode(of = "id")
@ToString(exclude = "feed")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedItemToProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private Feed feed;

    /**
     * ID taken from RSS/Atom feed (e.g. guid)
     */
    @Column(nullable = false, unique = true)
    @NotNull
    private String refId;

    @Lob
    @Column(nullable = false, length = 4096)
    @NotNull
    private String url;

    @Column(nullable = false)
    @NotNull
    private String title;

    @Column(nullable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    private ProcessState processState;

    @Column(nullable = false)
    @NotNull
    private Instant createdOn;

    @Column(nullable = false)
    @NotNull
    private Instant updatedOn;

}
