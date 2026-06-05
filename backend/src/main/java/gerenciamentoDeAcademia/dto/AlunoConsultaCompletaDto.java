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
        this.setNome(aluno.getNome());
        this.setRg(aluno.getRg());
        this.setCpf(aluno.getCpf());
        this.setDataDeNascimento(aluno.getDataDeNascimento());
        this.setEndereco(aluno.getEndereco());
        this.setTelefone(aluno.getTelefone());
        this.setEmail(aluno.getEmail());
        this.setNomeResponsavel(aluno.getNomeResponsavel());
        this.setTelefoneResponsavel(aluno.getTelefoneResponsavel());
    }
}
