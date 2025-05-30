package org.jabref.logic.exporter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.fileformat.BibtexImporter;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.util.DummyFileUpdateMonitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class MSBibExportFormatFilesTest {

    private static Path resourceDir;
    public BibDatabaseContext databaseContext;
    public Charset charset;
    private Path exportedFile;
    private MSBibExporter exporter;
    private BibtexImporter testImporter;

    static Stream<String> fileNames() throws IOException, URISyntaxException {
        // we have to point it to one existing file, otherwise it will return the default class path
        resourceDir = Path.of(MSBibExportFormatFilesTest.class.getResource("MsBibExportFormatTest1.bib").toURI()).getParent();
        try (Stream<Path> stream = Files.list(resourceDir)) {
            return stream.map(n -> n.getFileName().toString())
                         .filter(n -> n.endsWith(".bib"))
                         .filter(n -> n.startsWith("MsBib"))
                         // mapping required, because we get "source already consumed or closed" otherwise
                         .toList().stream();
        }
    }

    @BeforeEach
    void setUp(@TempDir Path testFolder) throws IOException {
        databaseContext = new BibDatabaseContext();
        charset = StandardCharsets.UTF_8;
        exporter = new MSBibExporter();
        Path path = testFolder.resolve("ARandomlyNamedFile.tmp");
        exportedFile = Files.createFile(path);
        testImporter = new BibtexImporter(mock(ImportFormatPreferences.class, Answers.RETURNS_DEEP_STUBS), new DummyFileUpdateMonitor());
    }

    @ParameterizedTest(name = "{index} file={0}")
    @MethodSource("fileNames")
    void performExport(String filename) throws IOException, SaveException {
        String xmlFileName = filename.replace(".bib", ".xml");
        Path expectedFile = resourceDir.resolve(xmlFileName);
        Path importFile = resourceDir.resolve(filename);

        BibDatabaseContext contextFromImport = testImporter.importDatabase(importFile).getDatabaseContext();
        List<BibEntry> entries = contextFromImport.getEntries();

        contextFromImport.getDatabase().getStringValues().forEach(this.databaseContext.getDatabase()::addString);
        exporter.export(databaseContext, exportedFile, entries);

        String expected = String.join("\n", Files.readAllLines(expectedFile));
        String actual = String.join("\n", Files.readAllLines(exportedFile));

        // The order of the XML elements changes from Windows to Travis environment somehow
        // The order does not really matter, so we ignore it.
        // Source: https://stackoverflow.com/a/16540679/873282
        assertThat(expected, isSimilarTo(actual)
                .ignoreWhitespace()
                .normalizeWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText)));
    }
}
