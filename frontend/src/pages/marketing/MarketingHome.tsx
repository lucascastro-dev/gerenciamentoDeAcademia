import { Link } from 'react-router-dom';
import MarketingShell from '../../components/marketing/MarketingShell';
import PortalPreviewDemo from '../../components/marketing/PortalPreviewDemo';import {
  COPY,
  FEATURES,
  SEGMENTS,
  STATS,
  STEPS_SAAS,
  TESTIMONIALS,
} from '../../constants/copy';
import './marketing.css';

const MarketingHome: React.FC = () => (
  <MarketingShell>
    <main>
      <section className="marketing__hero">
        <div className="marketing__hero-inner">
          <div className="marketing__hero-copy">
            <span className="marketing__eyebrow">{COPY.heroEyebrow}</span>
            <h1 className="marketing__headline">{COPY.tagline}</h1>
            <p className="marketing__lead">{COPY.heroLead}</p>
            <div className="marketing__cta-row">
              <Link to="/entrar" className="marketing__btn marketing__btn--primary marketing__btn--lg">
                Acessar minha conta
              </Link>
              <Link to="/precos" className="marketing__btn marketing__btn--ghost marketing__btn--lg">
                Ver planos
              </Link>
            </div>
          </div>
          <div className="marketing__hero-visual" aria-hidden="true">
            <PortalPreviewDemo />
          </div>        </div>
        <ul className="marketing__stats">
          {STATS.map((s) => (
            <li key={s.label}>
              <strong>{s.value}</strong>
              <span>{s.label}</span>
            </li>
          ))}
        </ul>
      </section>

      <section id="segmentos" className="marketing__section marketing__section--alt">
        <div className="marketing__container">
          <h2 className="marketing__section-title">Para o segmento da sua instituição</h2>
          <p className="marketing__section-sub">
            Do tatame à sala de aula: uma plataforma que acompanha a rotina de escolas, academias e cursos de todos os portes.
          </p>
          <ul className="marketing__segments">
            {SEGMENTS.map((s) => (
              <li key={s.title} className="marketing__segment-card">
                <span className="marketing__segment-icon">{s.icon}</span>
                <h3>{s.title}</h3>
                <p>{s.text}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section id="recursos" className="marketing__section">
        <div className="marketing__container">
          <h2 className="marketing__section-title">Recursos para o dia a dia da sua instituição</h2>
          <p className="marketing__section-sub">
            Pedagógico, financeiro e gestão de equipe integrados — para você focar no que importa: ensinar e crescer.
          </p>
          <ul className="marketing__grid">
            {FEATURES.map((f) => (
              <li key={f.title} className="marketing__card">
                <span className="marketing__card-icon">{f.icon}</span>
                <h3>{f.title}</h3>
                <p>{f.text}</p>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section id="acesso" className="marketing__section marketing__section--gradient">
        <div className="marketing__container">
          <h2 className="marketing__section-title marketing__section-title--light">Como funciona o acesso</h2>
          <p className="marketing__section-sub marketing__section-sub--light">
            Um único portal para toda a equipe: o sistema reconhece seus vínculos e você escolhe onde atuar.
          </p>
          <ol className="marketing__steps">
            {STEPS_SAAS.map((s) => (
              <li key={s.step} className="marketing__step">
                <span className="marketing__step-num">{s.step}</span>
                <div>
                  <h3>{s.title}</h3>
                  <p>{s.text}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </section>

      <section className="marketing__section">
        <div className="marketing__container">
          <h2 className="marketing__section-title">Quem já organizou a gestão com tecnologia</h2>
          <ul className="marketing__quotes">
            {TESTIMONIALS.map((t) => (
              <li key={t.author} className="marketing__quote">
                <div className="marketing__quote-header">
                  <span className="marketing__quote-avatar" aria-hidden="true">{t.initials}</span>
                  <div>
                    <strong>{t.author}</strong>
                    <span>{t.role}</span>
                  </div>
                </div>
                <blockquote>{t.quote}</blockquote>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="marketing__cta-banner">
        <div className="marketing__container marketing__cta-banner-inner">
          <div>
            <h2>Pronto para simplificar a gestão da sua instituição?</h2>
            <p>Conheça os planos ou converse com nosso time comercial.</p>
          </div>
          <div className="marketing__cta-banner-actions">
            <Link to="/precos" className="marketing__btn marketing__btn--light marketing__btn--lg">
              Ver planos
            </Link>
            <Link to="/contato" className="marketing__btn marketing__btn--ghost marketing__btn--lg">
              Fale conosco
            </Link>
          </div>
        </div>
      </section>
    </main>
  </MarketingShell>
);

export default MarketingHome;
