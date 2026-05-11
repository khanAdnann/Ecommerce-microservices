import React, { useEffect, useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardMedia,
  Typography,
  Button,
  Chip,
  Rating,
  CircularProgress,
  Alert,
  Grid,
  Divider,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material';
import {
  Add as AddIcon,
  Remove as RemoveIcon,
  ShoppingCart,
  Favorite,
  Share
} from '@mui/icons-material';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchProductById } from '../../redux/slices/productSlice';
import { addToCart } from '../../redux/slices/cartSlice';

const ProductDetail = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { id } = useParams();
  
  const { currentProduct, isLoading, isError, message } = useSelector((state) => state.products);
  const { isAuthenticated } = useSelector((state) => state.auth);
  
  const [quantity, setQuantity] = useState(1);
  const [selectedImage, setSelectedImage] = useState(0);
  const [shareDialogOpen, setShareDialogOpen] = useState(false);

  useEffect(() => {
    if (id) {
      dispatch(fetchProductById(id));
    }
  }, [dispatch, id]);

  const handleAddToCart = () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    dispatch(addToCart({ productId: parseInt(id), quantity }))
      .unwrap()
      .then(() => {
        // Show success message or navigate to cart
      })
      .catch((error) => {
        console.error('Failed to add to cart:', error);
      });
  };

  const handleQuantityChange = (type) => {
    if (type === 'increase') {
      setQuantity(quantity + 1);
    } else if (type === 'decrease' && quantity > 1) {
      setQuantity(quantity - 1);
    }
  };

  const handleShare = () => {
    if (navigator.share) {
      navigator.share({
        title: currentProduct?.name,
        text: `Check out this amazing product: ${currentProduct?.name}`,
        url: window.location.href
      });
    } else {
      setShareDialogOpen(true);
    }
  };

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isError || !currentProduct) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          {message || 'Product not found'}
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={4}>
        {/* Product Images */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent sx={{ p: 2 }}>
              <Box
                sx={{
                  height: 400,
                  backgroundColor: '#f5f5f5',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2
                }}
              >
                {currentProduct.images?.[selectedImage] ? (
                  <img
                    src={currentProduct.images[selectedImage]}
                    alt={currentProduct.name}
                    style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}
                  />
                ) : (
                  <Typography variant="h6" color="text.secondary">
                    No Image Available
                  </Typography>
                )}
              </Box>
              
              {/* Thumbnail Images */}
              {currentProduct.images && currentProduct.images.length > 1 && (
                <Box sx={{ display: 'flex', gap: 1, justifyContent: 'center' }}>
                  {currentProduct.images.map((image, index) => (
                    <Box
                      key={index}
                      sx={{
                        width: 60,
                        height: 60,
                        backgroundColor: '#f5f5f5',
                        border: selectedImage === index ? '2px solid primary.main' : '1px solid #ddd',
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center'
                      }}
                      onClick={() => setSelectedImage(index)}
                    >
                      <img
                        src={image}
                        alt={`Thumbnail ${index + 1}`}
                        style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}
                      />
                    </Box>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Product Details */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent sx={{ p: 3 }}>
              {currentProduct.featured && (
                <Chip label="Featured" color="primary" size="small" sx={{ mb: 2 }} />
              )}
              
              <Typography variant="h4" gutterBottom>
                {currentProduct.name}
              </Typography>
              
              <Typography variant="h6" color="text.secondary" gutterBottom>
                {currentProduct.brand}
              </Typography>
              
              <Typography variant="body1" color="text.secondary" paragraph>
                SKU: {currentProduct.sku}
              </Typography>

              {/* Rating */}
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Rating
                  value={currentProduct.rating}
                  precision={0.5}
                  size="large"
                  readOnly
                />
                <Typography variant="body2" sx={{ ml: 1 }}>
                  ({currentProduct.reviewCount} reviews)
                </Typography>
              </Box>

              <Divider sx={{ my: 2 }} />

              {/* Price */}
              <Typography variant="h3" color="primary" gutterBottom>
                ${currentProduct.price}
              </Typography>

              {/* Description */}
              <Typography variant="body1" paragraph sx={{ mb: 3 }}>
                {currentProduct.description}
              </Typography>

              {/* Product Attributes */}
              {currentProduct.attributes && currentProduct.attributes.length > 0 && (
                <Box sx={{ mb: 3 }}>
                  <Typography variant="h6" gutterBottom>
                    Product Details
                  </Typography>
                  {currentProduct.attributes.map((attr, index) => (
                    <Box key={index} sx={{ mb: 1 }}>
                      <Typography variant="body2" color="text.secondary">
                        {attr.attributeName}: {attr.attributeValue}
                      </Typography>
                    </Box>
                  ))}
                </Box>
              )}

              {/* Tags */}
              {currentProduct.tags && currentProduct.tags.length > 0 && (
                <Box sx={{ mb: 3 }}>
                  {currentProduct.tags.map((tag, index) => (
                    <Chip key={index} label={tag} size="small" sx={{ mr: 1, mb: 1 }} />
                  ))}
                </Box>
              )}

              {/* Quantity Selector */}
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <Typography variant="body1" sx={{ mr: 2 }}>
                  Quantity:
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <IconButton
                    onClick={() => handleQuantityChange('decrease')}
                    disabled={quantity <= 1}
                  >
                    <RemoveIcon />
                  </IconButton>
                  <Typography sx={{ mx: 2, minWidth: 40, textAlign: 'center' }}>
                    {quantity}
                  </Typography>
                  <IconButton onClick={() => handleQuantityChange('increase')}>
                    <AddIcon />
                  </IconButton>
                </Box>
              </Box>

              {/* Action Buttons */}
              <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                <Button
                  variant="contained"
                  size="large"
                  startIcon={<ShoppingCart />}
                  onClick={handleAddToCart}
                  disabled={!isAuthenticated}
                  sx={{ flexGrow: 1 }}
                >
                  Add to Cart
                </Button>
                
                <IconButton
                  variant="outlined"
                  size="large"
                  onClick={handleShare}
                >
                  <Share />
                </IconButton>
                
                <IconButton
                  variant="outlined"
                  size="large"
                >
                  <Favorite />
                </IconButton>
              </Box>

              {!isAuthenticated && (
                <Typography variant="body2" color="text.secondary">
                  Please login to add items to cart
                </Typography>
              )}

              <Divider sx={{ my: 2 }} />

              {/* Additional Info */}
              <Box>
                <Typography variant="body2" color="text.secondary">
                  Category: {currentProduct.category?.name || 'N/A'}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Status: {currentProduct.status}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Availability: In Stock
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Share Dialog */}
      <Dialog open={shareDialogOpen} onClose={() => setShareDialogOpen(false)}>
        <DialogTitle>Share Product</DialogTitle>
        <DialogContent>
          <Typography>
            Share this product with your friends!
          </Typography>
          <Typography variant="body2" sx={{ mt: 1 }}>
            URL: {window.location.href}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShareDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ProductDetail;
