package gerenciamentoDeAcademia.entidades;

import gerenciamentoDeAcademia.dto.AcademiaDto;
import gerenciamentoDeAcademia.excecao.ApplicationException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tb_academia")
public class Academia {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String razaoSocial;
    private String cnpj;
    private Boolean cadastroAtivo;
    private String endereco;
    private String telefone;
    private Boolean possuiCadastrosParaAprovar;

    @ManyToMany
    @JoinTable(name = "academia_funcionario", joinColumns = @JoinColumn(name = "academia_id"), inverseJoinColumns = @JoinColumn(name = "funcionario_id"))
    private Set<Funcionario> funcionarios = new HashSet<>();

    public Academia(AcademiaDto academiaDto) {
        this.razaoSocial = academiaDto.getRazaoSocial();
        this.cnpj = academiaDto.getCnpj();
        this.cadastroAtivo = academiaDto.getCadastroAtivo();
        this.endereco = academiaDto.getEndereco();
        this.telefone = academiaDto.getTelefone();
    }

    public void validarVinculo(Funcionario funcionario) {
        if (!this.getFuncionarios().contains(funcionario)) {
            throw new ApplicationException("Funcionário não cadastrado nesta academia", HttpStatus.BAD_REQUEST);
        }
    }

    public void atualizarStatusPendencias() {
        boolean pendente = this.funcionarios.stream()
                .anyMatch(f -> f.getCadastroAtivo() == null || !f.getCadastroAtivo());
        this.setPossuiCadastrosParaAprovar(pendente);
    }
}
