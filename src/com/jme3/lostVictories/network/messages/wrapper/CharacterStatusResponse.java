package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.objectives.Objective;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by dharshanar on 1/04/17.
 */

public class CharacterStatusResponse extends LostVictoryMessage {

    private Set<CharacterMessage> units;

    private CharacterStatusResponse(){}
    
    public CharacterStatusResponse(Collection<CharacterMessage> units) {
        this.units = new HashSet<>(units);
    }
    public Collection<CharacterMessage> getCharacters() {
        return units;
    }
}
