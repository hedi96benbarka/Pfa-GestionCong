package tn.iit.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.iit.entities.Contrat;
import tn.iit.repositories.ContratRepo;

@Service
public class ContratService {

	@Autowired
	private ContratRepo contratRepo;
	
	public List<Contrat> getAllContract() {
		return contratRepo.findAll();
	}
}
