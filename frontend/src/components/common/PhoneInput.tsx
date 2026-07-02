import { useEffect, useId, useRef, useState } from 'react';
import { PLACEHOLDERS } from '../../constants/copy';
import {
  PAIS_PADRAO,
  PAISES_TELEFONE,
  PaisTelefone,
  detectarPais,
  extrairDigitosNacionais,
  formatarParteNacional,
  formatarTelefoneExibicao,
  telefoneMascarado,
  telefoneParaApi,
} from '../../utils/phoneFormat';
import './PhoneFields.css';

interface Props {
  id?: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  disabled?: boolean;
  readOnly?: boolean;
  placeholder?: string;
  autoComplete?: string;
  labelClassName?: string;
}

const PhoneInput: React.FC<Props> = ({
  id,
  label,
  value,
  onChange,
  required = false,
  disabled = false,
  readOnly = false,
  placeholder = PLACEHOLDERS.telefone,
  autoComplete = 'tel',
  labelClassName,
}) => {
  const generatedId = useId();
  const inputId = id || `phone-${generatedId}`;
  const [pais, setPais] = useState<PaisTelefone>(PAIS_PADRAO);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const wrapRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!value || telefoneMascarado(value)) return;
    setPais(detectarPais(value));
  }, [value]);

  useEffect(() => {
    const onClickOutside = (e: MouseEvent) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target as Node)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  const somenteLeitura = readOnly || disabled;
  const mascarado = telefoneMascarado(value);

  if (somenteLeitura && mascarado) {
    return (
      <div className="phone-field">
        <label className={labelClassName || 'phone-field__label'} htmlFor={inputId}>
          {label}
        </label>
        <input
          id={inputId}
          className="phone-field__readonly"
          value={value}
          readOnly
          disabled
        />
      </div>
    );
  }

  const nacionalExibicao = mascarado
    ? value
    : formatarParteNacional(telefoneParaApi(value || formatarTelefoneExibicao(value)));

  const emitir = (novoPais: PaisTelefone, digitosNacionais: string) => {
    const parte = formatarParteNacional(digitosNacionais);
    onChange(parte ? `+${novoPais.dial} ${parte}` : '');
  };

  const onInputChange = (raw: string) => {
    const digitos = extrairDigitosNacionais(raw.replace(/\D/g, ''), pais.dial);
    emitir(pais, digitos);
  };

  const selecionarPais = (novoPais: PaisTelefone) => {
    setPais(novoPais);
    setDropdownOpen(false);
    const nacional = telefoneParaApi(value);
    if (nacional) {
      onChange(`+${novoPais.dial} ${formatarParteNacional(nacional)}`);
    }
  };

  return (
    <div className="phone-field" ref={wrapRef}>
      <label className={labelClassName || 'phone-field__label'} htmlFor={inputId}>
        {label}
      </label>
      <div className={`phone-field__wrap${somenteLeitura ? ' phone-field__wrap--disabled' : ''}`}>
        <div className="phone-field__country">
          <button
            type="button"
            className="phone-field__country-btn"
            onClick={() => !somenteLeitura && setDropdownOpen((o) => !o)}
            disabled={somenteLeitura}
            aria-label="Selecionar país"
            aria-expanded={dropdownOpen}
            aria-haspopup="listbox"
          >
            <span className="phone-field__flag" aria-hidden>{pais.flag}</span>
            <span className="phone-field__dial">+{pais.dial}</span>
            {!somenteLeitura && (
              <svg className="phone-field__chevron" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden>
                <polyline points="6 9 12 15 18 9" />
              </svg>
            )}
          </button>
          {dropdownOpen && (
            <ul className="phone-field__dropdown" role="listbox">
              {PAISES_TELEFONE.map((p) => (
                <li key={p.code}>
                  <button
                    type="button"
                    role="option"
                    aria-selected={p.code === pais.code}
                    className={`phone-field__dropdown-item${p.code === pais.code ? ' phone-field__dropdown-item--active' : ''}`}
                    onClick={() => selecionarPais(p)}
                  >
                    <span aria-hidden>{p.flag}</span>
                    <span>{p.name}</span>
                    <span className="phone-field__dial">+{p.dial}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
        <input
          id={inputId}
          className="phone-field__input"
          type="tel"
          inputMode="tel"
          autoComplete={autoComplete}
          required={required}
          disabled={somenteLeitura}
          readOnly={readOnly}
          placeholder={placeholder}
          value={nacionalExibicao}
          onChange={(e) => onInputChange(e.target.value)}
        />
      </div>
    </div>
  );
};

export default PhoneInput;
