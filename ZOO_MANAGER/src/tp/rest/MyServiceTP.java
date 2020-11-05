package tp.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;

import tp.model.Animal;
import tp.model.Cage;
import tp.model.CageNotFoundException;
import tp.model.Center;
import tp.model.CenterException;
import tp.model.Position;
import tp.model.RestException;

@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class MyServiceTP implements Provider<Source> {

    public static final String url = "http://127.0.0.1:8084/";
	private static final Pattern POSITION_REGEX = Pattern.compile("^lat=\\d*.\\d*&lng=\\d*.\\d*$");
	private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}$");
	private static final Object WOLFRAM_API_KEY = "77WRLE-HLPLXJKA7R";
	private static final Object GRAPHHOPPER_API_KEY = "84194dc6-c2dc-4137-829d-83de8e23ecdc";

    public static void main(String args[]) {
        Endpoint e = Endpoint.create(HTTPBinding.HTTP_BINDING, new MyServiceTP());

        e.publish(url);
        System.out.println("Service started, listening on " + url);
        // pour arréter : e.stop();
    }

    private JAXBContext jc;

    @javax.annotation.Resource(type = Object.class)
    protected WebServiceContext wsContext;

    private Center center = new Center(new LinkedList<>(), new Position(49.30494d, 1.2170602d), "Biotropica");

    public MyServiceTP() {
        try {
            jc = JAXBContext.newInstance(Center.class, Cage.class, Animal.class, Position.class, RestException.class);
        } catch (JAXBException je) {
            System.out.println("Exception " + je);
            throw new WebServiceException("Cannot create JAXBContext", je);
        }

        // Fill our center with some animals
        
        Cage usa = new Cage(
                "usa",
                new Position(49.305d, 1.2157357d),
                25,
                new LinkedList<>(Arrays.asList(
                        new Animal("Tic", "usa", "Chipmunk", UUID.randomUUID()),
                        new Animal("Tac", "usa", "Chipmunk", UUID.randomUUID())
                ))
        );

        Cage amazon = new Cage(
                "amazon",
                new Position(49.305142d, 1.2154067d),
                15,
                new LinkedList<>(Arrays.asList(
                        new Animal("Canine", "amazon", "Piranha", UUID.randomUUID()),
                        new Animal("Incisive", "amazon", "Piranha", UUID.randomUUID()),
                        new Animal("Molaire", "amazon", "Piranha", UUID.randomUUID()),
                        new Animal("De lait", "amazon", "Piranha", UUID.randomUUID())
                ))
        );

        center.getCages().addAll(Arrays.asList(usa, amazon));
    }

    public Source invoke(Source source) {
        MessageContext mc = wsContext.getMessageContext();
        String path = (String) mc.get(MessageContext.PATH_INFO);
        String method = (String) mc.get(MessageContext.HTTP_REQUEST_METHOD);
        
        if (path == null) {
            throw new HTTPException(404);
        }
        // determine the targeted ressource of the call
        try {
        	String[] path_parts = path.split("/");
            // no target, throw a 404 exception.
            
            // "/animals" target - Redirect to the method in charge of managing this sort of call.
            if (path.startsWith("animals")) {
                
                switch (path_parts.length){
                    case 1 :
                        return this.animalsCrud(method, source);
                    case 2 :
                        return this.animalCrud(method, source, path_parts[1]);
                    case 3 :
                    	if (path_parts[2].equals("wolf"))
                    		return this.animalWolframCrud(method, source, path_parts[1]);
                    	else
                    		throw new HTTPException(404);
                    default:
                        throw new HTTPException(404);
                }
            }
            else if (path.startsWith("find/") && method.equals("GET")) {
        		
            	if (path_parts.length == 3) {
            		return this.findCrud(path_parts[1], path_parts[2]);
            	} else {
            		throw new HTTPException(404);
            	}
            	
            	
            }
            else if (path.startsWith("center/journey/from") && path_parts.length == 4) {
            	return this.routeCrud(method, source, path_parts[3]);
            }
            else if (path.startsWith("cages")) {
            	switch (path_parts.length){
            		case 1:
            			return this.cagesCrud(method, source);
            		case 2:
            			return this.cageCrud(method, source, path_parts[1]);
            		case 3: 
            			if (path.startsWith("cages/clear") && method.equals("DELETE"))
            				return cagesClearCrud(source, path_parts[2]);
            			else {
            				throw new HTTPException(404);
            			}
            				
            		default:
            			throw new HTTPException(405);
            			
            	}
            	
            }
            else {
                throw new HTTPException(404);
            }
        } catch (JAXBException e) {
            throw new HTTPException(500);
        }
    }

    

	/**
     * Method bound to calls on /animals/{something}
     */
    private Source animalCrud(String method, Source source, String animal_id) throws JAXBException {
    	
		try {
			switch (method) {
			case "GET":
				return new JAXBSource(this.jc, center.findAnimalById(StringToUUID(animal_id)));
			case "POST":
				return new JAXBSource(this.jc, center.createAnimalById(unmarshalAnimal(source), StringToUUID(animal_id)));
			case "PUT":
				return new JAXBSource(this.jc, center.modifyAnimalById(unmarshalAnimal(source), StringToUUID(animal_id)));
			case "DELETE":
				return new JAXBSource(this.jc, center.deleteAnimalById(StringToUUID(animal_id)));
			default:
				throw new HTTPException(404);
			}
		} catch (CenterException e) {
			return new JAXBSource(this.jc, new RestException(e.getMessage()));
		} 
    }

	/**
     * Method bound to calls on /animals
     */ 
    private Source animalsCrud(String method, Source source) throws JAXBException {
    	try {
    		switch (method) {
				case "GET":
					return new JAXBSource(this.jc, center);
				case "POST":
					return new JAXBSource(this.jc, center.addAnimal(unmarshalAnimal(source)));
				case "PUT":
					return new JAXBSource(this.jc, center.modifyAllAnimals(unmarshalAnimal(source)));
				case "DELETE":
					return new JAXBSource(this.jc, center.deleteAllAnimals());
				default:
					throw new HTTPException(404);
			}
    	} catch (CenterException e) {
    		return new JAXBSource(this.jc, new RestException(e.getMessage()));
    	}
    }
    /**
     * Method bound to calls on /find/something/{something}
     */
    private Source findCrud(String command, String path_arg) throws JAXBException {
    	try {
    		switch (command) {
	    		case "byName" :
	            	return new JAXBSource(this.jc, center.findAnimalByName(path_arg));
	            case "at" :
	            	return new JAXBSource(this.jc, center.findAnimalByPosition(getPosition(path_arg)));
	            case "near" :
	            	return new JAXBSource(this.jc, center.findAnimalsNearPosition(getPosition(path_arg)));
	            case "allAt" :
	            	return new JAXBSource(this.jc, center.findAnimalsByPosition(getPosition(path_arg)));
	            default:
	            	throw new HTTPException(404);
			}
    	} catch (CenterException e) {
    		return new JAXBSource(this.jc, new RestException(e.getMessage()));
    	} 
    }
    
    /**
     * Method bound to calls on /center/journey/from/{position}
     */
    private Source routeCrud(String method, Source source, String position) throws JAXBException {
    	if (!method.equals("GET")) {
    		throw new HTTPException(404);
    	}
    	
    	try {
    		// Récupérer la position de départ du trajet
        	Position initialPosition = getPosition(position);
        	
        	// Récupérer la position du centre
        	Position centerPosition = center.getPosition();
        	
        	// Créer URL GraphHopper
        	String url = String.format(Locale.US, "https://graphhopper.com/api/1/route?point=%f,%f&point=%f,%f&locale=fr&type=gpx&key=%s", 
        			initialPosition.getLatitude(), initialPosition.getLongitude(), centerPosition.getLatitude(), 
        			centerPosition.getLongitude(), GRAPHHOPPER_API_KEY);
    		return getResponseFromService(url, method);
    	} catch (IOException | CenterException e) {
    		return new JAXBSource(this.jc, new RestException(e.getMessage()));
    	}
    }
    /**
     * Method bound to calls on /animals/{animalId}/wolf
     */
	private Source animalWolframCrud(String method, Source source, String animalId) throws JAXBException {
		// TODO Auto-generated method stub
    	if (!method.equals("GET")) {
			throw new HTTPException(404);
		}
    	try {
			// Récupération de l'animal dont on souhaite obtenir des informations
			Animal animal = center.findAnimalById(UUID.fromString(animalId));
			
			//Forme l'URL en respectant l'API imposée par WolframAlpha
			String url = String.format("http://api.wolframalpha.com/v2/query?appid=%s&input=%s", WOLFRAM_API_KEY, URLEncoder.encode(animal.getSpecies(), StandardCharsets.UTF_8.toString()));

	    	try {
	    		return getResponseFromService(url, method);
	    	} catch (IOException e) {
	    		throw new HTTPException(404);
	    	}
		} catch (CenterException e) {
			return new JAXBSource(this.jc, new RestException(e.getMessage()));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			return new JAXBSource(this.jc, e1);
		}
		
	}
	
	 /**final static
     * Method bound to calls on /cages/{something}
     */
    private Source cageCrud(String method, Source source, String cage_name) throws JAXBException {
		try {
			switch (method) {
			case "GET":
				return new JAXBSource(this.jc, center.findCageByName(cage_name));
			case "PUT":
				return new JAXBSource(this.jc, center.modifyCageByName(unmarshalCage(source), cage_name));
			case "DELETE":
				return new JAXBSource(this.jc, center.deleteCageByName(cage_name));
			default:
				throw new HTTPException(404);
			}
		} catch (CenterException e) {
			return new JAXBSource(this.jc, new RestException(e.getMessage()));
		}
    }
    
    /**
     * Method bound to calls on /cages/clear/{name}
     * 
     */
    private Source cagesClearCrud(Source source, String cage_name) throws JAXBException {
    	
		try {
			return new JAXBSource(this.jc, center.clearCageByName(cage_name));
		} catch (CageNotFoundException e) {
			// TODO Auto-generated catch block
			return new JAXBSource(this.jc, new RestException(e.getMessage()));
		}
    	
    }
    /**
     * Method bound to calls on /cages
     * 
     */
    private Source cagesCrud(String method, Source source) throws JAXBException {
    	try {
			switch (method) {
			case "POST":
				return new JAXBSource(this.jc, center.addCage(unmarshalCage(source)));
			case "PUT":
				return new JAXBSource(this.jc, center.modifyAllCages(unmarshalCage(source)));
			case "DELETE":
				return new JAXBSource(this.jc, center.deleteAllCages());
			default:
				throw new HTTPException(404);
			}
    	} catch (CenterException e) {
    		return new JAXBSource(this.jc, new RestException(e.getMessage()));
    	}
    }
    
    /*
     * Permet de faire appel é un service é partir d'un URL et de la méthode d'appel
     * Retource une source qui encapsule la réponse HTTP obtenu
     */
    
    private Source getResponseFromService(String url, String method) throws IOException {
    	// Ouvrir connexion HTTP é l'Url indiqué
    	HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    	// Spécifier la méthode de la requéte
    	connection.setRequestMethod(method);
		
    	// Récupérer la réponse modify
    	BufferedReader bufferReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
        
    	// Lire le résultat
    	String line;
    	StringBuilder response = new StringBuilder();
        while ((line = bufferReader.readLine()) != null) {
            response.append(line);
        }
        
        bufferReader.close();
        
        // Retourne le résultat sous forme de Source
        return new StreamSource(new StringReader(String.valueOf(response)));
    }
    
    /*
     * Permet de parser les éléments de position se trouvant sur l'url
     * Retouurne un objet Position
     */
    private Position getPosition(String position) throws CenterException {
    	if (!POSITION_REGEX.matcher(position).matches()) {
    		throw new CenterException("Le format de la position est incorrect");
    	}
    	String positions[] =  position.split("&");
    	double lat = Double.parseDouble(positions[0].replace("lat=", ""));
    	double lng = Double.parseDouble(positions[1].replaceAll("lng=", ""));
    	return new Position(lat, lng);
    }
 
    /*
     * Convertit un identifiant donné en String en UUID
     */
    
    private UUID StringToUUID(String uuid) throws CenterException{
		// TODO Auto-generated method stub
    	if (!UUID_REGEX.matcher(uuid).matches()) {
    		throw new CenterException("Le format de l'identifiant est incorrect");
    	}
		return UUID.fromString(uuid);
	}
    
    /*
     * Permet d'obtenir un animal généré par JAXB
     */
    
    private Animal unmarshalAnimal(Source source) throws JAXBException {
        return (Animal) this.jc.createUnmarshaller().unmarshal(source);
    }
    /*
     * Permet d'obtenir une cage généré par JAXB
     */
    private Cage unmarshalCage(Source source) throws JAXBException {
    	//Créer une cage par défaut
    	Cage cage = (Cage) this.jc.createUnmarshaller().unmarshal(source);
    	
    	//Instancier l'attribut residents s'il est null a la creation 
    	if (cage.getResidents() == null) {
    		cage.setResidents(new ArrayList<Animal>());
    	}
    	
    	return cage;
    }
}