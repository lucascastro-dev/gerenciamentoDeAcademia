package gerenciamentoDeAcademia.infra.arquivos;

import gerenciamentoDeAcademia.enums.TipoDocumentoRemuneracao;
import gerenciamentoDeAcademia.excecao.ExcecaoDeDominio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Component
public class ArmazenamentoDocumentoRemuneracao {

    private final Path basePath;

    public ArmazenamentoDocumentoRemuneracao(
            @Value("${app.remuneracao.documentos.base-path}") String basePathConfig) {
        this.basePath = Path.of(basePathConfig).toAbsolutePath().normalize();
    }

    public String salvarPdf(
            Long instituicaoId,
            String cpfColaborador,
            TipoDocumentoRemuneracao tipo,
            int mes,
            int ano,
            MultipartFile arquivo) {
        validarPdf(arquivo);
        String cpf = cpfColaborador.replaceAll("\\D", "");
        String nomeArquivo = tipo.name().toLowerCase(Locale.ROOT)
                + "_" + ano + "_" + String.format("%02d", mes) + "_" + UUID.randomUUID() + ".pdf";
        Path destino = basePath
                .resolve(String.valueOf(instituicaoId))
                .resolve(cpf)
                .resolve(nomeArquivo);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return basePath.relativize(destino).toString().replace('\\', '/');
        } catch (IOException e) {
            throw new ExcecaoDeDominio("Não foi possível salvar o arquivo PDF.");
        }
    }

    public Resource carregar(String caminhoRelativo) {
        ExcecaoDeDominio.quandoNuloOuVazio(caminhoRelativo, "Arquivo não informado.");
        Path arquivo = basePath.resolve(caminhoRelativo).normalize();
        ExcecaoDeDominio.quando(!arquivo.startsWith(basePath), "Caminho de arquivo inválido.");
        ExcecaoDeDominio.quando(!Files.isRegularFile(arquivo), "Arquivo PDF não encontrado.");
        try {
            Resource resource = new UrlResource(arquivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ExcecaoDeDominio("Arquivo PDF não encontrado.");
            }
            return resource;
        } catch (IOException e) {
            throw new ExcecaoDeDominio("Não foi possível ler o arquivo PDF.");
        }
    }

    public void removerSeExistir(String caminhoRelativo) {
        if (caminhoRelativo == null || caminhoRelativo.isBlank()) {
            return;
        }
        Path arquivo = basePath.resolve(caminhoRelativo).normalize();
        if (arquivo.startsWith(basePath)) {
            try {
                Files.deleteIfExists(arquivo);
            } catch (IOException ignored) {
                // substituição de anexo não deve falhar por limpeza
            }
        }
    }

    private void validarPdf(MultipartFile arquivo) {
        ExcecaoDeDominio.quando(arquivo == null || arquivo.isEmpty(), "Informe o arquivo PDF.");
        String contentType = arquivo.getContentType();
        String nome = arquivo.getOriginalFilename() != null ? arquivo.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
        boolean tipoOk = "application/pdf".equalsIgnoreCase(contentType) || nome.endsWith(".pdf");
        ExcecaoDeDominio.quando(!tipoOk, "Apenas arquivos PDF são aceitos.");
        ExcecaoDeDominio.quando(arquivo.getSize() > 10 * 1024 * 1024, "O PDF deve ter no máximo 10 MB.");
        try {
            byte[] inicio = arquivo.getInputStream().readNBytes(5);
            ExcecaoDeDominio.quando(inicio.length < 4 || !new String(inicio, 0, 4).equals("%PDF"),
                    "O arquivo enviado não é um PDF válido.");
        } catch (IOException e) {
            throw new ExcecaoDeDominio("Não foi possível validar o arquivo PDF.");
        }
    }
}
