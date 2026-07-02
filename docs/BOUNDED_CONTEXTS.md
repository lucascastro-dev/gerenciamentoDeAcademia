# Bounded contexts — backend Turma360

Mapa lógico do monorepo Java (`gerenciamentoDeAcademia`). Pacotes físicos ainda unificados; evolução futura (Onda B) pode alinhar pastas a estes contextos.

```mermaid
flowchart TB
  subgraph plataforma [Plataforma_SaaS]
    Instituicao[Instituicao_e_planos]
    Master[Master_e_cobranca]
    Auditoria[Auditoria]
  end
  subgraph academico [Academico]
    Turmas[Turmas_e_presenca]
    Programacao[Programacao_grade]
    Alunos[Alunos_matriculas]
    Certificados[Certificados]
  end
  subgraph financeiro [Financeiro_escolar]
    Mensalidades[Mensalidades]
    Inadimplencia[Inadimplencia]
    FolhaLegado[Folha_pagamento_legado]
  end
  subgraph equipe [Gestao_de_equipe]
    Ponto[Folha_de_ponto]
    Documentos[Holerite_PDF]
  end
  subgraph identidade [Identidade]
    Funcionario[Funcionarios_vinculos]
    Auth[JWT_permissoes]
    PortalAluno[Portal_aluno]
  end
  plataforma --> academico
  plataforma --> financeiro
  plataforma --> equipe
  identidade --> academico
  identidade --> equipe
```

## Contextos e pacotes atuais

| Contexto | Responsabilidade | Pacotes / controllers principais |
|----------|------------------|----------------------------------|
| **Identidade** | Login, JWT, funcionários, perfil | `controller/Login*`, `Funcionario*`, `infra/seguranca` |
| **Plataforma** | Multi-tenant, planos, master | `servicos/master`, `Instituicao*`, `PlanoInstituicao*` |
| **Acadêmico** | Turmas, alunos, programação, presença | `servicos/turma`, `servicos/programacao`, `Aluno*` |
| **Financeiro escolar** | Mensalidades, inadimplência | `servicos/financeiro`, `Financeiro*` |
| **Financeiro legado** | Folha interna, conciliação (congelado) | `FolhaPagamento*`, `Conciliacao*` |
| **Gestão de equipe** | Ponto, holerite PDF | `RhFolhaPonto*`, `RhRemuneracao*`, `Colaborador*` |
| **Portal aluno** | Self-service aluno | `PortalAluno*` |

## Frontend espelhado

| Módulo API (`frontend/src/services/api/`) | Domínio |
|-------------------------------------------|---------|
| `authApi` | Autenticação |
| `instituicaoApi` | Instituição e planos |
| `funcionarioApi` | Colaboradores |
| `alunoApi` | Alunos |
| `turmaApi` | Turmas e presença |
| `programacaoApi` | Grade horária |
| `financeiroApi` | Financeiro escolar + legado |
| `equipeApi` | Ponto e documentos PDF |
| `portalAlunoApi` | Portal do aluno |
| `plataformaApi` | Dashboard, certificados, auditoria |

## Regras de dependência (alvo)

- Acadêmico **não** depende de Financeiro legado.
- Gestão de equipe **não** calcula folha; apenas armazena PDF.
- Portal aluno **só** lê dados do contexto Acadêmico + Mensalidades.
