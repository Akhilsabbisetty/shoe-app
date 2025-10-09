import React, { useEffect, useState } from 'react';

function App() {
  const [backendMessage, setBackendMessage] = useState('Loading...');

  useEffect(() => {
    fetch('/api/hello')
      .then((res) => res.text())
      .then(setBackendMessage)
      .catch(() => setBackendMessage('Backend not reachable'));
  }, []);

  return (
    <div style={{ fontFamily: 'Arial', padding: '2rem', textAlign: 'center' }}>
      <h1>👟 Shoe App Frontend</h1>
      <p>Message from backend:</p>
      <pre>{backendMessage}</pre>
    </div>
  );
}

export default App;
