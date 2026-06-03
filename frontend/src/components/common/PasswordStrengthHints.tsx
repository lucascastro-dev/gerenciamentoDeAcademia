import { PASSWORD_RULES, avaliarSenha } from '../../utils/passwordPolicy';
import './PasswordFields.css';

interface Props {
  password: string;
  idPrefix?: string;
}

const PasswordStrengthHints: React.FC<Props> = ({ password, idPrefix = 'senha' }) => {
  const status = avaliarSenha(password);
  const todasOk = PASSWORD_RULES.every((r) => status[r.id]);

  return (
    <div
      className="password-strength"
      role="status"
      aria-live="polite"
      aria-label="Requisitos de senha forte"
    >
      <p className="password-strength__title">
        {password.length === 0
          ? 'A senha deve atender todos os critérios abaixo:'
          : todasOk
            ? 'Senha forte — todos os critérios atendidos.'
            : 'Para uma senha forte, ainda falta:'}
      </p>
      <ul className="password-strength__list">
        {PASSWORD_RULES.map((rule) => {
          const ok = status[rule.id];
          return (
            <li
              key={rule.id}
              id={`${idPrefix}-rule-${rule.id}`}
              className={ok ? 'password-strength__item--ok' : 'password-strength__item--pending'}
            >
              <span className="password-strength__icon" aria-hidden>
                {ok ? '✓' : '○'}
              </span>
              {rule.label}
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export default PasswordStrengthHints;
