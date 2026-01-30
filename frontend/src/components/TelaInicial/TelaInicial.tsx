import { useEffect, useState, type FC } from 'react';
import HttpService from '../../services/HttpService';
import { TelaInicialWrapper } from './TelaInicial.styled';

const TelaInicial: FC = () => {
   const [vinculo, setVinculo] = useState<string | null>(null);
   const [academia, setAcademia] = useState<any>(null);
   const [usuario, setUsuario] = useState<any>(null);
   const [loading, setLoading] = useState(true);

   useEffect(() => {
      const savedCpf = localStorage.getItem('@App:cpf');
      const savedVinculo = localStorage.getItem('@App:vinculo');
      const token = localStorage.getItem('@App:token');

      if (savedVinculo) setVinculo(savedVinculo);

      const carregarDados = async () => {
         try {
            const token = localStorage.getItem('@App:token');
            const savedVinculo = localStorage.getItem('@App:vinculo');
            const savedCpf = localStorage.getItem('@App:cpf');

            if (savedVinculo && token) {
               const resAcademia = await HttpService.consultarAcademia(savedVinculo, token);
               setAcademia(resAcademia.data);

               if (savedCpf) {
                  const resUsuario = await HttpService.consultarFuncionarioPorCpf(savedCpf, token);
                  setUsuario(resUsuario.data);
               }
            }
         } catch (error) {
            console.error("Erro ao carregar dados:", error);
         } finally {
            setLoading(false);
         }
      };

      if (savedCpf && savedVinculo) {
         carregarDados();
      }
   }, []);

   if (loading) return <TelaInicialWrapper><p>Carregando...</p></TelaInicialWrapper>;

   return (
      <TelaInicialWrapper>
         <h1>Bem-vindo à Gestão de Academias Inteligente</h1>

         <div className="info-box">
            <p><strong>Funcionário:</strong> {usuario?.nome}</p>
            <p><strong>Academia:</strong> {academia?.razaoSocial} (Cod: {vinculo})</p>
         </div>

         <button onClick={() => {
            localStorage.clear();
            window.location.href = '/';
         }}>
            Sair / Logout
         </button>
      </TelaInicialWrapper>
   );
};

export default TelaInicial;