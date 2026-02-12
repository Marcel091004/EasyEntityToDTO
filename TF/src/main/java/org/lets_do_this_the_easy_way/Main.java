package org.lets_do_this_the_easy_way;

import org.lets_do_this_the_easy_way.generated.SuperSergey;
import org.lets_do_this_the_easy_way.generated.SuperSergeyMapper;
import org.lets_do_this_the_easy_way.generated.United;
import org.lets_do_this_the_easy_way.generated.UnitedMapper;

public class Main {
    public static void main(String[] args) {

        Car car = new Car();
        car.horsePower = 100;

        User user = new User();
        user.name = "John Doe";

        House house = new House();
            house.address = "123 Main St";

        United united = UnitedMapper.mapToUnited(car,house, user);
        System.out.println(united.getHorsePower());
        System.out.println(united.getName());
        System.out.println(united.getAddress());

        Sergey sergey = new Sergey();
        sergey.age = 20;


        SuperSergey superSergey = SuperSergeyMapper.mapToSuperSergey(sergey);

        System.out.println(superSergey.getAge());

        Testing testing = new Testing();
        testing.name = "John Doe";

        TestingDTO testingDTO = TestingDTOMapper.mapToTestingDTO(testing);

        System.out.println(testingDTO.getName());
        System.out.println(testingDTO.getCountry());

    }
}