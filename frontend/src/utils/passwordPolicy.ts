export interface PasswordRule {
  id: string;
  label: string;
  test: (password: string) => boolean;
}

export const PASSWORD_RULES: PasswordRule[] = [
  { id: 'length', label: 'Mínimo de 8 caracteres', test: (p) => p.length >= 8 },
  { id: 'upper', label: 'Uma letra maiúscula (A–Z)', test: (p) => /[A-Z]/.test(p) },
  { id: 'lower', label: 'Uma letra minúscula (a–z)', test: (p) => /[a-z]/.test(p) },
  { id: 'digit', label: 'Um número (0–9)', test: (p) => /\d/.test(p) },
  { id: 'special', label: 'Um caractere especial (!@#$…)', test: (p) => /[^A-Za-z0-9]/.test(p) },
];

export function avaliarSenha(password: string): Record<string, boolean> {
  return Object.fromEntries(PASSWORD_RULES.map((r) => [r.id, r.test(password)]));
}

export function isSenhaForte(password: string): boolean {
  return PASSWORD_RULES.every((r) => r.test(password));
}
