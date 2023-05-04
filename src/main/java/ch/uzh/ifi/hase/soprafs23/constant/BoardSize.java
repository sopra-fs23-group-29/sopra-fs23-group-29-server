package ch.uzh.ifi.hase.soprafs23.constant;

public enum BoardSize {

    DUMMY_TEST(0),
    SMALL(12),
    MEDIUM(24),
    LARGE(48);

    private int boardSize;

    BoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public int getBoardSize() {return this.boardSize;}
    public void setBoardSize(int boardSize) {this.boardSize = boardSize;}
}
