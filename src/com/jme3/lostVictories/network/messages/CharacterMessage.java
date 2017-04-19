/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.network.messages.actions.Crouch;
import com.jme3.lostVictories.network.messages.actions.ManualControl;
import com.jme3.lostVictories.network.messages.actions.SetupWeapon;
import com.jme3.lostVictories.network.messages.actions.Shoot;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
    Map<String, String> objectives ;
    Set<String> completedObjectives;
    boolean dead;
    boolean engineDamaged;
    Long timeOfDeath;
    long version;
    int killCount;
    SquadType squadType;
    long creationTime;
    boolean busy;
    boolean attacking;

    
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
        this.creationTime = System.currentTimeMillis();
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
    
    public void setType(CharacterType type){
        this.type = type;
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
    
    public int getKillCount(){
        return killCount;
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
    
    public UUID getCheckoutClient(){
        return checkoutClient;
    }
    
    public long getCreationTime(){
        return creationTime;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) { return false; }
      if (obj == this) { return true; }
      if (obj.getClass() != getClass()) {
        return false;
      }
      CharacterMessage rhs = (CharacterMessage) obj;
        final EqualsBuilder builder = new EqualsBuilder()
                .append(location, rhs.location)
                .append(id, rhs.id)
                .append(orientation, rhs.orientation)
                .append(actions, rhs.actions)
                .append(completedObjectives, rhs.completedObjectives)
                .append(checkoutClient, rhs.checkoutClient);
        if(objectives!=null){
            builder.append(objectives.keySet(), rhs.objectives.keySet());
        }
      return builder.isEquals();
    }
    
    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(17, 37)
                .append(location)
                .append(orientation)
                .append(actions)
                .append(completedObjectives)
                .append(checkoutClient)
                .append(version);
        if(objectives!=null){
            builder.append(objectives.keySet());
        }

        return builder
          .toHashCode();
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    
    public boolean isBusy(){
        return busy;
    }
    
    public boolean isAttacking(){
        return attacking;
    }

    @JsonIgnore
    public boolean hasBeenSentRecently(long version) {
        if(type==CharacterType.AVATAR){
            if(isOlderVersion(version)){
                return false;
            }
        }
        return System.currentTimeMillis()-creationTime<2000;
        
    }

    public boolean isOlderVersion(long version) {
        return this.version<version;
    }
    
}
