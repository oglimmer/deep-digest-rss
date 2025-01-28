package de.oglimmer.news.db;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity(name = "tags")
@EqualsAndHashCode(of = "id")
@ToString(exclude = "news")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tags {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private List<News> news;

    private String text;


}