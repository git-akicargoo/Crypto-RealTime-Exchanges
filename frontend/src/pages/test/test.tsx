import { useEffect, useState, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Button, Container, MessageBox } from '@/components/shared/styles';

const TestPage = () => {
  const [messages, setMessages] = useState<string[]>([]);
  const [stompClient, setStompClient] = useState<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [retryCount, setRetryCount] = useState(0);
  const MAX_RETRIES = 5;
  const RETRY_DELAY = 2000;
  const clientRef = useRef<Client | null>(null);

  const connect = useCallback(() => {
    console.log('Creating SockJS connection...');
    
    if (clientRef.current?.active) {
      clientRef.current.deactivate();
    }

    const sock = new SockJS('http://localhost:8080/ws');
    
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws/websocket',
      debug: function (str) {
        console.log('STOMP: ' + str);
      },
    });

    client.onConnect = (frame) => {
      console.log('STOMP Connected:', frame);
      setIsConnected(true);
      setStompClient(client);
      setRetryCount(0);
      
      client.subscribe('/subscribe/test', (message) => {
        console.log('Received:', message.body);
        setMessages(prev => [...prev, message.body]);
      });

      client.publish({
        destination: '/publish/test/connect',
        body: 'Connected!'
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
      setIsConnected(false);
    };

    client.onWebSocketError = (event) => {
      console.error('WebSocket Error:', event);
      setIsConnected(false);
    };

    client.onWebSocketClose = () => {
      console.log('WebSocket closed');
      setIsConnected(false);
      
      if (retryCount < MAX_RETRIES) {
        const nextRetry = retryCount + 1;
        console.log(`Reconnecting in ${RETRY_DELAY}ms... (Attempt ${nextRetry}/${MAX_RETRIES})`);
        setTimeout(() => {
          setRetryCount(nextRetry);
          connect();
        }, RETRY_DELAY);
      }
    };

    try {
      console.log('Activating STOMP client...');
      client.activate();
      clientRef.current = client;
    } catch (error) {
      console.error('Error activating client:', error);
    }

    return () => {
      console.log('Cleaning up connection...');
      if (client.active) {
        client.deactivate();
      }
    };
  }, [retryCount]);

  useEffect(() => {
    const cleanup = connect();
    return () => {
      cleanup();
    };
  }, [connect]);

  const sendTestMessage = () => {
    if (stompClient?.active) {
      stompClient.publish({
        destination: '/publish/test/message',
        body: 'Test message from client!'
      });
    } else {
      console.warn('STOMP client not connected');
    }
  };

  return (
    <Container>
      <h1>WebSocket Test</h1>
      <div>Connection status: {isConnected ? 'Connected' : 'Disconnected'}</div>
      <div>Retry count: {retryCount}/{MAX_RETRIES}</div>
      <Button onClick={sendTestMessage}>Send Test Message</Button>
      
      <h2>Messages:</h2>
      {messages.map((msg, index) => (
        <MessageBox key={index}>{msg}</MessageBox>
      ))}
    </Container>
  );
};

export default TestPage;