package org.lets_do_this_the_easy_way.codeGeneration;

import com.google.auto.service.AutoService;
import org.lets_do_this_the_easy_way.annotations.ToMultiSourceDTO;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("org.lets_do_this_the_easy_way.annotations.ToMultiSourceDTO")
@AutoService(Processor.class)
public class MultiSourceDTOProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.forEach(annotation -> generateMultiSourceDTOFile(roundEnv.getElementsAnnotatedWith(annotation)));
        return true;
    }

    private void generateMultiSourceDTOFile(Set<? extends Element> elements) {

        while (!elements.isEmpty()) {
            Element currentElement = elements.iterator().next();
            elements = generateOneSpecificMultiSourceDTOFile(elements, currentElement);
        }

    }

    private Set<? extends Element> generateOneSpecificMultiSourceDTOFile(Set<? extends Element> elements, Element element) {
        Set<? extends Element> sharedSourceElements;

        ToMultiSourceDTO currentDTO = element.getAnnotation(ToMultiSourceDTO.class);
        sharedSourceElements =
                elements
                        .stream()
                        .filter(currentElement -> {
                            ToMultiSourceDTO annotation = currentElement.getAnnotation(ToMultiSourceDTO.class);
                            return currentDTO.identifierName().equals(annotation.identifierName());
                        }).collect(Collectors.toSet());

        generateSourceFile(sharedSourceElements);

        elements.removeAll(sharedSourceElements);


        return elements;
    }

    private void generateSourceFile(Set<? extends Element> sharedSourceElements) {
        //TODO code generation logic to be implemented
    }
}
