package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Instituicao;
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
public class InstituicaoDto {
    private Long registroInstituicao;
    private String razaoSocial;
    private String cnpj;
    private Boolean cadastroAtivo;
    private String endereco;
    private String telefone;
    private List<FuncionarioDto> funcionarios = new ArrayList<>();
    private Boolean possuiCadastroPendentes;

    public InstituicaoDto(Instituicao instituicao) {
        this.registroInstituicao = instituicao.getId();
        this.razaoSocial = instituicao.getRazaoSocial();
        this.cnpj = instituicao.getCnpj();
        this.cadastroAtivo = instituicao.getCadastroAtivo();
        this.endereco = instituicao.getEndereco();
        this.telefone = instituicao.getTelefone();
        this.possuiCadastroPendentes = instituicao.getPossuiCadastrosParaAprovar();

        this.funcionarios = Optional.ofNullable(instituicao.getFuncionarios())
                .orElse(Collections.emptySet())
                .stream()
                .map(FuncionarioDto::new)
                .collect(Collectors.toList());
    }
}
