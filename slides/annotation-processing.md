% Pluggable Annotation Processing API
%
% JUG Toulouse - 2015 - LTE Consulting

### Arnaud Tournier

**ArchiDév** passionné chez **LTE Consulting**

Speaker **Devoxx**, **GWT.create**, **Paris**/**Toulouse JUG**, etc...

@ltearno www.lteconsulting.fr

Développement - Formation - Conseil

**Full stack (x86_64 to JavaScript)** !

### Avant tout

Cette présentation est disponible sur [github.com/ltearno/annotation-processing](https://github.com/ltearno/annotation-processing)

Des hyper-liens sont présents

## JSR 269 ???

## Pluggable Annotation Processing API...
###

La JSR 269 - Pluggable Annotation Processing API permet d'exploiter les annotations présentes dans le code en s'insérant dans le processus de compilation, et de générer de nouvelles sources.

- Traitement des annotations à la compilation,
- Génération de code,
- Génération de fichiers de configuration,
- Documentation, Cartographie,
- Vérifications, Build breakers,
- etc...

On ne modifie pas les sources existants !

## Avantages
###

Le code généré est visible.

Pas de traitement au runtime donc pas d'impact sur les performances.

Pas d'instrumentation du byte-code, donc plus simple.

## Brève histoire du traitement des annotations

### Commentaires Javadoc

XDoclet (2002)

    /****
    * Account entity bean
    *
    * @ejb.bean
    *     name="bank/Account"
    *     jndi-name="ejb/bank/Account"
    *     primkey-field="id"
    *     schema = "Customers"
    * ...
    */
    public class MonBean { ... }

### APT

[Annotation Processing Tool](http://docs.oracle.com/javase/7/docs/technotes/guides/apt/), retiré officiellement avec Java 7 car [non extensible à Java > 5](http://openjdk.java.net/jeps/117).

Outil lancé en dehors de la compilation.

L'API Mirror utilise les packages `com.sun.mirror`.

### Pluggable Annotation Processing API

Depuis 2006 (Java 6) la [JSR-269](https://jcp.org/aboutJava/communityprocess/final/jsr269/index.html), créé par Joe Darcy.

Intégré à la compilation `javac`.

## Principe
###

- On fournit un processeur d'annotation,
- Le compilateur gère des `rounds` de processing,
- A chaque round, les nouveaux sources sont traités (phases *Parse* et *Enter*),
- Les processeurs sont choisis et reçoivent l'AST des classes traitées,
- Les processeurs peuvent générer de nouveaux fichiers (*sources*, *classes* et *resources*) qui seront parsés et traités au `round` suivant.

## Utilisation

### Création de l'annotation

    import java.lang.annotation.*;

    // package, class, method, ...
    @Target( value = { ElementType.METHOD } )
    @Retention( RetentionPolicy.SOURCE )
    public @interface MonAnnotation
    {
        ...
    }

### Le processeur

    @SupportedAnnotationTypes( value= { "fr.lteconsulting.MonAnnotation" } )
    @SupportedSourceVersion( SourceVersion.RELEASE_6 )
    public class MonAnnotationProcessor extends AbstractProcessor  {
        @Override
        public boolean process( Set<?> extends TypeElement> annotations,
                                RoundEnvironment roundEnv ) {
            Types typeUtils = processingEnv.getTypeUtils();
            Elements elementUtils = processingEnv.getElementUtils();
            Messager messager = processingEnv.getMessager();

            for ( TypeElement element : roundEnv.getElementsAnnotatedWith(MonAnnotation.class) )
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE,
                    element.getQualifiedName());
            return true;
        }
    }

### Le fichier SPI

Pour packager et activer un processeur, le plus simple est d'utiliser SPI :

Le fichier `META-INF/services/javax.annotation.processing.Processor` contient la liste des processeurs :

    fr.lteconsulting.MyAnnotationProcessor

**Packager le tout dans un jar**

### Un bout de code

Dans un autre projet on peut utiliser l'annotation et le processeur.

    class UneClasse {
        @MonAnnotation
        void uneMethode()
        {
            ...
        }
    }

### Packaging et compilation

Le processeur et le fichier SPI sont dans un jar.

Ce jar est dans le class path au moment de la compilation de `UneClasse.java`.

C'est tout !

### Sortie de notre exemple

    At line 90 of UneClasse.java :
	fr.lteconsulting.UneClasse.uneMethode()

## Fonctionnement
###

A chaque round, le processeur doit traiter les classes générées au round précédent.

S'il est appelé au premier round, il le sera pour les autres, jusqu'au dernier round (même si aucune annotation n'est présente pour lui).

### Découverte des processeurs

Les proceseurs sont découverts par le compilateur. `JavaCompiler` fournit des options pour contrôler l'ensemble des processeurs disponibles :

- une liste prédéfinie,
- un chemin de recherche,
- utiliser SPI.

### Choix du processeur

Appel des processeurs en fonction :

- des annotations présentes dans les classes traitées,
- les annotations supportées par les processeurs,
- le fait qu'un processeur ait *claimé* une annotation.

### Cycle de vie du processeur

- Le compilateur instancie le processeur,
- Appelle `init` avec un `ProcessingEnvironment`,
- Appelle `getSupportedAnnotationTypes`, `getSupportedOptions` et `getSupportedSourceVersion`,
- Et appelle `process` à chaque round.

### A chaque round

- `javac` calcule l'ensemble des annotations sur les classes en cours,
- si au moins une annotation est présente, au fur et à mesure que les processeurs les *claime*, elles sont retirées des annotations non *matchées*.
- quand l'ensemble est vide ou qu'il n'y a plus de processeur candidat, le round est fini.
- si aucune annotation n'est présente, seuls les processeurs *universels* ("*") sont appelés, et reçoivent un ensemble vide de classes à traiter.

### Précautions !

- Un processeur ne doit pas dépendre d'un autre,
- Idempotent,
- Commutatif.

### L'interface Processor

[`javax.annotation.processing.Processor`](http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/Processor.html)

```java
interface Processor {
	void init( ProcessingEnvironment processingEnv );

	Set<String> getSupportedAnnotationTypes();

	boolean process(    Set<? extends TypeElement> annotations,
						RoundEnvironment roundEnv );
}
```

### ProcessingEnvironment

[`javax.annotation.processing.ProcessingEnvironment`](http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/ProcessingEnvironment.html)

    // Utilitaires
    Elements getElementUtils();
    Types getTypeUtils();
    Locale getLocale();
    Map<String, String> getOptions();
    SourceVersion getSourceVersion();
    
    // Création de fichiers
    Filer getFiler();

    // Affichage utilisateur
    Messager getMessager();
	
	// Autres classes :
	// ElementFilter, AbstractVisitors...

### getSupportedAnnotationTypes

- `*`
- `fr.lteconsulting.annotations.*`
- `fr.lteconsulting.annotations.MonAnnotation`

### La méthode process

    boolean process(    Set<? extends TypeElement> annotations,
                        RoundEnvironment roundEnv)

- On reçoit l'ensemble des annotations à traiter,
- On retourne `true` pour *claimer* les annotations et empêcher les autres processeurs de les traiter.

### RoundEnvironment

[`javax.annotation.processing.RoundEnvironment`](http://docs.oracle.com/javase/7/docs/api/javax/annotation/processing/RoundEnvironment.html)

Liste des classes dans le round :

    Set<? extends Element> getRootElements()

Liste des éléments annotés :

    Set<? extends Element> getElementsAnnotatedWith(TypeElement a)
    Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a)

### Element

[javax.lang.model.element.Element](http://docs.oracle.com/javase/7/docs/api/javax/lang/model/element/Element.html)

Représente un *package*, une *classe*, une *méthode*, ...

Pour parcourir les données d'un élément, il faut soit appeler `getKind()` soit utiliser un visiteur.

**Ne pas utiliser `instanceof` !**

*Petit exercice : **POURQUOI ?** *

### Element

    // Visiter l'élément :
    <R,P> R accept(ElementVisitor<R,P> v, P p);

    // Obtenir le type :
    TypeMirror asType();
    
    // Demander la sorte :
    ElementKind getKind();

    // Demander les annotations présentes sur l'élément :
    List<? extends AnnotationMirror> getAnnotationMirrors();
    <A extends Annotation> A getAnnotation(Class<A> annotationType);
    
    // Autres :
    // getModifiers(); getSimpleName(); getEnclosingElement(); getEnclosedElements();

### Les sortes d'Element

`ElementKind`

*annotation, class, constructeur, enum, une constante enum, parametre d'exception, champ, initializeur d'instance, interface, variable locale, méthode, package, paramètre, variable de resource, initializeur statique, paramètre de type, autres* (futur).

<!---
### Récupérer un type

    TypeMirror serializable = processingEnv.getElementUtils().getTypeElement(Serializable.class.getCanonicalName()).asType();
--->

### Le Filer

[`javax.annotation.processing.Filer`](http://docs.oracle.com/javase/6/docs/api/javax/annotation/processing/Filer.html)

### Créer un nouveau source java

    Filer filer = processingEnv.getFiler();
    JavaFileObject jfo = filer.createSourceFile(
         classElement.getQualifiedName() + "Info");
    PrintWriter pw = new PrintWriter( jfo );
    ...

### Créer une nouvelle ressource

    Filer filer = processingEnv.getFiler();
    try {
        PrintWriter pw = new PrintWriter(filer.createResource(
              StandardLocation.SOURCE_OUTPUT, "fr.lteconsulting", "Todo.txt")
              .openOutputStream());
        pw.println("Quelque chose");
        pw.close();
    } catch (IOException ioe) {
        messager.printMessage(Kind.ERROR, ioe.getMessage());
    }

### Créer un nouveau fichier .class

    Filer filer = processingEnv.getFiler();
    JavaFileObject jfo = filer.createClassFile(
         classElement.getQualifiedName() + "Info");
    PrintWriter pw = new PrintWriter( jfo );
    ...

### Utilisation de templates !

- Ne générer que le minimum de code !
- Velocity, ...
- Java Poet, ...

### Le Messager

```java
messager.printMessage( Kind.ERROR, 
	"Cannot find an ID field !" );
```

```
// sortie :
error: At Book.java, line 12 : Cannot find an ID field !
1 error
```

### Le Messager dans Eclipse

![La même erreur dans Eclipse](error-in-eclipse.png)

## La compilation Java

### 3 phases

![Plan d'exécution de javac](javac-flow.png)

- *Parse* et *Enter*
- *Annotation Processing*
- *Analyse* et *Generate*

[openjdk.java.net/groups/compiler/doc/compilation-overview](http://openjdk.java.net/groups/compiler/doc/compilation-overview/index.html)

### JavaC

Action     |   Paramètres
-----------|--------
Génération des sources   | -s *répertoire*
Désigner un processeur |  -processor *fr.lteconsulting.MyAnnotationProcessor*,*autre...*
Spécifier un chemin de recherche   | -processorPath *le_chemin*
Passer des options  |  -A*cle=valeur*

### JavaC (suite)

Action     |   Paramètres
-----------|--------
Désactiver l'AP   |  -proc:none
Seulement l'AP    | -proc:only
Chemin de recherche des sources   |  -sourcePath
Désactiver la génération des `.class` implicite  | -implicit:none
Répertoire de sortie des `.class`  | -d
Affichage debug  |  -XprintRounds  -XprintProcessorInfo

*ATTENTION : le warning si on ne met pas `-implicit:none`*

[docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html#searching](http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html#searching)

## Intégration avec Eclipse

### Configuration

Eclipse utilise son propre compilateur, JDT.

*Configuration possible du projet par m2e*

<img src="eclipse-settings.png" style="height:40%;">

### ATTENTION

Dans Eclipse, si le projet contenant le processeur est ouvert, l'annotation processing est désactivé.


## Tests unitaires

### Exemple

```java
JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

// JDK Sun : task peut être casté en com.sun.source.util.JavacTask
CompilationTask task = compiler.getTask(
    null,                   // Default log (stderr)
    fileManager,            // (InMemory)JavaFileManager
    diagnosticCollector,    // DiagnosticCollector
    options,                // Set<String>
    new HashSet<String>(),  // Classes pour annotation processing
    sources);               // Iterable<? extends JavaFileObject>

// Force les processeurs
task.setProcessors(processors);

boolean successful = task.call();      // succès ou échec
diagnosticCollector.getDiagnostics();  // logs structurés de compilation
fileManager.getOutputFiles();          // fichiers de sortie
```

### [Compile-Testing](https://github.com/google/compile-testing)

Bibliothèque de test développée par Google, pour aider le développement de [Auto](https://github.com/google/auto/) et [Dagger](http://google.github.io/dagger/)

###

Test positif

```java
assert_().about(javaSource())
   .that(JavaFileObjects.forResource("HelloWorld.java"))
   .processedWith(new MyAnnotationProcessor())
   .compilesWithoutError()
   .and()
   .generatesSources(JavaFileObjects.forResource("GeneratedHelloWorld.java"));
```

###

et négatif

```java
JavaFileObject fileObject = JavaFileObjects.forResource("HelloWorld.java");
assert_().about(javaSource())
    .that(fileObject)
    .processedWith(new NoHelloWorld())
    .failsToCompile()
    .withErrorContaining("No types named HelloWorld!")
    .in(fileObject)
    .onLine(23)
    .atColumn(5);
```

## Démo Time !

Montrer le messager

Montrer la génération en dynamique


## Limitations
###

- Pas d'accès à l'AST complet (corps des méthodes)
- Pas possible de modifier des classes existantes
- Certains bug ne permettent pas de traiter correctement les génériques
- Java et les IDE ne surveillent que les éléments annotés pour redéclencher le processing (parfois problématique lorsque l'on a des dépendances complexes -> `Alt+F5` !)

### Hacking au delà

- [Lombok](https://projectlombok.org/) : de bons *hacks* pour accéder à l'implémentation (javac de sun et jdt) et modifier l'AST
- Immutables : quelques workarounds captant les implementations JDK / JDT pour gérer les génériques
- Explications techniques générales dans [The Hacker's guide to JavaC](http://scg.unibe.ch/archive/projects/Erni08b.pdf)

## Exemples de bibliothèques
###

- JPA meta-model generation (JSR-317),
- Dagger,
- Google Auto,
- Immutables,
- Hexa Binding,
- Lombok,
- GWT (RequestFactory)

## Liens en vrac
###

[Hibernate Validation](http://docs.jboss.org/hibernate/validator/5.1/reference/en-US/html/validator-annotation-processor.html), [JM Doudoux](http://jmdoudoux.developpez.com/cours/developpons/java/chap-annotations.php#annotations-8), [Lombok](https://projectlombok.org/), [How Lombok works ?](http://stackoverflow.com/questions/6107197/how-does-lombok-work), [Lombok encore...](http://notatube.blogspot.fr/2010/11/project-lombok-trick-explained.html), [Hacking JavaC](http://scg.unibe.ch/archive/projects/Erni08b.pdf), [HexaBinding](https://github.com/ltearno/hexa.tools/blob/master/hexa.binding/README.md), [Coders Breakfast](http://thecodersbreakfast.net/index.php?post/2009/07/09/Enforcing-design-rules-with-the-Pluggable-Annotation-Processor), [Prez d'Angelika Langer](http://www.angelikalanger.com/Conferences/Slides/JavaAnnotationProcessing-JSpring-2008.pdf), [Dr. Macphail's trance](https://deors.wordpress.com/2011/10/08/annotation-processors/), [Créez et utilisz vos annotations](https://openclassrooms.com/courses/java-et-les-annotations/creez-et-utilisez-vos-propres-annotations), [Histoire des annotation processing](https://docs.oracle.com/javase/7/docs/technotes/guides/apt/GettingStarted.html), [4 vidéos sur Parleys](https://www.parleys.com/search/croisier), [Utilisation de la JSR-269 chez Les Furets](https://www.google.fr/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0CCEQFjAAahUKEwi9gJu1gtPHAhVF6RQKHU9ZCPA&url=http%3A%2F%2Fwww.parisjug.org%2Fxwiki%2Fwiki%2Foldversion%2Fdownload%2FMeeting%2F20130212%2FpresentationDimitriBaeliGillesDiGuglielmo.pdf&ei=0x_kVf3KNcXSU8-yoYAP&usg=AFQjCNGgRSIrnU3yt0BMokotzqEC3aZ3VA&sig2=AKXWbGrIBdbw9N5SJhYr-w), [Save method parameter names](https://weblogs.java.net/blog/emcmanus/archive/2006/06/using_annotatio.html), [Au JUG Paris par Olivier Croisier...](http://fr.slideshare.net/Zenika/annotations-paris-jug), [Source Code Analysis Using Java 6 APIs](https://today.java.net/pub/a/today/2008/04/10/source-code-analysis-using-java-6-compiler-apis.html#accessing-the-abstract-syntax-tree-the-compiler-tree-api)


## Et voilà !
### Merci !

Rendez-vous sur

[github.com/ltearno/annotation-processing](https://github.com/ltearno/annotation-processing)

Twitter : `@ltearno`

LinkedIn : [fr.linkedin.com/in/lteconsulting](https://fr.linkedin.com/in/lteconsulting)

[LTE Consulting](http://www.lteconsulting.fr)






<!---
## email Eugene Lucas (Immutables)

Hi Eugene,

I am writing to you because you are the main committer of the immutables.io project.

I got a question related to annotation processing.

In the immutable docs, this sentence can be read :

    "Immutables do not support type parameters in the sense that you cannot add type variables to the abstract value type and construct parametrized instances."

As i am also writing a project using annotation processing to generate boiler plate code, i think i faced a similar problem :

When a generated type is referenced and parametrized, calling the TypeMirror.toString() method will return "<any>" instead of the fully qualified name of the refered type. It does not happen if the referenced type is not parametrized.

This behavior does not happen in Eclipse (where the JDT compiler is used). JDT always returns the correct result. This makes me think that the javac behavior is buggy.

So my first question is :

- is the immutables.io limitation due to that bug in the java compiler ?
- do you foresee any solution to mitigate this problem ? i find myself in a kind of despair because i cannot find a way to get the correct information from the compiler...

I've also contacted people from the Dagger project, which i think had similar problems (some comments in their code lead me to this idea)...

Thanks



Hi Arnaud

I really don't know if we (at Immutables) are gave up on full generic support, or just waiting the right time or tools, or just inspiration. So to the point, I believe it is possible to get the information we need during annotation processing, but it's complicated because of 2 factors:
1. Annotation processing api and utilities are not powerful enough to do proper type variable handling in all cases.
2. Both Javac's and Eclipse's annotation processing api implementations have various bugs in handling of generics.

A level of generic support should be also considered. If, for example, Immutables do not allow fully parametrized types, it does support extending/implementing types supplying actual type parameters. It was much easier to do this than full generic support, but still, it required to work around a number of Javac/Eclipse bugs. Full support would require to
1) Extract most precise information about declarations (which would not be distorted by bugs in mirror api implementation)
2) Resolve correct types, which might involve type variable substitution, upper and lower bounds, intersection types depending on what you need to support.

The 1) could be archived by decomposing DeclaredType and it's type arguments. In worst case it might require downcasting and extracting Eclipse and Javac specific ASTs to collect precise information. Such downcasting is heavily used by Lombok. Immutables uses it from time to time to overcome compiler bugs or limitations.

The 2) require some non-trivial computations, resembling ones in Guava's com.google.common.reflect.TypeResolver for reflection.

Hope this may help you. If you came up with some new information on this or have additional question feel free to share it or ask.

Thanks









## email question M. Gruber (Dagger)

Hi M. Gruber,

I am a developper and found your contact on github.

Currently writing a data binding library using annotation processing to generate boilerplate code, i found a problem that i think you also faced in the Dagger project.

When analysing existing type elements in classes, if a type element is a class generated during the annotation processing and parametrized with type parameters, then the toString() method of its TypeMirror returns <any> instead of the generated class name.

This does not happen when the generated class has no type parameter specified.

For example if this is written in a source file :

    class Blabla
    {
        GeneratedClass<Integer> field;
    }

the TypeMirror.toString() will return "<any>" for the field's type. But if this is written :

    class Blabla
    {
        GeneratedClass field;
    }

the same methods returns the correct result which is "GeneratedClass".



In the github repo of Dagger, in this file :

    https://github.com/square/dagger/blob/master/compiler/src/main/java/dagger/internal/codegen/Util.java

at line 165, i found

        // Paramterized types which don't exist are returned as an error type whose name is "<any>"
        if ("<any>".equals(errorType.toString())) {
          throw new CodeGenerationIncompleteException(
              "Type reported as <any> is likely a not-yet generated parameterized type.");
        }



So my question is : do you know a way to obtain the correct information for the type ? Maybe it's on purpose or a bug, i wanted to have your advice.

Just another info : inside Eclipse, that is compiling the java code with JDT, the thing works fine and gievs "GeneratedClass<Integer>".

So that seems to be a bug on javac side, what do you think ?

Thanks a lot for your answer !


-->
