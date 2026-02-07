import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

/**
 * WebSocket client for real-time notifications
 */
class NotificationWebSocket {
  constructor() {
    this.client = null;
    this.connected = false;
    this.userId = null;
    this.subscription = null;
  }

  /**
   * Connect to WebSocket server
   * @param {number} userId - User ID for personalized notifications
   */
  connect(userId) {
    return new Promise((resolve, reject) => {
      this.userId = userId;
      const socket = new SockJS('http://localhost:8080/ws-stock-prices');
      
      this.client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        
        onConnect: () => {
          console.log('Notification WebSocket connected for user:', userId);
          this.connected = true;
          
          // Auto-subscribe to user's notifications
          this.subscribeToNotifications(userId);
          resolve();
        },
        
        onDisconnect: () => {
          console.log('Notification WebSocket disconnected');
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
   * Subscribe to notifications for a specific user
   * @param {number} userId - User ID
   * @param {Function} callback - Callback function to handle notifications
   */
  subscribeToNotifications(userId, callback) {
    if (!this.connected) {
      console.error('WebSocket not connected');
      return;
    }

    this.subscription = this.client.subscribe(
      `/topic/notifications/${userId}`,
      (message) => {
        const notification = JSON.parse(message.body);
        console.log('Received notification:', notification);
        
        // Show browser notification if permission granted
        this.showBrowserNotification(notification);
        
        if (callback) {
          callback(notification);
        }
      }
    );
  }

  /**
   * Show browser notification
   */
  showBrowserNotification(notification) {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: '/logo.png',
        badge: '/logo.png',
        tag: notification.id.toString(),
        requireInteraction: notification.type === 'ALERT'
      });
    }
  }

  /**
   * Request browser notification permission
   */
  async requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
      const permission = await Notification.requestPermission();
      return permission === 'granted';
    }
    return Notification.permission === 'granted';
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }

    if (this.client) {
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
const notificationWebSocket = new NotificationWebSocket();

export default notificationWebSocket;
