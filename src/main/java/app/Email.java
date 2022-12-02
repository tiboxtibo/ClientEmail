package app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The email object that will be sent and written to the json file
 */
public class Email extends Object implements Serializable {

    public int id;
    public String mittente;
    public List<String> destinatari;
    public String oggetto;
    public String testo;
    public String data;

    public Email(String mittente, List<String> destinatari, String oggetto, String testo, String data) {
        this.mittente = mittente;
        this.destinatari = destinatari;
        this.oggetto = oggetto;
        this.testo = testo;
        this.data = data;
    }

    public static Email stringToObj(String[] parts) {
        if (parts.length == 6) {
            List<String> dests = new ArrayList<>();
            String[] destSplit = parts[2].split("-");
            for (int i = 0; i < destSplit.length; i++) {
                dests.add(destSplit[0]);
            }
            Email e = new Email(parts[1], dests, parts[3], parts[4], parts[5]);
            e.setId(Integer.parseInt(parts[0]));
            return e;
        } else return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMittente() {
        return mittente;
    }

    public void setMittente(String mittente) {
        this.mittente = mittente;
    }

    public List<String> getDestinatari() {
        return destinatari;
    }

    public void setDestinatario(List<String> destinatario) {
        this.destinatari = destinatario;
    }

    public void addDestinatario(String destinatario) {
        this.destinatari.add(destinatario);
    }

    public String destinatariToString() {
        String result =  destinatari.get(0);
        for (int i = 1; i < destinatari.size(); i++) {
            result += "," + destinatari.get(i) ;
        }
        //return result.substring(0, result.length() - 1);
        return result;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String toStringMail() {  //  ',' as field delimiter and ';' as email delimiter
        return id + "," + mittente + "," + destinatariToString() + "," + oggetto + "," + testo + "," + data + ";\n";
    }

    public void printMailContent() {  //  ',' as field delimiter and ';' as email delimiter
        System.out.println("Id: " + id + "\n" + "Mittente: " + mittente + "\n" + "Destinatario: " + destinatariToString() + "\n" +
                "Oggetto: " + oggetto + "\n" + "Testo: " + testo + "\n" + "Data: " + data);
    }

}
