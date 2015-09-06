package fr.lteconsulting.jsr69;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes( { "fr.lteconsulting.jsr69.MonAnnotation" } )
@SupportedSourceVersion( SourceVersion.RELEASE_8 )
public class MonProcesseur extends AbstractProcessor
{
	private Messager messager;

	private PrintWriter pw;

	private int round = 1;

	@Override
	public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment roundEnv )
	{
		boolean result = false;

		try
		{
			FileObject fo = processingEnv.getFiler().createResource( StandardLocation.SOURCE_OUTPUT, "fr.lteconsulting", "RapportAnnotation-" + round++ + ".txt" );
			pw = new PrintWriter( fo.openWriter() );
			
			pw.println( "Processing round, processingOver : " + roundEnv.processingOver() + ", errorRaised : " + roundEnv.errorRaised() );

			for( Element element : roundEnv.getElementsAnnotatedWith( MonAnnotation.class ) )
			{
				log( element, "ELEMENT ANNOTATED WITH " + MonAnnotation.class.getName() );
				visitElement( element );

				result = true;
			}

			pw.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}

		return result;
	}

	private int prefix = 0;

	private String getPrefix()
	{
		StringBuilder sb = new StringBuilder();
		for( int i = 0; i < prefix * 4; i++ )
			sb.append( " " );
		return sb.toString();
	}

	private void log( Element e, String message )
	{
		if( messager == null )
			messager = processingEnv.getMessager();

		String prefix = getPrefix();
		messager.printMessage( Kind.WARNING, prefix + message, e );
		pw.println( prefix + message );
	}

	private Set<Element> visitedElements = new HashSet<>();

	private void visitElement( Element element )
	{
		if( visitedElements.contains( element ) )
		{
			log( element, "! already visited element " + element.getSimpleName() );
			return;
		}
		visitedElements.add( element );

		log( element, "visiting element '" + element.getSimpleName() + "'" );
		element.accept( visitor, null );
		visitEnclosedElements( element );
	}

	private void visitElements( List<? extends Element> elements )
	{
		for( Element element : elements )
			visitElement( element );
	}

	private void visitEnclosedElements( Element element )
	{
		if( element.getEnclosedElements().isEmpty() )
			return;

		log( element, "visiting element's children" );
		prefix++;

		for( Element child : element.getEnclosedElements() )
		{
			visitElement( child );
		}

		prefix--;
	}

	private void visitTypeMirror( TypeMirror type )
	{
		log( null, "TypeMirror : " + type + ", kind : " + (type == null ? "null" : type.getKind()) );
	}

	private void visitTypeMirrors( List<? extends TypeMirror> types )
	{
		for( TypeMirror type : types )
			visitTypeMirror( type );
	}

	private final ElementVisitor<Void, Void> visitor = new ElementVisitor<Void, Void>()
	{
		@Override
		public Void visitVariable( VariableElement e, Void p )
		{
			log( e, "variable " + e.getSimpleName() + " = " + e.getConstantValue() );
			return null;
		}

		@Override
		public Void visitUnknown( Element e, Void p )
		{
			log( e, "Unknown " + e.getSimpleName() );
			return null;
		}

		@Override
		public Void visitTypeParameter( TypeParameterElement e, Void p )
		{
			log( e, "type parameter : " + e.getGenericElement() + ", bounds : " + e.getBounds() );
			return null;
		}

		@Override
		public Void visitType( TypeElement e, Void p )
		{
			log( e, "type : nesting kind " + e.getNestingKind() + ", qualified name : " + e.getQualifiedName() + ", superclass : " + e.getSuperclass() );
			log( e, "interfaces : " );
			visitTypeMirrors( e.getInterfaces() );
			log( e, "type parameters : " );
			visitElements( e.getTypeParameters() );

			visitEnclosedElements( e );
			return null;
		}

		@Override
		public Void visitPackage( PackageElement e, Void p )
		{
			log( e, "package" + e.getQualifiedName() + " isUnnamed : " + e.isUnnamed() );
			visitEnclosedElements( e );
			return null;
		}

		@Override
		public Void visitExecutable( ExecutableElement e, Void p )
		{
			log( e, "executable : isVarargs : " + e.isVarArgs() + ", isDefault : " + e.isDefault() + ", default value : " + e.getDefaultValue() );
			log( e, "type parameters : " );
			visitElements( e.getTypeParameters() );
			log( e, "return type : " );
			visitTypeMirror( e.getReturnType() );
			log( e, "parameters : " );
			visitElements( e.getParameters() );
			log( e, "receiver type : " );
			visitTypeMirror( e.getReceiverType() );
			log( e, "thrown types" );
			visitTypeMirrors( e.getThrownTypes() );
			return null;
		}

		@Override
		public Void visit( Element e, Void p )
		{
			log( e, "*element : " );
			visitElement( e );
			return null;
		}

		@Override
		public Void visit( Element e )
		{
			log( e, "**element : " );
			visitElement( e );
			return null;
		}
	};
}
