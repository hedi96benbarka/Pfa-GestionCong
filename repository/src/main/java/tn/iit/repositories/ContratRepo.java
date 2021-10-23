package tn.iit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.iit.entities.Contrat;

public interface ContratRepo extends JpaRepository<Contrat, Long> {

}
