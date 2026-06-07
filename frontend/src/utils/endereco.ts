export interface EnderecoCompleto {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
}

export const enderecoVazio = (): EnderecoCompleto => ({
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  uf: '',
});

export function parseEndereco(raw?: string | null): EnderecoCompleto {
  if (!raw?.trim()) return enderecoVazio();
  try {
    const parsed = JSON.parse(raw) as Partial<EnderecoCompleto>;
    if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
      return { ...enderecoVazio(), ...parsed };
    }
  } catch {
    /* legado: texto único */
  }
  return { ...enderecoVazio(), logradouro: raw.trim() };
}

export function serializarEndereco(e: EnderecoCompleto): string {
  const temAlgum = Object.values(e).some((v) => v?.trim());
  if (!temAlgum) return '';
  return JSON.stringify({
    cep: e.cep.replace(/\D/g, ''),
    logradouro: e.logradouro.trim(),
    numero: e.numero.trim(),
    complemento: e.complemento.trim(),
    bairro: e.bairro.trim(),
    cidade: e.cidade.trim(),
    uf: e.uf.trim().toUpperCase(),
  });
}

export function formatarEnderecoLegivel(raw?: string | null): string {
  const e = parseEndereco(raw);
  const partes = [
    e.logradouro,
    e.numero && `nº ${e.numero}`,
    e.complemento,
    e.bairro,
    e.cidade && e.uf ? `${e.cidade}/${e.uf}` : e.cidade || e.uf,
    e.cep && `CEP ${e.cep}`,
  ].filter(Boolean);
  return partes.join(' — ') || '—';
}
