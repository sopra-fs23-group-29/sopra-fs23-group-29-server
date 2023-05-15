package ch.uzh.ifi.hase.soprafs23.game.entity;

import java.util.Map;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Country {

    /*@JsonProperty("name")
    private String countryName;*/
    @JsonProperty("name")
    private Map<String, Object> nameMap;
    @JsonProperty("area")
    private Double area;
    @JsonProperty("population")
    private Long population;
    @JsonProperty("gini")
    private Map<String, Double> giniMap;
    @JsonProperty("capitalInfo")
    private Map<String, Object> capitalInfoMap;
    @JsonProperty("flags")
    private Map<String, Object> flagsMap;
    @JsonProperty("cioc")
    private String cioc;
    @JsonProperty("borders")
    private List<String> bordersList;
    @JsonProperty("landlocked")
    private Boolean landlocked;

    private Double gini;
    private String giniYear;
    private Double capitalLatitude;
    private String flagUrl;
    private String name;
    private Double populationDensity;


    public String getName() {
        if (nameMap == null || nameMap.size() == 0) {return null;}
        return (String) nameMap.get("common");
    }

    public Double getArea() {
        if (area == null) {return null;}
        return area;
    }

    public Long getPopulation() {
        if (population == null) {return null;}
        return population;
    }

    public Double getGini() {
        if (giniMap == null || giniMap.size() == 0) {return null;}
        return giniMap.values().iterator().next();
    }

    public String getGiniYear() {
        if (giniMap == null || giniMap.size() == 0) {return null;}
        return giniMap.keySet().iterator().next();
    }

    public Double getCapitalLatitude() {
        if (capitalInfoMap == null || capitalInfoMap.size() == 0 || !capitalInfoMap.containsKey("latlng")) {return null;}
        try {
            return ((List<Double>) capitalInfoMap.get("latlng")).get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getFlagUrl() {
        if (flagsMap == null || flagsMap.size() == 0) {return null;}
        return (String) flagsMap.get("svg");
    }

    public String getCioc() {
        if (cioc == null) {return null;}
        return cioc;
    }

    public Double getPopulationDensity() {
        if (area == null || population == null) {return null;}
        return population / area;
    }

    public Integer getNBorders() {
        if (bordersList == null) {return null;}
        return bordersList.size();
    }

    public Boolean getLandlocked() {
        if (landlocked == null) {return null;}
        return landlocked;
    }

    public void refreshDataFromNestedObjects() {
        this.gini = getGini();
        this.giniYear = getGiniYear();
        this.capitalLatitude = getCapitalLatitude();
        this.flagUrl = getFlagUrl();
        this.name = getName();
        this.populationDensity = getPopulationDensity();
    }
    
}

