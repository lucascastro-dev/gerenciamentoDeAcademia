import { useEffect, useState } from 'react';
import HttpService from '../../services/HttpService';

interface InadimplenteItem {
  cpf: string;
  nome: string;
  diaVencimento: number;
}

const Inadimplencia: React.FC = () => {
  const [lista, setLista] = useState<InadimplenteItem[]>([]);

  useEffect(() => {
    HttpService.financeiroInadimplentes().then((r) => setLista(r.data));
  }, []);

  return (
    <div className="card">
      <h2 style={{ marginTop: 0 }}>Inadimplência</h2>
      {lista.length === 0 ? (
        <p>Nenhum aluno inadimplente no momento.</p>
      ) : (
        <ul>
          {lista.map((m) => (
            <li key={m.cpf}>
              <strong>{m.nome}</strong> — CPF {m.cpf} — vencimento dia {m.diaVencimento}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default Inadimplencia;
