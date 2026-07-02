import { useCallback, useEffect, useMemo, useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import CompetenciaFiltro from './CompetenciaFiltro';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import './FinanceiroOperacional.css';

interface MensalidadeItem {
  cpf: string;
  nome: string;
  valorMensalidade: number;
  diaVencimento: number;
  dataUltimoPagamento?: string;
  inadimplente: boolean;
  statusDescricao?: string;
  dataVencimentoCompetencia?: string;
}

const Mensalidades: React.FC = () => {
  const [mes, setMes] = useState(String(new Date().getMonth() + 1));
  const [ano, setAno] = useState(String(new Date().getFullYear()));
  const [lista, setLista] = useState<MensalidadeItem[]>([]);
  const [carregando, setCarregando] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const competenciaAtual = useMemo(() => {
    const agora = new Date();
    return agora.getMonth() + 1 === Number(mes) && agora.getFullYear() === Number(ano);
  }, [mes, ano]);

  const carregar = useCallback(() => {
    setCarregando(true);
    HttpService.financeiroMensalidades(Number(mes), Number(ano))
      .then((r) => setLista(r.data))
      .catch(() => setLista([]))
      .finally(() => setCarregando(false));
  }, [mes, ano]);

  useEffect(() => { carregar(); }, [carregar]);

  const fmt = (v: number) => (v != null ? v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : '-');

  const formatarData = (iso?: string) => {
    if (!iso) return '—';
    const [y, m, d] = iso.split('T')[0].split('-');
    return y && m && d ? `${d}/${m}/${y}` : iso;
  };

  const darBaixa = async (cpf: string) => {
    try {
      const { data } = await HttpService.baixaMensalidade(cpf);
      setModal({ open: true, success: true, message: data.message || 'Baixa registrada.' });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  const statusCor = (item: MensalidadeItem) => {
    if (item.inadimplente) return 'var(--color-danger)';
    if (item.statusDescricao === 'Pago') return 'var(--color-success)';
    return 'var(--color-muted)';
  };

  return (
    <PageShell title="Mensalidades">
      <div className="fin-op">
        <div className="fin-op__toolbar card">
          <CompetenciaFiltro
            idPrefix="mensalidades"
            mes={mes}
            ano={ano}
            onMesChange={setMes}
            onAnoChange={setAno}
          />
          {carregando && <span className="field-hint">Carregando...</span>}
        </div>

        <div className="card table-wrap">
          <table className="audit-table">
            <thead>
              <tr>
                <th>Aluno</th>
                <th>CPF</th>
                <th>Valor</th>
                <th>Vencimento</th>
                <th>Pagamento</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {lista.length === 0 && !carregando && (
                <tr><td colSpan={7}>Nenhuma mensalidade encontrada para esta competência.</td></tr>
              )}
              {lista.map((m) => (
                <tr key={m.cpf}>
                  <td>{m.nome}</td>
                  <td>{m.cpf}</td>
                  <td>{fmt(m.valorMensalidade)}</td>
                  <td>{formatarData(m.dataVencimentoCompetencia) || `Dia ${m.diaVencimento}`}</td>
                  <td>{formatarData(m.dataUltimoPagamento)}</td>
                  <td style={{ color: statusCor(m) }}>
                    {m.statusDescricao || (m.inadimplente ? 'Inadimplente' : 'Em dia')}
                  </td>
                  <td>
                    {competenciaAtual && m.inadimplente && (
                      <button type="button" className="btn-secondary" onClick={() => darBaixa(m.cpf)}>
                        Dar baixa
                      </button>
                    )}
                  </td>
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

export default Mensalidades;
