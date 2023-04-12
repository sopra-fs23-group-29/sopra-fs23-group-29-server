package ch.uzh.ifi.hase.soprafs23.game.entity;

public class CountryCard {

  private String countryCode;
  private String countryName;
  private int countryArea;
  private int countryPopulation;
  private int countryGini;
  private int nBorders;
  private boolean landlocked;

  public CountryCard(
    String countryCode,
    String countryName,
    int countryArea,
    int countryPopulation,
    int countryGini,
    int nBorders,
    boolean landlocked
  ) {
    this.countryCode = countryCode;
    this.countryName = countryName;
    this.countryArea = countryArea;
    this.countryPopulation = countryPopulation;
    this.countryGini = countryGini;
    this.nBorders = nBorders;
    this.landlocked = landlocked;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryArea(int countryArea) {
    this.countryArea = countryArea;
  }

  public int getCountryArea() {
    return countryArea;
  }

  public void setCountryPopulation(int countryPopulation) {
    this.countryPopulation = countryPopulation;
  }

  public int getCountryPopulation() {
    return countryPopulation;
  }

  public void setCountryGini(int countryGini) {
    this.countryGini = countryGini;
  }

  public int getCountryGini() {
    return countryGini;
  }

  public void setnBorders(int nBorders) {
    this.nBorders = nBorders;
  }

  public int getnBorders() {
    return nBorders;
  }

  public void setLandlocked(boolean landlocked) {
    this.landlocked = landlocked;
  }

  public boolean getLandlocked() {
    return landlocked;
  }

}
