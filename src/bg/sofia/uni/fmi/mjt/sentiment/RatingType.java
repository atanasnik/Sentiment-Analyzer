package bg.sofia.uni.fmi.mjt.sentiment;

public enum RatingType {
    UNKNOWN(-1.0, "unknown"),
    NEGATIVE(0.0, "negative"),
    SOMEWHAT_NEGATIVE(1.0, "somewhat negative"),
    NEUTRAL(2.0, "neutral"),
    SOMEWHAT_POSITIVE(3.0, "somewhat positive"),
    POSITIVE(4.0, "positive");

    private final double ratingNumber;
    private final String ratingText;

    RatingType(double ratingNumber, String ratingText) {
        this.ratingNumber = ratingNumber;
        this.ratingText = ratingText;
    }

    public double getRatingNumber() {
        return ratingNumber;
    }

    public String getRatingText() {
        return ratingText;
    }

}
