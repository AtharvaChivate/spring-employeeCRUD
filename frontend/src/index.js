import React from 'react';
import ReactDOM from 'react-dom/client';  // This is correct for React 18
import './App.css';
import App from './App';

// Create a root element and render the App component using createRoot
const root = ReactDOM.createRoot(document.getElementById('root'));  // Create the root using createRoot
root.render(   // Use root.render() instead of ReactDOM.render()
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
