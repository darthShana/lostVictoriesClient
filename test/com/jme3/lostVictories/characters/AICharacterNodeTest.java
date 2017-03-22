/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.blenderModels.HalfTrackBlenderModel;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.effect.ParticleEmitter;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.HeadsUpDisplayAppState;
import com.jme3.lostVictories.characters.blenderModels.SoldierBlenderModel;
import com.jme3.lostVictories.characters.weapons.Rifle;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.objectives.EnemyActivityReport;
import com.jme3.material.MatParam;
import com.jme3.material.MaterialDef;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.testng.annotations.Test;

/**
 *
 * @author dharshanar
 */
public class AICharacterNodeTest {


    @Test
    public void testEnemyActivityReport() {
        Set<Vector3f> humans = new HashSet<Vector3f>();
        humans.add(new Vector3f(100, 0, 100));
        Set<Vector3f> vehicles = new HashSet<Vector3f>();
        vehicles.add(new Vector3f(100, 0, 200));
        
        Private s = createPrivate(null);
        
        s.reportEnemyActivity(humans, vehicles);
        
        EnemyActivityReport e = s.getEnemyActivity();
        assertEquals(humans, e.getCurrentHumanTargets());
        assertEquals(vehicles, e.getCurrentVehicleTargets());
        
        s.clearEnemyActivity();
        assertTrue(s.getEnemyActivity().getCurrentHumanTargets().isEmpty());
        assertTrue(s.getEnemyActivity().getCurrentVehicleTargets().isEmpty());

    }
    
    @Test 
    public void testEnemyActoivityFromUnit(){
        Set<Vector3f> humans = new HashSet<Vector3f>();
        humans.add(new Vector3f(100, 0, 100));
        Set<Vector3f> vehicles = new HashSet<Vector3f>();
        vehicles.add(new Vector3f(100, 0, 200));
        
        Set<Vector3f> humans2 = new HashSet<Vector3f>();
        humans2.add(new Vector3f(100, 0, 100));
        Set<Vector3f> vehicles2 = new HashSet<Vector3f>();
        vehicles2.add(new Vector3f(100, 0, 200));
        
        CadetCorporal s = createCadetCorporal(null);
        Private p = createPrivate(s);
        
        p.reportEnemyActivity(humans, vehicles);
        EnemyActivityReport e = s.getEnemyActivity();
        assertEquals(humans, e.getCurrentHumanTargets());
        assertEquals(vehicles, e.getCurrentVehicleTargets());
        
        s.reportEnemyActivity(humans2, vehicles2);
        e = s.getEnemyActivity();
        humans.addAll(humans2);
        vehicles.addAll(vehicles2);
        assertEquals(humans, e.getCurrentHumanTargets());
        assertEquals(vehicles, e.getCurrentVehicleTargets());
    }

    public static Private createPrivate(CommandingOfficer co) {
        return createPrivate(co, Weapon.rifle());
    }

    public static Private createPrivate(CommandingOfficer co, Weapon weapon) {
        final AssetManager mock = mock(AssetManager.class);
        final MaterialDef mock1 = mock(MaterialDef.class);
        when(mock1.getMaterialParam(anyString())).thenReturn(mock(MatParam.class));
        when(mock.loadAsset((AssetKey) any())).thenReturn(mock1);
        final CharcterParticleEmitter particleEmiter = mock(CharcterParticleEmitter.class);
        when(particleEmiter.getFlashEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getSmokeTrailEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBloodEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBulletFragments()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBlastFragments()).thenReturn(mock(ParticleEmitter.class));
        final BulletAppState mock2 = mock(BulletAppState.class);
        when(mock2.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        Private s = new Private(UUID.randomUUID(), new Node(), Country.GERMAN, co, Vector3f.ZERO, Vector3f.ZERO, new Node(), mock2, particleEmiter, mock(ParticleManager.class), mock(NavigationProvider.class), mock, new SoldierBlenderModel("", 2, weapon), new LocalAIBehaviourControler(), mock(Camera.class));
        if(co!=null){
            co.addCharactersUnderCommand(s);
        }
        return s;
    }
    
    public static CadetCorporal createCadetCorporal(CommandingOfficer co, Weapon weapon) {
        final AssetManager mock = mock(AssetManager.class);
        final MaterialDef mock1 = mock(MaterialDef.class);
        when(mock1.getMaterialParam(anyString())).thenReturn(mock(MatParam.class));
        when(mock.loadAsset((AssetKey) any())).thenReturn(mock1);
        final CharcterParticleEmitter particleEmiter = mock(CharcterParticleEmitter.class);
        when(particleEmiter.getFlashEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getSmokeTrailEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBloodEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBulletFragments()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBlastFragments()).thenReturn(mock(ParticleEmitter.class));
        final BulletAppState mock2 = mock(BulletAppState.class);
        when(mock2.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        CadetCorporal s = new CadetCorporal(UUID.randomUUID(), new Node(), Country.GERMAN, co, Vector3f.ZERO, Vector3f.ZERO, new Node(), mock2, particleEmiter, mock(ParticleManager.class), mock(NavigationProvider.class), mock, new SoldierBlenderModel("", 2, weapon), new LocalAIBehaviourControler(), mock(Camera.class));
        if(co!=null){
            co.addCharactersUnderCommand(s);
        }
        return s;
    }
    

    public static AvatarCharacterNode createAvatar(UUID id, CommandingOfficer co, Weapon weapon) {
        final AssetManager mock = mock(AssetManager.class);
        final MaterialDef mock1 = mock(MaterialDef.class);
        when(mock1.getMaterialParam(anyString())).thenReturn(mock(MatParam.class));
        when(mock.loadAsset((AssetKey) any())).thenReturn(mock1);
        final CharcterParticleEmitter particleEmiter = mock(CharcterParticleEmitter.class);
        when(particleEmiter.getFlashEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getSmokeTrailEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBloodEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBulletFragments()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBlastFragments()).thenReturn(mock(ParticleEmitter.class));
        final BulletAppState mock2 = mock(BulletAppState.class);
        when(mock2.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        final Node node = mock(Node.class);
        final BlenderModel mock3 = mock(BlenderModel.class);
        when(mock3.canShootMultipleTargets()).thenReturn(weapon.canShootMultipleTargets());
        when(mock3.isReadyToShoot((GameAnimChannel)any(), (Vector3f)any(), (Vector3f)any())).thenReturn(true);
        when(mock3.getMuzzelLocation()).thenReturn(Vector3f.ZERO);
        
        AvatarCharacterNode s = new AvatarCharacterNode(id, node, Country.GERMAN, co, Vector3f.ZERO, Vector3f.ZERO, new Node(), mock2, particleEmiter, mock(ParticleManager.class), mock(NavigationProvider.class), mock, mock3, Rank.CADET_CORPORAL, mock(HeadsUpDisplayAppState.class), mock(Camera.class));
        return s;
    }
    
    public static CadetCorporal createCadetCorporal(CommandingOfficer co) {
        final AssetManager mock = mock(AssetManager.class);
        final MaterialDef mock1 = mock(MaterialDef.class);
        when(mock1.getMaterialParam(anyString())).thenReturn(mock(MatParam.class));
        when(mock.loadAsset((AssetKey) any())).thenReturn(mock1);
        final CharcterParticleEmitter particleEmiter = mock(CharcterParticleEmitter.class);
        when(particleEmiter.getFlashEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getSmokeTrailEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBloodEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBulletFragments()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBlastFragments()).thenReturn(mock(ParticleEmitter.class));
        final BulletAppState mock2 = mock(BulletAppState.class);
        when(mock2.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        CadetCorporal s = new CadetCorporal(UUID.randomUUID(), new Node(), Country.GERMAN, co, Vector3f.ZERO, Vector3f.ZERO, new Node(), mock2, particleEmiter, mock(ParticleManager.class), mock(NavigationProvider.class), mock, new SoldierBlenderModel("", 2, Weapon.rifle()), new LocalAIBehaviourControler(), mock(Camera.class));
        return s;
    }
    
    public static Lieutenant createLieutenant(CommandingOfficer co) {
        final AssetManager mock = mock(AssetManager.class);
        final MaterialDef mock1 = mock(MaterialDef.class);
        when(mock1.getMaterialParam(anyString())).thenReturn(mock(MatParam.class));
        when(mock.loadAsset((AssetKey) any())).thenReturn(mock1);
        final CharcterParticleEmitter particleEmiter = mock(CharcterParticleEmitter.class);
        when(particleEmiter.getFlashEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getSmokeTrailEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBloodEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBulletFragments()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBlastFragments()).thenReturn(mock(ParticleEmitter.class));
        final BulletAppState mock2 = mock(BulletAppState.class);
        when(mock2.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        Lieutenant s = new Lieutenant(UUID.randomUUID(), new Node(), Country.GERMAN, co, Vector3f.ZERO, Vector3f.ZERO, new Node(), mock2, particleEmiter, mock(ParticleManager.class), mock(NavigationProvider.class), mock, new SoldierBlenderModel("", 2, Weapon.rifle()), new LocalAIBehaviourControler(), mock(Camera.class));
        return s;
    }
    
    public static GameVehicleNode createVehicle(CommandingOfficer co) {
        final AssetManager mock = mock(AssetManager.class);
        final MaterialDef mock1 = mock(MaterialDef.class);
        when(mock1.getMaterialParam(anyString())).thenReturn(mock(MatParam.class));
        when(mock.loadAsset((AssetKey) any())).thenReturn(mock1);
        final CharcterParticleEmitter particleEmiter = mock(CharcterParticleEmitter.class);
        when(particleEmiter.getFlashEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getSmokeTrailEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBloodEmitter()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBulletFragments()).thenReturn(mock(ParticleEmitter.class));
        when(particleEmiter.getBlastFragments()).thenReturn(mock(ParticleEmitter.class));
        when (particleEmiter.getSmokeEmiter()).thenReturn(mock(ParticleEmitter.class));
        final BulletAppState mock2 = mock(BulletAppState.class);
        when(mock2.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        final HashMap<Country, Node> hashMap = new HashMap<Country, Node>();
        final Node gunner = new Node();
        gunner.attachChild(new Node("operator"));
        gunner.attachChild(new Node("gunner"));
        hashMap.put(Country.GERMAN, gunner);
        hashMap.put(Country.AMERICAN, gunner);

        HalfTrackNode s = new HalfTrackNode(UUID.randomUUID(), new Node(), hashMap, Country.GERMAN, co, Vector3f.ZERO, Vector3f.ZERO, new Node(), mock2, particleEmiter, mock(ParticleManager.class), mock(NavigationProvider.class), mock, new HalfTrackBlenderModel("", 2, Weapon.rifle()), new LocalAIBehaviourControler(), mock(Camera.class));
        return s;
    }



}