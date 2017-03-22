/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author dharshanar
 */
public class HeadsUpDisplayAppStateTest {
    

    @Test
    public void testObserveGameEvents() {
        List<String> ms = HeadsUpDisplayAppState.observeGameEvents(1, 1, 1, 1, 2, 1, 2, 2);
        assertEquals(1, ms.size());
        assertEquals("we have captured a house!", ms.get(0));
        
        ms = HeadsUpDisplayAppState.observeGameEvents(1, 1, 1, 1, 1, 2, 2, 2);
        assertEquals(1, ms.size());
        assertEquals("we have lost a house!", ms.get(0));
    }
}