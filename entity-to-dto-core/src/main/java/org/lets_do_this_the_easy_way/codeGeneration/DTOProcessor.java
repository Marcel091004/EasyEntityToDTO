package org.lets_do_this_the_easy_way.codeGeneration;

import com.google.auto.service.AutoService;
import org.lets_do_this_the_easy_way.ExcludeFromDTO;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;


@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("org.lets_do_this_the_easy_way.ToDTO")
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

        String oldClassName = element.getSimpleName().toString();
        String oldPackageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String fileName = oldClassName + "DTO";
        String filePackageName = oldPackageName + "." + fileName;
        String capitalizedFieldName = Character.toUpperCase(oldClassName.charAt(0)) + oldClassName.substring(1);

        List<? extends Element> fields = element.getEnclosedElements().stream()
                .filter(e -> e.getKind().isField())
                .filter(e -> e.getAnnotation(ExcludeFromDTO.class) == null)
                .toList();

        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(filePackageName).openWriter())) {


            writer.println("""
                    package %s;
                    
                    public class %s {
                    """.formatted(oldPackageName, fileName));

            fields.forEach(field -> {
                writer.println("""
                                 private %s %s;
                                
                                 public %s get%s() {
                                   return this.%s;
                                 }
                                
                                 public void set%s(%s %s) {
                                    this.%s = %s;
                                 }
                                """.formatted(
                                field.asType(),
                                field.getSimpleName(),
                                field.asType(),
                                field.getSimpleName().toString().substring(0, 1).toUpperCase() + field.getSimpleName().toString().substring(1),
                                field.getSimpleName(),
                                field.getSimpleName().toString().substring(0, 1).toUpperCase() + field.getSimpleName().toString().substring(1),
                                field.asType(),
                                field.getSimpleName().toString().toLowerCase(),
                                field.getSimpleName(),
                                field.getSimpleName().toString().toLowerCase()

                        )
                );
            });

            writer.println("}");

            generateDTOMapper(element, fileName);

        } catch (IOException e) {
            throw new RuntimeException("Error creating new Java File for " + oldClassName + "." + e);
        }
    }

    private void generateDTOMapper(Element element, String dtoClassName) {

        String oldClassName = element.getSimpleName().toString();
        String oldPackageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String fileName = oldClassName + "DTOMapper";

        List<? extends Element> fields = element.getEnclosedElements().stream()
                .filter(e -> e.getKind().isField())
                .filter(e -> e.getAnnotation(ExcludeFromDTO.class) == null)
                .toList();

        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(oldPackageName + "." + fileName).openWriter())) {
            String sourceVarName = oldClassName.toLowerCase();
            String targetVarName = dtoClassName.toLowerCase();

            writer.printf("package %s;%n%n", oldPackageName);
            writer.printf("public class %s {%n%n", fileName);

            writer.printf("    %s %s = new %s();%n%n", dtoClassName, targetVarName, dtoClassName);

            writer.printf("    public %s mapTo%s(%s %s) {%n", dtoClassName, dtoClassName, oldClassName, sourceVarName);

            // Generate setter lines for each field
            for (Element field : fields) {
                String fieldName = field.getSimpleName().toString().toLowerCase();
                String capitalized = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                writer.printf("        %s.set%s(%s.get%s());%n", targetVarName, capitalized, sourceVarName, capitalized);
            }

            writer.printf("        return %s;%n", targetVarName);
            writer.println("    }");
            writer.println("}");
        } catch (IOException e) {
            throw new RuntimeException("Error creating new Java File for " + oldClassName + ": " + e);
        }
    }

}
