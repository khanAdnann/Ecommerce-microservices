import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Typography,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Rating,
  CircularProgress,
  Pagination
} from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { searchProducts } from '../../redux/slices/productSlice';
import { addToCart } from '../../redux/slices/cartSlice';
import { useNavigate } from 'react-router-dom';

const Products = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { products, isLoading, totalPages } = useSelector((state) => state.products);
  const { isAuthenticated } = useSelector((state) => state.auth);

  const [filters, setFilters] = useState({
    query: '',
    categoryId: '',
    brand: '',
    minPrice: '',
    maxPrice: '',
    sortBy: 'name',
    sortDirection: 'asc',
    page: 0,
    size: 12
  });

  const [searchInput, setSearchInput] = useState('');

  useEffect(() => {
    dispatch(searchProducts(filters));
  }, [dispatch, filters]);

  const handleSearch = () => {
    setFilters({ ...filters, query: searchInput, page: 0 });
  };

  const handleFilterChange = (field, value) => {
    setFilters({ ...filters, [field]: value, page: 0 });
  };

  const handlePageChange = (event, value) => {
    setFilters({ ...filters, page: value - 1 });
  };

  const handleAddToCart = (productId) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    dispatch(addToCart({ productId, quantity: 1 }));
  };

  const handleProductClick = (productId) => {
    navigate(`/products/${productId}`);
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Products
      </Typography>

      {/* Search and Filters */}
      <Box sx={{ mb: 3, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
        <TextField
          label="Search products"
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          sx={{ minWidth: 200 }}
        />
        <Button variant="contained" onClick={handleSearch}>
          Search
        </Button>
        
        <FormControl sx={{ minWidth: 150 }}>
          <InputLabel>Category</InputLabel>
          <Select
            value={filters.categoryId}
            onChange={(e) => handleFilterChange('categoryId', e.target.value)}
          >
            <MenuItem value="">All Categories</MenuItem>
            <MenuItem value="1">Electronics</MenuItem>
            <MenuItem value="2">Clothing</MenuItem>
            <MenuItem value="3">Books</MenuItem>
            <MenuItem value="4">Home & Garden</MenuItem>
            <MenuItem value="5">Sports</MenuItem>
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 120 }}>
          <InputLabel>Brand</InputLabel>
          <Select
            value={filters.brand}
            onChange={(e) => handleFilterChange('brand', e.target.value)}
          >
            <MenuItem value="">All Brands</MenuItem>
            <MenuItem value="TechBrand">TechBrand</MenuItem>
            <MenuItem value="FashionCo">FashionCo</MenuItem>
            <MenuItem value="SportGear">SportGear</MenuItem>
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 120 }}>
          <InputLabel>Sort By</InputLabel>
          <Select
            value={filters.sortBy}
            onChange={(e) => handleFilterChange('sortBy', e.target.value)}
          >
            <MenuItem value="name">Name</MenuItem>
            <MenuItem value="price">Price</MenuItem>
            <MenuItem value="rating">Rating</MenuItem>
            <MenuItem value="createdAt">Latest</MenuItem>
          </Select>
        </FormControl>

        <FormControl sx={{ minWidth: 100 }}>
          <InputLabel>Order</InputLabel>
          <Select
            value={filters.sortDirection}
            onChange={(e) => handleFilterChange('sortDirection', e.target.value)}
          >
            <MenuItem value="asc">Asc</MenuItem>
            <MenuItem value="desc">Desc</MenuItem>
          </Select>
        </FormControl>
      </Box>

      {/* Products Grid */}
      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          <Grid container spacing={3}>
            {products?.map((product) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
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

                    {product.featured && (
                      <Chip label="Featured" color="primary" size="small" sx={{ mb: 1 }} />
                    )}

                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      {product.description?.substring(0, 100)}...
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

          {/* Pagination */}
          {totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
              <Pagination
                count={totalPages}
                page={filters.page + 1}
                onChange={handlePageChange}
                color="primary"
              />
            </Box>
          )}
        </>
      )}
    </Box>
  );
};

export default Products;
