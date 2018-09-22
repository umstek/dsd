package lk.uom.cse14.dsd.bscom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Abstract class for communicating with bootstrap server. Has methods for parsing responses.
 */
abstract class RegistryCommunicator {
    String serverHost;
    int serverPort;
    String ownHost;
    int ownPort;
    String username;

    /**
     * Allows creating different types of RegistryCommunicators.
     *
     * @param serverHost The address of the bootstrap server (registry).
     * @param serverPort The port of the bootstrap server (registry).
     */
    RegistryCommunicator(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    /**
     * Parses un-registration response received from the bootstrap server.
     *
     * @param response Response received from bootstrap server.
     * @return Whether un-registration was successful.
     * @throws UnknownUnregisterResponseException If unknown value or message code received, this will throw.
     */
    static boolean parseUnregisterResponse(String response) throws UnknownUnregisterResponseException {
        /*
         * <message length> UNROK value
         */
        StringTokenizer tokenizer = new StringTokenizer(response);
        tokenizer.nextToken(); // Message length

        String messageCode = tokenizer.nextToken();
        int statusCode = Integer.parseInt(tokenizer.nextToken());

        if (Objects.equals(messageCode, "UNROK")) {
            if (statusCode == 0) {
                return true;
            } else if (statusCode == 9999) {
                return false;
            } else {
                /* Server doesn't send other numbers; should be unreachable. */
                throw new UnknownUnregisterResponseException();
            }
        } else {
            /* Should be unreachable unless we are sending an unknown message, which is not. */
            throw new UnknownUnregisterResponseException();
        }
    }

    /**
     * Parses registration response received from the bootstrap server.
     *
     * @param response Response received from bootstrap server.
     * @return A list of peers received from bootstrap server, can be empty if this is the first.
     * @throws RegisterException If cannot register for some reason, this will throw.
     */
    static List<PeerInfo> parseRegisterResponse(String response) throws RegisterException {
        /*
         * <message length> REGOK <node count> <node 1 host> <node 1 port> <node 2 host> <node 2 port> ...
         * We trust the registry to send the correct response.
         */

        StringTokenizer tokenizer = new StringTokenizer(response);

        /* We don't need length, this is not C++; forget about it silently. */
        tokenizer.nextToken();

        String messageCode = tokenizer.nextToken();

        /* Even if we failed registering, we get a REGOK */
        if (messageCode.equals("REGOK")) {
            int nodeCount = Integer.parseInt(tokenizer.nextToken());

            switch (nodeCount) {
                case 9999:
                    /* Failed: Some error in the message we sent.
                    This shouldn't happen. TODO Server sends this when already registered. */
                    throw new IncorrectRegisterRequestException();
                case 9998:
                    /* Failed: Already registered to you. We have to unregister.
                    This happens if our app crashed. TODO Server sends this when error in message. */
                    throw new AlreadyRegisteredException();
                case 9997:
                    /* Failed: Registered to another user. TODO Server sends this when registry full.
                    This might happen only when running multiple peers in the same host. */
                    throw new UnavailableAddressException();
                case 9996:
                    /* Failed: Registry full. TODO Server sends ERROR, not REGOK.
                    If this happens, we give up. Try later. */
                    throw new RegistryFullException();
                default:
                    /* We have nodeCount nodes. */
                    List<PeerInfo> peerInfos = new ArrayList<>();
                    for (int i = 0; i < nodeCount; i++) {
                        /* Again we fully trust bootstrap server to send a correct reply. */
                        peerInfos.add(new PeerInfo(tokenizer.nextToken(), Integer.parseInt(tokenizer.nextToken())));
                    }

                    return peerInfos;
            }
        } else {
            /* Should be unreachable unless we are sending an unknown message, which is not. */
            throw new UnknownRegisterResponseException();
        }
    }

    /**
     * Registers with a bootstrap server and returns a list of registrants found in the registry.
     * It automatically finds the host where to start the ports.
     *
     * @param ownPort  65536 > Port number >= 1024 to register with the bootstrap server.
     * @param username Username to register with the bootstrap server.
     * @return The peers that have previously registered with the bootstrap server or null if cannot register.
     */
    abstract List<PeerInfo> register(String ownHost, int ownPort, String username)
            throws IOException, RegisterException;

    /**
     * Unregisters from a previously registered bootstrap server.
     *
     * @return Whether un-registration was successful.
     */
    abstract boolean unregister() throws IOException, UnknownUnregisterResponseException;

    static String generateRequestString(String ownHost, int ownPort, String username, boolean unregister) {
        String strOwnPort = String.valueOf(ownPort);

        int msgLength = 4 + (unregister ? 5 : 3) +
                ownHost.length() + strOwnPort.length() + username.length()
                + 4; // Spaces
        String strMsgLength = String.format("%04d", msgLength);

        return String.join(
                " ",
                strMsgLength, unregister ? "UNREG" : "REG", ownHost, strOwnPort, username
        );
    }

}
