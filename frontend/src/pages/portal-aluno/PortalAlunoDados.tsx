import { useEffect, useState } from 'react';
import { COPY_UI } from '../../constants/copy';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { parseEndereco } from '../../utils/endereco';
import { formatarTelefoneExibicao } from '../../utils/phoneFormat';

interface PortalAlunoDados {
  nome: string;
  cpf: string;
  rg: string;
  dataDeNascimento: string;
  telefone?: string;
  email?: string;
  endereco?: string;
}

const PortalAlunoDados: React.FC = () => {
  const [dados, setDados] = useState<PortalAlunoDados | null>(null);

  useEffect(() => {
    HttpService.portalAlunoDados().then((r) => setDados(r.data)).catch(() => setDados(null));
  }, []);

  if (!dados) {
    return (
      <PageShell title={COPY_UI.portalAluno.dadosTitulo} subtitle={COPY_UI.portalAluno.dadosSubtitulo}>
        <div className="card"><p>{COPY_UI.carregando}</p></div>
      </PageShell>
    );
  }

  const end = parseEndereco(dados.endereco);

  return (
    <PageShell title={COPY_UI.portalAluno.dadosTitulo} subtitle={COPY_UI.portalAluno.dadosSubtitulo}>
      <div className="card">
        <div className="form-grid">
          <div><strong>Nome</strong><p>{dados.nome}</p></div>
          <div><strong>CPF</strong><p>{dados.cpf}</p></div>
          <div><strong>RG</strong><p>{dados.rg}</p></div>
          <div><strong>Nascimento</strong><p>{dados.dataDeNascimento}</p></div>
          <div><strong>Telefone</strong><p>{formatarTelefoneExibicao(dados.telefone || '') || '—'}</p></div>
          <div><strong>E-mail</strong><p>{dados.email || '—'}</p></div>
          <div className="form-grid__span-2">
            <strong>Endereço</strong>
            <p>{[end.logradouro, end.numero, end.bairro, end.cidade, end.uf].filter(Boolean).join(', ')}</p>
          </div>
        </div>
      </div>
    </PageShell>
  );
};

export default PortalAlunoDados;
