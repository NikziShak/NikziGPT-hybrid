const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('nikziDesktop', {
  saveHandover: markdown => ipcRenderer.invoke('save-handover', markdown)
});
