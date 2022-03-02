package com.gitlab.sszuev.inflector.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public static Set<String> load(String resource) {
        URL url = Objects.requireNonNull(PlainDictionary.class.getResource(resource));
        try (Stream<String> stream = Files.lines(Paths.get(url.toURI()))) {
            return Set.of(stream.toArray(String[]::new));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Can't load " + resource, e);
        }
    }
}
