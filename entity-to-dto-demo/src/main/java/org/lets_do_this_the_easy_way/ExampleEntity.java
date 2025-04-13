package org.lets_do_this_the_easy_way;


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


}
