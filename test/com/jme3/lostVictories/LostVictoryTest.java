/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.math.Vector3f;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author dharshanar
 */
public class LostVictoryTest {
    

    
    @Test
    public void testGetShootsFiredInRange() throws DecoderException, UnsupportedEncodingException {
        String ss = "lostVictoriesLauncher/game=7b226964223a2233346334343561652d333538662d343762352d396134622d366266656361393366646430222c226e616d65223a22746573745f6c6f73745f766963746f7269657331222c22686f7374223a223132372e302e302e31222c22706f7274223a2235303535222c22737461727444617465223a313435323932303738363331322c226a6f696e6564223a747275652c226176617461724944223a2263323762663761652d343332622d343063652d393033342d366663623331646339623930227d";
        String hexString = ss.substring("lostVictoriesLauncher/game=".length());
                
        byte[] bytes = Hex.decodeHex(hexString.toCharArray());
        System.out.println(new String(bytes, "UTF-8"));
    }

    
}