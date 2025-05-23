package ProcessorTest;

import org.lets_do_this_the_easy_way.annotations.ToDTO;

@ToDTO
public class TestUser {

    String name;
    TestPet testPet;
    TestPetEmpty testPetEmpty;
}
