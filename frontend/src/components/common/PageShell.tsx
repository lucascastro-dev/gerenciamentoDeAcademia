import { ReactNode } from 'react';
import BotaoVoltar from './BotaoVoltar';
import PageHeader from './PageHeader';

interface Props {
  title?: string;
  subtitle?: string;
  children: ReactNode;
  showBack?: boolean;
  /** Exibe título/subtítulo no conteúdo (desligado por padrão — menu lateral já identifica a página). */
  showHeader?: boolean;
}

const PageShell: React.FC<Props> = ({
  title = '',
  subtitle,
  children,
  showBack = true,
  showHeader = false,
}) => (
  <div className="page-shell">
    <div className="page-content">
      {showHeader && title && <PageHeader title={title} subtitle={subtitle} />}
      {children}
    </div>
    {showBack && <BotaoVoltar />}
  </div>
);

export default PageShell;
