import { useState } from 'react';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, serializarEndereco } from '../../utils/endereco';

const MatriculaAluno: React.FC = () => {
  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setDataDeNascimento] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [valorMensalidade, setValorMensalidade] = useState('');
  const [diaVencimentoMensalidade, setDiaVencimentoMensalidade] = useState('10');
  const [nomeResponsavel, setNomeResponsavel] = useState('');
  const [telefoneResponsavel, setTelefoneResponsavel] = useState('');
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await HttpService.matricularAluno({
        nome,
        cpf: cpf.replace(/\D/g, ''),
        rg,
        dataDeNascimento,
        endereco: serializarEndereco(endereco),
        telefone: telefone.replace(/\D/g, ''),
        valorMensalidade: parseFloat(valorMensalidade),
        diaVencimentoMensalidade: parseInt(diaVencimentoMensalidade, 10),
        nomeResponsavel,
        telefoneResponsavel: telefoneResponsavel.replace(/\D/g, ''),
      });
      setModal({ open: true, success: true, message: 'Aluno matriculado com sucesso.' });
    } catch (err) {
      setModal({ open: true, success: false, message: extractApiMessage(err, 'Erro ao matricular.') });
    }
  };

  return (
    <PageShell title="Matrícula de aluno">
      <form className="card" onSubmit={submit}>
        <div className="form-grid">
          <div><label>Nome</label><input value={nome} onChange={(e) => setNome(e.target.value)} required /></div>
          <div><label>CPF</label><input value={cpf} onChange={(e) => setCpf(e.target.value)} required /></div>
          <div><label>RG</label><input value={rg} onChange={(e) => setRg(e.target.value)} required /></div>
          <div><label>Nascimento</label><input type="date" value={dataDeNascimento} onChange={(e) => setDataDeNascimento(e.target.value)} required /></div>
          <div><label>Telefone</label><input value={telefone} onChange={(e) => setTelefone(e.target.value)} required /></div>
          <div><label>Mensalidade (R$)</label><input type="number" step="0.01" value={valorMensalidade} onChange={(e) => setValorMensalidade(e.target.value)} required /></div>
          <div><label>Dia vencimento</label><input type="number" min={1} max={28} value={diaVencimentoMensalidade} onChange={(e) => setDiaVencimentoMensalidade(e.target.value)} /></div>
          <div><label>Responsável</label><input value={nomeResponsavel} onChange={(e) => setNomeResponsavel(e.target.value)} /></div>
          <div><label>Tel. responsável</label><input value={telefoneResponsavel} onChange={(e) => setTelefoneResponsavel(e.target.value)} /></div>
        </div>
        <EnderecoFields value={endereco} onChange={setEndereco} />
        <div className="form-actions">
          <button type="submit" className="btn-primary">Matricular</button>
        </div>
      </form>

      <FeedbackModal
        open={modal.open}
        success={modal.success}
        message={modal.message}
        onClose={() => setModal((m) => ({ ...m, open: false }))}
      />
    </PageShell>
  );
};

export default MatriculaAluno;
