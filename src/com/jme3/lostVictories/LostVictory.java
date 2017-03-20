package com.jme3.lostVictories;
 
import com.jme3.ai.navmesh.CustomNavMeshBuilder;
import com.jme3.lostVictories.structures.GameHouseNode;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.lostVictories.effects.ParticleEmitterFactory;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.app.DebugKeysAppState;
//import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.minimap.MinimapNode;
import com.jme3.lostVictories.network.NetworkClient;
import com.jme3.lostVictories.network.ResponseFromServerMessageHandler;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.structures.GameObjectNode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import com.jme3.terrain.geomipmap.TerrainQuad;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonNode;



 
public class LostVictory extends SimpleApplication implements ActionListener {
    
    public AvatarCharacterNode avatar;

    private BulletAppState bulletAppState;

    private TerrainQuad terrain;
    ScheduledExecutorService worldMapUpdatorService;
    ScheduledExecutorService worldRunnerService;
    private final UUID avatarUUID;

    public static void main(String[] args) throws IOException, DecoderException {
        Object[] options = { "OK", "CANCEL" };
            
        String playerID, serverIP, gameVersion;
        int port = 5055;
//        args = new String[]{"lostvic://lostVictoriesLauncher/game=eyJpZCI6Im5vcndlZ2lhbl9jYW1wYWlnbiIsIm5hbWUiOiJOb3J3ZWdpYW4gQ2FtcGFpZ24iLCJob3N0IjoiY29ubmVjdC5sb3N0dmljdG9yaWVzLmNvbSIsInBvcnQiOiI1MDU1Iiwic3RhcnREYXRlIjoxNDg5MDgwOTg5MjAxLCJqb2luZWQiOnRydWUsImF2YXRhcklEIjoiNDUyZDNlYjItNzc3ZS00MDJjLTkyODctZDM4ZWEyNzI0NWZlIiwiZ2FtZVZlcnNpb24iOiJwcmVfYWxwaGEiLCJnYW1lU3RhdHVzIjoiaW5Qcm9ncmVzcyIsInZpY3RvciI6bnVsbCwiZW5kRGF0ZSI6bnVsbCwiY291bnRyeSI6IkFNRVJJQ0FOIn0="};
        if(args.length>0){    
//            JOptionPane.showOptionDialog(null, args[0], "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);         
//            if(args.length>1){
//                JOptionPane.showOptionDialog(null, args[1], "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);         
//            }
            
            String s = args.length>1?args[1].substring("lostVictoriesLauncher/game=".length()):args[0].substring("lostvic://lostVictoriesLauncher/game=".length());
            
            System.out.println("s:"+s);
            
            Base64 decoder = new Base64();
            byte[] decodedBytes = decoder.decode(s);
            String ss = new String(decodedBytes);
            System.out.println("ss:"+ss);
            
            JsonNode gameJson = MAPPER.readTree(ss);
            playerID = gameJson.get("avatarID").asText();
            serverIP = gameJson.get("host").asText();
            port = Integer.parseInt(gameJson.get("port").asText());
            gameVersion = gameJson.get("gameVersion").asText();
        }else{
            Map<String, String> env = System.getenv();
            playerID = env.get("player_id");
            serverIP = env.get("server_ip");
            if(playerID==null){
                playerID = "2fbe421f-f701-49c9-a0d4-abb0fa904204"; //german
                //playerID = "d993932f-a185-4a6f-8d86-4ef6e2c5ff95"; //american 1
                //playerID = "844fd93d-e65a-438a-82c5-dab9ad58e854"; //american 2
            }
            if(serverIP == null){
                serverIP = "localhost";
                //serverIP = "connect.lostvictories.com";
            }
            gameVersion = "pre_alpha";
        
        }
        
        LostVictory app = new LostVictory(UUID.fromString(playerID), serverIP, port, gameVersion);
        app.start();
    }

    private NavMesh navMesh;
    private WorldMap worldMap;
    private WorldRunner worldRunner;
    private boolean gameOver;
    private CharacterLoader characterLoader;
    //private IngameText ingameText = new IngameText();
    private Node sceneGraph;
    public RealTimeStrategyAppState chaseCameraAppState;
    private FirstPersonShooterAppState firstPersonShooterAppState;
    private HeadsUpDisplayAppState headsUpDisplayAppState;
    NetworkClientAppState networkClientAppState;
    private final String ipAddress;
    private final int port;
    private final String gameVersion;
 
    public LostVictory(UUID avatorID, String ipAddress, int port, String gameVersion) {
        super( new StatsAppState(), new DebugKeysAppState() );
        this.avatarUUID = avatorID;
        System.out.println("starting client:"+avatorID);
        this.ipAddress = ipAddress;
        this.port = port;
        this.gameVersion = gameVersion;      
    }
   
    @Override
    public void simpleInitApp() {
        if(!"pre_alpha".equals(gameVersion)){
            throw new RuntimeException("Sorry your game is out of date please install version:"+gameVersion);
        }
        
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletAppState);
//        bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        bulletAppState.getPhysicsSpace().addCollisionGroupListener(new VehicleCOllisionListener(), PhysicsCollisionObject.COLLISION_GROUP_01);
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        
        Set<GameStructureNode> structures = new HashSet<GameStructureNode>();
        Set<GameObjectNode> objects = new HashSet<GameObjectNode>();
        
        sceneGraph = TerrainLoader.instance().loadTerrain(assetManager, bulletAppState, cam, objects, "Scenes/testScene4.j3o");
        terrain = (TerrainQuad) sceneGraph.getChild("terrain-testScene4");
        
        DirectionalLight sun1 = new DirectionalLight();
        sun1.setColor(ColorRGBA.White);
        sun1.setDirection(new Vector3f(-.6f,-.6f,-.6f).normalizeLocal());
        rootNode.addLight(sun1);
        
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(.5f));
        rootNode.addLight(al);
        
        navMesh = CustomNavMeshBuilder.buildMesh((Geometry)sceneGraph.getChild("NavMesh"));
        StructureLoader structureLoader = StructureLoader.instance(rootNode, assetManager, bulletAppState, new NavMeshPathfinder(navMesh));
       
        MinimapNode minimapNode = new MinimapNode("minimap", this);
        chaseCameraAppState = new RealTimeStrategyAppState(minimapNode);
        firstPersonShooterAppState = new FirstPersonShooterAppState(minimapNode);
        headsUpDisplayAppState = new HeadsUpDisplayAppState(this, chaseCameraAppState);
        
        ParticleEmitterFactory pf = ParticleEmitterFactory.instance(assetManager);
        ParticleManager particleManager = new ParticleManager(sceneGraph, assetManager, renderManager);
        characterLoader = CharacterLoader.instance(sceneGraph, assetManager, bulletAppState, navMesh, pf, headsUpDisplayAppState, particleManager, this);
        ResponseFromServerMessageHandler serverSync = new ResponseFromServerMessageHandler(this, characterLoader, avatarUUID, particleManager, headsUpDisplayAppState);
        networkClientAppState = NetworkClientAppState.init(this, new NetworkClient(ipAddress, port, avatarUUID, serverSync), serverSync);
        
        Set<GameCharacterNode> characters = new HashSet<GameCharacterNode>();
        try {
            CheckoutScreenResponse checkout = networkClientAppState.checkoutSceenSynchronous(avatarUUID);
            structureLoader.loadStuctures(structures, sceneGraph, checkout, terrain, this);
            avatar = characterLoader.loadCharacters(characters, structures, objects, checkout, avatarUUID);
            sceneGraph.addControl(new SimpleGrassControl(assetManager, bulletAppState, (Node) sceneGraph, checkout.getAllTrees(), "Resources/Textures/Grass/grass.png"));
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        
        initKeys();
        setPauseOnLostFocus(false);
        rootNode.attachChild(sceneGraph);        
        worldMapUpdatorService = Executors.newScheduledThreadPool(1);
        
        worldMap = WorldMap.get();
        worldMapUpdatorService.scheduleAtFixedRate(worldMap, 0, 3, TimeUnit.SECONDS);

        worldRunnerService = Executors.newScheduledThreadPool(1);
        worldRunner = WorldRunner.instance(worldMap);
        worldRunnerService.scheduleAtFixedRate(worldRunner, 0, 2, TimeUnit.SECONDS);
        
        getStateManager().attach(chaseCameraAppState);
        getStateManager().attach(headsUpDisplayAppState);
        getStateManager().attach(networkClientAppState);
//        getStateManager().attach(new DetailedProfilerState());
//        setDisplayFps(false);
//        setDisplayStatView(false);
    }
    
    

    @Override
    public void simpleUpdate(float tpf) {     
        if(gameOver){
            return;
        }

        for(GameCharacterNode c: worldMap.getAllCharacters()){
          if(!c.isDead()){
              c.simpleUpate(tpf, worldMap, rootNode);
          }else{
              if(c == avatar && getStateManager().hasState(firstPersonShooterAppState)){
                  getStateManager().detach(firstPersonShooterAppState);
                  getStateManager().attach(chaseCameraAppState);
              }
          }
        }
              
        for(GameHouseNode s: worldMap.getAllHouses()){
            s.simpleUpate(tpf, worldMap);
        }

        if(worldRunner.hasCapturedAllStructures(avatar.getCountry()) || worldRunner.hasTheOnlyVictoryPoints()){
            headsUpDisplayAppState.printVictoryText();
            worldRunnerService.shutdownNow();
            worldMapUpdatorService.shutdownNow();
            gameOver =true;
        }
        if(worldRunner.hasLostAllStructures(avatar.getCountry()) || worldRunner.hasNoVictoryPoints()){
            headsUpDisplayAppState.printDefeatedText();
            worldRunnerService.shutdownNow();
            worldMapUpdatorService.shutdownNow();
            gameOver =true;
            System.out.println("1:"+worldRunner.hasLostAllStructures(avatar.getCountry()));
            System.out.println("2:"+worldRunner.hasNoVictoryPoints());
        }

    }

    @Override
    public void destroy() {
        worldMapUpdatorService.shutdownNow();
        worldRunnerService.shutdownNow();
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
    }

    /** Custom Keybinding: Map named actions to inputs. */
    private void initKeys() {
        inputManager.addMapping("switchMode", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addListener(this, "switchMode");  
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("switchMode") && isPressed) {
            if(getStateManager().hasState(chaseCameraAppState)){
                getStateManager().detach(chaseCameraAppState);
                getStateManager().attach(firstPersonShooterAppState);
            }else{
                getStateManager().detach(firstPersonShooterAppState);
                getStateManager().attach(chaseCameraAppState);
            }
        }
    }

    AvatarCharacterNode getAvatar() {
        return avatar;
    }
    
    public void setAvatar(AvatarCharacterNode newAvatar){
        this.avatar = newAvatar;
    }

    AppSettings getSettings(){
        return settings;
    }
    
    Node getTerrain(){
        return terrain;
    }
    
        
}