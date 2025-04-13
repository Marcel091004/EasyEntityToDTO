package org.lets_do_this_the_easy_way;

@ToDTO
public class ExampleUser {

    private String username;
    private int age;

    @ExcludeFromDTO
    private String password;

    public ExampleUser(String username, int age, String password) {
        this.username = username;
        this.age = age;
        this.password = password;
    }
}
