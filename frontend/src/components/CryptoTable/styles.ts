import styled from 'styled-components';

export const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
`;

export const Th = styled.th`
  background: #1a1a1a;
  color: white;
  padding: 12px;
  text-align: left;
`;

export const Td = styled.td<{ isPositive?: boolean }>`
  padding: 12px;
  border-bottom: 1px solid #ddd;
  color: ${props => props.isPositive ? '#4caf50' : '#f44336'};
`;

export const TrendArrow = styled.span<{ isPositive: boolean }>`
  color: ${props => props.isPositive ? '#4caf50' : '#f44336'};
  margin-left: 5px;
`; 