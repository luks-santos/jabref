package org.jabref.model.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibtexString;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.field.UnknownField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.event.EventListenerTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BibDatabaseTest {

    private BibDatabase database;
    private final BibtexString bibtexString = new BibtexString("DSP", "Digital Signal Processing");

    @BeforeEach
    void setUp() {
        database = new BibDatabase();
    }

    @Test
    void insertEntryAddsEntryToEntriesList() {
        BibEntry entry = new BibEntry();
        database.insertEntry(entry);
        assertEquals(1, database.getEntries().size());
        assertEquals(1, database.getEntryCount());
        assertEquals(entry, database.getEntries().getFirst());
    }

    @Test
    void containsEntryIdFindsEntry() {
        BibEntry entry = new BibEntry();
        assertFalse(database.containsEntryWithId(entry.getId()));
        database.insertEntry(entry);
        assertTrue(database.containsEntryWithId(entry.getId()));
    }

    @Test
    void insertEntryWithSameIdDoesNotThrowException() {
        BibEntry entry0 = new BibEntry();
        database.insertEntry(entry0);

        BibEntry entry1 = new BibEntry();
        entry1.setId(entry0.getId());
        database.insertEntry(entry1);
    }

    @Test
    void removeEntryRemovesEntryFromEntriesList() {
        BibEntry entry = new BibEntry();
        database.insertEntry(entry);

        database.removeEntry(entry);
        assertEquals(List.of(), database.getEntries());
        assertFalse(database.containsEntryWithId(entry.getId()));
    }

    @Test
    void removeSomeEntriesRemovesThoseEntriesFromEntriesList() {
        BibEntry entry1 = new BibEntry();
        BibEntry entry2 = new BibEntry();
        BibEntry entry3 = new BibEntry();
        List<BibEntry> allEntries = Arrays.asList(entry1, entry2, entry3);
        database.insertEntries(allEntries);
        List<BibEntry> entriesToDelete = Arrays.asList(entry1, entry3);
        database.removeEntries(entriesToDelete);
        assertEquals(List.of(entry2), database.getEntries());
        assertFalse(database.containsEntryWithId(entry1.getId()));
        assertTrue(database.containsEntryWithId(entry2.getId()));
        assertFalse(database.containsEntryWithId(entry3.getId()));
    }

    @Test
    void removeAllEntriesRemovesAllEntriesFromEntriesList() {
        List<BibEntry> allEntries = new ArrayList<>();
        BibEntry entry1 = new BibEntry();
        BibEntry entry2 = new BibEntry();
        BibEntry entry3 = new BibEntry();
        allEntries.add(entry1);
        allEntries.add(entry2);
        allEntries.add(entry3);

        database.removeEntries(allEntries);
        assertEquals(List.of(), database.getEntries());
        assertFalse(database.containsEntryWithId(entry1.getId()));
        assertFalse(database.containsEntryWithId(entry2.getId()));
        assertFalse(database.containsEntryWithId(entry3.getId()));
    }

    @Test
    void insertNullEntryThrowsException() {
        assertThrows(NullPointerException.class, () -> database.insertEntry(null));
    }

    @Test
    void removeNullEntryThrowsException() {
        assertThrows(NullPointerException.class, () -> database.removeEntry(null));
    }

    @Test
    void emptyDatabaseHasNoStrings() {
        assertEquals(Set.of(), database.getStringKeySet());
        assertTrue(database.hasNoStrings());
    }

    @Test
    void databaseHasStringAfterInsertion() {
        database.addString(bibtexString);
        assertFalse(database.hasNoStrings());
    }

    @Test
    void databaseStringKeySetIncreasesAfterStringInsertion() {
        assertEquals(0, database.getStringKeySet().size());

        database.addString(bibtexString);

        assertEquals(1, database.getStringKeySet().size());
    }

    @Test
    void databaseStringCountIncreasesAfterStringInsertion() {
        assertEquals(0, database.getStringCount());

        database.addString(bibtexString);

        assertEquals(1, database.getStringCount());
    }

    @Test
    void databaseContainsNewStringInStringValues() {
        database.addString(bibtexString);

        assertTrue(database.getStringValues().contains(bibtexString));
    }

    @Test
    void retrieveInsertedStringById() {
        database.addString(bibtexString);

        assertTrue(database.getStringKeySet().contains(bibtexString.getId()));
    }

    @Test
    void stringIsNotModifiedAfterInsertion() {
        database.addString(bibtexString);

        assertEquals(bibtexString, database.getString(bibtexString.getId()));
    }

    @Test
    void databaseHasNoStringsAfterRemoval() {
        database.addString(bibtexString);
        assertFalse(database.hasNoStrings());

        database.removeString(bibtexString.getId());
        assertTrue(database.hasNoStrings());
    }

    @Test
    void stringKeySizeIsEmptyAfterRemoval() {
        database.addString(bibtexString);

        database.removeString(bibtexString.getId());
        assertEquals(0, database.getStringKeySet().size());
    }

    @Test
    void stringCountIsZeroAfterRemoval() {
        database.addString(bibtexString);
        assertEquals(1, database.getStringCount());

        database.removeString(bibtexString.getId());
        assertEquals(0, database.getStringCount());
    }

    @Test
    void stringValuesDoesNotContainStringAfterRemoval() {
        database.addString(bibtexString);
        assertTrue(database.getStringValues().contains(bibtexString));

        database.removeString(bibtexString.getId());
        assertFalse(database.getStringValues().contains(bibtexString));
    }

    @Test
    void stringKeySetDoesNotContainStringIdAfterRemoval() {
        database.addString(bibtexString);
        assertTrue(database.getStringKeySet().contains(bibtexString.getId()));

        database.removeString(bibtexString.getId());
        assertFalse(database.getStringKeySet().contains(bibtexString.getId()));
    }

    @Test
    void databaseReturnsNullForRemovedString() {
        database.addString(bibtexString);
        assertEquals(bibtexString, database.getString(bibtexString.getId()));

        database.removeString(bibtexString.getId());
        assertNull(database.getString(bibtexString.getId()));
    }

    @Test
    void hasStringLabelFindsString() {
        database.addString(bibtexString);
        assertTrue(database.hasStringByName("DSP"));
        assertFalse(database.hasStringByName("VLSI"));
    }

    @Test
    void setSingleStringAsCollection() {
        List<BibtexString> strings = List.of(bibtexString);
        database.setStrings(strings);
        assertEquals(Optional.of(bibtexString), database.getStringByName("DSP"));
    }

    @Test
    void setStringAsCollectionWithUpdatedContentThrowsKeyCollisionException() {
        BibtexString newContent = new BibtexString("DSP", "ABCD");
        List<BibtexString> strings = Arrays.asList(bibtexString, newContent);
        assertThrows(KeyCollisionException.class, () -> database.setStrings(strings));
    }

    @Test
    void setStringAsCollectionWithNewContent() {
        BibtexString vlsi = new BibtexString("VLSI", "Very Large Scale Integration");
        List<BibtexString> strings = Arrays.asList(bibtexString, vlsi);
        database.setStrings(strings);
        assertEquals(Optional.of(bibtexString), database.getStringByName("DSP"));
        assertEquals(Optional.of(vlsi), database.getStringByName("VLSI"));
    }

    @Test
    void addSameStringLabelTwiceThrowsKeyCollisionException() {
        database.addString(bibtexString);
        final BibtexString finalString = new BibtexString("DSP", "Digital Signal Processor");

        assertThrows(KeyCollisionException.class, () -> database.addString(finalString));
    }

    @Test
    void addSameStringIdTwiceThrowsKeyCollisionException() {
        BibtexString string = new BibtexString("DSP", "Digital Signal Processing");
        string.setId("duplicateid");
        database.addString(string);
        final BibtexString finalString = new BibtexString("VLSI", "Very Large Scale Integration");
        finalString.setId("duplicateid");

        assertThrows(KeyCollisionException.class, () -> database.addString(finalString));
    }

    @Test
    void insertEntryPostsAddedEntryEvent() {
        BibEntry expectedEntry = new BibEntry();
        EventListenerTest tel = new EventListenerTest();
        database.registerListener(tel);
        database.insertEntry(expectedEntry);
        assertEquals(List.of(expectedEntry), tel.getAddedEntries());
        assertEquals(expectedEntry, tel.getFirstInsertedEntry());
    }

    @Test
    void insertMultipleEntriesPostsAddedEntryEvent() {
        BibEntry firstEntry = new BibEntry();
        BibEntry secondEntry = new BibEntry();
        EventListenerTest tel = new EventListenerTest();
        database.registerListener(tel);
        database.insertEntries(firstEntry, secondEntry);
        assertEquals(firstEntry, tel.getFirstInsertedEntry());
        assertEquals(Arrays.asList(firstEntry, secondEntry), tel.getAddedEntries());
    }

    @Test
    void removeEntriesPostsRemovedEntriesEvent() {
        BibEntry entry1 = new BibEntry();
        BibEntry entry2 = new BibEntry();
        List<BibEntry> expectedEntries = Arrays.asList(entry1, entry2);
        EventListenerTest tel = new EventListenerTest();
        database.insertEntries(expectedEntries);
        database.registerListener(tel);
        database.removeEntries(expectedEntries);
        List<BibEntry> actualEntry = tel.getRemovedEntries();
        assertEquals(expectedEntries, actualEntry);
    }

    @Test
    void changingEntryPostsChangeEntryEvent() {
        BibEntry entry = new BibEntry();
        EventListenerTest tel = new EventListenerTest();
        database.insertEntry(entry);
        database.registerListener(tel);

        entry.setField(new UnknownField("test"), "some value");

        assertEquals(entry, tel.getChangedEntry());
    }

    @Test
    void correctKeyCountOne() {
        BibEntry entry = new BibEntry();
        entry.setCitationKey("AAA");
        database.insertEntry(entry);
        assertEquals(1, database.getNumberOfCitationKeyOccurrences("AAA"));
    }

    @Test
    void correctKeyCountTwo() {
        BibEntry entry = new BibEntry();
        entry.setCitationKey("AAA");
        database.insertEntry(entry);
        entry = new BibEntry();
        entry.setCitationKey("AAA");
        database.insertEntry(entry);
        assertEquals(2, database.getNumberOfCitationKeyOccurrences("AAA"));
    }

    @Test
    void correctKeyCountAfterRemoving() {
        BibEntry entry = new BibEntry();
        entry.setCitationKey("AAA");
        database.insertEntry(entry);
        entry = new BibEntry();
        entry.setCitationKey("AAA");
        database.insertEntry(entry);
        database.removeEntry(entry);
        assertEquals(1, database.getNumberOfCitationKeyOccurrences("AAA"));
    }

    @Test
    void circularStringResolving() {
        BibtexString string = new BibtexString("AAA", "#BBB#");
        database.addString(string);
        string = new BibtexString("BBB", "#AAA#");
        database.addString(string);
        assertEquals("AAA", database.resolveForStrings("#AAA#"));
        assertEquals("BBB", database.resolveForStrings("#BBB#"));
    }

    @Test
    void circularStringResolvingLongerCycle() {
        BibtexString string = new BibtexString("AAA", "#BBB#");
        database.addString(string);
        string = new BibtexString("BBB", "#CCC#");
        database.addString(string);
        string = new BibtexString("CCC", "#DDD#");
        database.addString(string);
        string = new BibtexString("DDD", "#AAA#");
        database.addString(string);
        assertEquals("AAA", database.resolveForStrings("#AAA#"));
        assertEquals("BBB", database.resolveForStrings("#BBB#"));
        assertEquals("CCC", database.resolveForStrings("#CCC#"));
        assertEquals("DDD", database.resolveForStrings("#DDD#"));
    }

    @Test
    void resolveForStringsMonth() {
        assertEquals("January", database.resolveForStrings("#jan#"));
    }

    @Test
    void resolveForStringsSurroundingContent() {
        BibtexString string = new BibtexString("AAA", "aaa");
        database.addString(string);
        assertEquals("aaaaaAAA", database.resolveForStrings("aa#AAA#AAA"));
    }

    @Test
    void resolveForStringsOddHashMarkAtTheEnd() {
        BibtexString string = new BibtexString("AAA", "aaa");
        database.addString(string);
        assertEquals("AAAaaaAAA#", database.resolveForStrings("AAA#AAA#AAA#"));
    }

    @Test
    void getUsedStrings() {
        BibEntry entry = new BibEntry()
                .withField(StandardField.AUTHOR, "#AAA#");
        BibtexString tripleA = new BibtexString("AAA", "Some other #BBB#");
        BibtexString tripleB = new BibtexString("BBB", "Some more text");
        BibtexString tripleC = new BibtexString("CCC", "Even more text");
        Set<BibtexString> stringSet = new HashSet<>();
        stringSet.add(tripleA);
        stringSet.add(tripleB);

        database.addString(tripleA);
        database.addString(tripleB);
        database.addString(tripleC);
        database.insertEntry(entry);

        Set<BibtexString> usedStrings = new HashSet<>(database.getUsedStrings(List.of(entry)));
        assertEquals(stringSet, usedStrings);
    }

    @Test
    void getUsedStringsSingleString() {
        BibEntry entry = new BibEntry()
                .withField(StandardField.AUTHOR, "#AAA#");
        BibtexString tripleA = new BibtexString("AAA", "Some other text");
        BibtexString tripleB = new BibtexString("BBB", "Some more text");
        List<BibtexString> strings = new ArrayList<>(1);
        strings.add(tripleA);

        database.addString(tripleA);
        database.addString(tripleB);
        database.insertEntry(entry);

        List<BibtexString> usedStrings = database.getUsedStrings(List.of(entry));
        assertEquals(strings, usedStrings);
    }

    @Test
    void getUsedStringsNoString() {
        BibEntry entry = new BibEntry()
                .withField(StandardField.AUTHOR, "Oscar Gustafsson");
        BibtexString string = new BibtexString("AAA", "Some other text");
        database.addString(string);
        database.insertEntry(entry);
        Collection<BibtexString> usedStrings = database.getUsedStrings(List.of(entry));
        assertEquals(List.of(), usedStrings);
    }

    @Test
    void getEntriesSortedWithTwoEntries() {
        BibEntry entryB = new BibEntry(StandardEntryType.Article);
        entryB.setId("2");
        BibEntry entryA = new BibEntry(StandardEntryType.Article);
        entryB.setId("1");
        database.insertEntries(entryB, entryA);
        assertEquals(Arrays.asList(entryA, entryB), database.getEntriesSorted(Comparator.comparing(BibEntry::getId)));
    }

    @Test
    void preambleIsEmptyIfNotSet() {
        assertEquals(Optional.empty(), database.getPreamble());
    }

    @Test
    void setPreambleWorks() {
        database.setPreamble("Oh yeah!");
        assertEquals(Optional.of("Oh yeah!"), database.getPreamble());
    }

    @Test
    void getIndex() {
        BibEntry entryA = new BibEntry(StandardEntryType.Article);
        BibEntry entryB = new BibEntry(StandardEntryType.Article);
        BibEntry entryC = new BibEntry(StandardEntryType.Article);

        database.insertEntries(entryA, entryB, entryC);
        assertEquals(0, database.indexOf(entryA));
        assertEquals(1, database.indexOf(entryB));
        assertEquals(2, database.indexOf(entryC));
        assertEquals(-1, database.indexOf(new BibEntry()));

        database.removeEntry(entryB);
        assertEquals(-1, database.indexOf(entryB));
        assertEquals(0, database.indexOf(entryA));
        assertEquals(1, database.indexOf(entryC));

        database.removeEntry(entryA);
        assertEquals(-1, database.indexOf(entryA));
        assertEquals(0, database.indexOf(entryC));

        BibEntry entryD = new BibEntry(StandardEntryType.Article);
        database.insertEntry(entryD);

        assertEquals(0, database.indexOf(entryC));
        assertEquals(1, database.indexOf(entryD));
        assertEquals(-1, database.indexOf(entryA));
    }
}
