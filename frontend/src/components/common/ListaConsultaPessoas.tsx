import { useMemo, useState } from 'react';
import './ListaConsultaPessoas.css';

export interface PessoaListagemItem {
  id: number;
  vinculoId?: number;
  nome: string;
  cpf?: string;
  cpfExibicao?: string;
  dataDeNascimento?: string;
  cargo?: string;
  instituicaoId?: number;
  instituicaoNome?: string;
}

export type OrdenacaoCampo = 'nome' | 'cargo' | 'instituicao';
export type OrdenacaoDirecao = 'asc' | 'desc';

const TAMANHOS_PAGINA = [5, 15, 25, 50, 100] as const;

interface Props {
  itens: PessoaListagemItem[];
  carregando?: boolean;
  exibirCargo?: boolean;
  exibirInstituicao?: boolean;
  onVerDetalhes: (item: PessoaListagemItem) => void;
}

function chaveLinha(item: PessoaListagemItem) {
  if (item.vinculoId) return `v-${item.vinculoId}`;
  if (item.instituicaoId) return `${item.id}-${item.instituicaoId}`;
  return String(item.id);
}

function formatarData(iso?: string) {
  if (!iso) return '—';
  const [y, m, d] = iso.split('-');
  if (!y || !m || !d) return iso;
  return `${d}/${m}/${y}`;
}

function onlyDigits(v: string) {
  return v.replace(/\D/g, '');
}

function compararTexto(a: string, b: string, dir: OrdenacaoDirecao) {
  const r = a.localeCompare(b, 'pt-BR', { sensitivity: 'base' });
  return dir === 'asc' ? r : -r;
}

const ListaConsultaPessoas: React.FC<Props> = ({ itens, carregando, exibirCargo, exibirInstituicao, onVerDetalhes }) => {
  const [busca, setBusca] = useState('');
  const [pagina, setPagina] = useState(0);
  const [tamanhoPagina, setTamanhoPagina] = useState<number>(5);
  const [ordenarPor, setOrdenarPor] = useState<OrdenacaoCampo>('nome');
  const [ordenarDir, setOrdenarDir] = useState<OrdenacaoDirecao>('asc');

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    const digitos = onlyDigits(busca);
    if (!termo && !digitos) return itens;
    return itens.filter((item) => {
      const nomeOk = item.nome?.toLowerCase().includes(termo);
      const cpfRaw = item.cpf || '';
      const cpfExib = item.cpfExibicao || '';
      const cpfOk = digitos.length > 0
        && (cpfRaw.includes(digitos) || onlyDigits(cpfExib).includes(digitos) || cpfExib.includes(termo));
      return nomeOk || cpfOk;
    });
  }, [itens, busca]);

  const ordenados = useMemo(() => {
    const copia = [...filtrados];
    copia.sort((a, b) => {
      if (ordenarPor === 'cargo' && exibirCargo) {
        return compararTexto(a.cargo || '', b.cargo || '', ordenarDir);
      }
      if (ordenarPor === 'instituicao' && exibirInstituicao) {
        return compararTexto(a.instituicaoNome || '', b.instituicaoNome || '', ordenarDir);
      }
      return compararTexto(a.nome || '', b.nome || '', ordenarDir);
    });
    return copia;
  }, [filtrados, ordenarPor, ordenarDir, exibirCargo, exibirInstituicao]);

  const totalPaginas = Math.max(1, Math.ceil(ordenados.length / tamanhoPagina));
  const paginaAtual = Math.min(pagina, totalPaginas - 1);

  const paginaItens = useMemo(() => {
    const inicio = paginaAtual * tamanhoPagina;
    return ordenados.slice(inicio, inicio + tamanhoPagina);
  }, [ordenados, paginaAtual, tamanhoPagina]);

  const alternarOrdenacao = (campo: OrdenacaoCampo) => {
    if (ordenarPor === campo) {
      setOrdenarDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setOrdenarPor(campo);
      setOrdenarDir('asc');
    }
    setPagina(0);
  };

  const iconeOrdenacao = (campo: OrdenacaoCampo) => {
    if (ordenarPor !== campo) return '↕';
    return ordenarDir === 'asc' ? '↑' : '↓';
  };

  if (carregando) {
    return <p className="field-hint">Carregando cadastros...</p>;
  }

  return (
    <div className="lista-consulta">
      <div className="lista-consulta__toolbar">
        <div className="field-grow">
          <label>Buscar por nome ou CPF</label>
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
                <button type="button" className="lista-consulta__sort-btn" onClick={() => alternarOrdenacao('nome')}>
                  Nome
                  <span className={`lista-consulta__sort-icon ${ordenarPor === 'nome' ? 'lista-consulta__sort-icon--ativo' : ''}`}>
                    {iconeOrdenacao('nome')}
                  </span>
                </button>
              </th>
              <th>CPF</th>
              <th>Nascimento</th>
              {exibirInstituicao && (
                <th>
                  <button type="button" className="lista-consulta__sort-btn" onClick={() => alternarOrdenacao('instituicao')}>
                    Instituição
                    <span className={`lista-consulta__sort-icon ${ordenarPor === 'instituicao' ? 'lista-consulta__sort-icon--ativo' : ''}`}>
                      {iconeOrdenacao('instituicao')}
                    </span>
                  </button>
                </th>
              )}
              {exibirCargo && (
                <th>
                  <button type="button" className="lista-consulta__sort-btn" onClick={() => alternarOrdenacao('cargo')}>
                    Cargo
                    <span className={`lista-consulta__sort-icon ${ordenarPor === 'cargo' ? 'lista-consulta__sort-icon--ativo' : ''}`}>
                      {iconeOrdenacao('cargo')}
                    </span>
                  </button>
                </th>
              )}
              <th style={{ width: 56, textAlign: 'center' }} aria-label="Ações" />
            </tr>
          </thead>
          <tbody>
            {paginaItens.length === 0 ? (
              <tr>
                <td colSpan={3 + (exibirInstituicao ? 1 : 0) + (exibirCargo ? 1 : 0) + 1} className="lista-consulta__vazio">
                  Nenhum registro encontrado.
                </td>
              </tr>
            ) : (
              paginaItens.map((item) => (
                <tr key={chaveLinha(item)}>
                  <td>{item.nome?.trim() || '—'}</td>
                  <td>{item.cpfExibicao || item.cpf || '—'}</td>
                  <td>{formatarData(item.dataDeNascimento)}</td>
                  {exibirInstituicao && <td>{item.instituicaoNome || '—'}</td>}
                  {exibirCargo && <td>{item.cargo || '—'}</td>}
                  <td style={{ textAlign: 'center' }}>
                    <button
                      type="button"
                      className="lista-consulta__acao"
                      title="Ver cadastro completo"
                      aria-label={`Ver cadastro de ${item.nome?.trim() || 'aluno'}`}
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

export default ListaConsultaPessoas;
