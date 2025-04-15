package org.lets_do_this_the_easy_way;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        ExampleUser John = new ExampleUser("John Doe", 25, "very secure password");

        ExampleUserDTO JohnDTO = ExampleUserDTOMapper.mapToExampleUserDTO(John);

        System.out.println(JohnDTO.getUsername());
        System.out.println(JohnDTO.getAge_in_years());


        ExampleUser user1 = new ExampleUser("alice", 30, "secret123");
        ExampleUser user2 = new ExampleUser("bob", 25, "hunter2");

        List<ExampleUser> usersList = new ArrayList<>();
        usersList.add(user1);
        usersList.add(user2);

        List<ExampleUserDTO> dtosList = ExampleUserDTOMapper.mapToExampleUserDTO(usersList);

        dtosList.forEach(dto -> System.out.println("Username: " + dto.getUsername() + ", Age: " + dto.getAge_in_years()));



        ExampleUser[] usersArray = {
                new ExampleUser("John", 30, "secret123"),
                new ExampleUser("JOHN CENA", 25, "hunter2")
        };

        ExampleUserDTO[] dtosArray = ExampleUserDTOMapper.mapToExampleUserDTO(usersArray);

        Arrays.stream(dtosArray).forEach(dto ->
                System.out.println("Username: " + dto.getUsername() + ", Age: " + dto.getAge_in_years())
        );
    }
}