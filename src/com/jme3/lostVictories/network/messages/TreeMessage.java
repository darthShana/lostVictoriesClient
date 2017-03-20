package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.UUID;

public class TreeMessage implements Serializable{
	
    private UUID id;
    private Vector location;
    private String type;
    private boolean standing;

    public UUID getId() {
        return id;
    }

    public Vector getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public boolean isStanding() {
        return standing;
    }
	

}
