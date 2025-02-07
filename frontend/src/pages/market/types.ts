export interface MarketPrice {
  exchange: string;
  symbol: string;
  currency: string;
  price: number;
  volume?: number;
  timestamp: string;
  highPrice?: number;
  lowPrice?: number;
  openPrice?: number;
  changeRate?: number;
}