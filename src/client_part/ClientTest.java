package client_part;

import javax.swing.JFrame;

/**
 * @author Jaklin
 */
public class ClientTest {

    public static void main(String args[]) {
        Client application; // declare client application
        application = new Client("127.0.0.1"); // connect to localhost
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.runClient(); // run client application
    } // end main
} // end class ClientTest
