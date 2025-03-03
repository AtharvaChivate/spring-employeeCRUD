import React, { useState } from "react";
import axios from "axios";
import LogoutButton from "../pages/LogoutButton";
import AuthCheck from '../utils/AuthCheck';

const Admin = () => {
  AuthCheck('ADMIN');
  const [action, setAction] = useState("");
  const [employee, setEmployee] = useState({
    firstName: "",
    lastName: "",
    email: "",
    salary: "",
    department: "",
    joiningDate: "",
  });
  const [employeeId, setEmployeeId] = useState("");
  const [employees, setEmployees] = useState([]);
  const [message, setMessage] = useState("");
  const [errors, setErrors] = useState({});

  const resetForm = () => {
    setEmployee({
      firstName: "",
      lastName: "",
      email: "",
      salary: "",
      department: "",
      joiningDate: "",
    });
    setEmployeeId("");
    setErrors({});
  };

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

    if (!employee.email) {
      newErrors.email = "Email is required";
    } else if (!/\S+@\S+\.\S+/.test(employee.email)) {
      newErrors.email = "Email is invalid";
    }

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

  const handleAction = async (e) => {
    e.preventDefault();
    setErrors({});
    setMessage("");

    try {
      let response;

      switch (action) {
        case "add":
          if (!validateForm()) return;

          const employeeData = {
            ...employee,
            salary: employee.salary ? parseFloat(employee.salary) : 0,
            joiningDate:
              employee.joiningDate || new Date().toISOString().split("T")[0],
          };

          console.log("Sending employee data:", employeeData);

          response = await axios.post(
            "http://localhost:8080/api/employees",
            employeeData,
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
                "Content-Type": "application/json",
              },
            }
          );

          setMessage("Employee added successfully");
          resetForm();
          break;

        case "update":
          if (!employeeId) {
            setMessage("Employee ID is required");
            return;
          }

          if (!validateForm()) return;

          const updateData = {
            ...employee,
            salary: employee.salary ? parseFloat(employee.salary) : 0,
          };

          response = await axios.put(
            `http://localhost:8080/api/employees/${employeeId}`,
            updateData,
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
                "Content-Type": "application/json",
              },
            }
          );

          setMessage("Employee updated successfully");
          resetForm();
          break;

        case "delete":
          if (!employeeId) {
            setMessage("Employee ID is required");
            return;
          }

          response = await axios.delete(
            `http://localhost:8080/api/employees/${employeeId}`,
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
              },
            }
          );

          setMessage("Employee deleted successfully");
          resetForm();
          break;

        case "get":
          if (!employeeId) {
            setMessage("Employee ID is required");
            return;
          }

          response = await axios.get(
            `http://localhost:8080/api/employees/${employeeId}`,
            {
              headers: {
                Authorization: `Bearer ${localStorage.getItem("token")}`,
              },
            }
          );

          setEmployee(response.data);
          break;

        case "getAll":
          response = await axios.get("http://localhost:8080/api/employees", {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          });

          setEmployees(response.data);
          break;

        default:
          break;
      }
    } catch (error) {
      console.error("Action error:", error);

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
        setMessage("Error: " + error.message);
      }
    }
  };

  return (
    <div className="admin-dashboard">
      <h2>Admin Dashboard</h2>
      <div className="action-buttons">
        <button
          onClick={() => {
            resetForm();
            setAction("add");
          }}
        >
          Add a new employee
        </button>
        <button
          onClick={() => {
            resetForm();
            setAction("update");
          }}
        >
          Update an employee
        </button>
        <button
          onClick={() => {
            resetForm();
            setAction("delete");
          }}
        >
          Delete an employee
        </button>
        <button
          onClick={() => {
            resetForm();
            setAction("get");
          }}
        >
          Get a particular employee by ID
        </button>
        <button
          onClick={() => {
            resetForm();
            setAction("getAll");
          }}
        >
          Get all employees
        </button>
      </div>
      {message && (
        <p
          className={
            message.includes("success") ? "success-message" : "error-message"
          }
        >
          {message}
        </p>
      )}
      {action && (
        <div className="form-container">
          <h3>
            {action === "add"
              ? "Add Employee"
              : action === "update"
              ? "Update Employee"
              : action === "delete"
              ? "Delete Employee"
              : action === "get"
              ? "Get Employee"
              : "All Employees"}
          </h3>

          <form onSubmit={handleAction}>
            {action !== "getAll" && action !== "add" && (
              <div className="form-group">
                <label>Employee ID:</label>
                <input
                  type="text"
                  placeholder="Employee ID"
                  value={employeeId}
                  onChange={(e) => setEmployeeId(e.target.value)}
                />
                {errors.id && <p className="error">{errors.id}</p>}
              </div>
            )}

            {(action === "add" || action === "update") && (
              <>
                <div className="form-group">
                  <label>First Name: *</label>
                  <input
                    type="text"
                    name="firstName"
                    placeholder="First Name"
                    value={employee.firstName}
                    onChange={handleChange}
                  />
                  {errors.firstName && (
                    <p className="error">{errors.firstName}</p>
                  )}
                </div>

                <div className="form-group">
                  <label>Last Name: *</label>
                  <input
                    type="text"
                    name="lastName"
                    placeholder="Last Name"
                    value={employee.lastName}
                    onChange={handleChange}
                  />
                  {errors.lastName && (
                    <p className="error">{errors.lastName}</p>
                  )}
                </div>

                <div className="form-group">
                  <label>Email: * (Will be used as username)</label>
                  <input
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={employee.email}
                    onChange={handleChange}
                  />
                  {errors.email && <p className="error">{errors.email}</p>}
                </div>

                <div className="form-group">
                  <label>Salary:</label>
                  <input
                    type="number"
                    name="salary"
                    placeholder="Salary"
                    value={employee.salary}
                    onChange={handleChange}
                  />
                  {errors.salary && <p className="error">{errors.salary}</p>}
                </div>

                <div className="form-group">
                  <label>Department: *</label>
                  <input
                    type="text"
                    name="department"
                    placeholder="Department"
                    value={employee.department}
                    onChange={handleChange}
                  />
                  {errors.department && (
                    <p className="error">{errors.department}</p>
                  )}
                </div>

                <div className="form-group">
                  <label>Joining Date:</label>
                  <input
                    type="date"
                    name="joiningDate"
                    placeholder="Joining Date"
                    value={employee.joiningDate}
                    onChange={handleChange}
                    max={new Date().toISOString().split("T")[0]} // Prevent future dates
                  />
                  {errors.joiningDate && (
                    <p className="error">{errors.joiningDate}</p>
                  )}
                </div>
              </>
            )}

            <div className="form-group">
              <button type="submit" className="submit-button">
                {action === "getAll"
                  ? "Get All"
                  : action.charAt(0).toUpperCase() + action.slice(1)}
              </button>
            </div>
          </form>
        </div>
      )}
      {action === "getAll" && employees.length > 0 && (
        <div className="employee-list">
          <h3>All Employees</h3>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Salary</th>
                <th>Joining Date</th>
              </tr>
            </thead>
            <tbody>
              {employees.map((emp) => (
                <tr key={emp.id}>
                  <td>{emp.id}</td>
                  <td>
                    {emp.firstName} {emp.lastName}
                  </td>
                  <td>{emp.email}</td>
                  <td>{emp.department}</td>
                  <td>{emp.salary}</td>
                  <td>{emp.joiningDate}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {action === "get" && employee && employee.firstName && (
        <div className="employee-details">
          <h3>Employee Details</h3>
          <table>
            <tbody>
              <tr>
                <th>ID:</th>
                <td>{employee.id}</td>
              </tr>
              <tr>
                <th>First Name:</th>
                <td>{employee.firstName}</td>
              </tr>
              <tr>
                <th>Last Name:</th>
                <td>{employee.lastName}</td>
              </tr>
              <tr>
                <th>Email:</th>
                <td>{employee.email}</td>
              </tr>
              <tr>
                <th>Salary:</th>
                <td>{employee.salary}</td>
              </tr>
              <tr>
                <th>Department:</th>
                <td>{employee.department}</td>
              </tr>
              <tr>
                <th>Joining Date:</th>
                <td>{employee.joiningDate}</td>
              </tr>
            </tbody>
          </table>
        </div>
      )}

      <div className="admin-dashboard">
        <div className="header-controls">
          <h2>Admin Dashboard</h2>
          <LogoutButton />
        </div>
      </div>
    </div>
  );
};

export default Admin;
