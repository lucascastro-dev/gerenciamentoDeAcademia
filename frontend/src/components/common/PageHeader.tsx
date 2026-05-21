interface Props {
  title: string;
  subtitle?: string;
}

/** Título da página no conteúdo; navegação fica no header global do layout */
const PageHeader: React.FC<Props> = ({ title, subtitle }) => (
  <div className="page-header">
    <h1 className="page-header__title">{title}</h1>
    {subtitle && <p className="page-header__subtitle">{subtitle}</p>}
  </div>
);

export default PageHeader;
