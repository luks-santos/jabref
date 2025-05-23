package org.jabref.logic.layout;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

import org.jabref.logic.journals.JournalAbbreviationRepository;
import org.jabref.logic.layout.format.NameFormatterPreferences;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.field.UnknownField;
import org.jabref.model.entry.types.StandardEntryType;
import org.jabref.model.entry.types.UnknownEntryType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LayoutTest {

    private LayoutFormatterPreferences layoutFormatterPreferences;
    private JournalAbbreviationRepository abbreviationRepository;

    @BeforeEach
    void setUp() {
        layoutFormatterPreferences = mock(LayoutFormatterPreferences.class, Answers.RETURNS_DEEP_STUBS);
        abbreviationRepository = mock(JournalAbbreviationRepository.class);
    }

    private String layout(String layout, List<Path> fileDirForDatabase, BibEntry entry) throws IOException {
        Reader layoutReader = Reader.of(layout.replace("__NEWLINE__", "\n"));

        return new LayoutHelper(layoutReader, fileDirForDatabase, layoutFormatterPreferences, abbreviationRepository)
                .getLayoutFromText()
                .doLayout(entry, null);
    }

    private String layout(String layout, BibEntry entry) throws IOException {
        return layout(layout, List.of(), entry);
    }

    @Test
    void entryTypeForUnknown() throws IOException {
        BibEntry entry = new BibEntry(new UnknownEntryType("unknown")).withField(StandardField.AUTHOR, "test");

        assertEquals("Unknown", layout("\\bibtextype", entry));
    }

    @Test
    void entryTypeForArticle() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Article).withField(StandardField.AUTHOR, "test");

        assertEquals("Article", layout("\\bibtextype", entry));
    }

    @Test
    void entryTypeForMisc() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Misc).withField(StandardField.AUTHOR, "test");

        assertEquals("Misc", layout("\\bibtextype", entry));
    }

    @Test
    void HTMLChar() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Article).withField(StandardField.AUTHOR, "This\nis\na\ntext");

        String actual = layout("\\begin{author}\\format[HTMLChars]{\\author}\\end{author}", entry);

        assertEquals("This<br>is<br>a<br>text", actual);
    }

    @Test
    void HTMLCharWithDoubleLineBreak() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Article).withField(StandardField.AUTHOR, "This\nis\na\n\ntext");

        String layoutText = layout("\\begin{author}\\format[HTMLChars]{\\author}\\end{author} ", entry);

        assertEquals("This<br>is<br>a<p>text ", layoutText);
    }

    @Test
    void nameFormatter() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Article).withField(StandardField.AUTHOR, "Joe Doe and Jane, Moon");

        String layoutText = layout("\\begin{author}\\format[NameFormatter]{\\author}\\end{author}", entry);

        assertEquals("Joe Doe, Moon Jane", layoutText);
    }

    @Test
    void HTMLCharsWithDotlessIAndTiled() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Article)
                .withField(StandardField.ABSTRACT, "\\~{n} \\~n \\'i \\i \\i");

        String layoutText = layout(
                "<font face=\"arial\">\\begin{abstract}<BR><BR><b>Abstract: </b> \\format[HTMLChars]{\\abstract}\\end{abstract}</font>",
                entry);

        assertEquals(
                "<font face=\"arial\"><BR><BR><b>Abstract: </b> &ntilde; &ntilde; &iacute; &imath; &imath;</font>",
                layoutText);
    }

    @Test
    void beginConditionals() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Misc)
                .withField(StandardField.AUTHOR, "Author");

        // || (OR)
        String layoutText = layout("\\begin{editor||author}\\format[HTMLChars]{\\author}\\end{editor||author}", entry);

        assertEquals("Author", layoutText);

        // && (AND)
        layoutText = layout("\\begin{editor&&author}\\format[HTMLChars]{\\author}\\end{editor&&author}", entry);

        assertEquals("", layoutText);

        // ! (NOT)
        layoutText = layout("\\begin{!year}\\format[HTMLChars]{(no year)}\\end{!year}", entry);

        assertEquals("(no year)", layoutText);

        // combined (!a&&b)
        layoutText = layout(
                "\\begin{!editor&&author}\\format[HTMLChars]{\\author}\\end{!editor&&author}" +
                "\\begin{editor&&!author}\\format[HTMLChars]{\\editor} (eds.)\\end{editor&&!author}", entry);

        assertEquals("Author", layoutText);
    }

    /**
     * Test for http://discourse.jabref.org/t/the-wrapfilelinks-formatter/172 (the example in the help files)
     */
    @Test
    void wrapFileLinksExpandFile() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Article);
        entry.addFile(new LinkedFile("Test file", Path.of("encrypted.pdf"), "PDF"));

        String layoutText = layout(
                "\\begin{file}\\format[WrapFileLinks(\\i. \\d (\\p))]{\\file}\\end{file}",
                List.of(Path.of("src/test/resources/pdfs/")),
                entry);

        assertEquals(
                "1. Test file (" + Path.of("src/test/resources/pdfs/encrypted.pdf").toRealPath() + ")",
                layoutText);
    }

    @Test
    void expandCommandIfTerminatedByMinus() throws IOException {
        BibEntry entry = new BibEntry(StandardEntryType.Article).withField(StandardField.EDITION, "2");

        String layoutText = layout("\\edition-th ed.-", entry);

        assertEquals("2-th ed.-", layoutText);
    }

    @Test
    void customNameFormatter() throws IOException {
        when(layoutFormatterPreferences.getNameFormatterPreferences()).thenReturn(
                new NameFormatterPreferences(List.of("DCA"), List.of("1@*@{ll}@@2@1..1@{ff}{ll}@2..2@ and {ff}{l}@@*@*@more")));
        BibEntry entry = new BibEntry(StandardEntryType.Article).withField(StandardField.AUTHOR, "Joe Doe and Mary Jane");

        String layoutText = layout("\\begin{author}\\format[DCA]{\\author}\\end{author}", entry);

        assertEquals("JoeDoe and MaryJ", layoutText);
    }

    @Test
    void annotatedField() throws IOException {
        UnknownField annotatedField = new UnknownField("author+an");
        BibEntry entry = new BibEntry(StandardEntryType.Article)
            .withField(annotatedField, "1:corresponding,2:highlight")
            .withField(StandardField.AUTHOR, "Joe Doe and Mary Jane");

        String layoutText = layout("\\author: \\author \\author+an", entry);

        assertEquals("Joe Doe and Mary Jane: Joe Doe and Mary Jane 1:corresponding,2:highlight", layoutText);
    }
}
