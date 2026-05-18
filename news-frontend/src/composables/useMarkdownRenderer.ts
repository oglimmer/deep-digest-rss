const escapeHtml = (s: string): string =>
  s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

export const renderMarkdown = (content: string): string =>
  escapeHtml(content)
    .replace(/^# (.+)$/gm, '<h2>$1</h2>')
    .replace(/^## (.+)$/gm, '<h3>$1</h3>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n{2,}/g, '</p><p>')
    .replace(/\n/g, '<br>')
    .replace(/^/, '<p>')
    .replace(/$/, '</p>')

export const useMarkdownRenderer = () => ({
  render: renderMarkdown,
})
