import { useState } from 'react';
import './PasswordFields.css';

interface Props {
  id?: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  autoComplete?: string;
  showStrength?: boolean;
  strengthHints?: React.ReactNode;
}

const PasswordInput: React.FC<Props> = ({
  id,
  label,
  value,
  onChange,
  placeholder = 'Digite sua senha',
  autoComplete = 'current-password',
  showStrength = false,
  strengthHints,
}) => {
  const [visible, setVisible] = useState(false);
  const inputId = id || `pwd-${label.replace(/\s/g, '-').toLowerCase()}`;

  return (
    <div className="password-field">
      <label className="password-field__label" htmlFor={inputId}>
        {label}
      </label>
      <div className="password-field__wrap">
        <input
          id={inputId}
          type={visible ? 'text' : 'password'}
          value={value}
          placeholder={placeholder}
          autoComplete={autoComplete}
          onChange={(e) => onChange(e.target.value)}
          aria-describedby={showStrength && strengthHints ? `${inputId}-hints` : undefined}
        />
        <button
          type="button"
          className="password-field__toggle"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? 'Ocultar senha' : 'Mostrar senha'}
          aria-pressed={visible}
          tabIndex={0}
        >
          {visible ? (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
              <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
              <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
              <line x1="1" y1="1" x2="23" y2="23" />
            </svg>
          ) : (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
          )}
        </button>
      </div>
      {showStrength && strengthHints && (
        <div id={`${inputId}-hints`}>{strengthHints}</div>
      )}
    </div>
  );
};

export default PasswordInput;
