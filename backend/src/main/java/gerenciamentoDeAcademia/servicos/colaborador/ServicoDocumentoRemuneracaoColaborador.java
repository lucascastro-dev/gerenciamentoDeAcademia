package gerenciamentoDeAcademia.servicos.colaborador;

import gerenciamentoDeAcademia.dto.ArquivoPdfDto;
import gerenciamentoDeAcademia.dto.ConfirmarPagamentoFolhaDto;
import gerenciamentoDeAcademia.dto.DocumentoRemuneracaoDto;
import gerenciamentoDeAcademia.dto.FolhaPagamentoColaboradorDto;
import gerenciamentoDeAcademia.dto.PublicarHoleriteDto;
import gerenciamentoDeAcademia.dto.StatusIntegracaoPontoDto;
import gerenciamentoDeAcademia.entidades.DocumentoRemuneracaoColaborador;
import gerenciamentoDeAcademia.entidades.Funcionario;
import gerenciamentoDeAcademia.entidades.Instituicao;
import gerenciamentoDeAcademia.entidades.VinculoFuncionarioInstituicao;
import gerenciamentoDeAcademia.enums.TipoDocumentoRemuneracao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import gerenciamentoDeAcademia.infra.arquivos.ArmazenamentoDocumentoRemuneracao;
import gerenciamentoDeAcademia.repositorios.DocumentoRemuneracaoColaboradorRepository;
import gerenciamentoDeAcademia.repositorios.FuncionarioRepository;
import gerenciamentoDeAcademia.repositorios.InstituicaoRepository;
import gerenciamentoDeAcademia.repositorios.VinculoFuncionarioInstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicoDocumentoRemuneracaoColaborador {

    private static final Locale PT = Locale.forLanguageTag("pt-BR");

    private final DocumentoRemuneracaoColaboradorRepository repository;
    private final VinculoFuncionarioInstituicaoRepository vinculoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final ServicoFolhaPonto servicoFolhaPonto;
    private final ArmazenamentoDocumentoRemuneracao armazenamentoDocumentos;

    @Transactional(readOnly = true)
    public List<DocumentoRemuneracaoDto> listarMeusDocumentos(
            Long instituicaoId, String cpfColaborador, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        return repository
                .findByCpfColaboradorAndInstituicao_IdAndMesCompetenciaAndAnoCompetenciaOrderByTipoAsc(
                        cpfColaborador, instituicaoId, mes, ano)
                .stream()
                .map(DocumentoRemuneracaoDto::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentoRemuneracaoDto obterMeuDocumento(Long instituicaoId, String cpfColaborador, Long id) {
        DocumentoRemuneracaoColaborador doc = repository
                .findByIdAndCpfColaboradorAndInstituicao_Id(id, cpfColaborador, instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Documento não encontrado."));
        return DocumentoRemuneracaoDto.of(doc);
    }

    @Transactional(readOnly = true)
    public ArquivoPdfDto baixarMeuDocumentoPdf(Long instituicaoId, String cpfColaborador, Long id) {
        DocumentoRemuneracaoColaborador doc = repository
                .findByIdAndCpfColaboradorAndInstituicao_Id(id, cpfColaborador, instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Documento não encontrado."));
        return montarArquivoPdf(doc);
    }

    @Transactional
    public DocumentoRemuneracaoDto anexarDocumentoPdf(
            Long instituicaoId,
            String cpfPublicador,
            String cpfColaborador,
            TipoDocumentoRemuneracao tipo,
            Integer mes,
            Integer ano,
            String observacao,
            MultipartFile arquivo) {
        validarCompetencia(mes, ano);
        ExcecaoDeDominio.quandoNuloOuVazio(cpfColaborador, "Informe o colaborador.");
        ExcecaoDeDominio.quando(tipo == null || tipo == TipoDocumentoRemuneracao.INFORME,
                "Informe o tipo de documento (holerite ou recibo).");

        String cpf = cpfColaborador.replaceAll("\\D", "");
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(funcionario, "Colaborador não encontrado.");
        ExcecaoDeDominio.quando(
                !instituicaoRepository.existsByCnpjAndFuncionarioCpf(instituicaoId, cpf),
                "Colaborador não vinculado à instituição.");

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        DocumentoRemuneracaoColaborador documento = repository
                .findByInstituicao_IdAndCpfColaboradorAndTipoAndMesCompetenciaAndAnoCompetencia(
                        instituicaoId, cpf, tipo, mes, ano)
                .orElse(DocumentoRemuneracaoColaborador.builder()
                        .instituicao(instituicao)
                        .cpfColaborador(cpf)
                        .nomeColaborador(funcionario.getNome())
                        .tipo(tipo)
                        .mesCompetencia(mes)
                        .anoCompetencia(ano)
                        .build());

        armazenamentoDocumentos.removerSeExistir(documento.getCaminhoArquivo());
        String caminho = armazenamentoDocumentos.salvarPdf(instituicaoId, cpf, tipo, mes, ano, arquivo);

        documento.setNomeColaborador(funcionario.getNome());
        documento.setCaminhoArquivo(caminho);
        documento.setNomeArquivoOriginal(arquivo.getOriginalFilename());
        documento.setConteudo(observacao != null && !observacao.isBlank() ? observacao.trim() : null);
        documento.setValorBruto(null);
        documento.setValorLiquido(null);
        documento.setPublicadoEm(LocalDateTime.now());
        documento.setPublicadoPorCpf(cpfPublicador != null ? cpfPublicador.replaceAll("\\D", "") : null);

        return DocumentoRemuneracaoDto.of(repository.save(documento));
    }

    @Transactional(readOnly = true)
    public List<FolhaPagamentoColaboradorDto> listarColaboradoresFolha(Long instituicaoId, Integer mes, Integer ano) {
        validarCompetencia(mes, ano);
        return vinculoRepository.findByInstituicaoIdComDetalhes(instituicaoId).stream()
                .filter(v -> v.getFuncionario() != null && Boolean.TRUE.equals(v.getFuncionario().getCadastroAtivo()))
                .map(v -> montarLinhaFolha(v, mes, ano))
                .toList();
    }

    @Transactional
    public DocumentoRemuneracaoDto confirmarPagamento(
            Long instituicaoId, String cpfPublicador, ConfirmarPagamentoFolhaDto dto) {
        validarCompetencia(dto.getMesCompetencia(), dto.getAnoCompetencia());
        ExcecaoDeDominio.quando(!servicoFolhaPonto.isMesConferido(instituicaoId, dto.getMesCompetencia(), dto.getAnoCompetencia()),
                "A folha de ponto do mês precisa ser conferida pelo RH antes de confirmar o pagamento.");
        StatusIntegracaoPontoDto integracao = servicoFolhaPonto.statusIntegracao(
                instituicaoId, dto.getMesCompetencia(), dto.getAnoCompetencia());
        ExcecaoDeDominio.quando(!integracao.integradoFinanceiro(),
                "O financeiro precisa integrar a folha de ponto do mês antes de confirmar pagamentos.");
        ExcecaoDeDominio.quandoNuloOuVazio(dto.getCpfColaborador(), "Informe o colaborador.");

        String cpf = dto.getCpfColaborador().replaceAll("\\D", "");
        VinculoFuncionarioInstituicao vinculo = vinculoRepository.findByInstituicaoIdComDetalhes(instituicaoId).stream()
                .filter(v -> v.getFuncionario() != null && cpf.equals(v.getFuncionario().getCpf()))
                .findFirst()
                .orElseThrow(() -> new ExcecaoDeDominio("Colaborador não vinculado à instituição."));

        Funcionario funcionario = vinculo.getFuncionario();
        BigDecimal bruto = dto.getValorBruto() != null
                ? dto.getValorBruto()
                : salarioBaseEstimado(cpf);
        BigDecimal liquido = dto.getValorLiquido() != null
                ? dto.getValorLiquido()
                : bruto.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        String conteudo = montarConteudoRecibo(
                instituicao.getRazaoSocial(),
                funcionario.getNome(),
                vinculo.getTipoFuncionario() != null ? vinculo.getTipoFuncionario().getDescricao() : "Colaborador",
                dto.getMesCompetencia(),
                dto.getAnoCompetencia(),
                bruto,
                liquido);

        DocumentoRemuneracaoColaborador recibo = repository
                .findByInstituicao_IdAndCpfColaboradorAndTipoAndMesCompetenciaAndAnoCompetencia(
                        instituicaoId, cpf, TipoDocumentoRemuneracao.RECIBO,
                        dto.getMesCompetencia(), dto.getAnoCompetencia())
                .orElse(DocumentoRemuneracaoColaborador.builder()
                        .instituicao(instituicao)
                        .cpfColaborador(cpf)
                        .nomeColaborador(funcionario.getNome())
                        .tipo(TipoDocumentoRemuneracao.RECIBO)
                        .mesCompetencia(dto.getMesCompetencia())
                        .anoCompetencia(dto.getAnoCompetencia())
                        .build());

        recibo.setNomeColaborador(funcionario.getNome());
        recibo.setValorBruto(bruto);
        recibo.setValorLiquido(liquido);
        recibo.setConteudo(conteudo);
        recibo.setPublicadoEm(LocalDateTime.now());
        recibo.setPublicadoPorCpf(cpfPublicador != null ? cpfPublicador.replaceAll("\\D", "") : null);

        return DocumentoRemuneracaoDto.of(repository.save(recibo));
    }

    @Transactional
    public DocumentoRemuneracaoDto publicarHolerite(Long instituicaoId, String cpfPublicador, PublicarHoleriteDto dto) {
        validarCompetencia(dto.getMesCompetencia(), dto.getAnoCompetencia());
        ExcecaoDeDominio.quandoNuloOuVazio(dto.getCpfColaborador(), "Informe o colaborador.");

        String cpf = dto.getCpfColaborador().replaceAll("\\D", "");
        Funcionario funcionario = funcionarioRepository.findByCpf(cpf);
        ExcecaoDeDominio.quandoNulo(funcionario, "Colaborador não encontrado.");
        ExcecaoDeDominio.quando(
                !instituicaoRepository.existsByCnpjAndFuncionarioCpf(instituicaoId, cpf),
                "Colaborador não vinculado à instituição.");

        Instituicao instituicao = instituicaoRepository.findById(instituicaoId)
                .orElseThrow(() -> new ExcecaoDeDominio("Instituição não encontrada."));

        BigDecimal bruto = dto.getValorBruto() != null ? dto.getValorBruto() : salarioBaseEstimado(cpf);
        BigDecimal liquido = dto.getValorLiquido() != null
                ? dto.getValorLiquido()
                : bruto.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);

        String conteudo = montarConteudoHolerite(
                instituicao.getRazaoSocial(),
                funcionario.getNome(),
                dto.getMesCompetencia(),
                dto.getAnoCompetencia(),
                bruto,
                liquido,
                dto.getObservacao(),
                servicoFolhaPonto.resumoColaboradorFolha(instituicaoId, cpf, dto.getMesCompetencia(), dto.getAnoCompetencia()));

        DocumentoRemuneracaoColaborador holerite = repository
                .findByInstituicao_IdAndCpfColaboradorAndTipoAndMesCompetenciaAndAnoCompetencia(
                        instituicaoId, cpf, TipoDocumentoRemuneracao.HOLERITE,
                        dto.getMesCompetencia(), dto.getAnoCompetencia())
                .orElse(DocumentoRemuneracaoColaborador.builder()
                        .instituicao(instituicao)
                        .cpfColaborador(cpf)
                        .nomeColaborador(funcionario.getNome())
                        .tipo(TipoDocumentoRemuneracao.HOLERITE)
                        .mesCompetencia(dto.getMesCompetencia())
                        .anoCompetencia(dto.getAnoCompetencia())
                        .build());

        holerite.setNomeColaborador(funcionario.getNome());
        holerite.setValorBruto(bruto);
        holerite.setValorLiquido(liquido);
        holerite.setConteudo(conteudo);
        holerite.setPublicadoEm(LocalDateTime.now());
        holerite.setPublicadoPorCpf(cpfPublicador != null ? cpfPublicador.replaceAll("\\D", "") : null);

        return DocumentoRemuneracaoDto.of(repository.save(holerite));
    }

    private FolhaPagamentoColaboradorDto montarLinhaFolha(
            VinculoFuncionarioInstituicao vinculo, Integer mes, Integer ano) {
        Funcionario f = vinculo.getFuncionario();
        String cpf = f.getCpf();
        Optional<DocumentoRemuneracaoColaborador> recibo = repository
                .findByInstituicao_IdAndCpfColaboradorAndTipoAndMesCompetenciaAndAnoCompetencia(
                        vinculo.getInstituicao().getId(), cpf, TipoDocumentoRemuneracao.RECIBO, mes, ano);
        Optional<DocumentoRemuneracaoColaborador> holerite = repository
                .findByInstituicao_IdAndCpfColaboradorAndTipoAndMesCompetenciaAndAnoCompetencia(
                        vinculo.getInstituicao().getId(), cpf, TipoDocumentoRemuneracao.HOLERITE, mes, ano);

        String status;
        if (recibo.isPresent()) {
            status = "Pago";
        } else if (holerite.isPresent()) {
            status = "Processado";
        } else {
            status = "Pendente";
        }

        ServicoFolhaPonto.ResumoPontoColaboradorFolha ponto = servicoFolhaPonto.resumoColaboradorFolha(
                vinculo.getInstituicao().getId(), cpf, mes, ano);
        boolean pontoConferido = servicoFolhaPonto.isMesConferido(vinculo.getInstituicao().getId(), mes, ano);

        return new FolhaPagamentoColaboradorDto(
                cpf,
                f.getNome(),
                vinculo.getTipoFuncionario(),
                vinculo.getTipoFuncionario() != null ? vinculo.getTipoFuncionario().getDescricao() : "—",
                salarioBaseEstimado(cpf),
                status,
                recibo.isPresent(),
                ponto.diasTrabalhados(),
                ponto.minutosTrabalhados(),
                ponto.horasFormatadas(),
                pontoConferido);
    }

    private BigDecimal salarioBaseEstimado(String cpf) {
        int hash = Math.abs(cpf.hashCode() % 1800);
        return BigDecimal.valueOf(2200L + hash).setScale(2, RoundingMode.HALF_UP);
    }

    private void validarCompetencia(Integer mes, Integer ano) {
        ExcecaoDeDominio.quandoNulo(mes, "Informe o mês de competência.");
        ExcecaoDeDominio.quandoNulo(ano, "Informe o ano de competência.");
        ExcecaoDeDominio.quando(mes < 1 || mes > 12, "Mês de competência inválido.");
    }

    private String nomeMes(int mes) {
        return java.time.Month.of(mes).getDisplayName(TextStyle.FULL, PT);
    }

    private String montarConteudoRecibo(
            String instituicao, String nome, String cargo, int mes, int ano,
            BigDecimal bruto, BigDecimal liquido) {
        return """
                RECIBO DE PAGAMENTO
                Instituição: %s
                Colaborador: %s
                Função: %s
                Competência: %s/%d
                Valor bruto: R$ %s
                Valor líquido pago: R$ %s

                Declaro ter recebido a importância acima referente à remuneração do período.
                """.formatted(
                instituicao,
                nome,
                cargo,
                nomeMes(mes),
                ano,
                bruto.toPlainString(),
                liquido.toPlainString());
    }

    private String montarConteudoHolerite(
            String instituicao, String nome, int mes, int ano,
            BigDecimal bruto, BigDecimal liquido, String observacao,
            ServicoFolhaPonto.ResumoPontoColaboradorFolha ponto) {
        String obs = observacao != null && !observacao.isBlank() ? observacao : "—";
        return """
                CONTRACHEQUE — HOLERITE
                Instituição: %s
                Colaborador: %s
                Competência: %s/%d

                Horas apuradas (ponto): %s (%d dias)
                Proventos: R$ %s
                Descontos estimados: R$ %s
                Líquido: R$ %s

                Observação RH: %s
                """.formatted(
                instituicao,
                nome,
                nomeMes(mes),
                ano,
                ponto.horasFormatadas(),
                ponto.diasTrabalhados(),
                bruto.toPlainString(),
                bruto.subtract(liquido).toPlainString(),
                liquido.toPlainString(),
                obs);
    }

    private ArquivoPdfDto montarArquivoPdf(DocumentoRemuneracaoColaborador doc) {
        ExcecaoDeDominio.quando(
                doc.getCaminhoArquivo() == null || doc.getCaminhoArquivo().isBlank(),
                "Este documento não possui arquivo PDF anexado.");
        String nome = doc.getNomeArquivoOriginal();
        if (nome == null || nome.isBlank()) {
            nome = doc.getTipo().name().toLowerCase(Locale.ROOT) + ".pdf";
        }
        return new ArquivoPdfDto(armazenamentoDocumentos.carregar(doc.getCaminhoArquivo()), nome);
    }
}
