package io.github.cselagea.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(final Person person) {
        final String firstName = person.getFirstName();
        final String lastName = person.getLastName();

        final Person transformedPerson = new Person(firstName.toUpperCase(), lastName.toUpperCase());

        log.info("Converting {} into {}", person, transformedPerson);

        return transformedPerson;
    }

}
