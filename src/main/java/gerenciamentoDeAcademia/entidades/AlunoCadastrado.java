package gerenciamentoDeAcademia.entidades;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_aluno")
public class AlunoCadastrado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String rg;
    private String cpf;
    private LocalDate dataDeNascimento;
    private String endereco;
    private String telefone;
    private Double valorMensalidade;
    private Integer diaVencimentoMensalidade;
    private String nomeResponsavel;
    private String telefoneResponsavel;
}