/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

/**
 *
 * @author dharshanar
 */
public class AchivementStatus implements Serializable{
    private String achivementStatusText;
    private int achivementTotal;
    private int achivementCurrent;
    private long sentTIme;
    
    public String getAchivementStatusText() {
        return achivementStatusText;
    }

    public double getAchivementPercentage() {
        return ((double)achivementCurrent*100)/achivementTotal;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof AchivementStatus)){
            return false;
        }
        AchivementStatus other = (AchivementStatus) obj;
        return achivementStatusText.equals(other.achivementStatusText) &&
                achivementCurrent == other.achivementCurrent &&
                achivementTotal == other.achivementTotal;
    }

    @Override
    public int hashCode() {
        return achivementStatusText.hashCode()+(achivementCurrent+achivementTotal);
    }
    
    
    
    
    
}
