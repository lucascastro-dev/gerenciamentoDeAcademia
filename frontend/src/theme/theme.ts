export type ThemeMode = 'light' | 'dark';

const STORAGE_KEY = '@App:theme';

export function obterTema(): ThemeMode {
  const salvo = localStorage.getItem(STORAGE_KEY);
  if (salvo === 'dark' || salvo === 'light') return salvo;
  if (window.matchMedia('(prefers-color-scheme: dark)').matches) return 'dark';
  return 'light';
}

export function aplicarTema(tema: ThemeMode) {
  document.documentElement.setAttribute('data-theme', tema);
  localStorage.setItem(STORAGE_KEY, tema);
}

export function inicializarTema() {
  aplicarTema(obterTema());
}

export function alternarTema(): ThemeMode {
  const proximo: ThemeMode = obterTema() === 'light' ? 'dark' : 'light';
  aplicarTema(proximo);
  return proximo;
}
