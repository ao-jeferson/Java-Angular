export const MESES = Array.from({ length: 12 }, (_, index) => index + 1);
export const ANOS = Array.from(
  { length: new Date().getFullYear() + 11 - 1900 },
  (_, index) => 1900 + index,
);
