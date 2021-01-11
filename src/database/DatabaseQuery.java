package albumcredibilityapplication.database;

import albumcredibilityapplication.core.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.Result;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import albumcredibilityapplication.core.*;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.mysql.cj.x.protobuf.MysqlxPrepare;
import org.apache.commons.codec.binary.Hex;
import org.languagetool.language.SouthAfricanEnglish;
import sun.java2d.pipe.SpanShapeRenderer;
import sun.security.util.Password;

public abstract class DatabaseQuery {
    private static Connection conn;
    private static String query;

    /**
     * Connects to the MySQL database that contains information about users, albums, artists and reviews.
     * returns true if successful. Password is randomly made up for Github.
     * @return
     */
    public static boolean setUpDatabaseConnection(){
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/album_credibility?serverTimezone=UTC"
                    , "root", "ElderScrolls145Buddy$");
        } catch (Exception e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    //These are all methods for retrieving user information from the database.

    /**
     * Gets the specified user's albums from the database (The names of the albums)
     * @param aUser
     * @return an arraylist of strings containing each album's name. This can be used to find both the
     * total number of albums and the names of all albums
     */
    public static ArrayList<String> getUserAlbumsNames(User aUser){

        ArrayList<String> albumNames = new ArrayList<>();
        query = "SELECT a.album_name FROM albums a INNER JOIN user_has_album uha ON" +
                " uha.album_id = a.album_id WHERE user_id = " + aUser.getUserID();
        try{
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while(rs.next()){
                String albumTitle = rs.getString(1);
                albumNames.add(albumTitle);
            }
            st.close();
        } catch (Exception e){
            System.out.print(e.toString());
        }
        return albumNames;
    }

    /**
     * Retrieves a list of all the user's ratings for every album they have rated
     * @param aUser
     * @return An ArrayList object of all the integer ratings of the user for all of their albums
     */
    public static ArrayList<Integer> getUserRatings(User aUser){

        ArrayList<Integer> albumRatings = new ArrayList<>();
        query = "SELECT uha.rating FROM albums a INNER JOIN user_has_album uha ON" +
                " uha.album_id = a.album_id WHERE user_id = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, aUser.getUserID());

            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                Integer rating = rs.getInt(1);
                albumRatings.add(rating);
            }
            pstmt.close();
        } catch (Exception e){
            System.out.println(e.toString());
        }
        return albumRatings;
    }

    /**
     * Retrieves the user's review for a given album name.
     * @param aUser
     * @param albumTitle
     * @return A String object containing the user's review
     */
    public static Map<Integer, String> getReviewIDAndContent(User aUser, String albumTitle){
        Integer reviewID;
        String theReview;
        HashMap IDAndContent = new HashMap();

        query = "SELECT r.review_id ,r.review_content FROM reviews r INNER JOIN albums a ON r.album_id " +
                "= a.album_id WHERE user_id = ? AND a.album_name = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, aUser.getUserID());
            pstmt.setString(2, albumTitle);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                reviewID = rs.getInt(1);
                theReview = rs.getString(2);
                IDAndContent.put(reviewID, theReview);
            }
            pstmt.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return IDAndContent;
    }

    /**
     * Retrieves the user's registration date.
     * @param aUser
     * @return SQL Date object with the date the user registered
     */
    public static Date getRegistrationDate(User aUser){
        query = "SELECT ul.date_registered FROM user_list ul WHERE user_id = ?";
        Date registrationDate = null;

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, aUser.getUserID());
            ResultSet rs = pstmt.executeQuery();

            rs.next();
            registrationDate = rs.getDate(1);

            pstmt.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return registrationDate;
    }

    /**
     * Retrieves the last date the user was online
     * @param aUser
     * @return SQL Date object with the last online date
     */
    public static Date getLastOnlineDate(User aUser){
        query = "SELECT ul.last_online FROM user_list ul WHERE user_id = ?";
        Date lastOnlineDate = null;

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, aUser.getUserID());
            ResultSet rs = pstmt.executeQuery();

            rs.next();
            lastOnlineDate = rs.getDate(1);
            pstmt.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return lastOnlineDate;
    }

    public static String retrieveUsername(User aUser){
        String username = null;
        query = "SELECT ul.username FROM user_list ul WHERE user_id = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, aUser.getUserID());
            ResultSet rs = pstmt.executeQuery();

            rs.next();
            username = rs.getString(1);
            pstmt.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return username;
    }

    /**
     * Retrieves the number of helpful votes and the number of unhelpful votes that the album has been
     * given by other users.
     * @param aReview
     * @return A length 2 int array for the number of helpful and non-helpful votes respectively
     */
    public static int[] getReviewVotes(Review aReview) {
        query = "SELECT r.helpful, r.not_helpful FROM reviews r WHERE review_id = ?";
        int[] votes = new int[2];

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, aReview.getReviewID());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                int count = 1;
                for (int i = 0; i < 2; i++){
                    votes[i] = rs.getInt(count);
                    count++;
                }
            }
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return votes;
    }

    /**
     * Checks a username to see if it is available. Two users can't have the same username
     * @param aUsername
     * @return true if the username is available and false if it isn't. Two users cannot have the same
     * username (regardless of character case).
     */
    public static boolean checkUsername(String aUsername){
        aUsername = aUsername.toLowerCase();
        query = "SELECT username FROM user_list";
        boolean isUsernameAvailable = true;

        try{
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()){
                String currentUsername = rs.getString(1);
                if (aUsername.equals(currentUsername.toLowerCase())){
                    isUsernameAvailable = false;
                    break;
                }
            }
            st.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return isUsernameAvailable;
    }

    /**
     *
     * @param emailAddress
     * @return true if the email is already registered and false otherwise
     */
    public static boolean checkEmail(String emailAddress){
        emailAddress = emailAddress.toLowerCase();
        query = "SELECT email_address FROM user_list";
        boolean isEmailInUse = false;

        try{
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()){
                String currentEmail = rs.getString(1);
                if (currentEmail.equals(emailAddress)){
                    isEmailInUse = true;
                    break;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return isEmailInUse;
    }

    /**
     * The primary means of adding a new user to the system. Takes user details from UI and attempts to register the
     * user. If the username is already in the database/the email address is already in then it is unsuccessful.
     * If these checks pass, the salt is hashed as well as the password and this hashed password is added the database
     * along with all other user information.
     * @param username
     * @param password
     * @param firstName
     * @param surname
     * @param dateOfBirth
     * @param emailAddress
     * @return true if the user was added to the database, otherwise false.
     */
    public static boolean createUser(String username, char[] password, String firstName,
                                     String surname, String dateOfBirth, String emailAddress){
        if (checkUsername(username) && !checkEmail(emailAddress)){
            byte[] salt = PasswordHash.generateSalt();
            Calendar aCalendar = Calendar.getInstance();
            DateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd");
            String today = sqlFormat.format(aCalendar.getTime());

            try{
                byte[] hashedPassword = PasswordHash.createHashedPassword(password, salt);
                String hexSalt = Hex.encodeHexString(salt);
                String hexPassword = Hex.encodeHexString(hashedPassword);
                query = "INSERT INTO user_list VALUES (NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username.toLowerCase());
                pstmt.setString(2, hexPassword);
                pstmt.setString(3, hexSalt);
                pstmt.setString(4, firstName);
                pstmt.setString(5, surname);
                pstmt.setString(6, dateOfBirth);
                pstmt.setString(7, emailAddress);
                pstmt.setString(8, today);
                pstmt.setString(9, today);
                pstmt.setDouble(10, 0.0);

                pstmt.execute();
                pstmt.close();

            } catch (Exception e){
                System.out.println("An exception has occurred");
                e.printStackTrace();
                return false;
            }
        } else if (!checkUsername(username)){
            System.out.println("This username is already in use");
            return false;
        } else if (checkEmail(emailAddress)){
            System.out.println("This email address is already registered to an account");
            return false;
        }
        return true;
    }

    /**
     * Checks the user's input credentials against those stored in the database. The method pulls the user's hashed
     * password and salt, then hashes the password using the same salt. Then compares the values.
     * @param username
     * @param password
     * @return true if the user has been authenticated successfully (username and password match), false otherwise
     */
    public static boolean authenticateUser(String username, char[] password){
        if (!checkUsername(username)){
            query = "SELECT ul.password, ul.salt, ul.user_id FROM user_list ul WHERE username = ?";

            try{
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, username.toLowerCase());
                ResultSet rs = pstmt.executeQuery();
                String hexPassword = "";
                String salt = "";
                while(rs.next()){
                    hexPassword = rs.getString(1);
                    salt = rs.getString(2);
                }
                byte[] byteSalt = PasswordHash.hexToByte(salt);
                byte[] hashedPassword = PasswordHash.createHashedPassword(password, byteSalt);
                String newHexPassword = PasswordHash.byteToHex(hashedPassword);

                if (hexPassword.equals(newHexPassword)){
                    System.out.println("User has been authenticated!");
                    pstmt.close();
                    return true;
                } else
                    return false;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        } else
            return false;
    }

    /**
     * This method is used by the GUI to get create the initial user object. It gets the user ID associated with the given
     * username and then creates a new User object with this information.
     * @param username
     * @return the user's ID
     */
    public static int retrieveUserID(String username){
        query = "SELECT user_id FROM user_list WHERE username = ?";
        int userID = 0;
        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            userID = rs.getInt(1);
            pstmt.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return userID;
    }

    /**
     *
     * @param albumName
     * @return an Album object containing all information about that album if the name is an album held in the database,
     * otherwise an error message is returned.
     */
    public static Album findAlbumInfo(String albumName){
        query = "SELECT a.album_name, a.avg_rating, a.release_date, a.genre, al.artist_name, a.parental_advisory, " +
                "a.album_language, a.album_id FROM albums a INNER JOIN artist_list al WHERE al.artist_id = a.artist_id " +
                "AND album_name = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, albumName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()){
                Album a = new Album();
                a.setAlbumName(albumName);
                a.setArtist(rs.getString(5));
                a.setAvgRating(rs.getDouble(2));
                a.setReleaseDate(rs.getDate(3));
                a.setAlbumGenre(rs.getString(4));
                a.setParentalAdvisory(rs.getString(6));
                a.setAlbumLanguage(rs.getString(7));
                a.setAlbumID(rs.getInt(8));
                pstmt.close();
                return a;
            } else {
                System.out.println("Album could not be found!");
                return null;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     *
     * @param albumName
     * @return true if the album does exist, false otherwise
     */
    public static boolean doesAlbumExist(String albumName){
        albumName = albumName.toLowerCase();
        query = "SELECT album_name FROM albums WHERE album_name = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, albumName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                pstmt.close();
                return true;
            } else
                pstmt.close();
                return false;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks to see if the user already has the album in their collection, so that they cannot add it twice or more
     * @param userID
     * @param albumID
     * @return true if the album is in the user's collection, false otherwise
     */
    public static boolean doesUserHaveAlbum(int userID, int albumID) {
        query = "SELECT uha.user_id FROM user_has_album uha WHERE uha.user_id = ? AND uha.album_id = ?";

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userID);
            pstmt.setInt(2, albumID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                pstmt.close();
                return true;
            } else
                pstmt.close();
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addAlbumToCollection(int userID, int albumID, int userRating) {
        query = "INSERT INTO user_has_album VALUES(?, ?, ?)";

        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userID);
            pstmt.setInt(2, albumID);
            pstmt.setInt(3, userRating);
            pstmt.execute();
            pstmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes the specified album from the user's collection, i.e. removing it from user_has_album table in the database.
     * @param userID
     * @param albumID
     * @return true if the album has been removed, false otherwise
     */
    public static boolean removeAlbumFromCollection(int userID, int albumID){
        query = "DELETE FROM user_has_album WHERE user_has_album.user_id = ? AND user_has_album.album_id = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userID);
            pstmt.setInt(2, albumID);
            pstmt.execute();
            pstmt.close();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean submitUserReview(String reviewContent, int user_id, int album_id){
        //TODO credibility score needs to be calculated rather than 0.00
        query = "INSERT INTO reviews VALUES (NULL, ?, 0.00, ?, ?, 0, 0)";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, reviewContent);
            pstmt.setInt(2, user_id);
            pstmt.setInt(3, album_id);
            pstmt.execute();
            pstmt.close();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Allows the user to change their existing rating for an album.
     * @param aUser
     * @param anAlbum
     * @param newRating
     * @return true if the rating has been changed, and false otherwise
     */
    public static boolean changeAlbumRating(User aUser, Album anAlbum, int newRating){
        query = "UPDATE user_has_album SET rating = ? WHERE album_id = ? AND user_id = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, newRating);
            pstmt.setInt(2, anAlbum.getAlbumID());
            pstmt.setInt(3, aUser.getUserID());
            pstmt.execute();
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This is called whenever a user adds, removes or updates their rating for an album. This is so we get an up-to-date
     * reflection of the current average of the album
     * @param anAlbum
     * @return a double value representing the new average rating for the album
     */
    public static double updateAvgRatingForAlbum(Album anAlbum){
        query = "SELECT rating FROM user_has_album WHERE album_id = ?";

        try{
           PreparedStatement pstmt = conn.prepareStatement(query);
           pstmt.setInt(1,anAlbum.getAlbumID());
           ResultSet rs = pstmt.executeQuery();

           int count = 0;
           double average = 0.0;

           while (rs.next()){
               count++;
               average = average + rs.getInt(1);
           }
           average = average/count;


           String query2 = "UPDATE albums SET avg_rating = ? WHERE album_id = ?";

           try{
               PreparedStatement pstmt2 = conn.prepareStatement(query2);
               pstmt2.setDouble(1, average);
               pstmt2.setInt(2, anAlbum.getAlbumID());
               pstmt2.execute();
           } catch (Exception e){
               e.printStackTrace();
           }
           return average;
        } catch (Exception e){
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Everytime a user logs-in, this method is called to update their last online to today's date. This is for credibility
     * calculation purposes.
     * @param aUser
     */
    public static void updateUserLastOnline(User aUser){
        Calendar aCalendar = Calendar.getInstance();
        DateFormat sqlFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = sqlFormat.format(aCalendar.getTime());

        query = "UPDATE user_list SET last_online = ? WHERE user_id = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, today);
            pstmt.setInt(2, aUser.getUserID());
            pstmt.execute();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Checks to see if a user has a review for this album before letting them put in another one.
     * @param aUser
     * @param anAlbum
     * @return true if the user has a review for the album already, false otherwise
     */
    public static boolean doesUserHaveReview(User aUser, Album anAlbum){
        query = "SELECT r.review_content FROM reviews r WHERE r.user_id = ? AND r.album_id = ?";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, aUser.getUserID());
            pstmt.setInt(2, anAlbum.getAlbumID());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()){
                return true;
            }
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    public static String[] getBestReviews(Album anAlbum){
        String[] bestReviews = new String[3];

        query = "SELECT r.review_content, r.credibility_score, ul.username FROM reviews r INNER JOIN albums a ON " +
                "r.album_id = a.album_id INNER JOIN user_list ul ON ul.user_id = r.user_id WHERE album_name = ? ORDER BY " +
                "r.credibility_score DESC";

        try{
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, anAlbum.getAlbumName());
            ResultSet rs = pstmt.executeQuery();
            int count = 0;

            while (count < 3){
                if (rs.next()){
                    bestReviews[count] = rs.getString(1);
                }
                count++;
            }
            return bestReviews;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

