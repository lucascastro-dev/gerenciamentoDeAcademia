package gerenciamentoDeAcademia.servicos.master;

import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicoDelegacaoSubMaster {

    private final FuncionarioRepository funcionarioRepository;
    private final ServicoMasterPlataforma servicoMasterPlataforma;

    @Transactional
    public void definirSubMaster(Funcionario operador, String cpfAlvo, boolean habilitar) {
        ExcecaoDeDominio.quando(!servicoMasterPlataforma.podeConcederSubMaster(operador),
                "Apenas o usuário master raiz da plataforma pode delegar operadores.");
        String cpf = normalizarCpf(cpfAlvo);
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "Informe o CPF do colaborador.");
        Funcionario alvo = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(alvo, "Funcionário não encontrado.");
        ExcecaoDeDominio.quando(servicoMasterPlataforma.ehMasterRaiz(cpf),
                "O master raiz da plataforma não pode ser alterado por esta operação.");
        if (habilitar) {
            alvo.setPermitirGerenciarFuncoes(true);
        } else {
            alvo.setPermitirGerenciarFuncoes(false);
        }
        funcionarioRepository.save(alvo);
    }

    private String normalizarCpf(String cpf) {
        return cpf != null ? cpf.replaceAll("\\D", "") : "";
    }
}
