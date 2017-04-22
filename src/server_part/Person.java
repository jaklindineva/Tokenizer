package server_part;

import java.io.Serializable;
/**
 * @author Jaklin
 */
public class Person implements Serializable{

    private String name;
    private String password;
    private String rights;

    public Person(String name, String password, String rights) {
        this.setName(name);
        this.password = password;
        this.rights = rights;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getRights() {
        return rights;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name;
        } else {
            this.name = "ANONYMOUS";
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    @Override
    public String toString() {
        return "Person{" + "name=" + name + ", password=" + password + ", rights=" + rights + '}';
    }
    
    
}
