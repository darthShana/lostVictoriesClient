/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class DeathNotificationRequest extends LostVictoryMessage{
    private final UUID killer;
    private final UUID victim;

    public DeathNotificationRequest(UUID clientID, UUID killer, UUID victim) {
        super(clientID);
        this.killer = killer;
        this.victim = victim;        
    }
    
}
