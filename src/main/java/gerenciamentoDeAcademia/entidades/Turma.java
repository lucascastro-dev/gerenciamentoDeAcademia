package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.dto.FuncionarioDto;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_turma")
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String horario;
    @ElementCollection
    @CollectionTable(name = "dias")
    private List<String> dias;
    private String especificacao;
    @Transient
    @CollectionTable(name = "PROFESSOR", joinColumns = @JoinColumn(name = "TB_FUNCIONARIO"))
    private FuncionarioDto professor;
    @ElementCollection
    @Transient
    @CollectionTable(name = "ALUNOS", joinColumns = @JoinColumn(name = "TB_ALUNO"))
    private List<AlunoDto> alunos;
}