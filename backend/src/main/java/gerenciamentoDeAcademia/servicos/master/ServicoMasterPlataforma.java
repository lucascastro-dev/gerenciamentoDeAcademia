package gerenciamentoDeAcademia.servicos.master;

import gerenciamentoDeAcademia.entidades.Funcionario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Identifica o operador master da plataforma (CPF configurável) e sub-masters delegados.
 */
@Service
public class ServicoMasterPlataforma {

    @Value("${app.master.cpf}")
    private String masterCpf;

    public boolean ehMasterRaiz(String cpf) {
        return cpf != null && normalizarCpf(cpf).equals(normalizarCpf(masterCpf));
    }

    public boolean ehOperadorPlataforma(Funcionario funcionario) {
        if (funcionario == null || funcionario.getCpf() == null) {
            return false;
        }
        if (ehMasterRaiz(funcionario.getCpf())) {
            return true;
        }
        return Boolean.TRUE.equals(funcionario.getPermitirGerenciarFuncoes());
    }

    public boolean podeConcederSubMaster(Funcionario operador) {
        return operador != null && ehMasterRaiz(operador.getCpf());
    }

    public String getMasterCpf() {
        return normalizarCpf(masterCpf);
    }

    private String normalizarCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}
