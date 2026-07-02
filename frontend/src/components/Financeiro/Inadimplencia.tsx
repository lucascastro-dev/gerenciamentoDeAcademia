import { useCallback, useEffect, useState } from 'react';
import CompetenciaFiltro from './CompetenciaFiltro';
import HttpService from '../../services/HttpService';
import './FinanceiroOperacional.css';

interface InadimplenteItem {
  cpf: string;
  nome: string;
  valorMensalidade?: number;
  diaVencimento: number;
  statusDescricao?: string;
  dataVencimentoCompetencia?: string;
}

const Inadimplencia: React.FC = () => {
  const hoje = new Date();
  const [mes, setMes] = useState(String(hoje.getMonth() + 1));
  const [ano, setAno] = useState(String(hoje.getFullYear()));
  const [lista, setLista] = useState<InadimplenteItem[]>([]);
  const [carregando, setCarregando] = useState(false);

  const carregar = useCallback(() => {
    setCarregando(true);
    HttpService.financeiroInadimplentes(Number(mes), Number(ano))
      .then((r) => setLista(r.data))
      .catch(() => setLista([]))
      .finally(() => setCarregando(false));
  }, [mes, ano]);

  useEffect(() => { carregar(); }, [carregar]);

  const fmt = (v?: number) => (v != null ? v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : '—');

  const formatarData = (iso?: string) => {
    if (!iso) return '—';
    const [y, m, d] = iso.split('T')[0].split('-');
    return y && m && d ? `${d}/${m}/${y}` : iso;
  };

  return (
    <div className="fin-op">
      <div className="fin-op__toolbar card">
        <CompetenciaFiltro
          idPrefix="inadimplencia"
          mes={mes}
          ano={ano}
          onMesChange={setMes}
          onAnoChange={setAno}
        />
        {carregando && <span className="field-hint">Carregando...</span>}
      </div>

      <div className="card">
        <h2 style={{ marginTop: 0 }}>Inadimplência</h2>
        {lista.length === 0 && !carregando ? (
          <p>Nenhum aluno inadimplente nesta competência.</p>
        ) : (
          <div className="table-wrap">
            <table className="audit-table">
              <thead>
                <tr>
                  <th>Aluno</th>
                  <th>CPF</th>
                  <th>Valor</th>
                  <th>Vencimento</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {lista.map((m) => (
                  <tr key={m.cpf}>
                    <td>{m.nome}</td>
                    <td>{m.cpf}</td>
                    <td>{fmt(m.valorMensalidade)}</td>
                    <td>{formatarData(m.dataVencimentoCompetencia) || `Dia ${m.diaVencimento}`}</td>
                    <td style={{ color: 'var(--color-danger)' }}>
                      {m.statusDescricao || 'Inadimplente'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default Inadimplencia;
