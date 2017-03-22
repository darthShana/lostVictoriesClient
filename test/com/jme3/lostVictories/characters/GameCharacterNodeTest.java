/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dharshanar
 */
public class GameCharacterNodeTest {
    
    public GameCharacterNodeTest() {
    }

    @Test
    public void testPlayerDirectionWorks() {
        
        Vector3f localTranslation = new Vector3f(-57.97913f, 96.61415f, -221.02112f);
        Vector3f realTarget = new Vector3f(-66.78554f, 96.32174f, -254.6674f);
        
        Vector3f playerDirection = new Vector3f(-0.21880314f, 0.0077911178f, -0.9757379f);
        Vector3f shootingLocation = new Vector3f(-57.97844f, 99.63159f, -223.30313f);

        Vector3f requiredDirecting = realTarget.subtract(localTranslation);
        
        Vector2f v1 = new Vector2f(requiredDirecting.x, requiredDirecting.z);
        Vector2f v2 = new Vector2f(playerDirection.x, playerDirection.z);
        
        assertTrue(v1.smallestAngleBetween(v2)< FastMath.QUARTER_PI/8);
        
        Vector3f calculatedTarget = new Vector3f(-59.073147f, 97.65311f, -225.89981f);
        Vector3f shootingDirection = calculatedTarget.subtract(shootingLocation);
        
        Vector2f v3 = new Vector2f(shootingDirection.x, shootingDirection.z);
        assertTrue(v1.smallestAngleBetween(v3)< (FastMath.QUARTER_PI/8));
        
        assertTrue(Weapon.mg42().isWithinFieldOfVision(playerDirection, shootingDirection));
        
        
        
    }
}