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
public class CheckoutScreenRequest extends LostVictoryMessage {
    private final UUID avatar;

    public CheckoutScreenRequest(UUID clientID, UUID avatar) {
        super(clientID);
        this.avatar = avatar;
    }
    
    
}
