package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Aluno;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TurmaDto {
    private Long instituicaoId;
    private String horario;
    private String sala;
    private List<String> dias;
    private String modalidade;
    private String cpfProfessor;
    private List<Aluno> alunos;
}
