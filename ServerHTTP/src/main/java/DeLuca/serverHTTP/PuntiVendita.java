package DeLuca.serverHTTP;

import java.util.ArrayList;

public class PuntiVendita {
	 private float size;
	 ArrayList < Object > listaRisultati = new ArrayList < Object > (); 

	 public float getSize() {
	  return size;
	 }

	 public void setSize(float size) {
	  this.size = size;
	 }

	public ArrayList<Object> getListaRisultati() {
		return listaRisultati;
	}

	public void setListaRisultati(ArrayList<Object> listaRisultati) {
		this.listaRisultati = listaRisultati;
	}
}