# Planos e permissões por instituição

## Estado atual (jun/2026)

| Recurso | Implementado |
|---------|--------------|
| Tipos de plano (`TRIAL_7_DIAS`, `MENSAL`, …) | Sim — assinatura e expiração |
| Bloqueio por plano expirado / pagamento pendente | Sim — `FiltroPlanoInstituicao` |
| Limites por plano (ex.: trial 50 alunos) | **Pendente** |
| Feature flags por plano (módulos on/off) | **Pendente** |

Os valores exibidos na landing (`/precos`) são **referência comercial**. A contratação e o plano efetivo são definidos pelo master da plataforma na ativação da instituição.

## Próxima etapa sugerida

1. Tabela `limite_plano` ou campos em `AssinaturaPlataforma` (max alunos, portal aluno, gestão equipe).
2. Validação em cadastro/matrícula de alunos para trial.
3. Guard no frontend alinhado às mesmas regras do backend.
