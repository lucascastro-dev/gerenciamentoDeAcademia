import { useState } from 'react';

import { Link } from 'react-router-dom';

import EnderecoFields from '../common/EnderecoFields';

import FeedbackModal from '../common/FeedbackModal';

import PageShell from '../common/PageShell';

import CurrencyInput from '../common/CurrencyInput';

import { carregarSessao, isModoPlataforma, isProfessor } from '../../auth/permissoes';

import HttpService from '../../services/HttpService';

import { extractApiMessage } from '../../utils/apiError';

import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';

import { formatarMoeda, parseMoeda } from '../../utils/moeda';



interface MatriculaInstituicao {

  instituicaoId: number;

  razaoSocial: string;

  valorMensalidade?: number;

  diaVencimentoMensalidade?: number;

  turmas: Array<{ id: number; modalidade: string; horario: string; sala?: string }>;

}



interface FinanceiroInstituicao {

  valor: string;

  dia: string;

}



const GerenciarAlunos: React.FC = () => {

  const sessao = carregarSessao();
  const master = isModoPlataforma(sessao);
  const modoProfessor = isProfessor(sessao);

  const [cpfBusca, setCpfBusca] = useState('');

  const [nome, setNome] = useState('');

  const [rg, setRg] = useState('');

  const [dataDeNascimento, setDataDeNascimento] = useState('');

  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());

  const [telefone, setTelefone] = useState('');

  const [email, setEmail] = useState('');

  const [nomeResponsavel, setNomeResponsavel] = useState('');

  const [telefoneResponsavel, setTelefoneResponsavel] = useState('');

  const [matriculas, setMatriculas] = useState<MatriculaInstituicao[]>([]);
  const [turmasProfessor, setTurmasProfessor] = useState<Array<{ id: number; modalidade: string; horario: string; sala?: string }>>([]);

  const [financeiroPorInstituicao, setFinanceiroPorInstituicao] = useState<Record<number, FinanceiroInstituicao>>({});

  const [alunoCarregado, setAlunoCarregado] = useState(false);

  const [modal, setModal] = useState({ open: false, success: false, message: '' });

  const onlyNumbers = (v: string) => v.replace(/\D/g, '');



  const maskCPF = (v: string) =>

    v.replace(/\D/g, '')

      .replace(/(\d{3})(\d)/, '$1.$2')

      .replace(/(\d{3})(\d)/, '$1.$2')

      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')

      .slice(0, 14);



  const limparForm = () => {

    setCpfBusca('');

    setNome('');

    setRg('');

    setDataDeNascimento('');

    setEndereco(enderecoVazio());

    setTelefone('');

    setEmail('');

    setNomeResponsavel('');

    setTelefoneResponsavel('');

    setMatriculas([]);
    setTurmasProfessor([]);
    setFinanceiroPorInstituicao({});

    setAlunoCarregado(false);

  };



  const payloadPessoal = () => ({

    nome,

    cpf: onlyNumbers(cpfBusca),

    rg,

    dataDeNascimento,

    endereco: serializarEndereco(endereco),

    telefone: onlyNumbers(telefone),

    email,

    nomeResponsavel,

    telefoneResponsavel: onlyNumbers(telefoneResponsavel),

  });



  const aplicarFinanceiroDasMatriculas = (lista: MatriculaInstituicao[]) => {

    const mapa: Record<number, FinanceiroInstituicao> = {};

    lista.forEach((m) => {

      mapa[m.instituicaoId] = {

        valor: m.valorMensalidade != null ? formatarMoeda(m.valorMensalidade) : '',

        dia: String(m.diaVencimentoMensalidade ?? '10'),

      };

    });

    setFinanceiroPorInstituicao(mapa);

  };



  const consultar = async (cpfInformado?: string) => {

    const cpf = onlyNumbers(cpfInformado ?? cpfBusca);

    if (cpf.length < 11) {

      setModal({ open: true, success: false, message: 'Informe um CPF válido com 11 dígitos.' });

      return;

    }

    setCpfBusca(maskCPF(cpf));

    try {
      if (modoProfessor) {
        const r = await HttpService.consultarAlunoProfessor(cpf);
        const d = r.data;
        setNome(d.nome);
        setRg(d.rgMascarado);
        setDataDeNascimento(d.dataDeNascimento);
        setEndereco(parseEndereco(d.enderecoResumido || ''));
        setTelefone(d.telefoneMascarado);
        setEmail(d.emailMascarado ?? '');
        setNomeResponsavel(d.nomeResponsavel ?? '');
        setTelefoneResponsavel(d.telefoneResponsavelMascarado ?? '');
        setMatriculas([]);
        setTurmasProfessor(d.turmasInstituicao || []);
        setFinanceiroPorInstituicao({});
        setAlunoCarregado(true);
        return;
      }

      const r = await HttpService.consultarAlunoPorCpf(cpf);
      const d = r.data;
      setNome(d.nome);
      setRg(d.rg);
      setDataDeNascimento(d.dataDeNascimento);
      setEndereco(parseEndereco(d.endereco));
      setTelefone(d.telefone);
      setEmail(d.email ?? '');
      setNomeResponsavel(d.nomeResponsavel ?? '');
      setTelefoneResponsavel(d.telefoneResponsavel ?? '');
      const mats = d.matriculas || [];
      setMatriculas(mats);
      aplicarFinanceiroDasMatriculas(mats);
      setAlunoCarregado(true);
    } catch (e) {

      setAlunoCarregado(false);

      setMatriculas([]);
      setTurmasProfessor([]);
      setFinanceiroPorInstituicao({});

      setModal({ open: true, success: false, message: extractApiMessage(e, 'Aluno não encontrado.') });

    }

  };



  const atualizarFinanceiroInstituicao = (instituicaoId: number, campo: 'valor' | 'dia', valor: string) => {

    setFinanceiroPorInstituicao((prev) => ({

      ...prev,

      [instituicaoId]: {

        valor: campo === 'valor' ? valor : (prev[instituicaoId]?.valor ?? ''),

        dia: campo === 'dia' ? valor : (prev[instituicaoId]?.dia ?? '10'),

      },

    }));

  };



  const salvarAlteracoes = async () => {

    if (!alunoCarregado) {

      setModal({ open: true, success: false, message: 'Consulte um aluno pelo CPF antes de salvar.' });

      return;

    }

    try {

      await HttpService.alterarAluno(payloadPessoal());

      for (const m of matriculas) {

        const fin = financeiroPorInstituicao[m.instituicaoId];

        if (!fin) continue;

        await HttpService.alterarAluno({

          cpf: onlyNumbers(cpfBusca),

          instituicaoId: m.instituicaoId,

          valorMensalidade: parseMoeda(fin.valor),

          diaVencimentoMensalidade: parseInt(fin.dia, 10),

        });

      }

      setModal({ open: true, success: true, message: 'Dados do aluno atualizados com sucesso.' });

      await consultar(cpfBusca);

    } catch (e) {

      setModal({ open: true, success: false, message: extractApiMessage(e) });

    }

  };



  const desmatricular = async () => {

    if (!window.confirm('Desmatricular remove o cadastro do aluno em todo o sistema. Continuar?')) return;

    try {

      await HttpService.desmatricularAluno(onlyNumbers(cpfBusca));

      setModal({ open: true, success: true, message: 'Aluno desmatriculado.' });

      limparForm();

    } catch (e) {

      setModal({ open: true, success: false, message: extractApiMessage(e) });

    }

  };



  return (

    <PageShell

      title="Consultar alunos"

      subtitle={modoProfessor
        ? 'Consulta somente leitura de alunos matriculados na instituição'
        : master
        ? 'Busque apenas pelo CPF para ver dados pessoais e matrículas em todas as instituições'
        : 'Busque pelo CPF para ver dados e matrícula na sua instituição'}

    >

      <div className="card" style={{ marginBottom: '1rem' }}>

        <div className="form-grid">

          <div>

            <label>CPF</label>

            <input

              value={cpfBusca}

              onChange={(e) => setCpfBusca(maskCPF(e.target.value))}

              placeholder="000.000.000-00"

            />

          </div>

        </div>

        <div className="form-actions">

          <button type="button" className="btn-primary" onClick={() => consultar()}>Consultar</button>

          <button type="button" className="btn-secondary" onClick={limparForm}>Limpar</button>

          {!modoProfessor && (
            <Link to="/arealogada/matricula" className="btn-secondary" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center' }}>
              Matricular novo aluno
            </Link>
          )}

        </div>

      </div>



      {alunoCarregado && (

        <>

          {modoProfessor && turmasProfessor.length > 0 && (
            <div className="card" style={{ marginBottom: '1rem' }}>
              <h3 style={{ marginTop: 0 }}>Turmas na instituição</h3>
              <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
                {turmasProfessor.map((t) => (
                  <li key={t.id}>
                    {t.modalidade} — {t.horario}{t.sala ? ` · ${t.sala}` : ''}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {!modoProfessor && (
          <div className="card" style={{ marginBottom: '1rem' }}>

            <h3 style={{ marginTop: 0 }}>Matrículas e mensalidade por instituição</h3>

            {matriculas.length === 0 ? (

              <p className="field-hint">Nenhuma turma vinculada em instituições gerenciadas.</p>

            ) : (

              matriculas.map((m) => {

                const fin = financeiroPorInstituicao[m.instituicaoId] ?? { valor: '', dia: '10' };

                return (

                  <div key={m.instituicaoId} className="turmas-item" style={{ marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--color-border, #e2e8f0)' }}>

                    <strong>{m.razaoSocial}</strong>

                    <div className="form-grid" style={{ marginTop: '0.75rem' }}>

                      <div>

                        <label>Mensalidade</label>

                        <CurrencyInput

                          value={fin.valor}

                          onChange={(_n, formatado) => atualizarFinanceiroInstituicao(m.instituicaoId, 'valor', formatado)}

                        />

                      </div>

                      <div>

                        <label>Dia venc.</label>

                        <input

                          type="number"

                          min={1}

                          max={28}

                          value={fin.dia}

                          onChange={(e) => atualizarFinanceiroInstituicao(m.instituicaoId, 'dia', e.target.value)}

                        />

                      </div>

                    </div>

                    {m.turmas.length === 0 ? (

                      <p className="field-hint" style={{ margin: '0.35rem 0 0' }}>Sem turmas ativas nesta instituição.</p>

                    ) : (

                      <ul style={{ margin: '0.5rem 0 0', paddingLeft: '1.2rem' }}>

                        {m.turmas.map((t) => (

                          <li key={t.id}>

                            {t.modalidade} — {t.horario}

                            {t.sala ? ` · ${t.sala}` : ''}

                          </li>

                        ))}

                      </ul>

                    )}

                  </div>

                );

              })

            )}

          </div>
          )}

          <div className="card" style={{ marginBottom: '1rem' }}>

            <h3 style={{ marginTop: 0 }}>Dados do aluno</h3>

            <div className="form-grid">

              <div><label>Nome</label><input value={nome} readOnly={modoProfessor} onChange={(e) => setNome(e.target.value)} /></div>

              <div><label>RG</label><input value={rg} readOnly={modoProfessor} onChange={(e) => setRg(e.target.value)} /></div>

              <div><label>Nascimento</label><input type="date" value={dataDeNascimento} readOnly={modoProfessor} onChange={(e) => setDataDeNascimento(e.target.value)} /></div>

              <div><label>Telefone</label><input value={telefone} readOnly={modoProfessor} onChange={(e) => setTelefone(e.target.value)} /></div>

              <div>

                <label>E-mail</label>

                <input type="email" value={email} readOnly={modoProfessor} onChange={(e) => setEmail(e.target.value)} placeholder="nome@email.com" />

              </div>

              <div><label>Responsável</label><input value={nomeResponsavel} readOnly={modoProfessor} onChange={(e) => setNomeResponsavel(e.target.value)} /></div>

              <div><label>Tel. responsável</label><input value={telefoneResponsavel} readOnly={modoProfessor} onChange={(e) => setTelefoneResponsavel(e.target.value)} /></div>

            </div>

            <EnderecoFields value={endereco} onChange={setEndereco} disabled={modoProfessor} />

            {!modoProfessor && (
            <div className="form-actions">

              <button type="button" className="btn-primary" onClick={salvarAlteracoes}>Salvar alterações</button>

              <button type="button" className="btn-danger" onClick={desmatricular}>Desmatricular</button>

            </div>
            )}

          </div>

        </>

      )}



      <FeedbackModal

        open={modal.open}

        success={modal.success}

        message={modal.message}

        onClose={() => setModal((m) => ({ ...m, open: false }))}

      />

    </PageShell>

  );

};



export default GerenciarAlunos;
