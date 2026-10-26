package com.example.employeemanagement.security;

import com.example.employeemanagement.entity.*;
import com.example.employeemanagement.repository.DepartmentRepository;
import com.example.employeemanagement.repository.EmployeeRepository;
import com.example.employeemanagement.repository.RoleRepository;
import com.example.employeemanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.admin.username:admin@company.com}")
    private String adminUsername;

    @Value("${app.security.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.security.admin.fullName:System Administrator}")
    private String adminFullName;

    @Value("${app.security.user.username:user@company.com}")
    private String userUsername;

    @Value("${app.security.user.password:User@123}")
    private String userPassword;

    @Value("${app.security.user.fullName:Standard Staff User}")
    private String userFullName;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           EmployeeRepository employeeRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking system initialization and seed data...");

        // 1. Initialize Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN", "Administrator with full access")));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER", "Standard staff user with view access")));

        // 2. Initialize Admin User
        userRepository.findByUsername(adminUsername).ifPresentOrElse(
                user -> {
                    // Refresh password to ensure it matches current configuration
                    user.setPassword(passwordEncoder.encode(adminPassword));
                    user.getRoles().add(adminRole);
                    user.getRoles().add(userRole);
                    userRepository.save(user);
                },
                () -> {
                    User admin = new User(adminUsername, passwordEncoder.encode(adminPassword), adminFullName);
                    Set<Role> roles = new HashSet<>();
                    roles.add(adminRole);
                    roles.add(userRole);
                    admin.setRoles(roles);
                    userRepository.save(admin);
                    log.info("Initialized default admin user: {}", adminUsername);
                }
        );

        // 3. Initialize Standard User
        userRepository.findByUsername(userUsername).ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode(userPassword));
                    user.getRoles().add(userRole);
                    userRepository.save(user);
                },
                () -> {
                    User standardUser = new User(userUsername, passwordEncoder.encode(userPassword), userFullName);
                    Set<Role> roles = new HashSet<>();
                    roles.add(userRole);
                    standardUser.setRoles(roles);
                    userRepository.save(standardUser);
                    log.info("Initialized default staff user: {}", userUsername);
                }
        );

        // 4. Initialize Departments if empty
        if (departmentRepository.count() == 0) {
            Department d1 = departmentRepository.save(new Department("Engineering", "ENG", "Software engineering and infrastructure"));
            Department d2 = departmentRepository.save(new Department("Human Resources", "HR", "People operations and recruitment"));
            Department d3 = departmentRepository.save(new Department("Finance", "FIN", "Financial planning and accounting"));
            Department d4 = departmentRepository.save(new Department("Marketing", "MKT", "Brand and digital outreach"));
            Department d5 = departmentRepository.save(new Department("Product Management", "PROD", "Product discovery and UX"));
            Department d6 = departmentRepository.save(new Department("Sales", "SLS", "Enterprise revenue and client accounts"));
            Department d7 = departmentRepository.save(new Department("Operations", "OPS", "Business ops and workplace logistics"));
            log.info("Initialized 7 default departments");

            // 5. Initialize Sample Employees if empty
            if (employeeRepository.count() == 0) {
                employeeRepository.saveAll(List.of(
                        new Employee("Devon Vance", "devon.vance@company.com", d1, "Principal Cloud Architect", "+1-555-0101", LocalDate.of(2021, 3, 15), EmploymentStatus.ACTIVE, new BigDecimal("145000.00")),
                        new Employee("Elena Rostova", "elena.rostova@company.com", d1, "Senior Backend Engineer", "+1-555-0102", LocalDate.of(2022, 6, 1), EmploymentStatus.ACTIVE, new BigDecimal("125000.00")),
                        new Employee("Marcus Sterling", "marcus.sterling@company.com", d1, "Frontend Tech Lead", "+1-555-0103", LocalDate.of(2022, 9, 12), EmploymentStatus.ACTIVE, new BigDecimal("120000.00")),
                        new Employee("Priya Sharma", "priya.sharma@company.com", d1, "DevOps Specialist", "+1-555-0104", LocalDate.of(2023, 1, 10), EmploymentStatus.ACTIVE, new BigDecimal("115000.00")),
                        new Employee("Liam Gallagher", "liam.gallagher@company.com", d1, "Junior Software Engineer", "+1-555-0105", LocalDate.of(2024, 2, 15), EmploymentStatus.ACTIVE, new BigDecimal("85000.00")),
                        new Employee("Claire Beauchamp", "claire.beauchamp@company.com", d2, "Director of Human Resources", "+1-555-0201", LocalDate.of(2020, 1, 20), EmploymentStatus.ACTIVE, new BigDecimal("130000.00")),
                        new Employee("Carlos Mendez", "carlos.mendez@company.com", d2, "Talent Acquisition Manager", "+1-555-0202", LocalDate.of(2022, 4, 18), EmploymentStatus.ACTIVE, new BigDecimal("95000.00")),
                        new Employee("Aaliyah Khan", "aaliyah.khan@company.com", d2, "HR Business Partner", "+1-555-0203", LocalDate.of(2023, 8, 1), EmploymentStatus.ON_LEAVE, new BigDecimal("88000.00")),
                        new Employee("Robert Kim", "robert.kim@company.com", d3, "Chief Financial Officer", "+1-555-0301", LocalDate.of(2019, 11, 5), EmploymentStatus.ACTIVE, new BigDecimal("165000.00")),
                        new Employee("Jessica Taylor", "jessica.taylor@company.com", d3, "Senior Financial Analyst", "+1-555-0302", LocalDate.of(2021, 8, 22), EmploymentStatus.ACTIVE, new BigDecimal("105000.00")),
                        new Employee("David Miller", "david.miller@company.com", d3, "Payroll Administrator", "+1-555-0303", LocalDate.of(2023, 3, 14), EmploymentStatus.ACTIVE, new BigDecimal("75000.00")),
                        new Employee("Sophia Chen", "sophia.chen@company.com", d4, "VP of Marketing", "+1-555-0401", LocalDate.of(2020, 7, 10), EmploymentStatus.ACTIVE, new BigDecimal("140000.00")),
                        new Employee("Daniel Jackson", "daniel.jackson@company.com", d4, "Content Marketing Strategist", "+1-555-0402", LocalDate.of(2022, 11, 1), EmploymentStatus.ACTIVE, new BigDecimal("88000.00")),
                        new Employee("Emma Watson", "emma.watson@company.com", d4, "SEO & Growth Specialist", "+1-555-0403", LocalDate.of(2023, 5, 19), EmploymentStatus.INACTIVE, new BigDecimal("82000.00")),
                        new Employee("Tariq Al-Mansoor", "tariq.mansoor@company.com", d5, "Head of Product", "+1-555-0501", LocalDate.of(2021, 1, 18), EmploymentStatus.ACTIVE, new BigDecimal("150000.00")),
                        new Employee("Hannah Abbott", "hannah.abbott@company.com", d5, "Senior Product Designer (UI/UX)", "+1-555-0502", LocalDate.of(2022, 7, 25), EmploymentStatus.ACTIVE, new BigDecimal("110000.00")),
                        new Employee("Kevin O'Connor", "kevin.oconnor@company.com", d6, "VP of Global Sales", "+1-555-0601", LocalDate.of(2020, 5, 12), EmploymentStatus.ACTIVE, new BigDecimal("155000.00")),
                        new Employee("Maya Lin", "maya.lin@company.com", d6, "Senior Enterprise Account Executive", "+1-555-0602", LocalDate.of(2022, 2, 28), EmploymentStatus.ACTIVE, new BigDecimal("118000.00")),
                        new Employee("Arthur Pendelton", "arthur.pendelton@company.com", d7, "Operations Director", "+1-555-0701", LocalDate.of(2019, 9, 1), EmploymentStatus.ACTIVE, new BigDecimal("135000.00")),
                        new Employee("Zoe Washington", "zoe.washington@company.com", d7, "IT Infrastructure Specialist", "+1-555-0702", LocalDate.of(2023, 10, 15), EmploymentStatus.ACTIVE, new BigDecimal("92000.00"))
                ));
                log.info("Initialized 20 default seed employees");
            }
        }

        log.info("System initialization complete.");
    }
}
