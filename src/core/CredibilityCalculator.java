package albumcredibilityapplication.core;

import albumcredibilityapplication.database.DatabaseQuery;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.sql.Date;
import java.util.*;


    // Incomplete CredibilityCalculator class
public class CredibilityCalculator {

    private static final double userAlbumMultiplicationFactor = 0.30; // Not too important
    private static final double userRatingScoreMultiplicationFactor = 0.90; // More important
    private static final double userRegistrationDateMultiplicationFactor = 0.70;
    private static final double userLastOnlineDateMultiplicationFactor = 0.50;

    public static double totalUserCredibility(User aUser){
        // These first method calls are for a user's profile credibility, this will also need to take into account total
        // review score credibility.
        double userAlbumNumberScore = CredibilityCalculator.albumNumberScore(aUser);
        double userRatingScore = CredibilityCalculator.userRatingScore(aUser);
        double userRegistrationDateScore = CredibilityCalculator.userRegistrationScore(aUser);
        double lastOnlineScore = CredibilityCalculator.lastOnlineScore(aUser);

        return 0.0;
    }

    public static double totalReviewCredibility(Review aReview){
        double reviewVotesScore = CredibilityCalculator.reviewVotesScore(aReview);
        double reviewGrammarScore = CredibilityCalculator.reviewGrammarScore(aReview);



        return 0.0;
    }


    /**
     * calculates a credibility score based upon the number of albums that a user has in their collection.
     * These albums will be rated between 1 and 10 inclusive. The greater the number of albums, the more experience
     * a user presumably has and thus the more credible their review as they have more to compare to. Due to the nature
     * of people adding lots of albums over a short period of time, this will have a low weighting.
     * @param aUser
     * @return
     */
    public static double albumNumberScore(User aUser){
        double albumNumberCredibility = 0.0;
        int count = aUser.getAlbumNames().size();

        if(count > 21){
            albumNumberCredibility = 1.00;
        } else if (count > 16 && count <= 20){
            albumNumberCredibility = 0.80;
        } else if (count > 11 && count <=15){
            albumNumberCredibility = 0.60;
        } else if (count > 6 && count <= 10){
            albumNumberCredibility = 0.40;
        } else if (count > 0 && count <= 5) {
            albumNumberCredibility = 0.20;
        }
        return albumNumberCredibility;
    }

    /**
     * Calculates a credibility score based on the average rating of all the albums the user has reviewed.
     * Once all reviews have been summed, the mean is determined and if it is below 1 or above 9 it is presumed
     * that the user is intentionally rating everything either very high or very low. This has a lesser effect
     * at 2 and 8 and so on.
     * @param aUser
     * @return
     */
    public static double userRatingScore (User aUser){
        double userRatingCredibility;
        List<Integer> ratings;
        ratings = aUser.getAlbumRatings();
        double average = 0.0;

        for (int aRating : ratings){
            average += aRating;
        }
        average = average/ratings.size();
        System.out.println(average);

        if (average < 2 || average > 9){
            userRatingCredibility = 0.25;
        }
        else if (average < 3 || average > 8)
            userRatingCredibility = 0.50;
        else if (average < 4 || average > 7)
            userRatingCredibility = 0.75;
        else
            userRatingCredibility = 1.00;

        return userRatingCredibility;
    }

    /**
     * Users who have been a registered member for longer have a higher credibility score. This is to stop new
     * users from immediately having a significant impact on ratings when they first sign up. The method gets the
     * current date and the date they registered from the DB. It then converts these into ms, and then subtracts
     * the user's registration time from today's time. This means that newer users have a higher registration time
     * value. Therefore, when howLongBeenRegistered is calculated, newer users will have a smaller value and so
     * they are assigned a lower rating. Every 3 months the user gains more weighting score.
     * @param aUser
     * @return
     */
    public static double userRegistrationScore(User aUser){
        Date registrationDate = aUser.getRegistrationDate();
        java.util.Date today = new java.util.Date(System.currentTimeMillis());
        double userRegistrationCredibility;

        long registrationTime = registrationDate.getTime();
        long todayTime = today.getTime();
        long howLongBeenRegistered = todayTime - registrationTime;
        System.out.println(howLongBeenRegistered);

        if (howLongBeenRegistered < 7776000000L){
            userRegistrationCredibility = 0.20;
        }
        else if (howLongBeenRegistered < 15552000000L){
            userRegistrationCredibility = 0.40;
        }
        else if (howLongBeenRegistered < 23328000000L){
            userRegistrationCredibility = 0.60;
        }
        else if (howLongBeenRegistered < 31104000000L){
            userRegistrationCredibility = 0.80;
        } else{
            userRegistrationCredibility = 1.00;
        }


        return userRegistrationCredibility;
    }

    /**
     * Users who are more frequent visitors have a higher credibility. This is because more active users
     * should have more impact than those who never visit anymore. Thus, the method gets the user's last date
     * online and compares it to today's date. User's who have been online in the last 3 months have the
     * highest impact, and those active over 9 months ago have the lowest.
     * @param aUser
     * @return
     */
    public static double lastOnlineScore(User aUser){
        Date lastOnlineDate = DatabaseQuery.getLastOnlineDate(aUser);
        java.util.Date today = new java.util.Date(System.currentTimeMillis());
        double userLastOnlineCredibilityScore;

        long lastOnlineTime = lastOnlineDate.getTime();
        long todayTime = today.getTime();
        long howLongLastOnline = todayTime - lastOnlineTime;

        if (howLongLastOnline < 7776000000L){
            userLastOnlineCredibilityScore = 1.00;
        } else if (howLongLastOnline < 15552000000L){
            userLastOnlineCredibilityScore = 0.75;
        } else if (howLongLastOnline < 23328000000L){
            userLastOnlineCredibilityScore = 0.50;
        } else {
            userLastOnlineCredibilityScore = 0.25;
        }
        return userLastOnlineCredibilityScore;
    }

    /**
     * The credibility of a review is also determined by the number of users who have rated the review as
     * helpful vs the number of users who rated the review as not helpful. Those reviews that have more
     * users saying they are helpful than unhelpful are assigned higher scores.
     * @param aReview
     * @return
     */
    public static double reviewVotesScore (Review aReview){
        int[] votes = DatabaseQuery.getReviewVotes(aReview);
        double reviewVotesCredibilityScore;

        double ratioOfVotes = votes[0]/votes[1];

        if (ratioOfVotes >= 5.0){
            reviewVotesCredibilityScore = 1.00;
        } else if (ratioOfVotes >= 3.5){
            reviewVotesCredibilityScore = 0.75;
        } else if (ratioOfVotes >= 2.0){
            reviewVotesCredibilityScore = 0.50;
        } else if (ratioOfVotes >= 1.0){
            reviewVotesCredibilityScore = 0.25;
        } else
            reviewVotesCredibilityScore = 0.10;
        System.out.println(reviewVotesCredibilityScore);
        return reviewVotesCredibilityScore;
    }
    // printing out mistakes to user may be added later?

    /**
     * The perceived credibility of a review is affected by the correct use of grammar and punctuation. Therefore,
     * if a user's review has poor grammar/punctuation it will be assigned a lower credibility score.
     * @param aReview
     * @return a double representing the user's credibility score
     */
    public static double reviewGrammarScore (Review aReview){
        String theReview = aReview.getReviewContent();
        JLanguageTool langTool = new JLanguageTool(new BritishEnglish());
        int count = 0;
        double grammarCredibilityScore;

        // Words to ignore when determining grammar score
        for (Rule rule : langTool.getAllActiveRules()) {
            if (rule instanceof SpellingCheckRule) {
                ((SpellingCheckRule)rule).acceptPhrases(Arrays.asList("Radiohead", "radiohead"));
            }
        }

        try{
            List<RuleMatch> matches = langTool.check(theReview);
            count = matches.size();
            /*  This code may be used later to print the results of the errors to the user.
            for (RuleMatch match : matches) {
                // code here to print to the user their mistakes (see tests code)
            }
            */
            } catch (Exception e){
            e.printStackTrace();
        }
        if (count <= 3){
            grammarCredibilityScore = 1.00;
        } else if (count <= 6){
            grammarCredibilityScore = 0.75;
        } else if (count <= 9)
            grammarCredibilityScore = 0.50;
        else
            grammarCredibilityScore = 0.25;

        System.out.println(count);
        return grammarCredibilityScore;
    }

    /**
     * This method determines the overall credibility of a review using the StanfordCORE NLP library. It then returns
     * a credibility score value which is higher if the review is more balanced (slightly negative, neutral or slight positive)
     * Than if it is significantly at either end of the spectrum.
     * @param aReview
     * @return
     */
    public static double reviewSentimentScore(Review aReview){
        // Create new map to store occurrences of each sentiment
        HashMap<Integer, Integer> results = new HashMap<>();
        results.put(0, 0);
        results.put(1, 0);
        results.put(2, 0);
        results.put(3, 0);
        results.put(4, 0);


        String reviewContent = aReview.getReviewContent();
        System.out.println(reviewContent);


        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,parse,sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation annotation = new Annotation(reviewContent);
        pipeline.annotate(annotation);

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)){
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentimentScore = RNNCoreAnnotations.getPredictedClass(tree);
            System.out.println(sentimentScore);

            // gets each occurrence of a particular sentiment score
            for (Integer key : results.keySet()){
                if (key == sentimentScore){
                    Integer value = results.get(key);
                    int valuePrimitive = value;
                    valuePrimitive += 1;
                    results.put(key, valuePrimitive);
                }
            }
        }
        Integer totalSentiment = 0;

        for (Integer key : results.keySet()){
            System.out.println(key + " occurred " + results.get(key));
            Integer keyValue = results.get(key);
            if (keyValue > totalSentiment){
                totalSentiment = key;
            }
        }
        System.out.println(totalSentiment);

        if (totalSentiment == 2 || totalSentiment == 3 || totalSentiment == 4){
            return 1.0;
        } else
            return 0.5;
    }
}
