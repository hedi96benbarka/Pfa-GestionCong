package tn.iit.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;


@Data
@Entity
@Table(name = "contrats")
public class Contrat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String contrat ;
	private ContratType type;
	private float charge ;
	private float solde;
	
	public enum ContratType{
		ADMINISTRATIF, ENSEIGNANT
	}
}
