package fr.daliush.searchable.processor;

import com.google.auto.service.AutoService;
import fr.daliush.searchable.annotations.SearchableRepository;
import fr.daliush.searchable.processor.service.ImplGenerator;
import fr.daliush.searchable.processor.utils.ReflexionUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.Diagnostic;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("*")
public class SearchableProcessor extends AbstractProcessor {

    private Types types;
    private Elements elements;
    private Messager messager;
    private ImplGenerator generator;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.types = env.getTypeUtils();
        this.elements = env.getElementUtils();
        this.messager = env.getMessager();
        this.generator = new ImplGenerator(env);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // We only generate sources for SearchableRepositories
        TypeElement searchableRepoIface = elements.getTypeElement(SearchableRepository.class.getCanonicalName());
        //If there are none SearchableRepositories we let the other processor work
        if (searchableRepoIface == null) return false;

        for (Element root : roundEnv.getRootElements()) {
            //If it's not an interface, it's none of our business
            if (root.getKind() != ElementKind.INTERFACE) continue;
            TypeElement clazz = (TypeElement) root;

            if (ReflexionUtils.classImplementsInterface(clazz, searchableRepoIface, types)) {

                try {
                    generator.generateImplForRepository(clazz);
                } catch (Exception e) {
                    messager.printMessage(Diagnostic.Kind.ERROR,
                            "Generation failed for " + clazz.getQualifiedName() + " : " + e.getMessage(), clazz);
                }
            }
        }
        return false;
    }

    /**
     * Checks if a class implements a certain interface
     * Doesn't look at the generic types of the interface and class
     *
     * @param clazz The class
     * @param iface The interface
     * @return true if the class implements the interface, false otherwise
     */
    private boolean implementsRepo(TypeElement clazz, TypeElement iface) {
        for (TypeMirror itf : clazz.getInterfaces()) {
            // erasure removes the <T> for example. so Interface<T> == Interface<Something>
            if (types.erasure(itf).equals(types.erasure(iface.asType()))) {
                return true;
            }
        }
        return false;
    }
}
