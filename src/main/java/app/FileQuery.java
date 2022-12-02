package app;

import javax.json.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class FileQuery {
    private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * Method that writes the mail list to a json file.
     *
     * @param emailList the email list
     * @param user      the user
     * @throws FileNotFoundException the file not found exception
     */
    public static void writeMailListJSON(List<Email> emailList, User user) throws FileNotFoundException {
        readWriteLock.writeLock().lock();
        try {
            JsonArrayBuilder mailListBuilder = Json.createArrayBuilder();

            for (Email e : emailList) {

                JsonObjectBuilder mailBuilder = Json.createObjectBuilder();
                JsonArrayBuilder destsBuilder = Json.createArrayBuilder();

                for (int i = 0; i < e.destinatari.size(); i++) {
                    destsBuilder.add(e.destinatari.get(i).substring(1,e.destinatari.get(i).toString().length()-1));
                }
                mailBuilder.add("id", e.id)
                        .add("mittente", e.mittente)
                        .add("destinatari", destsBuilder)
                        .add("oggetto", e.oggetto)
                        .add("testo", e.testo)
                        .add("data", e.data);

                mailListBuilder.add(mailBuilder);
            }
            JsonArray mailJsonObj = mailListBuilder.build();
            OutputStream os = new FileOutputStream(user.userFolder() + "/ricevute.json");

            JsonWriter jsonWriter = Json.createWriter(os);
            /**
             * We can get JsonWriter from JsonWriterFactory also
             * JsonWriterFactory factory = Json.createWriterFactory(null);
             * jsonWriter = factory.createWriter(os);
             */
            jsonWriter.writeArray(mailJsonObj);
            jsonWriter.close();
        } finally {
            readWriteLock.writeLock().unlock();
        }

    }

    /**
     * Read json file and return the mail list.
     *
     * @param user the user
     * @return the mail list
     * @throws IOException the io exception
     */
    public static List<Email> readMailJSON(User user) throws IOException {

        List<Email> newMailList = new ArrayList<>();
        File file = new File(user.userFolder() + "/ricevute.json");

        readWriteLock.readLock().lock();
        try {
            if(file.length()!= 0) {

                InputStream fis = new FileInputStream(file);
                JsonReader jsonReader = Json.createReader(fis);
                JsonArray jsonArray = jsonReader.readArray();
                //we can close IO resource and JsonReader now
                fis.close();

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


