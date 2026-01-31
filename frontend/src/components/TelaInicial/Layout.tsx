import { useEffect, useState } from 'react';
import { NavLink, Outlet } from "react-router-dom";
import styled from "styled-components";
import HttpService from '../../services/HttpService';

const LayoutContainer = styled.div`
  display: grid;
  grid-template-columns: 250px 1fr; 
  grid-template-rows: 70px 1fr;
  grid-template-areas: 
    "sidebar header"
    "sidebar main";
  height: 100vh;
  width: 100vw;
`;

const Sidebar = styled.aside`
  grid-area: sidebar;
  background-color: #343746;
  color: white;
  display: flex;
  flex-direction: column;
  padding: 20px 0;
`;

const Header = styled.header`
  grid-area: header;
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 25px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  z-index: 10;
`;

const Main = styled.main`
  grid-area: main;
  background-color: #f5f6fa;
  padding: 25px;
  overflow-y: auto;
`;

const MenuLink = styled(NavLink)`
  background: transparent;
  color: #cfd0d7;
  text-decoration: none;
  padding: 15px 25px;
  display: block;
  font-size: 16px;

  &.active {
    background-color: #404454;
    color: white;
    border-left: 4px solid #cfd0d7;
  }

  &:hover {
    background-color: #404454;
    color: white;
  }
`;

const UserMenuContainer = styled.div`
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover {
    background-color: #f5f5f5;
  }
`;

const UserInfoText = styled.div`
  text-align: right; 
  margin-right: 12px;
  font-size: 13px;
  line-height: 1.4;
  color: #333;

  strong {
    font-weight: 600;
    color: #000;
    display: block;
  }
`;

const DropdownMenu = styled.div<{ $isOpen: boolean }>`
  display: ${props => (props.$isOpen ? 'block' : 'none')};
  position: absolute;
  top: 110%;
  right: 0;
  background-color: white;
  min-width: 160px;
  box-shadow: 0px 8px 16px rgba(0,0,0,0.1);
  border-radius: 6px;
  border: 1px solid #eee;
  z-index: 100;
  overflow: hidden;
`;

const DropdownItem = styled.button`
  width: 100%;
  padding: 12px 16px;
  text-align: right;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 14px;
  color: #ff4d4d;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    background-color: #fff5f5;
  }
`;

const Avatar = styled.div`
  width: 35px;
  height: 35px;
  background-color: #343746;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
`;

export const LayoutHome: React.FC = () => {
  const [usuario, setUsuario] = useState<any>(null);
  const [academia, setAcademia] = useState<any>(null);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  useEffect(() => {
    const savedCpf = localStorage.getItem('@App:cpf');
    const savedVinculo = localStorage.getItem('@App:vinculo');

    if (savedVinculo) setAcademia(savedVinculo);

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
      }
    };

    if (savedCpf && savedVinculo) {
      carregarDados();
    }

  }, []);

  const handleLogout = () => {
    localStorage.clear();
    window.location.href = '/';
  };

  return (
    <LayoutContainer>
      <Sidebar>
        <div style={{ padding: '20px', textAlign: 'center' }}>
          {/* <img src="/logo-sua.png" alt="Logo" style={{ maxWidth: '100%' }} /> */}
        </div>
        <MenuLink to="home">Início</MenuLink>
        {/* {usuario?.cargo === 'Mestre' && */}
        <MenuLink to="academias">Gerenciar Academias</MenuLink>
        {usuario?.permitirGerenciarFuncoes &&
        <MenuLink to="funcionarios">Gerenciar Funcionarios</MenuLink>}
        <MenuLink to="alunos">Gerenciar Alunos</MenuLink>
        <MenuLink to="turmas">Gerenciar Turmas</MenuLink>
        <MenuLink to="certificados">Gerador de Certificados</MenuLink>
        <MenuLink to="gestaoCadastro">Ativar/Inativar Cadastros</MenuLink>
        <MenuLink to="gestaoAcademia">Ativar/Inativar Academias</MenuLink>
        {usuario?.permitirGerenciarFuncoes &&
        <MenuLink to="financeiro">Financeiro</MenuLink>}
      </Sidebar>

      <Header>
        <div />
        <UserMenuContainer onClick={() => setIsDropdownOpen(!isDropdownOpen)}>
          <UserInfoText>
            <strong>{usuario?.nome || 'Carregando...'}</strong>
            <span>{academia?.razaoSocial || 'Empresa'}</span>
          </UserInfoText>

          <Avatar>
            {usuario?.nome?.charAt(0).toUpperCase() || 'U'}
          </Avatar>

          <DropdownMenu $isOpen={isDropdownOpen}>
            <DropdownItem onClick={handleLogout}>
              Sair da Conta
            </DropdownItem>
          </DropdownMenu>
        </UserMenuContainer>
      </Header>

      <Main onClick={() => setIsDropdownOpen(false)}>
        <Outlet />
      </Main>
    </LayoutContainer>
  );
};