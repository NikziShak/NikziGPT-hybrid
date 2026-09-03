const { app, BrowserWindow, shell, ipcMain } = require('electron');
const fs = require('node:fs/promises');
const path = require('node:path');

const createWindow = () => {
  const window = new BrowserWindow({
    width: 1240,
    height: 820,
    minWidth: 760,
    minHeight: 560,
    backgroundColor: '#f8f9fd',
    title: 'NikziGPT',
    webPreferences: {
      contextIsolation: true,
      sandbox: true,
      preload: path.join(__dirname, 'preload.cjs'),
      nodeIntegration: false
    }
  });
  window.loadFile(path.join(__dirname, '..', 'dist', 'index.html'));
  window.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });
};

ipcMain.handle('save-handover', async (_event, markdown) => {
  const file = path.join(app.getPath('documents'), 'NikziGPT', 'handover.md');
  await fs.mkdir(path.dirname(file), { recursive: true });
  await fs.writeFile(file, String(markdown || ''), 'utf8');
  return file;
});

app.whenReady().then(() => {
  createWindow();
  app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit(); });
