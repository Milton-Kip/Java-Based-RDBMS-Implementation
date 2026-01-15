-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 15, 2026 at 10:08 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `company_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `audit_logs`
--

CREATE TABLE `audit_logs` (
  `id` int(11) NOT NULL,
  `table_name` varchar(50) NOT NULL,
  `record_id` int(11) NOT NULL,
  `action` enum('INSERT','UPDATE','DELETE') NOT NULL,
  `old_values` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`old_values`)),
  `new_values` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`new_values`)),
  `changed_by` int(11) DEFAULT NULL,
  `changed_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `ip_address` varchar(45) DEFAULT NULL,
  `user_agent` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `audit_logs`
--

INSERT INTO `audit_logs` (`id`, `table_name`, `record_id`, `action`, `old_values`, `new_values`, `changed_by`, `changed_at`, `ip_address`, `user_agent`) VALUES
(1, 'users', 1, 'INSERT', NULL, '{\"username\": \"admin\", \"email\": \"admin@company.com\", \"role\": \"admin\"}', 1, '2026-01-14 06:12:50', NULL, NULL),
(2, 'users', 2, 'INSERT', NULL, '{\"username\": \"john.doe\", \"email\": \"john.doe@company.com\", \"role\": \"user\"}', 2, '2026-01-14 06:12:50', NULL, NULL),
(3, 'users', 3, 'INSERT', NULL, '{\"username\": \"jane.smith\", \"email\": \"jane.smith@company.com\", \"role\": \"manager\"}', 3, '2026-01-14 06:12:50', NULL, NULL),
(4, 'users', 4, 'INSERT', NULL, '{\"username\": \"bob.johnson\", \"email\": \"bob.johnson@company.com\", \"role\": \"user\"}', 4, '2026-01-14 06:12:50', NULL, NULL),
(5, 'users', 5, 'INSERT', NULL, '{\"username\": \"kipyegonmilton\", \"email\": \"kipyegonmilton@gmail.com\", \"role\": \"user\"}', 5, '2026-01-14 06:52:57', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `departments`
--

CREATE TABLE `departments` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `manager_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `departments`
--

INSERT INTO `departments` (`id`, `name`, `description`, `manager_id`, `created_at`) VALUES
(1, 'Engineering', 'Software development and engineering department', NULL, '2026-01-14 06:12:50'),
(2, 'Sales', 'Sales and business development', NULL, '2026-01-14 06:12:50'),
(3, 'Marketing', 'Marketing and communications', NULL, '2026-01-14 06:12:50'),
(4, 'HR', 'Human Resources and employee relations', NULL, '2026-01-14 06:12:50'),
(5, 'Finance', 'Finance and accounting department', NULL, '2026-01-14 06:12:50');

-- --------------------------------------------------------

--
-- Table structure for table `employees`
--

CREATE TABLE `employees` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `department_id` int(11) DEFAULT NULL,
  `employee_code` varchar(20) NOT NULL,
  `hire_date` date NOT NULL,
  `salary` decimal(10,2) DEFAULT NULL CHECK (`salary` >= 0),
  `job_title` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `employees`
--

INSERT INTO `employees` (`id`, `user_id`, `department_id`, `employee_code`, `hire_date`, `salary`, `job_title`, `phone`, `address`) VALUES
(1, 2, 1, 'EMP001', '2022-01-15', 75000.00, 'Software Engineer', NULL, NULL),
(2, 3, 1, 'EMP002', '2021-03-10', 85000.00, 'Senior Software Engineer', NULL, NULL),
(3, 4, 2, 'EMP003', '2023-05-20', 65000.00, 'Sales Representative', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `projects`
--

CREATE TABLE `projects` (
  `id` int(11) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `department_id` int(11) DEFAULT NULL,
  `manager_id` int(11) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `budget` decimal(15,2) DEFAULT NULL,
  `status` enum('planning','active','on_hold','completed','cancelled') DEFAULT 'planning',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `projects`
--

INSERT INTO `projects` (`id`, `name`, `description`, `department_id`, `manager_id`, `start_date`, `end_date`, `budget`, `status`, `created_at`) VALUES
(1, 'Website Redesign', 'Complete redesign of company website', 1, 3, '2024-01-01', '2024-06-30', 50000.00, 'active', '2026-01-14 06:12:51'),
(2, 'Mobile App Development', 'Development of new mobile application', 1, 3, '2024-02-01', '2024-12-31', 150000.00, 'planning', '2026-01-14 06:12:51'),
(3, 'Sales Portal', 'Internal sales management portal', 2, 3, '2024-03-01', '2024-09-30', 75000.00, 'active', '2026-01-14 06:12:51');

-- --------------------------------------------------------

--
-- Table structure for table `project_assignments`
--

CREATE TABLE `project_assignments` (
  `id` int(11) NOT NULL,
  `project_id` int(11) NOT NULL,
  `employee_id` int(11) NOT NULL,
  `assigned_date` date DEFAULT curdate(),
  `role_in_project` varchar(100) DEFAULT NULL,
  `hours_allocated` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `project_assignments`
--

INSERT INTO `project_assignments` (`id`, `project_id`, `employee_id`, `assigned_date`, `role_in_project`, `hours_allocated`) VALUES
(1, 1, 1, '2026-01-14', 'Frontend Developer', 200),
(2, 1, 2, '2026-01-14', 'Backend Developer', 180),
(3, 2, 1, '2026-01-14', 'Mobile Developer', 300);

-- --------------------------------------------------------

--
-- Table structure for table `tasks`
--

CREATE TABLE `tasks` (
  `id` int(11) NOT NULL,
  `project_id` int(11) NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `assigned_to` int(11) DEFAULT NULL,
  `priority` enum('low','medium','high','critical') DEFAULT 'medium',
  `status` enum('todo','in_progress','review','completed','blocked') DEFAULT 'todo',
  `due_date` date DEFAULT NULL,
  `estimated_hours` decimal(5,2) DEFAULT NULL,
  `actual_hours` decimal(5,2) DEFAULT 0.00,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `tasks`
--

INSERT INTO `tasks` (`id`, `project_id`, `title`, `description`, `assigned_to`, `priority`, `status`, `due_date`, `estimated_hours`, `actual_hours`, `created_at`, `updated_at`) VALUES
(1, 1, 'Design Homepage', 'Create new homepage design', 1, 'high', 'in_progress', '2024-02-15', 40.00, 0.00, '2026-01-14 06:12:52', '2026-01-14 06:12:52'),
(2, 1, 'Implement User Auth', 'Implement user authentication system', 2, 'high', 'todo', '2024-03-01', 60.00, 0.00, '2026-01-14 06:12:52', '2026-01-14 06:12:52'),
(3, 2, 'Requirements Gathering', 'Gather requirements for mobile app', 1, 'medium', 'todo', '2024-02-28', 20.00, 0.00, '2026-01-14 06:12:52', '2026-01-14 06:12:52');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `first_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) DEFAULT NULL,
  `role` enum('admin','manager','user') DEFAULT 'user',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password_hash`, `first_name`, `last_name`, `role`, `is_active`, `created_at`, `updated_at`) VALUES
(1, 'admin', 'admin@company.com', '$2a$10$YourHashedPasswordHere', 'System', 'Administrator', 'admin', 1, '2026-01-14 06:12:50', '2026-01-14 06:12:50'),
(2, 'john.doe', 'john.doe@company.com', '$2a$10$hashed1', 'John', 'Doe', 'user', 1, '2026-01-14 06:12:50', '2026-01-14 06:12:50'),
(3, 'jane.smith', 'jane.smith@company.com', '$2a$10$hashed2', 'Jane', 'Smith', 'manager', 1, '2026-01-14 06:12:50', '2026-01-14 06:12:50'),
(4, 'bob.johnson', 'bob.johnson@company.com', '$2a$10$hashed3', 'Bob', 'Johnson', 'user', 1, '2026-01-14 06:12:50', '2026-01-14 06:12:50'),
(5, 'kipyegonmilton', 'kipyegonmilton@gmail.com', '$2a$10$tempHashForDemoOnly', 'Kipyegon', 'Milton', 'user', 1, '2026-01-14 06:52:57', '2026-01-14 06:52:57');

--
-- Triggers `users`
--
DELIMITER $$
CREATE TRIGGER `users_audit_delete` AFTER DELETE ON `users` FOR EACH ROW BEGIN
    INSERT INTO audit_logs (table_name, record_id, action, old_values)
    VALUES ('users', OLD.id, 'DELETE',
            JSON_OBJECT('username', OLD.username, 'email', OLD.email, 'role', OLD.role));
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `users_audit_insert` AFTER INSERT ON `users` FOR EACH ROW BEGIN
    INSERT INTO audit_logs (table_name, record_id, action, new_values, changed_by)
    VALUES ('users', NEW.id, 'INSERT', 
            JSON_OBJECT('username', NEW.username, 'email', NEW.email, 'role', NEW.role),
            NEW.id);
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `users_audit_update` AFTER UPDATE ON `users` FOR EACH ROW BEGIN
    INSERT INTO audit_logs (table_name, record_id, action, old_values, new_values, changed_by)
    VALUES ('users', NEW.id, 'UPDATE',
            JSON_OBJECT('username', OLD.username, 'email', OLD.email, 'role', OLD.role, 'is_active', OLD.is_active),
            JSON_OBJECT('username', NEW.username, 'email', NEW.email, 'role', NEW.role, 'is_active', NEW.is_active),
            NEW.id);
END
$$
DELIMITER ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `changed_by` (`changed_by`),
  ADD KEY `idx_table_record` (`table_name`,`record_id`),
  ADD KEY `idx_changed_at` (`changed_at`),
  ADD KEY `idx_action` (`action`);

--
-- Indexes for table `departments`
--
ALTER TABLE `departments`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`),
  ADD KEY `manager_id` (`manager_id`);

--
-- Indexes for table `employees`
--
ALTER TABLE `employees`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`),
  ADD UNIQUE KEY `employee_code` (`employee_code`),
  ADD KEY `idx_employee_code` (`employee_code`),
  ADD KEY `idx_department` (`department_id`),
  ADD KEY `idx_job_title` (`job_title`);

--
-- Indexes for table `projects`
--
ALTER TABLE `projects`
  ADD PRIMARY KEY (`id`),
  ADD KEY `manager_id` (`manager_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_department` (`department_id`);

--
-- Indexes for table `project_assignments`
--
ALTER TABLE `project_assignments`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_assignment` (`project_id`,`employee_id`),
  ADD KEY `idx_project` (`project_id`),
  ADD KEY `idx_employee` (`employee_id`);

--
-- Indexes for table `tasks`
--
ALTER TABLE `tasks`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_project_status` (`project_id`,`status`),
  ADD KEY `idx_assigned` (`assigned_to`),
  ADD KEY `idx_priority` (`priority`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_username` (`username`),
  ADD KEY `idx_email` (`email`),
  ADD KEY `idx_role` (`role`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `audit_logs`
--
ALTER TABLE `audit_logs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `departments`
--
ALTER TABLE `departments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `employees`
--
ALTER TABLE `employees`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `projects`
--
ALTER TABLE `projects`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `project_assignments`
--
ALTER TABLE `project_assignments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `tasks`
--
ALTER TABLE `tasks`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `departments`
--
ALTER TABLE `departments`
  ADD CONSTRAINT `departments_ibfk_1` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `employees`
--
ALTER TABLE `employees`
  ADD CONSTRAINT `employees_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `employees_ibfk_2` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `projects`
--
ALTER TABLE `projects`
  ADD CONSTRAINT `projects_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `projects_ibfk_2` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `project_assignments`
--
ALTER TABLE `project_assignments`
  ADD CONSTRAINT `project_assignments_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `project_assignments_ibfk_2` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `tasks`
--
ALTER TABLE `tasks`
  ADD CONSTRAINT `tasks_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `tasks_ibfk_2` FOREIGN KEY (`assigned_to`) REFERENCES `employees` (`id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
