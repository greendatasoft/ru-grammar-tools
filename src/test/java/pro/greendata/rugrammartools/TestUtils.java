package pro.greendata.rugrammartools;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by @ssz on 01.12.2020.
 */
class TestUtils {

    static boolean equalsIgnoreSpecial(String expected, String actual) {
        if (expected.length() != actual.length()) {
            return false;
        }
        char[] ex = expected.toCharArray();
        char[] ac = expected.toCharArray();
        for (int i = 0; i < ex.length; i++) {
            if (ex[i] == ac[i]) {
                continue;
            }
            if (isSpecial(ex[i]) && isSpecial(ac[i])) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean isSpecial(char c) {
        return c == 'ё' || c == 'е';
    }

    static Path file(String fileName) {
        try {
            return Paths.get(Objects.requireNonNull(TestUtils.class.getResource("/" + fileName)).toURI());
        } catch (URISyntaxException e) {
            return Assertions.fail(e);
        }
    }

    static List<String[]> load(String fileName) {
        try (Stream<String> lines = Files.lines(file(fileName))) {
            return lines
                    .map(s -> s.replaceFirst("([^#]*)#.+", "$1").trim())
                    .filter(s -> !s.isEmpty())
                    .map(x -> x.split(",\\s*"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Assertions.fail(e);
        }
    }

}
