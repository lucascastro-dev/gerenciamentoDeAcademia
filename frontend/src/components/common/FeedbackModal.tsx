interface Props {
  open: boolean;
  success: boolean;
  title?: string;
  message: string;
  onClose: () => void;
}

const FeedbackModal: React.FC<Props> = ({ open, success, title, message, onClose }) => {
  if (!open) return null;
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className={`modal-content modal-content--${success ? 'success' : 'error'}`}>
        <h3>{title ?? (success ? 'Sucesso' : 'Atenção')}</h3>
        <p>{message}</p>
        <button type="button" className={success ? 'btn-primary' : 'btn-danger'} onClick={onClose}>
          Fechar
        </button>
      </div>
    </div>
  );
};

export default FeedbackModal;
