import { alternarTema, type ThemeMode } from '../../theme/theme';

function IconSun() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <circle cx="12" cy="12" r="4" stroke="currentColor" strokeWidth="1.75" />
      <path
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinecap="round"
        d="M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"
      />
    </svg>
  );
}

function IconMoon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden>
      <path
        d="M20 14.5A8.5 8.5 0 0 1 9.5 4 7 7 0 1 0 20 14.5Z"
        stroke="currentColor"
        strokeWidth="1.75"
        strokeLinejoin="round"
      />
    </svg>
  );
}

interface Props {
  tema: ThemeMode;
  onToggle: (tema: ThemeMode) => void;
  className?: string;
}

const ThemeToggle: React.FC<Props> = ({ tema, onToggle, className = 'app-theme-toggle' }) => (
  <button
    type="button"
    className={className}
    title={tema === 'light' ? 'Ativar tema escuro' : 'Ativar tema claro'}
    aria-label={tema === 'light' ? 'Ativar tema escuro' : 'Ativar tema claro'}
    onClick={() => onToggle(alternarTema())}
  >
    {tema === 'light' ? <IconMoon /> : <IconSun />}
  </button>
);

export default ThemeToggle;
