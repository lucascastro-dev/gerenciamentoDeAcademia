package gerenciamentoDeAcademia.dto;

import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.infra.seguranca.VinculoPlataforma;

public record VinculoInstituicaoDto(
        Long id,
        String razaoSocial,
        boolean cadastroAtivo,
        boolean selecionavel
) {
    public static VinculoInstituicaoDto daInstituicao(Instituicao instituicao) {
        boolean ativa = Boolean.TRUE.equals(instituicao.getCadastroAtivo());
        return new VinculoInstituicaoDto(
                instituicao.getId(),
                instituicao.getRazaoSocial(),
                ativa,
                ativa
        );
    }

    public static VinculoInstituicaoDto plataforma() {
        return new VinculoInstituicaoDto(
                VinculoPlataforma.ID_LONG,
                VinculoPlataforma.ROTULO,
                true,
                true
        );
    }
}
