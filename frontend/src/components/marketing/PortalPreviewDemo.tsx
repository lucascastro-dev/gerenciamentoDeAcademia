import { useCallback, useEffect, useRef, useState } from 'react';

type Persona = 'colaborador' | 'aluno';

interface ScreenContent {
  cards: Array<{
    label: string;
    title: string;
    detail?: string;
    chip?: { text: string; tone: 'ok' | 'warn' | 'info' };
    wide?: boolean;
    action?: string;
  }>;
  table?: {
    head: string[];
    rows: string[][];
  };
  list?: Array<{ tag: string; title: string; meta: string }>;
}

interface PersonaConfig {
  nome: string;
  perfil: string;
  menus: string[];
  screens: Record<string, ScreenContent>;
}

const INSTITUICAO_MOCK = 'Arena Esportiva Demo';

const PERSONAS: Record<Persona, PersonaConfig> = {
  colaborador: {
    nome: 'Marina Costa',
    perfil: 'Professora',
    menus: ['Meu ponto', 'Meu holerite', 'Minhas férias'],
    screens: {
      'Meu ponto': {
        cards: [
          { label: 'Marcação do dia', title: 'Entrada 08:15', detail: 'Saída ainda não registrada', chip: { text: 'Dia em andamento', tone: 'ok' } },
          { label: 'Saldo da semana', title: '32h 10min', detail: 'Meta: 40h semanais' },
        ],
        table: {
          head: ['Data', 'Entrada', 'Saída', 'Total'],
          rows: [
            ['01/07', '08:12', '17:40', '9h28'],
            ['02/07', '08:15', '—', '—'],
          ],
        },
      },
      'Meu holerite': {
        cards: [
          { label: 'Competência', title: 'Junho/2026', chip: { text: 'Holerite publicado', tone: 'info' } },
          { label: 'Valor líquido', title: 'R$ 2.840,00', detail: 'Bruto R$ 3.200,00' },
          {
            label: 'Recibo disponível',
            title: 'PDF pronto para download',
            wide: true,
            action: 'Baixar holerite',
          },
        ],
      },
      'Minhas férias': {
        cards: [
          { label: 'Próximo período', title: '10/07 a 24/07', chip: { text: 'Aguardando RH', tone: 'warn' } },
          { label: 'Saldo', title: '18 dias', detail: 'Vencimento em Dez/2026' },
        ],
        list: [
          { tag: 'Aprovada', title: 'Recesso de fim de ano', meta: '20/12 a 05/01 · 2025' },
          { tag: 'Histórico', title: 'Férias de julho', meta: 'Aprovada em Mar/2025' },
        ],
      },
    },
  },
  aluno: {
    nome: 'João Silva',
    perfil: 'Aluno',
    menus: ['Minhas turmas', 'Mensalidades', 'Minha programação'],
    screens: {
      'Minhas turmas': {
        cards: [
          { label: 'Turma ativa', title: 'Judô Iniciantes', detail: 'Ter e Qui · 18:30', chip: { text: 'Matriculado', tone: 'ok' } },
          { label: 'Professor(a)', title: 'Marina Costa', detail: 'Dojô Central' },
        ],
        list: [
          { tag: 'Modalidade', title: 'Judô Iniciantes', meta: '2x por semana · Dojô Central' },
          { tag: 'Modalidade', title: 'Condicionamento', meta: 'Sáb · 09:00 · Sala 2' },
        ],
      },
      Mensalidades: {
        cards: [
          { label: 'Mensalidade Jul/2026', title: 'R$ 120,00', chip: { text: 'Em aberto', tone: 'warn' } },
          { label: 'Vencimento', title: 'Dia 10', detail: 'Último pagamento: Jun/2026' },
          {
            label: 'Pagamento online',
            title: 'PIX ou cartão no portal',
            wide: true,
            action: 'Pagar via PIX',
          },
        ],
      },
      'Minha programação': {
        cards: [
          { label: 'Próximo evento', title: 'Exame de faixa', detail: '15/07 · 18:00 · Dojô Central' },
          { label: 'Esta semana', title: '2 atividades', chip: { text: 'Tudo em dia', tone: 'ok' } },
        ],
        list: [
          { tag: 'Aula', title: 'Treino técnico', meta: '12/07 · 18:30' },
          { tag: 'Prova', title: 'Exame de faixa', meta: '15/07 · 18:00' },
        ],
      },
    },
  },
};

const STEP_MS = 3200;
const PERSONA_SWITCH_MS = 400;

const PortalPreviewDemo: React.FC = () => {
  const [persona, setPersona] = useState<Persona>('colaborador');
  const [menuIndex, setMenuIndex] = useState(0);
  const [animKey, setAnimKey] = useState(0);
  const [paused, setPaused] = useState(false);
  const pauseTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const config = PERSONAS[persona];
  const menuAtivo = config.menus[menuIndex];
  const screen = config.screens[menuAtivo];

  const bumpAnim = useCallback(() => {
    setAnimKey((k) => k + 1);
  }, []);

  const pauseAuto = useCallback(() => {
    setPaused(true);
    if (pauseTimer.current) clearTimeout(pauseTimer.current);
    pauseTimer.current = setTimeout(() => setPaused(false), 12000);
  }, []);

  const irPara = useCallback((nextPersona: Persona, nextIndex: number) => {
    setPersona(nextPersona);
    setMenuIndex(nextIndex);
    bumpAnim();
  }, [bumpAnim]);

  const selecionarPersona = (p: Persona) => {
    pauseAuto();
    irPara(p, 0);
  };

  const selecionarMenu = (index: number) => {
    pauseAuto();
    setMenuIndex(index);
    bumpAnim();
  };

  useEffect(() => {
    if (paused) return undefined;

    const timer = setTimeout(() => {
      const nextIndex = menuIndex + 1;
      if (nextIndex < config.menus.length) {
        setMenuIndex(nextIndex);
        bumpAnim();
        return;
      }

      const nextPersona: Persona = persona === 'colaborador' ? 'aluno' : 'colaborador';
      setTimeout(() => irPara(nextPersona, 0), PERSONA_SWITCH_MS);
    }, STEP_MS);

    return () => clearTimeout(timer);
  }, [paused, persona, menuIndex, config.menus.length, bumpAnim, irPara]);

  useEffect(() => () => {
    if (pauseTimer.current) clearTimeout(pauseTimer.current);
  }, []);

  return (
    <div className="marketing__portal-preview">
      <div className="marketing__portal-tabs" role="tablist" aria-label="Demonstração do portal">
        <button
          type="button"
          role="tab"
          aria-selected={persona === 'colaborador'}
          className={`marketing__portal-tab${persona === 'colaborador' ? ' marketing__portal-tab--active' : ''}`}
          onClick={() => selecionarPersona('colaborador')}
        >
          Colaborador
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={persona === 'aluno'}
          className={`marketing__portal-tab${persona === 'aluno' ? ' marketing__portal-tab--active' : ''}`}
          onClick={() => selecionarPersona('aluno')}
        >
          Aluno
        </button>
      </div>

      <div className={`marketing__portal-frame marketing__portal-frame--${persona}`}>
        <aside className="marketing__portal-sidebar">
          <div className="marketing__portal-brand">Turma360</div>
          <nav>
            {config.menus.map((label, i) => (
              <button
                key={label}
                type="button"
                className={i === menuIndex ? 'active' : ''}
                onClick={() => selecionarMenu(i)}
              >
                {label}
              </button>
            ))}
          </nav>
          <div className="marketing__portal-progress" aria-hidden="true">
            {config.menus.map((label, i) => (
              <span key={label} className={i === menuIndex ? 'is-on' : i < menuIndex ? 'is-done' : ''} />
            ))}
          </div>
        </aside>

        <div className="marketing__portal-main">
          <header className="marketing__portal-header">
            <strong>{INSTITUICAO_MOCK}</strong>
            <em>{config.nome} · {config.perfil}</em>
          </header>

          <div key={`${persona}-${menuIndex}-${animKey}`} className="marketing__portal-screen">
            <div className="marketing__portal-cards">
              {screen.cards.map((card) => (
                <div
                  key={card.label}
                  className={`marketing__portal-card${card.wide ? ' marketing__portal-card--wide' : ''}`}
                >
                  <small>{card.label}</small>
                  <strong>{card.title}</strong>
                  {card.detail && <span>{card.detail}</span>}
                  {card.chip && (
                    <span className={`chip chip--${card.chip.tone}`}>{card.chip.text}</span>
                  )}
                  {card.action && (
                    <div className="marketing__portal-row">
                      <button type="button" className="marketing__portal-btn">{card.action}</button>
                    </div>
                  )}
                </div>
              ))}
            </div>

            {screen.table && (
              <div className="marketing__portal-table">
                <div className="marketing__portal-table-head">
                  {screen.table.head.map((h) => <span key={h}>{h}</span>)}
                </div>
                {screen.table.rows.map((row) => (
                  <div key={row.join('-')} className="marketing__portal-table-row">
                    {row.map((cell) => <span key={cell}>{cell}</span>)}
                  </div>
                ))}
              </div>
            )}

            {screen.list && (
              <ul className="marketing__portal-list">
                {screen.list.map((item) => (
                  <li key={item.title}>
                    <span className="marketing__portal-list-tag">{item.tag}</span>
                    <div>
                      <strong>{item.title}</strong>
                      <span>{item.meta}</span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <p className="marketing__portal-demo-hint">Demonstração ilustrativa · dados fictícios</p>
        </div>
      </div>
    </div>
  );
};

export default PortalPreviewDemo;
