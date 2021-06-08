package id1212;

import java.util.Random;

public class Guess {
  private final int answer = new Random().nextInt(100);

  private int numberOfGuess;
  private int guess;

  public int getAnswer() {
    return answer;
  }

  public int getNumberOfGuess() {
    return numberOfGuess;
  }

  public void setNumberOfGuess(int numberOfGuess) {
    this.numberOfGuess = numberOfGuess;
  }

  public int getGuess() {
    return guess;
  }

  public void setGuess(int guess) {
    this.guess = guess;
  }
}
