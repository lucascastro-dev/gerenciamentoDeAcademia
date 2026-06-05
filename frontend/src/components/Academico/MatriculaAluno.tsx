import { useState } from 'react';
import EnderecoFields from '../common/EnderecoFields';
import FeedbackModal from '../common/FeedbackModal';
import PageShell from '../common/PageShell';
import { carregarSessao, isModoPlataforma } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import { EnderecoCompleto, enderecoVazio, serializarEndereco } from '../../utils/endereco';
import { idInstituicao } from '../../utils/instituicao';
import CurrencyInput from '../common/CurrencyInput';
import { parseMoeda } from '../../utils/moeda';

function senhaInicialDoCpf(cpf: string): string {
  const digitos = cpf.replace(/\D/g, '');
  return digitos.length >= 6 ? digitos.substring(0, 6) : digitos;
}

const MatriculaAluno: React.FC = () => {
  const sessao = carregarSessao();
  const master = isModoPlataforma(sessao);
  const [nome, setNome] = useState('');
  const [cpf, setCpf] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setDataDeNascimento] = useState('');
  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());
  const [telefone, setTelefone] = useState('');
  const [email, setEmail] = useState('');
  const [valorMensalidade, setValorMensalidade] = useState('');
  const [diaVencimentoMensalidade, setDiaVencimentoMensalidade] = useState('10');
  const [nomeResponsavel, setNomeResponsavel] = useState('');
  const [telefoneResponsavel, setTelefoneResponsavel] = useState('');
  const [cnpjInstituicao, setCnpjInstituicao] = useState('');
  const [instituicaoNome, setInstituicaoNome] = useState('');
  const [instituicaoId, setInstituicaoId] = useState<number | null>(null);
  const [buscandoInst, setBuscandoInst] = useState(false);
  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const onlyNumbers = (v: string) => v.replace(/\D/g, '');

  const maskCNPJ = (v: string) =>
    v.toUpperCase().replace(/[^A-Z0-9]/g, '')
      .replace(/^([A-Z0-9]{2})([A-Z0-9])/, '$1.$2')
      .replace(/^([A-Z0-9]{2})\.([A-Z0-9]{3})([A-Z0-9])/, '$1.$2.$3')
      .replace(/\.([A-Z0-9]{3})([A-Z0-9])/, '.$1/$2')
      .replace(/([A-Z0-9]{4})([A-Z0-9])/, '$1-$2')
      .slice(0, 18);

  const buscarInstituicao = async () => {
    const cnpjLimpo = onlyNumbers(cnpjInstituicao);
    if (cnpjLimpo.length < 14) {
      setModal({ open: true, success: false, message: 'Informe um CNPJ válido (14 caracteres).' });
      return;
    }
    setBuscandoInst(true);
    setInstituicaoNome('');
    setInstituicaoId(null);
    try {
      const { data } = await HttpService.consultarInstituicaoDetalheCnpj(cnpjLimpo);
      const id = idInstituicao(data);
      if (!id) {
        setModal({ open: true, success: false, message: 'Instituição não identificada na resposta.' });
        return;
      }
      if (!data.cadastroAtivo) {
        setModal({
          open: true,
          success: false,
          message: 'Instituição encontrada, mas o cadastro não está ativo. Ative em Ativar / desativar instituição.',
        });
        return;
      }
      setInstituicaoId(id);
      setInstituicaoNome(data.razaoSocial || 'Instituição');
    } catch (e) {
      setModal({
        open: true,
        success: false,
        message: extractApiMessage(e, 'Instituição não encontrada para este CNPJ.'),
      });
    } finally {
      setBuscandoInst(false);
    }
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    const cpfLimpo = cpf.replace(/\D/g, '');
    let idDestino: number;
    if (master) {
      if (!instituicaoId) {
        setModal({ open: true, success: false, message: 'Busque e valide a instituição pelo CNPJ antes de matricular.' });
        return;
      }
      idDestino = instituicaoId;
    } else {
      idDestino = Number(sessao?.vinculo);
      if (!idDestino) {
        setModal({ open: true, success: false, message: 'Instituição não identificada na sessão. Faça login novamente.' });
        return;
      }
    }
    try {
      await HttpService.matricularAluno({
        nome,
        cpf: cpfLimpo,
        rg,
        dataDeNascimento,
        endereco: serializarEndereco(endereco),
        telefone: telefone.replace(/\D/g, ''),
        email,
        valorMensalidade: parseMoeda(valorMensalidade),
        diaVencimentoMensalidade: parseInt(diaVencimentoMensalidade, 10),
        nomeResponsavel,
        telefoneResponsavel: telefoneResponsavel.replace(/\D/g, ''),
        instituicaoId: idDestino,
      });
      const senha = senhaInicialDoCpf(cpfLimpo);
      const destino = master ? instituicaoNome : `instituição (ID ${idDestino})`;
      setModal({
        open: true,
        success: true,
        message: `Aluno matriculado e vinculado à ${destino}. Senha inicial do portal: ${senha} (6 primeiros dígitos do CPF). Oriente a alterar em Minha conta após o primeiro acesso.`,
      });
    } catch (err) {
      setModal({ open: true, success: false, message: extractApiMessage(err, 'Erro ao matricular.') });
    }
  };

  return (
    <PageShell title="Matrícula de aluno" subtitle="O aluno já fica vinculado à instituição e pode entrar no portal do aluno">
      <form className="card" onSubmit={submit}>
        {master ? (
          <div className="form-grid" style={{ marginBottom: '1rem' }}>
            <div className="form-grid__span-2">
              <label>CNPJ da instituição</label>
              <input
                value={cnpjInstituicao}
                onChange={(e) => {
                  setCnpjInstituicao(maskCNPJ(e.target.value));
                  setInstituicaoId(null);
                  setInstituicaoNome('');
                }}
                placeholder="00.000.000/0000-00"
              />
            </div>
            <div className="form-actions form-actions--compact" style={{ alignSelf: 'end' }}>
              <button
                type="button"
                className="btn-secondary"
                onClick={buscarInstituicao}
                disabled={buscandoInst || onlyNumbers(cnpjInstituicao).length < 14}
              >
                {buscandoInst ? 'Buscando...' : 'Buscar instituição'}
              </button>
            </div>
            {instituicaoNome && (
              <p className="field-hint form-grid__span-2" style={{ margin: 0 }}>
                Instituição válida: <strong>{instituicaoNome}</strong> (ID {instituicaoId})
              </p>
            )}
          </div>
        ) : (
          <p className="field-hint" style={{ marginTop: 0 }}>
            Instituição atual: vínculo da sua sessão (ID {sessao?.vinculo || '—'}).
          </p>
        )}
        <div className="form-grid">
          <div><label>Nome</label><input value={nome} onChange={(e) => setNome(e.target.value)} required /></div>
          <div><label>CPF</label><input value={cpf} onChange={(e) => setCpf(e.target.value)} required /></div>
          <div><label>RG</label><input value={rg} onChange={(e) => setRg(e.target.value)} required /></div>
          <div><label>Nascimento</label><input type="date" value={dataDeNascimento} onChange={(e) => setDataDeNascimento(e.target.value)} required /></div>
          <div><label>Telefone</label><input value={telefone} onChange={(e) => setTelefone(e.target.value)} required /></div>
          <div>
            <label>E-mail</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nome@email.com" />
          </div>
          <div>
            <label>Mensalidade</label>
            <CurrencyInput
              value={valorMensalidade}
              onChange={(_n, formatado) => setValorMensalidade(formatado)}
              required
            />
          </div>
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
