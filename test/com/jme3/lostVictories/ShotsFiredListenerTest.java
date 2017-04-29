/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.math.Vector3f;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dharshanar
 */
public class ShotsFiredListenerTest {
    
 


    /**
     * Test of getShootsFiredInRange method, of class ShotsFiredListener.
     */
    @Test
    public void testGetShootsFiredInRange() {
        ShotsFiredListener instance = ShotsFiredListener.instance();
        
        instance.register(new Vector3f(50, 10, 50));
        instance.register(new Vector3f(100, 10, 100));
        instance.register(new Vector3f(4, 2, 4));
        final Set<Vector3f> shootsFiredInRange = instance.getShootsFiredInRange(Vector3f.ZERO, 100, 10);
        
        assertEquals(1, shootsFiredInRange.size());
        assertEquals(new Vector3f(50, 0, 50), shootsFiredInRange.iterator().next());
    }
}