import React from "react";
import ReactDOM from "react-dom/client";

import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";

import "./index.css";
import Login from "./components/TelaLogin/Login";
import { Layout } from "./components/TelaLogin/Layout";
import Cadastro from "./components/TelaLogin/Cadastro";
import SolicitarAcesso from "./components/TelaLogin/SolicitarAcesso";
import EsqueciSenha from "./components/TelaLogin/EsqueciSenha";
import TelaInicial from "./components/TelaInicial/TelaInicial";
import ProtectedRoute from "./ProtectedRoute";

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);

root.render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/areapublica/login" replace />} />
        
        <Route path="areapublica" element={<Layout />}>
          <Route path="login" element={<Login />} />
          <Route path="cadastro" element={<Cadastro />} />
          <Route path="esqueciSenha" element={<EsqueciSenha />} />
          <Route path="solicitarAcesso" element={<SolicitarAcesso />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route path="arealogada" element={<Layout />}>
            <Route path="home" element={<TelaInicial />} />
          </Route>
        </Route>

      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);