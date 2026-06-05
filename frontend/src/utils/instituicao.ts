/** Normaliza payload da API (id ou registroInstituicao). */
export function idInstituicao(item: {
  id?: number;
  registroInstituicao?: number;
}): number {
  return item.id ?? item.registroInstituicao ?? 0;
}

export function mapInstituicoesApi<T extends { razaoSocial: string }>(
  lista: Array<T & { id?: number; registroInstituicao?: number }>,
): Array<{ id: number; razaoSocial: string }> {
  return (lista || [])
    .map((i) => ({ id: idInstituicao(i), razaoSocial: i.razaoSocial }))
    .filter((i) => i.id > 0);
}
