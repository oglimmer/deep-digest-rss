/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Entity(name = "news")
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"feed", "votes", "tags"})
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

  @Lob
  @Column(nullable = false, length = 4096)
  private String url;

  @Column(nullable = false)
  private String title;

  @Lob
  @Column(nullable = false, length = 2_000_000)
  @NotNull
  private String text;

  @OneToMany(mappedBy = "news", fetch = FetchType.LAZY)
  private List<NewsVote> votes;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "news_tags",
      joinColumns = @JoinColumn(name = "news_id"),
      inverseJoinColumns = @JoinColumn(name = "tags_id"))
  private List<Tags> tags;

  private Boolean advertising;

  @Column(nullable = false)
  @NotNull
  private Instant createdOn;
}
