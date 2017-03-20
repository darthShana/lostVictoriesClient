/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.terrain;

import com.jme3.asset.AssetManager;
import com.jme3.terrain.geomipmap.TerrainQuad;

/**
 *
 * @author dharshanar
 */
class CustomTerrainQuad extends TerrainQuad {
    private final AssetManager assetManager;

    public CustomTerrainQuad(AssetManager assetManager) {
        this.assetManager = assetManager;
    }
    
}
