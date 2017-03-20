/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;


import com.jme3.lostVictories.network.messages.DeathNotificationRequest;
import com.jme3.lostVictories.network.messages.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.DisembarkPassengersRequest;
import com.jme3.lostVictories.network.messages.EquipmentCollectionRequest;
import com.jme3.lostVictories.network.messages.PassengerDeathNotificationRequest;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

/**
 *
 * @author dharshanar
 */
public class NetworkClient {
    
    protected final ChannelFuture channelFuture;
    protected ClientBootstrap bootstrap;
    
    SynchronousQueue responseQueue = new SynchronousQueue();
    private final UUID clientID;
    
    
    public NetworkClient(String ipAddress, int port, UUID clientID, final ResponseFromServerMessageHandler responseFromServerMessageHandler) {
        Executor bossPool = Executors.newFixedThreadPool(1);
        Executor workerPool = Executors.newFixedThreadPool(2);
        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);
        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(
                                            
                    new ObjectEncoder(),
                    new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),
                    new AsyncClientMessageHandler(responseFromServerMessageHandler),
                    new SyncClientMessageHandler(responseQueue));
            }
        };
        bootstrap = new ClientBootstrap(channelFactory);
        bootstrap.setPipelineFactory(pipelineFactory);
        // Phew. Ok. We built all that. Now what ?
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        
        InetSocketAddress addressToConnectTo = new InetSocketAddress(ipAddress, port);
        channelFuture = bootstrap.connect(addressToConnectTo);
        this.clientID = clientID;
        
        while(!channelFuture.getChannel().isConnected()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    
    }

    public void shutDown() {
        bootstrap.shutdown();
    }

    public ChannelFuture updateLocalCharacters(Set<CharacterMessage> toUpdate, CharacterMessage avatar) {
        synchronized(channelFuture.getChannel()){
            return channelFuture.getChannel().write(new UpdateCharactersRequest(clientID, toUpdate, avatar));        
        }
    }

    public SynchronousQueue checkoutSceen(UUID avatar) {
        synchronized(channelFuture.getChannel()){
            channelFuture.getChannel().write(new CheckoutScreenRequest(clientID, avatar));
            return responseQueue;
        }
    }

    public void deathNotification(UUID killer, UUID victim) {
        synchronized(channelFuture.getChannel()){
            channelFuture.getChannel().write(new DeathNotificationRequest(clientID, killer, victim));
        }
    }
    
    public void gunnerDeathNotification(UUID killer, UUID victim) {
        synchronized(channelFuture.getChannel()){
            channelFuture.getChannel().write(new PassengerDeathNotificationRequest(clientID, killer, victim));
        }
    }

    public void addObjective(UUID characterId, UUID identity, String toMessage) {
        synchronized(channelFuture.getChannel()){
            channelFuture.getChannel().write(new AddObjectiveRequest(clientID, characterId, identity, toMessage));
        }
    }

    public void requestEquipmentCollection(UUID equipmentID, UUID characterID) {
        synchronized(channelFuture.getChannel()){
            channelFuture.getChannel().write(new EquipmentCollectionRequest(clientID, equipmentID, characterID));
        }
    }
    
    public void boardVehicle(UUID vehicleUUID, UUID characterID){
        synchronized(channelFuture.getChannel()){
            channelFuture.getChannel().write(new BoardVehicleRequest(clientID, vehicleUUID, characterID));
        }
    }

    public void disembarkPassengers(UUID vehicleUUID) {
        synchronized(channelFuture.getChannel()){
            channelFuture.getChannel().write(new DisembarkPassengersRequest(clientID, vehicleUUID));
        }
    }
    
}
