import { useEffect, useState } from 'react';

import FeedbackModal from '../common/FeedbackModal';

import PageShell from '../common/PageShell';

import { INSTITUICAO } from '../../constants/branding';

import HttpService from '../../services/HttpService';

import { extractApiMessage } from '../../utils/apiError';
import { descricaoPlano, filtrarTiposPlano } from '../../utils/planoInstituicao';
import { formatDateBr } from '../../utils/format';



interface InstituicaoDetalhe {

  razaoSocial?: string;

  cnpj?: string;

  cadastroAtivo?: boolean;

  trialUtilizado?: boolean;

  statusFinanceiro?: string;

  assinatura?: {

    plano?: string;

    dataInicio?: string;

    dataFim?: string;

    vigente?: boolean;

    ativo?: boolean;

  };

}



const GestaoAcademias: React.FC = () => {

  const [cnpj, setCnpj] = useState('');

  const [cpfAdmin, setCpfAdmin] = useState('');

  const [plano, setPlano] = useState('');

  const [tiposPlano, setTiposPlano] = useState<Array<{ codigo: string; descricao: string }>>([]);

  const [detalhe, setDetalhe] = useState<InstituicaoDetalhe | null>(null);

  const [loading, setLoading] = useState(false);

  const [modal, setModal] = useState({ open: false, success: false, message: '' });



  const onlyNumbers = (v: string) => v.replace(/\D/g, '');



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



  const aplicarDetalhe = (data: InstituicaoDetalhe) => {

    setDetalhe(data);

    setPlano(data.assinatura?.plano || '');

  };



  useEffect(() => {
    HttpService.tiposPlanoInstituicao()
      .then((r) => setTiposPlano(filtrarTiposPlano(r.data, detalhe?.trialUtilizado)))
      .catch(() => setTiposPlano([]));
  }, [detalhe?.trialUtilizado]);



  const buscarInstituicao = async () => {

    const cnpjLimpo = onlyNumbers(cnpj);

    if (cnpjLimpo.length < 14) return;

    setLoading(true);

    setDetalhe(null);

    setPlano('');

    try {

      const { data } = await HttpService.consultarInstituicaoDetalheCnpj(cnpjLimpo);

      aplicarDetalhe(data);

    } catch (e) {

      setModal({

        open: true,

        success: false,

        message: extractApiMessage(e, 'Instituição não cadastrada. Cadastre em Nova instituição.'),

      });

    } finally {

      setLoading(false);

    }

  };



  const ativar = async () => {

    try {

      const { data } = await HttpService.ativarInstituicao({

        cnpj: onlyNumbers(cnpj),

        cpfAdministrador: onlyNumbers(cpfAdmin),

        plano,

      });

      aplicarDetalhe(data);

      setModal({

        open: true,

        success: true,

        message: `${INSTITUICAO.capitalized} ativada. Administrador vinculado e plano registrado.`,

      });

      setCpfAdmin('');

    } catch (e) {

      setModal({ open: true, success: false, message: extractApiMessage(e) });

    }

  };



  const salvarPlano = async () => {

    if (!plano) {

      setModal({ open: true, success: false, message: 'Selecione um plano.' });

      return;

    }

    setLoading(true);

    try {

      const { data } = await HttpService.atualizarPlanoInstituicao({ cnpj: onlyNumbers(cnpj), plano });

      aplicarDetalhe(data);

      setModal({

        open: true,

        success: true,

        message: 'Plano atualizado. Colaboradores já podem entrar se o plano estiver vigente.',

      });

    } catch (e) {

      setModal({ open: true, success: false, message: extractApiMessage(e) });

    } finally {

      setLoading(false);

    }

  };



  const desativar = async () => {

    try {

      await HttpService.desativarInstituicao(onlyNumbers(cnpj));

      setModal({ open: true, success: true, message: `${INSTITUICAO.capitalized} desativada.` });

      setDetalhe(null);

      setPlano('');

    } catch (e) {

      setModal({ open: true, success: false, message: extractApiMessage(e, 'Erro ao desativar.') });

    }

  };



  const trialJaUsado = !!detalhe?.trialUtilizado;

  const podeAtivar = detalhe && !detalhe.cadastroAtivo && onlyNumbers(cpfAdmin).length === 11 && plano;

  const planoVigente = !!detalhe?.assinatura?.vigente;



  const blocoPlano = detalhe?.cadastroAtivo ? (

    <div className="card" style={{ marginTop: '1rem', padding: '1rem', background: 'var(--color-bg-soft, #f8fafc)' }}>

      <h4 style={{ marginTop: 0 }}>Plano da instituição</h4>

      <div className="form-grid">

        <div>

          <label>Plano</label>

          <select value={plano} onChange={(e) => setPlano(e.target.value)}>

            <option value="">Selecione o plano</option>

            {filtrarTiposPlano(tiposPlano, trialJaUsado).map((t) => (

                <option key={t.codigo} value={t.codigo}>{t.descricao}</option>

              ))}

          </select>

        </div>

        <div>

          <label>Situação do plano</label>

          <p className="field-hint" style={{ margin: '0.35rem 0 0' }}>

            {planoVigente ? (

              <>

                <strong style={{ color: 'var(--color-success, #059669)' }}>Vigente</strong>

                {detalhe.assinatura?.dataFim ? ` até ${formatDateBr(detalhe.assinatura.dataFim)}` : ''}

                {plano ? ` · ${descricaoPlano(plano, tiposPlano)}` : ''}

              </>

            ) : (

              <strong style={{ color: '#b45309' }}>

                Sem plano vigente — colaboradores não conseguem fazer login até renovar o plano.

              </strong>

            )}

          </p>

        </div>

      </div>

      <div className="form-actions form-actions--compact" style={{ marginTop: '0.75rem' }}>

        <button type="button" className="btn-primary" onClick={salvarPlano} disabled={loading || !plano}>

          {loading ? 'Salvando...' : 'Salvar plano'}

        </button>

      </div>

    </div>

  ) : null;



  return (

    <PageShell

      title={`Ativar / desativar ${INSTITUICAO.singular}`}

      subtitle="Busque pelo CNPJ, vincule o administrador e defina o plano SaaS da instituição."

    >

      <div className="card" style={{ marginBottom: '1rem' }}>

        <label>CNPJ da instituição</label>

        <input value={cnpj} onChange={(e) => setCnpj(maskCNPJ(e.target.value))} placeholder="00.000.000/0000-00" />

        <div className="form-actions">

          <button type="button" className="btn-primary" onClick={buscarInstituicao} disabled={loading || onlyNumbers(cnpj).length < 14}>

            {loading ? 'Buscando...' : 'Buscar instituição'}

          </button>

        </div>

      </div>



      {detalhe && (

        <div className="card" style={{ marginBottom: '1rem' }}>

          <h3 style={{ marginTop: 0 }}>Dados da instituição</h3>

          <p><strong>Razão social:</strong> {detalhe.razaoSocial}</p>

          <p><strong>Status cadastro:</strong> {detalhe.cadastroAtivo ? 'Ativa' : 'Inativa (aguardando ativação)'}</p>

          {detalhe.statusFinanceiro && (

            <p><strong>Status financeiro:</strong> {detalhe.statusFinanceiro.replace(/_/g, ' ')}</p>

          )}

          {trialJaUsado && (

            <p className="field-hint">Teste grátis de 7 dias já utilizado nesta instituição.</p>

          )}



          {blocoPlano}



          {!detalhe.cadastroAtivo && (

            <>

              <div className="form-grid" style={{ marginTop: '1rem' }}>

                <div>

                  <label>CPF do administrador</label>

                  <input

                    value={cpfAdmin}

                    onChange={(e) => setCpfAdmin(maskCPF(e.target.value))}

                    placeholder="000.000.000-00"

                  />

                  <p className="field-hint">Deve existir pré-cadastro na plataforma.</p>

                </div>

                <div>

                  <label>Plano (ativação)</label>

                  <select value={plano} onChange={(e) => setPlano(e.target.value)}>

                    <option value="">Selecione</option>

                    {filtrarTiposPlano(tiposPlano, trialJaUsado).map((t) => (

                        <option key={t.codigo} value={t.codigo}>{t.descricao}</option>

                      ))}

                  </select>

                </div>

              </div>

              <div className="form-actions">

                <button type="button" className="btn-primary" onClick={ativar} disabled={!podeAtivar}>

                  Ativar instituição

                </button>

              </div>

            </>

          )}

        </div>

      )}



      {detalhe?.cadastroAtivo && (

        <div className="card">

          <div className="form-actions">

            <button type="button" className="btn-danger" onClick={desativar}>

              Desativar {INSTITUICAO.singular}

            </button>

          </div>

        </div>

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



export default GestaoAcademias;

