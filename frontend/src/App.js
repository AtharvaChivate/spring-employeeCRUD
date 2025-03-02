import React from 'react';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Home from './pages/Home';
import Login from './pages/Login';
import Admin from './pages/Admin';
import Employee from './pages/Employee';
import './styles/App.css'; // Import the CSS

const App = () => {
  return (
    <Router>
      <div>
        <h1>Employee Management System</h1>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login/:role" element={<Login />} />
          <Route path="/admin" element={<Admin />} />
          <Route path="/employee/:id" element={<Employee />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>
    </Router>
  );
};

export default App;