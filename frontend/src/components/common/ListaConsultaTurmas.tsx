import { useMemo, useState } from 'react';
import './ListaConsultaPessoas.css';

export interface TurmaListagemItem {
  id: number;
  modalidade: string;
  horario: string;
  sala?: string;
  dias?: string[];
  professorNome?: string;
  professorCpf?: string;
  instituicaoNome?: string;
}

export type OrdenacaoTurmaCampo = 'modalidade' | 'horario' | 'professor' | 'instituicao';
export type OrdenacaoDirecao = 'asc' | 'desc';

const TAMANHOS_PAGINA = [5, 15, 25, 50, 100] as const;

interface ProfessorOpt {
  cpf: string;
  nome: string;
}

interface Props {
  itens: TurmaListagemItem[];
  carregando?: boolean;
  exibirInstituicao?: boolean;
  professores?: ProfessorOpt[];
  onVerDetalhes: (item: TurmaListagemItem) => void;
}

function compararTexto(a: string, b: string, dir: OrdenacaoDirecao) {
  const r = a.localeCompare(b, 'pt-BR', { sensitivity: 'base' });
  return dir === 'asc' ? r : -r;
}

const ListaConsultaTurmas: React.FC<Props> = ({
  itens,
  carregando,
  exibirInstituicao,
  professores = [],
  onVerDetalhes,
}) => {
  const [busca, setBusca] = useState('');
  const [filtroProfessor, setFiltroProfessor] = useState('');
  const [pagina, setPagina] = useState(0);
  const [tamanhoPagina, setTamanhoPagina] = useState<number>(5);
  const [ordenarPor, setOrdenarPor] = useState<OrdenacaoTurmaCampo>('modalidade');
  const [ordenarDir, setOrdenarDir] = useState<OrdenacaoDirecao>('asc');

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    return itens.filter((item) => {
      const nomeOk = !termo || item.modalidade.toLowerCase().includes(termo);
      const profOk = !filtroProfessor || item.professorCpf === filtroProfessor;
      return nomeOk && profOk;
    });
  }, [itens, busca, filtroProfessor]);

  const ordenados = useMemo(() => {
    const copia = [...filtrados];
    copia.sort((a, b) => {
      if (ordenarPor === 'horario') {
        return compararTexto(a.horario || '', b.horario || '', ordenarDir);
      }
      if (ordenarPor === 'professor') {
        return compararTexto(a.professorNome || '', b.professorNome || '', ordenarDir);
      }
      if (ordenarPor === 'instituicao' && exibirInstituicao) {
        return compararTexto(a.instituicaoNome || '', b.instituicaoNome || '', ordenarDir);
      }
      return compararTexto(a.modalidade || '', b.modalidade || '', ordenarDir);
    });
    return copia;
  }, [filtrados, ordenarPor, ordenarDir, exibirInstituicao]);

  const totalPaginas = Math.max(1, Math.ceil(ordenados.length / tamanhoPagina));
  const paginaAtual = Math.min(pagina, totalPaginas - 1);

  const paginaItens = useMemo(() => {
    const inicio = paginaAtual * tamanhoPagina;
    return ordenados.slice(inicio, inicio + tamanhoPagina);
  }, [ordenados, paginaAtual, tamanhoPagina]);

  const alternarOrdenacao = (campo: OrdenacaoTurmaCampo) => {
    if (ordenarPor === campo) {
      setOrdenarDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setOrdenarPor(campo);
      setOrdenarDir('asc');
    }
    setPagina(0);
  };

  const iconeOrdenacao = (campo: OrdenacaoTurmaCampo) => {
    if (ordenarPor !== campo) return '↕';
    return ordenarDir === 'asc' ? '↑' : '↓';
  };

  if (carregando) {
    return <p className="field-hint">Carregando turmas...</p>;
  }

  return (
    <div className="lista-consulta">
      <div className="lista-consulta__toolbar">
        <div className="field-grow">
          <label>Buscar por nome da turma</label>
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
        <div className="field-grow">
          <label>Professor</label>
          <select
            value={filtroProfessor}
            onChange={(e) => {
              setFiltroProfessor(e.target.value);
              setPagina(0);
            }}
          >
            <option value="">Todos</option>
            {professores.map((p) => (
              <option key={p.cpf} value={p.cpf}>{p.nome}</option>
            ))}
          </select>
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
        {ordenados.length} turma{ordenados.length !== 1 ? 's' : ''} encontrada{ordenados.length !== 1 ? 's' : ''}
      </p>

      <div className="table-wrap">
        <table className="audit-table">
          <thead>
            <tr>
              <th>
                <button type="button" className="lista-consulta__sort-btn" onClick={() => alternarOrdenacao('modalidade')}>
                  Turma
                  <span className={`lista-consulta__sort-icon ${ordenarPor === 'modalidade' ? 'lista-consulta__sort-icon--ativo' : ''}`}>
                    {iconeOrdenacao('modalidade')}
                  </span>
                </button>
              </th>
              <th>
                <button type="button" className="lista-consulta__sort-btn" onClick={() => alternarOrdenacao('horario')}>
                  Horário
                  <span className={`lista-consulta__sort-icon ${ordenarPor === 'horario' ? 'lista-consulta__sort-icon--ativo' : ''}`}>
                    {iconeOrdenacao('horario')}
                  </span>
                </button>
              </th>
              <th>Sala</th>
              <th>Dias</th>
              <th>
                <button type="button" className="lista-consulta__sort-btn" onClick={() => alternarOrdenacao('professor')}>
                  Professor
                  <span className={`lista-consulta__sort-icon ${ordenarPor === 'professor' ? 'lista-consulta__sort-icon--ativo' : ''}`}>
                    {iconeOrdenacao('professor')}
                  </span>
                </button>
              </th>
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
              <th style={{ width: 56, textAlign: 'center' }} aria-label="Ações" />
            </tr>
          </thead>
          <tbody>
            {paginaItens.length === 0 ? (
              <tr>
                <td colSpan={5 + (exibirInstituicao ? 1 : 0) + 1} className="lista-consulta__vazio">
                  Nenhuma turma encontrada.
                </td>
              </tr>
            ) : (
              paginaItens.map((item) => (
                <tr key={item.id}>
                  <td>{item.modalidade}</td>
                  <td>{item.horario || '—'}</td>
                  <td>{item.sala || '—'}</td>
                  <td>{item.dias?.length ? item.dias.join(', ') : '—'}</td>
                  <td>{item.professorNome || 'Não vinculado'}</td>
                  {exibirInstituicao && <td>{item.instituicaoNome || '—'}</td>}
                  <td style={{ textAlign: 'center' }}>
                    <button
                      type="button"
                      className="lista-consulta__acao"
                      title="Ver detalhes da turma"
                      aria-label={`Ver turma ${item.modalidade}`}
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

export default ListaConsultaTurmas;
