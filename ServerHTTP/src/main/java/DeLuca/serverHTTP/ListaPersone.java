package DeLuca.serverHTTP;

import java.util.ArrayList;

public class ListaPersone {
	private ArrayList<Persona> persone;
	
	public ListaPersone() {
		this.persone = new ArrayList<>();
	}
	
	public void addPersona(Persona p) {
		persone.add(p);
	}

	public ArrayList<Persona> getPersone() {
		return persone;
	}
	
}
