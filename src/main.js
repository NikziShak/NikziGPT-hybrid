import './style.css';
import './enhancements.css';
import { createIcons, Menu, Plus, Settings, Bot, Send, Square, ArrowLeft, RefreshCw, Search, Check, Trash2, MessageSquare, Sparkles, Copy, PanelLeftClose, Paperclip, FileText, Image, Video, Music, WandSparkles, Sun, Moon, X, Play } from 'lucide';
import { complete, listModels } from './api.js';
import { loadState, persist, newChat } from './storage.js';

const iconSet = { Menu, Plus, Settings, Bot, Send, Square, ArrowLeft, RefreshCw, Search, Check, Trash2, MessageSquare, Sparkles, Copy, PanelLeftClose, Paperclip, FileText, Image, Video, Music, WandSparkles, Sun, Moon, X, Play };
const root = document.querySelector('#app');
const state = { ...loadState(), view: 'chat', drawer: innerWidth >= 900, models: [], modelSearch: '', loadingModels: false, generating: false, error: '', aborter: null, attachments: [], skills: JSON.parse(localStorage.getItem('nikzigpt.skills') || '[]'), agents: JSON.parse(localStorage.getItem('nikzigpt.agents') || '[]') };

const activeChat = () => state.chats.find(chat => chat.id === state.activeId) || state.chats[0];
const esc = value => String(value ?? '').replace(/[&<>'"]/g, char => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', "'":'&#39;', '"':'&quot;' }[char]));
const providerLabel = () => state.settings.provider === 'openrouter' ? 'OpenRouter' : 'NVIDIA';
const modelLabel = () => state.models.find(m => m.id === state.settings.model)?.name || state.settings.model?.split('/').pop() || 'Choose model';
const compact = value => value.length > 30 ? `${value.slice(0, 29)}…` : value;

function icon(name, label = '') { return `<i data-lucide="${name}" aria-hidden="true"></i>${label ? `<span class="sr-only">${esc(label)}</span>` : ''}`; }

function renderSidebar() {
  return `<aside class="sidebar ${state.drawer ? 'open' : ''}" aria-label="Conversations">
    <div class="brand"><img class="brand-mark" src="./nikzigpt-logo.png" alt="NikziGPT" /><div><strong>NikziGPT</strong><small>Multi-provider AI</small></div>
      <button class="icon-btn close-drawer" data-action="drawer">${icon('panel-left-close', 'Close menu')}</button></div>
    <button class="new-chat" data-action="new-chat">${icon('plus')}<span>New conversation</span></button>
    <div class="history-label">Recent</div>
    <nav class="history">${state.chats.map(chat => `<button class="history-item ${chat.id === state.activeId ? 'active' : ''}" data-chat="${chat.id}">
      ${icon('message-square')}<span>${esc(compact(chat.title))}</span><span class="delete" data-delete="${chat.id}">${icon('trash-2', 'Delete')}</span>
    </button>`).join('')}</nav>
    <button class="settings-link" data-view="studio">${icon('wand-sparkles')}<span>Skills & Agents</span><small>Build</small></button>
    <button class="settings-link" data-view="settings">${icon('settings')}<span>Settings</span><small>${providerLabel()}</small></button>
  </aside>`;
}

function renderMessage(message) {
  const assistant = message.role === 'assistant';
  return `<article class="message ${assistant ? 'assistant' : 'user'}">
    ${assistant ? '<div class="avatar">N</div>' : ''}
    <div class="bubble"><div class="message-role">${assistant ? 'NikziGPT' : 'You'}</div><div class="message-text">${esc(message.content)}</div>
      ${assistant && message.content ? `<button class="copy-btn" data-copy="${esc(message.id)}">${icon('copy')} Copy</button>` : ''}
    </div>
  </article>`;
}

function renderChat() {
  const chat = activeChat();
  const empty = chat.messages.length === 0;
  const attachmentChips = state.attachments.length ? `<div class="attachments">${state.attachments.map((file, index) => `<span class="attachment-chip">${icon(file.kind === 'image' ? 'image' : file.kind === 'video' ? 'video' : file.kind === 'audio' ? 'music' : 'file-text')} ${esc(file.name)}<button type="button" data-remove-attachment="${index}">${icon('x')}</button></span>`).join('')}</div>` : '';
  return `<main class="main">
    <header class="topbar"><button class="icon-btn" data-action="drawer">${icon('menu', 'Menu')}</button>
      <button class="model-pill" data-view="models"><span class="status-dot"></span><span><small>${providerLabel()}</small>${esc(compact(modelLabel()))}</span><span class="chevron">⌄</span></button>
      <button class="icon-btn" data-action="new-chat">${icon('plus', 'New chat')}</button>
    </header>
    <section class="conversation" id="conversation">
      ${empty ? `<div class="welcome"><div class="hero-mark"><img src="./nikzigpt-logo.png" alt="" /></div><p class="eyebrow">NIKZIGPT</p><h1>What can I help you explore?</h1><p>Ask a question, draft something, analyze an idea, or write code with your choice of free AI models.</p>
        <div class="suggestions">${['Explain a complex idea simply','Help me write better code','Brainstorm a creative project','Summarize a topic for me'].map(x => `<button data-prompt="${esc(x)}">${esc(x)} <span>↗</span></button>`).join('')}</div></div>` : `<div class="messages">${chat.messages.map(renderMessage).join('')}${state.generating && chat.messages.at(-1)?.content === '' ? '<div class="thinking"><span></span><span></span><span></span></div>' : ''}</div>`}
    </section>
    <div class="composer-wrap">${state.error ? `<div class="error"><span>${esc(state.error)}</span><button data-action="clear-error">×</button></div>` : ''}
      <form class="composer" id="composer">${attachmentChips}<textarea id="prompt" rows="1" placeholder="Message NikziGPT" aria-label="Message NikziGPT" ${state.generating ? 'disabled' : ''}></textarea>
        <div class="composer-bottom"><span class="composer-tools"><button type="button" class="tool-btn" data-action="attach" title="Attach files, images, video, or audio">${icon('paperclip')}<span>Attach</span></button><button type="button" class="tool-btn" data-action="url" title="Add a URL">↗<span>URL</span></button></span><span class="model-mini">${icon('sparkles')} ${esc(compact(modelLabel()))}</span>
          <button class="send-btn" type="${state.generating ? 'button' : 'submit'}" data-action="${state.generating ? 'stop' : 'send'}">${icon(state.generating ? 'square' : 'send', state.generating ? 'Stop' : 'Send')}</button></div>
      </form><p class="disclaimer">AI can make mistakes. Verify important information.</p></div>
  </main>`;
}

function renderModels() {
  const query = state.modelSearch.toLowerCase();
  const filtered = state.models.filter(m => !query || `${m.name} ${m.id} ${m.provider}`.toLowerCase().includes(query));
  return `<main class="page"><header class="page-header"><button class="icon-btn" data-view="chat">${icon('arrow-left','Back')}</button><div><h2>Choose a model</h2><p>${providerLabel()} · ${filtered.length} available</p></div><button class="icon-btn ${state.loadingModels ? 'spin' : ''}" data-action="refresh-models">${icon('refresh-cw','Refresh')}</button></header>
    <div class="page-content narrow"><label class="search">${icon('search')}<input id="model-search" value="${esc(state.modelSearch)}" placeholder="Search models" /></label>
      ${state.settings.provider === 'nvidia' ? '<div class="notice">Only NVIDIA models explicitly reported as zero-priced are shown. If the catalog does not expose pricing, no models are listed.</div>' : '<div class="notice green">Only zero-priced OpenRouter models are shown, including the automatic Free Models Router.</div>'}
      <div class="model-list">${state.loadingModels ? '<div class="loader"></div>' : filtered.map(model => `<button class="model-card ${model.id === state.settings.model ? 'selected' : ''}" data-model="${esc(model.id)}">
        <div class="model-icon">${model.provider.toLowerCase().includes('nvidia') ? 'NV' : model.provider.slice(0,2).toUpperCase()}</div><div><strong>${esc(model.name)}</strong><small>${esc(model.provider)}${model.contextLength ? ` · ${(model.contextLength/1000).toFixed(0)}K context` : ''}</small><p>${esc(model.description || model.id)}</p></div>${model.id === state.settings.model ? icon('check') : ''}</button>`).join('') || '<div class="empty-list">No models match your search.</div>'}</div>
    </div></main>`;
}

function renderSettings() {
  const s = state.settings;
  return `<main class="page"><header class="page-header"><button class="icon-btn" data-view="chat">${icon('arrow-left','Back')}</button><div><h2>Settings</h2><p>Providers and generation</p></div><span></span></header>
    <form class="page-content settings-form" id="settings-form">
      <section><div class="section-title"><h3>AI provider</h3><p>Choose where prompts are processed.</p></div><div class="segmented">
        <button type="button" data-provider="openrouter" class="${s.provider === 'openrouter' ? 'active' : ''}">OpenRouter</button><button type="button" data-provider="nvidia" class="${s.provider === 'nvidia' ? 'active' : ''}">NVIDIA</button></div>
        <label><span>${s.provider === 'openrouter' ? 'OpenRouter' : 'NVIDIA'} API key</span><input name="apiKey" type="password" autocomplete="off" value="${esc(s.provider === 'openrouter' ? s.openrouterKey : s.nvidiaKey)}" placeholder="${s.provider === 'openrouter' ? 'sk-or-v1-…' : 'nvapi-…'}" /><small>Saved only on this device. Treat this device as trusted.</small></label>
        <label><span>API base URL</span><input name="baseUrl" value="${esc(s.provider === 'openrouter' ? s.openrouterBase : s.nvidiaBase)}" /></label>
      </section>
      <section><div class="section-title"><h3>Generation</h3><p>Fine-tune how responses are written.</p></div>
        <label><span>Temperature <output id="temp-output">${Number(s.temperature).toFixed(1)}</output></span><input name="temperature" id="temperature" type="range" min="0" max="2" step="0.1" value="${s.temperature}" /></label>
        <label><span>Maximum output tokens</span><input name="maxTokens" type="number" min="64" max="32768" value="${s.maxTokens}" /></label>
        <label><span>System prompt</span><textarea name="systemPrompt" rows="5">${esc(s.systemPrompt)}</textarea></label>
      </section>
      <label class="theme-row"><span>Appearance</span><button type="button" class="theme-toggle" data-action="theme">${s.theme === 'light' ? icon('sun') + ' Light mode' : icon('moon') + ' Dark mode'}</button></label>
      <button class="primary" type="submit">Save settings</button><p class="form-note">Changing provider refreshes its model catalog. OpenRouter free models may have daily rate limits.</p>
    </form></main>`;
}

function renderStudio() {
  return `<main class="page"><header class="page-header"><button class="icon-btn" data-view="chat">${icon('arrow-left','Back')}</button><div><h2>Skills & Agents</h2><p>Save repeatable workflows and invoke them whenever needed.</p></div><span></span></header>
    <div class="page-content studio"><div class="studio-hero"><div class="hero-mark"><img src="./nikzigpt-logo.png" alt="" /></div><div><h1>Make NikziGPT work your way</h1><p>Skills are reusable instructions. Agents combine instructions with a goal so routine work takes one prompt.</p></div></div>
      <div class="studio-grid"><section class="studio-card"><div class="card-heading"><span class="card-icon purple">${icon('sparkles')}</span><div><h3>Skills</h3><p>Reusable playbooks for common tasks.</p></div><button class="small-primary" data-action="new-skill">${icon('plus')} New</button></div>${state.skills.map((skill, i) => `<div class="saved-item"><div><strong>${esc(skill.name)}</strong><small>${esc(skill.instructions)}</small></div><button data-run-skill="${i}" class="run-btn">${icon('play')} Run</button></div>`).join('') || '<div class="empty-list compact-empty">No skills yet. Create one for a task you repeat.</div>'}</section>
      <section class="studio-card"><div class="card-heading"><span class="card-icon green">${icon('bot')}</span><div><h3>Agents</h3><p>Focused assistants with a job to do.</p></div><button class="small-primary" data-action="new-agent">${icon('plus')} New</button></div>${state.agents.map((agent, i) => `<div class="saved-item"><div><strong>${esc(agent.name)}</strong><small>${esc(agent.goal)}</small></div><button data-run-agent="${i}" class="run-btn">${icon('play')} Run</button></div>`).join('') || '<div class="empty-list compact-empty">No agents yet. Build one for an end-to-end workflow.</div>'}</section></div>
      <div class="notice green">Skills and Agents are stored locally on this device. Running one starts a new chat with its instructions.</div>
    </div></main>`;
}

function render() {
  document.documentElement.dataset.theme = state.settings.theme || 'dark';
  root.innerHTML = `<div class="app-shell">${renderSidebar()}${state.view === 'chat' ? renderChat() : state.view === 'models' ? renderModels() : state.view === 'settings' ? renderSettings() : renderStudio()}${state.drawer ? '<button class="scrim" data-action="drawer" aria-label="Close menu"></button>' : ''}</div>`;
  createIcons({ icons: iconSet, attrs: { 'stroke-width': 1.8 } });
  bind();
  if (state.view === 'chat') requestAnimationFrame(() => { const el = document.querySelector('#conversation'); if (el) el.scrollTop = el.scrollHeight; });
}

async function refreshModels() {
  state.loadingModels = true; state.error = ''; render();
  try {
    state.models = await listModels(state.settings);
    if (!state.models.some(m => m.id === state.settings.model)) state.settings.model = state.models[0]?.id || '';
    persist(state);
  } catch (error) { state.error = error.message; }
  finally { state.loadingModels = false; render(); }
}

function createConversation() {
  const chat = newChat(); state.chats.unshift(chat); state.activeId = chat.id; state.view = 'chat'; state.drawer = false; persist(state); render();
}

async function sendPrompt(value) {
  const files = state.attachments.splice(0);
  const attachmentNote = files.length ? `\n\n[Attachments: ${files.map(file => `${file.name} (${file.type || file.kind})`).join(', ')}]` : '';
  const text = `${value.trim()}${attachmentNote}`.trim(); if (!text || state.generating) { state.attachments.push(...files); return; }
  const chat = activeChat();
  if (chat.messages.length === 0) chat.title = text.replace(/\s+/g,' ').slice(0, 48);
  chat.messages.push({ id: crypto.randomUUID(), role: 'user', content: text }, { id: crypto.randomUUID(), role: 'assistant', content: '', model: state.settings.model });
  state.generating = true; state.error = ''; state.aborter = new AbortController(); persist(state); render();
  const target = chat.messages.at(-1);
  const context = chat.messages.slice(0, -1).map(({ role, content }) => ({ role, content }));
  try {
    await complete(state.settings, context, token => { target.content += token; persist(state); render(); }, state.aborter.signal);
    if (!target.content) target.content = 'The provider returned an empty response.';
  } catch (error) {
    if (error.name !== 'AbortError') { state.error = error.message || 'Could not complete the request.'; if (!target.content) chat.messages.pop(); }
  } finally { state.generating = false; state.aborter = null; persist(state); render(); }
}

function bind() {
  root.querySelectorAll('[data-view]').forEach(el => el.onclick = () => { state.view = el.dataset.view; state.drawer = false; render(); if (state.view === 'models' && !state.models.length) refreshModels(); });
  root.querySelectorAll('[data-action="drawer"]').forEach(el => el.onclick = () => { state.drawer = !state.drawer; render(); });
  root.querySelectorAll('[data-action="new-chat"]').forEach(el => el.onclick = createConversation);
  root.querySelector('[data-action="clear-error"]')?.addEventListener('click', () => { state.error = ''; render(); });
  root.querySelector('[data-action="stop"]')?.addEventListener('click', () => state.aborter?.abort());
  root.querySelector('[data-action="refresh-models"]')?.addEventListener('click', refreshModels);
  root.querySelector('[data-action="theme"]')?.addEventListener('click', () => { state.settings.theme = state.settings.theme === 'light' ? 'dark' : 'light'; persist(state); render(); });
  root.querySelector('[data-action="attach"]')?.addEventListener('click', () => { const input = document.createElement('input'); input.type = 'file'; input.multiple = true; input.accept = '.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.csv,.txt,image/*,video/*,audio/*'; input.onchange = () => { [...input.files].forEach(file => { const kind = file.type.startsWith('image/') ? 'image' : file.type.startsWith('video/') ? 'video' : file.type.startsWith('audio/') ? 'audio' : 'file'; state.attachments.push({ name: file.name, type: file.type, kind }); }); render(); }; input.click(); });
  root.querySelector('[data-action="url"]')?.addEventListener('click', () => { const url = window.prompt('Paste a URL to include in your prompt'); if (url?.trim()) { const prompt = root.querySelector('#prompt'); prompt.value = `${prompt.value} ${url.trim()}`.trim(); prompt.focus(); } });
  root.querySelectorAll('[data-remove-attachment]').forEach(el => el.onclick = () => { state.attachments.splice(Number(el.dataset.removeAttachment), 1); render(); });
  root.querySelector('[data-action="new-skill"]')?.addEventListener('click', () => { const name = window.prompt('Skill name'); if (!name) return; const instructions = window.prompt('What should this skill do?') || ''; state.skills.push({ name, instructions }); localStorage.setItem('nikzigpt.skills', JSON.stringify(state.skills)); render(); });
  root.querySelector('[data-action="new-agent"]')?.addEventListener('click', () => { const name = window.prompt('Agent name'); if (!name) return; const goal = window.prompt('What repetitive job should this agent handle?') || ''; state.agents.push({ name, goal }); localStorage.setItem('nikzigpt.agents', JSON.stringify(state.agents)); render(); });
  root.querySelectorAll('[data-run-skill]').forEach(el => el.onclick = () => { const skill = state.skills[Number(el.dataset.runSkill)]; createConversation(); state.settings.systemPrompt = skill.instructions; persist(state); render(); });
  root.querySelectorAll('[data-run-agent]').forEach(el => el.onclick = () => { const agent = state.agents[Number(el.dataset.runAgent)]; createConversation(); state.settings.systemPrompt = agent.goal; persist(state); render(); });
  root.querySelectorAll('[data-chat]').forEach(el => el.onclick = event => { if (event.target.closest('[data-delete]')) return; state.activeId = el.dataset.chat; state.view = 'chat'; state.drawer = false; persist(state); render(); });
  root.querySelectorAll('[data-delete]').forEach(el => el.onclick = event => { event.stopPropagation(); state.chats = state.chats.filter(c => c.id !== el.dataset.delete); if (!state.chats.length) state.chats = [newChat()]; state.activeId = state.chats[0].id; persist(state); render(); });
  root.querySelectorAll('[data-prompt]').forEach(el => el.onclick = () => sendPrompt(el.dataset.prompt));
  root.querySelectorAll('[data-copy]').forEach(el => el.onclick = async () => { const msg = activeChat().messages.find(m => m.id === el.dataset.copy); if (msg) await navigator.clipboard.writeText(msg.content); });
  root.querySelectorAll('[data-model]').forEach(el => el.onclick = () => { state.settings.model = el.dataset.model; state.view = 'chat'; persist(state); render(); });
  const search = root.querySelector('#model-search'); if (search) search.oninput = () => { state.modelSearch = search.value; render(); root.querySelector('#model-search')?.focus(); };
  root.querySelectorAll('[data-provider]').forEach(el => el.onclick = () => { state.settings.provider = el.dataset.provider; state.settings.model = el.dataset.provider === 'openrouter' ? 'openrouter/free' : ''; state.models = []; render(); });
  const temp = root.querySelector('#temperature'); if (temp) temp.oninput = () => { root.querySelector('#temp-output').value = Number(temp.value).toFixed(1); };
  const form = root.querySelector('#settings-form'); if (form) form.onsubmit = event => { event.preventDefault(); const data = new FormData(form); if (state.settings.provider === 'openrouter') { state.settings.openrouterKey = data.get('apiKey').trim(); state.settings.openrouterBase = data.get('baseUrl').trim(); } else { state.settings.nvidiaKey = data.get('apiKey').trim(); state.settings.nvidiaBase = data.get('baseUrl').trim(); } state.settings.temperature = Number(data.get('temperature')); state.settings.maxTokens = Number(data.get('maxTokens')); state.settings.systemPrompt = data.get('systemPrompt').trim(); state.models = []; state.view = 'models'; persist(state); render(); refreshModels(); };
  const composer = root.querySelector('#composer'); if (composer) composer.onsubmit = event => { event.preventDefault(); sendPrompt(root.querySelector('#prompt').value); };
  const prompt = root.querySelector('#prompt'); if (prompt) { prompt.oninput = () => { prompt.style.height = 'auto'; prompt.style.height = `${Math.min(prompt.scrollHeight, 160)}px`; }; prompt.onkeydown = event => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); sendPrompt(prompt.value); } }; }
}

render();
refreshModels();
