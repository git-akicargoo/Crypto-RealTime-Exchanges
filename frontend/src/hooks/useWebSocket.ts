import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';

export const useWebSocket = () => {
  const [isConnected, setIsConnected] = useState(false);
  const [prices, setPrices] = useState<any[]>([]);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    console.log('Initializing WebSocket connection...');
    
    // 이전 연결이 있다면 정리
    if (clientRef.current?.active) {
      console.log('Deactivating existing client...');
      clientRef.current.deactivate();
      clientRef.current = null;
    }

    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws/websocket',
      debug: function (str) {
        console.log('STOMP:', str);
      },
    });

    client.onConnect = (frame) => {
      console.log('STOMP Connected:', frame);
      setIsConnected(true);
      
      // 가격 데이터 구독
      client.subscribe('/subscribe/crypto/prices', (message) => {
        console.log('Received price data:', message.body);
        try {
          const data = JSON.parse(message.body);
          setPrices(prev => {
            console.log('Current prices:', prev);
            const updated = [...prev];
            const index = updated.findIndex(p => 
              p.exchange === data.exchange && p.symbol === data.symbol
            );
            if (index >= 0) {
              updated[index] = data;
            } else {
              updated.push(data);
            }
            console.log('Updated prices:', updated);
            return updated;
          });
        } catch (error) {
          console.error('Error parsing price data:', error);
        }
      });

      // 구독 요청 전송
      client.publish({
        destination: '/publish/crypto/subscribe',
        body: JSON.stringify({
          symbols: ['BTC', 'ETH', 'XRP', 'DOGE']
        })
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
      setIsConnected(false);
    };

    client.onWebSocketError = (event) => {
      console.error('WebSocket error:', event);
      setIsConnected(false);
    };

    client.onWebSocketClose = () => {
      console.log('WebSocket closed');
      setIsConnected(false);
    };

    try {
      console.log('Activating STOMP client...');
      client.activate();
      clientRef.current = client;
    } catch (error) {
      console.error('Error activating client:', error);
    }

    // cleanup 함수
    return () => {
      console.log('Cleanup called, current client:', clientRef.current?.active);
      if (clientRef.current?.active) {
        console.log('Deactivating client in cleanup...');
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
  }, []); // 의존성 배열 비움

  return { isConnected, prices };
}; 