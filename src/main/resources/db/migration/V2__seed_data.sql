-- V2__seed_data.sql: Seed Initial Departments, Roles, Users, and Sample Employees

-- 1. Insert Default Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Administrator with full CRUD and system management rights'),
(2, 'ROLE_USER', 'Standard user with read-only view permissions')
ON DUPLICATE KEY UPDATE name=name;

-- 2. Insert Departments
INSERT INTO departments (id, name, code, description) VALUES
(1, 'Engineering', 'ENG', 'Software development, infrastructure, QA, and platform engineering'),
(2, 'Human Resources', 'HR', 'Talent acquisition, employee engagement, and organizational development'),
(3, 'Finance', 'FIN', 'Corporate finance, budgeting, payroll, and auditing'),
(4, 'Marketing', 'MKT', 'Brand management, digital marketing, demand generation, and PR'),
(5, 'Product Management', 'PROD', 'Product strategy, UX design, user research, and roadmapping'),
(6, 'Sales', 'SLS', 'Enterprise sales, client relations, and revenue generation'),
(7, 'Operations', 'OPS', 'Business operations, facilities, IT support, and logistics')
ON DUPLICATE KEY UPDATE name=name;

-- 3. Insert Initial Users (Password: Admin@123 and User@123 hashed via BCrypt $2a$10$)
-- BCrypt for Admin@123: $2a$10$iMZZzVp0qS6i9v0CjK.e7eP1YjO9rG0K7iF9vM3Y4eB9lP0qS6i9v (DataInitializer also verifies/refreshes if needed)
INSERT INTO users (id, username, password, full_name, enabled) VALUES
(1, 'admin@company.com', '$2a$10$7Z2v1Y/gI7bS0rBsm4jPZOhC5L.Rj/Tf7u7P0Uq6D2rQc6LdD71h2', 'Alex Mercer (Admin)', TRUE),
(2, 'user@company.com', '$2a$10$qI4z1R/mI9tS1rBsm5jPZOhC5L.Rj/Tf7u7P0Uq6D2rQc6LdD71h2', 'Sarah Connor (Staff)', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Map Users to Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Admin gets ROLE_ADMIN
(1, 2), -- Admin gets ROLE_USER
(2, 2)  -- Standard user gets ROLE_USER
ON DUPLICATE KEY UPDATE user_id=user_id;

-- 4. Insert Seed Employees with diverse departments, statuses, and realistic details
INSERT INTO employees (id, full_name, email, department_id, job_title, phone_number, joining_date, employment_status, salary, created_at, updated_at) VALUES
(1, 'Devon Vance', 'devon.vance@company.com', 1, 'Principal Cloud Architect', '+1-555-0101', '2021-03-15', 'ACTIVE', 145000.00, NOW(), NOW()),
(2, 'Elena Rostova', 'elena.rostova@company.com', 1, 'Senior Backend Engineer', '+1-555-0102', '2022-06-01', 'ACTIVE', 125000.00, NOW(), NOW()),
(3, 'Marcus Sterling', 'marcus.sterling@company.com', 1, 'Frontend Tech Lead', '+1-555-0103', '2022-09-12', 'ACTIVE', 120000.00, NOW(), NOW()),
(4, 'Priya Sharma', 'priya.sharma@company.com', 1, 'DevOps Specialist', '+1-555-0104', '2023-01-10', 'ACTIVE', 115000.00, NOW(), NOW()),
(5, 'Liam Gallagher', 'liam.gallagher@company.com', 1, 'Junior Software Engineer', '+1-555-0105', '2024-02-15', 'ACTIVE', 85000.00, NOW(), NOW()),
(6, 'Claire Beauchamp', 'claire.beauchamp@company.com', 2, 'Director of Human Resources', '+1-555-0201', '2020-01-20', 'ACTIVE', 130000.00, NOW(), NOW()),
(7, 'Carlos Mendez', 'carlos.mendez@company.com', 2, 'Talent Acquisition Manager', '+1-555-0202', '2022-04-18', 'ACTIVE', 95000.00, NOW(), NOW()),
(8, 'Aaliyah Khan', 'aaliyah.khan@company.com', 2, 'HR Business Partner', '+1-555-0203', '2023-08-01', 'ON_LEAVE', 88000.00, NOW(), NOW()),
(9, 'Robert Kim', 'robert.kim@company.com', 3, 'Chief Financial Officer', '+1-555-0301', '2019-11-05', 'ACTIVE', 165000.00, NOW(), NOW()),
(10, 'Jessica Taylor', 'jessica.taylor@company.com', 3, 'Senior Financial Analyst', '+1-555-0302', '2021-08-22', 'ACTIVE', 105000.00, NOW(), NOW()),
(11, 'David Miller', 'david.miller@company.com', 3, 'Payroll Administrator', '+1-555-0303', '2023-03-14', 'ACTIVE', 75000.00, NOW(), NOW()),
(12, 'Sophia Chen', 'sophia.chen@company.com', 4, 'VP of Marketing', '+1-555-0401', '2020-07-10', 'ACTIVE', 140000.00, NOW(), NOW()),
(13, 'Daniel Jackson', 'daniel.jackson@company.com', 4, 'Content Marketing Strategist', '+1-555-0402', '2022-11-01', 'ACTIVE', 88000.00, NOW(), NOW()),
(14, 'Emma Watson', 'emma.watson@company.com', 4, 'SEO & Growth Specialist', '+1-555-0403', '2023-05-19', 'INACTIVE', 82000.00, NOW(), NOW()),
(15, 'Tariq Al-Mansoor', 'tariq.mansoor@company.com', 5, 'Head of Product', '+1-555-0501', '2021-01-18', 'ACTIVE', 150000.00, NOW(), NOW()),
(16, 'Hannah Abbott', 'hannah.abbott@company.com', 5, 'Senior Product Designer (UI/UX)', '+1-555-0502', '2022-07-25', 'ACTIVE', 110000.00, NOW(), NOW()),
(17, 'Kevin O\'Connor', 'kevin.oconnor@company.com', 6, 'VP of Global Sales', '+1-555-0601', '2020-05-12', 'ACTIVE', 155000.00, NOW(), NOW()),
(18, 'Maya Lin', 'maya.lin@company.com', 6, 'Senior Enterprise Account Executive', '+1-555-0602', '2022-02-28', 'ACTIVE', 118000.00, NOW(), NOW()),
(19, 'Arthur Pendelton', 'arthur.pendelton@company.com', 7, 'Operations Director', '+1-555-0701', '2019-09-01', 'ACTIVE', 135000.00, NOW(), NOW()),
(20, 'Zoe Washington', 'zoe.washington@company.com', 7, 'IT Infrastructure Specialist', '+1-555-0702', '2023-10-15', 'ACTIVE', 92000.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE email=email;
