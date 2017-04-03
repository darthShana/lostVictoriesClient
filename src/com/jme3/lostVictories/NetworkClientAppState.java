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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;


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
    final BlockingQueue<ServerResponse> updateInProgress = new LinkedBlockingQueue<ServerResponse>();
    
    private NetworkClientAppState(LostVictory app, NetworkClient networkClient, ResponseFromServerMessageHandler serverSync) {
        this.app = app;
        this.networkClient = networkClient;
        this.responseHandler = serverSync;
        //updateInProgress.offer(new ServerResponse(UUID.randomUUID(), new HashSet<CharacterMessage>()));
    }

    @Override
    public void update(float tpf) {
        final long currentTimeMillis = System.currentTimeMillis();
        responseHandler.syncroniseWithServerView();
        if(currentTimeMillis-lastRunTime>100 && updateInProgress.peek()!=null){
            updateInProgress.clear();
            
            Set<CharacterMessage> toUpdate = new HashSet<CharacterMessage>();
            
            final Iterable<GameCharacterNode> charactersInRange = WorldMap.get().getAllCharacters();
            Point.Float p = new Point.Float(app.avatar.getLocalTranslation().x, app.avatar.getLocalTranslation().z);
            Rectangle.Float r = new Rectangle.Float(p.x-CLIENT_RANGE, p.y-CLIENT_RANGE, CLIENT_RANGE*2, CLIENT_RANGE*2);
            
            for(GameCharacterNode c: charactersInRange){
                if(!c.isDead() && c.isControledLocaly() && r.contains(new Point.Float(c.getLocalTranslation().x, c.getLocalTranslation().z))){
                    toUpdate.add(c.toMessage());    
                }
            }
            
            try{
                if(!toUpdate.isEmpty()){
                    networkClient.updateLocalCharacters(toUpdate, (app.avatar!=null)?app.avatar.toMessage():null);
                }
            }catch(Throwable e){
                e.printStackTrace();
            }
            lastRunTime = currentTimeMillis;    
            
        }
        
        
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
        return responseHandler.getServerResponces();
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
