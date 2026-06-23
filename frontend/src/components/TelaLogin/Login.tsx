import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { salvarSessao, TipoFuncionario } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import { extractApiMessage } from '../../utils/apiError';
import FeedbackModal from '../common/FeedbackModal';
import PasswordInput from '../common/PasswordInput';
import '../common/PasswordFields.css';
import './Login.css';

interface Vinculo {
  id: number;
  razaoSocial: string;
  cadastroAtivo?: boolean;
  selecionavel?: boolean;
}

const Login: React.FC = () => {
  const [cpf, setCpf] = useState('');
  const [password, setPassword] = useState('');
  const [vinculo, setVinculo] = useState('');
  const [instituicoes, setInstituicoes] = useState<Vinculo[]>([]);
  const [buscandoVinculos, setBuscandoVinculos] = useState(false);
  const [vinculoAviso, setVinculoAviso] = useState<string | null>(null);
  const [erroLogin, setErroLogin] = useState<string | null>(null);
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
    setVinculoAviso(null);
    try {
      const { data } = await HttpService.listarVinculos(cpfLimpo);
      const ativas = (data || []).filter((i) => i.selecionavel !== false && i.cadastroAtivo !== false);
      setInstituicoes(ativas);
      if (ativas.length === 1) {
        setVinculo(String(ativas[0].id));
      } else {
        setVinculo('');
      }
      if (ativas.length === 0) {
        setVinculoAviso(
          'Nenhuma instituição ativa vinculada a este CPF. Instituições inativas não permitem acesso.',
        );
      }
    } catch {
      setInstituicoes([]);
      setVinculo('');
      setVinculoAviso('Não foi possível buscar suas instituições. Tente novamente.');
    } finally {
      setBuscandoVinculos(false);
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setErroLogin(null);

    try {
      const response = await HttpService.login(cpfLimpo, password, vinculo);
      const data = response.data;

      salvarSessao({
        token: data.token,
        cpf: cpfLimpo,
        vinculo,
        nome: data.nome,
        tipoFuncionario: (data.tipoFuncionario as TipoFuncionario | null) ?? null,
        perfilExibicao: data.perfilExibicao,
        usuarioMaster: data.usuarioMaster,
        masterRaiz: data.masterRaiz,
        acessoFinanceiroCompleto: data.acessoFinanceiroCompleto,
        permissoes: data.permissoes || [],
        tipoAcesso: (data.tipoAcesso as 'COLABORADOR' | 'ALUNO') || 'COLABORADOR',
        planoInstituicaoAtivo: data.planoInstituicaoAtivo,
        situacaoCobranca: data.situacaoCobranca,
        alertaCobranca: data.alertaCobranca,
        mensagemAlertaCobranca: data.mensagemAlertaCobranca,
      });

      navigate('/arealogada/home');
    } catch (err) {
      setErroLogin(extractApiMessage(err, 'Usuário, senha ou instituição inválidos.'));
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
          <label className="login-label" htmlFor="login-cpf">CPF</label>
          <input
            id="login-cpf"
            type="text"
            inputMode="numeric"
            placeholder="000.000.000-00"
            value={cpf}
            onChange={(e) => setCpf(maskCPF(e.target.value))}
            onBlur={carregarInstituicoes}
          />

          {buscandoVinculos && <p className="login-hint">Buscando instituições...</p>}
          {vinculoAviso && !buscandoVinculos && (
            <p className="login-hint login-hint--warn">{vinculoAviso}</p>
          )}

          {instituicoes.length > 0 && (
            <>
              <label className="login-label" htmlFor="login-vinculo">Instituição</label>
              <select
                id="login-vinculo"
                value={vinculo}
                onChange={(e) => setVinculo(e.target.value)}
              >
                <option value="" disabled>Selecione onde deseja entrar</option>
                {instituicoes.map((i) => (
                  <option key={i.id} value={String(i.id)}>{i.razaoSocial}</option>
                ))}
              </select>
            </>
          )}

          <PasswordInput
            id="login-senha"
            label="Senha"
            value={password}
            onChange={setPassword}
            placeholder="Digite sua senha"
            autoComplete="current-password"
          />

          <button className="auth-btn-primary" disabled={!podeEntrar} type="submit">
            {loading ? 'Entrando...' : 'Entrar'}
          </button>
        </form>

        <div className="links-container">
          <Link to="/areapublica/cadastro">Pré-cadastro colaborador</Link>
          <Link to="/areapublica/esqueciSenha">Esqueci minha senha</Link>
        </div>
      </div>

      <FeedbackModal
        open={!!erroLogin}
        success={false}
        title="Não foi possível entrar"
        message={erroLogin || ''}
        onClose={() => setErroLogin(null)}
      />
    </div>
  );
};

export default Login;
