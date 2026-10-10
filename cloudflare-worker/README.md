# Anugunj Cloudflare Worker API

This Worker is a lightweight JavaScript/TypeScript-hosted alternative to the Spring Boot API for Cloudflare's free tier. It keeps the existing Java backend unchanged.

## Deploy from the Cloudflare dashboard

1. Open **Workers & Pages** in the Cloudflare dashboard and create a Worker named `anugunj-api`.
2. Paste the contents of `src/index.js` into the Worker's editor and deploy once.
3. Under **Settings → Variables and Secrets**, add a secret named `ANUGUNJ_AI_API_KEY` with your provider key. Never put the key in GitHub or client-side JavaScript. Optional variables: `ANUGUNJ_AI_BASE_URL` (defaults to `https://api.openai.com/v1`) and `ANUGUNJ_AI_MODEL` (defaults to `gpt-4o-mini`).
4. For session context, create a Workers KV namespace named `ANUGUNJ_SESSIONS`, then add it under **Settings → Bindings → KV Namespace** with variable name `ANUGUNJ_SESSIONS`. If this binding is absent, the Worker still responds, but multi-turn history is not persisted between requests.
5. Test `https://YOUR-WORKER.YOUR-SUBDOMAIN.workers.dev/health`. It should return `status: UP`; `aiConfigured` is true only after the provider secret is set.

## Endpoints

- `GET /health`
- `POST /api/conversation/message` with JSON `{"message":"..." }`
- `POST /api/conversation/reset`

The Worker is an API adapter only. The GitHub Pages prototype currently uses local sample responses and has not yet been wired to this Worker. Browser microphone support and speech playback depend on the browser/device.

## Privacy and limits

- The Worker does not store raw audio.
- When configured, the user's message text is sent to the configured AI provider.
- KV session history expires after 30 minutes.
- Provider keys must be set as Worker secrets, not committed to the repository.
- This code has been committed to the repository, but a live Cloudflare deployment and end-to-end browser test have **not** been verified from this workspace.
