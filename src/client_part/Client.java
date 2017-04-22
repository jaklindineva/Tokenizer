package client_part;

/**
 * @author Jaklin
 */
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class Client extends JFrame {

    private final static int EXIT = 0; //type of message send to server when exit button is clicked
    private final static int LOGIN = 1; //type of message send to server when login button is clicked
    private final static int REGISTER = 2; //type of message send to server when register new token button is clicked
    private final static int GET_CARD = 3; //type of message send to server when get card  by token button is clicked
    private boolean verifiedProfile = false; // whether the user has logged in successfully
    private boolean stopped = false;//whether the client has pressed exit

    //Log In
    private final JPanel logInJPanel = new JPanel(new GridLayout(3, 2, 15, 5)); //log in panel
    private final JTextField nameJTextField = new JTextField(); // text area for the name
    private final JPasswordField passwordJPasswordField = new JPasswordField(); // area for the password
    private final JButton logInJButton = new JButton("Log in"); // button to login
    private final JTextField greetingJTextField = new JTextField(); // message whether the user is logged in or has entered incorrect name and/or password

    //Register new token
    private final JPanel registerTokenJPanel = new JPanel(new GridLayout(3, 2, 15, 5)); // register new token panel
    private final JTextField cardNumberJTextField = new JTextField(); // text field for the card number
    private final JButton registerJButton = new JButton("Register card"); // button to register the new token
    private final JTextField recievedTokenJTextField = new JTextField(); // dispays the responce from server - token or error message

    //Get card by token
    private final JPanel getCardByTokenJPanel = new JPanel(new GridLayout(3, 2, 15, 5)); // get card by token panel
    private final JTextField tokenNumberJTextField = new JTextField(); // text field for the card number
    private final JButton getCardJButton = new JButton("Get card"); // button to get the card to the token
    private final JTextField recievedCardNumberJTextField = new JTextField(); // dispays the responce from server - card number or error message

    //exit
    private final JPanel exitJPanel = new JPanel(new GridLayout(2, 1, 5, 5)); // exit panel
    private final JButton exitJButton = new JButton("Exit"); // exit button 

    //connection to server
    private ObjectOutputStream output; // output stream to server
    private ObjectInputStream input; // input stream from server
    private String message = ""; // message from server
    private final String chatServer; // host server for this application
    private Socket client; // socket to communicate with server

    // initialize chatServer and set up GUI
    public Client(String host) {
        super("Tokenizer Client");
        chatServer = host; // set server to which this client connects
        setLayout(new GridLayout(4, 1, 10, 10));

        // add GUI components to the log in panel panel
        logInJPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Log In"));
        logInJPanel.add(new JLabel("Enter your name:"));
        logInJPanel.add(nameJTextField);
        logInJPanel.add(new JLabel("Enter your password:"));
        logInJPanel.add(passwordJPasswordField);

        logInJButton.addActionListener(new ActionListener() { // send message to server that the client is trying to log in
            public void actionPerformed(ActionEvent event) {
                String name = nameJTextField.getText().trim(); // the name of the user
                String password = passwordJPasswordField.getText().trim(); // the password of the user
                if (isValid(name) && isValid(password)) { //cheking if the name and password are valid - contain no white spaces
                    sendData(LOGIN + " " + name + " " + password); // send message to server that the client is trying to log in if the name and password dont't contain white spaces
                } else {
                    displayMessage1("passwords and names can't contain white spaces"); // shows that nthe name and the password can't contain white spaces if such were used
                }
            } // end method actionPerformed

            private boolean isValid(String text) { // checks for white spaces
                Pattern pattern = Pattern.compile("[^\\s]+");
                Matcher matcher = pattern.matcher(text);
                return matcher.matches();
            }
        }); // end anonymous inner class and end call to addActionListener
        logInJPanel.add(logInJButton);
        greetingJTextField.setVisible(false); // cannon modify the responce from the server
        logInJPanel.add(greetingJTextField);
        add(logInJPanel);

        // add GUI components to the register new token panel
        registerTokenJPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Register New Token"));
        registerTokenJPanel.add(new JLabel("Enter the card number:"));
        registerTokenJPanel.add(cardNumberJTextField);

        registerJButton.addActionListener(
                new ActionListener() { // send message to server when the user presses the register token button
            public void actionPerformed(ActionEvent event) {
                if (verifiedProfile == true) { // checks if the user is logged in 
                    String cardNumber = cardNumberJTextField.getText().trim();
                    if (isValidCardNumber(cardNumber)) { // checks if the card number is valid
                        sendData(REGISTER + " " + cardNumber); // sends data to the server 
                    } else {
                        displayMessage2("enter 16 digits starting with 3,4,5 ot 6"); // if the number is not valid
                    }
                } else {
                    displayMessage2("you must be logged in"); // if the user is not logged in
                }
            } // end method actionPerformed

            private boolean isValidCardNumber(String cardNumber) { // 16 digits starting with 3, 4, 5 or 6
                Pattern pattern = Pattern.compile("^[3456]\\d{15}$");
                Matcher matcher = pattern.matcher(cardNumber);
                return matcher.matches();
            }
        }); // end anonymous inner class and end call to addActionListener
        registerTokenJPanel.add(registerJButton);
        registerTokenJPanel.add(new JLabel(""));
        registerTokenJPanel.add(new JLabel("Token from server:"));
        recievedTokenJTextField.setEditable(false);
        registerTokenJPanel.add(recievedTokenJTextField);
        add(registerTokenJPanel);

        // add GUI components to the get card by token panel
        getCardByTokenJPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Cet Card Number By Token"));
        getCardByTokenJPanel.add(new JLabel("Enter the token number:"));
        getCardByTokenJPanel.add(tokenNumberJTextField);

        getCardJButton.addActionListener(
                new ActionListener() { // send message to server when get card button is pressed
            public void actionPerformed(ActionEvent event) {
                if (verifiedProfile == true) {
                    String token = tokenNumberJTextField.getText().trim(); // retrieves the token, entered by the user
                    if (isValidToken(token)) { // checks if the token is valid
                        sendData(GET_CARD + " " + token); // sends to the sesrver the message type and the token
                    } else {
                        displayMessage3("enter 16 digits"); // if not valid
                    }
                } else {
                    displayMessage3("you must be logged in"); // if user is not logged in
                }
            }

            private boolean isValidToken(String token) { // checks if the token is 16 digits
                Pattern pattern = Pattern.compile("^\\d{16}$");
                Matcher matcher = pattern.matcher(token);
                return matcher.matches();
            }
        }); // end anonymous inner class and end call to addActionListener
        getCardByTokenJPanel.add(getCardJButton);
        getCardByTokenJPanel.add(new JLabel(""));
        getCardByTokenJPanel.add(new JLabel("Card to the token:"));
        recievedCardNumberJTextField.setEditable(false);
        getCardByTokenJPanel.add(recievedCardNumberJTextField);
        add(getCardByTokenJPanel);

        //exit panel
        exitJPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Exit"));
        exitJButton.setSize(30, 20);
        exitJButton.addActionListener(new ActionListener() { // when the exit button is pressed
            @Override
            public void actionPerformed(ActionEvent e) {
                if (verifiedProfile == true) { 
                    sendData(EXIT + ""); // sends to the server that the user is closing the connection, if the user is logged in (and there is a connection)
                }
                stopped = true; 
                System.exit(0);
            }
        });
        exitJPanel.add(new JLabel("Thank you for using "));
        exitJPanel.add(exitJButton);
        exitJPanel.add(new JLabel(" our services"));
        add(exitJPanel);

        setSize(500, 450); // set size of window
        setVisible(true); // show window
    } // end Client constructor

    // connect to server and process messages from server
    public void runClient() {
        try // connect to server, get streams, process connection
        {
            connectToServer(); // create a Socket to make connection
            getStreams(); // get the input and output streams
            processConnection(); // process connection
        } // end try
        catch (EOFException eofException) {
            stopped = true; //eofException.printStackTrace();
        } // end catch
        catch (IOException ioException) {
            ioException.printStackTrace();
        } // end catch
        finally {
            closeConnection(); // close connection
        } // end finally
    } // end method runClient

    // connect to server
    private void connectToServer() throws IOException {
        // create Socket to make connection to server
        client = new Socket(InetAddress.getByName(chatServer), 12345);
    } // end method connectToServer

    // get streams to send and receive data
    private void getStreams() throws IOException {
        // set up output stream for objects
        output = new ObjectOutputStream(client.getOutputStream());
        output.flush(); // flush output buffer to send header information

        // set up input stream for objects
        input = new ObjectInputStream(client.getInputStream());
    } // end method getStreams

    // process connection with server
    private void processConnection() throws IOException {
        do // process messages sent from server
        {
            try // read message and display it
            {
                message = (String) input.readObject(); // read new message
                proccessResponce(message); // proccess the responce from server
            } // end try
            catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            } // end catch

        } while (stopped == false); // when exit button is pressed
    } // end method processConnection

    // close streams and socket
    private void closeConnection() {
        try {
            output.close(); // close output stream
            input.close(); // close input stream
            client.close(); // close socket
        } // end try
        catch (IOException ioException) {
            ioException.printStackTrace();
        } // end catch
    } // end method closeConnection

    //process responce from server
    private void proccessResponce(String message) {
        if (message.startsWith("1")) { // if the type of the message is 1 (log in)
            proccessResponceType1(message);
        }
        if (message.startsWith("2")) { // if the type of the message is 2 (register token)
            proccessResponceType2(message);
        }
        if (message.startsWith("3")) { // if the type of the message is 3 (get card by token)
            proccessResponceType3(message);
        }
    } // end method proccessResponce

    // send message to server
    private void sendData(String message) {
        try // send object to server
        {
            output.writeObject(message);
            output.flush(); // flush data to output
        } // end try
        catch (IOException ioException) {
            ioException.printStackTrace();
        } // end catch
    } // end method sendData

    // manipulates greetingJTextField in the event-dispatch thread
    private void displayMessage1(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() // updates greetingJTextField
            {
                greetingJTextField.setVisible(true); 
                greetingJTextField.setEditable(false);
                greetingJTextField.setText(messageToDisplay); 
            } // end method run
        } // end anonymous inner class
        ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage1

    // manipulates recievedTokenJTextField in the event-dispatch thread
    private void displayMessage2(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() // updates recievedTokenJTextField
            {
                recievedTokenJTextField.setText(messageToDisplay);
            } // end method run
        } // end anonymous inner class
        ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage2

    // manipulates recievedCardNumberJTextField in the event-dispatch thread
    private void displayMessage3(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
            public void run() // updates recievedCardNumberJTextField
            {
                recievedCardNumberJTextField.setText(messageToDisplay);
            } // end method run
        } // end anonymous inner class
        ); // end call to SwingUtilities.invokeLater
    } // end method displayMessage3

    //proccess a responce, when it starts whit '1' - log in
    private void proccessResponceType1(String message) {
        if (message.contains("Error")) { // the server has not found such user
            displayMessage1("Invalid name or password"); // displays message to the greeting text field
        } else {
            verifiedProfile = true; // the user has successfully logged in
            displayMessage1("Welcome"); // displays message to the greeting text field
        }
    } // end proccessResponceType1 method

    private void proccessResponceType2(String message) {
        displayMessage2(message.substring(1)); // displays message to the recieved Token TextField
    }

    private void proccessResponceType3(String message) {  // displays message to the recieved Card Number TextField
        displayMessage3(message.substring(1)); 
    }
} // end class Client

