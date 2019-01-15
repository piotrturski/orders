package net.piotrturski.infra;

import org.springframework.lang.NonNull;

import java.util.stream.Stream;

public class JavaInterop {

    public static Class javaCrossCompilation = KotlinCrossCompilation.class;

    public Stream<String> java_11() {
        var a = 7;
        return Stream.of("")
                .map((@NonNull var s) -> a + s.toLowerCase());

    }
}
