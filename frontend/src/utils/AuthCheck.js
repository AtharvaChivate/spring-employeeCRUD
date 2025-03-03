// Updated utils/AuthCheck.js with token expiration check
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const parseJwt = (token) => {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch (e) {
    return null;
  }
};

const AuthCheck = (allowedRole) => {
  const navigate = useNavigate();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const verifyAuth = async () => {
      const token = localStorage.getItem("token");
      const role = localStorage.getItem("role");

      if (!token) {
        navigate("/");
        return;
      }

      // Check if token is expired
      const decodedToken = parseJwt(token);
      if (decodedToken && decodedToken.exp) {
        // Check if token is expired
        const currentTime = Date.now() / 1000;
        if (decodedToken.exp < currentTime) {
          // Token expired, clear storage and redirect to home
          localStorage.removeItem("token");
          localStorage.removeItem("role");
          localStorage.removeItem("userId");
          navigate("/", {
            state: {
              message: "Your session has expired. Please log in again.",
            },
          });
          return;
        }
      }

      if (allowedRole && role !== allowedRole) {
        navigate("/");
        return;
      }

      setIsChecking(false);
    };

    verifyAuth();
  }, [navigate, allowedRole]);

  return isChecking;
};

export default AuthCheck;
