# ZOO MANAGER (RESTFUL API)

**Zoo Manager** est une application de gestion d'un zoo


## Configuration

1. Démarrez la classe [MyServiceTP](ZOO_MANAGER/src/tp/rest/MyServiceTP.java) qui le serveur
1. Démarrez la classe [MyClient](ZOO_MANAGER/src/tp/rest/MyClient.java) qui représente le client définissant le scénario

Le code source du projet est disponible [ici](ZOO_MANAGER).


### Exemples de requête URL à lancer sur le navigateur après avoir démarrer le serveur

- **Afficher tous les animaux du centre :**
```
http://localhost:8084/animals
```

- **Rechercher un animal par son nom :**
```
http://localhost:8084/find/byName/Tic
```

- **Afficher la cage Amazon :**
```
http://localhost:8084/cages/amazon
```

- **Rechercher un animal dans la cage usa :**
```
http://localhost:8084/find/at/lat=49.305&lng=1.2157357
```

- **Obtenez l'animal identifié par un UUID spécifique: :**
```
http://localhost:8084/animals/4c512e3e-2282-442b-b460-48613d62ac38

```

- **Obtenez des informations sur l'itinéraire de Rouen au centre: :**
```
http://localhost:8084/center/journey/from/lat=49.443889&lng=1.103333

```

- **Recherche d'animaux à proximité de rouen (Dans un rayon de 100Km): :**
```
http://localhost:8084/find/near/lat=49.443889&lng=1.103333

```

- **Rechercher les animaux près de usa :**
```
http://localhost:8084/find/near/lat=49.3&lng=1.2
```


## Outils utilisés

- [Java 8](https://www.java.com) &mdash; Langage de prommation de base
- [JAX-WS](https://en.wikipedia.org/wiki/Java_API_for_XML_Web_Services) &mdash; Spécification qui permet de faire correspondre un document XML à un ensemble de classes
- [Geonames](http://www.geonames.org/) &mdash; Service Web permettant de connaitre le nom d'un lieu dont la position géographique est communiqué
- [Wolfram|Alpha](https://www.wolframalpha.com/) &mdash; Service Web semblable à un moteur de recherche
- [GraphHopper](https://graphhopper.com) &mdash; Service Web pour connaitre les informations sur un trajet


## Contributeurs

L'auteur de ce projet peut être trouvé ici [**AUTEURS.MD**](/AUTEURS.md).


