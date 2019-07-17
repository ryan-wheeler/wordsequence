package com.codechallenge;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * WordSequenceFrequency will report the count of the 100 most common word sequences in one or more sources
 */
public class WordSequenceFrequency {
    private static final int TOKEN_COUNT = 3;
    private static final long FIVE_MINUTES = 5;

    /**
     * main will will check for input provided as arguments or on system in, and process if available
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
            try {
                processMultipleFiles(args);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

        } else if (System.in.available() > 0) {

            processFrequencyReports(
                    "System.in",
                    new BufferedReader(new InputStreamReader(System.in)),
                    System.out::println
            );

        } else {
            System.out.println("No valid input provided");
        }

    }

    /**
     * processMultipleFiles accepts an array of filepaths, and for each file which exists, prints the 100 most
     * frequently occurring word sequences
     * @param files
     * @throws InterruptedException
     */
    public static void processMultipleFiles(final String[] files) throws InterruptedException {
        List<Runnable> filesToProcess = Arrays.stream(files).map(s -> {
            Runnable job = null;
            final File file = new File(s);
            if (file.exists() && file.isFile()) {
                try {
                    final BufferedReader reader = new BufferedReader(new FileReader(file));
                    job = () -> processFrequencyReports(s, reader, System.out::println);
                } catch (FileNotFoundException e) {
                    // we should not see this error as we're checking above
                }
            }
            return job;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (filesToProcess.size() > 0) {
            ExecutorService executorService = Executors.newWorkStealingPool();
            filesToProcess.stream().forEach(executorService::submit);
            executorService.shutdown();
            executorService.awaitTermination(FIVE_MINUTES, TimeUnit.MINUTES);
        }
    }

    /**
     * processFrequencyReports generates a string message reporting the most frequent word sequences,
     * and applies the provided consumer
     * @param descriptor
     * @param reader
     * @param consumer
     */
    public static void processFrequencyReports(final String descriptor,
                                                final BufferedReader reader,
                                                final Consumer<String> consumer) {
        if (Objects.nonNull(descriptor) && Objects.nonNull(reader) && Objects.nonNull(consumer)) {
            final String frequentSequences = getFrequentSequences(reader);
            consumer.accept(String.format("\n'%s' frequent sequences: \n\n%s",descriptor, frequentSequences));
        }
    }

    /**
     * getFrequentSequences generates a string message containing the most frequent word sequences
     * @param reader
     * @return
     */
    public static String getFrequentSequences(final BufferedReader reader) {
        if (Objects.isNull(reader)) {
            return "Invalid input";
        } else {

            try {
                final Map<String, AtomicLong> sequenceCountMap = getSequenceCountMap(reader);
                if (sequenceCountMap.isEmpty()) {
                    return "No word sequences found";
                }
                return sequenceCountMap.entrySet().stream()
                        .sorted((t0, t1) -> Long.compare(t1.getValue().get(), t0.getValue().get()))
                        .limit(100)
                        .map(entry -> String.format("%d - %s", entry.getValue().get(), entry.getKey()))
                        .collect(Collectors.joining("\n"));

            } catch (Exception e) {
                return "An error occurred";
            }

        }
    }

    /**
     * getSequenceCountMap creates a map of word sequence to occurrence count for the provided reader
     * @param reader
     * @return
     * @throws IOException
     */
    public static Map<String, AtomicLong> getSequenceCountMap(final BufferedReader reader) throws IOException {
        if (Objects.nonNull(reader)) {
            final List<String> tokens = new ArrayList<>(TOKEN_COUNT);
            final Map<String, AtomicLong> sequenceCountMap = new HashMap<>();

            reader.lines().filter(s -> Objects.nonNull(s) && !s.isEmpty())
                    .flatMap(s -> Arrays.stream(s.split("\\s+")))
                    .filter(s -> Objects.nonNull(s) && !s.isEmpty())
                    .map(s -> s.replaceAll("\\p{Punct}", "").toLowerCase())
                    .forEachOrdered(s -> {
                        tokens.add(s);
                        if (tokens.size() == TOKEN_COUNT) {
                            final String sequence = tokens.stream()
                                    .collect(Collectors.joining(" ")).toLowerCase();
                            AtomicLong count = sequenceCountMap.get(sequence);
                            if (Objects.isNull(count)) {
                                count = new AtomicLong();
                                sequenceCountMap.put(sequence, count);
                            }
                            count.incrementAndGet();
                            tokens.remove(0);
                        }

                    });

            return sequenceCountMap;

        } else {
            return Collections.EMPTY_MAP;
        }
    }
}
