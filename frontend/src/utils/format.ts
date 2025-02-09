export const formatNumber = (value: number): string => {
  return new Intl.NumberFormat('ko-KR').format(value);
}; 