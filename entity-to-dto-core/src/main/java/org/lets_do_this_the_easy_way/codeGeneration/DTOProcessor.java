package org.lets_do_this_the_easy_way.codeGeneration;

import com.google.auto.service.AutoService;
import org.lets_do_this_the_easy_way.annotations.DTOExtraField;
import org.lets_do_this_the_easy_way.annotations.DTOExtraFields;
import org.lets_do_this_the_easy_way.annotations.DTOName;
import org.lets_do_this_the_easy_way.annotations.ExcludeFromDTO;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;


@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("org.lets_do_this_the_easy_way.annotations.ToDTO")
@AutoService(Processor.class)
public class DTOProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.forEach(annotation -> roundEnv.getElementsAnnotatedWith(annotation).forEach(this::generateDTOFile));
        return true;
    }

    private void generateDTOFile(Element element) {

        String oldClassName = element.getSimpleName().toString();
        String oldPackageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String fileName = oldClassName + "DTO";
        String filePackageName = oldPackageName + "." + fileName;

        List<? extends Element> fields = element
                .getEnclosedElements()
                .stream()
                .filter(e -> e.getKind().isField())
                .filter(e -> e.getAnnotation(ExcludeFromDTO.class) == null)
                .toList();

        if (fields.isEmpty() && element.getAnnotation(DTOExtraFields.class) == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "@ToDTO class must have at least one eligible field.", element);
            return;
        }

        DTOExtraFields extraFieldsAnnotation = element.getAnnotation(DTOExtraFields.class);
        List<DTOExtraField> extraFields = extraFieldsAnnotation != null
                ? List.of(extraFieldsAnnotation.value())
                : List.of();


        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(filePackageName).openWriter())) {


            writer.println("""
                    package %s;
                    
                    public class %s {
                    """.formatted(oldPackageName, fileName));

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
            }
            writer.println("}");

            generateDTOMapper(element, fileName);

        } catch (IOException e) {
            throw new RuntimeException("Error creating new Java File for " + oldClassName + "." + e);
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

    private void generateDTOMapper(Element element, String dtoClassName) {
        String oldClassName = element.getSimpleName().toString();
        String oldPackageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
        String fileName = oldClassName + "DTOMapper";

        List<? extends Element> fields = element.getEnclosedElements().stream().filter(e -> e.getKind().isField()).filter(e -> e.getAnnotation(ExcludeFromDTO.class) == null).toList();


        try (PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(oldPackageName + "." + fileName).openWriter())) {

            // Package and imports
            writer.printf("package %s;%n%n", oldPackageName);
            writer.println("import java.lang.reflect.Field;");
            writer.println("import java.util.List;");
            writer.println("import java.util.ArrayList;");
            writer.println();

            // Class declaration
            writer.printf("public class %s {%n%n", fileName);

            // Method signature
            writer.printf("    public static %s mapTo%s(%s %s) {%n", dtoClassName, dtoClassName, oldClassName, oldClassName);
            writer.printf("        %s dto = new %s();%n%n", dtoClassName, dtoClassName);
            if (!fields.isEmpty()) {

                writer.println("        try {");
                writer.println();

                // Field reflection logic
                for (Element field : fields) {
                    String fieldName = field.getSimpleName().toString();
                    String fieldNameDTO = field.getSimpleName().toString();

                    if (field.getAnnotation(DTOName.class) != null) {
                        DTOName dtoName = field.getAnnotation(DTOName.class);
                        fieldNameDTO = dtoName.name();
                    }

                    writer.printf("            Field %sfield = %s.class.getDeclaredField(\"%s\");%n", fieldName, oldClassName, fieldName);
                    writer.printf("            %sfield.setAccessible(true);%n", fieldName);
                    writer.printf("            Object %sValue = %sfield.get(%s);%n%n", fieldName, fieldName, oldClassName);

                    writer.printf("            Field %sDTO = %s.class.getDeclaredField(\"%s\");%n", fieldNameDTO, dtoClassName, fieldNameDTO);
                    writer.printf("            %sDTO.setAccessible(true);%n", fieldNameDTO);
                    writer.printf("            %sDTO.set(dto, %sValue);%n%n", fieldNameDTO, fieldName);
                }

                // Catch block and return
                writer.println("        } catch (NoSuchFieldException | IllegalAccessException e) {");
                writer.printf("            throw new RuntimeException(\"cant access fields from %s\" + e);%n", oldClassName);
                writer.println("        }");
            }
            writer.println();
            writer.println("        return dto;");
            writer.println("    }\n");

            //Next mapper
            writer.printf("    public static List<%s> mapTo%s(List<%s> %sList) {%n", dtoClassName, dtoClassName, oldClassName, oldClassName);
            writer.printf("        List<%s> dtoList =  new ArrayList<>();%n%n", dtoClassName);
            writer.printf("        %sList.forEach(entity ->{%n%n", oldClassName);

            writer.printf("           dtoList.add(%s.mapTo%s(entity));\n", dtoClassName + "Mapper", dtoClassName);

            writer.println("        });");
            writer.println("        return dtoList;");
            writer.println("    }\n");

            //Next mapper
            writer.printf("    public static %s[] mapTo%s(%s[] %sArray) {%n", dtoClassName, dtoClassName, oldClassName, oldClassName);
            writer.printf("        %s[] dtoArray = new %s[%sArray.length];%n", dtoClassName, dtoClassName, oldClassName);
            writer.printf("       for(int i = 0; i < dtoArray.length; i++) {%n%n");

            writer.printf("           dtoArray[i] = %s.mapTo%s(%sArray[i]);\n", dtoClassName + "Mapper", dtoClassName, oldClassName);


            writer.println("        }");
            writer.println("        return dtoArray;");
            writer.println("    }");

            // Reverse mapper: DTO to Entity (Single Object)
            writer.printf("    public static %s mapTo%s(%s dto) {%n", oldClassName, oldClassName, dtoClassName);
            writer.printf("        //Every Entity has to have a default constructor%n");
            writer.printf("        %s entity = new %s();%n%n", oldClassName, oldClassName);

            if (!fields.isEmpty()) {

                writer.println("        try {");
                writer.println();

                for (Element field : fields) {
                    String fieldName = field.getSimpleName().toString();
                    String fieldNameDTO = field.getSimpleName().toString();

                    if (field.getAnnotation(DTOName.class) != null) {
                        DTOName dtoName = field.getAnnotation(DTOName.class);
                        fieldNameDTO = dtoName.name();
                    }

                    writer.printf("            Field %sField = %s.class.getDeclaredField(\"%s\");%n", fieldName, dtoClassName, fieldName);
                    writer.printf("            %sField.setAccessible(true);%n", fieldName);
                    writer.printf("            Object %sValue = %sField.get(dto);%n%n", fieldName, fieldName);

                    writer.printf("            Field %sEntityField = %s.class.getDeclaredField(\"%s\");%n", fieldNameDTO, oldClassName, fieldNameDTO);
                    writer.printf("            %sEntityField.setAccessible(true);%n", fieldNameDTO);
                    writer.printf("            %sEntityField.set(entity, %sValue);%n%n", fieldNameDTO, fieldName);
                }

                writer.println("        } catch (NoSuchFieldException | IllegalAccessException e) {");
                writer.printf("            throw new RuntimeException(\"cant access fields from %s\" + e);%n", dtoClassName);
                writer.println("        }");
            }
            writer.println();
            writer.println("        return entity;");
            writer.println("    }\n");

            // Reverse mapper: DTO List to Entity List
            writer.printf("    public static List<%s> mapTo%s(List<%s> dtoList) {%n", oldClassName, oldClassName, dtoClassName);
            writer.printf("        List<%s> entityList = new ArrayList<>();%n%n", oldClassName);
            writer.println("        dtoList.forEach(dto -> {");
            writer.printf("            entityList.add(%s.mapTo%s(dto));\n", dtoClassName + "Mapper", oldClassName);

            writer.println("        });");
            writer.println("        return entityList;");
            writer.println("    }\n");

            // Reverse mapper: DTO Array to Entity Array
            writer.printf("    public static %s[] mapTo%s(%s[] dtoArray) {%n", oldClassName, oldClassName, dtoClassName);
            writer.printf("        %s[] entityArray = new %s[dtoArray.length];%n", oldClassName, oldClassName);
            writer.println("        for (int i = 0; i < dtoArray.length; i++) {");

            writer.printf("            entityArray[i]= %s.mapTo%s(dtoArray[i]);\n", dtoClassName + "Mapper", oldClassName);

            writer.println("        }");
            writer.println("        return entityArray;");
            writer.println("    }\n");
            writer.println("}");

        } catch (IOException e) {
            throw new RuntimeException("Error creating new Java File for " + oldClassName + ": " + e);
        }
    }

}
