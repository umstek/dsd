package lk.uom.cse14.dsd.bscom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

/**
 * TCP implementation of the RegistryCommunicator.
 */
public class TcpRegistryCommunicator extends RegistryCommunicator {

    /**
     * Creates an instance of TcpRegistryCommunicator to communicate with the bootstrap server.
     *
     * @param serverHost The address of the bootstrap server (registry).
     * @param serverPort The port of the bootstrap server (registry).
     */
    public TcpRegistryCommunicator(String serverHost, int serverPort) {
        super(serverHost, serverPort);
        this.setOwnHost("");
        this.setOwnPort(0);
        this.setUsername(null);
    }

    /**
     * Sends a string based request and accepts a reply using a one-time TCP socket.
     *
     * @param message The message as a string to be sent to the bootstrap server.
     * @return Reply received from the bootstrap server.
     * @throws IOException If cannot connect to the server, this will throw an IOException.
     */
    private String request(String message) throws IOException {
        /* Take this method out if needed elsewhere. */

        try (Socket clientSocket = new Socket(getServerHost(), getServerPort())) {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            out.println(message);
            char[] chars = new char[8192];
            int read = in.read(chars);
            return String.valueOf(chars, 0, read);
        }
    }

    @Override
    public List<PeerInfo> register(String ownHost, int ownPort, String username) throws IOException, RegisterException {
        if (ownHost == null || ownHost.isEmpty()
                || username == null || username.isEmpty()
                || ownPort < 1024 || ownPort > 65535) {
            return null;
        }

        /*
         * If username is null, that means we can register safely.
         * If at least username/port supplied is different, that means we are re-registering with a different identity.
         * Otherwise, we try unregistering first, and if successful, we try to register again.
         * If this fails, the function will return null, so a new username and ownPort has to be tried.
         */
        if (this.getUsername() == null ||
                (this.getOwnPort() != ownPort || !this.getOwnHost().equals(ownHost)) && !this.getUsername().equals(username)
                || unregister()) {

            String requestMessage = generateRequestString(ownHost, ownPort, username, false);
            String response = request(requestMessage);
            List<PeerInfo> peerInfos = parseRegisterResponse(response);

            this.setOwnHost(ownHost);
            this.setOwnPort(ownPort);
            this.setUsername(username);

            return peerInfos;
        }

        return null;
    }

    @Override
    public boolean unregister() throws IOException, UnknownUnregisterResponseException {
        /* Cannot register if we have not yet registered/have already unregistered. */
        if (this.getUsername() == null || this.getOwnPort() < 1024) {
            return false;
        }

        String requestMessage = generateRequestString(getOwnHost(), getOwnPort(), getUsername(), true);
        String response = request(requestMessage);
        boolean status = parseUnregisterResponse(response);

        this.setOwnHost("");
        this.setOwnPort(0);
        this.setUsername("");

        return status;
    }
}
