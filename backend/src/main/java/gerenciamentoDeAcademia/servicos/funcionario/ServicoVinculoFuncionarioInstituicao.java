package gerenciamentoDeAcademia.servicos.funcionario;

import gerenciamentoDeAcademia.dto.AtualizarVinculoFuncionarioDto;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.enums.TipoFuncionario;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ServicoVinculoFuncionarioInstituicao {

    private final VinculoFuncionarioInstituicaoRepository vinculoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final FuncionarioRepository funcionarioRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void sincronizarVinculosLegado() {
        for (Instituicao instituicao : instituicaoRepository.findAll()) {
            if (instituicao.getFuncionarios() == null) {
                continue;
            }
            for (Funcionario funcionario : instituicao.getFuncionarios()) {
                garantirVinculo(instituicao, funcionario);
            }
        }
    }

    @Transactional
    public VinculoFuncionarioInstituicao garantirVinculo(Instituicao instituicao, Funcionario funcionario) {
        return vinculoRepository
                .findByFuncionarioCpfAndInstituicaoId(funcionario.getCpf(), instituicao.getId())
                .orElseGet(() -> criarVinculo(instituicao, funcionario));
    }

    private VinculoFuncionarioInstituicao criarVinculo(Instituicao instituicao, Funcionario funcionario) {
        VinculoFuncionarioInstituicao vinculo = new VinculoFuncionarioInstituicao();
        vinculo.setInstituicao(instituicao);
        vinculo.setFuncionario(funcionario);
        TipoFuncionario tipo = funcionario.getTipoFuncionario() != null
                ? funcionario.getTipoFuncionario()
                : TipoFuncionario.RECEPCIONISTA;
        vinculo.setTipoFuncionario(tipo);
        vinculo.setAreaTerceirizado(funcionario.getAreaTerceirizado());
        vinculo.setEspecializacao(funcionario.getEspecializacao());
        if (vinculo.getDataAdmissao() == null) {
            vinculo.setDataAdmissao(java.time.LocalDate.now());
        }
        return vinculoRepository.save(vinculo);
    }

    @Transactional
    public void aplicarFuncaoNaInstituicao(Funcionario funcionario, Instituicao instituicao,
                                           TipoFuncionario tipo, gerenciamentoDeAcademia.enums.AreaTerceirizado area,
                                           String especializacao) {
        VinculoFuncionarioInstituicao vinculo = garantirVinculo(instituicao, funcionario);
        if (vinculo.getDataAdmissao() == null) {
            vinculo.setDataAdmissao(java.time.LocalDate.now());
        }
        vinculo.atualizarFuncao(tipo, area, especializacao);
        vinculoRepository.save(vinculo);
        sincronizarFuncionarioPrincipal(funcionario);
    }

    @Transactional
    public void atualizarVinculo(AtualizarVinculoFuncionarioDto dto, Long instituicaoEscopo,
                                 boolean operadorPlataforma) {
        ExcecaoDeDominio.quandoNulo(dto, "Dados do vínculo são obrigatórios.");
        String cpf = dto.getCpf() != null ? dto.getCpf().replaceAll("\\D", "") : "";
        ExcecaoDeDominio.quandoNuloOuVazio(cpf, "CPF é obrigatório.");
        ExcecaoDeDominio.quandoNulo(dto.getInstituicaoId(), "Instituição é obrigatória.");
        if (!operadorPlataforma) {
            ExcecaoDeDominio.quando(
                    instituicaoEscopo == null || !instituicaoEscopo.equals(dto.getInstituicaoId()),
                    "Você só pode alterar vínculos da sua instituição.");
        }
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(funcionario, "Funcionário não encontrado.");
        VinculoFuncionarioInstituicao vinculo = vinculoRepository
                .findByFuncionarioCpfAndInstituicaoId(cpf, dto.getInstituicaoId())
                .orElseThrow(() -> new ExcecaoDeDominio("Vínculo com a instituição não encontrado."));
        vinculo.atualizarFuncao(dto.getTipoFuncionario(), dto.getAreaTerceirizado(), dto.getEspecializacao());
        vinculoRepository.save(vinculo);
        sincronizarFuncionarioPrincipal(funcionario);
    }

    private void sincronizarFuncionarioPrincipal(Funcionario funcionario) {
        var vinculos = vinculoRepository.findByFuncionarioCpfOrderByInstituicaoRazaoSocialAsc(funcionario.getCpf());
        if (vinculos.isEmpty()) {
            return;
        }
        VinculoFuncionarioInstituicao principal = vinculos.get(0);
        funcionario.setTipoFuncionario(principal.getTipoFuncionario());
        funcionario.setCargo(principal.descricaoCargo());
        funcionario.setAreaTerceirizado(principal.getAreaTerceirizado());
        funcionario.setEspecializacao(principal.getEspecializacao());
        funcionarioRepository.save(funcionario);
    }
}
