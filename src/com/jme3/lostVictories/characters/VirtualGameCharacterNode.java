/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.NetworkClientAppState;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.objectives.AttackObjective;
import com.jme3.lostVictories.objectives.CollectEquipment;
import com.jme3.lostVictories.objectives.EnemyActivityReport;
import com.jme3.lostVictories.objectives.FollowCommander;
import com.jme3.lostVictories.objectives.NavigateObjective;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.lostVictories.objectives.TravelObjective;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class VirtualGameCharacterNode implements Commandable, CommandingOfficer{
    private CharacterMessage msg;
    private boolean selected;
    private Geometry geometry;

    public VirtualGameCharacterNode(CharacterMessage msg, boolean selected) {
        this.msg = msg;
        this.selected = selected;
    }

    public Map<UUID, Objective> getAllObjectives() {
        return new HashMap<UUID, Objective>();
    }

    public void addObjective(Objective objective) {
        final ObjectNode valueToTree = objective.toJson();
        valueToTree.put("class", objective.getClass().getName());
        try {
            final NetworkClientAppState get = NetworkClientAppState.get();
            if(get!=null){
                get.addObjective(msg.getId(), objective.getIdentity(), MAPPER.writeValueAsString(valueToTree));
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter) {
        travel(mousePress, issuingCharacter);
    }

    public void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter) {
        if(msg.getType() == CharacterType.SOLDIER){
            addObjective(new TravelObjective(this, contactPoint, null));
        }else{
            addObjective(new NavigateObjective(contactPoint, null));
        }
        
        if(msg.getRank() == RankMessage.CADET_CORPORAL){
            for(UUID unit:msg.getUnitsUnderCommand()){
                final ObjectNode valueToTree = FollowCommander.toJson(msg.getId(), new Vector3f(2, 0, 2), 5);
                valueToTree.put("class", FollowCommander.class.getName());
                try {
                    NetworkClientAppState.get().addObjective(unit, UUID.randomUUID(), MAPPER.writeValueAsString(valueToTree));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        
    }

    public void follow(GameCharacterNode toFollow, GameCharacterNode issuingCharacter) {
        Vector3f f = new Vector3f(1, 0, 1).mult((float)Math.random());
        addObjective(new FollowCommander(f, 5));
    }

    public void attack(Vector3f target, GameCharacterNode issuingCharacter) {
        addObjective(new AttackObjective(target));      
    }
    
    public void collect(Pickable pickable, GameCharacterNode issuingCharacter) {
        addObjective(new CollectEquipment(pickable));
    }

    public void requestBoarding(GameVehicleNode key, GameCharacterNode issuingCharacter) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    

    public UUID getIdentity() {
        return msg.getId();
    }

    public int getKillCount() {
        return 0;
    }

    public void resetKillCount() {}
    
    String unitName;

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Weapon getWeapon() {
        return Weapon.get(msg.getWeapon());
    }

    public SquadType getSquadType(SquadType current, boolean expanded) {
        if(msg.getSquadType()==SquadType.ANTI_TANK_GUN){
            current = SquadType.ANTI_TANK_GUN;
        }else if(msg.getSquadType()==SquadType.ARMORED_VEHICLE && current!=SquadType.ANTI_TANK_GUN){
            current = SquadType.ARMORED_VEHICLE;
        }else if(msg.getSquadType()==SquadType.MORTAR_TEAM && current!=SquadType.ARMORED_VEHICLE && current!=SquadType.ANTI_TANK_GUN){
            current = SquadType.MORTAR_TEAM;
        }else if(msg.getSquadType()==SquadType.MG42_TEAM && current!=SquadType.ARMORED_VEHICLE && current!=SquadType.ANTI_TANK_GUN){
            current = SquadType.MG42_TEAM;
        }
        return current;
    }

    public boolean isHuman() {
        return msg.getType() == CharacterType.SOLDIER || msg.getType() == CharacterType.AVATAR;
    }
    
    public boolean isSelected() {
        return selected;
    }

    public boolean isBusy() {
        for(String s:msg.getObjectives().values()){
            if(!isPassiveObjective(toJsonNodeSafe(s))){
                return true;
            }
        }
        return false;
    }

    public boolean isAttacking() {
        for(String s:msg.getObjectives().values()){
            if(isAttackingObjective(toJsonNodeSafe(s))){
                return true;
            }
        }
        return false;
    }
    
    

    public Country getCountry() {
        return Country.valueOf(msg.getCountry().name());
    }
    
    
    
    private boolean isPassiveObjective(JsonNode n) {
        String s = n.get("class").asText();
        return "com.jme3.lostVictories.objectives.SurvivalObjective".equals(s) || "com.jme3.lostVictories.objectives.RemanVehicle".equals(s);
    }

    
    private boolean isAttackingObjective(JsonNode n) {
        String s = n.get("class").asText();
        return  "com.jme3.lostVictories.objectives.AttackBoggies".equals(s) || 
                "com.jme3.lostVictories.objectives.AttackAndTakeCoverObjective".equals(s) ||
                "com.jme3.lostVictories.objectives.AttackObjective".equals(s);
    }
    
    private JsonNode toJsonNodeSafe(String s) {
        try {
            return MAPPER.readTree(s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMessage(CharacterMessage get) {
        this.msg = get;
    }

    public Geometry unSelect() {
        selected = false;
        return geometry;
    }

    public Commandable select(Commandable selectedCharacter) {
        if(this == selectedCharacter){
            return this;
        }
        selected = true;
        if(selectedCharacter != null){
            Geometry g = selectedCharacter.unSelect();
            this.geometry = g;
        }
        return this;    
    }

    public boolean isUnderChainOfCommandOf(GameCharacterNode avatar, int i) {
        return true;
    }

    public Rank getRank() {
        if(RankMessage.COLONEL == msg.getRank()){
            return Rank.COLONEL;
        }else if(RankMessage.LIEUTENANT == msg.getRank()){
            return Rank.LIEUTENANT;
        }else if(RankMessage.CADET_CORPORAL == msg.getRank()){
            return Rank.CADET_CORPORAL;
        }else {
            return Rank.PRIVATE;
        }
    }

    public Vector3f getLocalTranslation() {
        return new Vector3f(msg.getLocation().x, msg.getLocation().y, msg.getLocation().z);
    }

    public Set<Commandable> getCharactersUnderCommand() {
        return new HashSet<Commandable>();
    }

    public void addCharactersUnderCommand(Set<Commandable> cc) {}

    public void addCharactersUnderCommand(Commandable c) {}

    public EnemyActivityReport getEnemyActivity() {
        return new EnemyActivityReport(new HashSet<Vector3f>(), new HashSet<Vector3f>());
    }

    public void removeCharacterUnderCommand(Commandable aThis) {}

    public void removeAllUnits() {}

    public boolean isTeam(Weapon... weapons) {
        return false;
    }

    public boolean isDead() {
        return msg.isDead();
    }

    public int getCurrentStrength() {
        return 1+msg.getUnitsUnderCommand().size();
    }

    public boolean hasBoardedVehicle() {
        return msg.getBoardedVehicle()!=null;
    }
}
