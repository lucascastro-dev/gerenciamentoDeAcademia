import { FormEvent, useEffect, useState } from 'react';
import FeedbackModal from '../../components/common/FeedbackModal';
import PageShell from '../../components/common/PageShell';
import { EQUIPE } from '../../constants/branding';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import '../../components/Financeiro/FinanceiroOperacional.css';
import './Rh.css';

interface ColaboradorOpt {
  cpf: string;
  nome: string;
}

type TipoDocumento = 'HOLERITE' | 'RECIBO';

const LancamentoHolerite: React.FC = () => {
  const hoje = new Date();
  const [colaboradores, setColaboradores] = useState<ColaboradorOpt[]>([]);
  const [cpf, setCpf] = useState('');
  const [tipo, setTipo] = useState<TipoDocumento>('HOLERITE');
  const [mes, setMes] = useState(String(hoje.getMonth() + 1));
  const [ano, setAno] = useState(String(hoje.getFullYear()));
  const [observacao, setObservacao] = useState('');
  const [arquivoPdf, setArquivoPdf] = useState<File | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  useEffect(() => {
    HttpService.listarFuncionariosResumo()
      .then((r) => setColaboradores(
        (r.data || [])
          .filter((f) => !!f.cpf)
          .map((f) => ({ cpf: f.cpf as string, nome: f.nome })),
      ))
      .catch(() => setColaboradores([]));
  }, []);

  const publicar = async (e: FormEvent) => {
    e.preventDefault();
    if (!cpf) {
      setModal({ open: true, success: false, message: 'Selecione o colaborador.' });
      return;
    }
    if (!arquivoPdf) {
      setModal({ open: true, success: false, message: 'Selecione o arquivo PDF do holerite ou recibo.' });
      return;
    }
    if (arquivoPdf.type !== 'application/pdf' && !arquivoPdf.name.toLowerCase().endsWith('.pdf')) {
      setModal({ open: true, success: false, message: 'Apenas arquivos PDF são aceitos.' });
      return;
    }

    const formData = new FormData();
    formData.append('cpfColaborador', cpf);
    formData.append('mesCompetencia', mes);
    formData.append('anoCompetencia', ano);
    formData.append('tipo', tipo);
    if (observacao.trim()) {
      formData.append('observacao', observacao.trim());
    }
    formData.append('arquivo', arquivoPdf);

    setEnviando(true);
    try {
      await HttpService.rhAnexarHoleritePdf(formData);
      setModal({
        open: true,
        success: true,
        message: 'Documento anexado. O colaborador pode visualizar em Meu holerite.',
      });
      setObservacao('');
      setArquivoPdf(null);
    } catch (err) {
      setModal({ open: true, success: false, message: extractApiMessage(err, 'Erro ao anexar documento.') });
    } finally {
      setEnviando(false);
    }
  };

  return (
    <PageShell showBack={false} title={EQUIPE.holeriteAnexoTitulo}>
      <div className="rh-page">
        <div className="card">
          <p className="field-hint">{EQUIPE.holeriteAnexoHint}</p>
          <form onSubmit={publicar} className="rh-form">
            <div className="form-grid">
              <div>
                <label htmlFor="holerite-colab">Colaborador</label>
                <select id="holerite-colab" value={cpf} onChange={(e) => setCpf(e.target.value)}>
                  <option value="">Selecione</option>
                  {colaboradores.map((c) => (
                    <option key={c.cpf} value={c.cpf}>{c.nome}</option>
                  ))}
                </select>
              </div>
              <div>
                <label htmlFor="holerite-tipo">Tipo de documento</label>
                <select id="holerite-tipo" value={tipo} onChange={(e) => setTipo(e.target.value as TipoDocumento)}>
                  <option value="HOLERITE">Holerite</option>
                  <option value="RECIBO">Recibo de pagamento</option>
                </select>
              </div>
              <div>
                <label htmlFor="holerite-mes">Mês</label>
                <select id="holerite-mes" value={mes} onChange={(e) => setMes(e.target.value)}>
                  {Array.from({ length: 12 }, (_, i) => (
                    <option key={i + 1} value={String(i + 1)}>{String(i + 1).padStart(2, '0')}</option>
                  ))}
                </select>
              </div>
              <div>
                <label htmlFor="holerite-ano">Ano</label>
                <select id="holerite-ano" value={ano} onChange={(e) => setAno(e.target.value)}>
                  {[hoje.getFullYear(), hoje.getFullYear() - 1].map((a) => (
                    <option key={a} value={a}>{a}</option>
                  ))}
                </select>
              </div>
            </div>
            <div style={{ marginTop: '0.75rem' }}>
              <label htmlFor="holerite-pdf">Arquivo PDF</label>
              <input
                id="holerite-pdf"
                type="file"
                accept="application/pdf,.pdf"
                onChange={(e) => setArquivoPdf(e.target.files?.[0] ?? null)}
              />
            </div>
            <div style={{ marginTop: '0.75rem' }}>
              <label htmlFor="holerite-obs">Observação (opcional)</label>
              <textarea id="holerite-obs" rows={2} value={observacao} onChange={(e) => setObservacao(e.target.value)} style={{ width: '100%' }} />
            </div>
            <div className="form-actions form-actions--compact" style={{ marginTop: '1rem' }}>
              <button type="submit" className="btn-primary" disabled={enviando}>
                {enviando ? 'Enviando...' : 'Anexar documento'}
              </button>
            </div>
          </form>
        </div>
      </div>
      <FeedbackModal open={modal.open} success={modal.success} message={modal.message} onClose={() => setModal((m) => ({ ...m, open: false }))} />
    </PageShell>
  );
};

export default LancamentoHolerite;
