/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.lostVictories.headsUpDisplay.ScorllingMessagePanel;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Rank;
import com.jme3.lostVictories.headsUpDisplay.MessageBoard;
import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.ElementBuilder;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 * @author dharshanar
 */
public class HeadsUpDisplayAppState extends AbstractAppState implements ActionListener{

    Nifty nifty;
    int killCount = 0;
    String rank;
    private LostVictory app;
    Rank selectedRank;
    private boolean wasDead;
    private final RealTimeStrategyAppState chaseCameraAppState;
    private final HeadsUpDisplayController headsUpDisplayController;
    private int alliedPoints;
    private int enemyPoints;
    private int blueHouses;
    private int redHouses;
    private boolean updateHUD = false;
    private AchievementStatus currentAchivementStatus;
    private MessageBoard gameMessages;
    private ScorllingMessagePanel achivementObjectivePanel;

    HeadsUpDisplayAppState(Application app, RealTimeStrategyAppState chaseCameraAppState) {
        this.chaseCameraAppState = chaseCameraAppState;
        this.headsUpDisplayController = new HeadsUpDisplayController(app, chaseCameraAppState);
    }
    
    
    @Override
    public void initialize(AppStateManager stateManager, final Application app) {
        super.initialize(stateManager, app);
        this.app = (LostVictory) app;
        NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
        app.getAssetManager(), app.getInputManager(), app.getAudioRenderer(), app.getGuiViewPort());
        nifty = niftyDisplay.getNifty();
        app.getGuiViewPort().addProcessor(niftyDisplay);
    
        selectedRank = chaseCameraAppState.getSelectedCharacter().getRank();
 
        nifty.loadStyleFile("nifty-default-styles.xml");
        nifty.loadControlFile("nifty-default-controls.xml");
        
        nifty.addScreen("hud", new ScreenBuilder("hud"){{
                
             controller(headsUpDisplayController);
             layer(new LayerBuilder("foreground") {{
                childLayoutVertical();
                //backgroundColor("#0000");

                // panel added
                panel(new PanelBuilder("panel_top") {{
                    childLayoutHorizontal();
                    //backgroundColor("#0f08");
                    alignLeft();
                    height("10%");
                    width("100%");
                    // <!-- spacer -->
                    panel(new PanelBuilder("achivement_status") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("50%");
                        width("35%");
                        alignLeft();
                        // <!-- spacer -->
                        panel(new PanelBuilder("achivement_status_image") {{
                            childLayoutHorizontal();
                            height("35%");
                            width("90%");
                            alignLeft();
                            // <!-- spacer -->
                        }});
                        panel(new PanelBuilder("achivement_status_text") {{
                            childLayoutVertical();
                            //backgroundColor("#0f08");
                            height("65%");
                            width("90%");
                            alignLeft();
                            // <!-- spacer -->
                        }});
                        
                    }});
                    panel(new PanelBuilder("game_points") {{
                        childLayoutHorizontal();
                        //backgroundColor("#0f08");
                        height("20%");
                        width("30%");
                        alignRight();
                        // <!-- spacer -->
                    }});
                    
                    
                }});
                panel(new PanelBuilder("panel_centre") {{
                    childLayoutHorizontal();
                    //backgroundColor("#0f08");
                    height("70%");
                    width("100%");
                    panel(new PanelBuilder("panel_cneter_left") {{
                        //backgroundColor("#0f08");
                        height("100%");
                        width("40%");
                        alignRight();
                        // <!-- spacer -->
                    }});
                    panel(new PanelBuilder("game_messages") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("20%");
                        alignRight();
                        // <!-- spacer -->
                        
                        text(new TextBuilder("game_status") {{
                                text("");
                                font("Interface/Fonts/Default.fnt");
                                height("50%");
                                width("100%");
                        }});
                        text(new TextBuilder("respawn_estimate") {{
                                text("");
                                font("Interface/Fonts/Default.fnt");
                                height("50%");
                                width("100%");
                        }});
                    }});
                    panel(new PanelBuilder("panel_control_units") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("40%");
                        alignRight();
                        // <!-- spacer -->
                    }});
                    // <!-- spacer -->
                    
                }});
                panel(new PanelBuilder("panel_bottom") {{
                    childLayoutHorizontal();
                    //backgroundColor("#0f08");
                    height("20%");
                    width("100%");
                    // <!-- spacer -->
                    
                    panel(new PanelBuilder("panel_selected") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("20%");
                        // <!-- spacer -->
                        
                        text(new TextBuilder("rank") {{
                                text("Rank: "+killCount);
                                font("Interface/Fonts/Default.fnt");
                                height("10%");
                                width("100%");
                        }});
                        
                        text(new TextBuilder("score") {{
                                text("Kills: "+killCount);
                                font("Interface/Fonts/Default.fnt");
                                height("10%");
                                width("100%");
                        }});
                    }});
                    panel(new PanelBuilder("objective_panel") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("60%");
                        alignLeft();
                        // <!-- spacer -->
                        
                        
                    }});
                    panel(new PanelBuilder("minimap_panel") {{
                        childLayoutVertical();
                        //backgroundColor("#0f08");
                        height("100%");
                        width("20%");
                        // <!-- spacer -->
                    }});
                }});
                    
                
            }});
                
        }}.build(nifty));
        // </screen>
        nifty.gotoScreen("hud"); // start the screen
        gameMessages = new MessageBoard(nifty, nifty.getCurrentScreen().findElementByName("objective_panel"));
        updateHeadsUpDisplayInternal(this.app.avatar);
        
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void update(float tpf) {
        if(chaseCameraAppState.getSelectedCharacter().getKillCount()!=killCount){                               
            killCount = chaseCameraAppState.getSelectedCharacter().getKillCount();           
            Element niftyElement = nifty.getCurrentScreen().findElementByName("score");
            niftyElement.getRenderer(TextRenderer.class).setText("Kills: "+killCount);
        }
        if(!chaseCameraAppState.getSelectedCharacter().getRank().getDescription().equals(rank)){                               
            rank = chaseCameraAppState.getSelectedCharacter().getRank().getDescription();
            Element niftyElement = nifty.getCurrentScreen().findElementByName("rank");
            niftyElement.getRenderer(TextRenderer.class).setText("Rank: "+rank);
        }
        if(chaseCameraAppState.getSelectedCharacter() instanceof GameCharacterNode && chaseCameraAppState.getSelectedCharacter().getRank()!=selectedRank){
            selectedRank = chaseCameraAppState.getSelectedCharacter().getRank();
            chaseCameraAppState.setSelectedCharacterBust(((GameCharacterNode)chaseCameraAppState.getSelectedCharacter()));
        }
        if(app.getAvatar().isDead()){
            Element niftyElement = nifty.getCurrentScreen().findElementByName("respawn_estimate");
            final Integer nextReSpawnTime = WorldRunner.get().getNextReSpawnTime(app.getAvatar().getCountry());
            niftyElement.getRenderer(TextRenderer.class).setText("Re birth in: "+((nextReSpawnTime==null)?"??":nextReSpawnTime)+" seconds");
            wasDead = true;
        }else if(wasDead){
            Element niftyElement = nifty.getCurrentScreen().findElementByName("respawn_estimate");
            niftyElement.getRenderer(TextRenderer.class).setText("");
            wasDead = false;
        }
        
        
        
        int ap = (WorldRunner.get().getBlueVictoryPoints()*100)/(WorldRunner.get().getBlueVictoryPoints()+WorldRunner.get().getRedVictoryPoints());
        int ep = (WorldRunner.get().getRedVictoryPoints()*100)/(WorldRunner.get().getBlueVictoryPoints()+WorldRunner.get().getRedVictoryPoints());
        
        int bh = WorldRunner.get().getBlueHouese();
        int rh = WorldRunner.get().getRedHouses();
        
        if(alliedPoints!=ap || enemyPoints!=ep || blueHouses!=bh || redHouses!=rh){
            List<String> meaasages = observeGameEvents(ap, alliedPoints, ep, enemyPoints, bh, blueHouses, rh, redHouses);
            //gameMessages.appendMessages(meaasages);
            alliedPoints = ap;
            enemyPoints = ep;
            blueHouses = bh;
            redHouses = rh;
            for(Element e:nifty.getCurrentScreen().findElementByName("game_points").getChildren()){
                nifty.removeElement(nifty.getCurrentScreen(), e);
            }
            //nifty.executeEndOfFrameElementActions();
            final TextBuilder textBuilder = new TextBuilder();
            textBuilder.text(blueHouses+"");
            textBuilder.font("Interface/Fonts/Default.fnt");
            textBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementByName("game_points"));
            PanelBuilder panelBuilder = new PanelBuilder();
            panelBuilder.backgroundImage("Interface/friendly_progress.png");
            panelBuilder.width(alliedPoints+"%");
            panelBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementByName("game_points"));
            panelBuilder.backgroundImage("Interface/enemy_progress.png");
            panelBuilder.width(enemyPoints+"%");
            panelBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementByName("game_points"));
            textBuilder.text(redHouses+"");
            textBuilder.font("Interface/Fonts/Default.fnt");
            textBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementByName("game_points"));
        }
        
        if(WorldRunner.get().getAchivementStatus()!=null && !WorldRunner.get().getAchivementStatus().equals(currentAchivementStatus)){
            currentAchivementStatus = WorldRunner.get().getAchivementStatus();
            final Element findElementByName = nifty.getCurrentScreen().findElementByName("achivement_status_text");
            for(Element e:findElementByName.getChildren()){
                nifty.removeElement(nifty.getCurrentScreen(), e);
            }
            achivementObjectivePanel = new ScorllingMessagePanel(Collections.singletonList(currentAchivementStatus.getAchivementStatusText()), 55, nifty, findElementByName, "Interface/Fonts/Console.fnt", 10000l);
            
            for(Element e:nifty.getCurrentScreen().findElementByName("achivement_status_image").getChildren()){
                nifty.removeElement(nifty.getCurrentScreen(), e);
            }
            PanelBuilder panelBuilder = new PanelBuilder();
            panelBuilder.backgroundColor("#0f08");
            panelBuilder.width(currentAchivementStatus.getAchivementPercentage()+"%");
            panelBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementByName("achivement_status_image"));
            panelBuilder.backgroundColor("#0f01");
            panelBuilder.width(100-currentAchivementStatus.getAchivementPercentage()+"%");
            panelBuilder.build(nifty, nifty.getCurrentScreen(), nifty.getCurrentScreen().findElementByName("achivement_status_image"));
        }
        
        if(updateHUD){
            updateHeadsUpDisplayInternal(app.getAvatar());
            updateHUD = false;
        }        
        if(achivementObjectivePanel!=null){
            if(achivementObjectivePanel.update()){
                achivementObjectivePanel = null;
            }
        }
        gameMessages.update();
        
    }
    
    public void addMessage(String... message) {
        gameMessages.appendMessages(message);
    }

    public void updateHeadsUpDisplay() {
        updateHUD = true;
    }
    
    private void updateHeadsUpDisplayInternal(AvatarCharacterNode avatar) {
        if(nifty==null){
            return;
        } 
        
        headsUpDisplayController.rebind(nifty, avatar);
        
            
//        if(avatar.getCurrentObjectives() instanceof MinimapPresentable){
//            final MinimapPresentable objective = (MinimapPresentable)avatar.getCurrentObjectives();
//
//             if(objective==null){
//                return;
//            }
//            missionObjectivePanel  = new ScorllingMessagePanel(objective.getInstructions(), 150, nifty, objectivePanel, "Interface/Fonts/Default.fnt", null);
//            
//        }
    }

    void printVictoryText() {
        System.out.println("Victory!");
        Element niftyElement = nifty.getCurrentScreen().findElementByName("game_status");
        niftyElement.getRenderer(TextRenderer.class).setText("Victory!");
    }

    void printDefeatedText() {
        System.out.println("You have Lost!");
        Element niftyElement = nifty.getCurrentScreen().findElementByName("game_status");
        niftyElement.getRenderer(TextRenderer.class).setText("You have Lost!");
    }

    static List<String> observeGameEvents(int ap, int alliedPoints, int ep, int enemyPoints, int bh, int blueHouses, int rh, int redHouses) {
        List<String> ret = new ArrayList<String>();
        if(bh>blueHouses){
            ret.add("we have captured a house!");
        }
        if(bh<blueHouses){
            ret.add("we have lost a house!");
        }
        return ret;
    }


    
    

}
