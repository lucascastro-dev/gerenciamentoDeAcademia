package gerenciamentoDeAcademia.controller;

import gerenciamentoDeAcademia.dto.AcademiaDto;
import gerenciamentoDeAcademia.servicos.academia.GerenciadorDeAcademia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping("academia")
public class GerenciarAcademiaController {

    @Autowired
    private GerenciadorDeAcademia gerenciadorDeAcademia;

    @PostMapping("/registrarAcademia")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrarNovaAcademia(@RequestBody AcademiaDto academiaDto) {
        gerenciadorDeAcademia.cadastrar(academiaDto);
    }

    @DeleteMapping("/desativarAcademia/{cnpjAcademia}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativarAcademia(@PathVariable("cnpjAcademia") String cnpjAcademia) {
        gerenciadorDeAcademia.desativarAcademia(cnpjAcademia);
    }

    @PutMapping("/atualizarDadosAcademia")
    @ResponseStatus(HttpStatus.OK)
    public void atualizarDadosAcademia(@RequestBody AcademiaDto academiaDto) {
        gerenciadorDeAcademia.atualizarDados(academiaDto);
    }
}
