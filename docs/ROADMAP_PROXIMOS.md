# Próximas entregas

## Implementado nesta fase
- Login com mensagem amigável para senha inválida (401)
- Permissões realistas: turma `gerenciar` só Diretor, Administrador e TI
- Terceirizado com área vinculada (RH, professor substituto, TI) e escopo limitado
- Portal do aluno (`/portal-aluno/*`) — dados, turmas, mensalidades
- Plano da instituição na plataforma (teste 7 dias, mensal, trimestral, semestral, anual)
- Usuário de portal criado automaticamente na matrícula do aluno

## Pendente (evolução)
- Integração gateway de pagamento (mensalidade aluno + plano instituição)
- Bloqueio operacional quando plano da instituição expirar (hoje: aviso no login e tela de plano)
- Redefinição de senha do aluno pelo portal
- Vínculo aluno ↔ instituição via turma/academia (hoje lista instituições ativas para alunos)
