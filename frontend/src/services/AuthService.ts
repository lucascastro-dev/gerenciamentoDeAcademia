import axios from 'axios';

const BASE_URL = 'http://localhost:8080/auth';

const AuthService = {
    login: (login: string, password: string) => {
            return axios.post(`${BASE_URL}/login`, { login, password });
    },

    cadastrar: (data: { login: string; password: string; role: string }) => {
        return axios.post(`${BASE_URL}/cadastrar`, data);
    },
};

export default AuthService;
