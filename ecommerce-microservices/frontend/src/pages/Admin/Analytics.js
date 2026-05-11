import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Paper,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  TrendingUp,
  ShoppingCart,
  People,
  AttachMoney,
} from '@mui/icons-material';

const Analytics = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [analytics, setAnalytics] = useState({
    totalRevenue: 0,
    totalOrders: 0,
    totalUsers: 0,
    totalProducts: 0,
    recentOrders: [],
    topProducts: [],
    revenueByMonth: [],
  });

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    setLoading(true);
    setError('');
    
    try {
      // This would be replaced with actual API call
      // const response = await adminService.getAnalytics();
      // setAnalytics(response.data);
      
      // Mock data for now
      const mockAnalytics = {
        totalRevenue: 125432.50,
        totalOrders: 847,
        totalUsers: 1234,
        totalProducts: 156,
        recentOrders: [
          { id: 1, orderNumber: 'ORD-001', amount: 129.99, customer: 'John Doe' },
          { id: 2, orderNumber: 'ORD-002', amount: 89.99, customer: 'Jane Smith' },
        ],
        topProducts: [
          { id: 1, name: 'Laptop Pro', sales: 45, revenue: 58495.50 },
          { id: 2, name: 'Wireless Mouse', sales: 234, revenue: 7019.46 },
        ],
        revenueByMonth: [
          { month: 'Jan', revenue: 15432.50 },
          { month: 'Feb', revenue: 18976.80 },
          { month: 'Mar', revenue: 22123.20 },
        ],
      };
      
      setAnalytics(mockAnalytics);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch analytics data');
    } finally {
      setLoading(false);
    }
  };

  const StatCard = ({ title, value, icon, color }) => (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="center">
          <Box
            sx={{
              backgroundColor: color,
              color: 'white',
              borderRadius: 1,
              p: 1,
              mr: 2,
            }}
          >
            {icon}
          </Box>
          <Box>
            <Typography variant="h4" component="div">
              {typeof value === 'number' ? value.toLocaleString() : value}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box m={3}>
      <Typography variant="h4" component="h1" gutterBottom>
        Analytics Dashboard
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Revenue"
            value={`$${analytics.totalRevenue.toFixed(2)}`}
            icon={<AttachMoney />}
            color="success.main"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Orders"
            value={analytics.totalOrders}
            icon={<ShoppingCart />}
            color="primary.main"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Users"
            value={analytics.totalUsers}
            icon={<People />}
            color="info.main"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Products"
            value={analytics.totalProducts}
            icon={<TrendingUp />}
            color="warning.main"
          />
        </Grid>

        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Revenue by Month
            </Typography>
            <Box>
              {analytics.revenueByMonth.map((item, index) => (
                <Box key={index} mb={2}>
                  <Box display="flex" justifyContent="space-between" mb={1}>
                    <Typography variant="body2">{item.month}</Typography>
                    <Typography variant="body2">
                      ${item.revenue.toFixed(2)}
                    </Typography>
                  </Box>
                  <Box
                    sx={{
                      backgroundColor: 'primary.main',
                      height: 8,
                      borderRadius: 1,
                      width: `${(item.revenue / Math.max(...analytics.revenueByMonth.map(r => r.revenue))) * 100}%`,
                    }}
                  />
                </Box>
              ))}
            </Box>
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Recent Orders
            </Typography>
            {analytics.recentOrders.map((order) => (
              <Box key={order.id} mb={2}>
                <Typography variant="body2" fontWeight="medium">
                  {order.orderNumber}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {order.customer}
                </Typography>
                <Typography variant="body2">
                  ${order.amount.toFixed(2)}
                </Typography>
              </Box>
            ))}
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Top Products
            </Typography>
            {analytics.topProducts.map((product) => (
              <Box key={product.id} mb={2}>
                <Box display="flex" justifyContent="space-between" mb={1}>
                  <Typography variant="body2" fontWeight="medium">
                    {product.name}
                  </Typography>
                  <Typography variant="body2">
                    {product.sales} sold
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Revenue: ${product.revenue.toFixed(2)}
                </Typography>
              </Box>
            ))}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Analytics;
