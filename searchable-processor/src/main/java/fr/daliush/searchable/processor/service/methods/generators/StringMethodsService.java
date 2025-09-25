package fr.daliush.searchable.processor.service.methods.generators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.List;

public class StringMethodsService implements FieldMethodGenerator {

    @Override
    public List<MethodSpec> generate(VariableElement field)
    {
        return List.of(MethodSpec.methodBuilder(field.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .build()
        );
    }
}
