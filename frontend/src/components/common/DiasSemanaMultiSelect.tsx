import './DiasSemanaMultiSelect.css';

const DIAS_OPCOES = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'] as const;

interface Props {
  value: string[];
  onChange: (dias: string[]) => void;
  disabled?: boolean;
}

const DiasSemanaMultiSelect: React.FC<Props> = ({ value, onChange, disabled }) => {
  return (
    <div className="dias-multiselect">
      <label className="dias-multiselect__label">Dias de aula</label>
      <p className="field-hint dias-multiselect__hint">Segure Ctrl (Windows) ou Cmd (Mac) para selecionar vários dias.</p>
      <select
        multiple
        size={6}
        className="dias-multiselect__select"
        value={value}
        disabled={disabled}
        onChange={(e) => {
          const selected = Array.from(e.target.selectedOptions).map((o) => o.value);
          onChange(selected);
        }}
      >
        {DIAS_OPCOES.map((d) => (
          <option key={d} value={d}>{d}</option>
        ))}
      </select>
      {value.length > 0 && (
        <p className="dias-multiselect__resumo">
          Selecionados: <strong>{value.join(', ')}</strong>
        </p>
      )}
    </div>
  );
};

export { DIAS_OPCOES };
export default DiasSemanaMultiSelect;
