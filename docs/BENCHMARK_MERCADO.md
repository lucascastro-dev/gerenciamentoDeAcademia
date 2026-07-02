# Benchmark de mercado — Turma360

Posicionamento do Turma360 frente a soluções educacionais, com foco no segmento **híbrido** (cursos livres, esportes, idiomas, escolas menores).

## Referências analisadas

| Solução | Perfil | O que observar | O que **não** replicar no Turma360 |
|---------|--------|----------------|-------------------------------------|
| [iScholar](https://www.ischolar.com.br/) | Gestão escolar completa, forte em financeiro escolar | Landing com segmentos, cobrança Pix/boleto/cartão, portal aluno, BI | Suite enterprise para redes grandes; régua de cobrança complexa no MVP |
| [Sponte](https://www.sponte.com.br/gestao-pedagogica) | Pedagógico + app agenda | Grade, diário, avaliações, app integrado | BNCC/Novo EM completo; avaliações online enterprise |
| [Gestão Educação](https://gestaoeducacao.com.br/) | Ecossistema rede pública | AVA, engajamento, presença em redes | Foco governo/SAEB/IDEB — fora do ICP inicial |
| [EduGestão](https://edugestao.com.br/) | ERP educacional BR | Matrículas, turmas, Educacenso | Nome/domínio conflitante; amplitude ERP |
| [Cogna](https://www.kroton.com.br/) | Holding educacional | Jornada 2–100 anos, lifelong learning | Escala corporativa — referência de narrativa, não de escopo |

## Matriz Turma360 vs mercado

| Capacidade | iScholar / Sponte | Turma360 hoje | Próximo passo |
|------------|-------------------|---------------|---------------|
| Landing institucional | Rico, segmentos | Redesenhada (Etapa C) | Depoimentos reais, SEO |
| Login SaaS único | Sim | `/entrar` + vínculo | Slug opcional (futuro) |
| Grade / programação | Sim | Sim + conflitos | — |
| Mensalidades / inadimplência | Sim | Sim | Asaas produção |
| Portal aluno | App + web | Web responsivo | PWA |
| Folha de pagamento | Parcial / ERP externo | **Não** — PDF anexo | — |
| Conciliação / fechamento caixa | Sim (iScholar) | **Removido da UI** | — |
| Gestão de equipe leve | Parcial | Ponto, férias, holerite PDF | — |
| Multi-instituição SaaS | Variável | Nativo | Planos + billing |

## Diferenciais Turma360

1. **Operação pedagógica + cobrança escolar leve** — sem ERP, sem folha interna.
2. **Multi-instituição nativa** — um login, vários vínculos.
3. **Gestão de equipe pragmática** — ponto e holerite/recibo em PDF do ERP externo.
4. **Segmento híbrido** — academias, idiomas, cursos livres (não compete com Cogna/Gestão Educação em rede pública).

## Deliberadamente fora do escopo

- Folha de pagamento completa, eSocial, DIRF
- Conciliação bancária e fechamento de caixa como módulo
- Educacenso / BNCC enterprise
- Competir com suites ERP de grande porte

## Público-alvo prioritário

- Academias de esporte e artes marciais
- Escolas de idiomas e cursos livres
- Instituições com 1–5 unidades e equipe enxuta
- Gestores que já usam ERP/contador externo para folha

Ver [ESCOPO_PRODUTO.md](./ESCOPO_PRODUTO.md) e [REFATORACAO_ETAPAS.md](./REFATORACAO_ETAPAS.md).
