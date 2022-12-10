package app;

import java.net.Socket;

/**
 * Creo una coppia socket-user
 * */
public class PairSocketUser {
    public Socket socket;
    public User user;

    public PairSocketUser(Socket socket, User user) {
        this.socket = socket;
        this.user = user;
    }
}