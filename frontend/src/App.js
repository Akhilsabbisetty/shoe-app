import React from 'react';
import ProductCard from './components/ProductCard';

const products = [
  { id: 1, name: 'Nike Air Zoom', price: 120, image: 'https://images.unsplash.com/photo-1606813902789-0f33d9a3d2d2?w=400' },
  { id: 2, name: 'Adidas Ultraboost', price: 150, image: 'https://images.unsplash.com/photo-1618354691373-4b52e7f8b7aa?w=400' },
  { id: 3, name: 'Puma Running', price: 100, image: 'https://images.unsplash.com/photo-1589187155473-1e31d99d7c15?w=400' }
];

function App() {
  return (
    <div style={{ fontFamily: 'Arial', padding: '20px', textAlign: 'center' }}>
      <h1>👟 Welcome to the Shoe Store</h1>
      <div style={{ display: 'flex', justifyContent: 'center', gap: '20px', flexWrap: 'wrap' }}>
        {products.map(p => <ProductCard key={p.id} product={p} />)}
      </div>
    </div>
  );
}

export default App;
