package org.test.codeGeneration;

import com.google.auto.service.AutoService;
import org.test.ExcludeFromDTOMapper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;


@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("org.test.ToDTO")
@AutoService(Processor.class)
public class DTOProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.forEach(annotation ->
                roundEnv
                        .getElementsAnnotatedWith(annotation)
                        .forEach(this::generateDTOFile)
        );
        return true;
    }

    private void generateDTOFile(Element element) {

        String className = element.getSimpleName().toString();
        String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String fileName = className + "DTO";
        String filePackageName = packageName + "." + fileName;

        List<? extends Element> fields = element.getEnclosedElements().stream()
                .filter(e -> e.getKind().isField()) // only keep fields
                .filter(e -> e.getAnnotation(ExcludeFromDTOMapper.class) == null)
                .toList();

        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(filePackageName).openWriter())) {
            writer.println("package " + filePackageName + ";");
            writer.println();
            writer.println("public class " + fileName + " {");
            writer.println("}");

        } catch (IOException e) {
            throw new RuntimeException("Error creating new Java File for " + className + "." + e);
        }
    }

}
