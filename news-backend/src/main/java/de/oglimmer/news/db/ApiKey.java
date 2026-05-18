/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package de.oglimmer.news.db;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Getter
@Setter
@Entity(name = "api_keys")
@EqualsAndHashCode(of = "id")
@ToString(exclude = "roles")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String name;

  @Column(name = "key_hash", nullable = false)
  private String keyHash;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "last_used_at")
  private Instant lastUsedAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "api_key_roles",
      joinColumns = @JoinColumn(name = "api_key_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();
}
