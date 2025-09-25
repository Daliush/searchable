package fr.daliush.searchable.processor.utils;

import fr.daliush.searchable.annotations.Searchable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

public class ReflexionUtils {

    private ReflexionUtils() {
        throw new IllegalStateException("Util class, you shouldn't be here");
    }
    /**
     * Checks if a class implements a certain interface
     * Doesn't look at the generic types of the interface and class
     * @param clazz The class
     * @param iface The interface
     * @return true if the class implements the interface, false otherwise
     */
    public static boolean classImplementsInterface(TypeElement clazz, TypeElement iface, Types types) {
        return getInterfaceFromImplements(clazz, iface, types).isPresent();
    }

    /**
     * Takes in a class and an interface and returns the exact interface declaration.
     * @param clazz The class where you wanna get the interface. Example : EntityRepository
     * @param iface The interface you wanna search in the class. Example : SearchRepository
     * @return if EntityRepository extends SearchRepository< Entity >, it will return the type mirror of
     * SearchRepository< Entity >
     *     <br>
     *     If the interface is not found, returns a empty optional.
     */
    public static Optional<TypeMirror> getInterfaceFromImplements(TypeElement clazz, TypeElement iface, Types types) {
        for (TypeMirror itf : clazz.getInterfaces()) {
            // erasure removes the <T> for example. so Interface<T> == Interface<Something>
            if (types.erasure(itf).equals(types.erasure(iface.asType()))) {
                return Optional.of(itf);
            }
        }
        return Optional.empty();
    }

    /**
     * Generates the repository implementation name
     * @param entity the entity
     * @return a class name like InterfaceName + "Impl"
     */
    public static String generateImplementationName(TypeElement entity) {
        String entityName = entity.getSimpleName().toString();
        return entityName + "GeneratedRepository";
    }

    /**
     * Gets the package of a class / interface
     * @param repoInterface the class / interface
     * @param elements utilities for elements
     * @return the package name of the class / interface
     */
    public static String getPackageNameOfClass(TypeElement repoInterface, Elements elements) {
        return elements.getPackageOf(repoInterface).getQualifiedName().toString();
    }

    /**
     * Get @Searchable fields of a class
     * @param entityType the class where you wanna search the @Searchable fields
     * @return A list of @Searchable fields
     */
    public static List<VariableElement> findDeclaredSearchableFields(Element entityType) {
        return entityType.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .filter(f -> f.getAnnotation(Searchable.class) != null)
                .toList();
    }
}
