package br.ufu.facom.mehar.sonar.interceptor.openflow;

import java.util.HashMap;
import java.util.Map;

public enum OFMessageType {
    OFPT_HELLO						(0, OFMessageTypeClassification.BIDIRECTIONAL),
    OFPT_ERROR						(1, OFMessageTypeClassification.BIDIRECTIONAL),
    OFPT_ECHO_REQUEST				(2, OFMessageTypeClassification.BIDIRECTIONAL),
    OFPT_ECHO_REPLY					(3, OFMessageTypeClassification.BIDIRECTIONAL),
    OFPT_EXPERIMENTER				(4, OFMessageTypeClassification.BIDIRECTIONAL),
    OFPT_FEATURES_REQUEST			(5, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_FEATURES_REPLY				(6, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_GET_CONFIG_REQUEST			(7, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_GET_CONFIG_REPLY			(8, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_SET_CONFIG					(9, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_PACKET_IN					(10, OFMessageTypeClassification.ASYNCHRONOUS),
    OFPT_FLOW_REMOVED				(11, OFMessageTypeClassification.ASYNCHRONOUS),
    OFPT_PORT_STATUS				(12, OFMessageTypeClassification.ASYNCHRONOUS),
    OFPT_PACKET_OUT					(13, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_FLOW_MOD					(14, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_GROUP_MOD					(15, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_PORT_MOD					(16, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_TABLE_MOD					(17, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_MULTIPART_REQUEST			(18, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_MULTIPART_REPLY			(19, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_BARRIER_REQUEST			(20, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_BARRIER_REPLY				(21, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_QUEUE_GET_CONFIG_REQUEST	(22, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_QUEUE_GET_CONFIG_REPLY		(23, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_ROLE_REQUEST				(24, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_ROLE_REPLY					(25, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_GET_ASYNC_REQUEST			(26, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_GET_ASYNC_REPLY			(27, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_SET_ASYNC					(28, OFMessageTypeClassification.SYNCHRONOUS),
    OFPT_METER_MOD					(29, OFMessageTypeClassification.SYNCHRONOUS);

	//Fields
    private int id;
    private OFMessageTypeClassification classification;
    
    //Constructor
	OFMessageType(int id, OFMessageTypeClassification direction) {
        this.id = id;
        this.classification = direction;
    }
	
	//ID to Enum Map
    private static Map<Integer, OFMessageType> idMap = new HashMap<>();
    static {
        for (OFMessageType type : OFMessageType.values()) {
            idMap.put(type.getId(), type);
        }
    }
    public static OFMessageType getById(int id) {
        return idMap.get(id);
    }
    
    //Getter's
    public int getId() {
    	return this.id;
    }

    public OFMessageTypeClassification getClassification() {
        return classification;
    }
}