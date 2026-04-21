---
name: spring-boot-kotlin
description: Spring Boot 3.x + Kotlin development best practices. Use when creating Spring Boot services, REST APIs, domain services, or Kotlin-based backend components. Covers idiomatic Kotlin, Spring annotations, dependency injection, and layered architecture.
---

## Spring Boot + Kotlin Best Practices

### Project Structure

Follow layered architecture:
```
com.example.app/
├── api/           # REST controllers, request/response DTOs
├── domain/        # Domain models, services, business logic
├── infrastructure/# External integrations, repositories
└── config/        # Spring configuration classes
```

### Idiomatic Kotlin

- Use `data class` for DTOs and entities
- Prefer `val` over `var` for immutability
- Use nullable types (`?`) explicitly
- Leverage extension functions for utility methods
- Use `sealed class` for state modeling

### Spring Annotations

- `@RestController` + `@RequestMapping` for REST endpoints
- `@Service` for domain services
- `@Repository` for data access
- `@Configuration` + `@Bean` for manual bean definitions
- `@Value` or `@ConfigurationProperties` for externalized config

### Dependency Injection

- Use constructor injection (Kotlin primary constructor)
- Avoid field injection (`@Autowired` on properties)
- Mark services as `open` or use `all-open` plugin for Spring proxies

### Error Handling

- Use `@ControllerAdvice` + `@ExceptionHandler` for global error handling
- Return consistent error response DTOs
- Log exceptions with context

### Testing

- Use `@SpringBootTest` for integration tests
- Use `@WebMvcTest` for controller tests
- Mock external dependencies with `@MockBean`
