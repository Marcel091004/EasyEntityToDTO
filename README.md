# EasyEntityToDTO

A lightweight annotation processor to automatically generate DTOs and DTO mappers from your Java entities with zero boilerplate.

## 🔧 Installation

Using [JitPack](https://jitpack.io):

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
<groupId>com.github.Marcel091004</groupId>
<artifactId>EasyEntityToDTO</artifactId>
<version>v1.2.0</version>
</dependency>
```

## 🚀 Quick Start Example

Here's how you can eliminate boilerplate and generate a DTO in seconds:

### ✍️ Step 1: Annotate your entity
```java
@ToDTO // 👈 Automatically generates a DTO and Mapper
@DTOExtraFields({ // ➕ Add custom fields to your DTO
        @DTOExtraField(name = "isAdmin", type = "boolean", defaultValue = "false"),
        @DTOExtraField(name = "displayName", type = "String")
})
public class ExampleUser {

    private String username;

    @DTOName(name = "age_in_years") // Change the name in the DTO to whatever you like :)
    private int age;

    @ExcludeFromDTO // 🔒 Won't appear in the DTO
    private String password;

    public ExampleUser(String username, int age, String password) {
        this.username = username;
        this.age = age;
        this.password = password;
    }

    public ExampleUser() {} // Default constructor is required for mapper instantiation 

}

```
🛠️ What Gets Generated?
```
public class ExampleUserDTO {
    private String username;
    private int age_in_years;              // ← field was renamed from Entity 
    private boolean isAdmin = false;       // ← custom field with default
    private String displayName;            // ← custom field
}

```


### 🔁 Step 2: Use the generated Mapper

```java
ExampleUser john = new ExampleUser("John Doe", 25, "secret123");

ExampleUserDTO dto = ExampleUserDTOMapper.mapToExampleUserDTO(john);

System.out.println(dto.getUsername());   // → John Doe
        System.out.println(dto.getAge());        // → 25
        System.out.println(dto.isAdmin());       // → false
        System.out.println(dto.getDisplayName()); // → null

// OR

ExampleUser user1 = new ExampleUser("alice", 30, "secret123");
ExampleUser user2 = new ExampleUser("bob", 25, "hunter2");

List<ExampleUser> usersList = new ArrayList<>();
        usersList.add(user1);
        usersList.add(user2);

List<ExampleUserDTO> dtosList = ExampleUserDTOMapper.mapToExampleUserDTO(usersList);
```
## 🎯 Features

- ✅ Automatic DTO generation
- ✅ Automatic Mapper generation
- ✅ Exclude fields with `@ExcludeFromDTO`
- ✅ Add virtual fields with `@DTOExtraFields` — even if they don’t exist in the original class!
- ✅ Set default values for extra fields
- ✅ Clean output with no boilerplate
- ✅ Fast compile-time processing using Java Annotation Processing (APT)


## 💎 Why Use This?

- ✨ Less boilerplate
- 🧼 Clean and simple annotations
- ⚡ Works out of the box
- 🚀 Fast and lightweight
- 🔮 Supports both real and custom fields via reflection

📦 Output File Location
Generated files will be located in the `target/generated-sources/annotations` directory (or your IDE’s generated sources folder).

🙌 Contributing
Have an idea? Found a bug? Want to add features?
Feel free to open an issue or a pull request — contributions welcome!
