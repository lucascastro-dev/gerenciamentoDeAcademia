import React from "react";
import ReactDOM from "react-dom/client";

import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";

import "./index.css";
import Login from "./components/Login";
import { Layout } from "./components/Layout";
import Cadastro from "./components/Cadastro";
import SolicitarAcesso from "./components/SolicitarAcesso";
import EsqueciSenha from "./components/EsqueciSenha";

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);

root.render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/arealogada/login" replace />} />


        <Route path="arealogada" element={<Layout />}>
          <Route path="login" element={<Login />} />
          <Route path="cadastro" element={<Cadastro />} />
          <Route path="esqueciSenha" element={<EsqueciSenha />} />
          <Route path="solicitarAcesso" element={<SolicitarAcesso />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
);