package org.lets_do_this_the_easy_way;


import org.lets_do_this_the_easy_way.annotations.ExcludeFromDTO;
import org.lets_do_this_the_easy_way.annotations.ToDTO;

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
