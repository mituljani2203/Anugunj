const SYSTEM_PROMPT = "You are Anugunj, a gentle voice-first reflection companion. Reply in the same language as the user, including Gujarati or Hindi when appropriate. Be calm, concise, context-aware and curious, not certain. Never claim to read minds or diagnose. Do not force emotional analysis for ordinary statements. Offer interpretations only as possibilities, respect user corrections, and ask at most one useful follow-up. Return only a JSON object with string keys response, interpretation, followUp.";

const corsHeaders = {
  "Access-Control-Allow-Origin": "https://mituljani2203.github.io",
  "Access-Control-Allow-Credentials": "true",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
  "Vary": "Origin",
  "Cache-Control": "no-store"
};

function json(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json; charset=utf-8" }
  });
}

function getCookie(request, name) {
  const raw = request.headers.get("Cookie") || "";
  const entry = raw.split(";").map(x => x.trim()).find(x => x.startsWith(name + "="));
  return entry ? entry.slice(name.length + 1) : "";
}

function newSessionId() {
  return crypto.randomUUID();
}

function sessionCookie(id) {
  return `anugunj_sid=${id}; Path=/; HttpOnly; Secure; SameSite=None; Max-Age=1800`;
}

async function getSession(request, env) {
  let id = getCookie(request, "anugunj_sid");
  let isNew = !/^[0-9a-f-]{36}$/i.test(id);
  if (isNew) id = newSessionId();
  let history = [];
  if (!isNew && env.ANUGUNJ_SESSIONS) {
    try {
      history = JSON.parse(await env.ANUGUNJ_SESSIONS.get(id) || "[]");
      if (!Array.isArray(history)) history = [];
    } catch { history = []; }
  }
  return { id, isNew, history: history.slice(-8) };
}

async function saveSession(session, history, env) {
  if (env.ANUGUNJ_SESSIONS) {
    await env.ANUGUNJ_SESSIONS.put(session.id, JSON.stringify(history.slice(-8)), { expirationTtl: 1800 });
  }
}

async function respondWithAI(message, history, env) {
  const apiKey = env.ANUGUNJ_AI_API_KEY;
  if (!apiKey) {
    return {
      response: "Thanks for putting that into words. We can stay with it without rushing to a conclusion.",
      interpretation: "I may not have the full context yet, so I won't read more into it than you've shared.",
      followUp: "What part of this feels most important to you right now?"
    };
  }
  const base = (env.ANUGUNJ_AI_BASE_URL || "https://api.openai.com/v1").replace(/\/+$/, "");
  const messages = [{ role: "system", content: SYSTEM_PROMPT }];
  for (const item of history.slice(-8)) {
    if (item && ["user", "assistant"].includes(item.role) && typeof item.content === "string") {
      messages.push({ role: item.role, content: item.content.slice(0, 1500) });
    }
  }
  messages.push({ role: "user", content: message });
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 20000);
  try {
    const response = await fetch(base + "/chat/completions", {
      method: "POST",
      headers: { "Authorization": "Bearer " + apiKey, "Content-Type": "application/json" },
      body: JSON.stringify({
        model: env.ANUGUNJ_AI_MODEL || "gpt-4o-mini",
        temperature: 0.5,
        response_format: { type: "json_object" },
        messages
      }),
      signal: controller.signal
    });
    if (!response.ok) throw new Error("AI provider status " + response.status);
    const payload = await response.json();
    const content = payload?.choices?.[0]?.message?.content;
    if (typeof content !== "string" || !content.trim()) throw new Error("Empty provider response");
    const parsed = JSON.parse(content);
    const fields = ["response", "interpretation", "followUp"];
    if (!fields.every(k => typeof parsed[k] === "string" && parsed[k].trim())) {
      throw new Error("Incomplete provider response");
    }
    return Object.fromEntries(fields.map(k => [k, parsed[k].trim().slice(0, 2000)]));
  } finally {
    clearTimeout(timeout);
  }
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: corsHeaders });
    const url = new URL(request.url);
    if (request.method === "GET" && url.pathname === "/health") {
      return json({ status: "UP", aiConfigured: Boolean(env.ANUGUNJ_AI_API_KEY) });
    }
    if (request.method === "POST" && url.pathname === "/api/conversation/reset") {
      const session = await getSession(request, env);
      if (env.ANUGUNJ_SESSIONS) await env.ANUGUNJ_SESSIONS.delete(session.id);
      const response = json({ status: "RESET" });
      response.headers.append("Set-Cookie", sessionCookie(session.id));
      return response;
    }
    if (request.method === "POST" && url.pathname === "/api/conversation/message") {
      let body;
      try { body = await request.json(); } catch { return json({ error: { code: "BAD_REQUEST", message: "Request body must be valid JSON." } }, 400); }
      const message = typeof body?.message === "string" ? body.message.trim() : "";
      if (!message) return json({ error: { code: "VALIDATION_ERROR", message: "Please enter a message." } }, 400);
      if (message.length > 4000) return json({ error: { code: "VALIDATION_ERROR", message: "Please keep your message under 4000 characters." } }, 400);
      const session = await getSession(request, env);
      try {
        const result = await respondWithAI(message, session.history, env);
        const history = [...session.history,
          { role: "user", content: message },
          { role: "assistant", content: result.response + "\n" + result.interpretation + "\n" + result.followUp }
        ].slice(-8);
        await saveSession(session, history, env);
        const response = json(result);
        response.headers.append("Set-Cookie", sessionCookie(session.id));
        return response;
      } catch (error) {
        const correlationId = crypto.randomUUID();
        console.error("Anugunj provider failure", correlationId, error?.name || "Error");
        return json({ error: { code: "AI_UNAVAILABLE", message: "Anugunj couldn't reach the AI service just now. Please try again.", correlationId } }, 502);
      }
    }
    return json({ error: { code: "NOT_FOUND", message: "Not found." } }, 404);
  }
};
