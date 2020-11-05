package tp.rest;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;

import tp.model.Animal;
import tp.model.Cage;
import tp.model.Center;
import tp.model.Position;


public class MyClient {
    private Service service;
    private JAXBContext jc;

    private static final QName qname = new QName("", "");
    private static final String URL = "http://127.0.0.1:8084";
    private static final String ANIMALS_URL = URL + "/animals";
    private static final String FIND_BY_NAME_URL = URL + "/find/byName/";
    private static final String FIND_AT_POS_URL = URL + "/find/at/";
    private static final String FIND_NEAR_POS_URL = URL + "/find/near/";
    private static final String WOLF_URL = "/wolf";
    private static final String ROUTE_URL= URL + "/center/journey/from/";
    private static final String CAGES_URL = URL + "/cages";
    private static final String CAGES_CLEAR_URL = CAGES_URL + "/clear/";
    private static final String POST = "POST";
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
	


    public MyClient() {
        try {
            jc = JAXBContext.newInstance(Center.class, Cage.class, Animal.class, Position.class);
        } catch (JAXBException je) {
            System.out.println("Cannot create JAXBContext " + je);
        }
    }

    public void add_animal(Animal animal) throws JAXBException {
        Source result = getSource(POST, ANIMALS_URL,new JAXBSource(jc, animal));
        printSource(result);
    }
    

    /**
     * Affiche tous les animaux du centre
     */
    public void getAll_animals() {
    	printSource(getSource(GET, ANIMALS_URL, null));
    }
    
    /**
     * Affiche l'animal identifié par "id"
     */
    public void get_animal_with_id(UUID id) {
    	printSource(getSource(GET, ANIMALS_URL + "/" + id, null));
    }
    
    /**
     * Affiche un animal nommé "nom"
     */
    public void get_animal_with_name(String name) {
    	printSource(getSource(GET, FIND_BY_NAME_URL + name, null));
    }
    
    /**
     * Affiche l'animal à la position indiquée 
     */
    public void get_animal_at_position(String position) {
    	printSource(getSource(GET, FIND_AT_POS_URL + position, null));
    }
    
    /**
     * Affiche les animaux proches de la position indiquée 
     */
    public void get_animals_near_position(String position) {
    	printSource(getSource(GET, FIND_NEAR_POS_URL + position, null));
    }
    
    /**
     * Affiche les informations de l'animal identifié par "id" en interrongeant l'API Wolfram
     */
    public void get_animal_informations(UUID id) {
    	printSource(getSource(GET, ANIMALS_URL + "/" + id + WOLF_URL, null));
    }
    
    /**
     * Affiche les informations GPS du trajet entre la position indiquée et le centre en interrongeant l'API GraphHopper 
     */
    public void get_route_informations(String position) {
    	printSource(getSource(GET, ROUTE_URL + position, null));
    }
    
    
    /**
     * Créer et afficher l'animal identifié par "id"
     */
    public void add_animal_with_id(Animal animal, UUID id) throws JAXBException {
        printSource(getSource(POST, ANIMALS_URL + "/" + id, new JAXBSource(jc, animal)));
    }
    
    /**
     * Modifier et afficher l'animal identifié par "id"
     */
    public void edit_animal_with_id(Animal animal, UUID id) throws JAXBException {
    	printSource(getSource(PUT, ANIMALS_URL + "/" + id, new JAXBSource(jc, animal)));
    }
    
    /**
     * Modifier et afficher tous les animaux 
     */
    public void edit_all_animals(Animal animal) throws JAXBException {
    	printSource(getSource(PUT, ANIMALS_URL, new JAXBSource(jc, animal)));
    }
    
    /**
     * Supprimer l'animal identifié par "id"
     */
    public void delete_animal_with_id(UUID id) throws JAXBException {
    	printSource(getSource(DELETE, ANIMALS_URL + "/" + id, null));
    }

    
    /**
     * Supprimer tous les animaux
     */
    public void delete_all_animals() throws JAXBException {
    	printSource(getSource(DELETE, ANIMALS_URL, null));
    }
    
    /**
     * Ajouter une case
     */
    public void add_cage(Cage cage) throws JAXBException {
    	printSource(getSource(POST, CAGES_URL, new JAXBSource(jc, cage)));
    }
    
    /**
     * Modifier une cage par son nom
     */
    public void edit_cage_with_name(Cage cage, String name) throws JAXBException {
    	printSource(getSource(PUT, CAGES_URL + "/" + name, new JAXBSource(jc, cage)));
    }
    
    /**
     * Modifier toutes les cages
     */
    public void edit_all_cages(Cage cage) throws JAXBException {
        printSource(getSource(PUT, CAGES_URL, new JAXBSource(jc, cage)));
    }
    
    /**
     * Vider la cage identifiée par son nom
     */
    public void clear_cage_with_name(String name) throws JAXBException {
    	printSource(getSource(DELETE, CAGES_CLEAR_URL + name, null));
    }
    
    /**
     * Supprimer toutes les cages
     */
    public void delete_all_cages() throws JAXBException {
        printSource(getSource(DELETE, CAGES_URL, null));
    }
    
    
    private Source getSource(String method, String url, Source body) {
    	// Initialisation du service
    	service = Service.create(qname);
        service.addPort(qname, HTTPBinding.HTTP_BINDING, url);
        
        // Création du dispatcher
        Dispatch<Source> dispatcher = service.createDispatch(qname, Source.class, Service.Mode.MESSAGE);
        Map<String, Object> requestContext = dispatcher.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, method);

        // Retourne le résultat de la requête
        return dispatcher.invoke(body);
    }

    public void printSource(Source s) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(s, new StreamResult(System.out));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public static void main(String args[]) throws Exception {
        MyClient client = new MyClient();
        // Scénario de test
    
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
        
        System.out.println("<!-- Supprimez tous les animaux -->");
        client.delete_all_animals();
        System.out.println();
        
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Panda à Rouen (Latitude : 49.443889 ; Longitude : 1.103333) -->");
        client.add_cage(new Cage("Rouen", new Position(49.443889, 1.103333), 50, new ArrayList<Animal>()));
        client.add_animal(new Animal("Sunner", "Rouen", "Panda", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Hocco unicorne à Paris (Latitude : 48.856578 ; Longitude : 2.351828) -->");
        client.add_cage(new Cage("Paris", new Position(48.856578, 2.351828), 30, new ArrayList<Animal>()));
        client.add_animal(new Animal("Hocco", "Paris", "Hocco unicorne", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
        
        System.out.println("<!-- Modifiez l'ensemble des animaux par un Lagotriche à queue jaune à Rouen (Latitude :49.443889 ; Longitude : 1.103333) -->");
        client.edit_all_animals(new Animal("Johnsson", "Rouen", "Lagotriche à queue jaune", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
        
        System.out.println("<!-- Ajoutez une Océanite de Matsudaira en Somalie (Latitude : 2.333333 ; Longitude : 48.85) -->");
        client.add_cage(new Cage("Somalie", new Position(2.333333, 48.85), 20, new ArrayList<Animal>()));
        client.add_animal(new Animal("Matsu", "Somalie", "Océanite de Matsudaira", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Ara de Spix à Rouen (Latitude : 49.443889 ; Longitude : 1.103333) -->");
        UUID araId = UUID.randomUUID();
        client.add_animal(new Animal("AraSpix", "Rouen", "Ara de Spix", araId));
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Galago de Rondo à Bihorel (Latitude : 49.455278 ; Longitude : 1.116944) -->");
        client.add_cage(new Cage("Bihorel", new Position(49.455278, 1.116944), 10, new ArrayList<Animal>()));
        UUID galagoId = UUID.randomUUID();
        client.add_animal(new Animal("Walfreid", "Bihorel", "Galago de Rondo", galagoId));
        System.out.println();
        
        System.out.println("<!-- Ajoutez une Palette des Sulu à Londres (Latitude : 51.504872 ; Longitude : -0.07857) -->");
        client.add_cage(new Cage("Londres", new Position(51.504872, -0.07857), 40, new ArrayList<Animal>()));
        client.add_animal(new Animal("Sulu", "Londres", "Palette des Sulu", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Kouprey à Paris (Latitude : 48.856578 ; Longitude : 2.351828) -->");
        client.add_animal(new Animal("Kourtney", "Paris", "Kouprey", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Tuit-tuit à Paris (Latitude : 48.856578 ; Longitude : 2.351828) -->");
        client.add_animal(new Animal("Tuitu", "Paris", "Tuit-tuit", UUID.randomUUID()));
        System.out.println();
   
        System.out.println("<!-- Ajoutez une Saïga au Canada (Latitude : 43.2 ; Longitude : -19.4435386) -->");
        client.add_cage(new Cage("Canada", new Position(43.2, -19.4435386), 60, new ArrayList<Animal>()));
        UUID saigaId = UUID.randomUUID();
        client.add_animal(new Animal("Saîgo", "Canada", "Saïga", saigaId));
        System.out.println();
        
        System.out.println("<!-- Ajoutez une Gazelle de Mhorr au Brésil (Latitude : -53.4715856 ; Longitude : -19.4435386) -->");
        client.add_cage(new Cage("Rouen", new Position(-53.4715856, -19.4435386), 50, new ArrayList<Animal>()));
        client.add_animal(new Animal("Gazolla", "Brésil", "Gazelle de Mhorr", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Inca de Bonaparte à Porto-Vecchio (Latitude : 41.5895241 ; Longitude : 9.2627) -->");
        client.add_cage(new Cage("PortoVecchio", new Position(41.5895241, 9.2627), 20, new ArrayList<Animal>()));
        client.add_animal(new Animal("Innico", "Porto-Vecchio", "Inca de Bonaparte", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Râle de Zapata à Montreux (Latitude : 46.4307133; Longitude : 6.9113575) -->");
        client.add_cage(new Cage("Montreux", new Position(46.4307133, 6.9113575), 20, new ArrayList<Animal>()));
        client.add_animal(new Animal("Zapata", "Montreux", "Râle de Zapata", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Ajoutez un Rhinocéros de Java à Villers-Bocage (Latitude : 50.0218 ; Longitude : 2.3261) -->");
        client.add_cage(new Cage("VillersBocage", new Position(50.0218, 2.3261), 10, new ArrayList<Animal>()));
        client.add_animal(new Animal("Maral", "Villers-Bocage", "Rhinocéros de Java", UUID.randomUUID()));
        System.out.println();
        
        System.out.println("<!-- Ajoutez 101 Dalmatiens dans une cage aux USA -->");
        for (int i = 1; i <= 100; i++) {
        	client.add_animal(new Animal("Dalmatien" + i, "usa", "Dalmatien", UUID.randomUUID()));
        }
        System.out.println();
        
        
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
        
        System.out.println("<!-- Supprimez tous les animaux de Paris -->");
        client.clear_cage_with_name("Paris");
        System.out.println();
        
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
        
        System.out.println("<!-- Recherchez le Galago de Rondo -->");
        client.get_animal_with_id(galagoId);
        System.out.println();
        
        System.out.println("<!-- Supprimez le Galago de Rondo -->");
        client.delete_animal_with_id(galagoId);
        System.out.println();
        
        System.out.println("<!-- Supprimez à nouveau le Galago de Rondo -->");
        client.delete_animal_with_id(galagoId);
        System.out.println();
        
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
        
        System.out.println("<!-- Affichez les animaux situés près de Rouen -->");
        client.get_animals_near_position("lat=49.3&lng=1.2");
        System.out.println();

        System.out.println("<!-- Affichez un animaux à Rouen -->");
        client.get_animal_at_position("lat=49.443889&lng=1.103333");
        System.out.println();
        
        System.out.println("<!-- Affichez les informations Wolfram Alpha du Saïga -->");
        client.get_animal_informations(saigaId);
        System.out.println();
       
        System.out.println("<!-- Affichez les informations Wolfram Alpha de l'Ara de Spix -->");
        client.get_animal_informations(araId);
        System.out.println();
       
        System.out.println("<!-- Affichez le trajet de Somalie jusqu'au centre -->");
        client.get_route_informations("lat=2.333333&lng=48.85");
        System.out.println();
        
        System.out.println("<!-- Affichez le trajet de Londres jusqu'au centre -->");
        client.get_route_informations("lat=51.504872&lng=0.07857");
        System.out.println();
        
        System.out.println("<!-- Supprimez tous les animaux -->");
        client.delete_all_animals();
        System.out.println();
        
        System.out.println("<!-- Affichez l'ensemble des animaux -->");
        client.getAll_animals();
        System.out.println();
                
    }
}
