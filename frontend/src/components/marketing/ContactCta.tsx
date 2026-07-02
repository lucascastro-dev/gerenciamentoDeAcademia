import { Link } from 'react-router-dom';
import { CONTATO, COPY, linkWhatsApp } from '../../constants/copy';
import './contact-cta.css';

interface ContactCtaProps {
  variant?: 'default' | 'compact' | 'banner';
  titulo?: string;
  lead?: string;
}

const ContactCta: React.FC<ContactCtaProps> = ({
  variant = 'default',
  titulo = COPY.contatoTitulo,
  lead = COPY.contatoLead,
}) => (
  <section className={`contact-cta contact-cta--${variant}`} id="contato">
    <div className="contact-cta__inner">
      <div className="contact-cta__copy">
        <h2>{titulo}</h2>
        <p>{lead}</p>
        <p className="contact-cta__horario">{CONTATO.horario}</p>
      </div>
      <div className="contact-cta__actions">
        <a href={linkWhatsApp()} className="contact-cta__btn contact-cta__btn--whatsapp" target="_blank" rel="noopener noreferrer">
          <span aria-hidden="true">💬</span>
          WhatsApp {CONTATO.whatsappLabel}
        </a>
        <Link to="/contato" className="contact-cta__btn contact-cta__btn--email">
          <span aria-hidden="true">✉</span>
          Enviar formulário
        </Link>
      </div>
    </div>
  </section>
);

export default ContactCta;
