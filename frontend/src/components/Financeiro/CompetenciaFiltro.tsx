const MESES = [
  'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
  'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro',
];

interface CompetenciaFiltroProps {
  mes: string;
  ano: string;
  onMesChange: (mes: string) => void;
  onAnoChange: (ano: string) => void;
  idPrefix?: string;
}

const CompetenciaFiltro: React.FC<CompetenciaFiltroProps> = ({
  mes,
  ano,
  onMesChange,
  onAnoChange,
  idPrefix = 'competencia',
}) => {
  const anoAtual = new Date().getFullYear();
  const anos = [anoAtual, anoAtual - 1, anoAtual - 2];

  return (
    <div>
      <label htmlFor={`${idPrefix}-mes`}>Competência</label>
      <div className="fin-op__competencia">
        <select id={`${idPrefix}-mes`} value={mes} onChange={(e) => onMesChange(e.target.value)}>
          {MESES.map((nome, i) => (
            <option key={nome} value={String(i + 1)}>{nome}</option>
          ))}
        </select>
        <select id={`${idPrefix}-ano`} value={ano} onChange={(e) => onAnoChange(e.target.value)}>
          {anos.map((a) => (
            <option key={a} value={a}>{a}</option>
          ))}
        </select>
      </div>
    </div>
  );
};

export default CompetenciaFiltro;
