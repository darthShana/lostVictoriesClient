/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.network.messages.actions.Crouch;
import com.jme3.lostVictories.network.messages.actions.ManualControl;
import com.jme3.lostVictories.network.messages.actions.SetupWeapon;
import com.jme3.lostVictories.network.messages.actions.Shoot;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class CharacterMessage implements Serializable{

    UUID id;
    UUID userID;
    Vector location;
    Country country;
    Weapon weapon;
    RankMessage rank;
    UUID commandingOfficer;
    UUID boardedVehicle;
    Set<UUID> unitsUnderCommand = new HashSet<UUID>();
    Set<UUID> passengers = new HashSet<UUID>();
    UUID checkoutClient;
    Long checkoutTime;
    CharacterType type;
    Vector orientation;
    Set<Action> actions;
    Map<String, String> objectives;
    Set<String> completedObjectives;
    boolean dead;
    boolean engineDamaged;
    Long timeOfDeath;
    long version;
    Set<UUID> kills;
    SquadType squadType;

    private CharacterMessage(){}
    
    public CharacterMessage(UUID id, Vector location, Vector orientation, RankMessage rank, Set<Action> actions, Map<String, String> objectives, Set<String> completedObjectives, long version) {
        this.id = id;
        this.location = location;
        this.orientation = orientation;
        this.rank = rank;
        this.actions = actions;
        this.objectives = objectives;
        this.completedObjectives = completedObjectives;
        this.version = version;
    }
    
    public UUID getId() {
        return id;
    }
    
    public Vector getLocation() {
	return location;
    }

    public Country getCountry() {
        return country;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    public RankMessage getRank() {
        return rank;
    }

    public UUID getCommandingOfficer() {
        return commandingOfficer;
    }

    public void setCommandingOfficer(UUID commandingOfficer){
        this.commandingOfficer = commandingOfficer;
    }
    
    public CharacterType getType() {
        return type;
    }

    public Set<UUID> getUnitsUnderCommand() {
        return unitsUnderCommand;
    }

    public boolean isDead() {
        return dead;
    }

    public boolean isCheckedOutBy(UUID clientID) {
        return clientID.equals(this.checkoutClient);
    }

    public boolean isAvatar() {
        return type == CharacterType.AVATAR;
    }

    public boolean shouldBeControledRemotely(UUID avatarId) {
        return this.checkoutClient!=null && !this.checkoutClient.equals(avatarId) && !id.equals(avatarId);
    }

    public Vector getOrientation() {
        return orientation;
    }
    
    public Set<UUID> getKills(){
        return kills;
    }
    
    public Map<String, String> getObjectives(){
        return objectives;
    }

    public Shoot isShooting() {
        for(Action a: actions){
            if(a instanceof Shoot){
                return (Shoot) a;
            }
        }
        return null;
    }
    
    
    public boolean isCrouching(){
        for(Action a: actions){
            if(a instanceof Crouch){
                return true;
            }
        }
        return false;
    }

    public boolean hasSetupWeapon() {
        for(Action a: actions){
            if(a instanceof SetupWeapon){
                return true;
            }
        }
        return false;
    }
    
//    public ManualControl controlingBoardedVehicle() {
//        for(Action a: actions){
//            if(a instanceof ManualControl){
//                return (ManualControl) a;
//            }
//        }
//        return null;
//    }
    
    public Set<UUID> getPassengers(){
        return passengers;
    }
    
    public void setPassengers(Set<UUID> passengers){
        this.passengers = passengers;
    }
    
    public SquadType getSquadType(){
        return squadType;
    }

    public UUID getBoardedVehicle() {
        return boardedVehicle;
    }
    
    public void setBoardedVehicle(UUID boardedVehicle){
        this.boardedVehicle = boardedVehicle;
    }

    public void setEngineDamage(boolean b) {
        engineDamaged = b;
    }

    public boolean hasEngineDamage() {
        return engineDamaged;
    }
    
    public void setCountry(Country country){
        this.country = country;
    }

    public void addAction(Action action) {
        actions.add(action);
    }
    
    public long getVersion(){
        return version;
    }
    
}
