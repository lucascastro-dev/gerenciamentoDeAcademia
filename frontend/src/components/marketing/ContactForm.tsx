import { FormEvent, useState } from 'react';
import { CONTATO, COPY, PLACEHOLDERS } from '../../constants/copy';
import { enviarContatoPublico } from '../../services/api/contatoApi';
import { extractApiMessage } from '../../utils/apiError';
import FeedbackModal from '../common/FeedbackModal';
import './contact-form.css';

interface ContactFormProps {
  compact?: boolean;
}

const ContactForm: React.FC<ContactFormProps> = ({ compact = false }) => {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [telefone, setTelefone] = useState('');
  const [instituicao, setInstituicao] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [feedback, setFeedback] = useState({ open: false, success: false, message: '' });

  const enviar = async (e: FormEvent) => {
    e.preventDefault();
    if (!nome.trim() || !email.trim() || !mensagem.trim()) {
      setFeedback({ open: true, success: false, message: 'Preencha nome, e-mail e mensagem.' });
      return;
    }
    setEnviando(true);
    try {
      const r = await enviarContatoPublico({
        nome: nome.trim(),
        email: email.trim(),
        telefone: telefone.trim() || undefined,
        instituicao: instituicao.trim() || undefined,
        mensagem: mensagem.trim(),
      });
      setFeedback({ open: true, success: true, message: r.data.message });
      setNome('');
      setEmail('');
      setTelefone('');
      setInstituicao('');
      setMensagem('');
    } catch (err) {
      setFeedback({
        open: true,
        success: false,
        message: extractApiMessage(err, 'Não foi possível enviar sua mensagem. Tente novamente.'),
      });
    } finally {
      setEnviando(false);
    }
  };

  return (
    <>
      <form className={`contact-form${compact ? ' contact-form--compact' : ''}`} onSubmit={enviar}>
        <div className="contact-form__grid">
          <div>
            <label htmlFor="contato-nome">Nome</label>
            <input
              id="contato-nome"
              type="text"
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              placeholder="Seu nome completo"
              required
            />
          </div>
          <div>
            <label htmlFor="contato-email">E-mail</label>
            <input
              id="contato-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder={PLACEHOLDERS.email}
              required
            />
          </div>
          <div>
            <label htmlFor="contato-telefone">Telefone (opcional)</label>
            <input
              id="contato-telefone"
              type="tel"
              value={telefone}
              onChange={(e) => setTelefone(e.target.value)}
              placeholder={PLACEHOLDERS.telefone}
            />
          </div>
          <div>
            <label htmlFor="contato-instituicao">Instituição (opcional)</label>
            <input
              id="contato-instituicao"
              type="text"
              value={instituicao}
              onChange={(e) => setInstituicao(e.target.value)}
              placeholder={PLACEHOLDERS.instituicao}
            />
          </div>
        </div>
        <div>
          <label htmlFor="contato-mensagem">Mensagem</label>
          <textarea
            id="contato-mensagem"
            rows={compact ? 4 : 5}
            value={mensagem}
            onChange={(e) => setMensagem(e.target.value)}
            placeholder={PLACEHOLDERS.mensagemContato}
            required
          />
        </div>
        <button type="submit" className="contact-form__submit" disabled={enviando}>
          {enviando ? 'Enviando...' : 'Enviar mensagem'}
        </button>
        <p className="contact-form__nota">{COPY.contatoRetorno} Horário: {CONTATO.horario}.</p>
      </form>

      <FeedbackModal
        open={feedback.open}
        success={feedback.success}
        message={feedback.message}
        onClose={() => setFeedback((f) => ({ ...f, open: false }))}
      />
    </>
  );
};

export default ContactForm;
