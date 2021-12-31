# Générateur de fractale 

### Génération de l'excutable 

./gradlew jar

### Lancer l'executable post generation

$build/libs/fractale.jar.  
lancer l'executable une fois qu'il a etait genere.

$java -jar build/libs/fractale.jar "z * z - 0.7269 + 0.1889i" -1 1 -1 1 1001 1001 3 fractale1  
generer une fractable depuis le terminale. 


$java -jar build/libs/fractale.jar  
Lancer la version graphique.

### Les points forts de notre programme

##### L'interactivité 

Notre générateur de fractale est interactif (dans l'ordre du raisonnable), vous n'aurez pas a attendre plusieurs secondes aprés chaque actions (sauf si vous generez des fractables de tailles démesuré) Chaque mode détaillé ci dessous est plus ou moins interactif.
 
##### Les différents mode de génération lors du déplacements 

Il y a plusieurs mode lorsque l'on se déplace
* Le mode no flicking  
Dans ce mode, lors du deplacement on ne change ce qui est affiche a l'écran que lorsque la génération de la nouvelle fractale a était finalisé. N'as aucun probleme d'interactivite lors du déplacement.
  
 
* Le mode low res  
Dans ce mode, lors du deplacement on regenere une fractale de basse résolution afin d'avoir un mouvement fluide mais d'avoir une idée de le fractale une fois generee. Quelque soit le zoom ou le deplacement n'as aucun probleme d'interactivite.


* Le mode low res composite  
Dans ce mode, lors du deplacement on regenere une fractale de basse résolution afin d'avoir un mouvement fluide, la varation étant que l'on garde l'ancienne image de la fractale en bonne résolution, de ce fait lors du deplacement seul les partie de la fractale qui de base etaient en dehors de l'écran sont genere en basse resolution. Bonne interactivite.


* Le mode FillMissing
Dans ce mode, lors du deplacement on regenere les rectangles manquant, si l'on se decale de 50 pixels vers le bas, on ne regenerera que la partie de la fractale se trouvant dans le rectangle de 50 pixels de hauteur et de la largeur de la fractale dans la partie superieur de l'ecran. Si on ne fait pas de grand mouvement a une bonne interactivite.

##### Un parser de polynome 

Notre programme contient un parser nous permettant de rentrer des polynome directement depuis l'interface graphique (ou la ligne de commande), notre parser vas ensuite nous generer la fonction<Complex, Complex> correspondante a la String de la fonction entre den parametre.

##### Les courbes de Bezier

Dans nos themes de couleurs se trouvent les courbes de Bezier [Wiki Bezier](https://fr.wikipedia.org/wiki/Courbe_de_B%C3%A9zier), en soit ce ne sont que des fonctions simples de translation de couleur mais elles offrent des variantes de couleur vraiment sympa.

##### L'utilisation de 2 pools de thread

L'utilisation de 2 pools de Thread nous permet d'effectuer les calculs immédiat (exemple : calcul des rectangles en mode FillMissing). et les "gros calculs" (exemple : generation de la fractale a la bonne résolution qui sera affichée).

