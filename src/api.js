const cleanBase = value => value.replace(/\/+$/, '');
const authHeaders = (key, provider) => ({
  'Authorization': `Bearer ${key}`,
  'Content-Type': 'application/json',
  ...(provider === 'openrouter' ? { 'HTTP-Referer': 'https://nikzigpt.local', 'X-Title': 'NikziGPT' } : {})
});

async function errorText(response) {
  const raw = await response.text();
  try { return JSON.parse(raw)?.error?.message || raw; } catch { return raw; }
}

export async function listModels(settings) {
  if (settings.provider === 'openrouter') {
    const headers = settings.openrouterKey ? { Authorization: `Bearer ${settings.openrouterKey}` } : {};
    const response = await fetch(`${cleanBase(settings.openrouterBase)}/models`, { headers });
    if (!response.ok) throw new Error(await errorText(response) || `Model request failed (${response.status})`);
    const json = await response.json();
    return (json.data || []).map(item => ({
      id: item.id,
      name: item.name || item.id,
      provider: item.id?.split('/')[0] || 'OpenRouter',
      description: item.description || '',
      contextLength: item.context_length || 0,
      free: item.id?.endsWith(':free') || (Number(item.pricing?.prompt) === 0 && Number(item.pricing?.completion) === 0)
    })).sort((a, b) => a.name.localeCompare(b.name));
  }

  if (!settings.nvidiaKey) return [];
  try {
    const response = await fetch(`${cleanBase(settings.nvidiaBase)}/models`, { headers: { Authorization: `Bearer ${settings.nvidiaKey}` } });
    if (!response.ok) throw new Error();
    const json = await response.json();
    return (json.data || []).map(item => ({ id: item.id, name: item.name || item.id, provider: 'NVIDIA API Catalog', description: item.description || 'Available through NVIDIA API Catalog; account limits apply.', contextLength: item.context_length || 0, free: item.id?.endsWith(':free') || (Number(item.pricing?.prompt) === 0 && Number(item.pricing?.completion) === 0) }));
  } catch {
    return [];
  }
}

export async function complete(settings, messages, onToken, signal) {
  const key = settings.provider === 'openrouter' ? settings.openrouterKey : settings.nvidiaKey;
  if (!key) throw new Error(`Add your ${settings.provider === 'openrouter' ? 'OpenRouter' : 'NVIDIA'} API key in Settings.`);
  const base = settings.provider === 'openrouter' ? settings.openrouterBase : settings.nvidiaBase;
  const model = settings.model;
  const payloadMessages = settings.systemPrompt.trim()
    ? [{ role: 'system', content: settings.systemPrompt.trim() }, ...messages]
    : messages;
  const body = {
    model,
    messages: payloadMessages.map(({ role, content }) => ({ role, content })),
    temperature: Number(settings.temperature),
    max_tokens: Number(settings.maxTokens),
    stream: true
  };
  const response = await fetch(`${cleanBase(base)}/chat/completions`, {
    method: 'POST', headers: authHeaders(key, settings.provider), body: JSON.stringify(body), signal
  });
  if (!response.ok) throw new Error(await errorText(response) || `Request failed (${response.status})`);

  if (!response.body?.getReader) {
    const json = await response.json();
    onToken(json.choices?.[0]?.message?.content || '');
    return;
  }
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() || '';
    for (const line of lines) {
      const data = line.trim().replace(/^data:\s*/, '');
      if (!data || data === '[DONE]') continue;
      try {
        const chunk = JSON.parse(data);
        const token = chunk.choices?.[0]?.delta?.content;
        if (token) onToken(token);
      } catch { /* wait for the next complete SSE frame */ }
    }
  }
}
