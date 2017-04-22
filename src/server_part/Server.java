
package server_part;

import javax.swing.JFrame;

/**
 *
 * @author Jaklin
 */
public class Server extends JFrame{
    private ServerPanel serverPanel;

    public Server(ServerPanel sserverPanel) {
        this.serverPanel = sserverPanel;
        add(this.serverPanel);
        setSize(550, 250); // set size of window
        setVisible(true);
    }
    
    public void run(){
        serverPanel.runServer();
    }
    
}
