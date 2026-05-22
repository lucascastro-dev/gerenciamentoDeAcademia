import { useEffect, useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { APP_NAME } from '../../constants/branding';
import { carregarSessao, isPortalAluno, limparSessao, SessaoUsuario } from '../../auth/permissoes';
import { obterMenus } from '../../auth/menuConfig';
import HttpService from '../../services/HttpService';
import FeedbackModal from '../common/FeedbackModal';
import '../../theme/layout.css';

const STORAGE_COLLAPSE = '@App:menuCollapsed';

function loadCollapsed(): Record<string, boolean> {
  try {
    const raw = localStorage.getItem(STORAGE_COLLAPSE);
    return raw ? JSON.parse(raw) : {};
  } catch {
    return {};
  }
}

export const LayoutHome: React.FC = () => {
  const [sessao, setSessao] = useState<SessaoUsuario | null>(carregarSessao());
  const [instituicao, setInstituicao] = useState<{ razaoSocial?: string } | null>(null);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [collapsed, setCollapsed] = useState<Record<string, boolean>>(loadCollapsed);
  const [alertaCobranca, setAlertaCobranca] = useState<{ open: boolean; message: string }>({
    open: false,
    message: '',
  });
  const menus = obterMenus(sessao);

  useEffect(() => {
    const s = carregarSessao();
    setSessao(s);
    if (s?.alertaCobranca && s.mensagemAlertaCobranca) {
      setAlertaCobranca({ open: true, message: s.mensagemAlertaCobranca });
    }
    if (s?.vinculo) {
      HttpService.consultarInstituicao(s.vinculo).then((r) => setInstituicao(r.data)).catch(() => undefined);
      if (!s.nome && s.cpf) {
        const carregarNome = isPortalAluno(s)
          ? HttpService.portalAlunoDados().then((r) => r.data.nome)
          : HttpService.meuPerfil().then((r) => r.data.nome);
        carregarNome
          .then((nome) => setSessao((prev) => (prev ? { ...prev, nome } : prev)))
          .catch(() => undefined);
      }
    }
  }, []);

  const toggleSection = (titulo: string) => {
    setCollapsed((prev) => {
      const next = { ...prev, [titulo]: !prev[titulo] };
      localStorage.setItem(STORAGE_COLLAPSE, JSON.stringify(next));
      return next;
    });
  };

  const handleLogout = () => {
    limparSessao();
    window.location.href = '/areapublica/login';
  };

  return (
    <div className={`app-layout ${mobileOpen ? 'app-layout--menu-open' : ''}`}>
      {mobileOpen && (
        <button
          type="button"
          className="app-layout__backdrop"
          aria-label="Fechar menu"
          onClick={() => setMobileOpen(false)}
        />
      )}

      <aside className="app-sidebar">
        <div className="app-sidebar__brand">{APP_NAME}</div>
        {menus.map((sec) => {
          const fechado = collapsed[sec.titulo];
          return (
            <div key={sec.titulo} className="app-sidebar__section">
              <button
                type="button"
                className="app-sidebar__section-toggle"
                onClick={() => toggleSection(sec.titulo)}
                aria-expanded={!fechado}
              >
                <span className="app-sidebar__arrow">{fechado ? '▶' : '▼'}</span>
                {sec.titulo}
              </button>
              {!fechado && (
                <nav className="app-sidebar__nav">
                  {sec.itens.map((item) => (
                    <NavLink
                      key={item.path}
                      to={item.path}
                      end={
                        item.path === '/arealogada/turmas'
                        || item.path === '/arealogada/alunos'
                        || item.path === '/arealogada/home'
                        || item.path === '/arealogada/financeiro'
                      }
                      className={({ isActive }) =>
                        `app-sidebar__link${isActive ? ' app-sidebar__link--active' : ''}`
                      }
                      onClick={() => setMobileOpen(false)}
                    >
                      {item.label}
                    </NavLink>
                  ))}
                </nav>
              )}
            </div>
          );
        })}
      </aside>

      <header className="app-header">
        <div className="app-header__left">
          <button
            type="button"
            className="app-layout__menu-toggle hide-desktop"
            aria-label="Abrir menu"
            onClick={() => setMobileOpen((o) => !o)}
          >
            ☰
          </button>
          <span className="hide-mobile app-header__instituicao">
            {instituicao?.razaoSocial || 'Instituição'}
          </span>
        </div>
        <div className="app-header__user">
          <strong>{sessao?.nome || 'Usuário'}</strong>
          <div className="app-header__role">
            {isPortalAluno(sessao) ? 'ALUNO' : (sessao?.tipoFuncionario || 'Perfil')}
          </div>
          <button type="button" className="app-header__logout" onClick={handleLogout}>
            Sair
          </button>
        </div>
      </header>

      <main className="app-main">
        <Outlet />
      </main>

      <FeedbackModal
        open={alertaCobranca.open}
        success={false}
        message={alertaCobranca.message}
        onClose={() => setAlertaCobranca((a) => ({ ...a, open: false }))}
      />
    </div>
  );
};
