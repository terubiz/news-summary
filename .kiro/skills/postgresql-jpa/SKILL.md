---
name: postgresql-jpa
description: PostgreSQL + Spring Data JPA best practices. Use when designing database schemas, creating JPA entities, or implementing repositories. Covers entity mapping, query methods, transactions, and PostgreSQL-specific features.
---

## PostgreSQL + Spring Data JPA Best Practices

### Entity Design

```kotlin
@Entity
@Table(name = "summaries")
data class Summary(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val summaryText: String,
    
    @Enumerated(EnumType.STRING)
    val status: SummaryStatus,
    
    @CreationTimestamp
    val createdAt: Instant? = null,
    
    @UpdateTimestamp
    val updatedAt: Instant? = null
)
```

### Repository Patterns

- Extend `JpaRepository<T, ID>` for CRUD operations
- Use query methods for simple queries
- Use `@Query` for complex queries
- Use `@Modifying` for UPDATE/DELETE queries

```kotlin
interface SummaryRepository : JpaRepository<Summary, Long> {
    fun findByStatusAndCreatedAtAfter(
        status: SummaryStatus,
        after: Instant
    ): List<Summary>
    
    @Query("SELECT s FROM Summary s WHERE s.userId = :userId ORDER BY s.createdAt DESC")
    fun findRecentByUser(@Param("userId") userId: Long, pageable: Pageable): Page<Summary>
}
```

### Transactions

- Use `@Transactional` on service methods
- Set `readOnly = true` for read operations
- Handle `OptimisticLockException` for concurrent updates
- Use `@Transactional(propagation = REQUIRES_NEW)` for independent transactions

### PostgreSQL-Specific Features

- Use `@Type(JsonType::class)` for JSON columns (Hibernate 6+)
- Use `@Column(columnDefinition = "TEXT")` for large text
- Use `@Column(columnDefinition = "ARRAY")` for array columns
- Leverage PostgreSQL full-text search with native queries

### Performance

- Add indexes on frequently queried columns
- Use `@BatchSize` for N+1 query prevention
- Use `JOIN FETCH` for eager loading
- Monitor query performance with `spring.jpa.show-sql=true` in dev

### Migrations

- Use Flyway or Liquibase for schema versioning
- Never modify existing migrations
- Test migrations on staging before production
