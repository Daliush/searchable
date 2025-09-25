package fr.daliush.searchable.processor.service;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import fr.daliush.searchable.processor.service.methods.exceptions.NoGeneratorException;
import fr.daliush.searchable.processor.service.methods.generators.MethodGeneratorRegistry;
import fr.daliush.searchable.processor.utils.ReflexionUtils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImplGenerator {

    private final Filer filer;
    private final Elements elements;
    private final Messager messager;
    private final Types types;
    private final MethodGeneratorRegistry methodGeneratorRegistry = new MethodGeneratorRegistry();
    private static final String SEARCHABLE_ANNOTATION = "fr.daliush.searchable.annotations.Searchable";

    public ImplGenerator(ProcessingEnvironment env) {
        this.filer = env.getFiler();
        this.elements = env.getElementUtils();
        this.messager = env.getMessager();
        this.types = env.getTypeUtils();
    }

    public void generateImplForRepository(TypeElement entity) throws IOException
    {
        String pkg = ReflexionUtils.getPackageNameOfClass(entity, elements);

        String implName = ReflexionUtils.generateImplementationName(entity);

        // Generates a public class
        TypeSpec.Builder searchClass = TypeSpec.classBuilder(implName)
                .addModifiers(Modifier.PUBLIC);

        // @Generated annotation so we that the class is generated and by wich package it has been generated
        searchClass.addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class)
                .addMember("value", "$S", "fr.daliush.searchable.processor.SearchableProcessor")
                .build());

        List<VariableElement> searchableFields = findDeclaredSearchableFields(entity);
        generateMethods(searchableFields, searchClass, entity);

        JavaFile javaFile = buildJavaFile(pkg, searchClass);
        javaFile.writeTo(filer);

        messager.printMessage(Diagnostic.Kind.NOTE,
                "Generated " + pkg + "." + implName + " for " + entity.getQualifiedName());

    }

    /**
     * Builds the java class ( actually write it )
     * @param pkg the package of the class
     * @param searchClass the builder of the class
     * @return the actual java file
     */
    private static JavaFile buildJavaFile(String pkg, TypeSpec.Builder searchClass) {
        return JavaFile.builder(pkg, searchClass.build())
                .skipJavaLangImports(true)
                .build();
    }

    /**
     * Génère les méthodes de la classe repository générée
     * @param searchableFields les fields annotés @Searchable
     * @param type The class to build
     * @param entity the entity that has the field
     */
    private void generateMethods(List<VariableElement> searchableFields,TypeSpec.Builder type, TypeElement entity) {
        searchableFields.forEach(
                field -> {
                    try
                    {
                        List<MethodSpec> methods = methodGeneratorRegistry.getMethodSpecsFromField(field);
                        methods.forEach(type::addMethod);
                    } catch (NoGeneratorException e) {
                        messager.printMessage(
                                Diagnostic.Kind.WARNING,
                                "Could not generate methods for field " + field.getSimpleName()
                                + " of class " + entity.getSimpleName()
                                        + " ( No generator found for type " + field.asType().toString() + " ) "
                                );
                    }
                }
        );
    }

    /**
     * Gets declared @Searchable fields inside a class
     * @param type the class
     * @return a List of all the fields
     */
    List<VariableElement> findDeclaredSearchableFields(TypeElement type) {
        return ElementFilter.fieldsIn(type.getEnclosedElements()).stream()
                .filter(this::hasSearchable)
                .toList();
    }

    /**
     * Is the Element is annotated with @Searchable ?
     * @param el the Element you want to test
     * @return true if the Element is annotated with @Searchable
     */
    private boolean hasSearchable(Element el) {
        for (AnnotationMirror am : el.getAnnotationMirrors()) {
            Element annEl = am.getAnnotationType().asElement();
            if (annEl instanceof TypeElement annType
                    && annType.getQualifiedName().contentEquals(SEARCHABLE_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find all the searchable fields ( from extended class too ) in a TypeElement
     * @param type the Element where you wanna get all the @Searchable fields
     * @return a List of all the @Seachable Fields
     */
    List<VariableElement> findAllSearchableFields(TypeElement type) {
        Map<String, VariableElement> byName = new LinkedHashMap<>();
        for (VariableElement f : ElementFilter.fieldsIn(elements.getAllMembers(type))) {
            if (hasSearchable(f) && !f.getModifiers().contains(Modifier.STATIC)) {
                byName.putIfAbsent(f.getSimpleName().toString(), f);
            }
        }
        return List.copyOf(byName.values());
    }


}
