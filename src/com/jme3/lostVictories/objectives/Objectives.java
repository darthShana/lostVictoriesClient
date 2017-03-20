/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.GameCharacterNode;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dharshanar
 */
public class Objectives<T extends GameCharacterNode> {
    
    private final List<Objective<T>> objectives = new ArrayList<Objective<T>>();
    private Set<String> completedObjectives = new HashSet<String>();

    public Objectives() {
        synchronized(objectives){
            objectives.add((Objective<T>) new SurvivalObjective());
        }
    }
    
    

    public void addObjective(Objective<T> objective) {
        if(completedObjectives.contains(objective.getIdentity().toString())){
            return;
        }
        synchronized(objectives){
            for(Objective o:new ArrayList<Objective>(objectives)){
                if(o.getClass() == objective.getClass() || o.clashesWith(objective) || objective.clashesWith(o)){
                    o.completeObjective();
                }
                if(o.getIdentity().equals(objective.getIdentity())){
                    return;
                }
            }
            objectives.add(objective);
        }
    }

    public List<AIAction> planObjectives(T character, WorldMap worldMap) {
        List<AIAction> actions = new ArrayList<AIAction>();
        final ArrayList<Objective> arrayList;
        synchronized(objectives){
            arrayList = new ArrayList<Objective>(objectives);
        }
        for(Objective o : arrayList){
            if(!o.isComplete()){
                final AIAction planObjective = o.planObjective(character, worldMap);
                if(planObjective!=null){
                    actions.add(planObjective);
                }
            }
            if(o.isComplete){
                synchronized(objectives){
                    objectives.remove(o);
                    completedObjectives.add(o.getIdentity().toString());
                }
            }
        }
                
        Collections.sort(actions, new Comparator<AIAction>(){

            public int compare(AIAction o1, AIAction o2) {
                if(o1 instanceof ShootTargetAction && !(o2 instanceof ShootTargetAction)){
                    return 1;
                }else if(!(o1 instanceof ShootTargetAction) && o2 instanceof ShootTargetAction){
                    return -1;
                }else{
                    return 0;
                }
            }
        });
        
        return actions;
    }
    
    public Set<String> getCompletedObjectives(){
        return completedObjectives;
    }
    
    @Override
    public String toString() {
        return objectives.toString(); //To change body of generated methods, choose Tools | Templates.
    }

    public Set<Objective> getAllObjectives(){
        synchronized(objectives){
            return new HashSet<Objective>(objectives);
        }
    }

    public boolean isBusy() {
        synchronized(objectives){
            for(Objective o : objectives){
                if(!(o instanceof PassiveObjective)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAttacking() {
        synchronized(objectives){
            for(Objective o : objectives){
                if(o instanceof AttackAndTakeCoverObjective || o instanceof AttackBoggies || o instanceof AttackObjective){
                    return true;
                }
            }
        }
        return false;
    }

    
}
