package hiberspring.repository;

import hiberspring.domain.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Employee findFirstByFirstNameAndLastName(String firstName, String lastName);

    @Query(value = "select * " +
            "from employees as e " +
            "join branches as b " +
            "on e.branch_id = b.id " +
            "join products as p " +
            "on b.id = p.branch_id " +
            "group by e.id " +
            "order by concat(e.first_name, ' ', e.last_name) asc, " +
            "length(e.position) desc", nativeQuery = true)
    List<Employee> getAllProductiveEmployees();

}
