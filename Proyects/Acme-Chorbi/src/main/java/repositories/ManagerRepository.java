
package repositories;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import domain.Manager;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {

	//Queries de la dashboard
	@Query("select e.organiser from Event e group by e.organiser order by count(e) DESC")
	Collection<Manager> findManagersOrderByEvent();

}
