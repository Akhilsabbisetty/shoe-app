import React from 'react';

const ProductCard = ({ product }) => {
  return (
    <div style={{
      border: '1px solid #ddd',
      borderRadius: '8px',
      width: '200px',
      padding: '10px',
      textAlign: 'center',
      boxShadow: '0 2px 5px rgba(0,0,0,0.1)'
    }}>
      <img src={product.image} alt={product.name} style={{ width: '100%', borderRadius: '8px' }} />
      <h3>{product.name}</h3>
      <p>${product.price}</p>
    </div>
  );
};

export default ProductCard;
