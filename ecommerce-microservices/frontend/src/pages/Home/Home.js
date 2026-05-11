import React, { useEffect, useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Button,
  Chip,
  Rating,
  Carousel,
  CircularProgress
} from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchFeaturedProducts, fetchLatestProducts } from '../../redux/slices/productSlice';

const Home = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { featuredProducts, latestProducts, isLoading } = useSelector((state) => state.products);
  const { isAuthenticated } = useSelector((state) => state.auth);

  const [currentSlide, setCurrentSlide] = useState(0);

  useEffect(() => {
    dispatch(fetchFeaturedProducts({ page: 0, size: 6 }));
    dispatch(fetchLatestProducts({ page: 0, size: 8 }));
  }, [dispatch]);

  const handleProductClick = (productId) => {
    navigate(`/products/${productId}`);
  };

  const handleAddToCart = (productId) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    // Add to cart logic here
  };

  const banners = [
    {
      title: "Summer Sale",
      subtitle: "Up to 50% off on selected items",
      image: "/banner1.jpg",
      cta: "Shop Now"
    },
    {
      title: "New Arrivals",
      subtitle: "Check out our latest products",
      image: "/banner2.jpg",
      cta: "Explore"
    },
    {
      title: "Free Shipping",
      subtitle: "On orders over $100",
      image: "/banner3.jpg",
      cta: "Learn More"
    }
  ];

  return (
    <Box>
      {/* Hero Banner */}
      <Box sx={{ bgcolor: 'primary.main', color: 'white', py: 8, mb: 6 }}>
        <Container maxWidth="lg">
          <Grid container spacing={4} alignItems="center">
            <Grid item xs={12} md={6}>
              <Typography variant="h2" component="h1" gutterBottom>
                Welcome to E-Commerce Platform
              </Typography>
              <Typography variant="h5" paragraph>
                Discover amazing products at great prices
              </Typography>
              <Button
                variant="contained"
                size="large"
                sx={{ bgcolor: 'white', color: 'primary.main', '&:hover': { bgcolor: 'grey.100' } }}
                onClick={() => navigate('/products')}
              >
                Start Shopping
              </Button>
            </Grid>
            <Grid item xs={12} md={6}>
              <Box
                sx={{
                  height: 400,
                  backgroundColor: 'rgba(255, 255, 255, 0.1)',
                  borderRadius: 2,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
                <Typography variant="h6">Hero Image</Typography>
              </Box>
            </Grid>
          </Grid>
        </Container>
      </Box>

      <Container maxWidth="lg">
        {/* Featured Products */}
        <Box sx={{ mb: 8 }}>
          <Typography variant="h4" gutterBottom>
            Featured Products
          </Typography>
          
          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <Grid container spacing={3}>
              {featuredProducts?.map((product) => (
                <Grid item xs={12} sm={6} md={4} key={product.id}>
                  <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                    <CardMedia
                      component="div"
                      sx={{
                        height: 200,
                        backgroundColor: '#f5f5f5',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        cursor: 'pointer'
                      }}
                      onClick={() => handleProductClick(product.id)}
                    >
                      {product.images?.[0] ? (
                        <img
                          src={product.images[0]}
                          alt={product.name}
                          style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}
                        />
                      ) : (
                        <Typography variant="body2" color="text.secondary">
                          No Image
                        </Typography>
                      )}
                    </CardMedia>
                    <CardContent sx={{ flexGrow: 1 }}>
                      <Typography
                        variant="h6"
                        sx={{
                          cursor: 'pointer',
                          '&:hover': { color: 'primary.main' }
                        }}
                        onClick={() => handleProductClick(product.id)}
                      >
                        {product.name}
                      </Typography>
                      
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {product.brand}
                      </Typography>

                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <Rating
                          value={product.rating}
                          precision={0.5}
                          size="small"
                          readOnly
                        />
                        <Typography variant="body2" sx={{ ml: 1 }}>
                          ({product.reviewCount})
                        </Typography>
                      </Box>

                      <Chip label="Featured" color="primary" size="small" sx={{ mb: 1 }} />

                      <Typography variant="h6" color="primary" sx={{ mb: 2 }}>
                        ${product.price}
                      </Typography>

                      <Button
                        variant="contained"
                        fullWidth
                        onClick={() => handleAddToCart(product.id)}
                        disabled={!isAuthenticated}
                      >
                        Add to Cart
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
          
          <Box sx={{ textAlign: 'center', mt: 3 }}>
            <Button
              variant="outlined"
              size="large"
              onClick={() => navigate('/products')}
            >
              View All Products
            </Button>
          </Box>
        </Box>

        {/* Categories */}
        <Box sx={{ mb: 8 }}>
          <Typography variant="h4" gutterBottom>
            Shop by Category
          </Typography>
          
          <Grid container spacing={3}>
            {[
              { name: 'Electronics', icon: '📱', color: '#2196F3' },
              { name: 'Clothing', icon: '👕', color: '#4CAF50' },
              { name: 'Books', icon: '📚', color: '#FF9800' },
              { name: 'Home & Garden', icon: '🏠', color: '#9C27B0' },
              { name: 'Sports', icon: '⚽', color: '#F44336' },
              { name: 'Toys', icon: '🎮', color: '#00BCD4' }
            ].map((category) => (
              <Grid item xs={6} sm={4} md={2} key={category.name}>
                <Card
                  sx={{
                    textAlign: 'center',
                    cursor: 'pointer',
                    transition: 'transform 0.2s',
                    '&:hover': {
                      transform: 'translateY(-5px)',
                      boxShadow: 4
                    }
                  }}
                  onClick={() => navigate(`/products?category=${category.name}`)}
                >
                  <CardContent>
                    <Typography variant="h3" sx={{ mb: 1 }}>
                      {category.icon}
                    </Typography>
                    <Typography variant="body1" fontWeight="bold">
                      {category.name}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>

        {/* Latest Products */}
        <Box sx={{ mb: 8 }}>
          <Typography variant="h4" gutterBottom>
            Latest Arrivals
          </Typography>
          
          <Grid container spacing={3}>
            {latestProducts?.slice(0, 4).map((product) => (
              <Grid item xs={12} sm={6} md={3} key={product.id}>
                <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                  <CardMedia
                    component="div"
                    sx={{
                      height: 150,
                      backgroundColor: '#f5f5f5',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      cursor: 'pointer'
                    }}
                    onClick={() => handleProductClick(product.id)}
                  >
                    {product.images?.[0] ? (
                      <img
                        src={product.images[0]}
                        alt={product.name}
                        style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}
                      />
                    ) : (
                      <Typography variant="body2" color="text.secondary">
                        No Image
                      </Typography>
                    )}
                  </CardMedia>
                  <CardContent sx={{ flexGrow: 1 }}>
                    <Typography
                      variant="h6"
                      sx={{
                        cursor: 'pointer',
                        '&:hover': { color: 'primary.main' }
                      }}
                      onClick={() => handleProductClick(product.id)}
                    >
                      {product.name}
                    </Typography>
                    
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                      {product.brand}
                    </Typography>

                    <Typography variant="h6" color="primary" sx={{ mb: 2 }}>
                      ${product.price}
                    </Typography>

                    <Button
                      variant="contained"
                      fullWidth
                      onClick={() => handleAddToCart(product.id)}
                      disabled={!isAuthenticated}
                    >
                      Add to Cart
                    </Button>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>

        {/* Newsletter */}
        <Box sx={{ bgcolor: 'grey.100', p: 4, borderRadius: 2, textAlign: 'center', mb: 8 }}>
          <Typography variant="h4" gutterBottom>
            Stay Updated
          </Typography>
          <Typography variant="body1" paragraph>
            Subscribe to our newsletter for exclusive offers and new product updates
          </Typography>
          <Button variant="contained" size="large">
            Subscribe Now
          </Button>
        </Box>
      </Container>
    </Box>
  );
};

export default Home;
