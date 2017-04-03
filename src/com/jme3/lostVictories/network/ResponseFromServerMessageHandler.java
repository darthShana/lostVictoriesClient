/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

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
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.structures.UnclaimedEquipmentNode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 *
 * @author dharshanar
 */
public class ResponseFromServerMessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final UUID clientID;
    private final LostVictory app;
    private final CharacterLoader characterLoader;
    ConcurrentLinkedDeque<ServerResponse> responseQueue = new ConcurrentLinkedDeque<ServerResponse>();
    
    private final ParticleManager particleManager;
    private final HeadsUpDisplayAppState hud;
    private final ServerMessageAssembler serverMessageAssembler;
    
    public ResponseFromServerMessageHandler(LostVictory app, CharacterLoader characterLoader, UUID clientID, ParticleManager particleManager, HeadsUpDisplayAppState hud) {
        this.clientID = clientID;
        this.app = app;
        this.characterLoader = characterLoader;     
        this.particleManager = particleManager;
        this.hud = hud;
        this.serverMessageAssembler = new ServerMessageAssembler();
    }

    public void syncroniseWithServerView() {
        ServerResponse msg = responseQueue.peekLast();
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

    @Override
    protected void messageReceived(ChannelHandlerContext chc, DatagramPacket msg) throws Exception {
        System.out.println("com.jme3.lostVictories.network.ResponseFromServerMessageHandler.messageReceived()");

        try {
            int i = 0;
            byte[] incomming = new byte[msg.content().capacity()];
            while (msg.content().isReadable()) {
                incomming[i++] = msg.content().readByte();
            }
            
            ByteArrayInputStream bis = new ByteArrayInputStream(incomming);
            GZIPInputStream gis = new GZIPInputStream(bis);
            BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                    sb.append(line);
            }
            br.close();
            gis.close();
            bis.close();

//            Inflater inflater = new Inflater();   
//            inflater.setInput(incomming);  
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(incomming.length);  
//            byte[] buffer = new byte[2048];  
//            while (!inflater.finished()) {  
//                int count = inflater.inflate(buffer);  
//                outputStream.write(buffer, 0, count);  
//            }  
//            outputStream.close();  
//            byte[] output = outputStream.toByteArray();  

            
            System.out.println("message decompressed:"+sb);
            //ok lets do something with this
            
            LostVictoryMessage message = MAPPER.readValue(sb.toString(), LostVictoryMessage.class);
            serverMessageAssembler.append(message);
//            if(message instanceof UpdateCharactersResponse){
//                handle((UpdateCharactersResponse) message);
//            }
//            if(messageReceivedListener!=null){
//                messageReceivedListener.messageReceived(message);
//            }

        } catch (IOException e) {
            throw new AssertionError(e);
        }
        
        
    }
    
    void handle(ServerResponse updateCharactersResponse) {
        responseQueue.addLast(updateCharactersResponse);
        
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

    void syncronizeCharacters(ServerResponse msg, WorldMap worldMap, UUID removedSelectedCharacter) throws RuntimeException {
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

    public ServerResponse getServerResponces() {
        return serverMessageAssembler.popResponces();
    }

    
}
