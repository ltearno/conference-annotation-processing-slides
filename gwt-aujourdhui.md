% GWT Aujourd'hui...
%
% BeZend 2015 - Université de Picardie - LTE Consulting

## Arnaud Tournier

**ArchiDév** passionné chez **LTE Consulting**

Speaker **Devoxx**, **GWT.create**, **Paris**/**Toulouse JUG**, etc...

@ltearno www.lteconsulting.fr

**Full stack** !

## Qu'est-ce que GWT ?

<table>
<tr>
<td style="vertical-align:top;">
<img style="border:none;box-shadow:none;" src="navLogoBig.png" alt="">
</td>
<td>
- Un outil de développement pour le Web
- Coder en **Java**
- Améliorer la **productivité** du développeur
- Offir un maximum de **performances** aux utilisateurs
</td>
</tr>
</table>

## Contenu

- Compilateur Java vers Javascript très efficace,
- Emulation d'une partie de la JCL,
- Gestion automatique des navigateurs,
- Des outils de production pour le web (resources, i18n, code splitting...),
- Un environnement de développement puissant.

## Histoire

- Ecrit par Bruce Johnson et open sourcé en 2006 par Google
- Piloté par un comité mixte (RedHat, Vaadin, Sencha, ArcBees, Bizo, Google)
- Utilisé dans de nombreuses applications
- Taille de la communauté (réactivité)

## Concurrents

- Web : Typescript, Dart, DuoCode, TeaVM, CoffeeScript, ...
- Applications hybrides : React Native, JUniversal, Xamarin, ...

## Utilisation

- Applications Web (Google Spreadsheet, AdWords, ...)
- Applications hybrides (Google Inbox, ...)
- Applications métier
- Ecriture de bibliothèques Javascript

- Une certaine courbe d'apprentissage
- Majorité des applis gwt font 20+ écrans et sont responsive

##

<img src="utilisationTypique.png" style="box-shadow:none; border:none;"/>

##

<img src="utilisationHybride.png" style="box-shadow:none; border:none;"/>

## Avantages du Java

- typage (pour les gros projets)
- ecosystème riche
- outillage
- communauté très active
- compétences disponibles
    
## Coeur

- compilateur Java vers Javascript
- permutations et DeferredBinding
- JsInterop / JSNI
- code splitting
- compilation incrémentale
- intégration SourceMaps

## Périphérie

- Widgets
- RPC
- UiBinder
- I18N
- CssResource

## Optimisations

- Permutations,
- ImageResources,
- CodeSplitting,
- DeferredBinding,
- Code pruning et autres

## Permutations

Chaque navigateur reçoit une implémentation différente

## Images

- Le spriting est réalisée automatiquement
- Latence réseau divisée

##

<img src="images.png" style="box-shadow:none; border:none;"/>

## Code splitting

L'application charge son code au fur et à mesure des besoins

##

<img src="codeSplitting.png" style="box-shadow:none; border:none;"/>

## Deferred Binding

Mécanisme permettant de sélectionner ou générer du code en fonction de variables résolues à la compilation.

##

<img src="deferredBinding.png" style="box-shadow:none; border:none;"/>

## Optimisations du code

        Shape shape = new Square(2);
        int area = shape.getArea();
        
devient

        int area = 4;

<!-- Et maintenant, la partie intéractive de la présentation -->

# Environnement de développement

GWT est fourni sous la forme d'un SDK (fichier jar), ou bien d'artefacts Maven. Il existe aussi un plugin Gradle.

La plupart des IDE Java prennent GWT en charge (Eclipse, IntelliJ, ...)

Deux modes de compilation existent : développement et production.

## Structure d'un projet

On part de :

- une page "hôte"
- des sources Java
- les resources de l'application (images, css, ...)

## Compilation

Le compilateur produit :

- un fichier Javascript pour chaque permutation,
- des fichiers de resources "cachables".

##

<img src="structure.png" style="box-shadow:none; border:none;"/>

## Débuggage

Le débugage des applications est possible dans l'**IDE** ou directement dans **Chrome Dev Tools**.

Le **SuperDevMode** est le compilateur incrémental dédié à la phase de développement.

Génère le Javascript et les informations **SourceMaps** pour le debug dans l'IDE ou dans Chrome directement en Java.

##

<img src="debug.png" style="box-shadow:none; border:none;"/>

<!--

Démonstration :
Projet maven avec gwt, hexacss et sass
SplitLayoutPanel
avec du CSS HexaCss
et le changeur de thème

DataBinding entre une liste et un formulaire

Une classe DTO,
une table

-->

## Quelques bibliothèques utiles avec GWT (PUB!)

- HexaTools (www.github.com/ltearno/hexatools)
    - HexaCss : intégration framework CSS
    - DataBinding
    - RPC rapide multi back-end (Java, Php, ...)
    - JPA 4 Gwt

# Demo !

## Questions ?

# Merci !!!

Twitter: @ltearno

http://www.lteconsulting.fr

http://blog.lteconsulting.fr