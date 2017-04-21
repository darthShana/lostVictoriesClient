/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.TreeGroupMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.wrapper.CharacterStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.EquipmentStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.GameStatsResponse;
import com.jme3.lostVictories.network.messages.wrapper.HouseStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.wrapper.RelatedCharacterStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.TreeStatusResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class ServerMessageAssembler {

    private Map<UUID, CharacterMessage> characters = new HashMap<>();
    private Map<UUID, UnClaimedEquipmentMessage> equipment = new HashMap<>();
    private Map<UUID, TreeGroupMessage> trees = new HashMap<>();
    private Map<UUID, HouseMessage> houses = new HashMap<>();
    private Map<UUID, CharacterMessage> relatedCharacters = new HashMap<>();
    private GameStatsResponse gameStatsResponse;
    
    public ServerMessageAssembler() {
        
    }

    void append(LostVictoryMessage message) {
        synchronized(this){
            if(message instanceof CharacterStatusResponse){
                CharacterMessage cm = ((CharacterStatusResponse) message).getCharacter();
                if(cm.getId().equals( UUID.fromString("2fbe421f-f701-49c9-a0d4-abb0fa904204"))){
                    System.out.println("received version:"+cm.getVersion()+" last sent version:");
                }
                characters.put(cm.getId(), cm);
            }else if(message instanceof RelatedCharacterStatusResponse){
                CharacterMessage cm = ((RelatedCharacterStatusResponse) message).getCharacter();
                relatedCharacters.put(cm.getId(), cm);
            }else if(message instanceof GameStatsResponse){
                gameStatsResponse = (GameStatsResponse) message;
            }else if(message instanceof EquipmentStatusResponse){
                ((EquipmentStatusResponse) message).getUnclaimedEquipment().forEach(
                    em->{
                        equipment.put(em.getId(), em);
                    }
                );
            }else if(message instanceof TreeStatusResponse){
                ((TreeStatusResponse) message).getTrees().forEach(
                    tm->{
                        trees.put(tm.getId(), tm);
                    }
                );
            }else if(message instanceof HouseStatusResponse){
                ((HouseStatusResponse) message).getHouses().forEach(
                    hm->{
                        houses.put(hm.getId(), hm);
                    }
                );
            }
            
        }
    }

    ServerResponse popResponces() {
        synchronized(this){
            ServerResponse ret = new ServerResponse(UUID.randomUUID(), 
                    new HashSet<>(characters.values()), 
                    new HashSet<>(relatedCharacters.values()),
                    new HashSet<>(houses.values()), 
                    new HashSet<>(equipment.values()), 
                    new HashSet<>(trees.values()),
                    (gameStatsResponse!=null)?gameStatsResponse.getMessages():null,
                    (gameStatsResponse!=null)?gameStatsResponse.getAchivementStatus():null,
                    (gameStatsResponse!=null)?gameStatsResponse.getGameStatistics():null);
            
            characters.clear();
            relatedCharacters.clear();
            houses.clear();
            equipment.clear();
            trees.clear();
            gameStatsResponse = null;
            return ret;
        }
    }
    
}
