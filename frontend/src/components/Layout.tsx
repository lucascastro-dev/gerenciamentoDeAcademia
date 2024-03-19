import styled from "styled-components";
import { Outlet } from "react-router-dom";

const ContainerApplication = styled.div`
  display: grid;
  grid-template-columns: repeat(20, 1fr);
  grid-template-rows: repeat(10, 1fr);
  height: 100vh;

  @media (max-width: 1366px) {
    grid-template-columns: repeat(10, 1fr);
  }

  @media (max-width: 768px) {
    grid-template-columns: repeat(10, 1fr);
  }

  @media (max-width: 480px) {
    grid-template-columns: 1fr;
  }
`;

const SingleSpaApplicationMenuLeft = styled.aside`
  //border-top-right-radius: 1rem;
  //border-bottom-right-radius: 1rem;
  box-shadow: 0 0 0.8rem #0000000f;
  /* background-color: #1d1b22; */
  background-color: #343746;

  grid-column: span 2;
  grid-row: span 10;

  position: relative;

  min-width: 15rem;

  @media (max-width: 1366px) {
    grid-column: span 2;
    grid-row: span 10;
  }

  @media (max-width: 950px) {
    grid-column: span 10;
    grid-row: span 1;
    display: flex;
    justify-content: end;
    align-items: end;
    border-radius: 0;

    max-height: 3.5rem;
  }
`;

const SingleSpaApplicationApps = styled.main`
  grid-column: span 18;
  grid-row: span 10;
  padding-left: 1rem;
  padding-right: 1rem;
  border-radius: 1rem;
  margin: 0.5rem;

  @media (max-width: 1366px) {
    grid-column: span 8;
    grid-row: span 10;
  }

  @media (max-width: 950px) {
    grid-column: span 10;
    grid-row: span 9;
  }

  @media (max-width: 480px) {
    grid-column: span 10;
    grid-row: span 9;
  }

  overflow: auto;
  &::-webkit-scrollbar {
    width: 0.5rem;
    height: 0.5rem;
    background-color: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--light-gray);
    border-radius: 0.5rem;
  }

  &::-webkit-scrollbar-thumb:hover {
    background-color: var(--gray);
  }
`;

export const Layout: React.FC = () => {
  return (
    <>
        <ContainerApplication>
          <SingleSpaApplicationMenuLeft>
          </SingleSpaApplicationMenuLeft>
          <SingleSpaApplicationApps>
            <Outlet />
          </SingleSpaApplicationApps>
        </ContainerApplication>
    </>
  );
};