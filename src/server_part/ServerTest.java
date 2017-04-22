package server_part;

// Test the Server application.
import com.thoughtworks.xstream.XStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class ServerTest{

    private final static String FILE_WITH_PEOPLE = "file_with_people"; // file from witch to initialize the list of users
    private final static String FILE_WITH_CARDS_AND_TOKENS = "file_with_cards_and_tokens"; // file from witch to initialize the list of cards and tokens

    public static void main(String args[]) {
        XStream xstream = new XStream(); // creating an xstream
        createXmlWithPeople(xstream); // creating an xml file with the users names and passwords
        createXmlWithCardsAndTokens(xstream); // creating an xml file with registered pairs <token - card>

        ServerPanel serverPanel = new ServerPanel(xstream); // create server panel
        Server application = new Server(serverPanel); // create server - JFrame with server panel
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.run(); // run server application
    } // end main

    // creating an xml file with the users names and passwords
    private static File createXmlWithPeople(XStream xstream) {
        Person p1 = new Person("jaki", "81107", "23");
        Person p2 = new Person("lili", "81108", "2");
        Person p3 = new Person("mimi", "81109", "3");
        xstream.alias("person", Person.class);
        CopyOnWriteArrayList<Person> people = new CopyOnWriteArrayList<>();
        people.add(p1);
        people.add(p2);
        people.add(p3);
        String str = xstream.toXML(people); // creating an xml string representing the data in the people list

        File file = new File(FILE_WITH_PEOPLE); // creating a file to write the xml string 
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file)); // writer to write in the created file
            writer.write(str); // write the xml string
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return file;
    }//end createXmlWithPeople method 

    private static void createXmlWithCardsAndTokens(XStream xstream) {
        Hashtable<String, String> cardsAndTokens = new Hashtable<String, String>();
        cardsAndTokens.put("7854587956669991", "4563960122019991 ");
        cardsAndTokens.put("6345960221019991", "1930455893249991 ");
        cardsAndTokens.put("6395460221019199", "9907707112659199 ");
        String str = xstream.toXML(cardsAndTokens); // creating an xml string representing the data in the hashtablet

        File file = new File(FILE_WITH_CARDS_AND_TOKENS); // creating a file to write the xml string 
        try {
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file)); // writer to write in the created file
            writer.write(str); // write the xml string
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    } //end createXmlWithCardsAndTokens method 
    
} // end class ServerTest

