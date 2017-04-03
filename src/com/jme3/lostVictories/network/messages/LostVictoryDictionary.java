package com.jme3.lostVictories.network.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dharshanar on 4/04/17.
 */
public class LostVictoryDictionary {

    String[][] translations = new String[][]{
            {"Models/Structures/casaMedieval.j3o", "!0"},
            {"Models/Structures/house.j3o", "!1"},
            {"Models/Structures/house2.j3o", "!2"},
            {"Models/Structures/house_1.j3o", "!3"},
            {"Models/Structures/cottage.j3o", "!4"},
    };

    Map<String, String> encoding = new HashMap<>();
    Map<String, String> decoding = new HashMap<>();

    public LostVictoryDictionary(){
        for(String[] translation:translations){
            encoding.put(translation[0], translation[1]);
            decoding.put(translation[1], translation[0]);
        }
    }

    public String encode(String s) {
        for(Map.Entry<String, String> entry:encoding.entrySet()){
            s = s.replace(entry.getKey(), entry.getValue());
        }
        return s;
    }

    public String decode(String s) {
        for(Map.Entry<String, String> entry:decoding.entrySet()){
            s = s.replace(entry.getKey(), entry.getValue());
        }
        return s;
    }
}

