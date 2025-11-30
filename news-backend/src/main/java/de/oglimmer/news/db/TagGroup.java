/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Entity(name = "tag_group")
@EqualsAndHashCode(of = "id")
@ToString()
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagGroup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotNull
  private String title;

  @Column(nullable = false)
  @Temporal(TemporalType.DATE)
  @NotNull
  private LocalDate createdOn;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "tag_group_tags",
      joinColumns = @JoinColumn(name = "tag_group_id"),
      inverseJoinColumns = @JoinColumn(name = "tags_id"))
  private List<Tags> tags;
}
