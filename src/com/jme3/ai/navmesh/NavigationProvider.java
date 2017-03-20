/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.ai.navmesh;

import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

/**
 *
 * @author dharshanar
 */
public class NavigationProvider {

    private final NavMeshPathfinder navMeshPathFinder;

    public NavigationProvider(NavMeshPathfinder customNavMeshPathfinder) {
        this.navMeshPathFinder = customNavMeshPathfinder;
    }

    public void warpInside(Vector3f location) {
        navMeshPathFinder.warpInside(location);
    }

    public Optional<List<Vector3f>> computePath(float entityRadius, Vector3f start, Vector3f destination) {
        synchronized(navMeshPathFinder){
            navMeshPathFinder.clearPath();
            navMeshPathFinder.setEntityRadius(entityRadius);
            navMeshPathFinder.setPosition(start);
            Vector3f dest = new Vector3f(destination);
            navMeshPathFinder.warpInside(dest);
            final DebugInfo debugInfo = new DebugInfo();

            if(navMeshPathFinder.computePath(dest, debugInfo)){
                List<Vector3f> path = new ArrayList<>();
                for(Path.Waypoint w: navMeshPathFinder.getPath().getWaypoints()){
                    path.add(new Vector3f(w.getPosition()));
                }
                return Optional.of(path);
            }
            return Optional.empty();
        }
    }
    
}
