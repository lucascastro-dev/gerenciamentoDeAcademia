import { APP_NAME } from '../../constants/branding';

const AppFooter: React.FC = () => (
  <footer className="app-footer">
    <p className="app-footer__line">
      © {new Date().getFullYear()} {APP_NAME}. Todos os direitos reservados.
    </p>
    <p className="app-footer__line">
      Desenvolvido por{' '}
      <a
        href="https://lucascastro-dev.github.io/lr-site/"
        target="_blank"
        rel="noopener noreferrer"
      >
        LR Info e Tecnologia Ltda
      </a>
      {' '}— CNPJ 65.608.492/0001-48
    </p>
  </footer>
);

export default AppFooter;
