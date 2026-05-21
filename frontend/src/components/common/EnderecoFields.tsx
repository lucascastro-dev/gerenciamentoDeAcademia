import { useState } from 'react';
import { buscarCep } from '../../services/viacep';
import { EnderecoCompleto } from '../../utils/endereco';

interface Props {
  value: EnderecoCompleto;
  onChange: (v: EnderecoCompleto) => void;
  disabled?: boolean;
}

const EnderecoFields: React.FC<Props> = ({ value, onChange, disabled }) => {
  const [buscando, setBuscando] = useState(false);
  const [erroCep, setErroCep] = useState('');

  const upd = (k: keyof EnderecoCompleto, v: string) => onChange({ ...value, [k]: v });

  const maskCep = (v: string) =>
    v.replace(/\D/g, '').replace(/^(\d{5})(\d)/, '$1-$2').slice(0, 9);

  const onCepBlur = async () => {
    const digits = value.cep.replace(/\D/g, '');
    if (digits.length !== 8) return;
    setBuscando(true);
    setErroCep('');
    try {
      const data = await buscarCep(digits);
      if (!data) {
        setErroCep('CEP não encontrado.');
        return;
      }
      onChange({
        ...value,
        cep: maskCep(data.cep),
        logradouro: data.logradouro || value.logradouro,
        bairro: data.bairro || value.bairro,
        cidade: data.localidade || value.cidade,
        uf: data.uf || value.uf,
        complemento: value.complemento || data.complemento || '',
      });
    } catch {
      setErroCep('Falha ao consultar CEP. Tente novamente.');
    } finally {
      setBuscando(false);
    }
  };

  return (
    <div className="endereco-fields">
      <div className="form-grid">
        <div>
          <label>CEP</label>
          <input
            value={value.cep}
            disabled={disabled}
            placeholder="00000-000"
            onChange={(e) => upd('cep', maskCep(e.target.value))}
            onBlur={onCepBlur}
          />
          {buscando && <span className="field-hint">Buscando CEP...</span>}
          {erroCep && <span className="field-hint field-hint--err">{erroCep}</span>}
        </div>
        <div className="form-grid__span-2">
          <label>Logradouro</label>
          <input
            value={value.logradouro}
            disabled={disabled}
            onChange={(e) => upd('logradouro', e.target.value)}
          />
        </div>
        <div>
          <label>Número</label>
          <input value={value.numero} disabled={disabled} onChange={(e) => upd('numero', e.target.value)} />
        </div>
        <div>
          <label>Complemento</label>
          <input value={value.complemento} disabled={disabled} onChange={(e) => upd('complemento', e.target.value)} />
        </div>
        <div>
          <label>Bairro</label>
          <input value={value.bairro} disabled={disabled} onChange={(e) => upd('bairro', e.target.value)} />
        </div>
        <div>
          <label>Município</label>
          <input value={value.cidade} disabled={disabled} onChange={(e) => upd('cidade', e.target.value)} />
        </div>
        <div>
          <label>UF</label>
          <input
            value={value.uf}
            disabled={disabled}
            maxLength={2}
            onChange={(e) => upd('uf', e.target.value.toUpperCase())}
          />
        </div>
      </div>
    </div>
  );
};

export default EnderecoFields;
