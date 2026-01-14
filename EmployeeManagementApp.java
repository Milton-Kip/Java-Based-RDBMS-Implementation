package com.company.db;

/**
 *
 * @author Kipyegon M
 */
// File: EmployeeManagementApp.java

// File: EmployeeManagementApp.java

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EmployeeManagementApp {
    private static final int PORT = 8080;
    private static UserDAO userDAO = new UserDAO();
    private static EmployeeDAO employeeDAO = new EmployeeDAO();
    
    public static void main(String[] args) {
        try {
            // Test database connection
            DatabaseConnection.testConnection();
            
            System.out.println("Starting Employee Management System on port " + PORT);
            System.out.println("Open http://localhost:" + PORT + " in your browser");
            
            startWebServer();
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void startWebServer() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClientRequest(clientSocket)).start();
            }
        }
    }
    
    private static void handleClientRequest(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) return;
            
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String path = requestParts[1];
            
            // Read headers
            Map<String, String> headers = new HashMap<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                String[] headerParts = line.split(": ", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                }
            }
            
            // Read request body for POST
            StringBuilder requestBody = new StringBuilder();
            if (method.equals("POST") && headers.containsKey("Content-Length")) {
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                char[] body = new char[contentLength];
                in.read(body, 0, contentLength);
                requestBody.append(body);
            }
            
            // Parse query parameters
            Map<String, String> params = new HashMap<>();
            if (path.contains("?")) {
                String query = path.substring(path.indexOf("?") + 1);
                path = path.substring(0, path.indexOf("?"));
                parseQueryString(query, params);
            }
            
            // Parse POST body parameters
            if (requestBody.length() > 0) {
                parseQueryString(requestBody.toString(), params);
            }
            
            String response = routeRequest(method, path, params);
            
            // Send HTTP response
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html; charset=UTF-8");
            out.println("Content-Length: " + response.getBytes("UTF-8").length);
            out.println("Connection: close");
            out.println();
            out.println(response);
            
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static void parseQueryString(String queryString, Map<String, String> params) {
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                try {
                    params.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }
    
    private static String routeRequest(String method, String path, Map<String, String> params) {
        try {
            switch (path) {
                case "/":
                    return getHomePage();
                case "/employees":
                    return getEmployeesPage();
                case "/employees/add":
                    if (method.equals("POST")) {
                        return handleAddEmployee(params);
                    } else {
                        return getAddEmployeeForm();
                    }
                case "/employees/edit":
                    if (method.equals("POST")) {
                        return handleUpdateEmployee(params);
                    } else if (params.containsKey("id")) {
                        return getEditEmployeeForm(Integer.parseInt(params.get("id")));
                    }
                    break;
                case "/employees/delete":
                    if (params.containsKey("id")) {
                        return handleDeleteEmployee(Integer.parseInt(params.get("id")));
                    }
                    break;
                case "/employees/view":
                    if (params.containsKey("id")) {
                        return getEmployeeDetails(Integer.parseInt(params.get("id")));
                    }
                    break;
                case "/users":
                    return getUsersPage();
                case "/dashboard":
                    return getDashboard();
                case "/api/employees":
                    return getEmployeesJson();
                case "/api/employees/search":
                    if (params.containsKey("q")) {
                        return searchEmployeesJson(params.get("q"));
                    }
                    break;
            }
        } catch (Exception e) {
            return getErrorPage(e);
        }
        
        return getNotFoundPage();
    }
    
    private static String getHomePage() throws SQLException {
        int totalEmployees = employeeDAO.getAllEmployees().size();
        int totalUsers = userDAO.countUsers();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Employee Management System</title>\n");
        html.append("    <style>\n");
        html.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("        body { \n");
        html.append("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; \n");
        html.append("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
        html.append("            color: #333;\n");
        html.append("            min-height: 100vh;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            max-width: 1200px;\n");
        html.append("            margin: 0 auto;\n");
        html.append("            padding: 20px;\n");
        html.append("        }\n");
        html.append("        header {\n");
        html.append("            background: rgba(255, 255, 255, 0.95);\n");
        html.append("            backdrop-filter: blur(10px);\n");
        html.append("            padding: 20px;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);\n");
        html.append("            margin-bottom: 30px;\n");
        html.append("        }\n");
        html.append("        .header-content {\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: space-between;\n");
        html.append("            align-items: center;\n");
        html.append("        }\n");
        html.append("        h1 {\n");
        html.append("            color: #4a5568;\n");
        html.append("            margin-bottom: 5px;\n");
        html.append("        }\n");
        html.append("        .subtitle {\n");
        html.append("            color: #718096;\n");
        html.append("            font-size: 1.1rem;\n");
        html.append("        }\n");
        html.append("        nav a {\n");
        html.append("            margin-left: 20px;\n");
        html.append("            text-decoration: none;\n");
        html.append("            color: #4a5568;\n");
        html.append("            font-weight: 500;\n");
        html.append("            padding: 8px 16px;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            transition: all 0.3s ease;\n");
        html.append("        }\n");
        html.append("        nav a:hover {\n");
        html.append("            background: #667eea;\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        .stats-grid {\n");
        html.append("            display: grid;\n");
        html.append("            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n");
        html.append("            gap: 20px;\n");
        html.append("            margin-bottom: 30px;\n");
        html.append("        }\n");
        html.append("        .stat-card {\n");
        html.append("            background: rgba(255, 255, 255, 0.95);\n");
        html.append("            padding: 25px;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);\n");
        html.append("            transition: transform 0.3s ease, box-shadow 0.3s ease;\n");
        html.append("        }\n");
        html.append("        .stat-card:hover {\n");
        html.append("            transform: translateY(-5px);\n");
        html.append("            box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);\n");
        html.append("        }\n");
        html.append("        .stat-icon {\n");
        html.append("            font-size: 2.5rem;\n");
        html.append("            margin-bottom: 15px;\n");
        html.append("            color: #667eea;\n");
        html.append("        }\n");
        html.append("        .stat-value {\n");
        html.append("            font-size: 2rem;\n");
        html.append("            font-weight: bold;\n");
        html.append("            color: #2d3748;\n");
        html.append("            margin-bottom: 5px;\n");
        html.append("        }\n");
        html.append("        .stat-label {\n");
        html.append("            color: #718096;\n");
        html.append("            font-size: 0.9rem;\n");
        html.append("            text-transform: uppercase;\n");
        html.append("            letter-spacing: 1px;\n");
        html.append("        }\n");
        html.append("        .actions-grid {\n");
        html.append("            display: grid;\n");
        html.append("            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));\n");
        html.append("            gap: 20px;\n");
        html.append("        }\n");
        html.append("        .action-card {\n");
        html.append("            background: rgba(255, 255, 255, 0.95);\n");
        html.append("            padding: 30px;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            text-align: center;\n");
        html.append("            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);\n");
        html.append("            transition: all 0.3s ease;\n");
        html.append("            text-decoration: none;\n");
        html.append("            color: inherit;\n");
        html.append("            display: block;\n");
        html.append("        }\n");
        html.append("        .action-card:hover {\n");
        html.append("            background: white;\n");
        html.append("            transform: translateY(-5px);\n");
        html.append("            box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);\n");
        html.append("        }\n");
        html.append("        .action-icon {\n");
        html.append("            font-size: 3rem;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            color: #667eea;\n");
        html.append("        }\n");
        html.append("        .action-title {\n");
        html.append("            font-size: 1.3rem;\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: #2d3748;\n");
        html.append("            margin-bottom: 10px;\n");
        html.append("        }\n");
        html.append("        .action-desc {\n");
        html.append("            color: #718096;\n");
        html.append("            line-height: 1.6;\n");
        html.append("        }\n");
        html.append("        footer {\n");
        html.append("            margin-top: 50px;\n");
        html.append("            text-align: center;\n");
        html.append("            color: rgba(255, 255, 255, 0.8);\n");
        html.append("            padding: 20px;\n");
        html.append("            font-size: 0.9rem;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css\">\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <header>\n");
        html.append("            <div class=\"header-content\">\n");
        html.append("                <div>\n");
        html.append("                    <h1>🏢 Employee Management System</h1>\n");
        html.append("                    <p class=\"subtitle\">Manage your organization's workforce efficiently</p>\n");
        html.append("                </div>\n");
        html.append("                <nav>\n");
        html.append("                    <a href=\"/\">🏠 Home</a>\n");
        html.append("                    <a href=\"/employees\">👥 Employees</a>\n");
        html.append("                    <a href=\"/users\">👤 Users</a>\n");
        html.append("                    <a href=\"/dashboard\">📊 Dashboard</a>\n");
        html.append("                </nav>\n");
        html.append("            </div>\n");
        html.append("        </header>\n");
        html.append("        \n");
        html.append("        <div class=\"stats-grid\">\n");
        html.append("            <div class=\"stat-card\">\n");
        html.append("                <div class=\"stat-icon\">\n");
        html.append("                    <i class=\"fas fa-users\"></i>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"stat-value\">").append(totalEmployees).append("</div>\n");
        html.append("                <div class=\"stat-label\">Total Employees</div>\n");
        html.append("            </div>\n");
        html.append("            \n");
        html.append("            <div class=\"stat-card\">\n");
        html.append("                <div class=\"stat-icon\">\n");
        html.append("                    <i class=\"fas fa-user-circle\"></i>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"stat-value\">").append(totalUsers).append("</div>\n");
        html.append("                <div class=\"stat-label\">System Users</div>\n");
        html.append("            </div>\n");
        html.append("            \n");
        html.append("            <div class=\"stat-card\">\n");
        html.append("                <div class=\"stat-icon\">\n");
        html.append("                    <i class=\"fas fa-chart-line\"></i>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"stat-value\">98%</div>\n");
        html.append("                <div class=\"stat-label\">System Uptime</div>\n");
        html.append("            </div>\n");
        html.append("            \n");
        html.append("            <div class=\"stat-card\">\n");
        html.append("                <div class=\"stat-icon\">\n");
        html.append("                    <i class=\"fas fa-database\"></i>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"stat-value\">MySQL</div>\n");
        html.append("                <div class=\"stat-label\">Database</div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <div class=\"actions-grid\">\n");
        html.append("            <a href=\"/employees\" class=\"action-card\">\n");
        html.append("                <div class=\"action-icon\">\n");
        html.append("                    <i class=\"fas fa-user-plus\"></i>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"action-title\">Manage Employees</div>\n");
        html.append("                <div class=\"action-desc\">\n");
        html.append("                    View, add, edit, and delete employee records. Search and filter through your workforce.\n");
        html.append("                </div>\n");
        html.append("            </a>\n");
        html.append("            \n");
        html.append("            <a href=\"/employees/add\" class=\"action-card\">\n");
        html.append("                <div class=\"action-icon\">\n");
        html.append("                    <i class=\"fas fa-user-plus\"></i>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"action-title\">Add New Employee</div>\n");
        html.append("                <div class=\"action-desc\">\n");
        html.append("                    Register new employees into the system with all necessary details and documentation.\n");
        html.append("                </div>\n");
        html.append("            </a>\n");
        html.append("            \n");
        html.append("            <a href=\"/users\" class=\"action-card\">\n");
        html.append("                <div class=\"action-icon\">\n");
        html.append("                    <i class=\"fas fa-users-cog\"></i>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"action-title\">User Management</div>\n");
        html.append("                <div class=\"action-desc\">\n");
        html.append("                    Manage system users, assign roles, and control access permissions.\n");
        html.append("                </div>\n");
        html.append("            </a>\n");
        html.append("            \n");
        html.append("            <a href=\"/dashboard\" class=\"action-card\">\n");
        html.append("                <div class=\"action-icon\">\n");
        html.append("                    <i class=\"fas fa-chart-bar\"></i>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"action-title\">Analytics Dashboard</div>\n");
        html.append("                <div class=\"action-desc\">\n");
        html.append("                    View comprehensive analytics and reports about your organization's workforce.\n");
        html.append("                </div>\n");
        html.append("            </a>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <footer>\n");
        html.append("            <p>Employee Management System v1.0 | Built with Java & MySQL</p>\n");
        html.append("            <p>Developed by: Kipyegon Milton: kipyegonmilton@gmail.com</p>\n");
        html.append("            <p>© 2026 Company Inc. All rights reserved.</p>\n");
        html.append("        </footer>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private static String getEmployeesPage() throws SQLException {
        List<Employee> employees = employeeDAO.getAllEmployees();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Employee List</title>\n");
        html.append("    <style>\n");
        html.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("        body { \n");
        html.append("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; \n");
        html.append("            background: #f5f7fa;\n");
        html.append("            color: #333;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            max-width: 1200px;\n");
        html.append("            margin: 0 auto;\n");
        html.append("            padding: 20px;\n");
        html.append("        }\n");
        html.append("        header {\n");
        html.append("            background: white;\n");
        html.append("            padding: 25px;\n");
        html.append("            border-radius: 10px;\n");
        html.append("            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\n");
        html.append("            margin-bottom: 30px;\n");
        html.append("        }\n");
        html.append("        .header-content {\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: space-between;\n");
        html.append("            align-items: center;\n");
        html.append("            flex-wrap: wrap;\n");
        html.append("            gap: 20px;\n");
        html.append("        }\n");
        html.append("        h1 { color: #2d3748; margin-bottom: 10px; }\n");
        html.append("        .controls {\n");
        html.append("            display: flex;\n");
        html.append("            gap: 15px;\n");
        html.append("            align-items: center;\n");
        html.append("        }\n");
        html.append("        .search-box {\n");
        html.append("            padding: 10px 15px;\n");
        html.append("            border: 2px solid #e2e8f0;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            width: 300px;\n");
        html.append("            font-size: 14px;\n");
        html.append("            transition: border-color 0.3s;\n");
        html.append("        }\n");
        html.append("        .search-box:focus {\n");
        html.append("            outline: none;\n");
        html.append("            border-color: #667eea;\n");
        html.append("        }\n");
        html.append("        .btn {\n");
        html.append("            padding: 10px 20px;\n");
        html.append("            border: none;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            cursor: pointer;\n");
        html.append("            font-weight: 500;\n");
        html.append("            text-decoration: none;\n");
        html.append("            display: inline-flex;\n");
        html.append("            align-items: center;\n");
        html.append("            gap: 8px;\n");
        html.append("            transition: all 0.3s;\n");
        html.append("        }\n");
        html.append("        .btn-primary {\n");
        html.append("            background: #667eea;\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        .btn-primary:hover {\n");
        html.append("            background: #5a67d8;\n");
        html.append("            transform: translateY(-2px);\n");
        html.append("        }\n");
        html.append("        .btn-success {\n");
        html.append("            background: #48bb78;\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        .btn-success:hover {\n");
        html.append("            background: #38a169;\n");
        html.append("        }\n");
        html.append("        .btn-danger {\n");
        html.append("            background: #f56565;\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        .btn-info {\n");
        html.append("            background: #4299e1;\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        .table-container {\n");
        html.append("            background: white;\n");
        html.append("            border-radius: 10px;\n");
        html.append("            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);\n");
        html.append("            overflow: hidden;\n");
        html.append("        }\n");
        html.append("        table {\n");
        html.append("            width: 100%;\n");
        html.append("            border-collapse: collapse;\n");
        html.append("        }\n");
        html.append("        thead {\n");
        html.append("            background: #667eea;\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        th {\n");
        html.append("            padding: 15px;\n");
        html.append("            text-align: left;\n");
        html.append("            font-weight: 600;\n");
        html.append("        }\n");
        html.append("        tbody tr {\n");
        html.append("            border-bottom: 1px solid #e2e8f0;\n");
        html.append("            transition: background 0.3s;\n");
        html.append("        }\n");
        html.append("        tbody tr:hover {\n");
        html.append("            background: #f7fafc;\n");
        html.append("        }\n");
        html.append("        td {\n");
        html.append("            padding: 15px;\n");
        html.append("            color: #4a5568;\n");
        html.append("        }\n");
        html.append("        .employee-info {\n");
        html.append("            display: flex;\n");
        html.append("            align-items: center;\n");
        html.append("            gap: 15px;\n");
        html.append("        }\n");
        html.append("        .avatar {\n");
        html.append("            width: 40px;\n");
        html.append("            height: 40px;\n");
        html.append("            border-radius: 50%;\n");
        html.append("            background: #667eea;\n");
        html.append("            color: white;\n");
        html.append("            display: flex;\n");
        html.append("            align-items: center;\n");
        html.append("            justify-content: center;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .employee-name {\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: #2d3748;\n");
        html.append("        }\n");
        html.append("        .employee-title {\n");
        html.append("            font-size: 0.9rem;\n");
        html.append("            color: #718096;\n");
        html.append("        }\n");
        html.append("        .badge {\n");
        html.append("            padding: 4px 8px;\n");
        html.append("            border-radius: 4px;\n");
        html.append("            font-size: 0.8rem;\n");
        html.append("            font-weight: 500;\n");
        html.append("        }\n");
        html.append("        .badge-department {\n");
        html.append("            background: #bee3f8;\n");
        html.append("            color: #2b6cb0;\n");
        html.append("        }\n");
        html.append("        .badge-active {\n");
        html.append("            background: #c6f6d5;\n");
        html.append("            color: #22543d;\n");
        html.append("        }\n");
        html.append("        .actions {\n");
        html.append("            display: flex;\n");
        html.append("            gap: 8px;\n");
        html.append("        }\n");
        html.append("        .empty-state {\n");
        html.append("            text-align: center;\n");
        html.append("            padding: 60px 20px;\n");
        html.append("            color: #a0aec0;\n");
        html.append("        }\n");
        html.append("        .empty-state i {\n");
        html.append("            font-size: 3rem;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            color: #cbd5e0;\n");
        html.append("        }\n");
        html.append("        footer {\n");
        html.append("            margin-top: 30px;\n");
        html.append("            text-align: center;\n");
        html.append("            color: #718096;\n");
        html.append("            padding: 20px;\n");
        html.append("            font-size: 0.9rem;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css\">\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <header>\n");
        html.append("            <div class=\"header-content\">\n");
        html.append("                <div>\n");
        html.append("                    <h1><i class=\"fas fa-users\"></i> Employee Directory</h1>\n");
        html.append("                    <p>Manage your organization's workforce</p>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"controls\">\n");
        html.append("                    <input type=\"text\" class=\"search-box\" placeholder=\"Search employees...\" id=\"searchInput\">\n");
        html.append("                    <a href=\"/employees/add\" class=\"btn btn-success\">\n");
        html.append("                        <i class=\"fas fa-user-plus\"></i> Add Employee\n");
        html.append("                    </a>\n");
        html.append("                    <a href=\"/\" class=\"btn btn-primary\">\n");
        html.append("                        <i class=\"fas fa-home\"></i> Home\n");
        html.append("                    </a>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("        </header>\n");
        html.append("        \n");
        html.append("        <div class=\"table-container\">\n");
        
        if (employees.isEmpty()) {
            html.append("            <div class=\"empty-state\">\n");
            html.append("                <i class=\"fas fa-user-friends\"></i>\n");
            html.append("                <h2>No Employees Found</h2>\n");
            html.append("                <p>Get started by adding your first employee to the system.</p>\n");
            html.append("                <a href=\"/employees/add\" class=\"btn btn-success\" style=\"margin-top: 20px;\">\n");
            html.append("                    <i class=\"fas fa-user-plus\"></i> Add First Employee\n");
            html.append("                </a>\n");
            html.append("            </div>\n");
        } else {
            html.append("            <table>\n");
            html.append("                <thead>\n");
            html.append("                    <tr>\n");
            html.append("                        <th>Employee</th>\n");
            html.append("                        <th>Employee Code</th>\n");
            html.append("                        <th>Department</th>\n");
            html.append("                        <th>Job Title</th>\n");
            html.append("                        <th>Hire Date</th>\n");
            html.append("                        <th>Salary</th>\n");
            html.append("                        <th>Actions</th>\n");
            html.append("                    </tr>\n");
            html.append("                </thead>\n");
            html.append("                <tbody>\n");
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            
            for (Employee emp : employees) {
                String initials = (emp.getFirstName().charAt(0) + "" + emp.getLastName().charAt(0)).toUpperCase();
                String hireDate = emp.getHireDate().format(dateFormatter);
                String salary = emp.getFormattedSalary();
                String department = emp.getDepartmentName() != null ? emp.getDepartmentName() : "Not Assigned";
                
                html.append("                    <tr>\n");
                html.append("                        <td>\n");
                html.append("                            <div class=\"employee-info\">\n");
                html.append("                                <div class=\"avatar\">").append(initials).append("</div>\n");
                html.append("                                <div>\n");
                html.append("                                    <div class=\"employee-name\">").append(emp.getFirstName()).append(" ").append(emp.getLastName()).append("</div>\n");
                html.append("                                    <div class=\"employee-title\">").append(emp.getEmail()).append("</div>\n");
                html.append("                                </div>\n");
                html.append("                            </div>\n");
                html.append("                        </td>\n");
                html.append("                        <td><span class=\"badge\">").append(emp.getEmployeeCode()).append("</span></td>\n");
                html.append("                        <td><span class=\"badge badge-department\">").append(department).append("</span></td>\n");
                html.append("                        <td>").append(emp.getJobTitle()).append("</td>\n");
                html.append("                        <td>").append(hireDate).append("</td>\n");
                html.append("                        <td><strong>").append(salary).append("</strong></td>\n");
                html.append("                        <td>\n");
                html.append("                            <div class=\"actions\">\n");
                html.append("                                <a href=\"/employees/view?id=").append(emp.getId()).append("\" class=\"btn btn-info\" style=\"padding: 8px 12px;\">\n");
                html.append("                                    <i class=\"fas fa-eye\"></i>\n");
                html.append("                                </a>\n");
                html.append("                                <a href=\"/employees/edit?id=").append(emp.getId()).append("\" class=\"btn btn-primary\" style=\"padding: 8px 12px;\">\n");
                html.append("                                    <i class=\"fas fa-edit\"></i>\n");
                html.append("                                </a>\n");
                html.append("                                <a href=\"/employees/delete?id=").append(emp.getId()).append("\" \n");
                html.append("                                   class=\"btn btn-danger\" \n");
                html.append("                                   style=\"padding: 8px 12px;\"\n");
                html.append("                                   onclick=\"return confirm('Are you sure you want to delete this employee?');\">\n");
                html.append("                                    <i class=\"fas fa-trash\"></i>\n");
                html.append("                                </a>\n");
                html.append("                            </div>\n");
                html.append("                        </td>\n");
                html.append("                    </tr>\n");
            }
            
            html.append("                </tbody>\n");
            html.append("            </table>\n");
        }
        
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <footer>\n");
        html.append("            <p>Total Employees: ").append(employees.size()).append(" | \n");
        html.append("               <a href=\"/api/employees\" style=\"color: #667eea;\">View JSON API</a>\n");
        html.append("            </p>\n");
        html.append("        </footer>\n");
        html.append("    </div>\n");
        html.append("    \n");
        html.append("    <script>\n");
        html.append("        document.getElementById('searchInput').addEventListener('input', function(e) {\n");
        html.append("            const searchTerm = e.target.value.toLowerCase();\n");
        html.append("            const rows = document.querySelectorAll('tbody tr');\n");
        html.append("            \n");
        html.append("            rows.forEach(row => {\n");
        html.append("                const text = row.textContent.toLowerCase();\n");
        html.append("                row.style.display = text.includes(searchTerm) ? '' : 'none';\n");
        html.append("            });\n");
        html.append("        });\n");
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private static String getAddEmployeeForm() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Add New Employee</title>\n");
        html.append("    <style>\n");
        html.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("        body { \n");
        html.append("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; \n");
        html.append("            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);\n");
        html.append("            min-height: 100vh;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            max-width: 800px;\n");
        html.append("            margin: 0 auto;\n");
        html.append("            padding: 30px;\n");
        html.append("        }\n");
        html.append("        .form-container {\n");
        html.append("            background: white;\n");
        html.append("            padding: 40px;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);\n");
        html.append("        }\n");
        html.append("        h1 {\n");
        html.append("            color: #2d3748;\n");
        html.append("            margin-bottom: 10px;\n");
        html.append("            display: flex;\n");
        html.append("            align-items: center;\n");
        html.append("            gap: 10px;\n");
        html.append("        }\n");
        html.append("        .subtitle {\n");
        html.append("            color: #718096;\n");
        html.append("            margin-bottom: 30px;\n");
        html.append("        }\n");
        html.append("        .form-group {\n");
        html.append("            margin-bottom: 25px;\n");
        html.append("        }\n");
        html.append("        label {\n");
        html.append("            display: block;\n");
        html.append("            margin-bottom: 8px;\n");
        html.append("            font-weight: 500;\n");
        html.append("            color: #4a5568;\n");
        html.append("        }\n");
        html.append("        input, select, textarea {\n");
        html.append("            width: 100%;\n");
        html.append("            padding: 12px 15px;\n");
        html.append("            border: 2px solid #e2e8f0;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            font-size: 16px;\n");
        html.append("            transition: border-color 0.3s;\n");
        html.append("        }\n");
        html.append("        input:focus, select:focus, textarea:focus {\n");
        html.append("            outline: none;\n");
        html.append("            border-color: #667eea;\n");
        html.append("        }\n");
        html.append("        .form-row {\n");
        html.append("            display: grid;\n");
        html.append("            grid-template-columns: 1fr 1fr;\n");
        html.append("            gap: 20px;\n");
        html.append("        }\n");
        html.append("        .btn {\n");
        html.append("            padding: 12px 30px;\n");
        html.append("            border: none;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            font-size: 16px;\n");
        html.append("            font-weight: 500;\n");
        html.append("            cursor: pointer;\n");
        html.append("            transition: all 0.3s;\n");
        html.append("            text-decoration: none;\n");
        html.append("            display: inline-flex;\n");
        html.append("            align-items: center;\n");
        html.append("            justify-content: center;\n");
        html.append("            gap: 8px;\n");
        html.append("        }\n");
        html.append("        .btn-primary {\n");
        html.append("            background: #667eea;\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        .btn-primary:hover {\n");
        html.append("            background: #5a67d8;\n");
        html.append("            transform: translateY(-2px);\n");
        html.append("            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);\n");
        html.append("        }\n");
        html.append("        .btn-secondary {\n");
        html.append("            background: #a0aec0;\n");
        html.append("            color: white;\n");
        html.append("        }\n");
        html.append("        .btn-secondary:hover {\n");
        html.append("            background: #718096;\n");
        html.append("        }\n");
        html.append("        .form-actions {\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: space-between;\n");
        html.append("            margin-top: 40px;\n");
        html.append("            padding-top: 30px;\n");
        html.append("            border-top: 2px solid #e2e8f0;\n");
        html.append("        }\n");
        html.append("        .back-link {\n");
        html.append("            color: #667eea;\n");
        html.append("            text-decoration: none;\n");
        html.append("            display: flex;\n");
        html.append("            align-items: center;\n");
        html.append("            gap: 8px;\n");
        html.append("            font-weight: 500;\n");
        html.append("        }\n");
        html.append("        .back-link:hover {\n");
        html.append("            color: #5a67d8;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css\">\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"form-container\">\n");
        html.append("            <h1><i class=\"fas fa-user-plus\"></i> Add New Employee</h1>\n");
        html.append("            <p class=\"subtitle\">Fill in the details below to register a new employee</p>\n");
        html.append("            \n");
        html.append("            <form method=\"POST\" action=\"/employees/add\">\n");
        html.append("                <div class=\"form-row\">\n");
        html.append("                    <div class=\"form-group\">\n");
        html.append("                        <label for=\"firstName\"><i class=\"fas fa-user\"></i> First Name *</label>\n");
        html.append("                        <input type=\"text\" id=\"firstName\" name=\"firstName\" required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class=\"form-group\">\n");
        html.append("                        <label for=\"lastName\"><i class=\"fas fa-user\"></i> Last Name *</label>\n");
        html.append("                        <input type=\"text\" id=\"lastName\" name=\"lastName\" required>\n");
        html.append("                    </div>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-row\">\n");
        html.append("                    <div class=\"form-group\">\n");
        html.append("                        <label for=\"email\"><i class=\"fas fa-envelope\"></i> Email *</label>\n");
        html.append("                        <input type=\"email\" id=\"email\" name=\"email\" required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class=\"form-group\">\n");
        html.append("                        <label for=\"phone\"><i class=\"fas fa-phone\"></i> Phone</label>\n");
        html.append("                        <input type=\"tel\" id=\"phone\" name=\"phone\">\n");
        html.append("                    </div>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-row\">\n");
        html.append("                    <div class=\"form-group\">\n");
        html.append("                        <label for=\"employeeCode\"><i class=\"fas fa-id-card\"></i> Employee Code *</label>\n");
        html.append("                        <input type=\"text\" id=\"employeeCode\" name=\"employeeCode\" required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class=\"form-group\">\n");
        html.append("                        <label for=\"jobTitle\"><i class=\"fas fa-briefcase\"></i> Job Title *</label>\n");
        html.append("                        <input type=\"text\" id=\"jobTitle\" name=\"jobTitle\" required>\n");
        html.append("                    </div>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-row\">\n");
        html.append("                    <div class=\"form-group\">\n");
        html.append("                        <label for=\"hireDate\"><i class=\"fas fa-calendar-alt\"></i> Hire Date *</label>\n");
        html.append("                        <input type=\"date\" id=\"hireDate\" name=\"hireDate\" required>\n");
        html.append("                    </div>\n");
        html.append("                    <div class=\"form-group\">\n");
        html.append("                        <label for=\"salary\"><i class=\"fas fa-money-bill-wave\"></i> Salary *</label>\n");
        html.append("                        <input type=\"number\" id=\"salary\" name=\"salary\" step=\"0.01\" min=\"0\" required>\n");
        html.append("                    </div>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-group\">\n");
        html.append("                    <label for=\"departmentId\"><i class=\"fas fa-building\"></i> Department</label>\n");
        html.append("                    <select id=\"departmentId\" name=\"departmentId\">\n");
        html.append("                        <option value=\"\">Select Department</option>\n");
        html.append("                        <option value=\"1\">Engineering</option>\n");
        html.append("                        <option value=\"2\">Sales</option>\n");
        html.append("                        <option value=\"3\">Marketing</option>\n");
        html.append("                        <option value=\"4\">HR</option>\n");
        html.append("                        <option value=\"5\">Finance</option>\n");
        html.append("                    </select>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-group\">\n");
        html.append("                    <label for=\"address\"><i class=\"fas fa-map-marker-alt\"></i> Address</label>\n");
        html.append("                    <textarea id=\"address\" name=\"address\" rows=\"3\"></textarea>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-actions\">\n");
        html.append("                    <a href=\"/employees\" class=\"back-link\">\n");
        html.append("                        <i class=\"fas fa-arrow-left\"></i> Back to Employees\n");
        html.append("                    </a>\n");
        html.append("                    <button type=\"submit\" class=\"btn btn-primary\">\n");
        html.append("                        <i class=\"fas fa-save\"></i> Save Employee\n");
        html.append("                    </button>\n");
        html.append("                </div>\n");
        html.append("            </form>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("    \n");
        html.append("    <script>\n");
        html.append("        // Set today's date as default for hire date\n");
        html.append("        document.getElementById('hireDate').valueAsDate = new Date();\n");
        html.append("        \n");
        html.append("        // Generate employee code based on name\n");
        html.append("        document.getElementById('firstName').addEventListener('blur', generateEmployeeCode);\n");
        html.append("        document.getElementById('lastName').addEventListener('blur', generateEmployeeCode);\n");
        html.append("        \n");
        html.append("        function generateEmployeeCode() {\n");
        html.append("            const firstName = document.getElementById('firstName').value;\n");
        html.append("            const lastName = document.getElementById('lastName').value;\n");
        html.append("            \n");
        html.append("            if (firstName && lastName) {\n");
        html.append("                const code = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase() + \n");
        html.append("                           Math.floor(1000 + Math.random() * 9000);\n");
        html.append("                document.getElementById('employeeCode').value = code;\n");
        html.append("            }\n");
        html.append("        }\n");
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private static String handleAddEmployee(Map<String, String> params) throws SQLException {
        try {
            // First create a user
            User user = new User();
            user.setUsername(params.get("email").split("@")[0]);
            user.setEmail(params.get("email"));
            user.setPasswordHash("$2a$10$tempHashForDemoOnly");
            user.setFirstName(params.get("firstName"));
            user.setLastName(params.get("lastName"));
            user.setRole(User.Role.USER);
            user.setActive(true);
            
            UserDAO userDAO = new UserDAO();
            int userId = userDAO.createUser(user);
            
            if (userId > 0) {
                // Then create employee
                Employee employee = new Employee();
                employee.setUserId(userId);
                employee.setEmployeeCode(params.get("employeeCode"));
                employee.setJobTitle(params.get("jobTitle"));
                employee.setHireDate(LocalDate.parse(params.get("hireDate")));
                employee.setSalary(new BigDecimal(params.get("salary")));
                employee.setPhone(params.get("phone"));
                employee.setAddress(params.get("address"));
                
                if (!params.get("departmentId").isEmpty()) {
                    employee.setDepartmentId(Integer.parseInt(params.get("departmentId")));
                }
                
                EmployeeDAO employeeDAO = new EmployeeDAO();
                int employeeId = employeeDAO.createEmployee(employee);
                
                if (employeeId > 0) {
                    StringBuilder html = new StringBuilder();
                    html.append("<!DOCTYPE html>\n");
                    html.append("<html>\n");
                    html.append("<head>\n");
                    html.append("    <title>Success</title>\n");
                    html.append("    <style>\n");
                    html.append("        body { font-family: Arial; padding: 50px; text-align: center; }\n");
                    html.append("        .success { color: #38a169; font-size: 3rem; margin-bottom: 20px; }\n");
                    html.append("        .message { font-size: 1.2rem; margin: 20px 0; }\n");
                    html.append("        .btn { \n");
                    html.append("            display: inline-block; \n");
                    html.append("            margin: 10px; \n");
                    html.append("            padding: 12px 24px; \n");
                    html.append("            background: #667eea; \n");
                    html.append("            color: white; \n");
                    html.append("            text-decoration: none; \n");
                    html.append("            border-radius: 6px;\n");
                    html.append("        }\n");
                    html.append("    </style>\n");
                    html.append("</head>\n");
                    html.append("<body>\n");
                    html.append("    <div class=\"success\">✓</div>\n");
                    html.append("    <h1>Employee Added Successfully!</h1>\n");
                    html.append("    <p class=\"message\">\n");
                    html.append("        Employee <strong>").append(user.getFullName()).append("</strong> has been added to the system.<br>\n");
                    html.append("        Employee Code: <strong>").append(employee.getEmployeeCode()).append("</strong>\n");
                    html.append("    </p>\n");
                    html.append("    <div>\n");
                    html.append("        <a href=\"/employees/add\" class=\"btn\">Add Another</a>\n");
                    html.append("        <a href=\"/employees\" class=\"btn\">View All Employees</a>\n");
                    html.append("        <a href=\"/\" class=\"btn\">Home</a>\n");
                    html.append("    </div>\n");
                    html.append("</body>\n");
                    html.append("</html>\n");
                    return html.toString();
                } else {
                    // Rollback user creation
                    userDAO.deactivateUser(userId);
                    throw new SQLException("Failed to create employee record");
                }
            } else {
                throw new SQLException("Failed to create user account");
            }
        } catch (Exception e) {
            throw new SQLException("Error adding employee: " + e.getMessage());
        }
    }
    
    private static String getEditEmployeeForm(int id) throws SQLException {
        Employee employee = employeeDAO.getEmployeeById(id);
        if (employee == null) {
            return getNotFoundPage();
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Edit Employee</title>\n");
        html.append("    <style>\n");
        html.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f7fa; }\n");
        html.append("        .container { max-width: 800px; margin: 50px auto; padding: 30px; }\n");
        html.append("        .form-container { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }\n");
        html.append("        h1 { color: #2d3748; margin-bottom: 30px; }\n");
        html.append("        .form-group { margin-bottom: 20px; }\n");
        html.append("        label { display: block; margin-bottom: 8px; font-weight: 500; color: #4a5568; }\n");
        html.append("        input, select { width: 100%; padding: 12px; border: 2px solid #e2e8f0; border-radius: 8px; font-size: 16px; }\n");
        html.append("        .form-actions { margin-top: 30px; display: flex; justify-content: space-between; }\n");
        html.append("        .btn { padding: 12px 24px; border: none; border-radius: 8px; cursor: pointer; font-weight: 500; }\n");
        html.append("        .btn-primary { background: #667eea; color: white; }\n");
        html.append("        .btn-secondary { background: #a0aec0; color: white; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"form-container\">\n");
        html.append("            <h1>Edit Employee: ").append(employee.getFirstName()).append(" ").append(employee.getLastName()).append("</h1>\n");
        html.append("            <form method=\"POST\" action=\"/employees/edit\">\n");
        html.append("                <input type=\"hidden\" name=\"id\" value=\"").append(employee.getId()).append("\">\n");
        html.append("                \n");
        html.append("                <div class=\"form-group\">\n");
        html.append("                    <label>Employee Code</label>\n");
        html.append("                    <input type=\"text\" name=\"employeeCode\" value=\"").append(employee.getEmployeeCode()).append("\" required>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-group\">\n");
        html.append("                    <label>Job Title</label>\n");
        html.append("                    <input type=\"text\" name=\"jobTitle\" value=\"").append(employee.getJobTitle()).append("\" required>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-group\">\n");
        html.append("                    <label>Department</label>\n");
        html.append("                    <select name=\"departmentId\">\n");
        html.append("                        <option value=\"\">Select Department</option>\n");
        html.append("                        <option value=\"1\"");
        if (employee.getDepartmentId() != null && employee.getDepartmentId() == 1) html.append(" selected");
        html.append(">Engineering</option>\n");
        html.append("                        <option value=\"2\"");
        if (employee.getDepartmentId() != null && employee.getDepartmentId() == 2) html.append(" selected");
        html.append(">Sales</option>\n");
        html.append("                        <option value=\"3\"");
        if (employee.getDepartmentId() != null && employee.getDepartmentId() == 3) html.append(" selected");
        html.append(">Marketing</option>\n");
        html.append("                        <option value=\"4\"");
        if (employee.getDepartmentId() != null && employee.getDepartmentId() == 4) html.append(" selected");
        html.append(">HR</option>\n");
        html.append("                        <option value=\"5\"");
        if (employee.getDepartmentId() != null && employee.getDepartmentId() == 5) html.append(" selected");
        html.append(">Finance</option>\n");
        html.append("                    </select>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-group\">\n");
        html.append("                    <label>Salary</label>\n");
        html.append("                    <input type=\"number\" name=\"salary\" step=\"0.01\" value=\"").append(employee.getSalary().toPlainString()).append("\" required>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-group\">\n");
        html.append("                    <label>Phone</label>\n");
        html.append("                    <input type=\"text\" name=\"phone\" value=\"");
        if (employee.getPhone() != null) html.append(employee.getPhone());
        html.append("\">\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-group\">\n");
        html.append("                    <label>Address</label>\n");
        html.append("                    <input type=\"text\" name=\"address\" value=\"");
        if (employee.getAddress() != null) html.append(employee.getAddress());
        html.append("\" style=\"height: 80px;\">\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <div class=\"form-actions\">\n");
        html.append("                    <a href=\"/employees\" class=\"btn btn-secondary\">Cancel</a>\n");
        html.append("                    <button type=\"submit\" class=\"btn btn-primary\">Update Employee</button>\n");
        html.append("                </div>\n");
        html.append("            </form>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private static String handleUpdateEmployee(Map<String, String> params) throws SQLException {
        int id = Integer.parseInt(params.get("id"));
        
        Employee employee = employeeDAO.getEmployeeById(id);
        if (employee == null) {
            return getNotFoundPage();
        }
        
        employee.setEmployeeCode(params.get("employeeCode"));
        employee.setJobTitle(params.get("jobTitle"));
        
        if (!params.get("departmentId").isEmpty()) {
            employee.setDepartmentId(Integer.parseInt(params.get("departmentId")));
        } else {
            employee.setDepartmentId(null);
        }
        
        employee.setSalary(new BigDecimal(params.get("salary")));
        employee.setPhone(params.get("phone"));
        employee.setAddress(params.get("address"));
        
        boolean success = employeeDAO.updateEmployee(employee);
        
        if (success) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n");
            html.append("<head>\n");
            html.append("    <title>Update Successful</title>\n");
            html.append("    <style>\n");
            html.append("        body { font-family: Arial; padding: 50px; text-align: center; }\n");
            html.append("        .success { color: #38a169; font-size: 3rem; margin-bottom: 20px; }\n");
            html.append("        .btn { \n");
            html.append("            display: inline-block; \n");
            html.append("            margin: 10px; \n");
            html.append("            padding: 12px 24px; \n");
            html.append("            background: #667eea; \n");
            html.append("            color: white; \n");
            html.append("            text-decoration: none; \n");
            html.append("            border-radius: 6px;\n");
            html.append("        }\n");
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("    <div class=\"success\">✓</div>\n");
            html.append("    <h1>Employee Updated Successfully!</h1>\n");
            html.append("    <p>The employee record has been updated.</p>\n");
            html.append("    <div>\n");
            html.append("        <a href=\"/employees\" class=\"btn\">View All Employees</a>\n");
            html.append("        <a href=\"/\" class=\"btn\">Home</a>\n");
            html.append("    </div>\n");
            html.append("</body>\n");
            html.append("</html>\n");
            return html.toString();
        } else {
            return getErrorPage(new SQLException("Failed to update employee"));
        }
    }
    
    private static String handleDeleteEmployee(int id) throws SQLException {
        boolean success = employeeDAO.deleteEmployee(id);
        
        if (success) {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n");
            html.append("<head>\n");
            html.append("    <title>Delete Successful</title>\n");
            html.append("    <style>\n");
            html.append("        body { font-family: Arial; padding: 50px; text-align: center; }\n");
            html.append("        .success { color: #38a169; font-size: 3rem; margin-bottom: 20px; }\n");
            html.append("        .btn { \n");
            html.append("            display: inline-block; \n");
            html.append("            margin: 10px; \n");
            html.append("            padding: 12px 24px; \n");
            html.append("            background: #667eea; \n");
            html.append("            color: white; \n");
            html.append("            text-decoration: none; \n");
            html.append("            border-radius: 6px;\n");
            html.append("        }\n");
            html.append("    </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("    <div class=\"success\">✓</div>\n");
            html.append("    <h1>Employee Deleted Successfully!</h1>\n");
            html.append("    <p>The employee record has been removed from the system.</p>\n");
            html.append("    <div>\n");
            html.append("        <a href=\"/employees\" class=\"btn\">View All Employees</a>\n");
            html.append("        <a href=\"/\" class=\"btn\">Home</a>\n");
            html.append("    </div>\n");
            html.append("</body>\n");
            html.append("</html>\n");
            return html.toString();
        } else {
            return getErrorPage(new SQLException("Failed to delete employee"));
        }
    }
    
    private static String getEmployeeDetails(int id) throws SQLException {
        Employee employee = employeeDAO.getEmployeeById(id);
        if (employee == null) {
            return getNotFoundPage();
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String initials = (employee.getFirstName().charAt(0) + "" + employee.getLastName().charAt(0)).toUpperCase();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Employee Details</title>\n");
        html.append("    <style>\n");
        html.append("        * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f7fa; }\n");
        html.append("        .container { max-width: 800px; margin: 50px auto; padding: 30px; }\n");
        html.append("        .profile-card { background: white; padding: 40px; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }\n");
        html.append("        .profile-header { display: flex; align-items: center; margin-bottom: 30px; }\n");
        html.append("        .avatar { width: 80px; height: 80px; border-radius: 50%; background: #667eea; color: white; \n");
        html.append("                  display: flex; align-items: center; justify-content: center; font-size: 2rem; font-weight: bold; }\n");
        html.append("        .profile-info { margin-left: 20px; }\n");
        html.append("        .profile-name { font-size: 1.5rem; font-weight: bold; color: #2d3748; }\n");
        html.append("        .profile-title { color: #718096; }\n");
        html.append("        .details-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }\n");
        html.append("        .detail-item { margin-bottom: 20px; }\n");
        html.append("        .detail-label { font-size: 0.9rem; color: #718096; margin-bottom: 5px; }\n");
        html.append("        .detail-value { font-size: 1.1rem; color: #2d3748; }\n");
        html.append("        .btn { \n");
        html.append("            display: inline-block; \n");
        html.append("            margin: 10px; \n");
        html.append("            padding: 12px 24px; \n");
        html.append("            background: #667eea; \n");
        html.append("            color: white; \n");
        html.append("            text-decoration: none; \n");
        html.append("            border-radius: 6px;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"profile-card\">\n");
        html.append("            <div class=\"profile-header\">\n");
        html.append("                <div class=\"avatar\">").append(initials).append("</div>\n");
        html.append("                <div class=\"profile-info\">\n");
        html.append("                    <div class=\"profile-name\">").append(employee.getFirstName()).append(" ").append(employee.getLastName()).append("</div>\n");
        html.append("                    <div class=\"profile-title\">").append(employee.getJobTitle()).append("</div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            \n");
        html.append("            <div class=\"details-grid\">\n");
        html.append("                <div class=\"detail-item\">\n");
        html.append("                    <div class=\"detail-label\">Employee Code</div>\n");
        html.append("                    <div class=\"detail-value\">").append(employee.getEmployeeCode()).append("</div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"detail-item\">\n");
        html.append("                    <div class=\"detail-label\">Email</div>\n");
        html.append("                    <div class=\"detail-value\">").append(employee.getEmail()).append("</div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"detail-item\">\n");
        html.append("                    <div class=\"detail-label\">Department</div>\n");
        html.append("                    <div class=\"detail-value\">").append(employee.getDepartmentName() != null ? employee.getDepartmentName() : "Not Assigned").append("</div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"detail-item\">\n");
        html.append("                    <div class=\"detail-label\">Hire Date</div>\n");
        html.append("                    <div class=\"detail-value\">").append(employee.getHireDate().format(dateFormatter)).append("</div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"detail-item\">\n");
        html.append("                    <div class=\"detail-label\">Salary</div>\n");
        html.append("                    <div class=\"detail-value\">").append(employee.getFormattedSalary()).append("</div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"detail-item\">\n");
        html.append("                    <div class=\"detail-label\">Phone</div>\n");
        html.append("                    <div class=\"detail-value\">").append(employee.getPhone() != null ? employee.getPhone() : "Not provided").append("</div>\n");
        html.append("                </div>\n");
        html.append("                <div class=\"detail-item\">\n");
        html.append("                    <div class=\"detail-label\">Address</div>\n");
        html.append("                    <div class=\"detail-value\">").append(employee.getAddress() != null ? employee.getAddress() : "Not provided").append("</div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            \n");
        html.append("            <div style=\"margin-top: 40px; text-align: center;\">\n");
        html.append("                <a href=\"/employees/edit?id=").append(employee.getId()).append("\" class=\"btn\">Edit</a>\n");
        html.append("                <a href=\"/employees\" class=\"btn\">Back to List</a>\n");
        html.append("                <a href=\"/\" class=\"btn\">Home</a>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private static String getEmployeesJson() throws SQLException {
        List<Employee> employees = employeeDAO.getAllEmployees();
        StringBuilder json = new StringBuilder("[");
        
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            json.append(String.format(
                "{\"id\":%d,\"firstName\":\"%s\",\"lastName\":\"%s\",\"employeeCode\":\"%s\",\"jobTitle\":\"%s\",\"department\":\"%s\",\"email\":\"%s\"}",
                emp.getId(), emp.getFirstName(), emp.getLastName(), emp.getEmployeeCode(),
                emp.getJobTitle(), emp.getDepartmentName() != null ? emp.getDepartmentName() : "",
                emp.getEmail()
            ));
            
            if (i < employees.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]");
        
        return json.toString();
    }
    
    private static String searchEmployeesJson(String query) throws SQLException {
        List<Employee> employees = employeeDAO.searchEmployees(query);
        StringBuilder json = new StringBuilder("{\"query\":\"" + query + "\",\"results\":[");
        
        for (int i = 0; i < employees.size(); i++) {
            Employee emp = employees.get(i);
            json.append(String.format(
                "{\"id\":%d,\"name\":\"%s %s\",\"employeeCode\":\"%s\",\"jobTitle\":\"%s\"}",
                emp.getId(), emp.getFirstName(), emp.getLastName(), emp.getEmployeeCode(), emp.getJobTitle()
            ));
            
            if (i < employees.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]}");
        
        return json.toString();
    }
    
    private static String getUsersPage() throws SQLException {
        List<User> users = userDAO.getAllUsers();
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>System Users</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial; padding: 20px; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
        html.append("        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n");
        html.append("        th { background-color: #667eea; color: white; }\n");
        html.append("        .btn { padding: 8px 16px; background: #667eea; color: white; text-decoration: none; border-radius: 4px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>System Users</h1>\n");
        html.append("    <a href=\"/\" class=\"btn\">Home</a>\n");
        html.append("    <table>\n");
        html.append("        <tr>\n");
        html.append("            <th>ID</th>\n");
        html.append("            <th>Username</th>\n");
        html.append("            <th>Email</th>\n");
        html.append("            <th>Name</th>\n");
        html.append("            <th>Role</th>\n");
        html.append("            <th>Status</th>\n");
        html.append("        </tr>\n");
        
        for (User user : users) {
            html.append("        <tr>\n");
            html.append("            <td>").append(user.getId()).append("</td>\n");
            html.append("            <td>").append(user.getUsername()).append("</td>\n");
            html.append("            <td>").append(user.getEmail()).append("</td>\n");
            html.append("            <td>").append(user.getFirstName()).append(" ").append(user.getLastName()).append("</td>\n");
            html.append("            <td>").append(user.getRole()).append("</td>\n");
            html.append("            <td>").append(user.isActive() ? "Active" : "Inactive").append("</td>\n");
            html.append("        </tr>\n");
        }
        
        html.append("    </table>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }
    
    private static String getDashboard() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>Dashboard</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial; padding: 20px; }\n");
        html.append("        .stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }\n");
        html.append("        .stat-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>Dashboard</h1>\n");
        html.append("    <a href=\"/\">Home</a>\n");
        html.append("    \n");
        html.append("    <div class=\"stats\">\n");
        html.append("        <div class=\"stat-card\">\n");
        html.append("            <h3>Coming Soon</h3>\n");
        html.append("            <p>Analytics Dashboard</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }
    
    private static String getErrorPage(Exception e) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>Error</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial; padding: 50px; text-align: center; }\n");
        html.append("        .error { color: #e53e3e; font-size: 3rem; margin-bottom: 20px; }\n");
        html.append("        .btn { \n");
        html.append("            display: inline-block; \n");
        html.append("            margin: 10px; \n");
        html.append("            padding: 12px 24px; \n");
        html.append("            background: #667eea; \n");
        html.append("            color: white; \n");
        html.append("            text-decoration: none; \n");
        html.append("            border-radius: 6px;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"error\">⚠️</div>\n");
        html.append("    <h1>An Error Occurred</h1>\n");
        html.append("    <p>").append(e.getMessage()).append("</p>\n");
        html.append("    <div>\n");
        html.append("        <a href=\"/\" class=\"btn\">Home</a>\n");
        html.append("        <a href=\"/employees\" class=\"btn\">Back to Employees</a>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }
    
    private static String getNotFoundPage() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>Not Found</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial; padding: 50px; text-align: center; }\n");
        html.append("        .not-found { color: #a0aec0; font-size: 3rem; margin-bottom: 20px; }\n");
        html.append("        .btn { \n");
        html.append("            display: inline-block; \n");
            html.append("            margin: 10px; \n");
        html.append("            padding: 12px 24px; \n");
        html.append("            background: #667eea; \n");
        html.append("            color: white; \n");
        html.append("            text-decoration: none; \n");
        html.append("            border-radius: 6px;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"not-found\">404</div>\n");
        html.append("    <h1>Page Not Found</h1>\n");
        html.append("    <p>The requested page could not be found.</p>\n");
        html.append("    <div>\n");
        html.append("        <a href=\"/\" class=\"btn\">Home</a>\n");
        html.append("        <a href=\"/employees\" class=\"btn\">Employees</a>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        return html.toString();
    }
}