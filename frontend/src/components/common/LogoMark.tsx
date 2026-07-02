import { APP_NAME } from '../../constants/branding';
import './LogoMark.css';

interface LogoMarkProps {
  size?: 'sm' | 'md' | 'lg';
  showName?: boolean;
  className?: string;
}

const LogoMark: React.FC<LogoMarkProps> = ({
  size = 'md',
  showName = true,
  className = '',
}) => (
  <div className={`logo-mark logo-mark--${size} ${className}`.trim()} aria-label={APP_NAME}>
    <span className="logo-mark__badge" aria-hidden="true">
      <span className="logo-mark__ring" />
      <span className="logo-mark__core">360</span>
    </span>
    {showName && (
      <span className="logo-mark__name">
        Turma<span className="logo-mark__name-accent">360</span>
      </span>
    )}
  </div>
);

export default LogoMark;
