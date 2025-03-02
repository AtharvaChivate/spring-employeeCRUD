import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();
  const { role } = useParams();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      console.log("Attempting login with:", { username, password });
      const response = await axios.post('http://localhost:8080/api/auth/login', { username, password });
      console.log("Login response:", response.data);
      
      const { token } = response.data;
      localStorage.setItem('token', token);
      
      // Use the role from URL params
      const userRole = role === 'admin' ? 'ADMIN' : 'EMPLOYEE';
      localStorage.setItem('role', userRole);
      
      console.log("User role:", userRole);

      // If user is ADMIN, redirect to admin dashboard
      if (userRole === 'ADMIN') {
        console.log("Redirecting to admin dashboard");
        navigate('/admin');
      } else {
        // If user is EMPLOYEE, redirect to employee page with their ID
        console.log("Redirecting to employee page");
        const employeeId = response.data.id || '';
        navigate(`/employee/${employeeId}`);
      }
    } catch (error) {
      console.error("Login error:", error);
      alert('Invalid credentials!');
    }
  };

  return (
    <div>
      <h2>{role === 'admin' ? 'Admin Login' : 'Employee Login'}</h2>
      <form onSubmit={handleLogin}>
        <div>
          <label>Username:</label>
          <input 
            type="text" 
            placeholder="Username" 
            value={username}
            onChange={(e) => setUsername(e.target.value)} 
          />
        </div>
        <div>
          <label>Password:</label>
          <input 
            type="password" 
            placeholder="Password" 
            value={password}
            onChange={(e) => setPassword(e.target.value)} 
          />
        </div>
        <button type="submit">Login</button>
      </form>
    </div>
  );
};

export default Login;