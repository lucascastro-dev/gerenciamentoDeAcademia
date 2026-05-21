import { useEffect, useState } from 'react';
import PageShell from '../components/common/PageShell';
import HttpService from '../services/HttpService';

interface FuncionarioResumo {
  id: number;
  cpf: string;
  nome: string;
  tipoFuncionario?: string;
}

interface Revisao {
  revisionNumber: number;
  revisionDate?: string;
  cpf?: string;
  nome?: string;
  tipoFuncionario?: string;
  cadastroAtivo?: boolean;
  enderecoResumo?: string;
}

const Auditoria: React.FC = () => {
  const [funcionarios, setFuncionarios] = useState<FuncionarioResumo[]>([]);
  const [selecionado, setSelecionado] = useState<FuncionarioResumo | null>(null);
  const [revisoes, setRevisoes] = useState<Revisao[]>([]);
  const [loading, setLoading] = useState(false);
  const [filtro, setFiltro] = useState('');

  useEffect(() => {
    HttpService.listarFuncionarios()
      .then((r) => setFuncionarios(r.data))
      .catch(() => setFuncionarios([]));
  }, []);

  const carregarRevisoes = async (f: FuncionarioResumo) => {
    setSelecionado(f);
    setLoading(true);
    try {
      const { data } = await HttpService.auditoriaFuncionario(f.id);
      setRevisoes(data);
    } catch {
      setRevisoes([]);
    } finally {
      setLoading(false);
    }
  };

  const listaFiltrada = funcionarios.filter(
    (f) =>
      f.nome?.toLowerCase().includes(filtro.toLowerCase()) ||
      f.cpf?.includes(filtro.replace(/\D/g, '')),
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
    <PageShell title="Auditoria" subtitle="Histórico de alterações (Envers) em cadastros de funcionários">
      <div className="card" style={{ marginBottom: '1rem' }}>
        <label>Buscar colaborador</label>
        <input
          placeholder="Nome ou CPF"
          value={filtro}
          onChange={(e) => setFiltro(e.target.value)}
        />
      </div>

      <div className="audit-layout">
        <div className="card table-wrap" style={{ maxHeight: 420, overflow: 'auto' }}>
          <h3 style={{ marginTop: 0 }}>Colaboradores</h3>
          <table className="audit-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
              </tr>
            </thead>
            <tbody>
              {listaFiltrada.map((f) => (
                <tr
                  key={f.id}
                  style={{ cursor: 'pointer', background: selecionado?.id === f.id ? '#eff6ff' : undefined }}
                  onClick={() => carregarRevisoes(f)}
                >
                  <td>{f.nome}</td>
                  <td>{f.cpf}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card table-wrap">
          <h3 style={{ marginTop: 0 }}>
            {selecionado ? `Revisões — ${selecionado.nome}` : 'Selecione um colaborador'}
          </h3>
          {loading && <p className="field-hint">Carregando histórico...</p>}
          {!loading && selecionado && revisoes.length === 0 && (
            <p className="field-hint">Nenhuma revisão registrada.</p>
          )}
          {!loading && revisoes.length > 0 && (
            <table className="audit-table">
              <thead>
                <tr>
                  <th>Rev.</th>
                  <th>Data</th>
                  <th>Perfil</th>
                  <th>Ativo</th>
                  <th>Endereço</th>
                </tr>
              </thead>
              <tbody>
                {revisoes.map((r) => (
                  <tr key={r.revisionNumber}>
                    <td>{r.revisionNumber}</td>
                    <td>{fmtData(r.revisionDate)}</td>
                    <td>{r.tipoFuncionario ?? '—'}</td>
                    <td>{r.cadastroAtivo ? 'Sim' : 'Não'}</td>
                    <td>{r.enderecoResumo || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </PageShell>
  );
};

export default Auditoria;
