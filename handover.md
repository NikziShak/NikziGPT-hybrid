# NikziGPT Handover

_Updated: 2026-09-05_

## Current implementation

- Hybrid Vite + Capacitor + Electron application for Android, iOS, Windows, macOS, and Linux.
- Light-only Material-inspired interface with responsive chat layout.
- Providers: OpenRouter, NVIDIA, Hugging Face, and Google AI Studio/Gemini.
- Provider model catalogs are loaded with the configured API key; no free-model restriction is applied.
- Model search filters the indexed catalog in place without rerendering the input, preventing cursor reversal and typing lag.
- Attachments show upload progress and type icons. Images are sent as multimodal image parts; Gemini receives supported files as inline data. Text/CSV/JSON/Markdown and basic PDF text are included as hidden model context.
- Web search context is fetched from DuckDuckGo for prompts requesting current/search/web/news information.
- Quota/rate-limit failures automatically select the next catalog model and regenerate this handover with the conversation context.

## Known limitations

- Gemini and other providers may enforce their own MIME type, size, context, and billing limits.
- Office files (DOC/DOCX/XLS/XLSX) and media are forwarded when supported by the selected provider; conversion to downloadable DOCX/XLSX/PPTX/MP3/MP4 output is not currently implemented.
- Browser builds download `handover.md`; desktop builds save it under `Documents/NikziGPT/handover.md`.

## Resume instructions

Continue from the current repository state. Preserve existing provider settings and conversation intent, avoid reintroducing Skills/Agents, and verify Android and desktop workflows after changes.
