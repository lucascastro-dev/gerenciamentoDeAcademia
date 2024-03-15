package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.ICadastradorDeAluno;
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
    public void cadastrar(AlunoDto alunoDto) {
        validar(alunoDto);

        alunoRepository.save(new Aluno(alunoDto));
    }

    public void validar(AlunoDto alunoDto) {
        ExcecaoDeDominio.quandoNulo(alunoDto, "Obrigatório preencher dados do aluno");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getNome(), "Nome é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getRg(), "RG é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getCpf(), "CPF é obrigatório!");
        ExcecaoDeDominio.quandoDataNulaOuVazia(alunoDto.getDataDeNascimento(), "Data de nascimento é obrigatória!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getEndereco(), "Endereço é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getTelefone(), "Telefone é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getValorMensalidade(), "Valor da mensalidade é obrigatório!");
        ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getDiaVencimentoMensalidade(), "Dia de vencimento da mensalidade é obrigatório!");

        if ((LocalDate.now().getYear() - alunoDto.getDataDeNascimento().getYear()) < 18) {
            ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getNomeResponsavel(), "Nome do responsável é obrigatório!");
            ExcecaoDeDominio.quandoNuloOuVazio(alunoDto.getTelefoneResponsavel(), "Telefone do responsável é obrigatório!");
        }
    }
}