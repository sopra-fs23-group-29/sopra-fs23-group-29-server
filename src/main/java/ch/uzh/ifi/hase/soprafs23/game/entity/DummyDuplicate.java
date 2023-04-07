package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DummyDuplicate {

  private String dummyString;
  private int dummyInt;
  private List<String> dummyStringList;
  private GameMode dummyEnum;

  public DummyDuplicate() {
    this.dummyString = "duplicate!";
    this.dummyInt = 99;
    this.dummyStringList = Arrays.asList("duplicate","duplicate");
    this.dummyEnum = GameMode.HOWFAR;
  }

}
