package com.company.db;

/**
 *
 * @author Kipyegon M
 */
// File: EmployeeDAO.java

import java.sql.*;
import java.util.*;

public class EmployeeDAO {
    
    public int createEmployee(Employee employee) throws SQLException {
        String sql = "INSERT INTO employees (user_id, department_id, employee_code, " +
                    "hire_date, salary, job_title, phone, address) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, employee.getUserId());
            pstmt.setObject(2, employee.getDepartmentId(), Types.INTEGER);
            pstmt.setString(3, employee.getEmployeeCode());
            //pstmt.setDate(4, Date.valueOf(employee.getHireDate()));
            pstmt.setBigDecimal(5, employee.getSalary());
            pstmt.setString(6, employee.getJobTitle());
            pstmt.setString(7, employee.getPhone());
            pstmt.setString(8, employee.getAddress());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            
            return -1;
        }
    }
    
    public Employee getEmployeeById(int id) throws SQLException {
        String sql = "SELECT e.*, u.username, u.email, u.first_name, u.last_name, u.role, " +
                    "d.name as department_name " +
                    "FROM employees e " +
                    "LEFT JOIN users u ON e.user_id = u.id " +
                    "LEFT JOIN departments d ON e.department_id = d.id " +
                    "WHERE e.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }
        
        return null;
    }
    
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT e.*, u.username, u.email, u.first_name, u.last_name, u.role, " +
                    "d.name as department_name " +
                    "FROM employees e " +
                    "LEFT JOIN users u ON e.user_id = u.id " +
                    "LEFT JOIN departments d ON e.department_id = d.id " +
                    "ORDER BY e.hire_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                employees.add(mapResultSetToEmployee(rs));
            }
        }
        
        return employees;
    }
    
    public boolean updateEmployee(Employee employee) throws SQLException {
        String sql = "UPDATE employees SET department_id = ?, employee_code = ?, " +
                    "hire_date = ?, salary = ?, job_title = ?, phone = ?, address = ? " +
                    "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, employee.getDepartmentId(), Types.INTEGER);
            pstmt.setString(2, employee.getEmployeeCode());
           // pstmt.setDate(3, Date.valueOf(employee.getHireDate()));
            pstmt.setBigDecimal(4, employee.getSalary());
            pstmt.setString(5, employee.getJobTitle());
            pstmt.setString(6, employee.getPhone());
            pstmt.setString(7, employee.getAddress());
            pstmt.setInt(8, employee.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean deleteEmployee(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public List<Employee> getEmployeesByDepartment(int departmentId) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT e.*, u.username, u.email, u.first_name, u.last_name, u.role, " +
                    "d.name as department_name " +
                    "FROM employees e " +
                    "LEFT JOIN users u ON e.user_id = u.id " +
                    "LEFT JOIN departments d ON e.department_id = d.id " +
                    "WHERE e.department_id = ? " +
                    "ORDER BY e.job_title";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, departmentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapResultSetToEmployee(rs));
                }
            }
        }
        
        return employees;
    }
    
    public List<Employee> searchEmployees(String keyword) throws SQLException {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT e.*, u.username, u.email, u.first_name, u.last_name, u.role, " +
                    "d.name as department_name " +
                    "FROM employees e " +
                    "LEFT JOIN users u ON e.user_id = u.id " +
                    "LEFT JOIN departments d ON e.department_id = d.id " +
                    "WHERE u.first_name LIKE ? OR u.last_name LIKE ? " +
                    "OR e.job_title LIKE ? OR e.employee_code LIKE ? " +
                    "OR d.name LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String likeKeyword = "%" + keyword + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, likeKeyword);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapResultSetToEmployee(rs));
                }
            }
        }
        
        return employees;
    }
    
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getInt("id"));
        employee.setUserId(rs.getInt("user_id"));
        employee.setDepartmentId(rs.getInt("department_id"));
        if (rs.wasNull()) {
            employee.setDepartmentId(null);
        }
        employee.setEmployeeCode(rs.getString("employee_code"));
        employee.setHireDate(rs.getDate("hire_date").toLocalDate());
        employee.setSalary(rs.getBigDecimal("salary"));
        employee.setJobTitle(rs.getString("job_title"));
        employee.setPhone(rs.getString("phone"));
        employee.setAddress(rs.getString("address"));
        
        // User details
        employee.setUsername(rs.getString("username"));
        employee.setEmail(rs.getString("email"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        
        // Department name
        employee.setDepartmentName(rs.getString("department_name"));
        
        return employee;
    }
}