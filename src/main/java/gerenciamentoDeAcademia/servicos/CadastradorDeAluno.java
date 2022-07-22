package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.entidades.AlunoCadastrado;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.utils.ExcecaoDeDominio;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
@Service
public class CadastradorDeAluno implements ICadastradorDeAluno {

    @Autowired
    private AlunoRepository alunoRepository;

    @Override
    public AlunoCadastrado cadastrar(Aluno aluno) {
        validar(aluno);

        var alunoCadastrado = AlunoCadastrado.builder()
                .nome(aluno.getNome())
                .rg(aluno.getRg())
                .cpf(aluno.getCpf())
                .dataDeNascimento(aluno.getDataDeNascimento())
                .endereco(aluno.getEndereco())
                .telefone(aluno.getTelefone())
                .valorMensalidade(aluno.getValorMensalidade())
                .diaVencimentoMensalidade(aluno.getDiaVencimentoMensalidade())
                .nomeResponsavel(aluno.getNomeResponsavel())
                .telefoneResponsavel(aluno.getTelefoneResponsavel());

        return alunoRepository.save(alunoCadastrado.build());
    }

    public void validar(Aluno aluno) {
        ExcecaoDeDominio.quandoTextoVazioOuNulo(aluno.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(aluno.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(aluno.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(aluno.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(aluno.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(aluno.getTelefone(), "Telefone é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(aluno.getValorMensalidade(), "Valor da mensalidade é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(aluno.getDiaVencimentoMensalidade(), "Dia de vencimento da mensalidade é obrigatório!");

        if ((LocalDate.now().getYear() - aluno.getDataDeNascimento().getYear()) < 18) {
            ExcecaoDeDominio.quandoTextoVazioOuNulo(aluno.getNomeResponsavel(), "Nome do responsável é obrigatório!");
            ExcecaoDeDominio.quandoTextoVazioOuNulo(aluno.getTelefoneResponsavel(), "Telefone do responsável é obrigatório!");
        }
    }
}