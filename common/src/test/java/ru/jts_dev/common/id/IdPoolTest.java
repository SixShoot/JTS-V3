package ru.jts_dev.common.id;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * @author Java-man
 * @since 24.01.2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BitSetIdPool.class})
public class IdPoolTest {
    @Autowired
    private IdPool idPool;

    @Test
    public void testIdBorrow() throws Exception {
        IntStream.rangeClosed(0, 10_000).forEach(value -> {
            int id = idPool.borrow();
            assertThat(id, equalTo(value));
        });

        IntStream.rangeClosed(0, 10_000).parallel().forEach(value -> {
            int id = idPool.borrow();
            assertThat(id, greaterThanOrEqualTo(10_000));
        });
    }

    @Test
    public void testIdRelease() throws Exception {
        IntStream.rangeClosed(0, 20_000).parallel().forEach(value -> {
            idPool.release(value);
        });
    }
}