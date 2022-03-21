package pro.greendata.rugrammartools.impl.dictionaries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A plain dictionary that contains only flat {@code Set}s.
 * <p>
 * Created by @ssz on 28.02.2022.
 */
public class PlainDictionary {
    public static final Set<String> FEMALE_NAMES = load("/female-names.txt");
    public static final Set<String> MALE_NAMES = load("/male-names.txt");
    public static final Set<String> ABBREVIATIONS = load("/abbreviations.txt");
    // collection of substantive feminine nouns that look like adjectives
    // (субстантивные существительные женского рода, которые выглядят как прилагательные)
    public static final Collection<String> FEMININE_SUBSTANTIVAT_NOUNS = load("/female-substantives.txt");
    // collection of substantive masculine nouns that look like adjectives
    // (субстантивные существительные мужского рода, которые выглядят как прилагательные)
    public static final Collection<String> MASCULINE_SUBSTANTIVAT_NOUNS = load("/male-substantives.txt");

    public static Set<String> load(String resource) {
        try (InputStream in = Objects.requireNonNull(PlainDictionary.class.getResourceAsStream(resource));
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             Stream<String> stream = reader.lines()) {
            return Set.of(stream.filter(x -> !skip(x)).map(String::trim).toArray(String[]::new));
        } catch (IOException e) {
            throw new IllegalStateException("Can't load " + resource, e);
        }
    }

    private static boolean skip(String s) {
        return s.isBlank() || s.startsWith("#");
    }
}
