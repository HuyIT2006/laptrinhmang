package common;

/**
 * Định nghĩa các loại tin nhắn, cổng mặc định và các hằng số.
 */
public class Protocol {
    // Message types
    public static final String REGISTER = "REGISTER";          // Peer -> Tracker
    public static final String REGISTER_OK = "REGISTER_OK";    // Tracker -> Peer
    public static final String PEER_JOINED = "PEER_JOINED";    // Tracker -> All Peers
    public static final String PEER_LEFT = "PEER_LEFT";        // Tracker -> All Peers
    public static final String SHARE_FILE = "SHARE_FILE";      // Peer -> Tracker
    public static final String SHARE_FILE_OK = "SHARE_FILE_OK"; // Tracker -> Peer
    public static final String SEARCH_FILE = "SEARCH_FILE";    // Peer -> Tracker
    public static final String SEARCH_RESULT = "SEARCH_RESULT"; // Tracker -> Peer
    public static final String CHAT = "CHAT";                  // Peer -> Peer
    public static final String FILE_REQUEST = "FILE_REQUEST";  // Peer -> Peer
    public static final String FILE_ACCEPT = "FILE_ACCEPT";    // Peer -> Peer
    public static final String FILE_REJECT = "FILE_REJECT";    // Peer -> Peer
    public static final String FILE_COMPLETE = "FILE_COMPLETE"; // Peer -> Peer
    public static final String UNREGISTER = "UNREGISTER";      // Peer -> Tracker
    public static final String GET_PEERS = "GET_PEERS";
    public static final String PEER_LIST = "PEER_LIST";
    
    // Separator
    public static final String SEP = "|";
    public static final String SEPARATOR = "|";
    
    // Default ports
    public static final int TRACKER_PORT = 5000;
    public static final int DEFAULT_CHAT_PORT = 6000;
    public static final int DEFAULT_FILE_PORT = 7000;
    
    // File transfer
    public static final int BUFFER_SIZE = 8192; // 8KB chunks
}
