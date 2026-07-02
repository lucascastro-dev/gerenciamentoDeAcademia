import { authApi } from './api/authApi';
import { instituicaoApi } from './api/instituicaoApi';
import { funcionarioApi } from './api/funcionarioApi';
import { alunoApi } from './api/alunoApi';
import { turmaApi } from './api/turmaApi';
import { financeiroApi } from './api/financeiroApi';
import { equipeApi } from './api/equipeApi';
import { programacaoApi, portalAlunoApi, plataformaApi } from './api/programacaoApi';
import { integracoesApi } from './api/integracoesApi';

/** Fachada única — métodos agrupados em `services/api/*` por domínio. */
const HttpService = {
  ...authApi,
  ...instituicaoApi,
  ...funcionarioApi,
  ...alunoApi,
  ...turmaApi,
  ...financeiroApi,
  ...equipeApi,
  ...programacaoApi,
  ...portalAlunoApi,
  ...plataformaApi,
  ...integracoesApi,
};

export default HttpService;
export type { LoginResponse } from './api/client';
