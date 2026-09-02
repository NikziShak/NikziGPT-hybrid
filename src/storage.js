const KEYS = {
  settings: 'nikzigpt.settings.v1',
  chats: 'nikzigpt.chats.v1',
  active: 'nikzigpt.active.v1'
};

export const defaults = {
  provider: 'openrouter',
  openrouterKey: '',
  nvidiaKey: '',
  openrouterBase: 'https://openrouter.ai/api/v1',
  nvidiaBase: 'https://integrate.api.nvidia.com/v1',
  model: 'openrouter/free',
  temperature: 0.7,
  maxTokens: 2048,
  systemPrompt: 'You are NikziGPT, a clear and helpful AI assistant.',
  theme: 'dark'
};

const parse = (key, fallback) => {
  try { return JSON.parse(localStorage.getItem(key)) ?? fallback; }
  catch { return fallback; }
};

export function loadState() {
  const settings = { ...defaults, ...parse(KEYS.settings, {}) };
  let chats = parse(KEYS.chats, []);
  if (!Array.isArray(chats) || chats.length === 0) chats = [newChat()];
  const activeId = localStorage.getItem(KEYS.active) || chats[0].id;
  return { settings, chats, activeId };
}

export function persist(state) {
  localStorage.setItem(KEYS.settings, JSON.stringify(state.settings));
  localStorage.setItem(KEYS.chats, JSON.stringify(state.chats.slice(0, 50)));
  localStorage.setItem(KEYS.active, state.activeId);
}

export function newChat() {
  return { id: crypto.randomUUID(), title: 'New conversation', createdAt: Date.now(), messages: [] };
}
