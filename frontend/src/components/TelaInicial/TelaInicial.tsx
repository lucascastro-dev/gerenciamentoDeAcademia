import { useEffect, useState, type FC } from 'react';
import { Link } from 'react-router-dom';
import { carregarSessao, isPortalAluno } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import PageShell from '../common/PageShell';
import { TelaInicialWrapper } from './TelaInicial.styled';

const TelaInicial: FC = () => {
  const sessao = carregarSessao();
  const [academia, setAcademia] = useState<any>(null);
  const [msgPagamento, setMsgPagamento] = useState('');
  const [loading, setLoading] = useState(true);
  const aluno = isPortalAluno(sessao);

  useEffect(() => {
    const carregarDados = async () => {
      try {
        if (sessao?.vinculo) {
          const resAcademia = await HttpService.consultarAcademia(sessao.vinculo);
          setAcademia(resAcademia.data);
        }
        if (aluno) {
          const r = await HttpService.portalAlunoPagamentoInfo().catch(() => null);
          if (r?.data?.message) setMsgPagamento(r.data.message);
        }
      } catch (error) {
        console.error('Erro ao carregar dados:', error);
      } finally {
        setLoading(false);
      }
    };
    carregarDados();
  }, [sessao?.vinculo, aluno]);

  if (loading) {
    return (
      <PageShell title="Carregando..." showBack={false}>
        <p>Aguarde...</p>
      </PageShell>
    );
  }

  if (aluno) {
    return (
      <PageShell
        title={`Olá, ${sessao?.nome || 'aluno'}`}
        subtitle="Acompanhe seus dados, turmas e mensalidades"
      >
        <div className="card">
          <p>
            Você está vinculado à <strong>{academia?.razaoSocial || 'sua instituição'}</strong>.
          </p>
          <p className="field-hint" style={{ marginTop: '0.75rem' }}>
            Use o menu lateral para navegar. Senha inicial do portal (após matrícula): <strong>123</strong>
            — altere quando a opção estiver disponível.
          </p>
          {msgPagamento && (
            <p style={{ marginTop: '1rem', padding: '0.75rem', background: '#f0f9ff', borderRadius: 8 }}>
              {msgPagamento}
            </p>
          )}
        </div>
        <div style={{ marginTop: '1rem', display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to="/arealogada/aluno/dados" className="btn-primary" style={{ textDecoration: 'none' }}>
            Meus dados
          </Link>
          <Link to="/arealogada/aluno/turmas" className="btn-secondary" style={{ textDecoration: 'none' }}>
            Minhas turmas
          </Link>
          <Link to="/arealogada/aluno/mensalidades" className="btn-secondary" style={{ textDecoration: 'none' }}>
            Mensalidades
          </Link>
        </div>
      </PageShell>
    );
  }

  return (
    <TelaInicialWrapper>
      <h1>Olá, {sessao?.nome || 'colaborador'}!</h1>
      <p>
        Você está vinculado à <strong>{academia?.razaoSocial || 'sua instituição'}</strong> no EduGestão
        Inteligente.
      </p>
      <p>
        Perfil: <strong>{sessao?.tipoFuncionario || '—'}</strong>
      </p>
      <div style={{ marginTop: '1.5rem', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
        {sessao?.permissoes?.includes('dashboard:visualizar') && (
          <Link to="/arealogada/dashboard" className="btn-primary" style={{ textDecoration: 'none' }}>
            Dashboard administrativo
          </Link>
        )}
        {(sessao?.usuarioMaster || sessao?.permissoes?.includes('financeiro:visualizar')) && (
          <Link
            to="/arealogada/financeiro"
            className="btn-primary"
            style={{ textDecoration: 'none', background: '#0369a1' }}
          >
            Dashboard financeiro
          </Link>
        )}
        {sessao?.tipoFuncionario === 'PROFESSOR' && (
          <Link
            to="/arealogada/professor/turmas"
            className="btn-primary"
            style={{ textDecoration: 'none', background: '#7c3aed' }}
          >
            Minhas turmas
          </Link>
        )}
        {sessao?.permissoes?.includes('aluno:consultar') && (
          <Link to="/arealogada/alunos" className="btn-primary" style={{ textDecoration: 'none', background: '#059669' }}>
            Alunos
          </Link>
        )}
      </div>
    </TelaInicialWrapper>
  );
};

export default TelaInicial;
