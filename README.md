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
    <version>v1.0.0</version>
</dependency>

## 🚀 Quick Start Example

Here's how you can eliminate boilerplate and generate a DTO in seconds:

### ✍️ Step 1: Annotate your entity

@ToDTO // 👈 Automatically generates a DTO and Mapper
public class ExampleUser {

    private String username;
    private int age;

    @ExcludeFromDTO // 🔒 Won't appear in the DTO
    private String password;

    public ExampleUser(String username, int age, String password) {
        this.username = username;
        this.age = age;
        this.password = password;
    }

    // These are needed for the mapper to work
    public String getUsername() { return username; }
    public int getAge() { return age; }
}

### 🔁 Step 2: Use the generated Mapper

```java
ExampleUser john = new ExampleUser("John Doe", 25, "secret123");

ExampleUserDTO dto = ExampleUserDTOMapper.mapToExampleUserDTO(john);

System.out.println(dto.getUsername()); // → John Doe
System.out.println(dto.getAge());      // → 25


---

💎 Why Use This?

✨ Less boilerplate

🧼 Clean and simple annotations

⚡ Works out of the box, no setup

🚀 Fast and lightweight


🙌 Contributing
Have an idea? Found a bug? Feel free to open an issue or a pull request!

