package de.oglimmer.news.db;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;


@Getter
@Setter
@Entity(name = "users")
@EqualsAndHashCode(of = "id")
@ToString(exclude = "votes")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String authToken;

    private Instant authTokenValidUntil;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<NewsVote> votes;

    @Column(nullable = false)
    private String timezone;
}
