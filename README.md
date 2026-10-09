# Anugunj (अनुगुंज)

**A voice-first reflection companion.** Speak naturally. Explore what you mean. Stay in control of your own interpretation.

## Current status

- **Public browser prototype:** https://mituljani2203.github.io/Anugunj/
- **Source repository:** https://github.com/mituljani2203/Anugunj
- **Hosted demo:** GitHub Pages, static HTML/CSS/JavaScript.
- **Important limitation:** the hosted page currently uses local sample reflection rules, not a real LLM. It does not send typed messages to a server or persist conversations.
- **Backend source:** a Java 17 / Spring Boot 3.5 modular-monolith starter is in `src/`. It supports an optional OpenAI-compatible chat-completions provider configured only through server environment variables; without a key or when the provider fails, it falls back to local sample reflections.

## Product principles

- Curious, not certain; never claim to read minds.
- Reflect explicit words and offer interpretations as possibilities, not facts.
- Respect the user's correction.
- Ask at most one useful follow-up question by default.
- No diagnosis, lie detection, permanent emotional profile, or raw audio storage by default.
- Keep secrets on the server; never commit provider keys.

## Prototype features

- Responsive dark interface.
- Editable text input.
- Browser-native speech recognition where supported, with English (India), Hindi, and Gujarati language choices.
- Browser-native speech synthesis where supported.
- Local example reflections, explicit feedback controls, and new-conversation reset.
- No accounts, database, analytics, or permanent conversation history.

Speech-recognition support depends on the browser and device. The prototype is not a substitute for professional care.

## Technology direction

- Java 17
- Spring Boot / Spring MVC / Thymeleaf
- HTML, CSS, vanilla JavaScript
- Maven
- JUnit 5 / Spring Boot Test
- Browser-native speech APIs for the first prototype, with a future path to a hosted provider if needed.

## Run the backend locally

Prerequisites: JDK 17+ and Maven. From the repository root, run:

```bash
mvn spring-boot:run
```

Then open http://localhost:8080. To run automated tests:

```bash
mvn test
```

These commands are documentation only; the hosted GitHub Pages site does not run the Spring Boot backend.

## Roadmap toward MVP v0.1

1. [x] Publish a free static browser prototype.
2. [x] Keep the first-run reflection hidden until the user submits a thought.
3. [x] Add browser language selection for English (India), Hindi, and Gujarati.
4. [x] Add feedback that explicitly accepts/rejects a tentative interpretation.
5. [ ] Integrate a real, configurable AI provider on the Spring Boot server.
6. [ ] Add bounded session context to the backend and reset it for new conversations.
7. [ ] Add provider timeouts, safe error handling, and basic rate limiting before public backend deployment.
8. [ ] Run and fix automated tests; verify the Spring Boot end-to-end flow.
9. [ ] Deploy the backend on a genuinely free host that does not require a credit card, if a suitable provider is available.
10. [ ] Verify the complete microphone → editable transcript → backend → structured reflection → read-aloud journey on mobile.

## Privacy

The static prototype keeps the text entered for reflection in the current page only. Browser speech recognition may be processed by the browser/vendor service according to its implementation and terms; Anugunj itself does not upload or store the recognized transcript. The Spring Boot backend is separate from the GitHub Pages site; its optional AI provider receives message text only when configured with a server-side key. Session context is held in the server's HTTP session for up to 30 minutes and cleared by the reset endpoint; it is not persisted to a database. The backend applies a basic limit of 20 messages per session per 10 minutes. This is a baseline safeguard, not a substitute for edge-level abuse protection. Before enabling a hosted AI provider, disclose what message text is sent to it and review its current retention/data-use terms.

## Specification

The product requirements and architecture source of truth is `Anugunj_In_Depth_Master_Specification.md`. Keep implementation and this README aligned with that specification. Never mark a requirement complete without evidence.
