package one.digitalinnovation.beerstock.repository;
import one.digitalinnovation.beerstock.entity.Beer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

//Papel de "conversar" (gerenciamento) com o BD;
public interface BeerRepository extends JpaRepository<Beer, Long> { //Entidade e Id;
    Optional<Beer> findByName(String name); //Busca pelo nome das cervejas;
}
