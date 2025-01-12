package de.oglimmer.news.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity(name = "news")
@EqualsAndHashCode(of = "id")
@ToString()
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotNull
    private String refId;

    @Column(nullable = false)
    @NotNull
    private String url;

    @Column(nullable = false)
    @NotNull
    private String title;

    @Lob
    @Column(nullable = false, length = 2_000_000)
    @NotNull
    private String text;

    @Column(nullable = false)
    @NotNull
    private Instant createdOn;

}
