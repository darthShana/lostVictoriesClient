package com.jme3.lostVictories.network.messages.wrapper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;
import java.util.HashSet;

import java.util.Set;
import java.util.UUID;


public class UpdateCharactersRequest extends LostVictoryMessage {
	
	private Set<CharacterMessage> characters = new HashSet<>();
	private UUID avatar;

	private UpdateCharactersRequest(){}

//	public UpdateCharactersRequest(UUID clientID) {
//		super(clientID);
//	}
	
        public UpdateCharactersRequest(UUID clientID, CharacterMessage character, UUID avatar){
            super(clientID);
            this.avatar = avatar;
            this.characters.add(character);
        }
        
//	public UpdateCharactersRequest(UUID clientID, Set<CharacterMessage> characters, CharacterMessage avatar) {
//		super(clientID);
//		this.characters.addAll(characters);
//		this.avatar = avatar.getId();
//	}

	public Set<CharacterMessage> getCharacters(){
		return characters;
	}
	
	public UUID getAvatar(){
		return avatar;
	}

}
