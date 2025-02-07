import styled from 'styled-components';
import { MarketPrice } from './types';

const Grid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
  padding: 1rem;
`;

const Card = styled.div`
  padding: 1rem;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin: 0.5rem;
`;

export const PriceCard = ({ data }: { data: MarketPrice }) => {
  const { exchange, symbol, currency, price, changeRate } = data;
  
  return (
    <Card>
      <h3>{exchange}</h3>
      <div>{symbol}/{currency}</div>
      <div>{price.toLocaleString()}</div>
      {changeRate && (
        <div style={{ color: changeRate > 0 ? 'green' : 'red' }}>
          {(changeRate * 100).toFixed(2)}%
        </div>
      )}
    </Card>
  );
};

export const PriceBoard = ({ prices }: { prices: MarketPrice[] }) => {
  return (
    <Grid>
      {prices.map((price) => (
        <PriceCard 
          key={`${price.exchange}-${price.symbol}-${price.currency}`} 
          data={price} 
        />
      ))}
    </Grid>
  );
};