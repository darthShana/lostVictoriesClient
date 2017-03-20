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
public class CheckoutScreenResponse extends LostVictoryMessage{
    private Set<CharacterMessage> allCharacters;
    private Set<HouseMessage> allHouses;
    private Set<UnClaimedEquipmentMessage> allEquipment;
    private Set<TreeGroupMessage> allTrees;

    public CheckoutScreenResponse(Set<CharacterMessage> allCharacters) {
        super(UUID.randomUUID());
        this.allCharacters = allCharacters;
    }

    public Set<CharacterMessage> getAllUnits() {
        return allCharacters;
    }
    
    public void setAllCharacters(Set<CharacterMessage> allCharacters){
        this.allCharacters = allCharacters;
    }

    public Set<HouseMessage> getAllHouses() {
        return allHouses;
    }
    
    public Set<UnClaimedEquipmentMessage> getAllEquipment(){
        return allEquipment;
    }
    
    public Set<TreeGroupMessage> getAllTrees(){
        return allTrees;
    }
}
