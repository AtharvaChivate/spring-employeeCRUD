import React from 'react';
import { useNavigate } from 'react-router-dom';

const LogoutButton = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    // Clear all authentication data from localStorage
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');
    
    // Redirect to home page
    navigate('/');
  };

  return (
    <button 
      onClick={handleLogout} 
      className="logout-button"
    >
      Logout
    </button>
  );
};

export default LogoutButton;