/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author dharshanar
 */
public class CharacterActionNGTest {
    
    public CharacterActionNGTest() {
    }

    @Test
    public void testManunalMoveStopsPathTravesal(){
        CharacterAction ca = new CharacterAction();
        final ArrayList<Vector3f> arrayList = new ArrayList<Vector3f>();
        arrayList.add(Vector3f.NAN);
        ca.travelPath(arrayList);
        
        assertTrue(ca.isTraversingPath());
        
        ca.goForward();
        assertFalse(ca.isTraversingPath());
    }
}