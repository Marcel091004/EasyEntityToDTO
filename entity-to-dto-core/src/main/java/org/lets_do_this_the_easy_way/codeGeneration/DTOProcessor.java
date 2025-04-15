package org.lets_do_this_the_easy_way.codeGeneration;

import com.google.auto.service.AutoService;
import org.lets_do_this_the_easy_way.annotations.DTOName;
import org.lets_do_this_the_easy_way.annotations.ExcludeFromDTO;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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
            writer.println("        try {");
            writer.println();

            // Field reflection logic
            for (Element field : fields) {
                String fieldName = field.getSimpleName().toString();

                writer.printf("            Field %sfield = %s.class.getDeclaredField(\"%s\");%n", fieldName, oldClassName, fieldName);
                writer.printf("            %sfield.setAccessible(true);%n", fieldName);
                writer.printf("            Object %sValue = %sfield.get(%s);%n%n", fieldName, fieldName, oldClassName);

                writer.printf("            Field %sDTO = %s.class.getDeclaredField(\"%s\");%n", fieldName, dtoClassName, fieldName);
                writer.printf("            %sDTO.setAccessible(true);%n", fieldName);
                writer.printf("            %sDTO.set(dto, %sValue);%n%n", fieldName, fieldName);
            }

            // Catch block and return
            writer.println("        } catch (NoSuchFieldException | IllegalAccessException e) {");
            writer.printf("            throw new RuntimeException(\"cant access fields from %s\" + e);%n", oldClassName);
            writer.println("        }");
            writer.println();
            writer.println("        return dto;");
            writer.println("    }");

            //Next mapper
            writer.printf("    public static List<%s> mapTo%s(List<%s> %sList) {%n", dtoClassName, dtoClassName, oldClassName, oldClassName);
            writer.printf("        List<%s> dtoList =  new ArrayList<>();%n%n", dtoClassName);
            writer.printf("        %sList.forEach(entity ->{%n%n", oldClassName);
            writer.println("        try {");
            writer.printf("             %s dto = new %s();%n", dtoClassName, dtoClassName);
            writer.println();

            // Field reflection logic
            for (Element field : fields) {
                String fieldName = field.getSimpleName().toString();

                if (element.getAnnotation(DTOName.class) != null) {
                    DTOName dtoName = element.getAnnotation(DTOName.class);
                    fieldName = dtoName.name();
                }

                writer.printf("            Field %sfield = %s.class.getDeclaredField(\"%s\");%n", fieldName, oldClassName, fieldName);
                writer.printf("            %sfield.setAccessible(true);%n", fieldName);
                writer.printf("            Object %sValue = %sfield.get(entity);%n%n", fieldName, fieldName);

                writer.printf("            Field %sDTO = %s.class.getDeclaredField(\"%s\");%n", fieldName, dtoClassName, fieldName);
                writer.printf("            %sDTO.setAccessible(true);%n", fieldName);
                writer.printf("            %sDTO.set(dto, %sValue);%n%n", fieldName, fieldName);
            }
            writer.println("            dtoList.add(dto);");

            // Catch block and return
            writer.println("             } catch (NoSuchFieldException | IllegalAccessException e) {");
            writer.printf("                throw new RuntimeException(\"cant access fields from %s\" + e);%n", oldClassName);
            writer.println("            }");
            writer.println("        });");
            writer.println("        return dtoList;");
            writer.println("    }\n");

            //Next mapper
            writer.printf("    public static %s[] mapTo%s(%s[] %sArray) {%n", dtoClassName, dtoClassName, oldClassName, oldClassName);
            writer.printf("        %s[] dtoArray = new %s[%sArray.length];%n", dtoClassName, dtoClassName, oldClassName);
            writer.printf("       for(int i = 0; i < dtoArray.length; i++) {%n%n");
            writer.println("        try {");
            writer.printf("             %s dto = new %s();%n", dtoClassName, dtoClassName);
            writer.println();

            // Field reflection logic
            for (Element field : fields) {
                String fieldName = field.getSimpleName().toString();

                writer.printf("            Field %sfield = %s.class.getDeclaredField(\"%s\");%n", fieldName, oldClassName, fieldName);
                writer.printf("            %sfield.setAccessible(true);%n", fieldName);
                writer.printf("            Object %sValue = %sfield.get(%sArray[i]);%n%n", fieldName, fieldName, oldClassName);

                writer.printf("            Field %sDTO = %s.class.getDeclaredField(\"%s\");%n", fieldName, dtoClassName, fieldName);
                writer.printf("            %sDTO.setAccessible(true);%n", fieldName);
                writer.printf("            %sDTO.set(dto, %sValue);%n%n", fieldName, fieldName);
            }
            writer.println("            dtoArray[i] = dto;");

            // Catch block and return
            writer.println("             } catch (NoSuchFieldException | IllegalAccessException e) {");
            writer.printf("                throw new RuntimeException(\"cant access fields from %s\" + e);%n", oldClassName);
            writer.println("            }");
            writer.println("        }");
            writer.println("        return dtoArray;");
            writer.println("    }");

            // Reverse mapper: DTO to Entity (Single Object)
            writer.printf("    public static %s mapTo%s(%s dto) {%n", oldClassName, oldClassName, dtoClassName);
            writer.printf("        //Every Entity has to have a default constructor%n");
            writer.printf("        %s entity = new %s();%n%n", oldClassName, oldClassName);
            writer.println("        try {");
            writer.println();

            for (Element field : fields) {
                String fieldName = field.getSimpleName().toString();

                writer.printf("            Field %sField = %s.class.getDeclaredField(\"%s\");%n", fieldName, dtoClassName, fieldName);
                writer.printf("            %sField.setAccessible(true);%n", fieldName);
                writer.printf("            Object %sValue = %sField.get(dto);%n%n", fieldName, fieldName);

                writer.printf("            Field %sEntityField = %s.class.getDeclaredField(\"%s\");%n", fieldName, oldClassName, fieldName);
                writer.printf("            %sEntityField.setAccessible(true);%n", fieldName);
                writer.printf("            %sEntityField.set(entity, %sValue);%n%n", fieldName, fieldName);
            }

            writer.println("        } catch (NoSuchFieldException | IllegalAccessException e) {");
            writer.printf("            throw new RuntimeException(\"cant access fields from %s\" + e);%n", dtoClassName);
            writer.println("        }");
            writer.println();
            writer.println("        return entity;");
            writer.println("    }\n");

            // Reverse mapper: DTO List to Entity List
            writer.printf("    public static List<%s> mapTo%s(List<%s> dtoList) {%n", oldClassName, oldClassName, dtoClassName);
            writer.printf("        List<%s> entityList = new ArrayList<>();%n%n", oldClassName);
            writer.println("        dtoList.forEach(dto -> {");
            writer.println("            try {");
            writer.printf("                //Every Entity has to have a default constructor%n");
            writer.printf("                %s entity = new %s();%n", oldClassName, oldClassName);
            writer.println();

            for (Element field : fields) {
                String fieldName = field.getSimpleName().toString();

                writer.printf("                Field %sField = %s.class.getDeclaredField(\"%s\");%n", fieldName, dtoClassName, fieldName);
                writer.printf("                %sField.setAccessible(true);%n", fieldName);
                writer.printf("                Object %sValue = %sField.get(dto);%n%n", fieldName, fieldName);

                writer.printf("                Field %sEntityField = %s.class.getDeclaredField(\"%s\");%n", fieldName, oldClassName, fieldName);
                writer.printf("                %sEntityField.setAccessible(true);%n", fieldName);
                writer.printf("                %sEntityField.set(entity, %sValue);%n%n", fieldName, fieldName);
            }

            writer.println("                entityList.add(entity);");
            writer.println("            } catch (NoSuchFieldException | IllegalAccessException e) {");
            writer.printf("                throw new RuntimeException(\"cant access fields from %s\" + e);%n", dtoClassName);
            writer.println("            }");
            writer.println("        });");
            writer.println("        return entityList;");
            writer.println("    }\n");

            // Reverse mapper: DTO Array to Entity Array
            writer.printf("    public static %s[] mapTo%s(%s[] dtoArray) {%n", oldClassName, oldClassName, dtoClassName);
            writer.printf("        %s[] entityArray = new %s[dtoArray.length];%n", oldClassName, oldClassName);
            writer.println("        for (int i = 0; i < dtoArray.length; i++) {");
            writer.println("            try {");
            writer.printf("                //Every Entity has to have a default constructor%n");
            writer.printf("                %s entity = new %s();%n", oldClassName, oldClassName);
            writer.println();

            for (Element field : fields) {
                String fieldName = field.getSimpleName().toString();

                writer.printf("                Field %sField = %s.class.getDeclaredField(\"%s\");%n", fieldName, dtoClassName, fieldName);
                writer.printf("                %sField.setAccessible(true);%n", fieldName);
                writer.printf("                Object %sValue = %sField.get(dtoArray[i]);%n%n", fieldName, fieldName);

                writer.printf("                Field %sEntityField = %s.class.getDeclaredField(\"%s\");%n", fieldName, oldClassName, fieldName);
                writer.printf("                %sEntityField.setAccessible(true);%n", fieldName);
                writer.printf("                %sEntityField.set(entity, %sValue);%n%n", fieldName, fieldName);
            }

            writer.println("                entityArray[i] = entity;");
            writer.println("            } catch (NoSuchFieldException | IllegalAccessException e) {");
            writer.printf("                throw new RuntimeException(\"cant access fields from %s\" + e);%n", dtoClassName);
            writer.println("            }");
            writer.println("        }");
            writer.println("        return entityArray;");
            writer.println("    }\n");
            writer.println("}");

        } catch (IOException e) {
            throw new RuntimeException("Error creating new Java File for " + oldClassName + ": " + e);
        }
    }

}
