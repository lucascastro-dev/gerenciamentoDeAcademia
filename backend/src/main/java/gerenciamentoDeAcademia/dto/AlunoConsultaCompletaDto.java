package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AlunoConsultaCompletaDto extends AlunoDto {
    private List<AlunoMatriculaInstituicaoDto> matriculas = new ArrayList<>();

    public AlunoConsultaCompletaDto(Aluno aluno) {
        this.setNome(textoSeguro(aluno.getNome()));
        this.setRg(textoSeguro(aluno.getRg()));
        this.setCpf(aluno.getCpf());
        this.setDataDeNascimento(aluno.getDataDeNascimento());
        this.setEndereco(textoSeguro(aluno.getEndereco()));
        this.setTelefone(textoSeguro(aluno.getTelefone()));
        this.setEmail(textoSeguro(aluno.getEmail()));
        this.setNomeResponsavel(textoSeguro(aluno.getNomeResponsavel()));
        this.setTelefoneResponsavel(textoSeguro(aluno.getTelefoneResponsavel()));
    }

    private static String textoSeguro(String valor) {
        return valor != null ? valor : "";
    }
}
