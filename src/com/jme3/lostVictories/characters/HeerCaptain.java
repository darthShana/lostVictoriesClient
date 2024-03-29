/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class HeerCaptain extends Soldier implements CommandingOfficer{

    Set<Commandable> charactersToCommand = new HashSet<Commandable>();
    
    public HeerCaptain(UUID id, Node player, Country country, CommandingOfficer commandingOfficer, Vector3f position, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter characterParticleEmitters, ParticleManager particleManager, NavigationProvider navMeshPathfinder, AssetManager assetManager, BlenderModel m, BehaviorControler behaviorControler, Camera camera) {
        super(id, player, country, commandingOfficer, position, rotation, rootNode, bulletAppState, characterParticleEmitters, particleManager, navMeshPathfinder, assetManager, m, behaviorControler, camera);
    }

    public Set<Commandable> getCharactersUnderCommand() {
        return charactersToCommand;
    }

    public void addCharactersUnderCommand(Set<Commandable> cc) {
        charactersToCommand.addAll(cc);
    }
    
    public void addCharactersUnderCommand(Commandable c) {
        charactersToCommand.add(c);
    }

    public void removeCharacterUnderCommand(Commandable aThis) {
        charactersToCommand.remove(aThis);
    }

    public void removeAllUnits() {
        charactersToCommand.clear();
    }
            
    @Override
    public Rank getRank() {
        return Rank.COLONEL;
    }

    public boolean isTeam(Weapon... weapons) {
        return false;
    }
    
}
