import { useEffect, useState, type FC } from 'react';
import { Link } from 'react-router-dom';
import { carregarSessao, isModoPlataforma, isPortalAluno, labelPerfil } from '../../auth/permissoes';
import { COPY_UI } from '../../constants/copy';
import HttpService from '../../services/HttpService';
import PageShell from '../common/PageShell';
import { TelaInicialWrapper } from './TelaInicial.styled';
import '../../theme/portal-aluno.css';

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
      <PageShell title={COPY_UI.carregando} showBack={false}>
        <p>{COPY_UI.aguarde}</p>
      </PageShell>
    );
  }

  if (aluno) {
    return (
      <PageShell
        title={`Olá, ${sessao?.nome || 'aluno'}`}
        subtitle="Turmas, mensalidades e programação em um só lugar"
        showBack={false}
      >
        <div className="portal-aluno-page">
          <section className="portal-aluno-hero">
            <h2>{COPY_UI.portalAluno.heroTitulo}</h2>
            <p>
              {COPY_UI.portalAluno.vinculoInstituicao(instituicao?.razaoSocial || 'sua instituição')}
            </p>
          </section>

          {msgPagamento && (
            <div className="portal-aluno-alert" style={{ background: '#f0f9ff', borderColor: '#bae6fd', color: '#0369a1' }}>
              {msgPagamento}
            </div>
          )}

          <div className="portal-aluno-quick">
            <Link to="/arealogada/aluno/turmas">
              <span className="portal-aluno-quick__icon" aria-hidden="true">🥋</span>
              Minhas turmas
            </Link>
            <Link to="/arealogada/aluno/programacao">
              <span className="portal-aluno-quick__icon" aria-hidden="true">📅</span>
              Programação
            </Link>
            <Link to="/arealogada/aluno/mensalidades">
              <span className="portal-aluno-quick__icon" aria-hidden="true">💳</span>
              Mensalidades
            </Link>
            <Link to="/arealogada/aluno/dados">
              <span className="portal-aluno-quick__icon" aria-hidden="true">👤</span>
              Meus dados
            </Link>
          </div>
        </div>
      </PageShell>
    );
  }

  if (master) {
    return (
      <PageShell
        title={`Olá, ${sessao?.nome || 'operador'}`}
        subtitle={COPY_UI.colaborador.masterSubtitulo}
        showBack={false}
      >
        <div className="card">
          <p>
            Perfil: <strong>{labelPerfil(sessao)}</strong>
            {sessao?.masterRaiz && ' (master raiz)'}
          </p>
          <p style={{ marginTop: '0.75rem' }}>
            Você administra o <strong>SaaS multi-instituição</strong>: cadastro de escolas, planos comerciais,
            ativação de colaboradores e suporte acadêmico em todas as unidades.
          </p>
          {resumoPlat && (
            <div style={{ marginTop: '1rem', display: 'grid', gap: '0.5rem' }}>
              <p>
                <strong>{resumoPlat.instituicoesAtivas}</strong> instituições ativas de{' '}
                <strong>{resumoPlat.instituicoesCadastradas}</strong> cadastradas.
              </p>
            </div>
          )}
        </div>
        <div style={{ marginTop: '1rem', display: 'grid', gap: '0.75rem', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
          <Link to="/arealogada/financeiro" className="btn-primary" style={{ textDecoration: 'none', textAlign: 'center' }}>
            Resumo financeiro
          </Link>
          <Link to="/arealogada/financeiro/pendentes" className="btn-primary" style={{ textDecoration: 'none', textAlign: 'center', background: '#0369a1' }}>
            Pagamentos pendentes
          </Link>
          <Link to="/arealogada/financeiro/planos-expirados" className="btn-secondary" style={{ textDecoration: 'none', textAlign: 'center' }}>
            Planos expirados
          </Link>
          <Link to="/arealogada/cadastrar-instituicao" className="btn-secondary" style={{ textDecoration: 'none', textAlign: 'center' }}>
            Nova instituição
          </Link>
          <Link to="/arealogada/instituicoes" className="btn-secondary" style={{ textDecoration: 'none', textAlign: 'center' }}>
            Consultar instituições
          </Link>
          <Link to="/arealogada/gestaoAcademia" className="btn-secondary" style={{ textDecoration: 'none', textAlign: 'center' }}>
            Ativar instituição
          </Link>
        </div>
      </PageShell>
    );
  }

  return (
    <TelaInicialWrapper>
      <h1>Olá, {sessao?.nome || 'colaborador'}!</h1>
      <p>
        {COPY_UI.colaborador.homeSubtitulo(instituicao?.razaoSocial || 'sua instituição')}
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
