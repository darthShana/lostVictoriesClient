/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.UpdateCharactersResponse;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 *
 * @author dharshanar
 */
 class AsyncClientMessageHandler extends SimpleChannelHandler {
    private final ResponseFromServerMessageHandler responseFromServerMessageHandler;

    AsyncClientMessageHandler(ResponseFromServerMessageHandler responseFromServerMessageHandler) {
        this.responseFromServerMessageHandler = responseFromServerMessageHandler;       
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        LostVictoryMessage message = (LostVictoryMessage)e.getMessage();
        
        if(message instanceof UpdateCharactersResponse){
            responseFromServerMessageHandler.handle((UpdateCharactersResponse)message);
        }
        
        super.messageReceived(ctx, e);
    }


    
}
