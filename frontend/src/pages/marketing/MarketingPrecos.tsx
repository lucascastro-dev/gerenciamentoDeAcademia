import { Link } from 'react-router-dom';
import MarketingShell from '../../components/marketing/MarketingShell';
import ContactCta from '../../components/marketing/ContactCta';
import { COPY, linkWhatsApp } from '../../constants/copy';
import { PLANOS_COMERCIAIS, RECURSOS_TODOS_PLANOS } from '../../constants/planos';
import './precos.css';

const MarketingPrecos: React.FC = () => (
  <MarketingShell>
    <main className="precos-page">
      <section className="precos-hero">
        <div className="precos-hero__inner">
          <h1>{COPY.precosTitulo}</h1>
          <p>{COPY.precosLead}</p>
        </div>
      </section>

      <section className="precos-grid-section">
        <div className="precos-grid">
          {PLANOS_COMERCIAIS.map((plano) => (
            <article
              key={plano.codigo}
              className={`precos-card${plano.destaque ? ' precos-card--destaque' : ''}`}
            >
              {plano.badge && <span className="precos-card__badge">{plano.badge}</span>}
              <h2>{plano.nome}</h2>
              <p className="precos-card__preco">
                <strong>{plano.preco}</strong>
                <span>{plano.periodo}</span>
              </p>
              <ul>
                {plano.recursos.map((r) => (
                  <li key={r}>{r}</li>
                ))}
              </ul>
              <a
                href={linkWhatsApp(`Olá! Tenho interesse no plano ${plano.nome} do Turma360.`)}
                className="precos-card__cta"
                target="_blank"
                rel="noopener noreferrer"
              >
                Contratar plano
              </a>
            </article>
          ))}
        </div>
      </section>

      <section className="precos-inclusos">
        <div className="precos-inclusos__inner">
          <h2>Incluído em todos os planos pagos</h2>
          <ul>
            {RECURSOS_TODOS_PLANOS.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
          <p className="precos-inclusos__nota">{COPY.precosAviso}</p>          <div className="precos-inclusos__links">
            <a href={linkWhatsApp()} className="precos-inclusos__btn" target="_blank" rel="noopener noreferrer">
              Falar no WhatsApp
            </a>
            <Link to="/contato" className="precos-inclusos__btn precos-inclusos__btn--ghost">
              Outras formas de contato
            </Link>
          </div>
        </div>
      </section>

      <ContactCta variant="banner" />
    </main>
  </MarketingShell>
);

export default MarketingPrecos;
