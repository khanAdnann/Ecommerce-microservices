import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import notificationService from '../../services/notificationService';

// Async thunks
export const fetchNotifications = createAsyncThunk(
  'notifications/fetchNotifications',
  async ({ page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'desc' }, { rejectWithValue }) => {
    try {
      const response = await notificationService.getNotifications(page, size, sortBy, sortDirection);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch notifications');
    }
  }
);

export const markAsRead = createAsyncThunk(
  'notifications/markAsRead',
  async (notificationId, { rejectWithValue }) => {
    try {
      const response = await notificationService.markAsRead(notificationId);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to mark notification as read');
    }
  }
);

export const markAllAsRead = createAsyncThunk(
  'notifications/markAllAsRead',
  async (_, { rejectWithValue }) => {
    try {
      await notificationService.markAllAsRead();
      return true;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to mark all notifications as read');
    }
  }
);

export const getUnreadCount = createAsyncThunk(
  'notifications/getUnreadCount',
  async (_, { rejectWithValue }) => {
    try {
      const response = await notificationService.getUnreadCount();
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to get unread count');
    }
  }
);

export const createNotification = createAsyncThunk(
  'notifications/createNotification',
  async (notificationData, { rejectWithValue }) => {
    try {
      const response = await notificationService.createNotification(notificationData);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to create notification');
    }
  }
);

// Initial state
const initialState = {
  notifications: [],
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
  unreadCount: 0,
  loading: false,
  error: null,
  lastFetched: null,
};

// Slice
const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    resetState: (state) => {
      state.notifications = [];
      state.currentPage = 0;
      state.totalPages = 0;
      state.totalElements = 0;
      state.unreadCount = 0;
      state.loading = false;
      state.error = null;
      state.lastFetched = null;
    },
    addRealtimeNotification: (state, action) => {
      const notification = action.payload;
      state.notifications.unshift(notification);
      state.totalElements += 1;
      if (!notification.readAt) {
        state.unreadCount += 1;
      }
    },
    updateNotificationStatus: (state, action) => {
      const { notificationId, status } = action.payload;
      const notification = state.notifications.find(n => n.id === notificationId);
      if (notification) {
        notification.status = status;
        if (status === 'READ' && !notification.readAt) {
          notification.readAt = new Date().toISOString();
          state.unreadCount = Math.max(0, state.unreadCount - 1);
        }
      }
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch notifications
      .addCase(fetchNotifications.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        state.loading = false;
        state.notifications = action.payload.content || action.payload;
        state.currentPage = action.payload.number || 0;
        state.totalPages = action.payload.totalPages || 1;
        state.totalElements = action.payload.totalElements || 0;
        state.lastFetched = new Date().toISOString();
      })
      .addCase(fetchNotifications.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Mark as read
      .addCase(markAsRead.fulfilled, (state, action) => {
        const updatedNotification = action.payload;
        const index = state.notifications.findIndex(n => n.id === updatedNotification.id);
        if (index !== -1) {
          state.notifications[index] = updatedNotification;
        }
        if (updatedNotification.status === 'READ') {
          state.unreadCount = Math.max(0, state.unreadCount - 1);
        }
      })
      .addCase(markAsRead.rejected, (state, action) => {
        state.error = action.payload;
      })
      
      // Mark all as read
      .addCase(markAllAsRead.fulfilled, (state) => {
        state.notifications.forEach(notification => {
          if (notification.status !== 'READ') {
            notification.status = 'READ';
            notification.readAt = new Date().toISOString();
          }
        });
        state.unreadCount = 0;
      })
      .addCase(markAllAsRead.rejected, (state, action) => {
        state.error = action.payload;
      })
      
      // Get unread count
      .addCase(getUnreadCount.fulfilled, (state, action) => {
        state.unreadCount = action.payload;
      })
      .addCase(getUnreadCount.rejected, (state, action) => {
        state.error = action.payload;
      })
      
      // Create notification
      .addCase(createNotification.fulfilled, (state, action) => {
        state.notifications.unshift(action.payload);
        state.totalElements += 1;
        if (action.payload.status !== 'READ') {
          state.unreadCount += 1;
        }
      })
      .addCase(createNotification.rejected, (state, action) => {
        state.error = action.payload;
      });
  },
});

// Actions
export const { clearError, resetState, addRealtimeNotification, updateNotificationStatus } = notificationSlice.actions;

// Selectors
export const selectNotifications = (state) => state.notifications.notifications;
export const selectNotificationsLoading = (state) => state.notifications.loading;
export const selectNotificationsError = (state) => state.notifications.error;
export const selectUnreadCount = (state) => state.notifications.unreadCount;
export const selectCurrentPage = (state) => state.notifications.currentPage;
export const selectTotalPages = (state) => state.notifications.totalPages;
export const selectTotalElements = (state) => state.notifications.totalElements;

// Reducer
export default notificationSlice.reducer;
