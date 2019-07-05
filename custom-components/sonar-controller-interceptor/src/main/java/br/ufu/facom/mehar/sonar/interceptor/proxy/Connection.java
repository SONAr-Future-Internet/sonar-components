package br.ufu.facom.mehar.sonar.interceptor.proxy;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.projectfloodlight.openflow.protocol.OFEchoReply;
import org.projectfloodlight.openflow.protocol.OFEchoRequest;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFeaturesReply;
import org.projectfloodlight.openflow.protocol.OFHello;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHeader;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageHolder;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessagePayload;
import br.ufu.facom.mehar.sonar.interceptor.openflow.OFMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

public class Connection {
	private static final Logger logger = LoggerFactory.getLogger(Connection.class);
	
	private static final byte[] ECHO_DATA = new byte[] { 0x53, 0x74, 0x6f, 0x70, 0x63, 0x6f, 0x63, 0x6b };
	
    //Unique connection identification
    private int uniqueId;
    
    //Datapath ID (DPID) collected with FEATURES_REPLY
    private byte[] datapathId;
    private String datapathIdString;
    
    // Netty channel used for the switch connection.
    private Channel upstream;
    private OFVersion upstreamVersion;
    // Netty channel used for the controller connection.
    private Channel downstream;
    private OFVersion downstreamVersion;
    
    
    // TRUE if the downstream connection is active
    private boolean downstreamActive = false;
    
    // Queue for outgoing packets to controller while waiting for downstream connection
    private Queue<OFMessageHolder> downstreamQueue = new ArrayDeque<>();

    //Statistics per Channel and OFMessageType
    private Map<OFMessageType, AtomicInteger> upstreamReceived = Collections.synchronizedMap(new HashMap<OFMessageType, AtomicInteger>());
    private Map<OFMessageType, AtomicInteger> downstreamReceived = Collections.synchronizedMap(new HashMap<OFMessageType, AtomicInteger>());

    //TRUE if the handshake has been completed
    private boolean readyForInjectMessage = false;

    /*
     * Constructor
     */
    public Connection(int uniqueId) {
    	//Set unique identification
        this.uniqueId = uniqueId;
        
        //Init Datapath ID
        setDatapathId(new byte[8]);

        //Start statistics
        for (OFMessageType type : OFMessageType.values()) {
            upstreamReceived.put(type, new AtomicInteger(0));
            downstreamReceived.put(type, new AtomicInteger(0));
        }
    }

    /*
     * Channel Management
     */
    //Register channel between switch and proxy
    public synchronized void registerUpstream(Channel upstreamChannel) {
        upstream = upstreamChannel;
        logger.info("["+uniqueId+"] Connected incoming stream from Device: " + upstream.remoteAddress());
    }

    //Register channel between controller and proxy
    public synchronized void registerDownstream(Channel downstreamChannel) {
        downstream = downstreamChannel;
        logger.info("["+uniqueId+"] Connecting outgoing stream for Controller " + downstream.remoteAddress()+"...");
    }

    //Set downstream channel as active send all messages from the queue
    public synchronized void activeDownstream() {
        downstreamActive = true;
        logger.info("["+uniqueId+"] Connected outgoing stream for Controller " + downstream.remoteAddress());

        OFMessageHolder container;
        
        //Send all messages from the queue
        while ((container = downstreamQueue.poll()) != null) {
            downstream.writeAndFlush(container);
        }
    }

    //Unregister channel between switch and proxy
    public synchronized void unregisterUpstream() {
        upstream = null;
        readyForInjectMessage = false;

        logger.info("["+uniqueId+"] Disconnected incoming stream from Device " + downstream.remoteAddress());

        if (downstream != null) {
            downstream.close();
        }
    }
    
    //Unregister channel between proxy and controller
    public synchronized void unregisterDownstream() {
        downstream = null;
        downstreamActive = false;
        readyForInjectMessage = false;

        logger.info("["+uniqueId+"] Disconnected outgoing stream for Controller " + (downstream != null? downstream.remoteAddress(): ""));

        if (upstream != null) {
            upstream.close();
        }
    }

    
    /*
     * Message Processing
     */
    //Process message and forward if required
    public synchronized void receive(Channel incoming, OFMessageHolder message) {
        ChannelType channelSource = (incoming == upstream ? ChannelType.SWITCH : ChannelType.CONTROLLER);
        ChannelType channelDestination = (incoming != upstream ? ChannelType.SWITCH : ChannelType.CONTROLLER);

        //Filter echo replies, verify ECHO data pattern and set channel type to PROXY
        if (message.getMessageType() == OFMessageType.OFPT_ECHO_REPLY) {
            OFEchoReply ofEchoReply = (OFEchoReply) message.getMessage();

            if (Arrays.equals(ECHO_DATA, ofEchoReply.getData())) {
                channelDestination = ChannelType.PROXY;
            }
        }

        //Intercept HELLO and record OpenFlow version
        if (message.getMessageType() == OFMessageType.OFPT_HELLO) {
            OFHello ofHello = (OFHello) message.getMessage();

            if (channelSource == ChannelType.SWITCH) {
                upstreamVersion = ofHello.getVersion();
            } else {
                downstreamVersion = ofHello.getVersion();
            }
        }

        //Intercept FeaturesReply and record Datapath ID
        if (message.getMessageType() == OFMessageType.OFPT_FEATURES_REPLY) {
            OFFeaturesReply ofFeaturesReply = (OFFeaturesReply) message.getMessage();
            setDatapathId(ofFeaturesReply.getDatapathId().getBytes());
        }

        //Send if channel type isn't PROXY
        if (channelDestination != ChannelType.PROXY) {
        	//Send message through channel 
            send(channelSource, channelDestination, message);
            
            //After send, if message is FeaturesReply set readyForInjectMessage=TRUE (handshake complete)
            if(message.getMessageType() == OFMessageType.OFPT_FEATURES_REPLY) {
            	logger.info("Device connected successfully! IP:"+incoming.remoteAddress()+" DPID:"+datapathIdString);
                readyForInjectMessage = true;
            }
        }
    }

    //Send a message to a specific channel
    public synchronized void send(ChannelType channelSource, Channel destination, OFMessageHolder message) {
        ChannelType channelDestination = (destination == upstream ? ChannelType.SWITCH : ChannelType.CONTROLLER);
        send(channelSource, channelDestination, message);
    }

    //Send a message according to source channelType and destination channelType
    public synchronized void send(ChannelType channelSource, ChannelType channelDestination, OFMessageHolder message) {
        Channel outputChannel = channelDestination == ChannelType.SWITCH ? upstream : downstream;

        //If the message should be sent to the switch (upstream) or if the downstream is active
        if (outputChannel!=null && ((outputChannel != downstream) || downstreamActive)) {
            outputChannel.writeAndFlush(message);
        } else {
        	//If not, enqueue message
            downstreamQueue.add(message);
        }
    }
    
    /*
     * Util 
     */
    //Deserialization of OFMessage
	private byte[] getRawData(OFMessage message) {
		ByteBuf byteBuf = upstream.alloc().buffer(8);
        message.writeTo(byteBuf);

        byte[] rawData = new byte[byteBuf.readableBytes()];

        byteBuf.resetReaderIndex();
        byteBuf.readBytes(rawData);

        ReferenceCountUtil.release(byteBuf);
        
		return rawData;
	}
	
	//Build a message with ECHO Request according to the upstream
	public OFMessageHolder createPing() {
        OFEchoRequest request = OFFactories.getFactory(upstreamVersion).echoRequest(ECHO_DATA);
        OFMessageHeader header = new OFMessageHeader((short) upstreamVersion.getWireVersion(), (short) OFMessageType.OFPT_ECHO_REQUEST.getId(), 8, request.getXid());
        OFMessagePayload payload = new OFMessagePayload(getRawData(request));
        return new OFMessageHolder(header, payload, OFMessageType.OFPT_ECHO_REQUEST, request);
    }
	
	//Identify ChannelType according to the Channel
	public ChannelType getChannelType(Channel channel) {
	    return (channel == downstream ? ChannelType.CONTROLLER : ChannelType.SWITCH);
	}

	//Set datapathId
    public void setDatapathId(byte[] datapathId) {
        this.datapathId = datapathId;

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            stringBuilder.append(String.format("%02x", datapathId[i]));
        }

        datapathIdString = stringBuilder.toString();
    }

    /*
     * Getters
     */
    public int getUniqueId() {
        return uniqueId;
    }

    public byte[] getDatapathId() {
        return datapathId;
    }

    public String getDatapathIdString(){
        return datapathIdString;
    }
    
    public OFVersion getUpstreamVersion() {
        return upstreamVersion;
    }

    public OFVersion getDownstreamVersion() {
        return downstreamVersion;
    }

    public Channel getUpstream() {
        return upstream;
    }

    public boolean isReadyForInjectMessage() {
        return readyForInjectMessage;
    }

	public Channel getDownstream() {
		return downstream;
	}

	public void setDownstream(Channel downstream) {
		this.downstream = downstream;
	}
}