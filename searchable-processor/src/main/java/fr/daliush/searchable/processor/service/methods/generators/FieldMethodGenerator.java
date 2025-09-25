package fr.daliush.searchable.processor.service.methods.generators;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.VariableElement;
import java.util.List;

public interface FieldMethodGenerator {

    /**
     * Generate the method for the field
     * @return the method to add to the class builder
     * @param field The entity field
     */
    List<MethodSpec> generate(VariableElement field);
}
