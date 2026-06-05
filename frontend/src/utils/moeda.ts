/** Formata número para exibição em Real (R$ 1.234,56). */
export function formatarMoeda(valor?: number | string | null): string {
  const num = typeof valor === 'string' ? parseMoeda(valor) : Number(valor ?? 0);
  if (Number.isNaN(num)) return 'R$ 0,00';
  return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

/** Extrai valor numérico de texto com ou sem máscara R$. */
export function parseMoeda(texto: string): number {
  const limpo = texto.replace(/[^\d,.-]/g, '').replace(/\./g, '').replace(',', '.');
  const num = parseFloat(limpo);
  return Number.isNaN(num) ? 0 : num;
}

/** Aplica máscara monetária enquanto o usuário digita. */
export function mascaraMoedaInput(texto: string): string {
  const digitos = texto.replace(/\D/g, '');
  if (!digitos) return '';
  const centavos = parseInt(digitos, 10);
  return (centavos / 100).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
