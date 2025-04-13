package org.example;


import org.test.ExcludeFromDTOMapper;
import org.test.ToDTO;

@ToDTO
public class ExampleEntity {

    public String name;
    public int age;

    @ExcludeFromDTOMapper
    public double salary;

    public ExampleEntity(String name, int age, double salary) {
        this.name = name;
        this.age = age;
        this.salary = salary;
    }

    //Empty Constructor is needed during new Object Generation
    public ExampleEntity() {
    }

}
