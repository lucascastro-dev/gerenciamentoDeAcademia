import { ReactNode } from 'react';
import BotaoVoltar from './BotaoVoltar';
import PageHeader from './PageHeader';

interface Props {
  title: string;
  subtitle?: string;
  children: ReactNode;
  showBack?: boolean;
}

const PageShell: React.FC<Props> = ({ title, subtitle, children, showBack = true }) => (
  <div className="page-shell">
    <div className="page-content">
      <PageHeader title={title} subtitle={subtitle} />
      {children}
    </div>
    {showBack && <BotaoVoltar />}
  </div>
);

export default PageShell;
