package gerenciamentoDeAcademia.servicos;

import gerenciamentoDeAcademia.dto.AlunoDto;
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
    public AlunoCadastrado cadastrar(AlunoDto alunoDto) {
        validar(alunoDto);

        var alunoCadastrado = AlunoCadastrado.builder()
                .nome(alunoDto.getNome())
                .rg(alunoDto.getRg())
                .cpf(alunoDto.getCpf())
                .dataDeNascimento(alunoDto.getDataDeNascimento())
                .endereco(alunoDto.getEndereco())
                .telefone(alunoDto.getTelefone())
                .valorMensalidade(alunoDto.getValorMensalidade())
                .diaVencimentoMensalidade(alunoDto.getDiaVencimentoMensalidade())
                .nomeResponsavel(alunoDto.getNomeResponsavel())
                .telefoneResponsavel(alunoDto.getTelefoneResponsavel());

        return alunoRepository.save(alunoCadastrado.build());
    }

    public void validar(AlunoDto alunoDto) {
        ExcecaoDeDominio.quandoTextoVazioOuNulo(alunoDto.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(alunoDto.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(alunoDto.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(alunoDto.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(alunoDto.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoTextoVazioOuNulo(alunoDto.getTelefone(), "Telefone é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getValorMensalidade(), "Valor da mensalidade é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getDiaVencimentoMensalidade(), "Dia de vencimento da mensalidade é obrigatório!");

        if ((LocalDate.now().getYear() - alunoDto.getDataDeNascimento().getYear()) < 18) {
            ExcecaoDeDominio.quandoTextoVazioOuNulo(alunoDto.getNomeResponsavel(), "Nome do responsável é obrigatório!");
            ExcecaoDeDominio.quandoTextoVazioOuNulo(alunoDto.getTelefoneResponsavel(), "Telefone do responsável é obrigatório!");
        }
    }
}