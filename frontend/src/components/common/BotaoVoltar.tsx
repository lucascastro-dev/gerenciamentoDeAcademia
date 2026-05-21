import { useNavigate } from 'react-router-dom';

interface Props {
  label?: string;
}

const BotaoVoltar: React.FC<Props> = ({ label = 'Voltar' }) => {
  const navigate = useNavigate();
  return (
    <button type="button" className="fab-voltar" onClick={() => navigate(-1)} aria-label={label}>
      ← {label}
    </button>
  );
};

export default BotaoVoltar;
