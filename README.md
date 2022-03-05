## Ru-Grammar-Tools (a set of tools to work with russian grammar)

#### A Java library for spelling and case-inflecting according to the rules of Russian declension cases.

#### It is suitable for declining of the Russian full-names (Surname Firstname Patronymic), official job-titles and organization names, numerals and for spelling numbers.

Based of [petrovich-rules](https://github.com/petrovich) and on built-in
OpenRussian [dictionary](https://github.com/Badestrand/russian-dictionary).
Supports for declensions of names, general terms (job-titles, legal organization names) and numerics. 
The library contains 1000+ test-cases.

#### Examples:

```java
GrammarTools.getInflectionEngine().inflect("Петрова", WordType.FAMILY_NAME, Case.PREPOSITIONAL, Gender.FEMALE, true, false);
GrammarTools.getInflectionEngine().inflectFullName("Петров Петр Петрович", Case.DATIVE);
GrammarTools.getInflectionEngine().inflectRegularTerm("Общество с ограниченной ответственностью Бёрнинг Мэн", Case.ACCUSATIVE, false);
GrammarTools.getInflectionEngine().inflectRegularTerm("Вентилевой гидравлического пресса", Case.ACCUSATIVE, true);
GrammarTools.getInflectionEngine().inflectNumeral("сорок два", "рубль", Case.INSTRUMENTAL);
GrammarTools.getSpellingEngine().spell(42.42);
GrammarTools.getSpellingEngine().spellOrdinal(42, Gender.NEUTER);
```

#### Related links:

- Petrovich online service for names: https://petrovich.nlpub.ru/
- Petrovich rules: https://github.com/petrovich/petrovich-rules
- OpenRussian dictionary: https://github.com/Badestrand/russian-dictionary 
- Other petrovich java libraries: 
  * https://github.com/petrovich/petrovich-java
  * https://github.com/petrovich4j/petrovich4j


##### Requirements:

- Git
- Java **11+**
- Maven **3+**

##### License

* Apache License Version 2.0