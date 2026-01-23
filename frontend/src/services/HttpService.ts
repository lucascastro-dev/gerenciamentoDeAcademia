import axios from 'axios';

const BASE_URL = 'http://localhost:8080/srv-gerenciaracademia';

const HttpService = {
    login: (login: string, password: string, vinculo: string) => {
        return axios.post(`${BASE_URL}/login`, { login, password, vinculo });
    },

    cadastrarPessoa: (data: { nome: string, cpf: string, rg: string, dataDeNascimento: string, endereco: string, telefone: string, cargo: string, especializacao: string, permitirGerenciarFuncoes: boolean, senha: string }) => {
        return axios.post(`${BASE_URL}/funcionario/cadastrarFuncionario`, data);
    },

    cadastrarEmpresa: (data: { razaoSocial: string, cnpj: string, cadastroAtivo: boolean, endereco: string, telefone: string }) => {
        return axios.post(`${BASE_URL}/academia/registrarAcademia`, data);
    },

    vincularFuncionario: (data: { cnpj: string, cpf: string }) => {
        return axios.put(`${BASE_URL}/academia/solicitarPrimeiroAcesso/${data.cpf}/${data.cnpj}`, data);
    },
};

export default HttpService;
