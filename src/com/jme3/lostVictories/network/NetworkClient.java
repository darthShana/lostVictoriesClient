/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;

import com.jme3.lostVictories.network.messages.wrapper.DeathNotificationRequest;
import com.jme3.lostVictories.network.messages.wrapper.UpdateCharactersRequest;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.wrapper.CheckoutScreenRequest;
import com.jme3.lostVictories.network.messages.wrapper.AddObjectiveRequest;
import com.jme3.lostVictories.network.messages.wrapper.BoardVehicleRequest;
import com.jme3.lostVictories.network.messages.wrapper.DisembarkPassengersRequest;
import com.jme3.lostVictories.network.messages.wrapper.EquipmentCollectionRequest;
import com.jme3.lostVictories.network.messages.wrapper.LostVictoryMessage;
import com.jme3.lostVictories.network.messages.wrapper.PassengerDeathNotificationRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;


/**
 *
 * @author dharshanar
 */
public class NetworkClient {
    
    EventLoopGroup group = new NioEventLoopGroup();
    final Channel channel;
    
    private final UUID clientID;
    private final String ipAddress;
    private final int port;
    
    
    public NetworkClient(String ipAddress, int port, UUID clientID, final ResponseFromServerMessageHandler responseFromServerMessageHandler) {
        this.ipAddress = ipAddress;
        this.port = port;
//        Executor bossPool = Executors.newCachedThreadPool();
//        Executor workerPool = Executors.newCachedThreadPool();
//        ChannelFactory channelFactory = new NioClientSocketChannelFactory(bossPool, workerPool);
//        ChannelPipelineFactory pipelineFactory = new ChannelPipelineFactory() {
//            public ChannelPipeline getPipeline() throws Exception {
//                return Channels.pipeline(
//                                            
//                    new ObjectEncoder(),
//                    new ObjectDecoder(ClassResolvers.cacheDisabled(getClass().getClassLoader())),
//                    new AsyncClientMessageHandler(responseFromServerMessageHandler),
//                    new SyncClientMessageHandler(responseQueue));
//            }
//        };
//        bootstrap = new ClientBootstrap(channelFactory);
//        bootstrap.setPipelineFactory(pipelineFactory);
//        // Phew. Ok. We built all that. Now what ?
//        bootstrap.setOption("child.tcpNoDelay", true);
//        bootstrap.setOption("child.keepAlive", true);
//        
//        InetSocketAddress addressToConnectTo = new InetSocketAddress(ipAddress, port);
//        channelFuture = bootstrap.connect(addressToConnectTo);
        this.clientID = clientID;
        
//        while(!channelFuture.getChannel().isConnected()){
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                throw new RuntimeException(ex);
//            }
//        }

        

        Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .handler(responseFromServerMessageHandler);
             

        try {
            channel = b.bind(0).sync().channel();

            // Broadcast the QOTM request to port 8080.
//            ch.writeAndFlush(new DatagramPacket(
//                    Unpooled.copiedBuffer("Quote", CharsetUtil.UTF_8),
//                    new InetSocketAddress("localhost", PORT))).sync();


// QuoteOfTheMomentClientHandler will close the DatagramChannel when a
// response is received.  If the channel is not closed within 5 seconds,
// print an error message and quit.
//            if (!channel.closeFuture().await(5000)) {
//                System.err.println("Quote request timed out.");
//            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        
    
    }

    public void shutDown() {
        group.shutdownGracefully();
    }
    
    
    public void checkoutSceen(UUID avatar) {
        sendMessage(new CheckoutScreenRequest(clientID, avatar));
    }

    public void updateLocalCharacters(Set<CharacterMessage> toUpdate, UUID avatar, long clientStartTime) {
        toUpdate.forEach(c->{
//            if("2fbe421f-f701-49c9-a0d4-abb0fa904204".equals(c.getId().toString())){
//                System.out.println("in here sending avatar version:"+c.getVersion());
//            }
            sendMessage(new UpdateCharactersRequest(clientID, c, avatar, System.currentTimeMillis()-clientStartTime));
        });
        
    }

    public void deathNotification(UUID killer, UUID victim) {
        sendMessage(new DeathNotificationRequest(clientID, killer, victim));
    }
    
    public void gunnerDeathNotification(UUID killer, UUID victim) {
        sendMessage(new PassengerDeathNotificationRequest(clientID, killer, victim));
    }

    public void addObjective(UUID characterId, UUID identity, String toMessage) {
        sendMessage(new AddObjectiveRequest(clientID, characterId, identity, toMessage));
    }

    public void requestEquipmentCollection(UUID equipmentID, UUID characterID) {
        sendMessage(new EquipmentCollectionRequest(clientID, equipmentID, characterID));
    }
    
    public void boardVehicle(UUID vehicleUUID, UUID characterID) {
        sendMessage(new BoardVehicleRequest(clientID, vehicleUUID, characterID));
    }

    public void disembarkPassengers(UUID vehicleUUID) {
        sendMessage(new DisembarkPassengersRequest(clientID, vehicleUUID));
    }
    
    
    private void sendMessage(LostVictoryMessage message)  {
        
        try {
            byte[] data = MAPPER.writeValueAsBytes(message);

            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
            GZIPOutputStream gzip;
            gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.close();
            byte[] compressed = bos.toByteArray();
            bos.close();
            synchronized(channel){
                try {
                    channel.writeAndFlush(new DatagramPacket(
                            Unpooled.copiedBuffer(compressed),
                            new InetSocketAddress(ipAddress, port))).sync();
//                    System.out.println("send request:"+message.getClass()+" size"+data.length+"/"+compressed.length);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
