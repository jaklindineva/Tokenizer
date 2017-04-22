package server_part;

/**
 * @author Jaklin
 */
import com.thoughtworks.xstream.XStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class ServerPanel extends JPanel {

    private final static String FILE_WITH_PEOPLE = "file_with_people"; // file form witch to initialize the list with users
    private final static String FILE_WITH_CARDS_AND_TOKENS = "file_with_cards_and_tokens"; // file in witch to write the cards and tokens when the server is shut down

    // panel with controling buttons
    private final JPanel toFilePanel = new JPanel(new GridLayout(1, 2, 5, 5));
    private JButton toFileSortedByCards; //saves the the <cardNumber - token> pairs in text file, sorted by the card number
    private JButton toFileSortedByTokens; //saves the the <cardNumber - token> pairs in text file, sorted by the token
    private JButton shutDownServer; //writes all current <cardNumber - token> pairs in xml file shuts down server

    private Hashtable<String, String> tokensAndCards; // token - card_number 
    private CopyOnWriteArrayList<Person> people; //users withe their names, passwords an rights

    //display what the server is doing
    private final JPanel InformationPlanel = new JPanel(new GridLayout(1, 1, 5, 5));
    private JTextArea displayArea; // display information to user
    
    //connection
    private ServerSocket server; // server socket
    private int counter = 1; // counter of number of connections

    // set up GUI
    public ServerPanel(XStream xstream) {
        this.people = parseXmlToList(xstream); // initialize the users 
        this.tokensAndCards = parseXmlToHashTable(xstream);

        setLayout(new GridLayout(2, 2, 10, 10));

        toFilePanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "to File"));
        toFileSortedByCards = new JButton("to File Sorted By Cards");
        toFileSortedByCards.addActionListener(new ActionListener() { // saves the the <cardNumber - token> pairs in text file, sorted by the card number
            @Override
            public void actionPerformed(ActionEvent e) {
                toFileSortedByCards(); // saves the the <cardNumber - token> pairs in text file, sorted by the card number
                displayMessage("a file with the cards and tokens sorted by the card numbers was created\n"); // displays to the usesr that a file is created
            } // end action performed

            private void toFileSortedByCards() { // saves the the <cardNumber - token> pairs in text file, sorted by the card number
                Path fileSortedByCards = Paths.get("sorted_by_cards.txt"); // creates the file
                try (BufferedWriter writer = Files.newBufferedWriter(fileSortedByCards, Charset.defaultCharset())){ // gets writer
                    StringBuilder sb = new StringBuilder();
                    sb.append("Card number        Card token\n"); // titels of the columns in the file
                    tokensAndCards.entrySet().stream().sorted((e1,e2) -> e1.getValue().compareTo(e2.getValue())) // sorts the entries by card number
                            .forEach(e -> sb.append(e.getValue()).append(" - ").append(e.getKey()).append('\n')); // appends the entries in a string builder
                    writer.write(sb.toString()); // writes to the created file
                }  catch (IOException ex) {
                    Logger.getLogger(ServerPanel.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }//end toFileSortedByCards methos
        }); // end anonimous class
        toFileSortedByTokens = new JButton("to File Sorted By Tokens");
        toFileSortedByTokens.addActionListener(new ActionListener() {  // saves the the <cardNumber - token> pairs in text file, sorted by the token
            @Override
            public void actionPerformed(ActionEvent e) {
                toFileSortedByTokens(); //saves the the <cardNumber - token> pairs in text file, sorted by the token
                displayMessage("a file with the cards and tokens sorted by the tokens was created\n"); // displays to the usesr that the file is created
            } // end action performed

            private void toFileSortedByTokens() { //saves the the <cardNumber - token> pairs in text file, sorted by the token
                Path fileSortedByTokens = Paths.get("sorted_by_tokens.txt"); // creates the file
                try (BufferedWriter writer = Files.newBufferedWriter(fileSortedByTokens, Charset.defaultCharset())){ // gets writer
                    StringBuilder sb = new StringBuilder();
                    sb.append("Card token         Card number \n");// titels of the columns in the file
                    tokensAndCards.entrySet().stream().sorted((e1,e2) -> e1.getKey().compareTo(e2.getKey())) // sorts the entries by token
                            .forEach(e -> sb.append(e.getKey()).append(" - ").append(e.getValue()).append('\n')); // appends the entries in a string builder
                    writer.write(sb.toString());
                }  catch (IOException ex) {
                    Logger.getLogger(ServerPanel.class.getName()).log(Level.SEVERE, null, ex);
                } 
            } //end toFileSortedByTokens method
        });
        toFilePanel.add(toFileSortedByCards);
        toFilePanel.add(toFileSortedByTokens);

        shutDownServer = new JButton("shut down");
        shutDownServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                XStream xstream = new XStream();
                String str = xstream.toXML(tokensAndCards); // creates xml string with data representing the hashtable with <token-card> pairs
                File file = new File(FILE_WITH_CARDS_AND_TOKENS); // creates a new file to save tha data into
                try {
                    file.createNewFile(); // new file
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file)); // gets writer
                    writer.write(str); // writes the xml string
                    writer.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.exit(0); // exiting the system
            }
        });
        toFilePanel.add(shutDownServer);
        add(toFilePanel);

        displayArea = new JTextArea(); // create displayArea
        InformationPlanel.add(new JScrollPane(displayArea), BorderLayout.CENTER);
        add(InformationPlanel);

        setSize(550, 250); // set size of window
        setVisible(true); // show window
    } // end Server constructor

    // set up and run server 
    public void runServer() {
        try // set up server to receive connections; process connections
        {
            server = new ServerSocket(12345, 100); // create ServerSocket
            ExecutorService service = Executors.newCachedThreadPool(); // creates an executors  service, using an cached thread pool
            while (true) {
                try {
                    displayMessage("Waiting for connection\n");
                    Socket socket = server.accept(); // allow server to accept connection            
                    displayMessage("Connection " + counter + " received from: "
                            + socket.getInetAddress().getHostName()); // shows the user that a new client has connected to the server
                    ClientTask client = new ClientTask(socket, socket.getInetAddress().getHostName()); // create a new client task
                    service.submit(client); // submit that tast to the executor service
                } // end try
                catch (EOFException eofException) {
                    displayMessage("\nServer terminated connection");
                }
            } // end while
        } // end try
        catch (IOException ioException) {
            ioException.printStackTrace();
        } // end catch
    } // end method runServer

    // manipulates displayArea in the event-dispatch thread
    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() // updates displayArea
            {
                displayArea.append(messageToDisplay); // append message
            } // end method run
        } // end anonymous inner class
        ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage

    private CopyOnWriteArrayList<Person> parseXmlToList(XStream xstream) { //initialize the users using the information in the given file
        File file = new File(FILE_WITH_PEOPLE);
        CopyOnWriteArrayList<Person> result = (CopyOnWriteArrayList<Person>) xstream.fromXML(file);
        return result;
    } // end  parseXmlToList method
    
    private Hashtable<String, String> parseXmlToHashTable(XStream xstream) { //initialize the users using the information in the given file
        File file = new File(FILE_WITH_CARDS_AND_TOKENS);
        Hashtable<String, String> result = (Hashtable<String, String>) xstream.fromXML(file);
        return result;
    } // end  parseXmlToList method

    //client task
    private class ClientTask implements Runnable {

        private Socket socket; // sochet for the connection
        private String name; // name of the user
        private String username;
        private String userRights = ""; // rights of the user
        private ObjectOutputStream output; // output stream to client
        private ObjectInputStream input; // input stream from client

        public ClientTask(Socket socket, String name) {
            this.socket = socket;
            try {
                input = new ObjectInputStream(socket.getInputStream());// get input stream form client
                output = new ObjectOutputStream(socket.getOutputStream()); // get output stream to client
            } catch (IOException ex) {
                Logger.getLogger(ServerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            try {
                processConnection(); // proccess the contection with the client
                closeConnection(); // closes the connection and the streams
            } catch (IOException ex) {
                Logger.getLogger(ServerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // process connection with client
        private void processConnection() throws IOException {
            String message = "";
            do // process messages sent from client
            {
                try // read message and proccess it 
                {
                    message = (String) input.readObject(); // read new message
                    processMessage(message); // proccess the message
                } // end try
                catch (ClassNotFoundException classNotFoundException) {
                    displayMessage("\nUnknown object type received\n");
                } // end catch

            } while (!message.startsWith("0")); // ends when the client pressess the exit button (whitch sends a message staring with 0)
        } // end method processConnection

        // close streams and socket
        private void closeConnection() {
            displayMessage(" Terminating connection\n");
            try {
                output.close(); // close output stream
                input.close(); // close input stream
                socket.close(); // close socket
            } // end try
            catch (IOException ioException) {
                ioException.printStackTrace();
            } // end catch
        } // end method closeConnection

        // send message to client
        private void sendData(String message) {
            try // send object to client
            {
                output.writeObject(message);
            } // end try
            catch (IOException ioException) {
                displayArea.append("\n" + name + " Error writing object\n");
            } // end catch
        } // end method sendData

        //proccess the message from the client
        private void processMessage(String message) {
            String[] parts = message.split(" ");// the first part is the type of the message
            if (message.startsWith("0")) { // when the client has pressed the exit button a message starting with 0 is send
                displayMessage("\n exited the system\n"); // display to the servers user
            }
            if (message.startsWith("1")) { // when the client has pressed log in button a message starting with 1 is send
                String username = parts[1]; // the client's username
                String password = parts[2]; // the client's password
                try {
                    logIn(username, password); // tries to log in the user if the name and password are correct
                } catch (VerifyError e) {
                    displayMessage("\nsomebody tried to login, invalid name or/and password\n"); // if no such users exists
                }
            }
            if (message.startsWith("2")) { // when the client has pressed register token button a message starting with 2 is send
                String cardNumber = parts[1]; // the card number
                try {
                    registerNewToken(cardNumber); // tries to reguster the card if it valid 
                } catch (VerifyError e) { 
                    displayMessage(username + " tried to register a new token, but has no rights\n"); // if the card is not valid
                }
            }
            if (message.startsWith("3")) { //  when the client has pressed the get card button a message starting with 3 is send
                String cardToken = parts[1];
                try {
                    getCard(cardToken); // tries to get the card for this token if it exists
                } catch (VerifyError e) {
                    displayMessage(username + " tried to get a card by token, but has no rights\n"); // if no such token exists
                }
            }
        } // end of processMessage method

        // log in user
        private void logIn(String name, String password) {
            String rights = getRights(name, password); // recieves the rights that the user has if the password and name are correct  ot "" if they are not
            if (!rights.equals("")) { // the user exists
                sendData("1 " + rights); //send the client the rights of the user
                username = name; // initialize
                userRights = rights;
                displayMessage(name + " logged in \n"); // dispays that the usesr has logged in
            } else {
                sendData("1 Error: no such user"); // if no such user exists
                throw new VerifyError("No such user"); // throws an error
            }
        } // end of logIn method

        //register a new token
        private void registerNewToken(String cardNumber) {
            if (userRights.contains("2")) {
                Tokenizer tokenizer = new Tokenizer(cardNumber); // initialize the tokenizer 
                if (tokenizer.isValidCardNumber()) { // checks if the card is valid 
                    String token;
                    do {
                        token = tokenizer.getToken(); // gets a token for the cars 
                    } while (unique(token) == false);  // until it is unique
                    tokensAndCards.put(token, cardNumber); // puts the ner token-card_number pair in the tokensAndCards hashtable
                    displayMessage(username + " registered new token for card " + cardNumber + "\n"); // shows that a new token has been registered
                    sendData("2 " + token); // sends the client the new token
                } else {
                    sendData("2 Error: invalid Card Number"); // if the card is not valid
                }
            } else {
                sendData("2 No rights"); // if the user doesn't have rights to register new tokens
                throw new VerifyError("No rights");
            }
        } // end registerNewToken method

        //gets the rights for an user
        private String getRights(String name, String password) {
            for (Person next : people) {
                if (next.getName().equals(name) && next.getPassword().equals(password)) { // tries to find an user with the recieved name and password
                    return next.getRights(); // if the user is found returns the rights
                }
            }
            return ""; // if no such user has been found
        } // emd getRights method

        //checks int the hashtable if the given token already exists
        private boolean unique(String token) {
            return !tokensAndCards.containsKey(token);
        }

        // cet card number by the token
        private void getCard(String cardToken) {
            if (userRights.contains("3")) { // check if the user has the rights t oget a card by a token
                if (tokensAndCards.containsKey(cardToken)) { // checks if the token exists 
                    String cardNumber = tokensAndCards.get(cardToken); // gets the card number represented by the token
                    sendData("3 " + cardNumber); // sends the client the card number 
                    displayMessage(username + " recieved the card number for token " + cardToken + "\n"); // shows that the message has been send
                } else { // if the token does not exist
                    sendData("3 No such token");
                }
            } else { // if the user has no rights
                sendData("3 No rights"); // send the client the message
                throw new VerifyError("No rights");
            }
        } // end getCard method

    }
} // end class Server
