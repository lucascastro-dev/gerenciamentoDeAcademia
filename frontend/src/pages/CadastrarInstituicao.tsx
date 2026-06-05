import { useState } from 'react';

import EnderecoFields from '../components/common/EnderecoFields';

import FeedbackModal from '../components/common/FeedbackModal';

import PageShell from '../components/common/PageShell';

import { INSTITUICAO } from '../constants/branding';

import HttpService from '../services/HttpService';

import { extractApiMessage } from '../utils/apiError';

import { EnderecoCompleto, enderecoVazio, serializarEndereco } from '../utils/endereco';



const CadastrarInstituicao: React.FC = () => {

  const [razaoSocial, setRazaoSocial] = useState('');

  const [cnpj, setCnpj] = useState('');

  const [endereco, setEndereco] = useState<EnderecoCompleto>(enderecoVazio());

  const [telefone, setTelefone] = useState('');

  const [email, setEmail] = useState('');

  const [modal, setModal] = useState({ open: false, success: false, message: '' });



  const maskCNPJ = (v: string) =>

    v.toUpperCase().replace(/[^A-Z0-9]/g, '')

      .replace(/^([A-Z0-9]{2})([A-Z0-9])/, '$1.$2')

      .replace(/^([A-Z0-9]{2})\.([A-Z0-9]{3})([A-Z0-9])/, '$1.$2.$3')

      .replace(/\.([A-Z0-9]{3})([A-Z0-9])/, '.$1/$2')

      .replace(/([A-Z0-9]{4})([A-Z0-9])/, '$1-$2')

      .slice(0, 18);



  const onlyNumbers = (v: string) => v.replace(/\D/g, '');



  const cadastrar = async () => {

    try {

      await HttpService.cadastrarInstituicao({

        razaoSocial,

        cnpj: onlyNumbers(cnpj),

        endereco: serializarEndereco(endereco),

        telefone: onlyNumbers(telefone),

        email,

        cadastroAtivo: false,

      });

      setModal({

        open: true,

        success: true,

        message: `${INSTITUICAO.capitalized} cadastrada. Ative o cadastro e defina o plano em Consultar instituições quando estiver pronta.`,

      });

      setRazaoSocial('');

      setCnpj('');

      setEndereco(enderecoVazio());

      setTelefone('');

      setEmail('');

    } catch (e) {

      setModal({

        open: true,

        success: false,

        message: extractApiMessage(

          e,

          'Erro ao cadastrar. Apenas o administrador da plataforma pode registrar novas instituições.',

        ),

      });

    }

  };



  return (

    <PageShell title={`Nova ${INSTITUICAO.singular}`} subtitle={INSTITUICAO.masterOnlyHint}>

      <div className="card">

        <div className="form-grid">

          <div className="form-grid__span-2"><label>Razão social</label><input value={razaoSocial} onChange={(e) => setRazaoSocial(e.target.value)} /></div>

          <div><label>CNPJ</label><input value={cnpj} onChange={(e) => setCnpj(maskCNPJ(e.target.value))} /></div>

          <div><label>Telefone</label><input value={telefone} onChange={(e) => setTelefone(e.target.value)} /></div>

          <div>

            <label>E-mail</label>

            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="contato@instituicao.edu.br" />

          </div>

        </div>

        <EnderecoFields value={endereco} onChange={setEndereco} />

        <div className="form-actions">

          <button type="button" className="btn-primary" onClick={cadastrar}>

            Cadastrar {INSTITUICAO.singular}

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



export default CadastrarInstituicao;
