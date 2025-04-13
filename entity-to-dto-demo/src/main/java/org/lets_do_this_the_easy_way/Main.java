package org.lets_do_this_the_easy_way;


public class Main {
    public static void main(String[] args) {

        ExampleUser John = new ExampleUser("John Doe", 25, "very secure password");

        ExampleUserDTO JohnDTO = ExampleUserDTOMapper.mapToExampleUserDTO(John);

        System.out.println(JohnDTO.getUsername());
        System.out.println(JohnDTO.getAge());
    }
}