/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.minimap;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.LostVictory;
import com.jme3.lostVictories.StructureStatus;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.testng.annotations.Test;

/**
 *
 * @author dharshanar
 */
public class MinimapNodeTest {
    
    @Test
    public void testGetInitialStatus() {
        final GameHouseNode house = mock(GameHouseNode.class);
        when(house.getStatus()).thenReturn(StructureStatus.NEUTRAL);
        ColorRGBA mc = MinimapNode.getStatusColour(house, Country.GERMAN);
        assertEquals(MinimapNode.NEUTRAL, mc);
        
        when(house.getStatus()).thenReturn(StructureStatus.CAPTURING);
        when(house.getContestingOwner()).thenReturn(Country.GERMAN);
        mc = MinimapNode.getStatusColour(house, Country.GERMAN);
        assertEquals(MinimapNode.LIGHT_GREEN, mc);
        when(house.getContestingOwner()).thenReturn(Country.AMERICAN);
        mc = MinimapNode.getStatusColour(house, Country.GERMAN);
        assertEquals(MinimapNode.LIGHT_YELLOW, mc);
        
        when(house.getOwner()).thenReturn(Country.GERMAN);
        when(house.getStatus()).thenReturn(StructureStatus.CAPTURED);
        mc = MinimapNode.getStatusColour(house, Country.GERMAN);
        assertEquals(MinimapNode.GREEN, mc);
        
        when(house.getOwner()).thenReturn(Country.AMERICAN);
        mc = MinimapNode.getStatusColour(house, Country.GERMAN);
        assertEquals(MinimapNode.ENEMY, mc);
        
        when(house.getStatus()).thenReturn(StructureStatus.DECAPTURING);
        when(house.getOwner()).thenReturn(Country.GERMAN);
        mc = MinimapNode.getStatusColour(house, Country.GERMAN);
        assertEquals(MinimapNode.LIGHT_YELLOW, mc);
        when(house.getOwner()).thenReturn(Country.AMERICAN);
        mc = MinimapNode.getStatusColour(house, Country.GERMAN);
        assertEquals(MinimapNode.LIGHT_GREEN, mc);
                
    }
    
    @Test
    public void testBlinkingHouseStatus(){
        final GameHouseNode house = mock(GameHouseNode.class);
        when(house.getStatus()).thenReturn(StructureStatus.CAPTURING);
        when(house.getContestingOwner()).thenReturn(Country.GERMAN);
        ColorRGBA mc = MinimapNode.getStatusColour(house, Country.GERMAN, new Geometry());
        assertEquals(MinimapNode.LIGHT_GREEN, mc);
        
        final Geometry geometry = new Geometry();
        geometry.setUserData("color", MinimapNode.LIGHT_GREEN);
        mc = MinimapNode.getStatusColour(house, Country.GERMAN, geometry);
        assertEquals(ColorRGBA.Black, mc);
        
        geometry.setUserData("color", ColorRGBA.Black);
        mc = MinimapNode.getStatusColour(house, Country.GERMAN, geometry);
        assertEquals(MinimapNode.LIGHT_GREEN, mc);
    }
}