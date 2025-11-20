package com.golf.dto;

public class HoleScoreDTO {
    private Integer holeNumber;
    private Integer strokes;
    private Integer par;
    
    // Getters and Setters
    public Integer getHoleNumber() {
        return holeNumber;
    }
    
    public void setHoleNumber(Integer holeNumber) {
        this.holeNumber = holeNumber;
    }
    
    public Integer getStrokes() {
        return strokes;
    }
    
    public void setStrokes(Integer strokes) {
        this.strokes = strokes;
    }
    
    public Integer getPar() {
        return par;
    }
    
    public void setPar(Integer par) {
        this.par = par;
    }
}