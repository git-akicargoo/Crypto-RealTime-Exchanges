import { useEffect, useState } from 'react';
import styled from 'styled-components';
import { MarketPrice } from './types';
import { PriceBoard } from './components.tsx';

const PageContainer = styled.div`
  padding: 20px;
`;

const Title = styled.h1`
  font-size: 24px;
  margin-bottom: 20px;
`;

const MarketPage = () => {
  const [prices, setPrices] = useState<MarketPrice[]>([]);
  const wsUrl = `${import.meta.env.VITE_WS_URL}/api/crypto/prices`;

  useEffect(() => {
    const ws = new WebSocket(wsUrl);
    
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      setPrices(prev => {
        const index = prev.findIndex(p => 
          p.exchange === data.exchange && 
          p.symbol === data.symbol &&
          p.currency === data.currency
        );
        
        if (index === -1) {
          return [...prev, data];
        }
        
        const newPrices = [...prev];
        newPrices[index] = data;
        return newPrices;
      });
    };
    
    return () => ws.close();
  }, []);

  return (
    <PageContainer>
      <Title>실시간 시세</Title>
      <PriceBoard prices={prices} />
    </PageContainer>
  );
};

export default MarketPage; 
