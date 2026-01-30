import { AxiosError } from 'axios';
import React, { useState } from 'react';
import HttpService from '../../services/HttpService';
import "./AreaLogada.css";

const GerenciarFuncionario: React.FC = () => {
  const [cpf, setCpf] = useState('');
  const [endereco, setEndereco] = useState('');
  const [telefone, setTelefone] = useState('');
  const [nome, setNome] = useState('');
  const [rg, setRg] = useState('');
  const [dataDeNascimento, setNascimento] = useState('');
  const [cargo, setCargo] = useState('');
  const [especializacao, setEspecializacao] = useState('');
  const [permitirGerenciarFuncoes, setGerenciarFuncoes] = useState(false);
  const [cadastroAtivo, setCadastroAtivo] = useState(false);

  const [loading, setLoading] = useState(false);
  const [isEditable, setIsEditable] = useState(false); // Controla o bloqueio dos campos
  const [modal, setModal] = useState<{ show: boolean, message: string, isSuccess: boolean }>({
    show: false,
    message: '',
    isSuccess: false
  });

  const maskCPF = (v: string) => v.replace(/\D/g, "").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2").slice(0, 14);
  const maskPhone = (v: string) => v.replace(/\D/g, "").replace(/^(\d{2})(\d)/g, "($1) $2").replace(/(\d)(\d{4})$/, "$1-$2").slice(0, 15);
  const onlyNumbers = (v: string) => v.replace(/\D/g, "");

  const getErrorMessage = (err: any) => {
    const axiosError = err as AxiosError;
    if (axiosError.response?.data) {
      const data = axiosError.response.data as any;
      return data.message || data.error || "Erro ao processar requisição.";
    }
    return "Erro ao conectar ao servidor.";
  };

  const handleConsultarPessoa = async () => {
    const token = localStorage.getItem('@App:token');
    if (!token) {
      setModal({ show: true, message: "Sessão expirada. Faça login novamente.", isSuccess: false });
      return;
    }

    setLoading(true);
    setIsEditable(false);

    try {
      const res = await HttpService.consultarFuncionarioPorCpf(onlyNumbers(cpf), token);
      const data = res.data;

      setNome(data.nome || '');
      setRg(data.rg || '');
      setNascimento(data.dataDeNascimento || '');
      setCargo(data.cargo || '');
      setEspecializacao(data.especializacao || '');
      setEndereco(data.endereco || '');
      setTelefone(data.telefone || '');

      setIsEditable(true);
    } catch (err) {
      setModal({ show: true, message: "Funcionário não encontrado ou erro na busca.", isSuccess: false });
      setIsEditable(false);
    } finally {
      setLoading(false);
    }
  };

  const handleEditarPessoa = async () => {
    setLoading(true);
    try {
      await HttpService.cadastrarPessoa({
        nome,
        cpf: onlyNumbers(cpf),
        rg,
        dataDeNascimento,
        endereco,
        telefone: onlyNumbers(telefone),
        cargo,
        especializacao,
      });
      setModal({ show: true, message: "Dados atualizados com sucesso!", isSuccess: true });
    } catch (err) {
      setModal({ show: true, message: getErrorMessage(err), isSuccess: false });
    } finally {
      setLoading(false);
    }
  };

  const closeModal = () => {
    if (modal.isSuccess) setModal({ ...modal, show: false });
    else setModal({ ...modal, show: false });
  };

  return (
    <div>
      <h2>Gerenciar Funcionários</h2>

      <div className="search-section">
        <input
          placeholder="Digite o CPF para buscar"
          value={cpf}
          onChange={(e) => setCpf(maskCPF(e.target.value))}
        />
        <button type="button" onClick={handleConsultarPessoa} disabled={loading || cpf.length < 14}>
          {loading ? "Buscando..." : "Consultar"}
        </button>
      </div>

      <hr />

      <div className="switch__container">
        <span>Cadastro ativo</span>
        <input
          id="switch-shadow"
          className="switch switch--shadow"
          type="checkbox"
          checked={cadastroAtivo}
          onChange={(e) => setCadastroAtivo(e.target.checked)}
          disabled={!isEditable}
        />
        <label htmlFor="switch-shadow"></label>
      </div><br />

      <div className={`form-container ${!isEditable ? 'form-disabled' : ''}`}>
        <input className='formEdit' disabled={!isEditable} style={{ width: '92%' }} placeholder="Nome" value={nome} onChange={(e) => setNome(e.target.value)} />
        <input className='formEdit' disabled={true} placeholder="CPF (Não editável)" value={cpf} />
        <input className='formEdit' disabled={!isEditable} placeholder="RG" value={rg} onChange={(e) => setRg(e.target.value)} />
        <input className='formEdit' disabled={!isEditable} type="date" value={dataDeNascimento} onChange={(e) => setNascimento(e.target.value)} />
        <input className='formEdit' disabled={!isEditable} placeholder="Cargo" value={cargo} onChange={(e) => setCargo(e.target.value)} />
        <input className='formEdit' disabled={!isEditable} placeholder="Especialização" value={especializacao} onChange={(e) => setEspecializacao(e.target.value)} />
        <input className='formEdit' disabled={!isEditable} style={{ width: '52%' }} placeholder="Endereço" value={endereco} onChange={(e) => setEndereco(e.target.value)} />
        <input className='formEdit' disabled={!isEditable} placeholder="Telefone" type="tel" value={telefone} onChange={(e) => setTelefone(maskPhone(e.target.value))} />

        <div className="switch__container">
          <span>Permite gerenciar funcionalidades?</span>
          <input
            id="switch-shadow"
            className="switch switch--shadow"
            type="checkbox"
            checked={permitirGerenciarFuncoes}
            onChange={(e) => setGerenciarFuncoes(e.target.checked)}
            disabled={!isEditable}
          />
          <label htmlFor="switch-shadow"></label>
        </div>

      </div>

      <button
        type="button"
        onClick={handleEditarPessoa}
        disabled={!isEditable || loading}
        style={{ marginTop: '20px', backgroundColor: isEditable ? '#4CAF50' : '#ccc' }}
      >
        {loading ? "Processando..." : "Salvar Alterações"}
      </button>

      {modal.show && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3 style={{ color: modal.isSuccess ? '#2e7d32' : '#d32f2f' }}>
              {modal.isSuccess ? 'Sucesso!' : 'Atenção'}
            </h3>
            <p>{modal.message}</p>
            <button onClick={closeModal}>Fechar</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default GerenciarFuncionario;