import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

class NotificationService {
  // Get all notifications for the authenticated user
  async getNotifications(page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'desc') {
    try {
      const response = await axios.get(`${API_BASE_URL}/notifications/my`, {
        params: { page, size, sortBy, sortDirection }
      });
      return response;
    } catch (error) {
      console.error('Error fetching notifications:', error);
      throw error;
    }
  }

  // Get all notifications (Admin only)
  async getAllNotifications(page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'desc') {
    try {
      const response = await axios.get(`${API_BASE_URL}/notifications`, {
        params: { page, size, sortBy, sortDirection }
      });
      return response;
    } catch (error) {
      console.error('Error fetching all notifications:', error);
      throw error;
    }
  }

  // Get notifications by type (Admin only)
  async getNotificationsByType(type, page = 0, size = 10) {
    try {
      const response = await axios.get(`${API_BASE_URL}/notifications/type/${type}`, {
        params: { page, size }
      });
      return response;
    } catch (error) {
      console.error('Error fetching notifications by type:', error);
      throw error;
    }
  }

  // Mark a notification as read
  async markAsRead(notificationId) {
    try {
      const response = await axios.put(`${API_BASE_URL}/notifications/${notificationId}/read`);
      return response;
    } catch (error) {
      console.error('Error marking notification as read:', error);
      throw error;
    }
  }

  // Mark all notifications as read
  async markAllAsRead() {
    try {
      const response = await axios.put(`${API_BASE_URL}/notifications/read-all`);
      return response;
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
      throw error;
    }
  }

  // Get unread count
  async getUnreadCount() {
    try {
      const response = await axios.get(`${API_BASE_URL}/notifications/unread-count`);
      return response;
    } catch (error) {
      console.error('Error fetching unread count:', error);
      throw error;
    }
  }

  // Create a new notification (Admin only)
  async createNotification(notificationData) {
    try {
      const response = await axios.post(`${API_BASE_URL}/notifications`, notificationData);
      return response;
    } catch (error) {
      console.error('Error creating notification:', error);
      throw error;
    }
  }

  // Get notification by ID
  async getNotificationById(notificationId) {
    try {
      const response = await axios.get(`${API_BASE_URL}/notifications/${notificationId}`);
      return response;
    } catch (error) {
      console.error('Error fetching notification by ID:', error);
      throw error;
    }
  }

  // WebSocket connection for real-time notifications
  connectWebSocket(userId, onNotificationReceived) {
    const wsUrl = process.env.REACT_APP_WS_URL || 'ws://localhost:8080/ws';
    const socket = new WebSocket(`${wsUrl}/notifications/${userId}`);

    socket.onopen = () => {
      console.log('WebSocket connected for notifications');
    };

    socket.onmessage = (event) => {
      try {
        const notification = JSON.parse(event.data);
        onNotificationReceived(notification);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
      }
    };

    socket.onclose = (event) => {
      console.log('WebSocket disconnected:', event);
      // Attempt to reconnect after 5 seconds
      setTimeout(() => {
        this.connectWebSocket(userId, onNotificationReceived);
      }, 5000);
    };

    socket.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    return socket;
  }

  // Send notification through WebSocket (for testing)
  sendWebSocketNotification(socket, notification) {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify(notification));
    }
  }

  // Disconnect WebSocket
  disconnectWebSocket(socket) {
    if (socket) {
      socket.close();
    }
  }
}

export default new NotificationService();
