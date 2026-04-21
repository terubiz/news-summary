---
name: spring-ai-integration
description: Spring AI integration patterns for LLM-powered features. Use when integrating OpenAI, building AI-powered services, or implementing prompt-based workflows. Covers ChatClient, prompt engineering, and error handling.
---

## Spring AI Integration Patterns

### ChatClient Setup

```kotlin
@Configuration
class AiConfig {
    @Bean
    fun chatClient(chatClientBuilder: ChatClient.Builder): ChatClient {
        return chatClientBuilder.build()
    }
}
```

### Prompt Engineering

- Use structured prompts with clear instructions
- Include examples for few-shot learning
- Specify output format (JSON, plain text, etc.)
- Set temperature and max tokens appropriately

```kotlin
val response = chatClient.prompt()
    .user { u -> u.text("""
        Summarize the following news article in Japanese.
        
        Article: {article}
        
        Format: 300 characters max, include stock index impact.
    """.trimIndent())
    .param("article", articleText)
    }
    .call()
    .content()
```

### Dynamic Prompt Building

- Build prompts based on user settings
- Use template engines for complex prompts
- Validate prompt length before API call

### Error Handling

- Catch `OpenAiApiException` for API errors
- Implement retry logic with exponential backoff
- Log failed prompts for debugging
- Provide fallback responses when API fails

### Cost Optimization

- Cache responses when appropriate
- Use streaming for long responses
- Monitor token usage
- Consider using cheaper models for simple tasks

### Testing

- Mock `ChatClient` in unit tests
- Use test fixtures for prompt responses
- Test prompt building logic separately
- Validate output parsing
