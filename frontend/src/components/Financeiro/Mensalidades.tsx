import { useEffect, useState } from 'react';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';

const Mensalidades: React.FC = () => {
  const [lista, setLista] = useState<any[]>([]);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const carregar = () => {
    HttpService.financeiroMensalidades().then((r) => setLista(r.data));
  };

  useEffect(() => { carregar(); }, []);

  const fmt = (v: number) => (v != null ? v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : '-');

  const darBaixa = async (cpf: string) => {
    try {
      const { data } = await HttpService.baixaMensalidade(cpf);
      setModal({ open: true, success: true, message: data.message || 'Baixa registrada.' });
      carregar();
    } catch (e) {
      setModal({ open: true, success: false, message: extractApiMessage(e) });
    }
  };

  return (
    <PageShell title="Mensalidades">
      <div className="card table-wrap">
        <table className="audit-table">
          <thead>
            <tr>
              <th>Aluno</th><th>CPF</th><th>Valor</th><th>Vencimento</th><th>Último pagamento</th><th>Status</th><th />
            </tr>
          </thead>
          <tbody>
            {lista.map((m) => (
              <tr key={m.cpf}>
                <td>{m.nome}</td>
                <td>{m.cpf}</td>
                <td>{fmt(m.valorMensalidade)}</td>
                <td>Dia {m.diaVencimento}</td>
                <td>{m.dataUltimoPagamento || '—'}</td>
                <td style={{ color: m.inadimplente ? 'var(--color-danger)' : 'var(--color-success)' }}>
                  {m.inadimplente ? 'Inadimplente' : 'Em dia'}
                </td>
                <td>
                  {m.inadimplente && (
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
