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
public class GameStatistics implements Serializable{

    private Long blueHouses;
    private Long redHouses;
    private Integer blueVictoryPoints;
    private Integer redVictoryPoints;
    private Long avatarRespawnEstimate;
    
    public Long getBlueHouses(){
        return blueHouses;
    }
    
    public Long getRedHouses(){
        return redHouses;
    }
    
    public Integer getBlueVictoryPoints(){
        return blueVictoryPoints;
    }
    
    public Integer getRedVictoryPoints(){
        return redVictoryPoints;
    }
    
    public Long getAvatarReswapnInterval(){
        return avatarRespawnEstimate;
    }

}
