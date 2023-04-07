package ch.uzh.ifi.hase.soprafs23.game.entity;

import ch.uzh.ifi.hase.soprafs23.constant.GameMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dummy {

  private String dummyString;
  private int dummyInt;
  private List<String> dummyStringList;
  private GameMode dummyEnum;
  private List<DummyDuplicate> dummyDummyList;
  private List<DummyDuplicate> dummyDummyListEmpty;

  public Dummy() {
    this.dummyString = "dummyString";
    this.dummyInt = 1;
    this.dummyStringList = Arrays.asList("a","b");
    this.dummyEnum = GameMode.PVP;
    this.dummyDummyList = Arrays.asList(
            new DummyDuplicate(), new DummyDuplicate()
    );
  }

}
