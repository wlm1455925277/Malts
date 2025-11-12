package dev.jsinco.malts.commands.interfaces;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentFlagReaderTest {

    @Test
    void testParsesFlagsAndArgumentsCorrectly() {
        List<String> args = List.of(
                "random", "content", "and", "a", "random", "argument",
                "-player", "Jsinco", "-page", "2", "more", "random", "content", "possibly"
        );

        ArgumentFlagReader reader = new ArgumentFlagReader(args);

        Map<String, String> flags = reader.getFlags();
        List<String> newArgs = reader.getNewArguments();

        // Flags should be parsed correctly
        assertEquals("Jsinco", flags.get("player"));
        assertEquals("2", flags.get("page"));

        // New arguments should exclude the flags
        assertTrue(newArgs.contains("random"));
        assertFalse(newArgs.contains("-player"));
        assertEquals(
                List.of("random", "content", "and", "a", "random", "argument", "more", "random", "content", "possibly"),
                newArgs
        );
    }

    @Test
    void testFlagWithoutValueDefaultsToTrue() {
        List<String> args = List.of("-debug", "start");

        ArgumentFlagReader reader = new ArgumentFlagReader(args);

        assertEquals("true", reader.getFlagValue("debug"));
        assertEquals(List.of("start"), reader.getNewArguments());
    }

    @Test
    void testGetFlagValueAsReturnsCorrectType() {
        List<String> args = List.of("-player", "Jsinco", "-page", "5");
        ArgumentFlagReader reader = new ArgumentFlagReader(args);


        String pageStr = reader.getFlagValueAs("player", "defaultPlayer", String.class);
        Integer pageInt = reader.getFlagValueAs("page", 100, Integer.class);
        assertEquals("Jsinco", pageStr);
        assertEquals(5, pageInt);
    }

    @Test
    void testMissingFlagReturnsNull() {
        ArgumentFlagReader reader = new ArgumentFlagReader(List.of("no", "flags", "here"));
        assertNull(reader.getFlagValue("missing"));
    }
}