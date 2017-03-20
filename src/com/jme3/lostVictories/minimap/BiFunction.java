/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.minimap;

import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;

/**
 *
 * @author dharshanar
 */
interface BiFunction<T0, T1, T2> {

    public T2 apply(T0 t0, T1 t1);
    
}
