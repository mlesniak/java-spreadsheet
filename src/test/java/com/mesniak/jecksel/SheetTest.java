package com.mesniak.jecksel;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SheetTest {
    // Simple iteratively extended test to
    @Test
    public void playgroundTest() {
        var sheet = Sheet.load(testFile("sheet.csv"));
        System.out.println(sheet);
        sheet.compute();

        var expected = """
                1 |a   |b     |c   |d    |
                2 |34.0|34.0  |    |     |
                3 |68.0|      |45.0|71.0 |
                4 |    |55.0  |55.0|55.0 |
                5 |    |string|68.0|237.0|
                """;
        System.out.println(sheet);
        assertEquals(expected, sheet.toString());
    }

    @Test
    public void cycleTest() {
        var sheet = Sheet.load(testFile("cycle.csv"));
        System.out.println(sheet);
        assertThrows(IllegalStateException.class, sheet::compute);
    }

    private InputStream testFile(String name) {
        return getClass().getResourceAsStream("/" + name);
    }
}
