package net.piotrturski.infra;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class Junit5Test {


    @Test
    public void test5() {
        assertThat(false).isFalse();
    }
}