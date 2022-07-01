package gerenciamentoDeAcademia.entidades;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TurmaMontada {
    private String horario;
    private List<String> dias;
    private String especificacao;
    private Funcionario professor;
    private List<Aluno> alunos;
}