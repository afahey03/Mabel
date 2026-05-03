import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs every .mabel file found in the {@code tests/} resource directory.
 * For each {@code NN_name.mabel} file there must be a matching
 * {@code NN_name.expected} file containing the exact expected stdout output.
 *
 * Tests are generated dynamically so that each .mabel file is an independent
 * test case — one failure does not prevent the others from running.
 */
class MabelLanguageTest {

    @TestFactory
    Stream<DynamicTest> runMabelTestFiles() throws IOException, URISyntaxException {
        URL testsDir = getClass().getClassLoader().getResource("mabel-tests");
        if (testsDir == null) {
            throw new IllegalStateException(
                    "Could not find 'mabel-tests' directory on the test classpath. " +
                            "Ensure Maven copies the tests/ directory as a test resource.");
        }

        Path dir = Paths.get(testsDir.toURI());
        List<Path> mabelFiles = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(dir, 1)) {
            walk.filter(p -> p.toString().endsWith(".mabel"))
                    .sorted()
                    .forEach(mabelFiles::add);
        }

        if (mabelFiles.isEmpty()) {
            throw new IllegalStateException("No .mabel test files found in " + dir);
        }

        return mabelFiles.stream().map(mabelFile -> {
            String fileName = mabelFile.getFileName().toString();
            String baseName = fileName.replace(".mabel", "");
            Path expectedFile = mabelFile.resolveSibling(baseName + ".expected");

            return DynamicTest.dynamicTest(baseName, () -> {
                String source = Files.readString(mabelFile, StandardCharsets.UTF_8);
                String expected = Files.readString(expectedFile, StandardCharsets.UTF_8)
                        // Normalise line endings and trailing whitespace
                        .replace("\r\n", "\n")
                        .stripTrailing();

                String actual = MabelInterpreter.run(source)
                        .replace("\r\n", "\n")
                        .stripTrailing();

                assertEquals(expected, actual,
                        "Output mismatch for " + fileName);
            });
        });
    }
}
