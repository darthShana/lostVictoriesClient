/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.app.Application;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.CommandingOfficer;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Lieutenant;
import com.jme3.lostVictories.characters.Rank;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.controls.dynamic.PanelCreator;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.loaderv2.types.PanelType;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class HeadsUpDisplayController implements ScreenController {
    private final LostVictory app;
    private final RealTimeStrategyAppState chaseCameraAppState;
    private Map<UUID, Boolean> expandedUnits = new HashMap<UUID, Boolean>();

    HeadsUpDisplayController(Application app, RealTimeStrategyAppState chaseCameraAppState) {
        this.app = (LostVictory) app;
        this.chaseCameraAppState = chaseCameraAppState;
    }

    
    public void bind(Nifty nifty, Screen screen) {
        nifty.setIgnoreKeyboardEvents(true);
        addAllCharactersUnderCommand(nifty, this.app.getAvatar());
    }

    public void onStartScreen() {
        
    }

    public void onEndScreen() {
        
    }
    
    public void selectCharacterInCommand(String identity){
        final Commandable characterUnderCommand = app.getAvatar().getCharacterUnderCommand(identity);
        if(characterUnderCommand!=null){
            chaseCameraAppState.selectCharacter(characterUnderCommand);
        }
    }
    
    public void expandCharacter(String identity){
        final Commandable characterUnderCommand = app.getAvatar().getCharacterUnderCommand(identity);
        if(characterUnderCommand!=null){
            expandedUnits.put(characterUnderCommand.getIdentity(), true);
            chaseCameraAppState.updateHeadsUpDisplay();

        }
    }
            
    public void colapseCharacter(String identity){
        final Commandable characterUnderCommand = app.getAvatar().getCharacterUnderCommand(identity);
        if(characterUnderCommand!=null){
            expandedUnits.put(characterUnderCommand.getIdentity(), false);
            chaseCameraAppState.updateHeadsUpDisplay();

        }
    }

    void rebind(Nifty nifty, AvatarCharacterNode avatar) {
        if(nifty!=null && nifty.getCurrentScreen()!=null){
            for(Element e:nifty.getCurrentScreen().findElementByName("panel_control_units").getChildren()){
                nifty.removeElement(nifty.getCurrentScreen(), e);
            }
            //nifty.executeEndOfFrameElementActions();
            addAllCharactersUnderCommand(nifty, avatar);
        }
    }

    public void addAllCharactersUnderCommand(Nifty nifty, AvatarCharacterNode avatar) {
        int i = 0;       
        final Element parent = nifty.getCurrentScreen().findElementByName("panel_control_units"); 

        for(Commandable n:avatar.getCharactersUnderCommand()){
            if(n!=null && (!(n instanceof GameCharacterNode) || ((GameCharacterNode)n).getBoardedVehicle()==null)){
                i = addRow(i, n, nifty, parent); 
            }
        }
        parent.layoutElements();
    }
    
    public int addRow(int i, Commandable n, Nifty nifty, Element parent){
        if(!expandedUnits.containsKey(n.getIdentity())){
            expandedUnits.put(n.getIdentity(), false);
        }
        
        PanelBuilder panelCreator = new PanelBuilder("row_"+n.getIdentity().toString());
        panelCreator.childLayoutHorizontal();
        panelCreator.width("100%");
        panelCreator.height("90px");
        panelCreator.alignRight();
        //panelCreator.backgroundColor("#0f08");
        Element row = panelCreator.build(nifty, nifty.getCurrentScreen(), parent);
        
        panelCreator = new PanelBuilder(n.getIdentity().toString()+"_pad");        
        panelCreator.width("*");
        panelCreator.build(nifty, nifty.getCurrentScreen(), row);
        
        if (!expandedUnits.get(n.getIdentity()) || Rank.PRIVATE==n.getRank()) {
            panelCreator = new PanelBuilder(n.getIdentity().toString() + "_minimised");
            panelCreator.childLayoutHorizontal();
            panelCreator.alignRight();
//        panelCreator.backgroundColor("#0f08");
            panelCreator.width("95px");
            panelCreator.height("90px");
            Element minimised = panelCreator.build(nifty, nifty.getCurrentScreen(), row);

            if (Rank.PRIVATE!=n.getRank()) {
                panelCreator = new PanelBuilder(n.getIdentity().toString() + "_open_icon");
                panelCreator.backgroundImage("Interface/arrow-left-01.png");
                panelCreator.width("25px");
                panelCreator.height("70px");
                panelCreator.interactOnClick("expandCharacter("+n.getIdentity()+")");
                panelCreator.build(nifty, nifty.getCurrentScreen(), minimised);
//            
//            System.out.println("in here add in g commander");
//            ImageBuilder imageBuilder = new ImageBuilder(n.getIdentity().toString()+"_open");
//            imageBuilder.filename("Interface/arrow-left-01.png");
//            imageBuilder.visibleToMouse(true);
                //
                //imageBuilder.interactOnClick("selectCharacterInCommand("+n.getIdentity()+")");
//            imageBuilder.build(nifty, nifty.getCurrentScreen(), open);
            } else {
                panelCreator = new PanelBuilder(n.getIdentity().toString() + "_open");
                panelCreator.width("*");
                panelCreator.build(nifty, nifty.getCurrentScreen(), minimised);
            }

            addIcon(i, n, nifty, minimised, false);
            minimised.layoutElements();
        }
        
        if(Rank.PRIVATE!=n.getRank() && expandedUnits.get(n.getIdentity())){
            final Set<Commandable> charactersUnderCommand = ((CommandingOfficer)n).getCharactersUnderCommand();
            for(Iterator<Commandable> it = charactersUnderCommand.iterator();it.hasNext();){
                if(!(it.next() instanceof GameCharacterNode)){
                    it.remove();
                }
            }
            
            panelCreator = new PanelBuilder(n.getIdentity().toString()+"_maximised"); 
            panelCreator.childLayoutHorizontal();
            panelCreator.alignRight();
//            panelCreator.backgroundColor("#0f08");
            panelCreator.width(((70*(charactersUnderCommand.size()+1))+25)+"px");
            panelCreator.height("90px");
            Element maximised = panelCreator.build(nifty, nifty.getCurrentScreen(), row);
            
            panelCreator = new PanelBuilder(n.getIdentity().toString()+"_close_icon"); 
            panelCreator.backgroundImage("Interface/arrow-right-01.png");
            panelCreator.width("25px");
            panelCreator.height("70px");
            panelCreator.interactOnClick("colapseCharacter("+n.getIdentity()+")");
            panelCreator.build(nifty, nifty.getCurrentScreen(), maximised);
            addIcon(i, n, nifty, maximised, true);
            for(Commandable c:charactersUnderCommand){
                addIcon(i, c, nifty, maximised, false);
            }
            
        }
        row.layoutElements();
        return i;
    }

    protected int addIcon(int i, Commandable n, Nifty nifty, Element parent, boolean expanded) {
        PanelBuilder panelCreator = new PanelBuilder(n.getIdentity().toString());
        panelCreator.childLayoutVertical();
        panelCreator.width("70px");
        panelCreator.height("90px");
        panelCreator.alignCenter();
        //panelCreator.backgroundColor("#0f08");
        Element cell = panelCreator.build(nifty, nifty.getCurrentScreen(), parent);
        
        
        ImageBuilder imageBuilder = new ImageBuilder(n.getIdentity().toString()+"_image");
        imageBuilder.filename(UnitIconMap.getIcon(n, expanded));
        imageBuilder.visibleToMouse(true);
        imageBuilder.interactOnClick("selectCharacterInCommand("+n.getIdentity()+")");
        imageBuilder.build(nifty, nifty.getCurrentScreen(), cell);
        
        if(Rank.PRIVATE!=n.getRank() && !expanded){
            final TextBuilder textBuilder = new TextBuilder();
            textBuilder.text(((CommandingOfficer)n).getCharactersUnderCommand().size()+"/"+n.getRank().getFullStrengthPopulation());
            textBuilder.font("Interface/Fonts/Default.fnt");
            textBuilder.build(nifty, nifty.getCurrentScreen(), cell);
        }
        return i;
    }
    
}
