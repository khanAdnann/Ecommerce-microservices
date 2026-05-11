import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Paper,
  CircularProgress,
  Alert,
  List,
  ListItem,
  ListItemText,
  Divider,
  Button
} from '@mui/material';
import {
  ShoppingCart,
  People,
  AttachMoney,
  TrendingUp,
  Inventory,
  Star,
  Notifications
} from '@mui/icons-material';

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalOrders: 0,
    totalRevenue: 0,
    totalUsers: 0,
    totalProducts: 0,
    recentOrders: [],
    topProducts: [],
    lowStockItems: []
  });

  useEffect(() => {
    // Simulate API call to fetch dashboard stats
    const fetchDashboardStats = async () => {
      try {
        // Mock data - in real app, this would be API calls
        const mockStats = {
          totalOrders: 1250,
          totalRevenue: 45678.90,
          totalUsers: 3421,
          totalProducts: 156,
          recentOrders: [
            { id: 1, orderNumber: 'ORD-001', customer: 'John Doe', amount: 129.99, status: 'DELIVERED' },
            { id: 2, orderNumber: 'ORD-002', customer: 'Jane Smith', amount: 89.99, status: 'SHIPPED' },
            { id: 3, orderNumber: 'ORD-003', customer: 'Bob Johnson', amount: 199.99, status: 'PROCESSING' },
            { id: 4, orderNumber: 'ORD-004', customer: 'Alice Brown', amount: 45.99, status: 'PENDING' },
            { id: 5, orderNumber: 'ORD-005', customer: 'Charlie Wilson', amount: 299.99, status: 'CONFIRMED' }
          ],
          topProducts: [
            { id: 1, name: 'Laptop Pro 15"', sales: 45, revenue: 58499.55 },
            { id: 2, name: 'Wireless Headphones', sales: 120, revenue: 23998.80 },
            { id: 3, name: 'Running Shoes', sales: 89, revenue: 8009.11 },
            { id: 4, name: 'Coffee Maker', sales: 67, revenue: 5359.33 },
            { id: 5, name: 'Smart Watch', sales: 34, revenue: 16996.66 }
          ],
          lowStockItems: [
            { id: 1, name: 'USB Cable', stock: 5, minStock: 10 },
            { id: 2, name: 'Phone Case', stock: 3, minStock: 15 },
            { id: 3, name: 'Screen Protector', stock: 8, minStock: 20 }
          ]
        };
        
        setStats(mockStats);
      } catch (error) {
        console.error('Failed to fetch dashboard stats:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardStats();
  }, []);

  const StatCard = ({ title, value, icon, color = 'primary' }) => (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography color="textSecondary" gutterBottom variant="overline">
              {title}
            </Typography>
            <Typography variant="h4" component="div">
              {value}
            </Typography>
          </Box>
          <Box sx={{ color: `${color}.main` }}>
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Admin Dashboard
      </Typography>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Orders"
            value={stats.totalOrders.toLocaleString()}
            icon={<ShoppingCart sx={{ fontSize: 40 }} />}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Revenue"
            value={`$${stats.totalRevenue.toLocaleString()}`}
            icon={<AttachMoney sx={{ fontSize: 40 }} />}
            color="success"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Users"
            value={stats.totalUsers.toLocaleString()}
            icon={<People sx={{ fontSize: 40 }} />}
            color="info"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Total Products"
            value={stats.totalProducts.toLocaleString()}
            icon={<Inventory sx={{ fontSize: 40 }} />}
            color="warning"
          />
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Recent Orders */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Recent Orders
            </Typography>
            <List>
              {stats.recentOrders.map((order, index) => (
                <React.Fragment key={order.id}>
                  <ListItem>
                    <ListItemText
                      primary={order.orderNumber}
                      secondary={`${order.customer} - $${order.amount} - ${order.status}`}
                    />
                  </ListItem>
                  {index < stats.recentOrders.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* Top Products */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
              <TrendingUp sx={{ mr: 1 }} />
              Top Products
            </Typography>
            <List>
              {stats.topProducts.map((product, index) => (
                <React.Fragment key={product.id}>
                  <ListItem>
                    <ListItemText
                      primary={product.name}
                      secondary={`${product.sales} sold - $${product.revenue.toFixed(2)}`}
                    />
                  </ListItem>
                  {index < stats.topProducts.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* Low Stock Items */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', color: 'error.main' }}>
              <Notifications sx={{ mr: 1 }} />
              Low Stock Items
            </Typography>
            <List>
              {stats.lowStockItems.map((item, index) => (
                <React.Fragment key={item.id}>
                  <ListItem>
                    <ListItemText
                      primary={item.name}
                      secondary={`Stock: ${item.stock} (Min: ${item.minStock})`}
                    />
                  </ListItem>
                  {index < stats.lowStockItems.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </Paper>
        </Grid>

        {/* Quick Actions */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Quick Actions
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Button variant="outlined" fullWidth>
                  View All Orders
                </Button>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Button variant="outlined" fullWidth>
                  Manage Products
                </Button>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Button variant="outlined" fullWidth>
                  View Users
                </Button>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Button variant="outlined" fullWidth>
                  Analytics
                </Button>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
