import { useMemo, useState } from 'react';
import './ListaConsultaPessoas.css';

export interface InstituicaoListagemItem {
  id: number;
  razaoSocial: string;
  cnpj?: string;
  cnpjExibicao?: string;
  plano?: string;
  planoExibicao?: string;
  statusFinanceiro?: string;
  statusFinanceiroExibicao?: string;
  cadastroAtivo?: boolean;
  statusCadastroExibicao?: string;
}

export type OrdenacaoCampo = 'razaoSocial';
export type OrdenacaoDirecao = 'asc' | 'desc';

const TAMANHOS_PAGINA = [5, 15, 25, 50, 100] as const;

interface Props {
  itens: InstituicaoListagemItem[];
  carregando?: boolean;
  onVerDetalhes: (item: InstituicaoListagemItem) => void;
}

function onlyAlnum(v: string) {
  return v.replace(/[^A-Za-z0-9]/g, '').toUpperCase();
}

function compararTexto(a: string, b: string, dir: OrdenacaoDirecao) {
  const r = a.localeCompare(b, 'pt-BR', { sensitivity: 'base' });
  return dir === 'asc' ? r : -r;
}

const ListaConsultaInstituicoes: React.FC<Props> = ({ itens, carregando, onVerDetalhes }) => {
  const [busca, setBusca] = useState('');
  const [pagina, setPagina] = useState(0);
  const [tamanhoPagina, setTamanhoPagina] = useState<number>(5);
  const [ordenarDir, setOrdenarDir] = useState<OrdenacaoDirecao>('asc');

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    const alnum = onlyAlnum(busca);
    if (!termo && !alnum) return itens;
    return itens.filter((item) => {
      const nomeOk = item.razaoSocial?.toLowerCase().includes(termo);
      const cnpjRaw = item.cnpj || '';
      const cnpjExib = item.cnpjExibicao || '';
      const cnpjOk = alnum.length > 0
        && (onlyAlnum(cnpjRaw).includes(alnum) || onlyAlnum(cnpjExib).includes(alnum) || cnpjExib.toLowerCase().includes(termo));
      return nomeOk || cnpjOk;
    });
  }, [itens, busca]);

  const ordenados = useMemo(() => {
    const copia = [...filtrados];
    copia.sort((a, b) => compararTexto(a.razaoSocial || '', b.razaoSocial || '', ordenarDir));
    return copia;
  }, [filtrados, ordenarDir]);

  const totalPaginas = Math.max(1, Math.ceil(ordenados.length / tamanhoPagina));
  const paginaAtual = Math.min(pagina, totalPaginas - 1);

  const paginaItens = useMemo(() => {
    const inicio = paginaAtual * tamanhoPagina;
    return ordenados.slice(inicio, inicio + tamanhoPagina);
  }, [ordenados, paginaAtual, tamanhoPagina]);

  const alternarOrdenacao = () => {
    setOrdenarDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    setPagina(0);
  };

  const iconeOrdenacao = () => (ordenarDir === 'asc' ? '↑' : '↓');

  if (carregando) {
    return <p className="field-hint">Carregando instituições...</p>;
  }

  return (
    <div className="lista-consulta">
      <div className="lista-consulta__toolbar">
        <div className="field-grow">
          <label>Buscar por nome ou CNPJ</label>
          <input
            type="search"
            placeholder="Digite para filtrar..."
            value={busca}
            onChange={(e) => {
              setBusca(e.target.value);
              setPagina(0);
            }}
          />
        </div>
        <div className="field-page-size">
          <label>Itens por página</label>
          <select
            value={tamanhoPagina}
            onChange={(e) => {
              setTamanhoPagina(Number(e.target.value));
              setPagina(0);
            }}
          >
            {TAMANHOS_PAGINA.map((n) => (
              <option key={n} value={n}>{n}</option>
            ))}
          </select>
        </div>
      </div>

      <p className="lista-consulta__contador">
        {ordenados.length} registro{ordenados.length !== 1 ? 's' : ''} encontrado{ordenados.length !== 1 ? 's' : ''}
      </p>

      <div className="table-wrap">
        <table className="audit-table">
          <thead>
            <tr>
              <th>
                <button type="button" className="lista-consulta__sort-btn" onClick={alternarOrdenacao}>
                  Nome instituição
                  <span className="lista-consulta__sort-icon lista-consulta__sort-icon--ativo">
                    {iconeOrdenacao()}
                  </span>
                </button>
              </th>
              <th>CNPJ</th>
              <th>Plano</th>
              <th>Status financeiro</th>
              <th>Status cadastro</th>
              <th style={{ width: 56, textAlign: 'center' }} aria-label="Ações" />
            </tr>
          </thead>
          <tbody>
            {paginaItens.length === 0 ? (
              <tr>
                <td colSpan={6} className="lista-consulta__vazio">
                  Nenhum registro encontrado.
                </td>
              </tr>
            ) : (
              paginaItens.map((item) => (
                <tr key={item.id}>
                  <td>{item.razaoSocial}</td>
                  <td>{item.cnpjExibicao || item.cnpj || '—'}</td>
                  <td>{item.planoExibicao || '—'}</td>
                  <td>{item.statusFinanceiroExibicao || '—'}</td>
                  <td>{item.statusCadastroExibicao || (item.cadastroAtivo ? 'Ativo' : 'Inativo')}</td>
                  <td style={{ textAlign: 'center' }}>
                    <button
                      type="button"
                      className="lista-consulta__acao"
                      title="Ver e alterar cadastro completo"
                      aria-label={`Ver cadastro de ${item.razaoSocial}`}
                      onClick={() => onVerDetalhes(item)}
                    >
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="lista-consulta__paginacao">
        <span>
          Página {paginaAtual + 1} de {totalPaginas}
        </span>
        <div className="lista-consulta__paginacao-btns">
          <button
            type="button"
            className="btn-secondary"
            disabled={paginaAtual <= 0}
            onClick={() => setPagina((p) => Math.max(0, p - 1))}
          >
            Anterior
          </button>
          <button
            type="button"
            className="btn-secondary"
            disabled={paginaAtual >= totalPaginas - 1}
            onClick={() => setPagina((p) => Math.min(totalPaginas - 1, p + 1))}
          >
            Próxima
          </button>
        </div>
      </div>
    </div>
  );
};

export default ListaConsultaInstituicoes;
