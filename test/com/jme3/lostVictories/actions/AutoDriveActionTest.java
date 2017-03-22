/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.math.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dharshanar
 */
public class AutoDriveActionTest {
    
    @Test
    public void testIsInTriangle() {
        
        assertTrue(AutoDriveAction.isInTriangle(new Vector3f(-4, 0, -3), new Vector3f(0, 0, 0), new Vector3f(-5, 0, -4), new Vector3f(-5, 0, 4)));
        assertFalse(AutoDriveAction.isInTriangle(new Vector3f(-4, 0, -30), new Vector3f(0, 0, 0), new Vector3f(-5, 0, -4), new Vector3f(-5, 0, 4)));

    }
    
    @Test
    public void testGetReverseTrianlge(){
        Vector3f[] points = AutoDriveAction.getReverseTrianlge(new Vector3f(100, 0, 100), new Vector3f(0, 0, 1));
        assertEquals(new Vector3f(104, 0, 93), points[0]);
        assertEquals(new Vector3f(96, 0, 93), points[1]);
    }
    
    @Test
    public void testCombo1(){
        Vector3f[] points = AutoDriveAction.getReverseTrianlge(
                new Vector3f(121.012085f, 100.50934f, -410.1363f), 
                new Vector3f(0.2915073f, -3.5054E-8f, 0.9565686f));
        
        assertTrue(AutoDriveAction.isInTriangle(new Vector3f(120.823654f, 100.19363f, -413.7734f), 
                new Vector3f(121.012085f, 100.50934f, -410.1363f), points[0], points[1]));

    }
}