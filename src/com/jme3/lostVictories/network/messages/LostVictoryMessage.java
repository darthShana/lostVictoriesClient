/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class LostVictoryMessage implements Serializable{
    
    private UUID clientID;
    
    public LostVictoryMessage(UUID clientID) {
        this.clientID = clientID;
    }

    public UUID getClientID() {
        return clientID;
    }
        
}
