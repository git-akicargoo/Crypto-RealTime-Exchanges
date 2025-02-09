import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import ExchangePage from '@/pages/exchange/exchange';
import TestPage from '@/pages/test/test';
import './App.css'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/exchange" replace />} />
        <Route path="/exchange" element={<ExchangePage />} />
        <Route path="/test" element={<TestPage />} />
      </Routes>
    </Router>
  );
}

export default App;
