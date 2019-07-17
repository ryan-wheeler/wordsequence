package com.codechallenge;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class WordSequenceFrequencyTest {

    @Test
    public void processMultipleFiles() {
    }

    @Test
    public void processFrequencyReports() {
    }

    @Test
    public void getFrequentSequences() {
    }

    @Test
    public void nullReaderReturnsEmptyMapTest() throws IOException {
        final Map<String, AtomicLong> frequencyMap = WordSequenceFrequency.getSequenceCountMap(null);
        assertTrue(Objects.nonNull(frequencyMap) && frequencyMap.isEmpty());
    }

    @Test
    public void expectedSequencesCreatedTest() throws IOException {
        final String input = "one two three four five";
        final String first = "one two three";
        final String second = "two three four";
        final String third = "three four five";
        final Map<String, AtomicLong> frequencyMap = getFrequency(input);
        Arrays.asList(first, second, third).stream().forEach(s -> assertTrue(frequencyMap.containsKey(s)));
    }

    @Test
    public void ignorePunctuationTest() throws IOException {
        final String input = "It's a test. It's, a test. Its; a test!!!";
        final String expected = "its a test";
        final int expectedCount = 3;
        final Map<String, AtomicLong> frequencyMap = getFrequency(input);
        assertTrue(frequencyMap.containsKey(expected));
        assertTrue(frequencyMap.get(expected).get() == 3);
    }

    private Map<String, AtomicLong> getFrequency(final String input) throws IOException {
        return WordSequenceFrequency.getSequenceCountMap(new BufferedReader(new StringReader(input)));
    }
}