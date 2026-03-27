import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

/**
 * WebSocket service for real-time stock price updates.
 * Handles connection, subscription, and reconnection logic.
 */
class StockPriceWebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000; // 3 seconds
  }

  /**
   * Connect to WebSocket server.
   * @param {string} url - WebSocket server URL (e.g., 'http://localhost:8080/ws-stock-prices')
   * @param {function} onConnect - Callback when connection is established
   * @param {function} onError - Callback when error occurs
   */
  connect(url = 'http://localhost:8080/ws-stock-prices', onConnect, onError) {
    try {
      // Create SockJS connection
      const socket = new SockJS(url);

      // Create STOMP client
      this.client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => {
          console.log('[WebSocket Debug]', str);
        },
        reconnectDelay: this.reconnectDelay,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      // Connection success handler
      this.client.onConnect = (frame) => {
        console.log('[WebSocket] Connected:', frame);
        this.connected = true;
        this.reconnectAttempts = 0;
        
        if (onConnect) {
          onConnect(frame);
        }
      };

      // Connection error handler
      this.client.onStompError = (frame) => {
        console.error('[WebSocket] Error:', frame);
        this.connected = false;
        
        if (onError) {
          onError(frame);
        }
        
        this.handleReconnect(url, onConnect, onError);
      };

      // WebSocket close handler
      this.client.onWebSocketClose = () => {
        console.warn('[WebSocket] Connection closed');
        this.connected = false;
        this.handleReconnect(url, onConnect, onError);
      };

      // Activate the connection
      this.client.activate();
    } catch (error) {
      console.error('[WebSocket] Connection failed:', error);
      if (onError) {
        onError(error);
      }
    }
  }

  /**
   * Handle reconnection logic.
   */
  handleReconnect(url, onConnect, onError) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`[WebSocket] Reconnecting... Attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
      
      setTimeout(() => {
        this.connect(url, onConnect, onError);
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.error('[WebSocket] Max reconnection attempts reached');
    }
  }

  /**
   * Subscribe to all stock price updates.
   * @param {function} callback - Function to call when price updates are received
   * @returns {string} Subscription ID
   */
  subscribeToAllStocks(callback) {
    if (!this.connected || !this.client) {
      console.error('[WebSocket] Not connected. Cannot subscribe.');
      return null;
    }

    const subscription = this.client.subscribe('/topic/stock-prices', (message) => {
      try {
        const priceUpdates = JSON.parse(message.body);
        callback(priceUpdates);
      } catch (error) {
        console.error('[WebSocket] Error parsing message:', error);
      }
    });

    const subscriptionId = 'all-stocks';
    this.subscriptions.set(subscriptionId, subscription);
    console.log('[WebSocket] Subscribed to all stock prices');
    
    return subscriptionId;
  }

  /**
   * Subscribe to a specific stock's price updates.
   * @param {string} symbol - Stock symbol (e.g., 'RELIANCE', 'TCS')
   * @param {function} callback - Function to call when price update is received
   * @returns {string} Subscription ID
   */
  subscribeToStock(symbol, callback) {
    if (!this.connected || !this.client) {
      console.error('[WebSocket] Not connected. Cannot subscribe.');
      return null;
    }

    const subscription = this.client.subscribe(`/topic/stock-price/${symbol}`, (message) => {
      try {
        const priceUpdate = JSON.parse(message.body);
        callback(priceUpdate);
      } catch (error) {
        console.error('[WebSocket] Error parsing message:', error);
      }
    });

    const subscriptionId = `stock-${symbol}`;
    this.subscriptions.set(subscriptionId, subscription);
    console.log(`[WebSocket] Subscribed to ${symbol} price updates`);
    
    return subscriptionId;
  }

  /**
   * Request price for a specific stock.
   * @param {string} symbol - Stock symbol
   */
  requestStockPrice(symbol) {
    if (!this.connected || !this.client) {
      console.error('[WebSocket] Not connected. Cannot send message.');
      return;
    }

    this.client.publish({
      destination: `/app/stock-price/${symbol}`,
      body: JSON.stringify({ symbol }),
    });
  }

  /**
   * Unsubscribe from a specific subscription.
   * @param {string} subscriptionId - The subscription ID returned from subscribe methods
   */
  unsubscribe(subscriptionId) {
    const subscription = this.subscriptions.get(subscriptionId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(subscriptionId);
      console.log(`[WebSocket] Unsubscribed from ${subscriptionId}`);
    }
  }

  /**
   * Unsubscribe from all subscriptions.
   */
  unsubscribeAll() {
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
    console.log('[WebSocket] Unsubscribed from all topics');
  }

  /**
   * Disconnect from WebSocket server.
   */
  disconnect() {
    if (this.client) {
      this.unsubscribeAll();
      this.client.deactivate();
      this.connected = false;
      console.log('[WebSocket] Disconnected');
    }
  }

  /**
   * Check if connected.
   */
  isConnected() {
    return this.connected;
  }
}

// Export singleton instance
const stockPriceWebSocket = new StockPriceWebSocketService();
export default stockPriceWebSocket;
