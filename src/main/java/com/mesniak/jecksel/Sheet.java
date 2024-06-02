package com.mesniak.jecksel;

import com.mesniak.jecksel.expression.Expression;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static com.mesniak.jecksel.Value.Type.NUMBER;

public class Sheet {
    private final Cell[][] cells;

    private Sheet(Cell[][] cells) {
        this.cells = cells;
    }

    public int getHeight() {
        return cells.length;
    }

    public int getWidth() {
        return cells[0].length;
    }

    // Very poor CSV parser which does not handle
    // separators inside strings, well, does not
    // even support proper strings, no newlines,
    // ..., but is totally fine for this proof
    // of concept.
    public static Sheet load(String path) {
        try {
            return load(new FileInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load sheet from path " + path, e);
        }
    }

    public static Sheet load(InputStream istream) {
        try {
            var is = new BufferedInputStream(istream);
            var content = new String(is.readAllBytes(), Charset.defaultCharset());
            var lines = Arrays.asList(content.split(System.lineSeparator()));
            var numColumns = lines.getFirst().replaceAll("[^,]", "").length();
            Cell[][] cells = new Cell[lines.size()][numColumns];
            for (int y = 0; y < lines.size(); y++) {
                var line = lines.get(y).split(",");
                for (int x = 0; x < numColumns; x++) {
                    Value value;
                    if (x >= line.length) {
                        value = new Value(Value.Type.STRING, "");
                    } else {
                        value = Value.from(line[x]);
                    }
                    cells[y][x] = new Cell(x, y, value);
                }
            }

            computeDependencies(cells);
            return new Sheet(cells);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load sheet", e);
        }
    }

    private static void computeDependencies(Cell[][] cells) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                Cell cell = cells[i][j];
                if (cell.getValue().getType() == Value.Type.FORMULA) {
                    String formula = cell.getValue().getFormula();
                    var dependencies = new LinkedList<Cell>();
                    var s = formula.substring(1);
                    var parts = s.split("[\\(\\)+\\-*/]");
                    for (String part : parts) {
                        part = part.toUpperCase();
                        if (part.matches("[A-Z]+[0-9]+")) {
                            var x = part.charAt(0) - 'A';
                            var y = Integer.parseInt(part.substring(1)) - 1;
                        dependencies.add(cells[y][x]);
                        }
                    }
                    cell.setDependencies(dependencies);

                }
            }
        }
    }

    public void compute() {
        // iterate through all cells
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                var cell = cells[y][x];
                cell.setValue(compute(cell, new HashSet<>()));
            }
        }
    }

    private Value compute(Cell cell, Set<Cell> visited) {
        if (visited.contains(cell)) {
            throw new IllegalStateException("Cycle detected for " + cell.getLocation());
        }
        visited.add(cell);

        if (cell.getValue().getType() != Value.Type.FORMULA) {
            // Value already computed.
            return cell.getValue();
        }

        // A formula, start recursively computing cells.
        var depValues = new HashMap<String, Value>();
        for (var depCell : cell.getDependencies()) {
            var v = compute(depCell, visited);
            depValues.put(depCell.getLocation(), v);
        }

        // Replace formula values with their values and
        // evaluate the resulting expression.
        var f = cell.getValue().toString();
        for (var e : depValues.entrySet()) {
            f = f.replaceAll(e.getKey(), e.getValue().toString());
        }
        f = f.substring(1);

        var expr = Expression.evaluate(f);
        var newV = Value.from(expr);
        cell.setValue(newV);
        return newV;
    }

    public String toString() {
        var sb = new StringBuilder();
        var colWidth = new int[getWidth()];
        for (int col = 0; col < getWidth(); col++) {
            int max = 0;
            for (int row = 0; row < getHeight(); row++) {
                if (cells[row] == null || cells[row][col] == null) {
                    continue;
                }
                var cellWidth = cells[row][col].toString().length();
                if (cellWidth > max) {
                    max = cellWidth;
                }
            }
            colWidth[col] = max;
        }

        int numWidth = Integer.toString(getHeight()).length();

        for (int row = 0; row < getHeight(); row++) {
            sb.append(String.format("%" + numWidth + "d" + " |", row + 1));
            for (int col = 0; col < getWidth(); col++) {
                sb.append(cells[row][col].toString());
                for (int k = 0; k < colWidth[col] - cells[row][col].toString().length(); k++) {
                    sb.append(" ");
                }
                sb.append("|");
            }
            sb.append("\n");
        }

        // Remove comments to show dependencies between cells:
        //
        // for (int y = 0; y < getHeight(); y++) {
        //     for (int x = 0; x < getWidth(); x++) {
        //         Cell cell = cells[y][x];
        //         if (cell.getDependencies().isEmpty()) {
        //             continue;
        //         }
        //         sb.append((char) (cell.getX() + 'A')).append(cell.getY() + 1).append(" -> ");
        //         for (Cell dependency : cell.getDependencies()) {
        //             sb.append((char) (dependency.getX() + 'A')).append(dependency.getY() + 1).append(", ");
        //         }
        //         sb.delete(sb.length() - 2, sb.length());
        //         sb.append("\n");
        //     }
        // }

        return sb.toString();
    }
}
