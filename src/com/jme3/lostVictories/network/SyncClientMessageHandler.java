/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.network.messages.LostVictoryMessage;
import java.util.concurrent.SynchronousQueue;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

/**
 *
 * @author dharshanar
 */
class SyncClientMessageHandler extends SimpleChannelHandler{
    private final SynchronousQueue responseQueue;

    public SyncClientMessageHandler(SynchronousQueue responseQueue) {
        this.responseQueue = responseQueue;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        LostVictoryMessage message = (LostVictoryMessage)e.getMessage();
        responseQueue.offer(message);
    }
}
