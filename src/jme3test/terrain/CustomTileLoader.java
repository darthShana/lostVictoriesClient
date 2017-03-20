/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jme3test.terrain;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainGridTileLoader;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.grid.AssetTileLoader;
import java.io.IOException;


/**
 *
 * @author dharshanar
 */
class CustomTileLoader implements TerrainGridTileLoader {

    

    private AssetManager manager;
    private String assetPath;
    private String name;
    private int size;
    private int patchSize;
    private int quadSize;


    public CustomTileLoader(AssetManager manager, String name, String assetPath) {
        this.manager = manager;
        this.name = name;
        this.assetPath = assetPath;
    }

    public TerrainQuad getTerrainQuadAt(Vector3f location) {
        final int roundX = Math.round(location.x);
        final int roundZ = Math.round(location.z);
        String modelName = assetPath + "/" + name + "-" + roundX + "-" + roundZ + ".j3o";
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Load terrain grid tile: {0}", modelName);
        TerrainQuad quad = null;
        try {
            Node model = (Node) manager.loadModel(modelName);
            quad = (TerrainQuad)model.getChild("terrain-world_grid-" + roundX + "-" + roundZ);
            quad.setMaterial(null);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        if (quad == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Could not load terrain grid tile: {0}", modelName);
            quad = createNewQuad(location);
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Loaded terrain grid tile: {0}", modelName);
        }
        return quad;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public String getName() {
        return name;
    }

    public void setPatchSize(int patchSize) {
        this.patchSize = patchSize;
    }

    public void setQuadSize(int quadSize) {
        this.quadSize = quadSize;
    }

    private TerrainQuad createNewQuad(Vector3f location) {
        TerrainQuad q = new TerrainQuad("Quad" + location, patchSize, quadSize, null);
        return q;
    }

    public void write(JmeExporter ex) throws IOException {
        OutputCapsule c = ex.getCapsule(this);
        c.write(assetPath, "assetPath", null);
        c.write(name, "name", null);
    }

    public void read(JmeImporter im) throws IOException {
        InputCapsule c = im.getCapsule(this);
        manager = im.getAssetManager();
        assetPath = c.readString("assetPath", null);
        name = c.readString("name", null);
    }
    
    
}
