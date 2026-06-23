import { FormEvent, useState } from 'react';

import FeedbackModal from '../../components/common/FeedbackModal';

import PageShell from '../../components/common/PageShell';

import './Colaborador.css';



const STATUS = ['Todos', 'Pendente', 'Aprovado', 'Rejeitado', 'Cancelado'] as const;



const MinhasFerias: React.FC = () => {

  const [filtroStatus, setFiltroStatus] = useState<(typeof STATUS)[number]>('Todos');

  const [periodo, setPeriodo] = useState('todos');

  const [modalAberto, setModalAberto] = useState(false);

  const [dataInicio, setDataInicio] = useState('');

  const [dataFim, setDataFim] = useState('');

  const [feedback, setFeedback] = useState({ open: false, success: false, message: '' });



  const abrirSolicitacao = () => {

    setDataInicio('');

    setDataFim('');

    setModalAberto(true);

  };



  const enviarSolicitacao = (e: FormEvent) => {

    e.preventDefault();

    if (!dataInicio || !dataFim) {

      setFeedback({ open: true, success: false, message: 'Informe o período desejado.' });

      return;

    }

    if (dataFim < dataInicio) {

      setFeedback({ open: true, success: false, message: 'A data fim deve ser igual ou posterior à data início.' });

      return;

    }

    setModalAberto(false);

    setFeedback({

      open: true,

      success: true,

      message: 'Solicitação registrada. O RH analisará seu pedido em breve.',

    });

  };



  return (

    <PageShell showBack={false}>

      <div className="colab-page colab-ferias">

        <div className="card colab-filter colab-filter--row">

          <div>

            <label htmlFor="filtro-status">Status</label>

            <select

              id="filtro-status"

              value={filtroStatus}

              onChange={(e) => setFiltroStatus(e.target.value as (typeof STATUS)[number])}

            >

              {STATUS.map((s) => (

                <option key={s} value={s}>{s}</option>

              ))}

            </select>

          </div>

          <div>

            <label htmlFor="filtro-periodo">Período aquisitivo</label>

            <select id="filtro-periodo" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>

              <option value="todos">Todos</option>

              <option value="atual">Atual</option>

            </select>

          </div>

          <div className="colab-filter__action">

            <button type="button" className="btn-primary" onClick={abrirSolicitacao}>

              Solicitar férias

            </button>

          </div>

        </div>



        <div className="colab-ferias-grid">

          <div className="card colab-saldo">

            <h3>Saldo</h3>

            <div className="colab-saldo__row">

              <div className="colab-saldo__circulo">0%</div>

              <div>

                <p className="colab-saldo__dias"><strong>0</strong> dias disponíveis</p>

                <p className="field-hint">0 aprovados · 0 pendentes</p>

              </div>

            </div>

            <div className="colab-saldo__bar">

              <span>Utilizado</span>

              <div className="colab-saldo__track"><div style={{ width: '0%' }} /></div>

              <span>0%</span>

            </div>

          </div>



          <div className="card">

            <h3>Períodos aquisitivos</h3>

            <ul className="colab-periodos">

              <li>

                <span className="colab-badge colab-badge--info">Em aquisição</span>

                <span>16/03/2026 – 15/03/2027</span>

                <strong>30/30</strong>

              </li>

              <li>

                <span className="colab-badge">Futuro</span>

                <span>16/03/2027 – 15/03/2028</span>

                <strong>30/30</strong>

              </li>

            </ul>

          </div>

        </div>



        <div className="card colab-empty">

          <h3>Nenhuma solicitação</h3>

          <p className="field-hint">Use o botão acima para registrar seu primeiro pedido de férias.</p>

        </div>

      </div>



      {modalAberto && (

        <div className="colab-modal-backdrop" role="presentation" onClick={() => setModalAberto(false)}>

          <div

            className="card colab-modal"

            role="dialog"

            aria-labelledby="modal-ferias-titulo"

            onClick={(ev) => ev.stopPropagation()}

          >

            <h3 id="modal-ferias-titulo">Solicitar férias</h3>

            <form onSubmit={enviarSolicitacao} className="colab-modal__form">

              <div className="form-grid">

                <div>

                  <label htmlFor="ferias-inicio">Data início</label>

                  <input

                    id="ferias-inicio"

                    type="date"

                    value={dataInicio}

                    onChange={(e) => setDataInicio(e.target.value)}

                    required

                  />

                </div>

                <div>

                  <label htmlFor="ferias-fim">Data fim</label>

                  <input

                    id="ferias-fim"

                    type="date"

                    value={dataFim}

                    onChange={(e) => setDataFim(e.target.value)}

                    required

                  />

                </div>

              </div>

              <div className="form-actions form-actions--compact">

                <button type="button" className="btn-secondary" onClick={() => setModalAberto(false)}>

                  Cancelar

                </button>

                <button type="submit" className="btn-primary">Enviar solicitação</button>

              </div>

            </form>

          </div>

        </div>

      )}



      <FeedbackModal

        open={feedback.open}

        success={feedback.success}

        message={feedback.message}

        onClose={() => setFeedback((f) => ({ ...f, open: false }))}

      />

    </PageShell>

  );

};



export default MinhasFerias;

