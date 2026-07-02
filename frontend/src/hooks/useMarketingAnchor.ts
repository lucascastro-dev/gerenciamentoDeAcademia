import { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

/** Navega para âncoras da home (#segmentos, #recursos) com scroll suave. */
export function useMarketingAnchor() {
  const { pathname, hash } = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    if (pathname !== '/' || !hash) return;
    const id = hash.replace('#', '');
    const timer = window.setTimeout(() => {
      document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 80);
    return () => window.clearTimeout(timer);
  }, [pathname, hash]);

  const goToSection = (sectionId: string) => (e: React.MouseEvent) => {
    e.preventDefault();
    const hashTarget = `#${sectionId}`;
    if (pathname === '/') {
      document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
      window.history.replaceState(null, '', hashTarget);
    } else {
      navigate(`/${hashTarget}`);
    }
  };

  return { goToSection };
}
