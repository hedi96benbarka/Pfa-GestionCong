package tn.iit.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tn.iit.entities.SoldeHistory;
import tn.iit.repositories.SoldeHistoryRepo;

@Service
public class HistoryService {
	
	@Autowired
	private SoldeHistoryRepo soldeRepo;
	
	public void saveSoldeHistory(SoldeHistory soldHis) {
		soldeRepo.save(soldHis);
	}
	
}
