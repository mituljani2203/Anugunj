# Anugunj (अनुगुंज)

**A voice-first reflection companion.** Speak naturally. Explore what you mean. Stay in control of your own interpretation.

## Current status

- **Public browser prototype:** https://mituljani2203.github.io/Anugunj/
- **Source repository:** https://github.com/mituljani2203/Anugunj
- **Hosted demo:** GitHub Pages, static HTML/CSS/JavaScript.
- **Backend source:** Java 17 / Spring Boot 3.5 application in `src/`, with an optional OpenAI-compatible chat-completions provider configured only through server environment variables.
- **Important limitation:** the hosted GitHub Pages demo uses local sample reflection rules. It does not call the Spring Boot backend, so real AI responses are not active on the public demo.
- **Verification:** the current backend suite contains 8 tests covering structured responses, ordinary statements, uncertainty, blank input, API response/reset, validation errors, session rate limiting, and the health endpoint. The latest recorded CI run passed all 8 tests; its Docker image step failed because Docker Hub returned HTTP 429, a registry rate-limit response. Container image builds are not part of the Pages deployment gate while this external registry issue is unresolved.

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

Speech-recognition support depends on the browser and device. Browser speech recognition may be processed by the browser/vendor according to its own implementation and terms. The prototype is not a substitute for professional care.

## Backend capabilities implemented in source

- Optional OpenAI-compatible chat-completions integration.
- Server-side configuration through `ANUGUNJ_AI_API_KEY`, `ANUGUNJ_AI_BASE_URL`, and `ANUGUNJ_AI_MODEL`; never expose the provider key to the browser.
- Local fallback if the key is absent or the provider call fails.
- Bounded provider connection/read timeouts and bounded response fields.
- Session-scoped conversation context, capped to the most recent 8 messages.
- New-conversation reset endpoint.
- Basic limit of 20 messages per session per 10 minutes.
- Structured validation/error responses and a health endpoint that does not expose secrets.
- HTTP session timeout of 30 minutes; no database persistence of conversation history.

These are source-code capabilities, not a claim that the backend is hosted or that a live provider key has been tested.

## Technology

- Java 17
- Spring Boot / Spring MVC / Thymeleaf
- HTML, CSS, vanilla JavaScript
- Maven
- JUnit 5 / Spring Boot Test
- Browser-native speech APIs for the public prototype

## Run the backend locally

Prerequisites: JDK 17+ and Maven. From the repository root:

```bash
mvn spring-boot:run
```

Then open http://localhost:8080. To run automated tests:

```bash
mvn test
```

These commands are documentation only; the hosted GitHub Pages site does not run the Spring Boot backend.

## Container deployment

A multi-stage `Dockerfile` is included and configures a non-root runtime user. Docker image verification is currently separated from the Pages deployment workflow because GitHub Actions repeatedly received HTTP 429 from Docker Hub while fetching the base image metadata. This is a registry-rate-limit issue; the container build has not yet been verified successfully in CI. The repository does not publish a container image or deploy the backend automatically.

## MVP roadmap

1. [x] Publish a free static browser prototype.
2. [x] Hide the reflection until the user submits a thought.
3. [x] Add browser language choices for English (India), Hindi, and Gujarati.
4. [x] Add explicit feedback controls for tentative interpretations.
5. [x] Implement an optional server-side AI provider adapter with local fallback.
6. [x] Add bounded session context and a reset endpoint to the backend.
7. [x] Add provider timeouts, safe error handling, and basic rate limiting.
8. [x] Add automated backend tests; 8 tests passed in the latest recorded run.
9. [ ] Deploy the backend on a suitable low-cost/free host, ideally without a credit card.
10. [ ] Configure a provider key securely and verify live AI responses.
11. [ ] Verify the complete microphone → editable transcript → backend → structured reflection → read-aloud journey on mobile.
12. [ ] Verify Docker image build and production deployment independently of the Docker Hub rate-limit issue.

## Privacy and data handling

The static prototype keeps typed text in the current page only and does not persist conversation history. Browser speech recognition may be processed by the browser/vendor service according to its implementation and terms; Anugunj itself does not upload or store recognized audio. The separate Spring Boot backend sends message text to the configured AI provider only when a server-side key is configured. Backend session context is held in the HTTP session for up to 30 minutes and cleared by the reset endpoint; it is not persisted to a database. The 20-message-per-10-minute session limit is a baseline safeguard, not a substitute for edge-level abuse protection. Before enabling a hosted provider, disclose what message text is sent to it and review the provider's current retention and data-use terms.

## Specification

Keep implementation and this README aligned with the product specification. Never mark a requirement complete without evidence.
