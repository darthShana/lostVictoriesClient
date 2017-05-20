/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.characters.physicsControl.BetterVehicleControl;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.characters.blenderModels.Panzer4BlenderModel;
import com.jme3.lostVictories.characters.blenderModels.VehicleBlenderModel;
import com.jme3.lostVictories.characters.physicsControl.BetterTankControl;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class MediumTankNode extends GameVehicleNode{

    private final Node turretModel;


    public MediumTankNode(UUID id, Node chassisModel, Node turretModel, Map<Country, Node> operator, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, VehicleBlenderModel m, BehaviorControler behaviorControler, Camera camera) {
        super(id, chassisModel, operator, country, commandingOfficer, worldCoodinates, rotation, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, behaviorControler, camera);
        this.turretModel = turretModel;
    }

    @Override
    public void attachToRootNode() {
        super.attachToRootNode();
        ((BetterTankControl)playerControl).addTurret(this, turretModel);

    }
    
    @Override
    public Vector3f getPositionToTarget(GameCharacterNode targetedBy) {
        return getLocalTranslation().add(new Vector3f(0f, .5f, 0f));
    }

    @Override
    public float getTurnSpeed() {
        return 10;
    }

    @Override
    public float getDriveSpeed() {
        return 25;
    }

    @Override
    public float getEnginePower() {
        return 4000;
    }

    @Override
    public float getMaxStearingAngle() {
        return .5f;
    }

    @Override
    protected BetterVehicleControl createCharacterControl(AssetManager manager) {
        return new BetterTankControl(2000, this, (Panzer4BlenderModel)model, manager, bulletAppState);
    }

    public void trretLeft() {
        ((BetterTankControl)playerControl).turretLeft();
    }

    public void stopTurret() {
        ((BetterTankControl)playerControl).turretStop();
    }

    public void trretRight() {
        ((BetterTankControl)playerControl).turretRight();
    }

    @Override
    public Vector3f getAimingDirection() {
        return ((BetterTankControl)playerControl).getTurretDirection();
    }
    
    
    
}
