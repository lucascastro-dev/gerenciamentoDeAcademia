package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.util.CpfUtil;
import gerenciamentoDeAcademia.servicos.interfaces.IAlteradorDeDadosDoAluno;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlteadorDeDadosDoAluno implements IAlteradorDeDadosDoAluno {

    private final AlunoRepository alunoRepository;
    private final ServicoMatriculaInstituicao servicoMatriculaInstituicao;

    @Override
    public void alterarAluno(AlunoDto alunoDto) {
        String cpf = CpfUtil.somenteDigitos(alunoDto.getCpf());
        ExcecaoDeDominio.quando(cpf.length() != 11, "CPF obrigatório com 11 dígitos.");

        Aluno aluno = alunoRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(aluno, "Aluno não encontrado!");

        aplicarDadosPessoais(aluno, alunoDto);
        alunoRepository.save(aluno);

        if (alunoDto.getInstituicaoId() != null
                && alunoDto.getValorMensalidade() != null
                && alunoDto.getDiaVencimentoMensalidade() != null) {
            servicoMatriculaInstituicao.atualizarFinanceiro(aluno, alunoDto);
        }
    }

    /** Atualiza somente campos enviados — evita apagar dados na atualização só de mensalidade. */
    private void aplicarDadosPessoais(Aluno aluno, AlunoDto dto) {
        if (StringUtils.hasText(dto.getNome())) {
            aluno.setNome(dto.getNome().trim());
        }
        if (StringUtils.hasText(dto.getRg())) {
            aluno.setRg(dto.getRg().trim());
        }
        if (dto.getDataDeNascimento() != null) {
            aluno.setDataDeNascimento(dto.getDataDeNascimento());
        }
        if (StringUtils.hasText(dto.getEndereco())) {
            aluno.setEndereco(dto.getEndereco().trim());
        }
        if (StringUtils.hasText(dto.getTelefone())) {
            aluno.setTelefone(CpfUtil.somenteDigitos(dto.getTelefone()).isEmpty()
                    ? dto.getTelefone().trim()
                    : CpfUtil.somenteDigitos(dto.getTelefone()));
        }
        if (dto.getEmail() != null) {
            aluno.setEmail(dto.getEmail().isBlank() ? null : dto.getEmail().trim());
        }
        if (dto.getNomeResponsavel() != null) {
            aluno.setNomeResponsavel(dto.getNomeResponsavel().isBlank() ? null : dto.getNomeResponsavel().trim());
        }
        if (StringUtils.hasText(dto.getTelefoneResponsavel())) {
            String telResp = CpfUtil.somenteDigitos(dto.getTelefoneResponsavel());
            aluno.setTelefoneResponsavel(telResp.isEmpty()
                    ? dto.getTelefoneResponsavel().trim()
                    : telResp);
        }
    }
}
