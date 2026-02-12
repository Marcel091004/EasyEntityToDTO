package ProcessorTesting;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;
import org.lets_do_this_the_easy_way.codeGeneration.DTOProcessor;
import org.lets_do_this_the_easy_way.codeGeneration.MultiSourceDTOProcessor;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class DTOProcessorTest {

    @Test
    public void shouldGenerateDTOAndMapper() {
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
    public void shouldGenerateDTOAndMapperWithRename() {
        JavaFileObject input = JavaFileObjects.forSourceString("example.User", """
                       package example;
                
                       import org.lets_do_this_the_easy_way.annotations.*;
                
                       @ToDTO(className = "CoolerUser")
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
                .generatedSourceFile("example.CoolerUser")
                .contentsAsUtf8String()
                .contains("public class CoolerUser");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUser")
                .contentsAsUtf8String()
                .contains("private java.lang.String Username;");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUser")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String name;");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUser")
                .contentsAsUtf8String()
                .contains("private int age_in_years;");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUser")
                .contentsAsUtf8String()
                .doesNotContain("private int age;");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUser")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String privateStuff;");


        //Mapper
        assertThat(compilation)
                .generatedSourceFile("example.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public class CoolerUserMapper");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public static CoolerUser mapToCoolerUser(User User)");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public static List<CoolerUser> mapToCoolerUser(List<User> UserList)");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public static CoolerUser[] mapToCoolerUser(User[] UserArray)");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public static User mapToUser(CoolerUser dto)");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public static List<User> mapToUser(List<CoolerUser> dtoList)");

        assertThat(compilation)
                .generatedSourceFile("example.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public static User[] mapToUser(CoolerUser[] dtoArray)");
    }

    @Test
    public void shouldNotGenerateDTOAndMapperWhenNoFields() {
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
    public void shouldGenerateDTOAndMapperWhenDTOExtraFieldsExist() {
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
    public void shouldNotGenerateDTOAndMapperWhenDTOExtraFieldsContainNonExistingTypes() {
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
    public void shouldNotGenerateDTOAndMapperFieldWhenFieldIsAnnotatedTwice() {
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
    public void shouldGenerateDTOAndMapperFieldEvenForInnerClass() {
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
    public void shouldGenerateDTOAndMapperForMultiSourceDTO() {
        JavaFileObject input = JavaFileObjects.forSourceString("example.User", """
                       package example;
                
                       import org.lets_do_this_the_easy_way.annotations.*;
                
                       @ToMultiSourceDTO(identifierName = "CoolerUser")
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

        JavaFileObject input2 = JavaFileObjects.forSourceString("example.House", """
                       package example;
                
                       import org.lets_do_this_the_easy_way.annotations.*;
                
                       @ToMultiSourceDTO(identifierName = "CoolerUser")
                       @DTOExtraFields({
                                       @DTOExtraField(name = "isHouse", type = "boolean", defaultValue = "true"),
                                       @DTOExtraField(name = "address", type = "String", defaultValue = "Middle of nowhere"),
                                        @DTOExtraField(name = "color", type = "String")
                               })
                       public class House {
                \s
                           @DTOName(name = "house_age")
                           public int age_in_years;
                          \s
                           @ExcludeFromDTO
                           private String privateStuff;
                       }
                  \s""");

        Compilation compilation = Compiler.javac()
                .withProcessors(new MultiSourceDTOProcessor(), new DTOProcessor())
                .compile(input, input2);

        assertThat(compilation).succeeded();

        //The DTO
        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .contains("public class CoolerUser");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .contains("private java.lang.String Username;");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String name;");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .contains("private int age_in_years;");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .doesNotContain("private int age;");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .doesNotContain("private java.lang.String privateStuff;");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .contains("private boolean isHouse = true;");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .contains("String address = \"Middle of nowhere\";");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .contains("String color = null;");

        assertThat(compilation)
                .generatedSourceFile("CoolerUser")
                .contentsAsUtf8String()
                .contains("int house_age;");

        //Mapper
        assertThat(compilation)
                .generatedSourceFile("org.lets_do_this_the_easy_way.generated.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public class CoolerUserMapper");

        assertThat(compilation)
                .generatedSourceFile("org.lets_do_this_the_easy_way.generated.CoolerUserMapper")
                .contentsAsUtf8String()
                .contains("public static CoolerUser mapToCoolerUser(House house, User user)");

    }


}
