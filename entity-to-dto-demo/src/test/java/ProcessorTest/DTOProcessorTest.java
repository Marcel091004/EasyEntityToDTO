package ProcessorTest;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.lets_do_this_the_easy_way.ExampleUser;
import org.lets_do_this_the_easy_way.ExampleUserDTO;
import org.lets_do_this_the_easy_way.ExampleUserDTOMapper;
import org.lets_do_this_the_easy_way.codeGeneration.DTOProcessor;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class DTOProcessorTest {

    @Test
    void shouldGenerateDTOAndMapper() {
        JavaFileObject input = JavaFileObjects.forSourceString("example.User", """
            package example;

            import org.lets_do_this_the_easy_way.annotations.*;

            @ToDTO
            @DTOExtraFields({
                            @DTOExtraField(name = "isCool", type = "boolean", defaultValue = "true"),
                            @DTOExtraField(name = "full_name", type = "String")
                    })
            public class User {
     \s
                @DTOName(name = "Username")
                public String name;
               \s
                @DTOName(name = "age_in_years")
                public int age;
               \s
                @ExcludeFromDTO
                private String privateStuff;
            }
       \s""");

        Compilation compilation = Compiler.javac()
                .withProcessors(new DTOProcessor())
                .compile(input);

        assertThat(compilation).succeeded();

        //The DTO
        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .contains("public class UserDTO");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .contains("private java.lang.String Username;");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String name;");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .contains("private int age_in_years;");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .doesNotContain("private int age;");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String privateStuff;");


        //Mapper
        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public class UserDTOMapper");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static UserDTO mapToUserDTO(User User)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static List<UserDTO> mapToUserDTO(List<User> UserList)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static UserDTO[] mapToUserDTO(User[] UserArray)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static User mapToUser(UserDTO dto)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static List<User> mapToUser(List<UserDTO> dtoList)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static User[] mapToUser(UserDTO[] dtoArray)");
    }

    @Test
    void shouldNotGenerateDTOAndMapperWhenNoFields() {
        JavaFileObject input = JavaFileObjects.forSourceString("example.User", """
                       package example;
                
                       import org.lets_do_this_the_easy_way.annotations.*;
                
                       @ToDTO
                       public class User {}
                  \s""");

        Compilation compilation = Compiler.javac()
                .withProcessors(new DTOProcessor())
                .compile(input);

        assertThat(compilation).failed();

    }

    @Test
    void shouldGenerateDTOAndMapperWhenDTOExtraFieldsExist() {
        JavaFileObject input = JavaFileObjects.forSourceString("example.User", """
                       package example;
                
                       import org.lets_do_this_the_easy_way.annotations.*;
                
                       @ToDTO
                       @DTOExtraFields({
                             @DTOExtraField(name = "name", type = "String")
                     })
                       public class User {}
                  \s""");

        Compilation compilation = Compiler.javac()
                .withProcessors(new DTOProcessor())
                .compile(input);

        assertThat(compilation).succeeded();

        //The DTO
        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .contains("public class UserDTO");

        //Mapper
        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public class UserDTOMapper");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static UserDTO mapToUserDTO(User User)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static List<UserDTO> mapToUserDTO(List<User> UserList)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static UserDTO[] mapToUserDTO(User[] UserArray)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static User mapToUser(UserDTO dto)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static List<User> mapToUser(List<UserDTO> dtoList)");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTOMapper")
                .contentsAsUtf8String()
                .contains("public static User[] mapToUser(UserDTO[] dtoArray)");

    }

    @Test
    void testGeneratedMapperAtRuntime() {
        ExampleUser John = new ExampleUser("John Doe", 25, "very secure password");

        ExampleUserDTO JohnDTO = ExampleUserDTOMapper.mapToExampleUserDTO(John);

        Assertions.assertEquals("John Doe", JohnDTO.getUsername());
        Assertions.assertEquals(25, JohnDTO.getAge_in_years());
    }


}
