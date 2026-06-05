import { mascaraMoedaInput, parseMoeda } from '../../utils/moeda';

interface Props {
  value: string;
  onChange: (valorNumerico: number, valorFormatado: string) => void;
  disabled?: boolean;
  required?: boolean;
  placeholder?: string;
}

const CurrencyInput: React.FC<Props> = ({
  value,
  onChange,
  disabled,
  required,
  placeholder = 'R$ 0,00',
}) => (
  <input
    type="text"
    inputMode="decimal"
    value={value}
    disabled={disabled}
    required={required}
    placeholder={placeholder}
    onChange={(e) => {
      const formatado = mascaraMoedaInput(e.target.value);
      onChange(parseMoeda(formatado), formatado);
    }}
  />
);

export default CurrencyInput;
