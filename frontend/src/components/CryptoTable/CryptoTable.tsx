import { useWebSocket } from '@/hooks/useWebSocket';
import { Table, Th, Td, TrendArrow } from './styles';
import { formatNumber } from '@/utils/format';

const CryptoTable = () => {
  const { isConnected, prices } = useWebSocket();

  return (
    <div>
      <div>상태: {isConnected ? '연결됨' : '연결 중...'}</div>
      <Table>
        <thead>
          <tr>
            <Th>거래소</Th>
            <Th>심볼</Th>
            <Th>가격</Th>
            <Th>24h 변동</Th>
            <Th>거래량</Th>
          </tr>
        </thead>
        <tbody>
          {prices.map((price) => (
            <tr key={`${price.exchange}-${price.symbol}`}>
              <Td>{price.exchange}</Td>
              <Td>{price.symbol}</Td>
              <Td>{price.price ? formatNumber(price.price) : '-'}</Td>
              <Td isPositive={price.changeRate > 0}>
                {price.changeRate ? `${price.changeRate.toFixed(2)}%` : '-'}
                {price.changeRate && (
                  <TrendArrow isPositive={price.changeRate > 0}>
                    {price.changeRate > 0 ? '↑' : '↓'}
                  </TrendArrow>
                )}
              </Td>
              <Td>{price.volume ? formatNumber(price.volume) : '-'}</Td>
            </tr>
          ))}
        </tbody>
      </Table>
    </div>
  );
};

export default CryptoTable; 