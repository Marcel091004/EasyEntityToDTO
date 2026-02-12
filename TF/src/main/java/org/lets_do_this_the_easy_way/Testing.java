package org.lets_do_this_the_easy_way;

import org.lets_do_this_the_easy_way.annotations.DTOExtraField;
import org.lets_do_this_the_easy_way.annotations.DTOExtraFields;
import org.lets_do_this_the_easy_way.annotations.ToDTO;

@ToDTO
@DTOExtraFields(
        @DTOExtraField(name = "country", type = "String", defaultValue = "Germany")
)
public class Testing {
    String name;
}
