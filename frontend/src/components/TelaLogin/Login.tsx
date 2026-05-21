import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { salvarSessao } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import './Login.css';

interface Vinculo {
  id: number;
  razaoSocial: string;
}

const Login: React.FC = () => {
  const [cpf, setCpf] = useState('');
  const [password, setPassword] = useState('');
  const [vinculo, setVinculo] = useState('');
  const [instituicoes, setInstituicoes] = useState<Vinculo[]>([]);
  const [buscandoVinculos, setBuscandoVinculos] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const maskCPF = (value: string) =>
    value
      .replace(/\D/g, '')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2')
      .slice(0, 14);

  const cpfLimpo = cpf.replace(/\D/g, '');

  const carregarInstituicoes = async () => {
    if (cpfLimpo.length < 11) {
      setInstituicoes([]);
      setVinculo('');
      return;
    }
    setBuscandoVinculos(true);
    setErrorMsg(null);
    try {
      const { data } = await HttpService.listarVinculos(cpfLimpo);
      setInstituicoes(data);
      if (data.length === 1) {
        setVinculo(String(data[0].id));
      } else {
        setVinculo('');
      }
      if (data.length === 0) {
        setErrorMsg('Nenhuma instituição vinculada a este CPF ou cadastro inativo.');
      }
    } catch {
      setInstituicoes([]);
      setVinculo('');
      setErrorMsg('Não foi possível buscar suas instituições. Tente novamente.');
    } finally {
      setBuscandoVinculos(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg(null);

    try {
      const response = await HttpService.login(cpfLimpo, password, vinculo);
      const data = response.data;

      salvarSessao({
        token: data.token,
        cpf: cpfLimpo,
        vinculo,
        nome: data.nome,
        tipoFuncionario: data.tipoFuncionario as any,
        usuarioMaster: data.usuarioMaster,
        permissoes: data.permissoes || [],
        tipoAcesso: (data.tipoAcesso as 'COLABORADOR' | 'ALUNO') || 'COLABORADOR',
        planoInstituicaoAtivo: data.planoInstituicaoAtivo,
      });

      const destino = '/arealogada/home';
      navigate(destino);
    } catch (err) {
      setErrorMsg(extractApiMessage(err, 'Usuário, senha ou instituição inválidos.'));
    } finally {
      setLoading(false);
    }
  };

  const podeEntrar =
    cpfLimpo.length >= 11 &&
    password.length > 0 &&
    vinculo.length > 0 &&
    !loading &&
    !buscandoVinculos;

  return (
    <div className="auth-page">
      <div className="auth-page__brand">EduGestão Inteligente</div>

      <div className="login-container">
        <h2>Entrar</h2>

        <form onSubmit={handleLogin}>
          <label className="login-label">CPF</label>
          <input
            type="text"
            inputMode="numeric"
            placeholder="000.000.000-00"
            value={cpf}
            onChange={(e) => setCpf(maskCPF(e.target.value))}
            onBlur={carregarInstituicoes}
          />

          {buscandoVinculos && <p className="login-hint">Buscando instituições...</p>}

          {instituicoes.length > 0 && (
            <>
              <label className="login-label">Instituição</label>
              <select value={vinculo} onChange={(e) => setVinculo(e.target.value)}>
                <option value="" disabled>Selecione onde deseja entrar</option>
                {instituicoes.map((i) => (
                  <option key={i.id} value={String(i.id)}>{i.razaoSocial}</option>
                ))}
              </select>
            </>
          )}

          <label className="login-label">Senha</label>
          <input
            type="password"
            placeholder="Digite sua senha"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button disabled={!podeEntrar} type="submit">
            {loading ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <div className="links-container">
          <Link to="/areapublica/cadastro">Pré-cadastro colaborador</Link>
          <Link to="/areapublica/esqueciSenha">Esqueci minha senha</Link>
          <Link to="/areapublica/solicitarAcesso">Solicitar ativação (RH)</Link>
        </div>
      </div>

      {errorMsg && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Atenção</h3>
            <p>{errorMsg}</p>
            <button type="button" onClick={() => setErrorMsg(null)}>Fechar</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Login;
