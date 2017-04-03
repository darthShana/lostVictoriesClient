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
import com.jme3.lostVictories.network.messages.wrapper.HouseStatusResponse;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.wrapper.TreeStatusResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 * @author dharshanar
 */
public class ServerMessageAssembler {

    private Map<UUID, CharacterMessage> characters = new HashMap<>();
    private Map<UUID, UnClaimedEquipmentMessage> equipment = new HashMap<>();
    private Map<UUID, TreeGroupMessage> trees = new HashMap<>();
    private Map<UUID, HouseMessage> houses = new HashMap<>();
    int count =0;
    
    public ServerMessageAssembler() {
        
    }

    void append(LostVictoryMessage message) {
        count++;
        synchronized(this){
            if(message instanceof CharacterStatusResponse){
                ((CharacterStatusResponse) message).getCharacters().forEach(
                    cm->{
                        characters.put(cm.getId(), cm);
                    }
                );
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
            System.out.println("received mesage count:"+count);
            
        }
    }

    ServerResponse popResponces() {
        synchronized(this){
            ServerResponse ret = new ServerResponse(UUID.randomUUID(), 
                    new HashSet<>(characters.values()), 
                    new HashSet<>(houses.values()), 
                    new HashSet<>(equipment.values()), 
                    new HashSet<>(trees.values()));
            
            characters.clear();
            houses.clear();
            equipment.clear();
            trees.clear();
            return ret;
        }
    }
    
}
