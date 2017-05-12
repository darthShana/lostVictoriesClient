/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.physicsControl;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.blenderModels.Panzer4BlenderModel;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class BetterTankControl extends BetterVehicleControl {

    private final Panzer4BlenderModel blenderModel;
    private final BulletAppState bulletAppState;
    
    public BetterTankControl(int mass, GameVehicleNode vehicleNode, Panzer4BlenderModel blenderModel, AssetManager assetManager, BulletAppState bulletAppState) {
        super(mass, vehicleNode, blenderModel, assetManager);
        this.blenderModel = blenderModel;
        this.bulletAppState = bulletAppState;
    }
    
    public void addTurret(Node tank, Node turret){
        RigidBodyControl rigidBodyControl = new RigidBodyControl(blenderModel.getTurretShape(), 100);
        Node turretNode = new Node();
        turretNode.attachChild(turret);
        tank.attachChild(turretNode);

        HingeJoint turretJoint = new HingeJoint(this, rigidBodyControl, Vector3f.ZERO, Vector3f.ZERO, Vector3f.UNIT_Y, Vector3f.UNIT_Y);
        turretJoint.setLimit(0, 0);
        turret.setLocalTranslation(0, -.5f, 0);
        turretNode.addControl(rigidBodyControl);
        
        bulletAppState.getPhysicsSpace().add(rigidBodyControl);
        bulletAppState.getPhysicsSpace().add(turretJoint);
    }
    
}
