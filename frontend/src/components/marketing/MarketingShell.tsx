import { Link, useLocation } from 'react-router-dom';

import LogoMark from '../common/LogoMark';

import { APP_NAME } from '../../constants/branding';

import { CONTATO, linkWhatsApp } from '../../constants/copy';

import { useMarketingAnchor } from '../../hooks/useMarketingAnchor';

import './marketing-shell.css';



interface MarketingShellProps {

  children: React.ReactNode;

}



const MarketingShell: React.FC<MarketingShellProps> = ({ children }) => {

  const { pathname } = useLocation();

  const { goToSection } = useMarketingAnchor();

  const isActive = (path: string) => pathname === path;



  return (

    <div className="mkt-shell">

      <header className="mkt-shell__header">

        <Link to="/" className="mkt-shell__logo" aria-label={`${APP_NAME} — início`}>

          <LogoMark size="sm" />

        </Link>

        <nav className="mkt-shell__nav" aria-label="Principal">

          <a href="/#segmentos" className="mkt-shell__link" onClick={goToSection('segmentos')}>

            Segmentos

          </a>

          <a href="/#recursos" className="mkt-shell__link" onClick={goToSection('recursos')}>

            Recursos

          </a>

          <Link to="/precos" className={`mkt-shell__link${isActive('/precos') ? ' mkt-shell__link--active' : ''}`}>

            Preços

          </Link>

          <Link to="/contato" className={`mkt-shell__link${isActive('/contato') ? ' mkt-shell__link--active' : ''}`}>

            Fale conosco

          </Link>

          <Link to="/entrar" className="mkt-shell__btn mkt-shell__btn--primary">

            Entrar

          </Link>

        </nav>

      </header>

      {children}

      <footer className="mkt-shell__footer">

        <div className="mkt-shell__footer-grid">

          <div className="mkt-shell__footer-brand">

            <LogoMark size="sm" />

            <p className="mkt-shell__footer-tagline">Gestão educacional inteligente para escolas, cursos e academias.</p>

          </div>

          <div className="mkt-shell__footer-col">

            <h3>Produto</h3>

            <nav>

              <a href="/#segmentos" onClick={goToSection('segmentos')}>Segmentos</a>

              <a href="/#recursos" onClick={goToSection('recursos')}>Recursos</a>

              <Link to="/precos">Preços</Link>

              <Link to="/entrar">Entrar</Link>

            </nav>

          </div>

          <div className="mkt-shell__footer-col">

            <h3>Contato</h3>

            <nav>

              <Link to="/contato">Formulário</Link>

              <a href={linkWhatsApp()} target="_blank" rel="noopener noreferrer">

                WhatsApp {CONTATO.whatsappLabel}

              </a>

              <span>{CONTATO.horario}</span>

            </nav>

          </div>

        </div>

        <div className="mkt-shell__footer-bottom">

          <p>© {new Date().getFullYear()} {APP_NAME} — gestão educacional inteligente.</p>

          <p>

            Desenvolvido por{' '}

            <a href="https://lucascastro-dev.github.io/lr-site/" target="_blank" rel="noopener noreferrer">

              LR Info e Tecnologia Ltda

            </a>

          </p>

        </div>

      </footer>

    </div>

  );

};



export default MarketingShell;


