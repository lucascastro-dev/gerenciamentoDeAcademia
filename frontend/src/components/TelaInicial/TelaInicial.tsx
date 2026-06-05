import { useEffect, useState, type FC } from 'react';
import { Link } from 'react-router-dom';
import { carregarSessao, isModoPlataforma, isPortalAluno, labelPerfil } from '../../auth/permissoes';
import HttpService from '../../services/HttpService';
import PageShell from '../common/PageShell';
import { TelaInicialWrapper } from './TelaInicial.styled';

const TelaInicial: FC = () => {
  const sessao = carregarSessao();
  const [instituicao, setInstituicao] = useState<{ razaoSocial?: string } | null>(null);
  const [resumoPlat, setResumoPlat] = useState<{ instituicoesAtivas: number; instituicoesCadastradas: number } | null>(null);
  const [msgPagamento, setMsgPagamento] = useState('');
  const [loading, setLoading] = useState(true);
  const aluno = isPortalAluno(sessao);
  const master = isModoPlataforma(sessao);

  useEffect(() => {
    const carregarDados = async () => {
      try {
        if (master) {
          const r = await HttpService.dashboardPlataformaResumo();
          setResumoPlat(r.data);
        } else if (sessao?.vinculo && sessao.vinculo !== '0') {
          const resInstituicao = await HttpService.consultarInstituicao(sessao.vinculo);
          setInstituicao(resInstituicao.data);
        }
        if (aluno) {
          const res = await HttpService.portalAlunoPagamentoInfo().catch(() => null);
          if (res?.data?.message) setMsgPagamento(res.data.message);
        }
      } catch (error) {
        console.error('Erro ao carregar dados:', error);
      } finally {
        setLoading(false);
      }
    };
    carregarDados();
  }, [sessao?.vinculo, aluno, master]);

  if (loading) {
    return (
      <PageShell title="Carregando..." showBack={false}>
        <p>Aguarde...</p>
      </PageShell>
    );
  }

  if (aluno) {
    return (
      <PageShell title={`Olá, ${sessao?.nome || 'aluno'}`} subtitle="Acompanhe seus dados, turmas e mensalidades">
        <div className="card">
          <p>
            Você está vinculado à <strong>{instituicao?.razaoSocial || 'sua instituição'}</strong>.
          </p>
          {msgPagamento && (
            <p style={{ marginTop: '1rem', padding: '0.75rem', background: '#f0f9ff', borderRadius: 8 }}>
              {msgPagamento}
            </p>
          )}
        </div>
        <div style={{ marginTop: '1rem', display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to="/arealogada/aluno/dados" className="btn-primary" style={{ textDecoration: 'none' }}>Meus dados</Link>
          <Link to="/arealogada/aluno/turmas" className="btn-secondary" style={{ textDecoration: 'none' }}>Minhas turmas</Link>
        </div>
      </PageShell>
    );
  }

  if (master) {
    return (
      <PageShell
        title={`Olá, ${sessao?.nome || 'operador'}`}
        subtitle="Central de gestão da plataforma EduGestão Inteligente"
        showBack={false}
      >
        <div className="card">
          <p>
            Perfil: <strong>{labelPerfil(sessao)}</strong>
          </p>
          <p style={{ marginTop: '0.75rem' }}>
            Você opera o <strong>SaaS multi-instituição</strong>: cadastro de instituições, planos, ativação de
            colaboradores e suporte acadêmico em todas as instituições.
          </p>
          {resumoPlat && (
            <p style={{ marginTop: '1rem' }}>
              <strong>{resumoPlat.instituicoesAtivas}</strong> instituições ativas de{' '}
              <strong>{resumoPlat.instituicoesCadastradas}</strong> cadastradas.
            </p>
          )}
        </div>
        <div style={{ marginTop: '1rem', display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          <Link to="/arealogada/dashboard" className="btn-primary" style={{ textDecoration: 'none' }}>
            Dashboard administrativo
          </Link>
          <Link to="/arealogada/financeiro" className="btn-primary" style={{ textDecoration: 'none', background: '#0369a1' }}>
            Dashboard financeiro
          </Link>
          <Link to="/arealogada/gestaoAcademia" className="btn-secondary" style={{ textDecoration: 'none' }}>
            Ativar instituição
          </Link>
          <Link to="/arealogada/instituicoes" className="btn-secondary" style={{ textDecoration: 'none' }}>
            Consultar instituições
          </Link>
        </div>
      </PageShell>
    );
  }

  return (
    <TelaInicialWrapper>
      <h1>Olá, {sessao?.nome || 'colaborador'}!</h1>
      <p>
        Você está vinculado à <strong>{instituicao?.razaoSocial || 'sua instituição'}</strong> no EduGestão
        Inteligente.
      </p>
      <p>Perfil: <strong>{labelPerfil(sessao)}</strong></p>
      <div style={{ marginTop: '1.5rem', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
        {sessao?.permissoes?.includes('dashboard:visualizar') && (
          <Link to="/arealogada/dashboard" className="btn-primary" style={{ textDecoration: 'none' }}>
            Dashboard administrativo
          </Link>
        )}
        {sessao?.permissoes?.includes('financeiro:visualizar') && (
          <Link to="/arealogada/financeiro" className="btn-primary" style={{ textDecoration: 'none', background: '#0369a1' }}>
            Dashboard financeiro
          </Link>
        )}
      </div>
    </TelaInicialWrapper>
  );
};

export default TelaInicial;
