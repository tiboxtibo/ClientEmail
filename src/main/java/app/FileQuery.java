package app;

import javax.json.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Classe Query -> serve per interrogare (non un database, in questo caso) i file json
 * per eliminare e salvare le email
 * */
public class FileQuery {

     //Il ReentrantReadWriteLock è un implementazione del readWriteLock e serve
     //per accedere in modo esclusivo alla lettura o scrittura di un file
    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /** Metodo che scrive la mailList in un file Json in modo da poterle salvare */
    public static void writeMailListJSON(List<Email> emailList, User user) throws FileNotFoundException {
        readWriteLock.writeLock().lock();//acquisizione del lock di write
        try {
            JsonArrayBuilder mailListBuilder = Json.createArrayBuilder();//builder per creare un array di oggetti json

            for (Email e : emailList) {

                JsonObjectBuilder mailBuilder = Json.createObjectBuilder();
                JsonArrayBuilder destsBuilder = Json.createArrayBuilder();

                //aggiungo tutti i destinatari ad un arrayJson
                for (int i = 0; i < e.destinatari.size(); i++) {
                    destsBuilder.add(e.destinatari.get(i).substring(1,e.destinatari.get(i).toString().length()-1));
                }
                //inserisco tutti gli elementi della mail all'interno di un oggetto json
                mailBuilder.add("id", e.id)
                        .add("mittente", e.mittente)
                        .add("destinatari", destsBuilder)
                        .add("oggetto", e.oggetto)
                        .add("testo", e.testo)
                        .add("data", e.data);

                //inserisco l'oggetto json all'interno dell'array json
                mailListBuilder.add(mailBuilder);
            }
            JsonArray mailJsonObj = mailListBuilder.build();//costruisco tale array
            //creo un nuovo file (o lo sovrascrivo) e lo salvo all'interno della cartella riservata all'utente
            //che differenzio in base all'id
            OutputStream os = new FileOutputStream(user.userFolder() + "/ricevute.json");

            JsonWriter jsonWriter = Json.createWriter(os);
            jsonWriter.writeArray(mailJsonObj);//per scrivere infine l'array json all'interno del file
            jsonWriter.close();
        } finally {
            readWriteLock.writeLock().unlock();//libero il lock
        }

    }


    /** Leggo il file json e restituisco la lista di mail, in base all'user passato */
    public static List<Email> readMailJSON(User user) throws IOException {

        List<Email> newMailList = new ArrayList<>();
        File file = new File(user.userFolder() + "/ricevute.json");

        readWriteLock.readLock().lock();//acquisisco il lock di read
        try {
            if(file.length()!= 0) {

                InputStream fis = new FileInputStream(file);//leggo il file
                JsonReader jsonReader = Json.createReader(fis);
                JsonArray jsonArray = jsonReader.readArray();//leggo l'array json

                fis.close();//qui possiamo già chiudere la risorsa di lettura -> JsonReader

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject mail = jsonArray.getJsonObject(i);
                    JsonArray destsArray = mail.getJsonArray("destinatari");
                    List<String> destinatari = new ArrayList<>();
                    for (JsonValue s : destsArray) {
                        destinatari.add(s.toString());
                    }
                    Email e = new Email(mail.getString("mittente"), destinatari, mail.getString("oggetto"), mail.getString("testo"), mail.getString("data"));
                    e.setId(mail.getInt("id"));
                    newMailList.add(e);
                    //e.printMailContent();
                }
                jsonReader.close();
            }
        } finally {
            readWriteLock.readLock().unlock();
        }

        return newMailList;
    }

    /**
     * Remove mail from json file
     *
     * @param user   the user
     * @param mailId the mail id to be removed
     * @return if deleted or not
     * @throws IOException the io exception
     */
    public static Boolean deleteMail(User user, int mailId) throws IOException {
        List<Email> emails = FileQuery.readMailJSON(user);
        for(int i = 0; i < emails.size(); i++){
            if(emails.get(i).getId() == mailId){
                emails.remove(i);
                FileQuery.writeMailListJSON(emails, user);
                return true;
            }
        }
        return false;
    }


    /**
     * Given mail and password return the user id.
     *
     * @param mail the mail
     * @param pwd  the pwd
     * @return the user id
     * @throws IOException the io exception
     */
    public static int getUserId(String mail, String pwd) throws IOException {
        int id = 0;
        readWriteLock.readLock().lock();
        try {
            File inputFile = new File("src/main/java/mails/users.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String currentLine;
            if (inputFile.exists())
                while ((currentLine = reader.readLine()) != null) {
                    String[] splittedLine = currentLine.split(",");
                    if (mail.equals(splittedLine[1]) && pwd.equals(splittedLine[2]))
                        id = Integer.parseInt(splittedLine[0]);
                }
            else System.out.println("File not exist!");
        } finally {
            readWriteLock.readLock().unlock();
        }
        return id;

    }

    /**
     * Given a mail return the user or false if not found.
     *
     * @param mail the mail
     * @return The user found or false if not
     * @throws IOException the io exception
     */
    public static Object getUserByMail(String mail) throws IOException {
        readWriteLock.readLock().lock();
        try {
            File inputFile = new File("src/main/java/mails/users.txt");
            User currUser = null;
            boolean found = false;
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String currentLine;
            if (inputFile.exists())
                while ((currentLine = reader.readLine()) != null) {
                    String[] splittedLine = currentLine.split(",");
                    if (mail.trim().equals(splittedLine[1].trim())){
                        currUser = new User(Integer.parseInt(splittedLine[0]), splittedLine[1], splittedLine[2]);
                        found = true;
                    }
                }
            else System.out.println("File not exist!");
            if(found)
                return currUser;
            else return false;
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

}


