import { useCallback, useEffect, useState } from 'react';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import './Colaborador.css';

const MESES = [
  { v: '01', l: 'Janeiro' }, { v: '02', l: 'Fevereiro' }, { v: '03', l: 'Março' },
  { v: '04', l: 'Abril' }, { v: '05', l: 'Maio' }, { v: '06', l: 'Junho' },
  { v: '07', l: 'Julho' }, { v: '08', l: 'Agosto' }, { v: '09', l: 'Setembro' },
  { v: '10', l: 'Outubro' }, { v: '11', l: 'Novembro' }, { v: '12', l: 'Dezembro' },
];
const ANO_ATUAL = new Date().getFullYear();

interface Documento {
  id: number;
  tipo: 'HOLERITE' | 'RECIBO' | 'INFORME';
  tipoDescricao: string;
  possuiArquivoPdf?: boolean;
  nomeArquivo?: string;
  valorLiquido?: number;
}

const MeuHolerite: React.FC = () => {
  const [mes, setMes] = useState(String(new Date().getMonth() + 1).padStart(2, '0'));
  const [ano, setAno] = useState(String(ANO_ATUAL));
  const [documentos, setDocumentos] = useState<Documento[]>([]);
  const [visualizando, setVisualizando] = useState<{ titulo: string; conteudo: string } | null>(null);
  const [abrindoPdf, setAbrindoPdf] = useState(false);
  const [erroPdf, setErroPdf] = useState<string | null>(null);

  const carregar = useCallback(() => {
    HttpService.meusDocumentosRemuneracao(Number(mes), Number(ano))
      .then((r) => setDocumentos(r.data || []))
      .catch(() => setDocumentos([]));
  }, [mes, ano]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const docPorTipo = (tipo: Documento['tipo']) => documentos.find((d) => d.tipo === tipo);

  const abrirPdf = async (doc: Documento, titulo: string) => {
    setAbrindoPdf(true);
    setErroPdf(null);
    try {
      const response = await HttpService.meuDocumentoRemuneracaoPdf(doc.id);
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = URL.createObjectURL(blob);
      window.open(url, '_blank', 'noopener,noreferrer');
      setTimeout(() => URL.revokeObjectURL(url), 60_000);
    } catch {
      setErroPdf(`Não foi possível abrir o PDF de ${titulo}.`);
    } finally {
      setAbrindoPdf(false);
    }
  };

  const abrir = async (tipo: Documento['tipo'], titulo: string) => {
    const doc = docPorTipo(tipo);
    if (!doc) return;

    if (doc.possuiArquivoPdf) {
      await abrirPdf(doc, titulo);
      return;
    }

    try {
      const r = await HttpService.meuDocumentoRemuneracao(doc.id);
      setVisualizando({ titulo, conteudo: r.data.conteudo || 'Documento sem conteúdo.' });
    } catch {
      setVisualizando({ titulo, conteudo: 'Não foi possível carregar o documento.' });
    }
  };

  const cards = [
    { tipo: 'HOLERITE' as const, titulo: 'Holerite', descricao: 'Contracheque do mês (PDF)' },
    { tipo: 'RECIBO' as const, titulo: 'Recibo', descricao: 'Recibo de pagamento (PDF)' },
    { tipo: 'INFORME' as const, titulo: 'Informe de rendimentos', descricao: 'Documento anual (IR)' },
  ];

  return (
    <PageShell showBack={false}>
      <div className="colab-page colab-holerite">
        <div className="colab-toolbar card">
          <div className="colab-toolbar__field">
            <label htmlFor="competencia-mes">Competência</label>
            <div className="colab-competencia">
              <select id="competencia-mes" value={mes} onChange={(e) => setMes(e.target.value)}>
                {MESES.map((m) => (
                  <option key={m.v} value={m.v}>{m.l}</option>
                ))}
              </select>
              <select id="competencia-ano" value={ano} onChange={(e) => setAno(e.target.value)}>
                {[ANO_ATUAL, ANO_ATUAL - 1].map((a) => (
                  <option key={a} value={a}>{a}</option>
                ))}
              </select>
            </div>
          </div>
          <p className="field-hint colab-toolbar__hint">
            Documentos anexados pela gestão de equipe da sua instituição.
          </p>
          {erroPdf && <p className="field-hint" style={{ color: 'var(--color-danger)' }}>{erroPdf}</p>}
        </div>

        <div className="colab-doc-grid">
          {cards.map((doc) => {
            const publicado = !!docPorTipo(doc.tipo);
            const item = docPorTipo(doc.tipo);
            const labelPdf = item?.possuiArquivoPdf ? 'Abrir PDF' : 'Visualizar';
            return (
              <div key={doc.titulo} className={`colab-doc-card ${publicado ? '' : 'colab-doc-card--off'}`}>
                <div className="colab-doc-card__head">
                  <span className="colab-doc-card__badge">{doc.titulo.charAt(0)}</span>
                  <div>
                    <h3>{doc.titulo}</h3>
                    <p>{doc.descricao}</p>
                    {item?.nomeArquivo && (
                      <p className="field-hint" style={{ marginTop: '0.35rem' }}>{item.nomeArquivo}</p>
                    )}
                  </div>
                </div>
                <button
                  type="button"
                  className={publicado ? 'btn-primary' : 'btn-secondary'}
                  disabled={!publicado || abrindoPdf}
                  onClick={() => abrir(doc.tipo, doc.titulo)}
                >
                  {publicado ? (abrindoPdf ? 'Abrindo...' : labelPdf) : 'Indisponível'}
                </button>
              </div>
            );
          })}
        </div>
      </div>

      {visualizando && (
        <div className="colab-modal-backdrop" role="presentation" onClick={() => setVisualizando(null)}>
          <div className="card colab-modal colab-doc-view" role="dialog" onClick={(e) => e.stopPropagation()}>
            <h3>{visualizando.titulo}</h3>
            <pre className="colab-doc-view__content">{visualizando.conteudo}</pre>
            <button type="button" className="btn-secondary" onClick={() => setVisualizando(null)}>Fechar</button>
          </div>
        </div>
      )}
    </PageShell>
  );
};

export default MeuHolerite;
