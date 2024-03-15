package gerenciamentoDeAcademia.servicos.aluno;

import gerenciamentoDeAcademia.dto.AlunoDto;
import gerenciamentoDeAcademia.entidades.Aluno;
import gerenciamentoDeAcademia.repositorios.AlunoRepository;
import gerenciamentoDeAcademia.servicos.interfaces.ICadastradorDeAluno;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CadastradorDeAluno implements ICadastradorDeAluno {

    private final AlunoRepository alunoRepository;

    @Override
    public void cadastrar(AlunoDto alunoDto) {
        alunoRepository.save(new Aluno(alunoDto));
    }
}