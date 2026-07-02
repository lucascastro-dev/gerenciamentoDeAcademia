import { Link } from 'react-router-dom';
import LogoMark from './LogoMark';
import { APP_TAGLINE } from '../../constants/branding';
import { COPY } from '../../constants/copy';
import './AuthLayout.css';

interface AuthLayoutProps {
  children: React.ReactNode;
  /** Formulário largo (pré-cadastro) */
  wide?: boolean;
  title?: string;
  subtitle?: string;
}

const AuthLayout: React.FC<AuthLayoutProps> = ({ children, wide = false, title, subtitle }) => (
  <div className="auth-layout">
    <aside className="auth-layout__aside" aria-hidden="true">
      <LogoMark size="lg" className="auth-layout__aside-logo" />
      <p className="auth-layout__aside-tagline">{APP_TAGLINE}</p>
      <ul className="auth-layout__aside-list">
        {COPY.authAsideBullets.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
      <Link to="/contato" className="auth-layout__aside-link">
        {COPY.authAsideCta}
      </Link>
    </aside>

    <div className="auth-layout__main">
      <Link to="/" className="auth-layout__back">← Voltar ao site</Link>
      <LogoMark size="sm" className="auth-layout__brand-mobile" showName />

      <div className={`auth-layout__card${wide ? ' auth-layout__card--wide' : ''}`}>
        {title && <h1 className="auth-layout__title">{title}</h1>}
        {subtitle && <p className="auth-layout__subtitle">{subtitle}</p>}
        {children}
      </div>
    </div>
  </div>
);

export { COPY as AuthCopy };
export default AuthLayout;
