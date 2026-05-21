import { useEffect, useState } from 'react';
import PageShell from '../../components/common/PageShell';
import HttpService from '../../services/HttpService';
import { parseEndereco } from '../../utils/endereco';

const PortalAlunoDados: React.FC = () => {
  const [dados, setDados] = useState<any>(null);

  useEffect(() => {
    HttpService.portalAlunoDados().then((r) => setDados(r.data)).catch(() => setDados(null));
  }, []);

  if (!dados) {
    return <PageShell title="Meus dados"><div className="card"><p>Carregando...</p></div></PageShell>;
  }

  const end = parseEndereco(dados.endereco);

  return (
    <PageShell title="Meus dados pessoais">
      <div className="card">
        <div className="form-grid">
          <div><strong>Nome</strong><p>{dados.nome}</p></div>
          <div><strong>CPF</strong><p>{dados.cpf}</p></div>
          <div><strong>RG</strong><p>{dados.rg}</p></div>
          <div><strong>Nascimento</strong><p>{dados.dataDeNascimento}</p></div>
          <div><strong>Telefone</strong><p>{dados.telefone}</p></div>
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
