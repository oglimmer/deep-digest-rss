/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity(name = "roles")
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String name;
}
