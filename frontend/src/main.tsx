import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import { LayoutHome } from "./components/TelaInicial/Layout";
import TelaInicial from "./components/TelaInicial/TelaInicial";
import Cadastro from "./components/TelaLogin/Cadastro";
import EsqueciSenha from "./components/TelaLogin/EsqueciSenha";
import { LayoutLogin } from "./components/TelaLogin/Layout";
import Login from "./components/TelaLogin/Login";
import SolicitarAcesso from "./components/TelaLogin/SolicitarAcesso";
import "./index.css";
import ProtectedRoute from "./ProtectedRoute";
import GerenciarFuncionario from "./components/TelaInicial/GerenciarFuncionario";

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
          <Route path="solicitarAcesso" element={<SolicitarAcesso />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route path="arealogada" element={<LayoutHome />}>
            <Route index element={<Navigate to="home" replace />} />
            <Route path="home" element={<TelaInicial />} />

            <Route path="academias" element={<div>Tela de Academias</div>} />
            <Route path="funcionarios" element={<GerenciarFuncionario/>} />
            <Route path="alunos" element={<div>Tela de Alunos</div>} />
            <Route path="turmas" element={<div>Tela de Turmas</div>} />
            <Route path="certificados" element={<div>Gerador de Certificados</div>} />
            <Route path="financeiro" element={<div>Financeiro</div>} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);