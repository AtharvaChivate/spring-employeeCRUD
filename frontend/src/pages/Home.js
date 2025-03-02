import React from 'react';
import { Link } from 'react-router-dom';

const Home = () => {
  return (
    <div>
      <h2>Welcome to Employee Management System</h2>
      <ul>
        <li><Link to="/login/admin">Admin Login</Link></li>
        <li><Link to="/login/employee">Employee Login</Link></li>
      </ul>
    </div>
  );
};

export default Home;