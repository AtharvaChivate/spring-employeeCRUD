import React, { useState, useEffect } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";
import LogoutButton from "../pages/LogoutButton";
import AuthCheck from "../utils/AuthCheck";

const Employee = () => {
  AuthCheck("EMPLOYEE");
  const { id } = useParams();
  const navigate = useNavigate();
  const [employee, setEmployee] = useState({
    firstName: "",
    lastName: "",
    email: "",
    salary: "",
    department: "",
    joiningDate: "",
  });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEmployee = async () => {
      // If ID is undefined, redirect to the home page
      if (!id || id === "undefined") {
        setMessage("Employee ID is not provided");
        setLoading(false);
        return;
      }

      try {
        const response = await axios.get(
          `http://localhost:8080/api/employees/${id}`,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }
        );
        setEmployee(response.data);
        setLoading(false);
      } catch (error) {
        console.error("Error fetching employee:", error);
        if (error.response && error.response.status === 403) {
          setMessage(
            "You do not have permission to view this employee profile"
          );
        } else {
          setMessage("Error fetching employee data");
        }
        setLoading(false);
      }
    };
    fetchEmployee();
  }, [id, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setEmployee({ ...employee, [name]: value });
    // Clear validation error when field is edited
    if (errors[name]) {
      setErrors({ ...errors, [name]: null });
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!employee.firstName) newErrors.firstName = "First name is required";
    if (!employee.lastName) newErrors.lastName = "Last name is required";

    if (!employee.department) newErrors.department = "Department is required";

    if (employee.salary && parseFloat(employee.salary) < 0) {
      newErrors.salary = "Salary cannot be negative";
    }

    // Check if joining date is in the future
    if (employee.joiningDate) {
      const joiningDate = new Date(employee.joiningDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0); // Reset time part for date comparison

      if (joiningDate > today) {
        newErrors.joiningDate = "Joining date cannot be in the future";
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    setErrors({});
    setMessage("");

    // If ID is undefined, show an error message
    if (!id || id === "undefined") {
      setMessage("Employee ID is not provided");
      return;
    }

    if (!validateForm()) return;

    try {
      const updateData = {
        ...employee,
        salary: employee.salary ? parseFloat(employee.salary) : 0,
      };

      await axios.put(`http://localhost:8080/api/employees/${id}`, updateData, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "Content-Type": "application/json",
        },
      });

      setMessage("Profile updated successfully");
    } catch (error) {
      console.error("Update error:", error);

      if (error.response) {
        const responseData = error.response.data;
        console.log("Server error response:", responseData);

        // Handle validation errors from server
        if (typeof responseData === "object" && !Array.isArray(responseData)) {
          setErrors(responseData);
          setMessage("Please correct the errors in the form");
        } else {
          setMessage(
            "Error: " + (error.response.data.message || error.message)
          );
        }
      } else {
        setMessage("Error updating profile");
      }
    }
  };

  if (loading) {
    return <div>Loading employee data...</div>;
  }

  return (
    <div className="employee-profile">
      <h2>Employee Profile</h2>
      {message && (
        <p
          className={
            message.includes("success") ? "success-message" : "error-message"
          }
        >
          {message}
        </p>
      )}

      {!message.includes("not provided") && !message.includes("permission") && (
        <form onSubmit={handleUpdate}>
          <div className="form-group">
            <label>First Name:</label>
            <input
              type="text"
              name="firstName"
              placeholder="First Name"
              value={employee.firstName || ""}
              onChange={handleChange}
            />
            {errors.firstName && <p className="error">{errors.firstName}</p>}
          </div>

          <div className="form-group">
            <label>Last Name:</label>
            <input
              type="text"
              name="lastName"
              placeholder="Last Name"
              value={employee.lastName || ""}
              onChange={handleChange}
            />
            {errors.lastName && <p className="error">{errors.lastName}</p>}
          </div>

          <div className="form-group">
            <label>Email:</label>
            <input
              type="email"
              name="email"
              placeholder="Email"
              value={employee.email || ""}
              readOnly // Email can't be changed as it's the username
            />
            {errors.email && <p className="error">{errors.email}</p>}
          </div>

          <div className="form-group">
            <label>Salary:</label>
            <input
              type="number"
              name="salary"
              placeholder="Salary"
              value={employee.salary || ""}
              onChange={handleChange}
            />
            {errors.salary && <p className="error">{errors.salary}</p>}
          </div>

          <div className="form-group">
            <label>Department:</label>
            <input
              type="text"
              name="department"
              placeholder="Department"
              value={employee.department || ""}
              onChange={handleChange}
            />
            {errors.department && <p className="error">{errors.department}</p>}
          </div>

          <div className="form-group">
            <label>Joining Date:</label>
            <input
              type="date"
              name="joiningDate"
              placeholder="Joining Date"
              value={employee.joiningDate || ""}
              onChange={handleChange}
              max={new Date().toISOString().split("T")[0]} // Prevent future dates
            />
            {errors.joiningDate && (
              <p className="error">{errors.joiningDate}</p>
            )}
          </div>

          <div className="form-group">
            <button type="submit">Update Profile</button>
          </div>
        </form>
      )}

      <div className="employee-profile">
        <div className="header-controls">
          <h2>Employee Profile</h2>
          <LogoutButton />
        </div>
      </div>
    </div>
  );
};

export default Employee;
