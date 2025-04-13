package org.lets_do_this_the_easy_way;

@ToDTO
public class ExampleUser {

    private String username;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private int age;

    @ExcludeFromDTO
    private String password;

    public ExampleUser(String username, int age, String password) {
        this.username = username;
        this.age = age;
        this.password = password;
    }
}
