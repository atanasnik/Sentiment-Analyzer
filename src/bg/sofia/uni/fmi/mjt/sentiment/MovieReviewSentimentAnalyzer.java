package bg.sofia.uni.fmi.mjt.sentiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class MovieReviewSentimentAnalyzer implements SentimentAnalyzer {

    private Writer reviewsWriter;
    private Map<String, Double> scores;
    private Map<String, Integer> occurrences;
    private Set<String> stopwords;
    private Map<String, List<Integer>> words;
    private final static int REVIEW_INDEX = 0;
    private final static int DEFAULT_NUMBER_OF_OCCURRENCES = 0;
    private final static int COMPARISON_EQUALITY = 0;
    private final static String WORD_ARGUMENT = "Word";
    private final static String REVIEW_ARGUMENT = "Review";
    private final static String EMPTY_SYMBOL = " ";
    private final static String SPLIT_REGEX = "[^a-zA-Z0-9']";
    private final static int MIN_WORD_SIZE = 2;

    public MovieReviewSentimentAnalyzer(Reader stopwordsIn, Reader reviewsIn, Writer reviewsOut) {

        if (stopwordsIn == null || reviewsIn == null || reviewsOut == null) {
            throw new IllegalArgumentException("A file is invalid");
        }

        reviewsWriter = reviewsOut;
        scores = new HashMap<>();
        stopwords = new HashSet<>();
        occurrences = new HashMap<>();
        words = new HashMap<>();
        BufferedReader reviewsInBuffered = new BufferedReader(reviewsIn);
        BufferedReader stopWordsInBuffered = new BufferedReader(stopwordsIn);

        extractStopwords(stopWordsInBuffered);

        setData(reviewsInBuffered);

    }

    private void setData(BufferedReader reviewsInBuffered) {

        try {
            String line;
            while ((line = reviewsInBuffered.readLine()) != null) {
                parseLine(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading reviews");
        }

        calculateScores();

    }

    private void extractStopwords(BufferedReader stopWordsInBuffered) {

        try {
            String line;
            while ((line = stopWordsInBuffered.readLine()) != null) {
                stopwords.add(line.toLowerCase().strip());
                // they should be all lowercase already but just to be sure
            }
        } catch (IOException e) {
            throw new IllegalStateException("Stopwords could not be extracted", e);
        }

    }

    private void parseLine(String line) {

        int rating = Integer.parseInt(String.valueOf(line.charAt(REVIEW_INDEX)));

        List<String> wordsList = splitLine(line.strip());
        Set<String> wordsNonRepeating = new HashSet<>();

        for (String word : wordsList) {

            String wordLowerCase = word.toLowerCase().strip();

            if (isStopWord(wordLowerCase)) {
                continue;
            }

            occurrences.putIfAbsent(wordLowerCase, 0);
            occurrences.put(wordLowerCase, occurrences.get(wordLowerCase) + 1);

            if (wordsNonRepeating.contains(wordLowerCase)) {
                continue;
            }

            wordsNonRepeating.add(wordLowerCase);

            words.putIfAbsent(wordLowerCase, new ArrayList<>());
            words.get(wordLowerCase).add(rating);
        }

    }

    private List<String> splitLine(String line) {
        return Arrays.stream(line.split(SPLIT_REGEX))
                .toList()
                .stream()
                .filter(string -> string.length() >= MIN_WORD_SIZE)
                .toList();
    }

    private void calculateScores() {
        for (var entry : words.entrySet()) {
            scores.put(entry.getKey(), entry.getValue()
                    .stream()
                    .mapToInt(x -> x)
                    .average()
                    .orElseThrow(() -> new NoSuchElementException("No average score calculated")));
        }
    }

    public void validateStringArgument(String string, String argName) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException(argName + " cannot be null, blank or empty");
        }
    }

    public void validateIntegerArgument(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The number of words must be non-negative");
        }
    }

    @Override
    public double getReviewSentiment(String review) {

        validateStringArgument(review, REVIEW_ARGUMENT);

        return new HashSet<>(splitLine(review))
                .stream()
                .filter(word -> !stopwords.contains(word.toLowerCase().strip())
                        && scores.containsKey(word.toLowerCase().strip()))
                .mapToDouble(word -> scores.get(word.toLowerCase()))
                .average()
                .orElse(RatingType.UNKNOWN.getRatingNumber());

    }

    @Override
    public String getReviewSentimentAsName(String review) {

        var sentiment = Math.round(getReviewSentiment(review));

        String result = RatingType.UNKNOWN.getRatingText();

        for (RatingType value : RatingType.values()) {
            if (Double.compare(value.getRatingNumber(), sentiment) == COMPARISON_EQUALITY) {
                result = value.getRatingText();
            }
        }

        return result;

    }

    @Override
    public double getWordSentiment(String word) {

        validateStringArgument(word, WORD_ARGUMENT);

        Double result = scores.get(word.toLowerCase().trim());
        return result != null ? result : RatingType.UNKNOWN.getRatingNumber();

    }

    @Override
    public int getWordFrequency(String word) {

        validateStringArgument(word, WORD_ARGUMENT);

        return occurrences.getOrDefault(word.toLowerCase().trim(), DEFAULT_NUMBER_OF_OCCURRENCES);

    }

    @Override
    public List<String> getMostFrequentWords(int n) {

        validateIntegerArgument(n);

        return occurrences.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue((a, b) -> Integer.compare(b, a)))
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();

    }

    @Override
    public List<String> getMostPositiveWords(int n) {

        validateIntegerArgument(n);

        return scores.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue((a, b) -> Double.compare(b, a)))
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();

    }

    @Override
    public List<String> getMostNegativeWords(int n) {

        validateIntegerArgument(n);

        return scores.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();

    }

    @Override
    public boolean appendReview(String review, int sentiment) {

        validateStringArgument(review, REVIEW_ARGUMENT);
        if (sentiment < RatingType.NEGATIVE.getRatingNumber() ||
                sentiment > RatingType.POSITIVE.getRatingNumber()) {
            throw new IllegalArgumentException("Review sentiment is not in the required range");
        }

        String line = sentiment + EMPTY_SYMBOL + review + System.lineSeparator();

        try {
            reviewsWriter.append(line);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        parseLine(line.strip());

        calculateScores();

        return true;
    }

    @Override
    public int getSentimentDictionarySize() {
        return scores.size();
    }

    @Override
    public boolean isStopWord(String word) {
        return stopwords.contains(word.toLowerCase().strip());
    }
}
