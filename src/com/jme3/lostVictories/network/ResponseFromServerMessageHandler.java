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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
    private Map<UUID, Long> receivedMessages = new HashMap<>();
    private Map<UUID, CharacterMessage> relatedCharacters = new HashMap<>();
    
    public ResponseFromServerMessageHandler(LostVictory app, CharacterLoader characterLoader, UUID clientID, ParticleManager particleManager, HeadsUpDisplayAppState hud) {
        this.clientID = clientID;
        this.app = app;
        this.characterLoader = characterLoader;     
        this.particleManager = particleManager;
        this.hud = hud;
        this.serverMessageAssembler = new ServerMessageAssembler();
    }
    
    public void syncroniseWithServerView(){
        final WorldMap worldMap = WorldMap.get();
        final ServerResponse popResponces = serverMessageAssembler.popResponces();
        
        popResponces.getAllRelatedUnits().forEach(c->{
            c.setCreationTime(System.currentTimeMillis());
            relatedCharacters.put(c.getId(), c);
        });
        relatedCharacters = relatedCharacters.values().stream()
                .filter(value->System.currentTimeMillis()-value.getCreationTime()<5000)
                .collect(Collectors.toMap(r->r.getId(), Function.identity()));
        
        popResponces.getAllUnits().forEach(msg -> {
            receivedMessages.put(msg.getId(), System.currentTimeMillis());
           
            GameCharacterNode clientView = worldMap.getCharacter(msg.getId());
            if(clientView!=null){
                if(msg.isDead()){
                    if(!clientView.isDead()){
                        clientView.playDistroyAnimation(clientView.getPositionToTarget(clientView));
                        clientView.doDeathEffects();
                    }
                    worldMap.removeCharacter(clientView);
                }else{
                    if(!clientView.isSameRank(msg)){
                        particleManager.playPromotionEffect(clientView);
                        characterLoader.destroyCharacter(clientView);
                    }else if(!clientView.hasSameWeapon(msg)){
                        characterLoader.destroyCharacter(clientView);
                    }else{
                        updateOnSceneCharacter(clientView, msg);
                    }
                }
            } 
            
            
            if(worldMap.getCharacter(msg.getId())==null && !msg.isDead()){
                try {
                    GameCharacterNode n = characterLoader.loadCharacter(msg, clientID);
                    n.checkForNewObjectives(msg.getObjectives());
                    worldMap.addCharacter(n);
                    if(app.chaseCameraAppState != null && app.chaseCameraAppState.isSelected(n.getIdentity())){
                        app.chaseCameraAppState.selectCharacter(n);
                    }
                    if(n instanceof AvatarCharacterNode){
                        System.out.println("reincatnate avatar:"+msg.getLocation());
                        app.setAvatar((AvatarCharacterNode) n);
                        app.avatar.updateHeadsUpDisplay();
                        
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        
        for(Iterator<Entry<UUID, Long>> it = receivedMessages.entrySet().iterator();it.hasNext();){
            final Entry<UUID, Long> next = it.next();
            final GameCharacterNode character = WorldMap.get().getCharacter(next.getKey());
            if(character!=null && System.currentTimeMillis()-next.getValue()>3000){
                characterLoader.destroyCharacter(character);
                it.remove();
            }
        }
        
    }

    public void syncroniseWithServerViewOld() {
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
//        System.out.println("com.jme3.lostVictories.network.ResponseFromServerMessageHandler.messageReceived()");

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

            
//            System.out.println("message decompressed:"+sb);
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

    private boolean hasSameUnits(CommandingOfficer c, Set<UUID> unitsUnderCommand, WorldMap worldMap, Map<UUID, CharacterMessage> relatedCharacters) {
        Set<UUID> localSquad = new HashSet<UUID>();
        for(Commandable u:c.getCharactersUnderCommand()){
            localSquad.add(u.getIdentity());
            if(!unitsUnderCommand.contains(u.getIdentity())){
                return false;
            }else if(u instanceof VirtualGameCharacterNode && worldMap.getCharacter(u.getIdentity())!=null){
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
        Commandable oldCO = n.getCommandingOfficer();
        WorldMap worldMap = WorldMap.get();
        Commandable newCo = worldMap.getCharacter(cMessage.getCommandingOfficer());
        
        if(oldCO!=null && cMessage.getCommandingOfficer()==null){
            n.setCommandingOfficer(null);
        }else if(oldCO==null && cMessage.getCommandingOfficer()!=null && newCo instanceof CommandingOfficer){
            n.setCommandingOfficer((CommandingOfficer) newCo);            
        }else if(oldCO!=null && cMessage.getCommandingOfficer()!=null && !oldCO.getIdentity().equals(cMessage.getCommandingOfficer())){
            if(newCo!=null && newCo instanceof CommandingOfficer){
                n.setCommandingOfficer((CommandingOfficer) newCo);
            }
        }
        
        if(worldMap.getCharacter(cMessage.getCommandingOfficer())==null && relatedCharacters.containsKey(cMessage.getCommandingOfficer())){
            n.setCommandingOfficer(new VirtualGameCharacterNode(relatedCharacters.get(cMessage.getCommandingOfficer()), false));
        }else if(worldMap.getCharacter(cMessage.getCommandingOfficer())!=null && n.getCommandingOfficer() instanceof VirtualGameCharacterNode){
            n.setCommandingOfficer((CommandingOfficer) worldMap.getCharacter(cMessage.getCommandingOfficer()));
        }
        
        if(n instanceof CommandingOfficer && !hasSameUnits((CommandingOfficer) n, cMessage.getUnitsUnderCommand(), worldMap, relatedCharacters)){
            ((CommandingOfficer)n).removeAllUnits();
            for(UUID u:cMessage.getUnitsUnderCommand()){
                GameCharacterNode unit = worldMap.getCharacter(u);
                if(unit!=null){
                    ((CommandingOfficer)n).addCharactersUnderCommand(unit);
                }else if(relatedCharacters.get(u)!=null){
                    boolean selected = false;
//                    if(u.equals(removedSelectedCharacter)){
//                        selected = true;
//                    }
                    final VirtualGameCharacterNode virtualGameCharacterNode = new VirtualGameCharacterNode(relatedCharacters.get(u), selected);
                    ((CommandingOfficer)n).addCharactersUnderCommand(virtualGameCharacterNode);
                    if(selected){
                        app.chaseCameraAppState.selectCharacter(virtualGameCharacterNode);
                    }
                }
            }
        }
        
        if(!n.getCountry().name().equals(cMessage.getCountry().name())){
            n.setCountry(Country.valueOf(cMessage.getCountry().name()));
        }
        
        if(cMessage.getBoardedVehicle()!=null){
            GameVehicleNode vehicle = (GameVehicleNode)worldMap.getCharacter(cMessage.getBoardedVehicle());
            final GameCharacterNode passenger = worldMap.getCharacter(cMessage.getId());
            if(vehicle!=null && passenger!=null && passenger.getBoardedVehicle()!=vehicle){
                vehicle.boardPassenger(passenger);
            }
        }else{
            GameCharacterNode nn = worldMap.getCharacter(cMessage.getId());
            if(nn!=null && nn.getBoardedVehicle()!=null){
                nn.disembarkVehicle();            
            }
        }
        
        if(n instanceof GameVehicleNode){
            ((GameVehicleNode)n).synchronisePassengers(cMessage.getPassengers());
        }
        
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
                
            }else{
                localCharacters.put(n.getIdentity(), n);
                
            }

        } 
        
    }

    public ServerResponse getServerResponces() {
        return serverMessageAssembler.popResponces();
    }

    
}
