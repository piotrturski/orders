package net.piotrturski.shop.order;

import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class JavaSrc {

    public static BigDecimal ZERO = BigDecimal.ZERO;

    public static Class c = ExceptionMapper.class;

    public Stream<String> java11() {
        var a = 7;
        return Stream.of("")
                .map((@NonNull var s) -> a + s.toLowerCase());

    }
}
