# Projeto Gerenciamento de Academia

Este é um projeto de estudo para a prática de conceitos avançados de desenvolvimento de software utilizando Java 17, Maven, JPA (H2), Spring Boot para o backend e React com VITE para o frontend. O objetivo principal é criar uma aplicação para gerenciar uma academia, aplicando diferentes técnicas e práticas de programação aprendidas.

## Funcionalidades

- **Gestão de Alunos:** Permite o cadastro, atualização, remoção e consulta de informações dos alunos.
- **Gestão de Funcionários:** Permite o registro, edição, exclusão e visualização de informações dos funcionários da academia.
- **Gestão de Turmas Coletivas:** Oferece recursos para o montagem de turma nova, inserir alunos em turmas existentes e consulta de turmas coletivas oferecidas pela academia.

## Tecnologias Utilizadas

### Backend

- **Java 17:** Linguagem de programação principal.
- **Maven:** Gerenciamento de dependências e construção do projeto.
- **JPA (H2):** Framework de persistência para o banco de dados em memória H2.
- **Spring Boot:** Framework para desenvolvimento de aplicações web e API REST.
- **Testes Unitários com JUnit e Mockito:** Ferramentas para testar o código de forma automatizada e isolada.

### Frontend

- **React:** Biblioteca JavaScript para a construção de interfaces de usuário.
- **VITE:** Ferramenta de construção rápida para projetos React.

### Práticas e Conceitos Aplicados
- **Clean Code:** Foco na escrita de código legível, organizado e de fácil manutenção.
- **DDD (Domain-Driven Design):** Modelagem de domínio baseada em conceitos de negócio.
- **BDD (Behavior-Driven Development):** Desenvolvimento orientado a comportamento, com cenários descritos em linguagem natural.
- **TDD (Test-Driven Development):** Desenvolvimento orientado a testes, escrevendo testes antes de implementar a funcionalidade.
- **JWT Auth:** Autenticação e autorização baseada em tokens JWT (JSON Web Token).

## Como Executar o Projeto

1. **Clonar o repositório:**
git clone https://github.com/lucascastro-dev/gerenciamentoDeAcademia.git


2. **Backend:**
- Navegue até o diretório `backend`.
- Execute o comando `mvn spring-boot:run` para iniciar o servidor backend.
- O servidor estará acessível em `http://localhost:8080`.

3. **Frontend:**
- Navegue até o diretório `frontend`.
- Execute o comando `npm install` para instalar as dependências.
- Execute o comando `npm run dev` para iniciar o servidor de desenvolvimento.
- O aplicativo estará disponível em `http://localhost:5173`.

## Contribuições

Contribuições são bem-vindas! Se você tem sugestões de melhorias, correções de bugs ou novas funcionalidades, fique à vontade para abrir uma issue ou enviar um pull request.