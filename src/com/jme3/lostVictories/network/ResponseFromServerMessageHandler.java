/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;


import com.jme3.lostVictories.CharacterLoader;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.HeadsUpDisplayAppState;
import com.jme3.lostVictories.LostVictory;
import com.jme3.lostVictories.NetworkClientAppState;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.WorldRunner;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.CommandingOfficer;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.HalfTrackNode;
import com.jme3.lostVictories.characters.LocalAIBehaviourControler;
import com.jme3.lostVictories.characters.RemoteBehaviourControler;
import com.jme3.lostVictories.characters.VirtualGameCharacterNode;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersResponse;
import com.jme3.lostVictories.structures.UnclaimedEquipmentNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 * @author dharshanar
 */
public class ResponseFromServerMessageHandler {
    private final UUID clientID;
    private final LostVictory app;
    private final CharacterLoader characterLoader;
    ConcurrentLinkedDeque<UpdateCharactersResponse> responseQueue = new ConcurrentLinkedDeque<UpdateCharactersResponse>();
    private final ParticleManager particleManager;
    private final HeadsUpDisplayAppState hud;
    private NetworkClientAppState messageReceivedListener;
    
    public ResponseFromServerMessageHandler(LostVictory app, CharacterLoader characterLoader, UUID clientID, ParticleManager particleManager, HeadsUpDisplayAppState hud) {
        this.clientID = clientID;
        this.app = app;
        this.characterLoader = characterLoader;     
        this.particleManager = particleManager;
        this.hud = hud;        
    }

    public void syncroniseWithServerView() {
        UpdateCharactersResponse msg = responseQueue.peekLast();
        responseQueue.clear();
        
        if(msg!=null){
            UUID removedSelectedCharacter = null;
            WorldRunner.get().setGameStatistics(msg.getGameStatistics());
            WorldRunner.get().setAchiveemntStatus(msg.getAchivementStatus());
            if(msg.getMessages()!=null){
                hud.addMessage(msg.getMessages().toArray(new String[]{}));
            }
            
            WorldMap worldMap = WorldMap.get();
            Set<UUID> fromMsg = new HashSet<UUID>();
            for(UnClaimedEquipmentMessage eq:msg.getUnclaimedEquipment()){
                fromMsg.add(eq.getId());
                if(!worldMap.hasUnclaimedEquipment(eq)){
                    characterLoader.laodUnclaimedEquipment(eq);
                }
            }
            for(UnclaimedEquipmentNode n:worldMap.getAllEquipment()){
                if(!fromMsg.contains(n.getId())){
                    n.destroy();
                    worldMap.removeEquipment(n);
                }
            }
            syncronizeCharacters(msg, worldMap, removedSelectedCharacter);
                  
            for(HouseMessage structure:msg.getAllHouses()){
                worldMap.getHouse(structure.getId()).updateOwership(structure);
            }
                                    
        }
        
    }

    void handle(UpdateCharactersResponse updateCharactersResponse) {
        responseQueue.addLast(updateCharactersResponse);
        if(messageReceivedListener!=null){
            messageReceivedListener.messageReceived(updateCharactersResponse);
        }
    }

    private boolean hasSameUnits(CommandingOfficer c, Set<UUID> unitsUnderCommand, Map<UUID, GameCharacterNode> allCharacters, Map<UUID, CharacterMessage> relatedCharacters) {
        Set<UUID> localSquad = new HashSet<UUID>();
        for(Commandable u:c.getCharactersUnderCommand()){
            localSquad.add(u.getIdentity());
            if(!unitsUnderCommand.contains(u.getIdentity())){
                return false;
            }else if(u instanceof VirtualGameCharacterNode && allCharacters.containsKey(u.getIdentity())){
                return false;
            }else if((u instanceof VirtualGameCharacterNode) && relatedCharacters.get(u.getIdentity())!=null){
                ((VirtualGameCharacterNode)u).updateMessage(relatedCharacters.get(u.getIdentity()));
            }
                    
        }
        
        for(UUID u:unitsUnderCommand){
            if(!localSquad.contains(u)){
                return false;
            }
        }
        return true;
    }

    private void updateOnSceneCharacter(GameCharacterNode n, final CharacterMessage cMessage) {
        
        n.initialiseKills(cMessage.getKills());
        n.setVersion(cMessage.getVersion());
        
        if(cMessage.shouldBeControledRemotely(clientID) && !n.isControledRemotely()){
            n.setBehaviourControler(new RemoteBehaviourControler(cMessage.getId(), cMessage));
        }
        if(!cMessage.shouldBeControledRemotely(clientID) && !n.isControledLocaly()){
            n.setBehaviourControler(new LocalAIBehaviourControler());
        }
        if(n.isControledRemotely()){
            ((RemoteBehaviourControler)n.getBehaviourControler()).updateRemoteState(cMessage);
        }else{
            try {
                n.checkForNewObjectives(cMessage.getObjectives());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if(n instanceof HalfTrackNode){
            if(cMessage.hasEngineDamage() && !((HalfTrackNode)n).hasEngineDamage()){
                ((HalfTrackNode)n).doEngineDamage();
            }
        }
                    
    }

    public void synchronizCharacter(CharacterMessage m, GameCharacterNode c, Map<UUID, GameCharacterNode> localCharacters, Map<UUID, CharacterMessage> relatedCharacters, UUID removedSelectedCharacter) {
        Commandable oldCO = c.getCommandingOfficer();
        if(oldCO!=null && m.getCommandingOfficer()==null){
            c.setCommandingOfficer(null);
        }else if(oldCO==null && m.getCommandingOfficer()!=null && localCharacters.get(m.getCommandingOfficer()) instanceof CommandingOfficer){
            final CommandingOfficer co = (CommandingOfficer) localCharacters.get(m.getCommandingOfficer());
            if(co!=null){
                c.setCommandingOfficer(co);
            }
        }else if(oldCO!=null && m.getCommandingOfficer()!=null && !oldCO.getIdentity().equals(m.getCommandingOfficer())){
            final GameCharacterNode get = localCharacters.get(m.getCommandingOfficer());
            if(get!=null && get instanceof CommandingOfficer){
                c.setCommandingOfficer((CommandingOfficer) get);
            }
        }else if(!localCharacters.containsKey(m.getCommandingOfficer()) && relatedCharacters.containsKey(m.getCommandingOfficer())){
            c.setCommandingOfficer(new VirtualGameCharacterNode(relatedCharacters.get(m.getCommandingOfficer()), false));
        }else if(localCharacters.containsKey(m.getCommandingOfficer()) && c.getCommandingOfficer() instanceof VirtualGameCharacterNode){
            c.setCommandingOfficer((CommandingOfficer) localCharacters.get(m.getCommandingOfficer()));
        }
                        
        if(c instanceof CommandingOfficer && !hasSameUnits((CommandingOfficer) c, m.getUnitsUnderCommand(), localCharacters, relatedCharacters)){
            ((CommandingOfficer)c).removeAllUnits();
            for(UUID u:m.getUnitsUnderCommand()){
                if(localCharacters.containsKey(u)){
                    final GameCharacterNode cnew = localCharacters.get(u);
                    ((CommandingOfficer)c).addCharactersUnderCommand(cnew);
                }else if(relatedCharacters.get(u)!=null){
                    boolean selected = false;
                    if(u.equals(removedSelectedCharacter)){
                        selected = true;
                    }
                    final VirtualGameCharacterNode virtualGameCharacterNode = new VirtualGameCharacterNode(relatedCharacters.get(u), selected);
                    ((CommandingOfficer)c).addCharactersUnderCommand(virtualGameCharacterNode);
                    if(selected){
                        app.chaseCameraAppState.selectCharacter(virtualGameCharacterNode);
                    }
                }
            }
        }
        
        if(!c.getCountry().name().equals(m.getCountry().name())){
            c.setCountry(Country.valueOf(m.getCountry().name()));
        }
        
        if(m.getBoardedVehicle()!=null){
            GameVehicleNode vehicle = (GameVehicleNode)localCharacters.get(m.getBoardedVehicle());
            final GameCharacterNode passenger = localCharacters.get(m.getId());
            if(vehicle!=null && passenger!=null && passenger.getBoardedVehicle()!=vehicle){
                vehicle.boardPassenger(passenger);
            }
        }else if(localCharacters.containsKey(m.getId()) && localCharacters.get(m.getId()).getBoardedVehicle()!=null){
            localCharacters.get(m.getId()).disembarkVehicle();            
        }
        
        if(c instanceof GameVehicleNode){
            ((GameVehicleNode)c).synchronisePassengers(m.getPassengers());
        }
    }

    void syncronizeCharacters(UpdateCharactersResponse msg, WorldMap worldMap, UUID removedSelectedCharacter) throws RuntimeException {
        Map<UUID, GameCharacterNode> localCharacters = new HashMap<UUID, GameCharacterNode>();
        Map<UUID, CharacterMessage> remoteCharacters = new HashMap<UUID, CharacterMessage>();
        Map<UUID, CharacterMessage> relatedCharacters = new HashMap<UUID, CharacterMessage>();
        Set<GameCharacterNode> newCharacters = new HashSet<GameCharacterNode>();
        
        for(CharacterMessage c:msg.getAllUnits()){
            remoteCharacters.put(c.getId(), c);
        }
        
        for(CharacterMessage c:msg.getAllRelatedCharacters()){
            relatedCharacters.put(c.getId(), c);
        }
        
        for(GameCharacterNode n: worldMap.getAllCharacters()){
            final CharacterMessage cMessage = remoteCharacters.get(n.getIdentity());
            if(cMessage==null){
                if(n.isSelected()){
                    removedSelectedCharacter = n.getIdentity();
                }
                characterLoader.destroyCharacter(n);
            }else if(cMessage.isDead()){
                n.playDistroyAnimation(n.getPositionToTarget(n));
                n.doDeathEffects();
                WorldMap.get().removeCharacter(n);
            }else if(!n.isSameRank(cMessage)){
                particleManager.playPromotionEffect(n);
                characterLoader.destroyCharacter(n);
            }else if(!n.hasSameWeapon(cMessage)){
                characterLoader.destroyCharacter(n);
            }else {
                localCharacters.put(n.getIdentity(), n);
                updateOnSceneCharacter(n, cMessage);
            }

        }
        boolean newAvatar = false;
        for(CharacterMessage message:msg.getAllUnits()){
            if(!localCharacters.containsKey(message.getId()) && !message.isDead()){
                GameCharacterNode n = characterLoader.loadCharacter(message, clientID);
                localCharacters.put(n.getIdentity(), n);
                newCharacters.add(n);
                if(app.chaseCameraAppState != null && app.chaseCameraAppState.isSelected(n.getIdentity())){
                    app.chaseCameraAppState.selectCharacter(n);
                }
                if(n instanceof AvatarCharacterNode){
                    System.out.println("reincatnate avatar:"+message.getLocation());
                    app.setAvatar((AvatarCharacterNode) n);  
                    newAvatar = true;
                }
            }

        }
            
        for(GameCharacterNode c:localCharacters.values()){
            CharacterMessage m = remoteCharacters.get(c.getIdentity());
            synchronizCharacter(m, c, localCharacters, relatedCharacters, removedSelectedCharacter);
                           
        }
        
        if(app.avatar!=null && newAvatar){
            app.avatar.updateHeadsUpDisplay();
        }
        
        for(GameCharacterNode n: newCharacters){
            try {                
                n.checkForNewObjectives(remoteCharacters.get(n.getIdentity()).getObjectives());
                worldMap.addCharacter(n);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void addListener(NetworkClientAppState appState) {
        this.messageReceivedListener = appState;
    }
    
}
