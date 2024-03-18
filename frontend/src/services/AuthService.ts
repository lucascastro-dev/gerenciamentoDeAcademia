import axios from 'axios';

const BASE_URL = 'http://localhost:8080/auth';

const AuthService = {
    login: async (login: string, password: string) => {
        try {
            const response = await axios.post(`${BASE_URL}/login`, { login, password });
            console.log(response)
            return response.data.token;
        } catch (error) {
            console.log('Falha na autenticação', error);
            throw new Error('Falha na autenticação');
        }
    },

    cadastrar: async (data: { login: string; password: string; role: string }): Promise<void> => {
        try {
            await axios.post(`${BASE_URL}/cadastrar`, data);
        } catch (error) {
            console.log('Erro ao cadastrar', error);
            throw new Error('Erro ao cadastrar');
        }
    },
};

export default AuthService;
