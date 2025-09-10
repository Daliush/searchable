package fr.daliush.searchable.processor.service;

import com.squareup.javapoet.*;
import fr.daliush.searchable.processor.utils.ReflexionUtils;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ImplGenerator {

    private final Filer filer;
    private final Elements elements;
    private final Messager messager;
    private final Types types;

    public ImplGenerator(ProcessingEnvironment env) {
        this.filer = env.getFiler();
        this.elements = env.getElementUtils();
        this.messager = env.getMessager();
        this.types = env.getTypeUtils();
    }

    public void generateImplForRepository(TypeElement repoInterface) throws IOException {
        String pkg = ReflexionUtils.getPackageNameOfClass(repoInterface, elements);

        String implName = ReflexionUtils.generateImplementationName(repoInterface);

        // Generates a public class
        TypeSpec.Builder searchClass = TypeSpec.classBuilder(implName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(repoInterface.asType()));

        // @Generated annotation so we that the class is generated and by wich package it has been generated
        searchClass.addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class)
                .addMember("value", "$S", "fr.daliush.searchable.processor.SearchableProcessor")
                .build());


        TypeElement base = elements.getTypeElement("fr.daliush.searchable.annotations.SearchableRepository");
        Optional<TypeMirror> searchRepoInterface = ReflexionUtils.getInterfaceFromImplements(repoInterface, base, types);

        if(searchRepoInterface.isPresent()) {
            if(searchRepoInterface.get() instanceof DeclaredType dt) {
                generateMethods(dt, searchClass);

                JavaFile javaFile = buildJavaFile(pkg, searchClass);
                javaFile.writeTo(filer);

                messager.printMessage(Diagnostic.Kind.NOTE,
                        "Generated " + pkg + "." + implName + " for " + repoInterface.getQualifiedName());
            }
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    repoInterface + " Does not implements SearchableRepository");
        }
    }

    private static JavaFile buildJavaFile(String pkg, TypeSpec.Builder searchClass) {
        return JavaFile.builder(pkg, searchClass.build())
                .skipJavaLangImports(true)
                .build();
    }

    private void generateMethods(DeclaredType dt, TypeSpec.Builder type) {
        Element entity = types.asElement(dt.getTypeArguments().getFirst());
        List<VariableElement> searchableFields = ReflexionUtils.findDeclaredSearchableFields(entity);
        searchableFields.forEach(
                field -> {
                    MethodSpec empty = MethodSpec.methodBuilder(field.getSimpleName().toString())
                            .addModifiers(Modifier.PUBLIC)
                            .returns(TypeName.VOID)
                            .build();

                    type.addMethod(empty);
                }
        );
    }

}
