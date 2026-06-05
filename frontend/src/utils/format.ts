/** Converte data ISO (yyyy-MM-dd) ou Date para dd/MM/yyyy. */
export function formatDateBr(valor?: string | Date | null): string {
  if (!valor) return '';
  if (valor instanceof Date) {
    const d = valor;
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
  }
  const texto = String(valor).trim();
  const iso = texto.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (iso) {
    return `${iso[3]}/${iso[2]}/${iso[1]}`;
  }
  return texto;
}

function pad(n: number): string {
  return n < 10 ? `0${n}` : String(n);
}
