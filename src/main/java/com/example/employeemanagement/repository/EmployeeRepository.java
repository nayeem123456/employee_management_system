package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.entity.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("SELECT e FROM Employee e " +
           "WHERE (:query IS NULL OR :query = '' OR " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.department.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Employee> searchEmployees(@Param("query") String query, Pageable pageable);

    @Query("SELECT e FROM Employee e " +
           "WHERE (:departmentId IS NULL OR e.department.id = :departmentId) " +
           "AND (:status IS NULL OR e.employmentStatus = :status) " +
           "AND (:query IS NULL OR :query = '' OR " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.department.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Employee> filterEmployees(@Param("departmentId") Long departmentId,
                                  @Param("status") EmploymentStatus status,
                                  @Param("query") String query,
                                  Pageable pageable);

    long countByEmploymentStatus(EmploymentStatus status);

    long countByDepartmentId(Long departmentId);

    long countByDepartmentIdAndEmploymentStatus(Long departmentId, EmploymentStatus status);

    long countByJoiningDateBetween(LocalDate startDate, LocalDate endDate);

    List<Employee> findTop5ByOrderByCreatedAtDesc();

    List<Employee> findByJoiningDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT e.department.id, e.department.name, e.department.code, COUNT(e), " +
           "SUM(CASE WHEN e.employmentStatus = 'ACTIVE' THEN 1 ELSE 0 END), " +
           "AVG(e.salary) " +
           "FROM Employee e GROUP BY e.department.id, e.department.name, e.department.code " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> getDepartmentAggregationStats();

    @Query("SELECT e.employmentStatus, COUNT(e) FROM Employee e GROUP BY e.employmentStatus")
    List<Object[]> getStatusDistribution();
}
