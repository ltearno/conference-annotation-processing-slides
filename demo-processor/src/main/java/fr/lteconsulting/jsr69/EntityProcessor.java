package fr.lteconsulting.jsr69;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes( { "fr.lteconsulting.jsr69.Entity" } )
@SupportedSourceVersion( SourceVersion.RELEASE_7 )
public class EntityProcessor extends AbstractProcessor
{
	private Messager messager;

	@Override
	public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		for( Element element : roundEnv.getElementsAnnotatedWith( Entity.class ) )
		{
			if( !ensureHasId( element ) )
				continue;
			
			generateManager( element );
		}

		return true;
	}

	boolean ensureHasId( Element element )
	{
		Element idElement = getIdElement( element );
		if( idElement == null )
		{
			getMessager().printMessage( Kind.ERROR, "Cannot find an ID field !", element );
			return false;
		}
		else
		{
			getMessager().printMessage( Kind.WARNING, "This is the ID field for the class " + element.getSimpleName(), idElement );
			return true;
		}
	}
	
	Element getIdElement(Element classElement)
	{
		Optional<? extends Element> idElement = classElement.getEnclosedElements().stream().filter( e -> 
			(e.getKind() == ElementKind.METHOD 
			&& 
			(e.getSimpleName().toString().equals( "getId" ) || e.getSimpleName().equals( "id" )) && ((ExecutableElement) e).getParameters().isEmpty() )
			||
			(e.getKind() == ElementKind.FIELD
			&&
			e.getSimpleName().toString().equals( "id" ) ))
				.findFirst();
		
		if(idElement.isPresent())
			return idElement.get();
		return null;
	}
	
	Messager getMessager()
	{
		if( messager == null )
			messager = processingEnv.getMessager();
		return messager;
	}
	
	private String getter( Element classElement )
	{
		Element idElement = getIdElement( classElement );
		
		if( idElement.getKind() == ElementKind.FIELD )
			return idElement.getSimpleName().toString();
		
		if(idElement.getKind() == ElementKind.METHOD )
			return idElement.getSimpleName() + "()";
		
		return "ERROR-GETTER";
	}
	
	void generateManager( Element classElement )
	{
		try
		{
			String fqn = ((TypeElement)classElement).getQualifiedName().toString();
			String packageName = processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
			String typeName = classElement.getSimpleName().toString().replace(".", "_") + "Manager";
			
			JavaFileObject file = processingEnv.getFiler().createSourceFile( packageName + "." + typeName, classElement );
			Writer writer = file.openWriter();
			PrintWriter pw = new PrintWriter( writer );
			pw.println( "package "+packageName+";" );
			pw.println( "" );
			pw.println( "public class " + typeName + " {" );
			pw.println( "    private final java.util.Map<Object, " + classElement.getSimpleName() + "> items = new java.util.HashMap<>();" );
			pw.println( "    public void add( " + classElement.getSimpleName() + " item ) {"  );
			pw.println( "        items.put( item." + getter(classElement) + ", item);" );
			pw.println( "    }" );
			pw.println( "    " );
			pw.println( "    public " + classElement.getSimpleName() + " get( Object id ) {" );
			pw.println( "        return items.get( id );" );
			pw.println( "    }" );
			pw.println( "}" );
			pw.close();
			writer.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
}
