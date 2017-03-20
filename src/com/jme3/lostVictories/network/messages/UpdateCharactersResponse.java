/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class UpdateCharactersResponse extends LostVictoryMessage{
    private Set<CharacterMessage> allCharacters;
    private Set<CharacterMessage> relatedCharacters;
    private Set<HouseMessage> allHouses;
    private GameStatistics gameStatistics;
    private AchivementStatus achivementStatus;
    private Set<UnClaimedEquipmentMessage> unClaimedEquipment;
    private List<String> messages;
    
    public UpdateCharactersResponse(UUID clientId, Set<CharacterMessage> allCharacters) {
        super(clientId);
        this.allCharacters = allCharacters;

    }
    
    public UpdateCharactersResponse(UUID clientId, Set<CharacterMessage> allCharacters, Set<CharacterMessage> relatedCharacters, Set<UnClaimedEquipmentMessage> unClaimedEquipment) {
        super(clientId);
        this.allCharacters = allCharacters;
        this.relatedCharacters = relatedCharacters;
        this.unClaimedEquipment = unClaimedEquipment;

    }

    public Set<CharacterMessage> getAllUnits() {
        return allCharacters;
    }
    
    public Set<CharacterMessage> getAllRelatedCharacters(){
        return relatedCharacters;
    }
    
    public Set<HouseMessage> getAllHouses(){
        return allHouses;
    }
    
    public void setAllCharacters(Set<CharacterMessage> allCharacters){
        this.allCharacters = allCharacters;
    }
    
    public GameStatistics getGameStatistics(){
        return gameStatistics;
    }

    public AchivementStatus getAchivementStatus() {
        return achivementStatus;
    }
    
    public Set<UnClaimedEquipmentMessage> getUnclaimedEquipment(){
        return unClaimedEquipment;
    }
    
    public List<String> getMessages(){
        return messages;
    }
}
