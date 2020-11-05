package tp.model;

import javax.xml.bind.annotation.XmlRootElement;

/* cette classe permet à JAX-WS de le traiter comme un élément a part entière et
de le sérialiser en XML. Cela permet donc d'avertir l'utilisateur de façon concise sur la raison
de l'échec de sa requête
*/

@XmlRootElement
public class RestException {

	private String message;
	
	public RestException() {
	}
	
	public RestException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String toString() {
		return message;
	}
}
