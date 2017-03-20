/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.headsUpDisplay;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.elements.Element;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author dharshanar
 */
public class MessageBoard {
    private final Element niftyPanel;
    private final Nifty nifty;
    private final LinkedList<Element> allElements = new LinkedList<Element>();
    private final Map<Element, Long> elementTime = new HashMap<Element, Long>();
    

    public MessageBoard(Nifty nifty, Element niftyPanel) {
        this.niftyPanel = niftyPanel;
        this.nifty = nifty;
    }

    public void appendMessages(String... meaasages) {
        if(meaasages==null || meaasages.length==0){
            return;
        }
        
        for(String message:meaasages){
            if(allElements.size()>14){
                final Element remove = allElements.remove(0);
                nifty.removeElement(nifty.getCurrentScreen(), remove);
                elementTime.remove(remove);
            }
            final TextBuilder textBuilder = new TextBuilder();
            textBuilder.text(message);
            textBuilder.font("Interface/Fonts/Console.fnt");
            textBuilder.alignLeft();
            Element currentElement = textBuilder.build(nifty, nifty.getCurrentScreen(), niftyPanel);
            allElements.add(currentElement);
            elementTime.put(currentElement, System.currentTimeMillis());
        }
    }

    public void update() {
        for(Iterator<Entry<Element, Long>> it =elementTime.entrySet().iterator();it.hasNext();){
            Entry<Element, Long> e = it.next();
            if(System.currentTimeMillis()-e.getValue()>10000){
                final Element remove = allElements.remove(0);
                nifty.removeElement(nifty.getCurrentScreen(), remove);
                it.remove();
            }
        }
    }

    
}
