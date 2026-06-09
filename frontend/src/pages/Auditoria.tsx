import { useEffect, useMemo, useState } from 'react';
import PageShell from '../components/common/PageShell';
import HttpService from '../services/HttpService';

interface RegistroAuditoria {
  id?: number;
  ajuste: string;
  dataHora: string;
  usuarioLogin: string;
  entidade: string;
  referencia?: string;
  motivo?: string;
}

const TAMANHOS_PAGINA = [10, 25, 50, 100] as const;
const AJUSTES = ['Todos', 'Criação', 'Edição', 'Exclusão'] as const;

const Auditoria: React.FC = () => {
  const [registros, setRegistros] = useState<RegistroAuditoria[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [busca, setBusca] = useState('');
  const [filtroAjuste, setFiltroAjuste] = useState<(typeof AJUSTES)[number]>('Todos');
  const [pagina, setPagina] = useState(0);
  const [tamanhoPagina, setTamanhoPagina] = useState<number>(25);

  useEffect(() => {
    setCarregando(true);
    setErro('');
    HttpService.listarAuditoria()
      .then((r) => setRegistros(r.data))
      .catch(() => {
        setRegistros([]);
        setErro('Não foi possível carregar os registros de auditoria.');
      })
      .finally(() => setCarregando(false));
  }, []);

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    return registros.filter((r) => {
      const ajusteOk = filtroAjuste === 'Todos' || r.ajuste === filtroAjuste;
      if (!ajusteOk) return false;
      if (!termo) return true;
      const campos = [
        r.ajuste,
        r.usuarioLogin,
        r.entidade,
        r.referencia,
        r.motivo,
      ].filter(Boolean).join(' ').toLowerCase();
      return campos.includes(termo);
    });
  }, [registros, busca, filtroAjuste]);

  const totalPaginas = Math.max(1, Math.ceil(filtrados.length / tamanhoPagina));
  const paginaAtual = Math.min(pagina, totalPaginas - 1);
  const itensPagina = filtrados.slice(
    paginaAtual * tamanhoPagina,
    paginaAtual * tamanhoPagina + tamanhoPagina,
  );

  const fmtData = (iso?: string) => {
    if (!iso) return '—';
    try {
      return new Date(iso).toLocaleString('pt-BR');
    } catch {
      return iso;
    }
  };

  return (
    <PageShell
      title="Auditoria"
      subtitle="Histórico de criações, edições e exclusões registradas no sistema"
    >
      <div className="card" style={{ marginBottom: '1rem' }}>
        <div className="form-grid" style={{ gridTemplateColumns: '1fr 200px', gap: '1rem' }}>
          <div>
            <label>Buscar</label>
            <input
              placeholder="Usuário, referência, entidade ou motivo"
              value={busca}
              onChange={(e) => {
                setBusca(e.target.value);
                setPagina(0);
              }}
            />
          </div>
          <div>
            <label>Ajuste</label>
            <select
              value={filtroAjuste}
              onChange={(e) => {
                setFiltroAjuste(e.target.value as (typeof AJUSTES)[number]);
                setPagina(0);
              }}
            >
              {AJUSTES.map((a) => (
                <option key={a} value={a}>{a}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      <div className="card table-wrap">
        {carregando && <p className="field-hint">Carregando registros...</p>}
        {!carregando && erro && <p className="field-hint" style={{ color: '#b91c1c' }}>{erro}</p>}
        {!carregando && !erro && filtrados.length === 0 && (
          <p className="field-hint">Nenhum registro de auditoria encontrado.</p>
        )}
        {!carregando && filtrados.length > 0 && (
          <>
            <table className="audit-table">
              <thead>
                <tr>
                  <th>Ajuste</th>
                  <th>Quando</th>
                  <th>Por quem</th>
                  <th>Entidade</th>
                  <th>Referência</th>
                  <th>Motivo</th>
                </tr>
              </thead>
              <tbody>
                {itensPagina.map((r, idx) => (
                  <tr key={r.id ?? `env-${r.dataHora}-${idx}`}>
                    <td>
                      <span className={`audit-badge audit-badge--${r.ajuste.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, '')}`}>
                        {r.ajuste}
                      </span>
                    </td>
                    <td>{fmtData(r.dataHora)}</td>
                    <td>{r.usuarioLogin || '—'}</td>
                    <td>{r.entidade || '—'}</td>
                    <td>{r.referencia || '—'}</td>
                    <td>{r.motivo || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="paginacao-bar" style={{ marginTop: '1rem' }}>
              <span className="field-hint">
                {filtrados.length} registro(s) — página {paginaAtual + 1} de {totalPaginas}
              </span>
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap' }}>
                <label className="field-hint" style={{ margin: 0 }}>
                  Por página
                  <select
                    value={tamanhoPagina}
                    onChange={(e) => {
                      setTamanhoPagina(Number(e.target.value));
                      setPagina(0);
                    }}
                    style={{ marginLeft: '0.35rem' }}
                  >
                    {TAMANHOS_PAGINA.map((t) => (
                      <option key={t} value={t}>{t}</option>
                    ))}
                  </select>
                </label>
                <button
                  type="button"
                  className="btn btn-secondary"
                  disabled={paginaAtual <= 0}
                  onClick={() => setPagina((p) => Math.max(0, p - 1))}
                >
                  Anterior
                </button>
                <button
                  type="button"
                  className="btn btn-secondary"
                  disabled={paginaAtual >= totalPaginas - 1}
                  onClick={() => setPagina((p) => Math.min(totalPaginas - 1, p + 1))}
                >
                  Próxima
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </PageShell>
  );
};

export default Auditoria;
