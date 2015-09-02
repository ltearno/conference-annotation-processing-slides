% Pluggable Annotation Processing API
%
% JUG Toulouse - 2015 - LTE Consulting

## Arnaud Tournier

**ArchiDév** passionné chez **LTE Consulting**

Speaker **Devoxx**, **GWT.create**, **Paris**/**Toulouse JUG**, etc...

@ltearno www.lteconsulting.fr

**Full stack (x86_64 to JavaScript)** !

## Objectifs

Pluggable Annotation Processing API permet de s'inscrire dans le processus de compilation Java en exploitant les annotations présentes dans le code.

- Traitement des annotations à la compilation,
- Génération de nouvelles classes Java,
- Génération de fichiers de configuration,
- etc...

## Avantages

- Pas d'instrumentation du byte-code,
- Pas de traitement au runtime donc pas d'impact sur les performances.

## Histoire

- Commentaires Javadoc,
- APT ou [Annotation Processing Tool](http://docs.oracle.com/javase/7/docs/technotes/guides/apt/), retiré car [non extensible à java > 5](http://openjdk.java.net/jeps/117) avec java 7,
- et depuis 2006 (java 6) la [JSR-269](https://jcp.org/aboutJava/communityprocess/final/jsr269/index.html), créé par Joe Darcy

## Principes

- On fournit (par SPI) un processeur d'annotation,
- javac gère les `rounds` de processing,
- A chaque round, le processeur a accès à l'AST des classes parsées,
- Le processeur peut générer de nouveaux fichiers Java qui seront parsés et traités au prochain `round`.  

## JAVAC

Paramètres...

-s -sourcePath, ...

ATTENTION : le warning si on ne met pas *-implicit:none*

http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html#searching

## Rappel, création d'une annotation

	public @interface MonAnnotation
	{
		...
	}

## Limitations

- Pas possible de modifier des classes existantes
- Certains bug ne permettent pas de traiter correctement les génériques

## Au delà des limitations

- Lombok : quelques hack pour accéder à l'implémentation (javac de sun et jdt) et modifier l'AST
- Immutables : quelques workaround captant les implementations JDK / JDT pour gérer les génériques

## Tests unitaires

## Utilisations

- Dagger,
- Google Auto,
- Immutables,
- Hexa Binding,
- Lombok,
- GWT,
- JPA model generation (JSR-317)

## Lombok

- http://stackoverflow.com/questions/6107197/how-does-lombok-work

## links



Articles :
http://www.javabeat.net/java-6-0-features-part-2-pluggable-annotation-processing-api/

Lombok :
http://notatube.blogspot.fr/2010/11/project-lombok-trick-explained.html

Description des phases de compilation javac
http://openjdk.java.net/groups/compiler/doc/compilation-overview/index.html

http://thecodersbreakfast.net/index.php?post/2009/07/09/Enforcing-design-rules-with-the-Pluggable-Annotation-Processor

https://deors.wordpress.com/2011/10/08/annotation-processors/

http://jmdoudoux.developpez.com/cours/developpons/java/chap-annotations.php

http://www.angelikalanger.com/Conferences/Slides/JavaAnnotationProcessing-JSpring-2008.pdf

https://weblogs.java.net/blog/emcmanus/archive/2006/06/using_annotatio.html

http://docs.jboss.org/hibernate/validator/5.1/reference/en-US/html/validator-annotation-processor.html

http://scg.unibe.ch/archive/projects/Erni08b.pdf ****

https://openclassrooms.com/courses/java-et-les-annotations/creez-et-utilisez-vos-propres-annotations

histoire et APT
https://docs.oracle.com/javase/7/docs/technotes/guides/apt/GettingStarted.html

https://www.google.fr/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=0CCEQFjAAahUKEwi9gJu1gtPHAhVF6RQKHU9ZCPA&url=http%3A%2F%2Fwww.parisjug.org%2Fxwiki%2Fwiki%2Foldversion%2Fdownload%2FMeeting%2F20130212%2FpresentationDimitriBaeliGillesDiGuglielmo.pdf&ei=0x_kVf3KNcXSU8-yoYAP&usg=AFQjCNGgRSIrnU3yt0BMokotzqEC3aZ3VA&sig2=AKXWbGrIBdbw9N5SJhYr-w

https://www.parleys.com/search/croisier  : il y a 4 vidéos sur les annotations

http://fr.slideshare.net/Zenika/annotations-paris-jug

http://thecodersbreakfast.net/index.php?post/2010/11/26/Conference-Annotations-les-slides

http://www.touilleur-express.fr/2010/12/17/soiree-java-avance-avec-olivier-croisier-au-paris-jug/




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

##

<img src="utilisationTypique.png" style="box-shadow:none; border:none;"/>

##

<img src="utilisationHybride.png" style="box-shadow:none; border:none;"/>

