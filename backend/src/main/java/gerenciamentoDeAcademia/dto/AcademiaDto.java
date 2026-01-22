package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Academia;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class AcademiaDto {
    private Long registroAcademia;
    private String razaoSocial;
    private String cnpj;
    private Boolean cadastroAtivo;
    private String endereco;
    private String telefone;
    private List<FuncionarioDto> funcionarios = new ArrayList<>();

    public AcademiaDto(Academia academia) {
        this.registroAcademia = academia.getId();
        this.razaoSocial = academia.getRazaoSocial();
        this.cnpj = academia.getCnpj();
        this.cadastroAtivo = academia.getCadastroAtivo();
        this.endereco = academia.getEndereco();
        this.telefone = academia.getTelefone();

        this.funcionarios = Optional.ofNullable(academia.getFuncionarios())
                .orElse(Collections.emptySet())
                .stream()
                .map(FuncionarioDto::new)
                .collect(Collectors.toList());
    }
}
