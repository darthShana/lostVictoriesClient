/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class UpdateCharactersRequest extends LostVictoryMessage{
    private final Set<CharacterMessage> characters;
    private CharacterMessage avatar;

    public UpdateCharactersRequest(UUID clientID, Set<CharacterMessage> toUpdate, CharacterMessage avatar) {
        super(clientID);
        this.characters = toUpdate;
        this.avatar = avatar;
    }
    
}
