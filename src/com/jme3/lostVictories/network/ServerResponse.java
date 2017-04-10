/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.TreeGroupMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class ServerResponse {

    private final UUID id;
    private final HashSet<CharacterMessage> charcters;
    private final HashSet<CharacterMessage> relatedCharcters;
    private final HashSet<HouseMessage> houses;
    private final HashSet<UnClaimedEquipmentMessage> equipment;
    private final HashSet<TreeGroupMessage> trees;

    ServerResponse(UUID id, HashSet<CharacterMessage> charcters, HashSet<CharacterMessage> relatedCharcters, HashSet<HouseMessage> houses, HashSet<UnClaimedEquipmentMessage> equipment, HashSet<TreeGroupMessage> trees) {
        this.id = id;
        this.charcters = charcters;
        this.relatedCharcters = relatedCharcters;
        this.houses = houses;
        this.equipment = equipment;
        this.trees = trees;
        
    }

    public Collection<CharacterMessage> getAllUnits() {
        return charcters;
    }

   public Collection<CharacterMessage> getAllRelatedCharacters() {
        return relatedCharcters;
    }

    public Iterable<UnClaimedEquipmentMessage> getAllEquipment() {
        return equipment;
    }

    public Set<HouseMessage> getAllHouses() {
        return houses;
    }

    public Set<TreeGroupMessage> getAllTrees() {
        return trees;
    }

    GameStatistics getGameStatistics() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    AchievementStatus getAchivementStatus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    List<String> getMessages() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Iterable<UnClaimedEquipmentMessage> getUnclaimedEquipment() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Collection<CharacterMessage> getAllRelatedUnits() {
        return relatedCharcters;
    }
    
}
