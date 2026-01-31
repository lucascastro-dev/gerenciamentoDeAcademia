import { AxiosError } from 'axios';
import React, { useState } from 'react';
import HttpService from '../../services/HttpService';
import "./AreaLogada.css";

const GerenciarAcademia: React.FC = () => {
  const [cnpj, setCnpj] = useState('');
  const [endereco, setEndereco] = useState('');
  const [telefone, setTelefone] = useState('');
  const [razaoSocial, setRazaoSocial] = useState('');
  const [cadastroAtivo, setCadastroAtivo] = useState(false);

  const [loading, setLoading] = useState(false);
  const [isEditable, setIsEditable] = useState(false);

  const [modal, setModal] = useState<{
    show: boolean;
    message: string;
    isSuccess: boolean;
  }>({
    show: false,
    message: '',
    isSuccess: false,
  });

  const maskCNPJ = (v: string) => v.toUpperCase().replace(/[^A-Z0-9]/g, "").replace(/^([A-Z0-9]{2})([A-Z0-9])/, "$1.$2").replace(/^([A-Z0-9]{2})\.([A-Z0-9]{3})([A-Z0-9])/, "$1.$2.$3").replace(/\.([A-Z0-9]{3})([A-Z0-9])/, ".$1/$2").replace(/([A-Z0-9]{4})([A-Z0-9])/, "$1-$2").slice(0, 18);

  const maskPhone = (v: string) =>
    v
      .replace(/\D/g, "")
      .replace(/^(\d{2})(\d)/g, "($1) $2")
      .replace(/(\d)(\d{4})$/, "$1-$2")
      .slice(0, 15);

  const onlyNumbers = (v: string) => v.replace(/\D/g, "");

  const getErrorMessage = (err: any) => {
    const axiosError = err as AxiosError<any>;
    return (
      axiosError.response?.data?.message ||
      axiosError.response?.data?.error ||
      "Erro ao processar requisição."
    );
  };

  const resetForm = () => {
    setRazaoSocial('');
    setTelefone('');
    setCadastroAtivo(false);
    setIsEditable(false);
  };

  const handleConsultarPessoa = async () => {
    const token = localStorage.getItem('@App:token');

    if (!token) {
      setModal({
        show: true,
        message: "Sessão expirada. Faça login novamente.",
        isSuccess: false,
      });
      return;
    }

    setLoading(true);
    resetForm();

    try {
      const res = await HttpService.consultarAcademiaPorCnpj(
        onlyNumbers(cnpj),
        token
      );

      const data = res.data;

      setRazaoSocial(data.razaoSocial ?? '');
      setEndereco(data.endereco ?? '');
      setTelefone(data.telefone ?? '');
      setCadastroAtivo(data.cadastroAtivo ?? false);

      setIsEditable(true);
    } catch (err) {
      setModal({
        show: true,
        message: "Academia não encontrada ou erro na busca.",
        isSuccess: false,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleEditarPessoa = async () => {
    setLoading(true);

    try {
      const token: any = localStorage.getItem('@App:token');
      const payload: any = {
        razaoSocial: razaoSocial,
        cnpj: onlyNumbers(cnpj),
        endereco,
        telefone: onlyNumbers(telefone),
        cadastroAtivo,
      };

      await HttpService.editarAcademia(payload, token);

      setModal({
        show: true,
        message: "Dados atualizados com sucesso!",
        isSuccess: true,
      });
    } catch (err) {
      setModal({
        show: true,
        message: getErrorMessage(err),
        isSuccess: false,
      });
    } finally {
      setLoading(false);
    }
  };

  const closeModal = () => {
    setModal({ ...modal, show: false });
  };

  return (
    <div>
      <h2>Gerenciar Academias</h2>

      <div className="search-section">
        <input style={{marginRight: '15px'}}
          placeholder="Digite o CNPJ para buscar"
          value={cnpj}
          onChange={(e) => setCnpj(maskCNPJ(e.target.value))}
        />
        <button
          type="button"
          onClick={handleConsultarPessoa}
          disabled={loading || cnpj.length < 14}
        >
          {loading ? "Buscando..." : "Consultar"}
        </button>
      </div>

      <hr />

      <div className={`form-container ${!isEditable ? 'form-disabled' : ''}`}>
        <input
          className="formEdit"
          disabled={!isEditable}
          style={{ width: '92%' }}
          placeholder="Razão Social"
          value={razaoSocial}
          onChange={(e) => setRazaoSocial(e.target.value)}
        />

        <input
          className="formEdit"
          disabled
          placeholder="CNPJ (Não editável)"
          value={cnpj}
        />

        <input
          className="formEdit"
          disabled={!isEditable}
          style={{ width: '52%' }}
          placeholder="Endereço"
          value={endereco}
          onChange={(e) => setEndereco(e.target.value)}
        />

        <input
          className="formEdit"
          disabled={!isEditable}
          placeholder="Telefone"
          type="tel"
          value={telefone}
          onChange={(e) => setTelefone(maskPhone(e.target.value))}
        />

        <div style={{ marginLeft: '10px' }} className="switch-row">
          <label htmlFor="cadastro-ativo" className="switch-label">
            <span>Cadastro ativo</span>

            <div className="switch-wrapper">
              <input
                id="cadastro-ativo"
                className="switch switch--shadow"
                type="checkbox"
                checked={cadastroAtivo}
                onChange={(e) => setCadastroAtivo(e.target.checked)}
                disabled={!isEditable}
              />
              <span className="slider"></span>
            </div>
          </label>
        </div>
      </div>

      <button
        type="button"
        onClick={handleEditarPessoa}
        disabled={!isEditable || loading}
        style={{
          marginTop: '20px',
          backgroundColor: isEditable ? '#4CAF50' : '#ccc'
        }}
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

export default GerenciarAcademia;
