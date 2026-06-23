import { useMemo, useState } from 'react';
import PageShell from '../common/PageShell';
import './FinanceiroOperacional.css';

interface Lancamento {
  id: number;
  data: string;
  descricao: string;
  valor: number;
  tipo: 'Entrada' | 'Saída';
  conciliado: boolean;
}

const LANCAMENTOS_MOCK: Lancamento[] = [
  { id: 1, data: '2026-06-02', descricao: 'Mensalidade — Aluno A', valor: 180, tipo: 'Entrada', conciliado: true },
  { id: 2, data: '2026-06-03', descricao: 'Mensalidade — Aluno B', valor: 180, tipo: 'Entrada', conciliado: false },
  { id: 3, data: '2026-06-05', descricao: 'Fornecedor — Material esportivo', valor: 420, tipo: 'Saída', conciliado: false },
  { id: 4, data: '2026-06-08', descricao: 'PIX — Turma infantil', valor: 360, tipo: 'Entrada', conciliado: true },
];

const ConciliacaoBancaria: React.FC = () => {
  const [lancamentos, setLancamentos] = useState(LANCAMENTOS_MOCK);
  const [filtro, setFiltro] = useState<'todos' | 'pendentes' | 'conciliados'>('todos');

  const fmt = (v: number) => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });

  const lista = useMemo(() => {
    if (filtro === 'pendentes') return lancamentos.filter((l) => !l.conciliado);
    if (filtro === 'conciliados') return lancamentos.filter((l) => l.conciliado);
    return lancamentos;
  }, [lancamentos, filtro]);

  const resumo = useMemo(() => {
    const entradas = lancamentos.filter((l) => l.tipo === 'Entrada').reduce((s, l) => s + l.valor, 0);
    const saidas = lancamentos.filter((l) => l.tipo === 'Saída').reduce((s, l) => s + l.valor, 0);
    const pendentes = lancamentos.filter((l) => !l.conciliado).length;
    return { entradas, saidas, saldo: entradas - saidas, pendentes };
  }, [lancamentos]);

  const conciliar = (id: number) => {
    setLancamentos((listaAtual) => listaAtual.map((l) => (
      l.id === id ? { ...l, conciliado: true } : l
    )));
  };

  return (
    <PageShell showBack={false}>
      <div className="fin-op">
        <div className="fin-op__kpis">
          <div className="fin-op__kpi card"><strong>{fmt(resumo.entradas)}</strong><span>Entradas</span></div>
          <div className="fin-op__kpi card"><strong>{fmt(resumo.saidas)}</strong><span>Saídas</span></div>
          <div className="fin-op__kpi card"><strong>{fmt(resumo.saldo)}</strong><span>Saldo</span></div>
          <div className="fin-op__kpi card"><strong>{resumo.pendentes}</strong><span>Pendentes</span></div>
        </div>

        <div className="fin-op__toolbar card">
          <label htmlFor="filtro-conciliacao">Exibir</label>
          <select id="filtro-conciliacao" value={filtro} onChange={(e) => setFiltro(e.target.value as typeof filtro)}>
            <option value="todos">Todos os lançamentos</option>
            <option value="pendentes">Somente pendentes</option>
            <option value="conciliados">Somente conciliados</option>
          </select>
        </div>

        <div className="card table-wrap">
          <table className="audit-table">
            <thead>
              <tr>
                <th>Data</th>
                <th>Descrição</th>
                <th>Tipo</th>
                <th>Valor</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {lista.map((l) => (
                <tr key={l.id}>
                  <td>{l.data.split('-').reverse().join('/')}</td>
                  <td>{l.descricao}</td>
                  <td>{l.tipo}</td>
                  <td>{fmt(l.valor)}</td>
                  <td>
                    <span className={`fin-op__badge fin-op__badge--${l.conciliado ? 'pago' : 'pendente'}`}>
                      {l.conciliado ? 'Conciliado' : 'Pendente'}
                    </span>
                  </td>
                  <td>
                    {!l.conciliado && (
                      <button type="button" className="btn-secondary" onClick={() => conciliar(l.id)}>
                        Conciliar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </PageShell>
  );
};

export default ConciliacaoBancaria;
