package albumcredibilityapplication.database;

import albumcredibilityapplication.core.CredibilityCalculator;
import albumcredibilityapplication.core.*;
import albumcredibilityapplication.view.UserInterfaceHelperMethods;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.RuleMatch;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
import java.sql.Date;

/* This is my main file which was used to test my final year project. This allowed me to continously
   test functionality was still intact when a new feature was added. If I were to do this again, I would
   have tests separated into more test classes.
 */

public class DatabaseQueryTest {
    User aUser;
    User aUser2;
    User aUser3;
    Review aReview;
    Review aReview2;

    @Before
    public void init(){
        DatabaseQuery.setUpDatabaseConnection();
        aUser = new User(1);
        aUser2 = new User(10);
        aUser3 = new User(4);

        aReview = new Review(aUser, "In Rainbows");
        aReview2 = new Review(aUser2, "The Slow Rush");
    }

    // Is the database set up properly?
    @Test
    public void setUpDatabaseConnectionTest(){
        assert DatabaseQuery.setUpDatabaseConnection() : "Connection was not established";
    }

    // Are the user's albums retrieved successfully?
    @Test
    public void getUserAlbumsTest(){
        System.out.println(aUser.getAlbumNames());
        assert !aUser.getAlbumNames().isEmpty() : "The user has no albums when they should";
    }

    // Are the user's ratings retrieved successfully?
    @Test
    public void retrieveUserAlbumsRatingsTest(){
        System.out.println(aUser.getAlbumRatings());
        assert !aUser.getAlbumRatings().isEmpty() : "The user has no ratings when they should";
    }


    // Is the user's review retrieved successfully?
    @Test
    public void retrieveReviewTest(){
        String theReview = aReview.getReviewContent();
        System.out.println(theReview);
        assert theReview != null : "The user should have a review for this album";
    }

    @Test
    public void retrieveUserRegistrationDateTest(){
        Date theDate = aUser.getRegistrationDate();
        System.out.println(theDate);
        assert theDate != null : "The user should have a date";
    }


    // Is the user's last online date retrieved successfully?
    @Test
    public void retrieveLastOnlineDateTest(){
        Date lastOnlineDate = DatabaseQuery.getLastOnlineDate(aUser);
        System.out.println(lastOnlineDate);
        assert lastOnlineDate != null : "The user should have a last online date retrieved";
    }

    // Is the user's username retrieved successfully?
    @Test
    public void retrieveUsernameTest(){
        System.out.println(aUser.getUsername());
        assert aUser.getUsername() != null;
    }

    // CREDIBILITY TEST METHODS ARE BEYOND THIS POINT

    // has a correct credibility score been calculated for the number of albums the user has rated?
    @Test
    public void getAlbumNumberScoreTest(){
        System.out.println(DatabaseQuery.getUserAlbumsNames(aUser));
        System.out.println(CredibilityCalculator.albumNumberScore(aUser));
        assert CredibilityCalculator.albumNumberScore(aUser) != 0.00 : "The user should have been given a score";
    }

    // has a correct credibility score been calculated for the average rating of the user for their albums?
    @Test
    public void getRatingScoreTest(){
        System.out.println(DatabaseQuery.getUserRatings(aUser));
        System.out.println(CredibilityCalculator.userRatingScore(aUser));
        assert CredibilityCalculator.userRatingScore(aUser) != 0.00 : "The user should have been given a score";
    }

    // is the user generated a credibility score based on when they registered?
    @Test
    public void getRegistrationScoreTest(){
        double score = CredibilityCalculator.userRegistrationScore(aUser);
        System.out.println("This user has been a member for over a year (long time)");
        System.out.println(score);
        assert score != 0.00 : "The user should have been given a score";
        score = CredibilityCalculator.userRegistrationScore(aUser2);
        System.out.println("This member is very new at only a week or so");
        System.out.println(score);
        assert score != 0.00 : "The user should have been given a score";
    }

    // Tests to ensure different registration dates result in different credibility scores
    @Test
    public void differentRegistrationScoresTest(){
        User aUser2 = new User(2);
        User aUser3 = new User(3);
        double score = CredibilityCalculator.userRegistrationScore(aUser);
        System.out.println(score);
        assert score == 1.0 : "User 1 has been assigned an incorrect score";
        score = CredibilityCalculator.userRegistrationScore(aUser2);
        System.out.println(score);
        assert score == 0.60 : "User 2 has been assigned an incorrect score";
        score = CredibilityCalculator.userRegistrationScore(aUser3);
        System.out.println(score);
        assert score == 0.60 : "User 3 has been assigned an incorrect score";
    }

    // Test to see if the last online score is calculated correctly
    @Test
    public void getLastOnlineScoreTest(){
        double score = CredibilityCalculator.lastOnlineScore(aUser);
        System.out.println("This user was online very recently");
        System.out.println(score);
        assert score != 0.0;
        score = CredibilityCalculator.lastOnlineScore(aUser3);
        System.out.println("This user has not been online for a long time");
        System.out.println(score);
        assert score != 0.0;
    }

    @Test
    public void differentLastOnlineTest(){
        User aUser2 = new User(3);
        User aUser3 = new User(4);

        double onlineCredibilityScore;

        onlineCredibilityScore = CredibilityCalculator.lastOnlineScore(aUser);
        System.out.println(onlineCredibilityScore);
        assert onlineCredibilityScore == 1.00 : "User1 has an incorrect credibility score";

        onlineCredibilityScore = CredibilityCalculator.lastOnlineScore(aUser2);
        System.out.println(onlineCredibilityScore);
        assert onlineCredibilityScore == 0.75 : "User2 has an incorrect credibility score";

        onlineCredibilityScore = CredibilityCalculator.lastOnlineScore(aUser3);
        System.out.println(onlineCredibilityScore);
        assert onlineCredibilityScore == 0.25 : "User3 has an incorrect credibility score";
    }
    @Test
    public void reviewVotesTest(){
        int[] votes = DatabaseQuery.getReviewVotes(aReview);
        assert votes[0] == 5 && votes[1] == 1 : "The votes have not been retrieved correctly";
    }

    @Test
    public void getReviewVotesScoreTest(){
        double score = CredibilityCalculator.reviewVotesScore(aReview);
        assert score == 1.0 : "The user has been assigned an incorrect score for their review";
    }
    @Test
    public void grammarScoreTest(){
        double review1Score = CredibilityCalculator.reviewGrammarScore(aReview);
        System.out.println(aReview.getReviewContent());
        System.out.println(aReview2.getReviewContent());
        System.out.println(review1Score);
        double review2Score = CredibilityCalculator.reviewGrammarScore(aReview2);
        System.out.println(review2Score);
        assert review1Score == 1.00 : "The user has been assigned an incorrect score";
        assert review2Score == 0.75 : "The user has been assigned an incorrect score";
    }
    @Test
    public void wordCountTest(){
        //Review theReview = new Review(1);
        //Review
        //int numberOfWords = theReview.getReviewWordCount();
        // assert numberOfWords ==
    }
    // Checking if a username is available
    @Test
    public void checkUsernameTest(){
        String username1 = "Orritius";
        String username2 = "TectriQ";

        assert DatabaseQuery.checkUsername(username1) == false : "Username Orritius should be taken";
        assert DatabaseQuery.checkUsername(username2) == true : "This username should be avaialable";
    }
    // Checking if an email address is already in use
    @Test
    public void checkEmailTest(){
        String email1 = "orritius@gmail.com";
        String email2 = "incorrectemail@gmail.com";
        assert DatabaseQuery.checkEmail(email1) == true : "email orritius@gmail.com should be in use already";
        assert DatabaseQuery.checkEmail(email2) == false : "email incorrectemail@gmail.com should not be in use already";
    }
    // Check if the email is in a valid format
    @Test
    public void checkEmailValidityTest(){
     String email1 = "Orritius@gmail.com";
     String email2 = "invalidemail";
     char[] email1Array = email1.toCharArray();
     char[] email2Array = email2.toCharArray();

     assert UserInterfaceHelperMethods.checkEmailValid(email1Array) : "This email is valid";
     assert !UserInterfaceHelperMethods.checkEmailValid(email2Array) : "This email is not valid";
    }

    // A test to check if user information is correctly entered into the SQL database.
    @Test
    public void checkUserRegistration(){
        char[] password = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};

        assert DatabaseQuery.createUser("MusicMaster123", password, "John", "Lennon",
                "1960-07-11", "lenny@gmail.com") : "The user has not been inserted into the database";
    }
    // Is the user authenticated successfully?
    @Test
    public void authenticationTest(){
        char[] password = {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'};
        boolean wasSuccessful = DatabaseQuery.authenticateUser("Musicmaster123", password);
        assert wasSuccessful : "The user should have been authenticated. It is the same username and password";
    }
    @Test
    public void albumSearchTest(){
        String albumName = "Kid A";
        Album a = DatabaseQuery.findAlbumInfo(albumName);
        assert a != null : "The album information should have been found";
    }
    @Test
    public void retriveUserIDTest(){
        String username = "Orritius";
        int id = DatabaseQuery.retrieveUserID(username);
        System.out.println(id);
    }
    @Test
    public void doPasswordsMatch(){
        char[] password1 = {'P', 'a', 's', 's', 'w', 'o', 'r', 'd'};
        char[] password2 = {'P', 'a', 's', 's', 'w', 'o', 'r', 'd'};

        assert UserInterfaceHelperMethods.doPasswordsMatch(password1, password2) : "The passwords do match";
    }
    @Test
    public void doesAlbumExistTest(){
        assert DatabaseQuery.doesAlbumExist("In Rainbows") : "This album is in the db";
        assert !DatabaseQuery.doesAlbumExist("At Folsom Prison") : "This is not in the db";
    }
    @Test
    public void doesUserHaveAlbum(){
        assert DatabaseQuery.doesUserHaveAlbum(1, 10) : "The user does have this album";
        assert !DatabaseQuery.doesUserHaveAlbum(1, 2) : "The user does not have this album";
    }
    @Test
    public void reviewSentimentCredibilityScoreTest(){
        assert CredibilityCalculator.reviewSentimentScore(aReview) == 1.00 : "There is an incorrect credibility score";
    }












    // TESTING OF CONVERSION BETWEEN HEX AND BYTE
    @Test
    public void hexAndByte(){
        // Returns a hexadecimal representation of each byte in order in the byte array
        byte[] bytes = {122, -45, 43, 50, 110, -56};
        String hexString = PasswordHash.byteToHex(bytes);
        System.out.println(hexString);

        // Returns a byte array containing the hexadecimal string converted
        byte[] bytesRetrieved = PasswordHash.hexToByte(hexString);
        for (byte b : bytesRetrieved){
            System.out.println(b);
        }

    }
   @Test
    public void passwordCreationTest(){
        String plaintextPassword = "Password";
        try{
            byte[] salt = PasswordHash.generateSalt();
            byte[] passwordInByte = PasswordHash.createHashedPassword(plaintextPassword.toCharArray(), salt);
            String hexPassword = PasswordHash.byteToHex(passwordInByte);
            System.out.println(hexPassword);

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    //TESTING OF CORRECT DATE FORMAT
    @Test
    public void dateTest(){
        Calendar myCalendar = Calendar.getInstance();
        DateFormat today = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(today.format(myCalendar.getTime()));
    }




    // TESTING OF JLANGUAGETOOL. This is for personal testing to test the API and capabilities of checker.
    @Test
    public void JLanguageToolTest1(){
        JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
        try{
            List<RuleMatch> matches = langTool.check("This sentence does not have any errors.");
            for (RuleMatch match : matches){
                System.out.println("Potential errors at characters " + match.getFromPos() + "-" +
                        match.getToPos() + ": " + match.getMessage());
                System.out.println("Suggested correction(s): " + match.getSuggestedReplacements());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void JLanguageToolSpellingErrorTest(){
        JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
        try{
            List<RuleMatch> matches = langTool.check("This senence does conain errors.");
            for (RuleMatch match : matches){
                System.out.println("Potential errors at characters " + match.getFromPos() + "-" +
                        match.getToPos() + ": " + match.getMessage());
                System.out.println("Suggested correction(s): " + match.getSuggestedReplacements());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void JLanguageToolPunctuationErrorTest(){
        JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
        try{
            List<RuleMatch> matches = langTool.check("Contains,Punctuation.Errors in this text");
            for (RuleMatch match : matches){
                System.out.println("Potential errors at characters " + match.getFromPos() + "-" +
                        match.getToPos() + ": " + match.getMessage());
                System.out.println("Suggested correction(s): " + match.getSuggestedReplacements());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    /* You're to your is detected correctly. There, their, and they're doesn't appear to be. Whose and
    who's seems to be successful. Could of is detected.
    */
    @Test
    public void JLanguageToolIncorrectSpellingOfWordTest(){
        JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
        try{
            List<RuleMatch> matches = langTool.check("I could of done this. You're car needs a wash " +
                    "There going to the shops.lower case start of sentence.");
            for (RuleMatch match : matches){
                System.out.println("Potential errors at characters " + match.getFromPos() + "-" +
                        match.getToPos() + ": " + match.getMessage());
                System.out.println("Suggested correction(s): " + match.getSuggestedReplacements());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    // Missing apostrophes are found
    @Test
    public void JLanguageToolApostropheMissingTest(){
        JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
        try{
            List<RuleMatch> matches = langTool.check("Didnt I say i couldnt do it.");
            for (RuleMatch match : matches){
                System.out.println("Potential errors at characters " + match.getFromPos() + "-" +
                        match.getToPos() + ": " + match.getMessage());
                System.out.println("Suggested correction(s): " + match.getSuggestedReplacements());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    @Test
    public void reviewGrammarTest(){
        JLanguageTool langTool = new JLanguageTool(new BritishEnglish());

        try{
            List<RuleMatch> matches = langTool.check("This album isnt that great. In fact it's one of the worst albums I've " +
                    "heard in recent years. You're stupid if you think this is anything more than uter rubbis.");
            for (RuleMatch match : matches){
                System.out.println("Potential errors at characters " + match.getFromPos() + "-" +
                        match.getToPos() + ": " + match.getMessage());
                System.out.println("Suggested correction(s): " + match.getSuggestedReplacements());
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // TESTING OF STANFORD CORENLP
    @Test
    public void testStanfordNLP(){
        String text = "Abbey Road is an amazing album. It's the finest creation of the Beatles and a huge success.";

        PrintWriter out;
        out = new PrintWriter(System.out);

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation annotation = new Annotation(text);
        pipeline.annotate(annotation);
        pipeline.prettyPrint(annotation, out);
        out.println(annotation.toShorterString());
        // We have now successfully created an object to determine sentiment

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)){
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            System.out.println(sentiment);
            String sentimentString = "";
            switch (sentiment) {
                case 0:
                    sentimentString = "Very Negative";
                    System.out.println(sentimentString);
                    break;
                case 1:
                    sentimentString = "Negative";
                    System.out.println(sentimentString);
                    break;
                case 2:
                    sentimentString = "Neutral";
                    System.out.println(sentimentString);
                    break;
                case 3:
                    sentimentString = "Positive";
                    System.out.println(sentimentString);
                    break;
                case 4:
                    sentimentString = "Very Positive";
                    System.out.println(sentimentString);
                    break;
            }
        }
    }
    @Test
    public void doTest(){
        // Java always rounds down
        double x = 4.1;
        System.out.println((int) x);
    }
    @Test
    public void ternaryTest(){
        for (int i = 1; i < 101; i++){
            String result = "";
            if (i % 3 == 0){
                result = result + "Fizz";
            } if (i % 5 == 0){
                result = result + "Buzz";
            } if (result.isEmpty()){
                System.out.println(i);
            } else
                System.out.println(result);
        }
    }
}
