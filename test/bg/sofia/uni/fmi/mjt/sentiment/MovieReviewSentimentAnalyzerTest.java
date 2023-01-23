package bg.sofia.uni.fmi.mjt.sentiment;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.io.Writer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MovieReviewSentimentAnalyzerTest {

    private static MovieReviewSentimentAnalyzer movieReviewSentimentAnalyzer;
    private static Writer reviewsWriter;

    @BeforeAll
    static void setUpClass() {

        try (var reviewsReader = new StringReader(MovieReviewsSampleData.getInstance())) {

            try (var stopWordsReader = new StringReader(StopWordSampleData.getInstance())) {

                reviewsWriter = new BufferedWriter(new StringWriter());
                reviewsWriter.append(MovieReviewsSampleData.getInstance());

                movieReviewSentimentAnalyzer =
                        new MovieReviewSentimentAnalyzer(stopWordsReader, reviewsReader, reviewsWriter);

            }
        } catch (IOException e) {
            throw new IllegalStateException("Stream reading/writing interrupted", e);
        }

    }

    @AfterAll
    static void tearDown() {
        try {
            reviewsWriter.close();
        } catch (IOException e) {
            throw new IllegalStateException("Writer could not be closed", e);
        }
    }

    @Test
    void testGetReviewSentimentAsNameReviewIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getReviewSentimentAsName("   "),
                "The review that is being evaluated must not be blank");
    }

    @Test
    void testGetReviewSentimentAsNameReviewIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getReviewSentimentAsName(null),
                "The review that is being evaluated must not be null");
    }

    @Test
    void testGetReviewSentimentAsNameReviewIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getReviewSentimentAsName(""),
                "The review that is being evaluated must not be empty");
    }

    @Test
    void testGetReviewSentimentAsNameNegativeReview() {
        assertEquals("negative", movieReviewSentimentAnalyzer
                        .getReviewSentimentAsName("Dire disappointment: dull and unamusing freakshow"),
                "Review must be evaluated correctly");
    }

    @Test
    void testGetReviewSentimentAsNamePositiveReview() {
        assertEquals("positive",
                movieReviewSentimentAnalyzer.getReviewSentimentAsName("really an epic movie!"),
                "Review must be evaluated correctly");
    }

    @Test
    void testGetReviewSentimentAsNameNoRecognizedWords() {
        assertEquals("unknown",
                movieReviewSentimentAnalyzer.getReviewSentimentAsName("Java it is!"),
                "Review that consists of unrecognized words must be evaluated correctly");
    }

    @Test
    void testGetReviewSentimentAsNameSomewhatNegativeReview() {
        assertEquals("somewhat negative", movieReviewSentimentAnalyzer
                        .getReviewSentimentAsName("huge aggressive combinatioN?"),
                "Review must be evaluated correctly");
    }

    @Test
    void testGetReviewSentimentSomewhatPositiveReview() {
        assertEquals(3,
                Math.round(movieReviewSentimentAnalyzer.getReviewSentiment("Pretty much worth it!")),
                "Review must be evaluated correctly");
    }

    @Test
    void testGetWordSentimentWordIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getWordSentiment(" "),
                "The word that is being evaluated must not be blank");
    }

    @Test
    void testGetWordSentimentWordIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getWordSentiment(""),
                "The word that is being evaluated must not be empty");
    }

    @Test
    void testGetWordSentimentWordIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getWordSentiment(null),
                "The word that is being evaluated must not be null");
    }

    @Test
    void testGetWordSentimentPositive() {
        assertEquals(4.0, movieReviewSentimentAnalyzer.getWordSentiment(" SINCERE "),
                "Single word score must be evaluated correctly");
    }

    @Test
    void testGetWordSentimentSomewhatNegative() {
        assertEquals(1.0, Math.round(movieReviewSentimentAnalyzer.getWordSentiment("  HaRD")),
                "Single word score must be evaluated correctly");
    }

    @Test
    void testGetWordSentimentNeutral() {
        assertEquals(2.0, Math.round(movieReviewSentimentAnalyzer.getWordSentiment("Big ")),
                "Single word score must be evaluated correctly");
    }

    @Test
    void testGetWordFrequencyWordIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getWordFrequency(null),
                "The word that is being evaluated must not be null");
    }

    @Test
    void testGetWordFrequencyWordIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getWordFrequency(""),
                "The word that is being evaluated must not be empty");
    }

    @Test
    void testGetWordFrequencyWordIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.getWordFrequency("   "),
                "The word that is being evaluated must not be blank");
    }

    @Test
    void testGetWordFrequencyCorrect() {
        assertEquals(2, movieReviewSentimentAnalyzer.getWordFrequency(" fans"),
                "Word frequency must be evaluated correctly");
    }

    @Test
    void testGetWordFrequencyNotFound() {
        assertEquals(0, movieReviewSentimentAnalyzer.getWordFrequency("java"),
                "Word frequency must be evaluated correctly");
    }

    @Test
    void testGetMostFrequentWordsTopThree() {
        List<String> mostFrequent = movieReviewSentimentAnalyzer.getMostFrequentWords(3);

        assertEquals("even", mostFrequent.get(0), "Most frequent words must be evaluated correctly");
        assertEquals("one", mostFrequent.get(1), "Most frequent words must be evaluated correctly");
        assertEquals("big", mostFrequent.get(2), "Most frequent words must be evaluated correctly");

    }

    @Test
    void testGetMostFrequentWordsNegativeNumber() {
        assertThrows(IllegalArgumentException.class, () -> movieReviewSentimentAnalyzer.getMostFrequentWords(-1),
                "Most frequent words must be a non-negative number");
    }

    @Test
    void testGetMostPositiveWordsTopOneHundred() {
        List<String> mostPositive = movieReviewSentimentAnalyzer.getMostPositiveWords(100);

        assertEquals(Math.round(movieReviewSentimentAnalyzer.getWordSentiment(mostPositive.get(3))),
                4.0, "Most positive words must be evaluated correctly");
    }

    @Test
    void testGetMostPositiveWordsNegativeNumber() {
        assertThrows(IllegalArgumentException.class, () -> movieReviewSentimentAnalyzer.getMostPositiveWords(-1),
                "Most positive words must be a non-negative number");
    }

    @Test
    void testGetMostNegativeWordsTopTen() {
        List<String> mostNegative = movieReviewSentimentAnalyzer.getMostNegativeWords(10);

        assertEquals(Math.round(movieReviewSentimentAnalyzer.getWordSentiment(mostNegative.get(9))),
                0.0, "Most negative words must be evaluated correctly");
    }

    @Test
    void testGetMostNegativeWordsNegativeNumber() {
        assertThrows(IllegalArgumentException.class, () -> movieReviewSentimentAnalyzer.getMostNegativeWords(-1),
                "Most negative words must be a non-negative number");
    }

    @Test
    void testAppendReviewExecutesSuccessfully() {
        assertTrue(movieReviewSentimentAnalyzer.appendReview("Some", 4),
                "Appending a review must be performed successfully");
    }

    @Test
    void testAppendReviewWordAddedToDictionary() {
        int oldSize = movieReviewSentimentAnalyzer.getSentimentDictionarySize();
        movieReviewSentimentAnalyzer.appendReview("picturesque", 4);
        int newSize = movieReviewSentimentAnalyzer.getSentimentDictionarySize();
        assertEquals(oldSize + 1, newSize, "Dictionary must be updated after adding a new word");
    }

    @Test
    void testAppendReviewWordChangeExistingWordScore() {
        double oldSentiment = movieReviewSentimentAnalyzer.getWordSentiment("movie");
        movieReviewSentimentAnalyzer.appendReview("This movie", 4);
        double newSentiment = movieReviewSentimentAnalyzer.getWordSentiment("movie");
        assertTrue(newSentiment - oldSentiment > 0,
                "Adding a review with an existing word is expected to change its sentiment score");
    }

    @Test
    void testAppendReviewWordReviewIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.appendReview("", 1),
                "A review that is being appended cannot be empty");
    }

    @Test
    void testAppendReviewWordReviewIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.appendReview("   ", 1),
                "A review that is being appended cannot be blank");
    }

    @Test
    void testAppendReviewWordReviewIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.appendReview(null, 1),
                "A review that is being appended cannot be null");
    }

    @Test
    void testAppendReviewWordSentimentIsBigger() {
        assertThrows(IllegalArgumentException.class,
                () -> movieReviewSentimentAnalyzer.appendReview("Some review", 5),
                "A review that is being appended's sentiment must be within the limits");
    }

    @Test
    void testGetWordSentimentRepeatingWord() {
        movieReviewSentimentAnalyzer.appendReview("test that", 1);
        movieReviewSentimentAnalyzer.appendReview("test test", 3);

        assertTrue(movieReviewSentimentAnalyzer.getWordFrequency("test") == 3 &&
                        Double.compare(movieReviewSentimentAnalyzer.getWordSentiment("test"), 2.0) == 0,
                "Adding a review with a repeating word must be " +
                        "handled correctly for frequency and sentiment score");

    }

    @Test
    void testGetReviewSentimentRepeatingWord() {
        assertEquals(movieReviewSentimentAnalyzer.getReviewSentiment("independent independent energy"), 2.0,
                "A sentiment of a review with a repeating word must be evaluated correctly");
    }

    @Test
    void testGetWordSentimentRepeatingWords() {
        movieReviewSentimentAnalyzer.appendReview("word1 word2 word1", 4);
        movieReviewSentimentAnalyzer.appendReview("word1", 3);
        movieReviewSentimentAnalyzer.appendReview("word2, word2", 1);
        assertEquals(3.5, movieReviewSentimentAnalyzer.getWordSentiment("word1"),
                "A sentiment score of a repeating word must be calculated correctly");
        assertEquals(2.5, movieReviewSentimentAnalyzer.getWordSentiment("word2"),
                "A sentiment score of a repeating word must be calculated correctly");
    }

}
