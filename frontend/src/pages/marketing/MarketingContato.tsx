import MarketingShell from '../../components/marketing/MarketingShell';
import ContactForm from '../../components/marketing/ContactForm';
import { CONTATO, COPY, FAQ_CONTATO, linkWhatsApp } from '../../constants/copy';
import './contato.css';

const MarketingContato: React.FC = () => (
  <MarketingShell>
    <main className="contato-page">
      <section className="contato-hero">
        <h1>{COPY.contatoTitulo}</h1>
        <p>{COPY.contatoLead}</p>
      </section>

      <section className="contato-layout">
        <div className="contato-layout__form">
          <h2>Envie sua mensagem</h2>
          <p className="contato-layout__lead">{COPY.contatoFormLead}</p>
          <ContactForm />
        </div>

        <aside className="contato-layout__aside">
          <article className="contato-card contato-card--whatsapp">
            <h2>WhatsApp comercial</h2>
            <p>Canal direto para dúvidas sobre planos, implantação e demonstração.</p>
            <p className="contato-card__destaque">{CONTATO.whatsappLabel}</p>
            <p className="contato-card__horario">{CONTATO.horario}</p>
            <a href={linkWhatsApp()} className="contato-card__btn" target="_blank" rel="noopener noreferrer">
              Iniciar conversa
            </a>
          </article>
        </aside>
      </section>

      <section className="contato-faq">
        <h2>Perguntas frequentes</h2>
        <dl>
          {FAQ_CONTATO.map((item) => (
            <div key={item.pergunta}>
              <dt>{item.pergunta}</dt>
              <dd>{item.resposta}</dd>
            </div>
          ))}
        </dl>
      </section>
    </main>
  </MarketingShell>
);

export default MarketingContato;
