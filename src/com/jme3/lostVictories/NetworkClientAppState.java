/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.app.state.AbstractAppState;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.network.NetworkClient;
import com.jme3.lostVictories.network.ResponseFromServerMessageHandler;
import com.jme3.lostVictories.network.ServerResponse;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


/**
 *
 * @author dharshanar
 */
public class NetworkClientAppState extends AbstractAppState {
    
    private static NetworkClientAppState instance;
    private static float CLIENT_RANGE = 251;
    
    private final LostVictory app;
    private long lastRunTime = System.currentTimeMillis();
    private final NetworkClient networkClient;
    private final ResponseFromServerMessageHandler responseHandler;


    public static NetworkClientAppState init(LostVictory app, NetworkClient networkClient, ResponseFromServerMessageHandler serverSync){
        instance = new NetworkClientAppState(app, networkClient, serverSync);
        return instance;
    }
    
    public static NetworkClientAppState get(){
        return instance;
    }
    private final Map<UUID, CharacterMessage> lastSent = new HashMap<>();
    private final long clientStartTime;
    
    private NetworkClientAppState(LostVictory app, NetworkClient networkClient, ResponseFromServerMessageHandler serverSync) {
        this.app = app;
        this.networkClient = networkClient;
        this.responseHandler = serverSync;
        this.clientStartTime = System.currentTimeMillis();
        //updateInProgress.offer(new ServerResponse(UUID.randomUUID(), new HashSet<CharacterMessage>()));
    }

    @Override
    public void update(float tpf) {
        final long currentTimeMillis = System.currentTimeMillis();
        responseHandler.syncroniseWithServerView();
            
        Set<GameCharacterNode> charactersInRange = WorldMap.get().getAllCharacters();
        Point.Float p = new Point.Float(app.avatar.getLocalTranslation().x, app.avatar.getLocalTranslation().z);
        Rectangle.Float r = new Rectangle.Float(p.x-CLIENT_RANGE, p.y-CLIENT_RANGE, CLIENT_RANGE*2, CLIENT_RANGE*2);

        //..why are we still sending documents with the same version number
        charactersInRange = charactersInRange.stream().filter(c->{
                return !c.isDead() && c.isControledLocaly() && r.contains(new Point.Float(c.getLocalTranslation().x, c.getLocalTranslation().z));
            }).collect(Collectors.toSet());
        if(WorldMap.get().getCharacter(app.avatar.getIdentity())==null){
            charactersInRange.add(app.avatar);
        }
        
        Set<CharacterMessage> toUpdate = charactersInRange.stream()
            .filter(hc->{
                return !lastSent.containsKey(hc.getIdentity()) || (hc.getVersion()>lastSent.get(hc.getIdentity()).getVersion()) || System.currentTimeMillis()-lastSent.get(hc.getIdentity()).getCreationTime()>2000;
            })
            .map(c->c.toMessage())
            .filter(m->{
                return !lastSent.containsKey(m.getId()) || !m.equals(lastSent.get(m.getId())) || System.currentTimeMillis()-lastSent.get(m.getId()).getCreationTime()>2000;
            })
            .collect(Collectors.toSet());

        try{
            if(!toUpdate.isEmpty()){
                networkClient.updateLocalCharacters(toUpdate, (app.avatar!=null)?app.avatar.getIdentity():null, clientStartTime);
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
//        toUpdate.forEach(cm->System.out.println("sending:"+cm.getId()+" version:"+cm.getVersion()+" creation:"+cm.getCreationTime()));
//        System.out.println("");
        toUpdate.forEach(cm->lastSent.put(cm.getId(), cm));

        lastRunTime = currentTimeMillis;    
            
        
        
    }
    
    

    @Override
    public void cleanup() {
        System.out.println("shutting down network client");
        networkClient.shutDown();
        super.cleanup();
    }

        
    public ServerResponse checkoutSceenSynchronous(UUID avatar) throws InterruptedException {
        System.out.println("sending checkout request");
        networkClient.checkoutSceen(avatar);
        Thread.sleep(5000);
        final ServerResponse serverResponces = responseHandler.getServerResponces();
        System.out.println("received checkout messages:"+responseHandler.getMessagesReceivedCouunt());
        return serverResponces;
    }

    public void notifyDeath(UUID killer, UUID victim) {
        networkClient.deathNotification(killer, victim);
    }
    
    public void notifyGunnerDeath(UUID killer, UUID victim) {
        networkClient.gunnerDeathNotification(killer, victim);
    }

    public void addObjective(UUID characterID, UUID identity, String toMessage) {
        networkClient.addObjective(characterID, identity, toMessage);
    }

    public void requestEquipmentCollection(UUID equipmentID, UUID characterID) {
        networkClient.requestEquipmentCollection(equipmentID, characterID);
    }

    public void requestBoardVehicle(UUID vehicleID, UUID characterID){
        networkClient.boardVehicle(vehicleID, characterID);
    }

    public void disembarkPassengers(UUID identity) {
        networkClient.disembarkPassengers(identity);
    }

    public void messageReceived(LostVictoryMessage message) {
        
//        if(message instanceof UpdateCharactersResponse){
//            updateInProgress.offer((UpdateCharactersResponse) message);
//        }
//        if(message instanceof CheckoutScreenResponse){
//            synchronusResponseQueue.offer(message);
//        }
        
    }
    
    
}
