import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

/**
 * WebSocket client for real-time stock price updates
 */
class StockPriceWebSocket {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
  }

  /**
   * Connect to WebSocket server
   */
  connect() {
    return new Promise((resolve, reject) => {
      const socket = new SockJS('http://localhost:8080/ws-stock-prices');
      
      this.client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        
        onConnect: () => {
          console.log('Stock price WebSocket connected');
          this.connected = true;
          resolve();
        },
        
        onDisconnect: () => {
          console.log('Stock price WebSocket disconnected');
          this.connected = false;
        },
        
        onStompError: (frame) => {
          console.error('STOMP error:', frame);
          reject(frame);
        }
      });

      this.client.activate();
    });
  }

  /**
   * Subscribe to stock price updates for a specific symbol
   * @param {string} symbol - Stock symbol (e.g., "RELIANCE")
   * @param {Function} callback - Callback function to handle price updates
   * @returns {string} - Subscription ID
   */
  subscribeToSymbol(symbol, callback) {
    if (!this.connected) {
      console.error('WebSocket not connected');
      return null;
    }

    const subscription = this.client.subscribe(
      `/topic/stock-prices/${symbol}`,
      (message) => {
        const priceData = JSON.parse(message.body);
        callback(priceData);
      }
    );

    this.subscriptions.set(symbol, subscription);
    return subscription.id;
  }

  /**
   * Subscribe to portfolio updates
   * @param {Function} callback - Callback function to handle portfolio updates
   * @returns {string} - Subscription ID
   */
  subscribeToPortfolioUpdates(callback) {
    if (!this.connected) {
      console.error('WebSocket not connected');
      return null;
    }

    const subscription = this.client.subscribe(
      '/topic/portfolio-updates',
      (message) => {
        const updateData = JSON.parse(message.body);
        callback(updateData);
      }
    );

    this.subscriptions.set('portfolio', subscription);
    return subscription.id;
  }

  /**
   * Unsubscribe from a symbol
   * @param {string} symbol - Stock symbol to unsubscribe from
   */
  unsubscribeFromSymbol(symbol) {
    const subscription = this.subscriptions.get(symbol);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(symbol);
    }
  }

  /**
   * Unsubscribe from all symbols
   */
  unsubscribeAll() {
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    if (this.client) {
      this.unsubscribeAll();
      this.client.deactivate();
      this.connected = false;
    }
  }

  /**
   * Check if connected
   */
  isConnected() {
    return this.connected;
  }
}

// Create singleton instance
const stockPriceWebSocket = new StockPriceWebSocket();

export default stockPriceWebSocket;
