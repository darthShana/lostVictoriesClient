/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.minimap.MinimapNode;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author dharshanar
 */
class CustomLODControl extends AbstractControl {
    private final Camera camera;
    private final Spatial stickFigure;
    private boolean isFlat = false;

    public CustomLODControl(Camera camera, Spatial loadModel) {
        this.camera = camera;
        this.stickFigure = loadModel;
    }

    @Override
    protected void controlUpdate(float f) {
        GameCharacterNode c = (GameCharacterNode) spatial;
        if(!c.isAbstracted() && c.getLocalTranslation().distance(camera.getLocation())>150){
            c.makeAbstract(stickFigure);
        }else if(c.isAbstracted() && c.getLocalTranslation().distance(camera.getLocation())<150){
            c.makeUnAbstracted(stickFigure);
        }
        if(!isFlat && c.isDead()){
            stickFigure.setLocalRotation(MinimapNode.x_rot);
            isFlat = true;
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}
