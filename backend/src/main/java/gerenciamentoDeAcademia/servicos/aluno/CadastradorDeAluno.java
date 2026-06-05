package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.ICadastradorDeAluno;
import gerenciamentoDeAcademia.util.CpfUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CadastradorDeAluno implements ICadastradorDeAluno {

    private final AlunoRepository alunoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final ServicoAcessoAluno servicoAcessoAluno;
    private final ServicoVinculoAlunoInstituicao servicoVinculoAlunoInstituicao;
    private final ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    @Override
    public void cadastrar(AlunoDto alunoDto) {
        ExcecaoDeDominio.quandoNulo(alunoDto, "Obrigatório preencher dados do aluno");
        String cpf = CpfUtil.somenteDigitos(alunoDto.getCpf());
        alunoDto.setCpf(cpf);

        Long instituicaoId = alunoDto.getInstituicaoId();
        ExcecaoDeDominio.quandoNulo(instituicaoId, "Instituição é obrigatória para matricular o aluno.");

        Aluno existente = alunoRepository.findByCpf(cpf);
        if (existente != null) {
            ExcecaoDeDominio.quando(
                    instituicaoRepository.alunoVinculadoInstituicao(cpf, instituicaoId),
                    "Este CPF já está matriculado nesta instituição.");
            servicoAcessoAluno.garantirUsuarioPortal(existente);
            servicoVinculoAlunoInstituicao.vincularAlunoNaInstituicao(instituicaoId, existente);
            servicoMatriculaInstituicao.salvarFinanceiro(instituicaoId, existente, alunoDto);
            return;
        }

        Aluno aluno = alunoRepository.save(new Aluno(alunoDto));
        servicoAcessoAluno.garantirUsuarioPortal(aluno);
        servicoVinculoAlunoInstituicao.vincularAlunoNaInstituicao(instituicaoId, aluno);
        servicoMatriculaInstituicao.salvarFinanceiro(instituicaoId, aluno, alunoDto);
    }
}
