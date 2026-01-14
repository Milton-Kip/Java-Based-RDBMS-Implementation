# Employee Management System

![img](https://github.com/user-attachments/assets/8b63f6ce-7183-473c-8987-f82e9d861186)

A full-featured Java web application with MySQL database backend for managing employee records with complete CRUD operations.

## Features

- **Complete CRUD Operations**: Create, Read, Update, and Delete employee records
- **Modern Web Interface**: Responsive, mobile-friendly UI with real-time search
- **Database Management**: Professional MySQL schema with proper indexing and constraints
- **Connection Pooling**: Efficient database connection management
- **Audit Logging**: Track all changes with database triggers
- **JSON API**: Programmatic access to employee data
- **User Management**: Role-based access control system

## Technology Stack

- **Backend**: Java 8+
- **Database**: MySQL 5.7+
- **Web Server**: Built-in Java HTTP server
- **Frontend**: HTML5, CSS3, JavaScript
- **Database Driver**: MySQL Connector/J

## Database Schema

The system uses a normalized database design with the following tables:

- **users**: System user accounts with authentication
- **employees**: Employee details and work information
- **departments**: Organizational department structure
- **projects**: Project management
- **project_assignments**: Employee-project relationships (many-to-many)
- **tasks**: Individual project tasks
- **audit_logs**: Change tracking and audit history

## Installation

### Prerequisites

1. **Java 8 or higher**
   ```bash
   java -version
   ```

2. **MySQL 5.7 or higher**
   ```bash
   mysql --version
   ```

3. **MySQL Connector/J** (included in setup)

### Step 1: Clone and Setup

```bash
# Clone the repository
git clone <repository-url>
cd employee-management-system

# Set execute permissions
chmod +x setup.sh
chmod +x run.sh
```

### Step 2: Database Setup

```bash
# Create database and tables
mysql -u root -p < database_schema.sql
```

Enter your MySQL root password when prompted.

### Step 3: Configure Database Connection

Edit `com/company/db/DatabaseConnection.java`:

```java
private static final String DB_USER = "your_username";
private static final String DB_PASSWORD = "your_password";
```

### Step 4: Compile

```bash
# Compile all Java files
javac com/company/db/*.java
javac com/company/model/*.java
javac com/company/dao/*.java
javac com/company/web/*.java
```

Or use the setup script:

```bash
./setup.sh
```

### Step 5: Run the Application

```bash
./run.sh
```

The application will start on port 8080.

## Usage

### Access the Web Interface

Open your browser and navigate to:
```
http://localhost:8080
```

### Main Features

#### 1. Dashboard
- View system statistics
- Quick access to all features
- Visual overview of employee count

#### 2. Employee Management
- **View All Employees**: Browse complete employee directory
- **Add Employee**: Register new employees with full details
- **Edit Employee**: Update employee information
- **Delete Employee**: Remove employee records
- **Search**: Real-time search by name, department, or job title

#### 3. User Management
- View system users
- Monitor user roles and status
- Track user activity

#### 4. API Access
- JSON endpoints for programmatic access:
  - `GET /api/employees` - List all employees in JSON format
  - `GET /api/employees/search?q=query` - Search employees

### Sample Operations

#### Create a New Employee
1. Navigate to "Add New Employee"
2. Fill in the form with employee details
3. Click "Save Employee"
4. The system automatically creates both user and employee records

#### Update Employee Information
1. Go to "Employee Directory"
2. Click the edit icon next to an employee
3. Modify the information
4. Click "Update Employee"

#### Search Employees
1. Use the search box in the employee directory
2. Type any search term (name, department, job title)
3. Results filter in real-time

## Project Structure

```
employee-management-system/
├── com/company/
│   ├── db/                    # Database connection and utilities
│   │   ├── DatabaseConnection.java
│   │   └── ...
│   ├── dao/                   # Data Access Objects
│   │   ├── UserDAO.java
│   │   ├── EmployeeDAO.java
│   │   └── ...
│   ├── model/                 # Data models
│   │   ├── User.java
│   │   ├── Employee.java
│   │   └── ...
│   └── web/                   # Web application
│       └── EmployeeManagementApp.java
├── database_schema.sql        # Database creation script
├── setup.sh                   # Setup and compilation script
├── run.sh                     # Application startup script
└── README.md                  # This file
```

## API Documentation

### GET /api/employees
Returns a JSON array of all employees.

**Response:**
```json
[
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "employeeCode": "JD001",
    "jobTitle": "Software Engineer",
    "department": "Engineering",
    "email": "john.doe@company.com"
  }
]
```

### GET /api/employees/search?q={query}
Searches employees by name, job title, or department.

**Response:**
```json
{
  "query": "John",
  "results": [
    {
      "id": 1,
      "name": "John Doe",
      "employeeCode": "JD001",
      "jobTitle": "Software Engineer"
    }
  ]
}
```

## Database Schema Details

### Users Table
```sql
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role ENUM('admin', 'manager', 'user') DEFAULT 'user',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Employees Table
```sql
CREATE TABLE employees (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    department_id INT,
    employee_code VARCHAR(20) UNIQUE NOT NULL,
    hire_date DATE NOT NULL,
    salary DECUMAL(10, 2) CHECK (salary >= 0),
    job_title VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);
```

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Verify MySQL is running: `sudo systemctl status mysql`
   - Check credentials in `DatabaseConnection.java`
   - Test connection: `mysql -u username -p`

2. **Compilation Errors**
   - Ensure Java 8+: `java -version`
   - Check for missing semicolons or imports
   - Verify all .java files are in correct directories

3. **Port Already in Use**
   - Change port in `EmployeeManagementApp.java`: `private static final int PORT = 8080;`
   - Check for other services on port 8080: `netstat -tulpn | grep :8080`

4. **Database Not Found**
   - Run the SQL script: `mysql -u root -p < database_schema.sql`
   - Verify database exists: `SHOW DATABASES;`

### Logs and Debugging

- Application logs appear in console
- Database errors are printed to stderr
- Use browser developer tools for frontend debugging

## Security Notes

- This is a demonstration system
- Passwords are stored with placeholder hashing (replace with proper hashing for production)
- SQL injection prevented via prepared statements
- No HTTPS in this demo version (add for production)

## Performance

- Connection pooling for efficient database access
- Database indexes for faster queries
- Client-side search reduces server load
- Efficient memory usage with streaming responses

## Extending the System

### Add New Features

1. **Add New Tables**
   - Create table in `database_schema.sql`
   - Create corresponding Model and DAO classes
   - Add web interface components

2. **Add Authentication**
   - Implement login/logout functionality
   - Add session management
   - Secure endpoints with role-based access

3. **Add Reporting**
   - Create analytics dashboard
   - Add export functionality (CSV, PDF)
   - Implement charts and graphs

### Production Deployment

For production deployment:

1. **Security**
   - Implement proper password hashing (BCrypt)
   - Add HTTPS/SSL
   - Implement CSRF protection
   - Add input validation and sanitization

2. **Performance**
   - Use connection pool like HikariCP
   - Implement caching (Redis, Memcached)
   - Add database replication

3. **Monitoring**
   - Add logging framework (Log4j, SLF4J)
   - Implement health checks
   - Set up monitoring and alerts

## License

This project is for educational purposes. Feel free to modify and use as needed.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review the code comments
3. Create an issue in the repository

---

**Happy Coding!** 🚀

Built with ❤️ using Java and MySQL
