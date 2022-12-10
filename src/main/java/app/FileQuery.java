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
                    destsBuilder.add(e.destinatari.get(i).substring(0,e.destinatari.get(i).toString().length()));

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
            //TODO capire bene il lock
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

                fis.close();//qui possiamo già chiudere la risorsa di lettura del file

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject mail = jsonArray.getJsonObject(i);
                    JsonArray destsArray = mail.getJsonArray("destinatari");
                    List<String> destinatari = new ArrayList<>();
                    for (JsonValue s : destsArray) {//scompongo l'array json di destinatari
                        destinatari.add(s.toString());
                    }
                    //salvo tutto dentro Email e
                    Email e = new Email(mail.getString("mittente"), destinatari, mail.getString("oggetto"), mail.getString("testo"), mail.getString("data"));
                    e.setId(mail.getInt("id"));//setto l'id della mail
                    newMailList.add(e);
                    //e.printMailContent();
                }
                jsonReader.close();
            }
        } finally {
            readWriteLock.readLock().unlock();//rilascio il lock
        }

        return newMailList;
    }


    /** Rimuovo una mail dal file json in base al mailId e all'user*/
    public static Boolean deleteMail(User user, int mailId) throws IOException {
        List<Email> emails = FileQuery.readMailJSON(user);//leggo la mail
        for(int i = 0; i < emails.size(); i++){
            if(emails.get(i).getId() == mailId){
                emails.remove(i);//rimuovo la mail
                FileQuery.writeMailListJSON(emails, user);//riscrivo il file json senza la mail eliminata
                return true;
            }
        }
        return false;
    }


    /** Data una mail (login) e la password ritorno l'user id */
    public static int getUserId(String mail, String pwd) throws IOException {
        int id = 0;
        readWriteLock.readLock().lock();
        try {
            File inputFile = new File("src/main/java/mails/users.txt");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));//leggo il file degli user
            String currentLine;
            if (inputFile.exists())
                while ((currentLine = reader.readLine()) != null) {
                    String[] splittedLine = currentLine.split(",");
                    if (mail.equals(splittedLine[1]) && pwd.equals(splittedLine[2]))//se trovo la mail e la password
                        id = Integer.parseInt(splittedLine[0]);//setto l'id
                }
            else System.out.println("File Inesistente");
        } finally {
            readWriteLock.readLock().unlock();
        }
        return id;

    }


    /** Data una mail restituisco l'user (ovvero id,mail,password) -> false se non lo trovo */
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
                    if (mail.trim().equals(splittedLine[1].trim())){//se trovo la mail nel file degli user.txt
                        //restituisco id,mail,user
                        currUser = new User(Integer.parseInt(splittedLine[0]), splittedLine[1], splittedLine[2]);
                        found = true;
                    }
                }
            else System.out.println("File Inesistente");
            if(found)
                return currUser;
            else return false;
        } finally {
            readWriteLock.readLock().unlock();
        }

    }

}


