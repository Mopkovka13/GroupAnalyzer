package org.example;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static ArrayList<HashMap<String, Integer>> columns
            = new ArrayList<>();
    private static HashMap<Integer, Set<String>> groups
            = new HashMap<>();
    private static int numberGroupCounter;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        ///////////////////////////////////////////////////////////////////
        if (args.length == 0) {
            System.out.println("args not found");

            return;
        }

        var inputFilename = args[0];
        var outputFilename = "output.txt";

        readFile(inputFilename);
        List<Map.Entry<Integer, Set<String>>> sortedGroups = getSortedGroups();
        writeResult(outputFilename, sortedGroups);
        /////////////////////////////////////////////////////////////////////
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Time: " + duration / 1000 + "s");
    }

    public static void readFile(String filename) {
        String regex = "^\"(\\d*\\.?\\d*)?\"(;\"(\\d*\\.?\\d*)?\")*$";
        Pattern pattern = Pattern.compile(regex);

        try (BufferedReader reader = new BufferedReader(
                new FileReader(filename))) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (!pattern.matcher(line).matches()) {
                    // Если строка некорректна, пропускаем
                    continue;
                }

                var parts = line.split(";");

                var resultCoincidences = getNumberGroup(parts);

                var numberGroup = resultCoincidences == -1
                        ? numberGroupCounter++
                        : resultCoincidences;

                for (int i = 0; i < parts.length; i++) {
                    columns.get(i).put(parts[i], numberGroup);
                }

                if (!groups.containsKey(numberGroup)) {
                    groups.put(numberGroup, new HashSet<>());
                }

                groups.get(numberGroup).add(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static int getNumberGroup(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if (columns.size() <= i) {
                // Если элементов в этом столбце ещё не было, то просто его добавляем
                columns.add(new HashMap<>());
            } else {
                if (parts[i].equals("\"\""))
                    continue;

                var group = columns.get(i);

                if (group.containsKey(parts[i])) {
                    return group.get(parts[i]);
                }
            }
        }

        return -1;
    }

    private static void writeResult(String filename, List<Map.Entry<Integer, Set<String>>> sortedGroups) {
        int groupCounter = 1;

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8))) {
            writer.write(String.valueOf(sortedGroups.size()));
            writer.newLine();

            for (Map.Entry<Integer, Set<String>> entry : sortedGroups) {
                writer.write("Группа " + groupCounter + "\n");

                for (String line : entry.getValue()) {
                    writer.write(line + "\n");
                }

                writer.newLine();
                groupCounter++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Map.Entry<Integer, Set<String>>> getSortedGroups() {
        return groups.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toList());
    }
}