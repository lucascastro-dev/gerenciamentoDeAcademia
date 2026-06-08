import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import { LayoutHome } from "./components/TelaInicial/Layout";
import TelaInicial from "./components/TelaInicial/TelaInicial";
import Cadastro from "./components/TelaLogin/Cadastro";
import EsqueciSenha from "./components/TelaLogin/EsqueciSenha";
import { LayoutLogin } from "./components/TelaLogin/Layout";
import Login from "./components/TelaLogin/Login";
import "./index.css";
import "./theme/global.css";
import "./theme/programacao.css";
import "./theme/turmas.css";
import ProtectedRoute from "./ProtectedRoute";
import PermissaoRoute from "./components/common/PermissaoRoute";
import GerenciarFuncionario from "./components/TelaInicial/GerenciarFuncionario";
import GerenciarAcademia from "./components/TelaInicial/GerenciarAcademia";
import DashboardAdmin from "./components/Dashboard/DashboardAdmin";
import DashboardFinanceiro from "./components/Dashboard/DashboardFinanceiro";
import GeradorCertificados from "./components/Professores/GeradorCertificados";
import GerenciarAlunos from "./components/TelaInicial/GerenciarAlunos";
import GerenciarTurmas from "./components/TelaInicial/GerenciarTurmas";
import FinanceiroLayout from "./components/Financeiro/FinanceiroLayout";
import Mensalidades from "./components/Financeiro/Mensalidades";
import Inadimplencia from "./components/Financeiro/Inadimplencia";
import TurmasProfessor from "./components/Professor/TurmasProfessor";
import PresencaTurma from "./components/Professor/PresencaTurma";
import GestaoCadastros from "./components/Administrativo/GestaoCadastros";
import GestaoAcademias from "./components/Administrativo/GestaoAcademias";
import MatriculaAluno from "./components/Academico/MatriculaAluno";
import MeuPerfil from "./pages/MeuPerfil";
import CadastrarInstituicao from "./pages/CadastrarInstituicao";
import Auditoria from "./pages/Auditoria";
import PagamentosPendentes from "./pages/PagamentosPendentes";
import PlanosExpirados from "./pages/PlanosExpirados";
import PlanoInstituicao from "./pages/PlanoInstituicao";
import PlanoInstituicaoGuard from "./components/common/PlanoInstituicaoGuard";
import PortalAlunoSenha from "./pages/portal-aluno/PortalAlunoSenha";
import PortalAlunoProgramacao from "./pages/portal-aluno/PortalAlunoProgramacao";
import PortalAlunoDados from "./pages/portal-aluno/PortalAlunoDados";
import PortalAlunoTurmas from "./pages/portal-aluno/PortalAlunoTurmas";
import PortalAlunoMensalidades from "./pages/portal-aluno/PortalAlunoMensalidades";
import GestaoProgramacao from "./pages/academico/GestaoProgramacao";

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);

root.render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/areapublica/login" replace />} />

        <Route path="areapublica" element={<LayoutLogin />}>
          <Route path="login" element={<Login />} />
          <Route path="cadastro" element={<Cadastro />} />
          <Route path="esqueciSenha" element={<EsqueciSenha />} />
          <Route path="solicitarAcesso" element={<Navigate to="/areapublica/cadastro" replace />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route path="portal-aluno/*" element={<Navigate to="/arealogada/home" replace />} />
          <Route path="arealogada" element={<LayoutHome />}>
            <Route element={<PlanoInstituicaoGuard />}>
            <Route index element={<Navigate to="home" replace />} />
            <Route path="home" element={<TelaInicial />} />

            <Route path="aluno/dados" element={
              <PermissaoRoute permissao="aluno-portal:dados"><PortalAlunoDados /></PermissaoRoute>
            } />
            <Route path="aluno/turmas" element={
              <PermissaoRoute permissao="aluno-portal:turmas"><PortalAlunoTurmas /></PermissaoRoute>
            } />
            <Route path="aluno/mensalidades" element={
              <PermissaoRoute permissao="aluno-portal:mensalidades"><PortalAlunoMensalidades /></PermissaoRoute>
            } />
            <Route path="aluno/senha" element={
              <PermissaoRoute permissao="aluno-portal:senha"><PortalAlunoSenha /></PermissaoRoute>
            } />
            <Route path="aluno/programacao" element={
              <PermissaoRoute permissao="aluno-portal:programacao"><PortalAlunoProgramacao /></PermissaoRoute>
            } />
            <Route path="meu-perfil" element={<MeuPerfil />} />

            <Route path="dashboard" element={
              <PermissaoRoute permissao="dashboard:visualizar"><DashboardAdmin /></PermissaoRoute>
            } />

            <Route path="financeiro" element={<FinanceiroLayout />}>
              <Route index element={<DashboardFinanceiro />} />
              <Route path="pendentes" element={
                <PermissaoRoute somenteMaster><PagamentosPendentes /></PermissaoRoute>
              } />
              <Route path="planos-expirados" element={
                <PermissaoRoute somenteMaster><PlanosExpirados /></PermissaoRoute>
              } />
              <Route path="mensalidades" element={
                <PermissaoRoute permissao="financeiro:visualizar"><Mensalidades /></PermissaoRoute>
              } />
              <Route path="inadimplencia" element={
                <PermissaoRoute permissao="financeiro:relatorio"><Inadimplencia /></PermissaoRoute>
              } />
            </Route>

            <Route path="professor/turmas" element={
              <PermissaoRoute permissao="turma:consultar"><TurmasProfessor /></PermissaoRoute>
            } />
            <Route path="professor/alunos" element={
              <Navigate to="/arealogada/professor/turmas" replace />
            } />
            <Route path="professor/certificados" element={
              <PermissaoRoute permissao="certificado:gerar"><GeradorCertificados /></PermissaoRoute>
            } />
            <Route path="professor/presenca" element={
              <PermissaoRoute permissao="turma:presenca"><PresencaTurma /></PermissaoRoute>
            } />

            <Route path="alunos" element={
              <PermissaoRoute permissao="aluno:consultar"><GerenciarAlunos /></PermissaoRoute>
            } />
            <Route path="turmas" element={
              <PermissaoRoute permissao="turma:consultar"><GerenciarTurmas modo="consulta" /></PermissaoRoute>
            } />
            <Route path="turmas/gerenciar" element={
              <PermissaoRoute permissao="turma:gerenciar"><GerenciarTurmas modo="gerenciar" /></PermissaoRoute>
            } />
            <Route path="programacao" element={
              <PermissaoRoute permissao="programacao:consultar"><GestaoProgramacao /></PermissaoRoute>
            } />
            <Route path="plano-instituicao" element={
              <PermissaoRoute permissao="plano:visualizar"><PlanoInstituicao /></PermissaoRoute>
            } />
            <Route path="matricula" element={
              <PermissaoRoute permissao="aluno:matricular"><MatriculaAluno /></PermissaoRoute>
            } />

            <Route path="instituicoes" element={
              <PermissaoRoute somenteMaster><GerenciarAcademia /></PermissaoRoute>
            } />
            <Route path="academias" element={<Navigate to="/arealogada/instituicoes" replace />} />
            <Route path="cadastrar-instituicao" element={
              <PermissaoRoute somenteMaster><CadastrarInstituicao /></PermissaoRoute>
            } />
            <Route path="funcionarios" element={
              <PermissaoRoute permissao="funcionario:consultar"><GerenciarFuncionario /></PermissaoRoute>
            } />
            <Route path="certificados" element={<Navigate to="/arealogada/professor/certificados" replace />} />
            <Route path="gestaoCadastro" element={
              <PermissaoRoute permissao="funcionario:ativar"><GestaoCadastros /></PermissaoRoute>
            } />
            <Route path="gestaoAcademia" element={
              <PermissaoRoute somenteMaster><GestaoAcademias /></PermissaoRoute>
            } />
            <Route path="auditoria" element={
              <PermissaoRoute somenteMaster><Auditoria /></PermissaoRoute>
            } />
            </Route>
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);
