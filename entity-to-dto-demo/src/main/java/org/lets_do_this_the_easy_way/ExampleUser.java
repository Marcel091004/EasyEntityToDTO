package org.lets_do_this_the_easy_way;


import org.lets_do_this_the_easy_way.annotations.*;

@ToDTO(className = "UserDTO")
@DTOExtraFields({
        @DTOExtraField(name = "isAdmin", type = "boolean", defaultValue = "false"),
        @DTOExtraField(name = "displayName", type = "String")
})
public class ExampleUser {

    @DTOName(name = "Username")
    private String username;

    @DTOName(name = "age_in_years")
    private int age;

    @ExcludeFromDTO
    private String password;

    public ExampleUser(String username, int age, String password) {
        this.username = username;
        this.age = age;
        this.password = password;
    }

    public ExampleUser() {
    }
}
