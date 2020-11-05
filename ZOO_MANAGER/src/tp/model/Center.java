package tp.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Center {
    
	Collection<Cage> cages;
    Position position;
    String name;

    public Center() {
        cages = new LinkedList<>();
    }

    public Center(Collection<Cage> cages, Position position, String name) {
        this.cages = cages;
        this.position = position;
        this.name = name;
    }

    public Animal findAnimalById(UUID uuid) throws AnimalNotFoundException {
        return this.cages.stream()
                .map(Cage::getResidents)
                .flatMap(Collection::stream)
                .filter(animal -> uuid.equals(animal.getId()))
                .findFirst()
                .orElseThrow(() -> new AnimalNotFoundException("Animal non trouvé"));
    }

    public Collection<Cage> getCages() {
        return cages;
    }

    public Position getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public void setCages(Collection<Cage> cages) {
        this.cages = cages;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
   /*
    * Crée un nouvel animal dont l'identifiant est précisé
    */
    
    
	public Animal createAnimalById(Animal animal, UUID fromString) throws CenterException {
		// TODO Auto-generated method stub
		// Création du nouvel animal avec l'UUID passé en paramétres
    	Animal createdAnimal = new Animal(animal.getName(), animal.getCage(), animal.getSpecies(), fromString);
    	
    	// Ajout de l'animal dans sa cage
    	addAnimal(createdAnimal);
    	
    	return createdAnimal;
	}
	/*
	 * Modifier l'animal dont l'id est donné en paramètre avec les données de l'animal mis en parametre
	 */
	public Animal modifyAnimalById(Animal modifiedAnimal, UUID uuid) throws AnimalNotFoundException {
    	// Récupérer l'animal à modifier
    	Animal animal = findAnimalById(uuid);
    	
    	// Modifie les caractéristiques de l'animal par les caractéristiques de l'animal passé en paramètres
    	animal.setName(modifiedAnimal.getName());
    	animal.setCage(modifiedAnimal.getCage());
    	animal.setSpecies(modifiedAnimal.getSpecies());
    	
    	return animal;
    }
	/*
	 * Supprime l'animal dont le l'id est indiqué en paramètre
	 */
	public Center deleteAnimalById(UUID uuid) throws CenterException {
		// TODO Auto-generated method stub
		Animal animal = findAnimalById(uuid);
		
		findCageByName(animal.getCage()).getResidents().remove(animal);
		
		return this;
	}
	/*
	 * Ajoute l'animal indiqué en paramètre dans le centre
	 */
	public Cage addAnimal(Animal animal) throws CenterException {
    	// Récupérer la cage de l'animal
    	Cage cage = findCageByName(animal.getCage());
		
    	// Vérifier si la cage n'est pas déjà remplie
    	if (cage.getCapacity() <= cage.getResidents().size()) {
    		throw new CenterException("La taille maximum de la cage" + cage.getName () + " est atteinte");
    	}
    	
    	// Ajouter l'animal dans sa cage
    	cage.getResidents().add(animal);
    	
    	return cage;
    }
	/*
	 * Recherche la cage dont le nom est indiqué en paramètre dans le centre
	 */
	public Cage findCageByName(String cage) throws CageNotFoundException {
		// TODO Auto-generated method stub
		return this.cages.stream()
                .filter(c -> cage.equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElseThrow(() -> new CageNotFoundException("Cage non trouvée"));
	}
	/*
	 * Modifie tous animaux du centre en modifiant 
	 */
	public Center modifyAllAnimals(Animal animal) throws CageNotFoundException {
    	// Récupérer la cage de l'animal source 
    	Cage cage = findCageByName(animal.getCage());

    	Collection<Animal> animals = new LinkedList<Animal>();
    	
    	// Récupérer tous les animaux
    	cages.forEach(c -> animals.addAll(c.getResidents()));
    	
    	// Supprimer toutes les cages
    	deleteAllAnimals();
    	
    	// Ajouter tous les animaux dans la cage de l'animal par défaut
    	cage.setResidents(animals);
    	
    	// Modifier les caractéristiques de l'ensemble des animaux
    	cage.getResidents().forEach(a -> {
    		a.setName(animal.getName());
    		a.setCage(animal.getCage());
    		a.setSpecies(animal.getSpecies());
    	});
        
    	return this;
    }
	/*
	 * Supprime tous les animaux du centre
	 */
	public Center deleteAllAnimals() {
		// TODO Auto-generated method stub
		cages.forEach(c ->{ c.getResidents().clear();} );
		return this;
	}
	/*
	 * Recherche l'animal dans le centre dont le nom est indiqué en paramètre
	 */
	public Animal findAnimalByName(String name) throws CenterException {
		// TODO Auto-generated method stub
		return this.cages.stream()
                .map(Cage::getResidents)
                .flatMap(Collection::stream)
                .filter(animal -> name.equalsIgnoreCase(animal.getName()))
                .findFirst()
                .orElseThrow(() -> new AnimalNotFoundException("Animal non trouvé"));
	}
	/*
	 * Retourne un animal au hasard de la cage dont la position est indiquée en paramètre
	 */
	public Animal findAnimalByPosition(Position position) throws CenterException {
		// TODO Auto-generated method stub
		
		return cages.stream()
    	        .filter(cage -> position.equals(cage.getPosition()))
    	        .map(Cage::getResidents)
    	        .flatMap(Collection::stream)
    	        .findAny()
                .orElseThrow(() -> new CageNotFoundException("Aucun animal trouvé à la position : " + position));
	}
	/*
	 * Retourne tous les animaux de la cage dont la position est indiquée en paramètre
	 */
	public Cage findAnimalsByPosition(Position position) throws CenterException {
		// TODO Auto-generated method stub
		
		return cages.stream()
    	        .filter(cage -> position.equals(cage.getPosition()))
    	        .findFirst()
    	        .orElseThrow(() -> new CageNotFoundException("Aucun animal trouvé à la position : " + position));
	}
	/*
	 * Recherche tous les animaux proche de cette position de 100km
	 */
	public Cage findAnimalsNearPosition(Position position) throws CenterException {
		// TODO Auto-generated method stub
		Collection<Animal> animals = cages.stream()
    	        .filter(cage -> cage.getPosition().isNear(position))
    	        .map(Cage::getResidents)
    	        .flatMap(Collection<Animal>::stream)
                .collect(Collectors.toList());
		Cage cage = new Cage();
		cage.setResidents(animals);
		return cage;
	}
	
	/*
	 * Ajoute la cage donnée en paramètre dans le centre
	 */
	public Center addCage(Cage cage) throws CenterException {
    	try {
    		//Vérifier l'existence de cette cage en cherchant dans le centre une cage avec le même nom
			Cage c = findCageByName(cage.getName());
			//Ligne Exécutée uniquement si la cage existe déja
			throw new CenterException("La cage " + c.getName() + " existe déjà");
		} catch (CageNotFoundException e) {		
	    	//Bloc éxécuter si la cage n'existe pas dans le centre
	    	cages.add(cage);
		}

    	return this;
    }
	
	/*
	 * Supprime la cage dont le nom est donné en paramètre
	 */
	public Center deleteCageByName(String name) throws CageNotFoundException {
    	// Récupérer la cage à supprimer
    	Cage cage = findCageByName(name);
    	
    	// Supprimer la cage du centre
    	cages.remove(cage);
    	
    	return this;
    }
	
	/*
	 * Vide la cage dont le nom est donné en paramètre
	 */
	public Cage clearCageByName(String name) throws CageNotFoundException {
    	// Récupérer la cage à supprimer
    	Cage cage = findCageByName(name);
    	
    	// Supprimer les animaux de la cage du centre
    	cage.getResidents().clear();
    	
    	return cage;
    }
    /*
     * Supprime toutes les cages du centre
     */
    public Center deleteAllCages() {
    	cages.clear();
    	
    	return this;
    }
    /*
     * Permet de modifier toutes les cages du centre
     * Chaque cage du centre aura la même position et la même capacité que la cage générée donnée en paramètre
     */
    public Center modifyAllCages(Cage cage) {
    	
    	cages.forEach(c -> {
    		c.setPosition(cage.getPosition());
    		c.setCapacity(cage.getCapacity());
    	});
    	
    	return this;
    }
    
    /*
     * Permet de modifer la cage du centre dont le nom est donné en paramètre avec les valeurs de la cage donnée en 
     * paramètre
     */
    public Cage modifyCageByName(Cage modifiedCage, String cageName) throws CageNotFoundException {
    	//Récuperer la cage à éditer
    	
    	Cage cage = findCageByName(cageName);
    	
    	// Modifier la cage dans le centre avec les valeurs de la nouvelle cage
    	cage.setCapacity(modifiedCage.getCapacity());
    	cage.setPosition(modifiedCage.getPosition());
    	cage.setResidents(modifiedCage.getResidents());
    	
    	return cage;
    }
}
