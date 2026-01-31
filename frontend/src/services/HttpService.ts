import axios from 'axios';

const BASE_URL = 'http://localhost:8000/srv-gerenciaracademia';

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

    consultarAcademia: (codAcademia: string | number, token: string) => {
        return axios.get(`${BASE_URL}/academia/consultarAcademiaId/${codAcademia}`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    },

    consultarFuncionarioPorCpf: (cpf: string, token: string) => {
        return axios.get(`${BASE_URL}/funcionario/consultarPorCpf/${cpf}`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    },

    editarPessoa: (data: { nome: string, cpf: string, rg: string, dataDeNascimento: string, endereco: string, telefone: string, cargo: string, especializacao: string, permitirGerenciarFuncoes: boolean }, token: string) => {
        return axios.put(`${BASE_URL}/funcionario/editarFuncionario`, data, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    },

    consultarAcademiaPorCnpj: (cnpj: string, token: string) => {
        return axios.get(`${BASE_URL}/academia/consultarAcademiaCnpj/${cnpj}`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    },

    editarAcademia: (data: { razaoSocial: string, cnpj: string, endereco: string, telefone: string, cadastroAtivo: boolean }, token: string) => {
        return axios.put(`${BASE_URL}/academia/atualizarDadosAcademia`, data, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    }
};

export default HttpService;
