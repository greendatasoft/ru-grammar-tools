## Ru-Grammar-Tools (a set of tools for working with russian grammar)

#### This is a java library intended for case declension Russian full-names (Surname, Firstname, Patronymic), official job-titles, legal organization names, regular terms and numerals, and also for spelling cardinal and ordinal numbers.

It is based on [petrovich-rules](https://github.com/petrovich) and [OpenRussian dictionary](https://github.com/Badestrand/russian-dictionary). 
The library contains 1000+ test-cases.

#### Examples:

```java
GrammarTools.getInflectionEngine().inflectPatronymic("Петрович", Case.GENITIVE, Gender.MALE);
GrammarTools.getInflectionEngine().inflectSurname("Петрова", Case.PREPOSITIONAL, Gender.FEMALE);
GrammarTools.getInflectionEngine().inflectFullname("Петрова Петра Петровна", Case.DATIVE);
GrammarTools.getInflectionEngine().inflectNameOfOrganization("Общество с ограниченной ответственностью Бёрнинг Мэн", Case.ACCUSATIVE);
GrammarTools.getInflectionEngine().inflectNameOfProfession("Вентилевой гидравлического пресса", Case.ACCUSATIVE);
GrammarTools.getInflectionEngine().inflectNumeral("сорок два", "доллар", Case.INSTRUMENTAL);
GrammarTools.getSpellingEngine().spell(42.42);
GrammarTools.getSpellingEngine().spellOrdinal(42, Gender.NEUTER);
```

#### Related links:

- Petrovich online service for names: https://petrovich.nlpub.ru/
- OpenRussian online service: https://en.openrussian.org/
- Petrovich rules: https://github.com/petrovich/petrovich-rules
- OpenRussian dictionary: https://github.com/Badestrand/russian-dictionary 
- Other petrovich java libraries: 
  * https://github.com/petrovich/petrovich-java
  * https://github.com/petrovich4j/petrovich4j

#### How to use:
- https://jitpack.io/

##### Requirements:

- Git
- Java **11+**
- Maven **3+**

##### License

* Apache License Version 2.0