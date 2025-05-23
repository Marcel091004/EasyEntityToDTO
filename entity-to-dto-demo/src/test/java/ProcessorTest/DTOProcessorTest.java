package ProcessorTest;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    void shouldNotGenerateDTOAndMapperWhenDTOExtraFieldsContainNonExistingTypes() {
        JavaFileObject input = JavaFileObjects.forSourceString("example.User", """
                     package example;
                
                     import org.lets_do_this_the_easy_way.annotations.*;
                
                     @ToDTO
                     @DTOExtraFields({
                           @DTOExtraField(name = "isAdmin", type = "crazy", defaultValue = "hedqhdq"),
                           @DTOExtraField(name = "class", type = "String")
                   })
                     public class User {}
                \s""");

        Compilation compilation = Compiler.javac()
                .withProcessors(new DTOProcessor())
                .compile(input);

        assertThat(compilation).failed();

    }

    @Test
    void shouldNotGenerateDTOAndMapperFieldWhenFieldIsAnnotatedTwice() {
        JavaFileObject input = JavaFileObjects.forSourceString("example.User", """
                        package example;
                \s
                        import org.lets_do_this_the_easy_way.annotations.*;
                \s
                        @ToDTO
                        public class User {
                       \s
                        private String name;
                       \s
                        @DTOName(name = "secret")
                        @ExcludeFromDTO
                        String privateStuff;
                       \s
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
                .contains("private java.lang.String name;");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String privateStuff;");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String secret;");
    }


    @Test
    void shouldGenerateDTOAndMapperFieldEvenForInnerClass() {
        JavaFileObject input = JavaFileObjects.forSourceString("example.User", """
                        package example;
                \s
                        import org.lets_do_this_the_easy_way.annotations.*;
                \s
                        @ToDTO
                        public class User {
                       \s
                        private String name;
                       \s
                        }
                       \s
                        @ToDTO
                        class User2 {
                       \s
                        String name;
                        }
                   \s""");

        Compilation compilation = Compiler.javac()
                .withProcessors(new DTOProcessor())
                .compile(input);

        assertThat(compilation).succeeded();

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .contains("public class UserDTO");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .contains("private java.lang.String name;");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String privateStuff;");

        assertThat(compilation)
                .generatedSourceFile("example.UserDTO")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String secret;");

        assertThat(compilation)
                .generatedSourceFile("example.User2DTO")
                .contentsAsUtf8String()
                .contains("public class User2DTO");

        assertThat(compilation)
                .generatedSourceFile("example.User2DTO")
                .contentsAsUtf8String()
                .contains("private java.lang.String name;");

    }

    @Test
    void testGeneratedMapperAtRuntime() {
        TestPet dog = new TestPet();

        dog.name = "Fini";
        dog.type = "Chihuahua";

        TestUser testUser = new TestUser();

        testUser.testPet = dog;
        testUser.name = "John Doe";
        testUser.testPetEmpty = new TestPetEmpty();

        TestUserDTO testUserDTO = TestUserDTOMapper.mapToTestUserDTO(testUser);

        Assertions.assertEquals("John Doe", testUserDTO.getName());
        Assertions.assertEquals("Fini", testUserDTO.getTestPet().name);
        Assertions.assertEquals("Chihuahua", testUserDTO.getTestPet().type);
        Assertions.assertEquals(testUser.testPetEmpty, testUserDTO.getTestPetEmpty());

        TestUser testUser2 = TestUserDTOMapper.mapToTestUser(testUserDTO);

        Assertions.assertEquals("John Doe", testUser2.name);
        Assertions.assertEquals("Fini", testUser2.testPet.name);
        Assertions.assertEquals("Chihuahua", testUser2.testPet.type);
        Assertions.assertEquals(testUser.testPetEmpty, testUser2.testPetEmpty);
    }


}
