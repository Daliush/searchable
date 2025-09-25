package fr.daliush.searchable.processor.service.methods.generators;

import com.squareup.javapoet.MethodSpec;
import fr.daliush.searchable.processor.service.methods.exceptions.NoGeneratorException;
import fr.daliush.searchable.processor.service.methods.utils.TypeEnum;

import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MethodGeneratorRegistry {
    private final Map<String, FieldMethodGenerator> generators;


    public MethodGeneratorRegistry()
    {
        this.generators = Map.of(
                TypeEnum.STRING.getValue(), new StringMethodsService()
        );
    }

    /**
     * Returns the generated searchable methods for a specific field
     * @param field the field used to generate the methods
     * @return A list of the methods
     * @throws NoGeneratorException if no generator has been found for this kind of field
     */
    public List<MethodSpec> getMethodSpecsFromField(VariableElement field) throws NoGeneratorException
    {
        FieldMethodGenerator generator = generators.get(field.asType().toString());
        if (Objects.isNull(generator)) {
            throw new
                    NoGeneratorException("No generator has been found for type " + field.asType().toString());
        } else {
            return generator.generate(field);
        }

    }
}
