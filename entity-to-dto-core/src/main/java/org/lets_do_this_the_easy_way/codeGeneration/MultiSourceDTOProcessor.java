package org.lets_do_this_the_easy_way.codeGeneration;

import com.google.auto.service.AutoService;
import org.lets_do_this_the_easy_way.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("org.lets_do_this_the_easy_way.annotations.ToMultiSourceDTO")
@AutoService(Processor.class)
public class MultiSourceDTOProcessor extends AbstractProcessor {

    String generatedPackageName = "org.lets_do_this_the_easy_way.generated";


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.forEach(annotation -> generateMultiSourceDTOFile(roundEnv.getElementsAnnotatedWith(annotation)));
        return true;
    }

    private void generateMultiSourceDTOFile(Set<? extends Element> elements) {
        // Group all elements by their identifierName
        Map<String, List<Element>> groups = elements.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getAnnotation(ToMultiSourceDTO.class).identifierName()
                ));

        // Generate one source file per group
        groups.values().forEach(group -> generateSourceFile(Set.copyOf(group)));
    }

    private void generateSourceFile(Set<? extends Element> sharedSourceElements) {

        // FIXED: convert to a sorted list for stable iteration
        List<? extends Element> sortedElements = sharedSourceElements.stream()
                .sorted(Comparator.comparing(e -> e.getSimpleName().toString()))
                .toList();

        String fileName = sortedElements.get(0)
                .getAnnotation(ToMultiSourceDTO.class)
                .identifierName();

        List<? extends Element> fields = sortedElements.stream()
                .flatMap(element -> element.getEnclosedElements().stream())
                .filter(e -> e.getKind().isField())
                .filter(e -> e.getAnnotation(ExcludeFromDTO.class) == null)
                .toList();

        List<DTOExtraFields> extraFieldsAnnotations = sortedElements.stream()
                .map(e -> e.getAnnotation(DTOExtraFields.class))
                .filter(Objects::nonNull)
                .toList();

        List<DTOExtraField> extraFields = extraFieldsAnnotations.stream()
                .flatMap(dtoExtraFields -> Stream.of(dtoExtraFields.value()))
                .toList();

        if (fields.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@ToMultiSourceDTO classes must have at least one eligible field.");
            return;
        }

        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(fileName).openWriter())) {

            writer.println("package %s;".formatted(generatedPackageName));
            writer.println("public class %s {".formatted(fileName));
            fields.forEach(field -> {

                String fieldName = field.getSimpleName().toString();

                if (field.getAnnotation(DTOName.class) != null) {
                    DTOName dtoName = field.getAnnotation(DTOName.class);
                    fieldName = dtoName.name();
                }

                writer.println("""
                         private %s %s;
                        
                         public %s get%s() {
                           return this.%s;
                         }
                        
                         public void set%s(%s %s) {
                            this.%s = %s;
                         }
                        """.formatted(field.asType(), fieldName, field.asType(), fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), fieldName, fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), field.asType(), fieldName.toLowerCase(), fieldName, fieldName.toLowerCase()

                ));
            });

            for (DTOExtraField extra : extraFields) {
                String name = extra.name();
                String type = extra.type();
                String defaultValue = extra.defaultValue();

                if (!Objects.equals(type, "String")) {
                    writer.printf("""
                                     private %s %s = %s;
                                    
                                     public %s get%s() {
                                       return this.%s;
                                     }
                                    
                                     public void set%s(%s %s) {
                                        this.%s = %s;
                                     }
                                    
                                    """,
                            type, name, defaultValue.isEmpty() ? getDefaultValueForType(type) : defaultValue,
                            type, capitalize(name), name,
                            capitalize(name), type, name, name, name
                    );
                } else {

                    writer.printf("""
                                     private %s %s = "%s";
                                    
                                     public %s get%s() {
                                       return this.%s;
                                     }
                                    
                                     public void set%s(%s %s) {
                                        this.%s = %s;
                                     }
                                    
                                    """,
                            type, name, defaultValue.isEmpty() ? getDefaultValueForType(type) : defaultValue,
                            type, capitalize(name), name,
                            capitalize(name), type, name, name, name
                    );
                }
            }
            writer.println("}");

            generateDTOMapper(sortedElements, fileName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void generateDTOMapper(List<? extends Element> elements, String dtoClassName) {

        String fileName = dtoClassName + "Mapper";

        List<SourceFieldInfo> fieldInfos = elements.stream()
                .flatMap(source -> source.getEnclosedElements().stream()
                        .filter(e -> e.getKind().isField())
                        .filter(e -> e.getAnnotation(ExcludeFromDTO.class) == null)
                        .map(field -> new SourceFieldInfo(source, field)))
                .toList();


        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(generatedPackageName + "." + dtoClassName + "Mapper").openWriter())) {

            // Package and imports
            writer.printf("package %s;%n%n", generatedPackageName);
            writer.println("import java.lang.reflect.Field;");
            writer.println("import java.util.List;");
            writer.println("import java.util.ArrayList;");
            writer.println("import %s.%s;".formatted(generatedPackageName, dtoClassName));

            // FIXED: deterministic import order
            elements.forEach(element -> writer.println("import %s;".formatted(element.asType().toString())));
            writer.println();

            // Class declaration
            writer.printf("public class %s {%n%n", fileName);

            // Method signature
            writer.print("    public static %s mapTo%s(".formatted(dtoClassName, dtoClassName));

            // FIXED: deterministic parameter order
            String result = elements.stream()
                    .map(e -> {
                        String name = e.getSimpleName().toString();
                        return "%s %s".formatted(name, name.toLowerCase());
                    })
                    .collect(Collectors.joining(", "));

            writer.print(result);

            writer.printf(") {%n");
            writer.printf("        %s dto = new %s();%n%n", dtoClassName, dtoClassName);
            if (!fieldInfos.isEmpty()) {

                writer.println("        try {");
                writer.println();

                // Field reflection logic
                for (SourceFieldInfo info : fieldInfos) {
                    String oldClassName = info.sourceClass.getSimpleName().toString();

                    String fieldName = info.field.getSimpleName().toString();
                    String fieldNameDTO = info.field.getSimpleName().toString();

                    if (info.field.getAnnotation(DTOName.class) != null) {
                        DTOName dtoName = info.field.getAnnotation(DTOName.class);
                        fieldNameDTO = dtoName.name();
                    }

                    writer.printf("            Field %sfield = %s.class.getDeclaredField(\"%s\");%n", fieldName, oldClassName, fieldName);
                    writer.printf("            %sfield.setAccessible(true);%n", fieldName);
                    writer.printf("            Object %sValue = %sfield.get(%s);%n%n", fieldName, fieldName, oldClassName.toLowerCase());

                    writer.printf("            Field %sDTO = %s.class.getDeclaredField(\"%s\");%n", fieldNameDTO, dtoClassName, fieldNameDTO);
                    writer.printf("            %sDTO.setAccessible(true);%n", fieldNameDTO);
                    writer.printf("            %sDTO.set(dto, %sValue);%n%n", fieldNameDTO, fieldName);
                }

                // Catch block and return
                writer.println("        } catch (NoSuchFieldException | IllegalAccessException e) {");
                writer.print("            throw new RuntimeException(\"cant access fields from");
                elements.forEach(element -> writer.print("%s, ".formatted(element.getSimpleName().toString())));
                writer.println("\"+ e);");
                writer.println("        }");
            }
            writer.println();
            writer.println("        return dto;");
            writer.println("    }\n");
            writer.println("        }\n");

        } catch (IOException e) {
            throw new RuntimeException("Error creating new Java File for " + fileName + ": " + e);
        }
    }

    private String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private String getDefaultValueForType(String type) {
        return switch (type) {
            case "int", "short", "byte", "long", "float", "double" -> "0";
            case "boolean" -> "false";
            case "char" -> "'\\0'";
            default -> "null";
        };
    }

    private static class SourceFieldInfo {
        final Element sourceClass;
        final Element field;

        SourceFieldInfo(Element sourceClass, Element field) {
            this.sourceClass = sourceClass;
            this.field = field;
        }
    }

}
