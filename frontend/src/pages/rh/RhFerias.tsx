import { useCallback, useEffect, useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import { carregarSessao, possuiPermissao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import './Rh.css';

const STATUS_OPCOES = ['TODOS', 'PENDENTE', 'APROVADO', 'REJEITADO'] as const;

function formatarData(iso: string): string {
  const [y, m, d] = iso.split('-');
  return `${d}/${m}/${y}`;
}

const RhFerias: React.FC = () => {
  const sessao = carregarSessao();
  const podeGerenciar = possuiPermissao(sessao, 'rh:ferias-gerenciar');
  const [filtro, setFiltro] = useState<(typeof STATUS_OPCOES)[number]>('PENDENTE');
  const [lista, setLista] = useState<Array<{
    id: number;
    nomeColaborador: string;
    dataInicio: string;
    dataFim: string;
    diasSolicitados: number;
    status: string;
    statusDescricao: string;
  }>>([]);
  const [carregando, setCarregando] = useState(true);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const carregar = useCallback(async () => {
    setCarregando(true);
    try {
      const status = filtro === 'TODOS' ? undefined : filtro;
      const r = await HttpService.rhFeriasListar(status);
      setLista(r.data || []);
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao carregar solicitações.') });
      setLista([]);
    } finally {
      setCarregando(false);
    }
  }, [filtro]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const decidir = async (id: number, status: 'APROVADO' | 'REJEITADO') => {
    try {
      await HttpService.rhFeriasDecidir(id, { status });
      setModal({
        open: true,
        success: true,
        message: status === 'APROVADO' ? 'Férias aprovadas.' : 'Solicitação rejeitada.',
      });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao registrar decisão.') });
    }
  };

  return (
    <PageShell showBack={false}>
      <div className="rh-page">
        <div className="fin-op__toolbar card">
          <div>
            <label htmlFor="rh-ferias-status">Filtrar por status</label>
            <select id="rh-ferias-status" value={filtro} onChange={(e) => setFiltro(e.target.value as typeof filtro)}>
              {STATUS_OPCOES.map((s) => (
                <option key={s} value={s}>{s === 'TODOS' ? 'Todos' : s.charAt(0) + s.slice(1).toLowerCase()}</option>
              ))}
            </select>
          </div>
        </div>

        <p className="field-hint">
          Cada colaborador tem direito a 30 dias de descanso remunerado a cada 365 dias de vínculo na instituição.
          {!podeGerenciar && ' Você pode consultar as solicitações; aprovações são feitas pelo RH ou Administrador.'}
        </p>

        <div className="card table-wrap">
          {carregando && <p className="field-hint">Carregando...</p>}
          <table className="audit-table">
            <thead>
              <tr>
                <th>Colaborador</th>
                <th>Período</th>
                <th>Dias</th>
                <th>Status</th>
                {podeGerenciar && <th />}
              </tr>
            </thead>
            <tbody>
              {lista.length === 0 && !carregando && (
                <tr><td colSpan={podeGerenciar ? 5 : 4}>Nenhuma solicitação encontrada.</td></tr>
              )}
              {lista.map((s) => (
                <tr key={s.id}>
                  <td>{s.nomeColaborador}</td>
                  <td>{formatarData(s.dataInicio)} – {formatarData(s.dataFim)}</td>
                  <td>{s.diasSolicitados}</td>
                  <td>{s.statusDescricao}</td>
                  {podeGerenciar && (
                    <td>
                      {s.status === 'PENDENTE' && (
                        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                          <button type="button" className="btn-primary" onClick={() => decidir(s.id, 'APROVADO')}>
                            Aprovar
                          </button>
                          <button type="button" className="btn-secondary" onClick={() => decidir(s.id, 'REJEITADO')}>
                            Rejeitar
                          </button>
                        </div>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        message={modal.message}
        onClose={() => setModal((m) => ({ ...m, open: false }))}
      />
    </PageShell>
  );
};

export default RhFerias;
