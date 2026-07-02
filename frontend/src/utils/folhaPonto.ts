const FUSO_BR = 'America/Sao_Paulo';

function extrairHoraMinuto(iso: string): string | null {
  const match = iso.match(/T(\d{2}):(\d{2})/);
  if (!match) return null;
  return `${match[1]}:${match[2]}`;
}

export const FolhaPontoUtil = {
  formatarHora: (iso?: string) => {
    if (!iso) return '—';
    const direto = extrairHoraMinuto(iso);
    if (direto) return direto;
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return '—';
    return d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', timeZone: FUSO_BR });
  },

  formatarDataHora: (iso?: string) => {
    if (!iso) return '—';
    const hora = extrairHoraMinuto(iso);
    const data = FolhaPontoUtil.formatarData(iso.split('T')[0]);
    if (hora && data !== '—') return `${data}, ${hora}`;
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return '—';
    return d.toLocaleString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      timeZone: FUSO_BR,
    });
  },

  formatarData: (iso?: string) => {
    if (!iso) return '—';
    const [y, m, d] = iso.split('T')[0].split('-');
    if (y && m && d) return `${d}/${m}/${y}`;
    const dt = new Date(iso.includes('T') ? iso : `${iso}T12:00:00`);
    if (Number.isNaN(dt.getTime())) return iso;
    return dt.toLocaleDateString('pt-BR', { timeZone: FUSO_BR });
  },
};
