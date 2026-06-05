import React, { useEffect, useState } from 'react';

import EnderecoFields from '../common/EnderecoFields';

import FeedbackModal from '../common/FeedbackModal';

import PageShell from '../common/PageShell';

import { INSTITUICAO } from '../../constants/branding';

import HttpService from '../../services/HttpService';

import { extractApiMessage } from '../../utils/apiError';
import { filtrarTiposPlano } from '../../utils/planoInstituicao';
import { formatDateBr } from '../../utils/format';

import { EnderecoCompleto, enderecoVazio, parseEndereco, serializarEndereco } from '../../utils/endereco';



const STATUS_FINANCEIRO = [

  { value: 'NAO_APLICAVEL', label: 'Não aplicável' },

  { value: 'PENDENTE_PAGAMENTO', label: 'Pagamento pendente' },

  { value: 'PAGAMENTO_CONFIRMADO', label: 'Pagamento confirmado' },

];



const GerenciarAcademia: React.FC = () => {

  const [cnpj, setCnpj] = useState('');

  const [telefone, setTelefone] = useState('');

  const [email, setEmail] = useState('');

  const [razaoSocial, setRazaoSocial] = useState('');

  const [cadastroAtivo, setCadastroAtivo] = useState(false);

  const [statusFinanceiro, setStatusFinanceiro] = useState('NAO_APLICAVEL');

  const [trialUtilizado, setTrialUtilizado] = useState(false);
  const [plano, setPlano] = useState('');
  const [planoVigente, setPlanoVigente] = useState(false);
  const [planoDataFim, setPlanoDataFim] = useState('');
  const [tiposPlano, setTiposPlano] = useState<Array<{ codigo: string; descricao: string }>>([]);

  const [cpfAdministrador, setCpfAdministrador] = useState('');

  const [nomeAdministrador, setNomeAdministrador] = useState('');

  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());

  const [loading, setLoading] = useState(false);

  const [isEditable, setIsEditable] = useState(false);

  const [modal, setModal] = useState({ open: false, success: false, message: '' });



  const maskCNPJ = (v: string) =>

    v.toUpperCase().replace(/[^A-Z0-9]/g, '')

      .replace(/^([A-Z0-9]{2})([A-Z0-9])/, '$1.$2')

      .replace(/^([A-Z0-9]{2})\.([A-Z0-9]{3})([A-Z0-9])/, '$1.$2.$3')

      .replace(/\.([A-Z0-9]{3})([A-Z0-9])/, '.$1/$2')

      .replace(/([A-Z0-9]{4})([A-Z0-9])/, '$1-$2')

      .slice(0, 18);



  const maskCPF = (v: string) =>

    v.replace(/\D/g, '')

      .replace(/(\d{3})(\d)/, '$1.$2')

      .replace(/(\d{3})(\d)/, '$1.$2')

      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')

      .slice(0, 14);



  const maskPhone = (v: string) =>

    v.replace(/\D/g, '').replace(/^(\d{2})(\d)/g, '($1) $2').replace(/(\d)(\d{4})$/, '$1-$2').slice(0, 15);



  const onlyNumbers = (v: string) => v.replace(/\D/g, '');



  const getErrorMessage = (err: unknown) => extractApiMessage(err, 'Erro ao processar requisição.');



  const resetForm = () => {

    setRazaoSocial('');

    setTelefone('');

    setEmail('');

    setEndereco(enderecoVazio());

    setCadastroAtivo(false);

    setStatusFinanceiro('NAO_APLICAVEL');

    setTrialUtilizado(false);
    setPlano('');
    setPlanoVigente(false);
    setPlanoDataFim('');

    setCpfAdministrador('');

    setNomeAdministrador('');

    setIsEditable(false);

  };



  const aplicarDetalhe = (data: {

    razaoSocial?: string;

    endereco?: string;

    telefone?: string;

    email?: string;

    cadastroAtivo?: boolean;

    statusFinanceiro?: string;

    trialUtilizado?: boolean;

    cpfAdministrador?: string;

    nomeAdministrador?: string;

    assinatura?: { plano?: string; dataFim?: string; vigente?: boolean };

  }) => {

    setRazaoSocial(data.razaoSocial ?? '');

    setEndereco(parseEndereco(data.endereco));

    setTelefone(data.telefone ?? '');

    setEmail(data.email ?? '');

    setCadastroAtivo(!!data.cadastroAtivo);

    setStatusFinanceiro(data.statusFinanceiro ?? 'NAO_APLICAVEL');

    setTrialUtilizado(data.trialUtilizado ?? false);

    const cpfAdmin = data.cpfAdministrador ?? '';

    setCpfAdministrador(cpfAdmin ? maskCPF(cpfAdmin) : '');

    setNomeAdministrador(data.nomeAdministrador ?? '');

    setPlano(data.assinatura?.plano || '');
    setPlanoVigente(!!data.assinatura?.vigente);
    setPlanoDataFim(data.assinatura?.dataFim || '');

    setIsEditable(true);

  };

  useEffect(() => {
    HttpService.tiposPlanoInstituicao()
      .then((r) => setTiposPlano(filtrarTiposPlano(r.data, trialUtilizado)))
      .catch(() => setTiposPlano([]));
  }, [trialUtilizado]);



  const recarregarDetalhe = async () => {

    const res = await HttpService.consultarInstituicaoDetalheCnpj(onlyNumbers(cnpj));

    aplicarDetalhe(res.data);

  };



  const handleConsultar = async () => {

    setLoading(true);

    resetForm();

    try {

      await recarregarDetalhe();

    } catch {

      setModal({ open: true, success: false, message: 'Instituição não encontrada ou erro na busca.' });

    } finally {

      setLoading(false);

    }

  };



  const handleSalvar = async () => {

    setLoading(true);

    try {

      await HttpService.editarInstituicao({

        razaoSocial,

        cnpj: onlyNumbers(cnpj),

        endereco: serializarEndereco(endereco),

        telefone: onlyNumbers(telefone),

        email,

        cadastroAtivo,

      });

      await HttpService.atualizarStatusFinanceiro({

        cnpj: onlyNumbers(cnpj),

        statusFinanceiro,

      });

      if (cadastroAtivo && plano) {
        await HttpService.atualizarPlanoInstituicao({ cnpj: onlyNumbers(cnpj), plano });
      }

      setModal({ open: true, success: true, message: 'Dados atualizados com sucesso.' });

      await recarregarDetalhe();

    } catch (err) {

      setModal({ open: true, success: false, message: getErrorMessage(err) });

    } finally {

      setLoading(false);

    }

  };



  const handleAtivarCadastro = async () => {
    if (!plano) {
      setModal({ open: true, success: false, message: 'Selecione o plano antes de ativar o cadastro.' });
      return;
    }

    setLoading(true);

    try {

      const res = await HttpService.ativarCadastroInstituicao(onlyNumbers(cnpj), plano);

      aplicarDetalhe(res.data);

      setModal({ open: true, success: true, message: 'Cadastro ativado e plano registrado. Usuários já podem entrar se o plano estiver vigente.' });

    } catch (err) {

      setModal({ open: true, success: false, message: getErrorMessage(err) });

    } finally {

      setLoading(false);

    }

  };



  const handleDesativarCadastro = async () => {

    if (!window.confirm('Desativar o cadastro? Nenhum usuário poderá entrar nesta instituição.')) return;

    setLoading(true);

    try {

      await HttpService.desativarInstituicao(onlyNumbers(cnpj));

      setCadastroAtivo(false);

      setModal({ open: true, success: true, message: 'Cadastro da instituição desativado.' });

      await recarregarDetalhe();

    } catch (err) {

      setModal({ open: true, success: false, message: getErrorMessage(err) });

    } finally {

      setLoading(false);

    }

  };



  const handleSalvarAdministrador = async () => {

    const cpf = onlyNumbers(cpfAdministrador);

    if (cpf.length < 11) {

      setModal({ open: true, success: false, message: 'Informe o CPF do administrador com 11 dígitos.' });

      return;

    }

    setLoading(true);

    try {

      const res = await HttpService.trocarAdministradorInstituicao({

        cnpj: onlyNumbers(cnpj),

        cpfAdministrador: cpf,

      });

      aplicarDetalhe(res.data);

      setModal({ open: true, success: true, message: 'Administrador atualizado na instituição.' });

    } catch (err) {

      setModal({ open: true, success: false, message: getErrorMessage(err) });

    } finally {

      setLoading(false);

    }

  };



  return (

    <PageShell title={`Consultar ${INSTITUICAO.plural}`}>

      <div className="card" style={{ marginBottom: '1rem' }}>

        <label>CNPJ</label>

        <input

          placeholder="Digite o CNPJ para buscar"

          value={cnpj}

          onChange={(e) => setCnpj(maskCNPJ(e.target.value))}

        />

        <div className="form-actions">

          <button type="button" className="btn-primary" onClick={handleConsultar} disabled={loading || cnpj.length < 14}>

            {loading ? 'Buscando...' : 'Consultar'}

          </button>

        </div>

      </div>



      <div className={`card ${!isEditable ? 'card--disabled' : ''}`}>

        <div className="form-grid">

          <div className="form-grid__span-2">

            <label>Razão social</label>

            <input disabled={!isEditable} value={razaoSocial} onChange={(e) => setRazaoSocial(e.target.value)} />

          </div>

          <div>

            <label>CNPJ</label>

            <input disabled value={cnpj} />

          </div>

          <div>

            <label>Telefone</label>

            <input

              disabled={!isEditable}

              type="tel"

              value={telefone}

              onChange={(e) => setTelefone(maskPhone(e.target.value))}

            />

          </div>

          <div>

            <label>E-mail da instituição</label>

            <input

              disabled={!isEditable}

              type="email"

              value={email}

              onChange={(e) => setEmail(e.target.value)}

              placeholder="contato@instituicao.edu.br"

            />

          </div>

          <div>

            <label>Status financeiro</label>

            <select

              disabled={!isEditable}

              value={statusFinanceiro}

              onChange={(e) => setStatusFinanceiro(e.target.value)}

            >

              {STATUS_FINANCEIRO.map((s) => (

                <option key={s.value} value={s.value}>{s.label}</option>

              ))}

            </select>

          </div>

          <div>

            <label>Plano da instituição</label>

            <select

              disabled={!isEditable}

              value={plano}

              onChange={(e) => setPlano(e.target.value)}

            >

              <option value="">Selecione o plano</option>

              {tiposPlano.map((t) => (

                  <option key={t.codigo} value={t.codigo}>{t.descricao}</option>

                ))}

            </select>

            {isEditable && !cadastroAtivo && (
              <p className="field-hint" style={{ marginTop: '0.35rem' }}>
                Obrigatório ao clicar em Ativar cadastro.
              </p>
            )}

            {isEditable && cadastroAtivo && (

              <p className="field-hint" style={{ marginTop: '0.35rem' }}>

                {planoVigente ? (

                  <>

                    Plano <strong>vigente</strong>

                    {planoDataFim ? ` até ${formatDateBr(planoDataFim)}` : ''}

                  </>

                ) : (

                  <strong style={{ color: '#b45309' }}>

                    Sem plano vigente — usuários não conseguem entrar até salvar um plano.

                  </strong>

                )}

              </p>

            )}

          </div>

        </div>

        <EnderecoFields value={endereco} onChange={setEndereco} disabled={!isEditable} />



        {isEditable && (

          <div className="card" style={{ marginTop: '1rem', padding: '1rem', background: 'var(--color-bg-soft, #f8fafc)' }}>

            <h4 style={{ marginTop: 0 }}>Administrador da instituição</h4>

            <p className="field-hint">O colaborador deve existir na plataforma (pré-cadastro).</p>

            <div className="form-grid">

              <div>

                <label>CPF do administrador</label>

                <input

                  value={cpfAdministrador}

                  onChange={(e) => setCpfAdministrador(maskCPF(e.target.value))}

                  placeholder="000.000.000-00"

                />

              </div>

              <div>

                <label>Nome</label>

                <input disabled value={nomeAdministrador} placeholder="Preenchido após salvar" />

              </div>

            </div>

            <div className="form-actions form-actions--compact">

              <button type="button" className="btn-secondary" onClick={handleSalvarAdministrador} disabled={loading}>

                Salvar administrador

              </button>

            </div>

          </div>

        )}



        <div className="form-actions form-actions--compact" style={{ marginTop: '1rem' }}>

          <span className="field-hint" style={{ marginRight: 'auto' }}>

            Cadastro ativo: <strong>{cadastroAtivo ? 'Sim' : 'Não'}</strong>

            {trialUtilizado ? ' · Teste grátis já utilizado' : ''}

          </span>

          {isEditable && (

            cadastroAtivo ? (

              <button type="button" className="btn-danger" onClick={handleDesativarCadastro} disabled={loading}>

                Desativar cadastro

              </button>

            ) : (

              <button type="button" className="btn-secondary" onClick={handleAtivarCadastro} disabled={loading || !plano}>

                Ativar cadastro

              </button>

            )

          )}

        </div>



        <div className="form-actions">

          <button type="button" className="btn-primary" onClick={handleSalvar} disabled={!isEditable || loading}>

            {loading ? 'Processando...' : 'Salvar alterações'}

          </button>

        </div>

      </div>



      <FeedbackModal

        open={modal.open}

        success={modal.success}

        message={modal.message}

        onClose={() => setModal((m) => ({ ...m, open: false }))}

      />

    </PageShell>

  );

};



export default GerenciarAcademia;

