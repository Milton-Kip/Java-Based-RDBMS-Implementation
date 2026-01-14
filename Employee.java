package com.company.db;

/**
 *
 * @author Kipyegon M
 */
// File: Employee.java

import java.math.BigDecimal;
import java.time.LocalDate;

public class Employee {
    private int id;
    private int userId;
    private Integer departmentId;
    private String employeeCode;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String jobTitle;
    private String phone;
    private String address;
    
    // Joined fields
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String departmentName;
    
    // Constructors
    public Employee() {}
    
    public Employee(int userId, String employeeCode, LocalDate hireDate, 
                   BigDecimal salary, String jobTitle) {
        this.userId = userId;
        this.employeeCode = employeeCode;
        this.hireDate = hireDate;
        this.salary = salary;
        this.jobTitle = jobTitle;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getFormattedSalary() {
        return salary != null ? "$" + salary.toPlainString() : "N/A";
    }
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", employeeCode='" + employeeCode + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", department='" + departmentName + '\'' +
                ", salary=" + getFormattedSalary() +
                '}';
    }
}
