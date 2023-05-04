package ch.uzh.ifi.hase.soprafs23.constant;

public enum MaxDuration {
    NA(0),
    SHORT(3),
    MEDIUM(10),
    LONG(20);

    private int maxDuration;
    MaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }
}
