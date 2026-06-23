export interface PaisTelefone {
  code: string;
  dial: string;
  flag: string;
  name: string;
}

export const PAISES_TELEFONE: PaisTelefone[] = [
  { code: 'BR', dial: '55', flag: '🇧🇷', name: 'Brasil' },
  { code: 'PT', dial: '351', flag: '🇵🇹', name: 'Portugal' },
  { code: 'US', dial: '1', flag: '🇺🇸', name: 'Estados Unidos' },
  { code: 'AR', dial: '54', flag: '🇦🇷', name: 'Argentina' },
];

export const PAIS_PADRAO = PAISES_TELEFONE[0];

export function somenteDigitosTelefone(v: string): string {
  return v.replace(/\D/g, '');
}

export function telefoneMascarado(v: string): boolean {
  return v.includes('*');
}

/** Formata DDD + número nacional: 21 96532 4465 */
export function formatarParteNacional(nacional: string): string {
  const d = nacional.replace(/\D/g, '').slice(0, 11);
  if (d.length === 0) return '';
  if (d.length <= 2) return d;

  const ddd = d.slice(0, 2);
  const resto = d.slice(2);
  if (resto.length === 0) return ddd;

  if (resto.length > 8 || resto[0] === '9') {
    const p1 = resto.slice(0, 5);
    const p2 = resto.slice(5, 9);
    return p2 ? `${ddd} ${p1} ${p2}` : `${ddd} ${p1}`;
  }

  const p1 = resto.slice(0, 4);
  const p2 = resto.slice(4, 8);
  return p2 ? `${ddd} ${p1} ${p2}` : `${ddd} ${p1}`;
}

function detectarPaisPorDigitos(digits: string): PaisTelefone | null {
  const ordenados = [...PAISES_TELEFONE].sort((a, b) => b.dial.length - a.dial.length);
  for (const pais of ordenados) {
    if (digits.startsWith(pais.dial) && digits.length > pais.dial.length + 8) {
      return pais;
    }
  }
  return null;
}

/** Exibe no padrão internacional: +55 21 96532 4465 */
export function formatarTelefoneExibicao(valor: string, dialPadrao = PAIS_PADRAO.dial): string {
  if (!valor || telefoneMascarado(valor)) return valor;

  let digits = somenteDigitosTelefone(valor);
  if (digits.length === 0) return '';

  const pais = detectarPaisPorDigitos(digits);
  const dial = pais?.dial ?? dialPadrao;
  const nacional = pais ? digits.slice(pais.dial.length) : digits;

  const parte = formatarParteNacional(nacional);
  return parte ? `+${dial} ${parte}` : `+${dial} `;
}

/** Converte para envio à API (apenas dígitos nacionais, sem código do país). */
export function telefoneParaApi(valor: string): string {
  if (!valor || telefoneMascarado(valor)) {
    return somenteDigitosTelefone(valor);
  }

  const digits = somenteDigitosTelefone(valor);
  const pais = detectarPaisPorDigitos(digits);
  if (pais) {
    return digits.slice(pais.dial.length);
  }
  return digits;
}

export function telefoneValido(valor: string): boolean {
  if (!valor || telefoneMascarado(valor)) return true;
  const api = telefoneParaApi(valor);
  return api.length >= 10 && api.length <= 11;
}

export function detectarPais(valor: string): PaisTelefone {
  const digits = somenteDigitosTelefone(valor);
  return detectarPaisPorDigitos(digits) ?? PAIS_PADRAO;
}
