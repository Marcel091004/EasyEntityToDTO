package org.example;


public class Main {
    public static void main(String[] args) {

        ExampleEntity example = new ExampleEntity("John Doe", 25, 100000.0);

        DTOMapper<ExampleEntity> mapper = new DTOMapper<>();

        ExampleEntity mappedExample = mapper.maptoDTO(example);

        System.out.println(mappedExample.name);
        System.out.println(mappedExample.age);
        System.out.println(mappedExample.salary);
    }
}