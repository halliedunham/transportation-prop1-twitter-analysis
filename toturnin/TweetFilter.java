import java.util.*;
import java.io.File;
import jxl.*;

public class TweetFilter
{
   private ArrayList<Tweet> proTweets;
   private ArrayList<Tweet> againstTweets;
   
   public TweetFilter(Sheet sh)
   {
      //initialize list for tweets
      proTweets=new ArrayList<Tweet>();
      againstTweets=new ArrayList<Tweet>();
      //read excel sheet and fill lists with tweets
      separate(sh);
   }
   
   public ArrayList<Tweet> getPro()
   {
      return proTweets;
   }
   
   public ArrayList<Tweet> getAgainst()
   {
      return againstTweets;
   }
   
   public int proSize()
   {
      return proTweets.size();
   }
   
   public int againstSize()
   {
      return againstTweets.size();
   }
   
   public int totalTweets()
   {
      return proSize()+againstSize();
   }
   
   private void separate(Sheet sh)
   {
      Tweet tw;
      String text;
      boolean isRT;
      //loop through rows of excel sheet as long as there is data in row
      for(int row=1; row<sh.getRows()&&!sh.getCell(0,row).getContents().equals(""); row++)
      {
         isRT=false;
         String RTfrom="";
         //set text of tweet, text is in collumn 10
         text=sh.getCell(10,row).getContents().toLowerCase();
         if (text.contains("rt @"))//if tweet is a retweet
         {
            isRT=true;
            int RTindex=text.indexOf("rt @");
            //set the retweeter's personal addition which comes before RT (includes space after)
            String addition=text.substring(0,RTindex);
            //loop through chars of text after "RT @"
            for(int c=RTindex+3; c<text.length(); c++)
            {
               //the first space after "RT @" marks the beginning of the text of the tweet
               if (text.substring(RTindex).charAt(c-RTindex)==(' '))
               {
                  //set who is being retweeted (@ is 3 chars after R, and handle ends with the space at index c)
                  RTfrom=text.substring(RTindex+3,c-1);
                  if(text.substring(c+1).length()>=20)
                  {
                     //set text to the first 20 chars after the space if there are 20 or more
                     text=text.substring(c+1,c+21);
                  }
                  else
                  {
                     //set text to all chars after the space if less than 20
                     text=text.substring(c+1);
                  }
                  break;
               }
            }
            //look for original tweet
            text=searchPro(text,addition);
            text=searchAgainst(text,addition);
            //reset text to retweet if original not found
            if (text.equals(""))
            {
               text=sh.getCell(10,row).getContents().toLowerCase();
            }
         }
         String time=sh.getCell(1,row).getContents();//time is in the 1st collomn of the excel file
         //make new tweet object
         tw=new Tweet(text, isRT, RTfrom, new Time(toInt(time.substring(8,10)),toInt(time.substring(11,13)),toInt(time.substring(14,16))), "@"+sh.getCell(2, row).getContents());//these are the indices of the day, hour, and min in the time string format, the user is in the 2nd collumn
         //add tweet to correct array
         analyse(tw);
         
      }
   }
   
   private void analyse(Tweet tw)
   {
      String text=tw.getText();
      //these trump all for pro
      if (has(text,"#savemetro","#yesonprop1","#yesprop1"))
         proTweets.add(tw);
      //definitly against
      else if (has(text,"#noonprop1"))
         againstTweets.add(tw);
      //strongly suggest pro because of main point of the movement
      else if (has(text,"save","cuts","cut","17%","yes on prop 1","yes on proposition 1","support"))
         proTweets.add(tw);
      else if (has(text,"no on prop 1"))
      {
         //context determines
         if (has(text,"sign","senior","disabled","low income","dumb","traffic","who voted no","who are voting no","if you","fuck","shit","hate"))
            proTweets.add(tw);
         else
            againstTweets.add(tw);
      }
      else if (has(text,"prop 1","#prop1","prop. 1","proposition 1"))
      {
         //common words in arguments for
         if (has(text,"@seatransitblog","@movekingconow","@transpochoices","yes","reduce")||tw.getRTfrom().equals("@seatransitblog")||tw.getRTfrom().equals("@movekingconow")||tw.getRTfrom().equals("@transpochoices"))
            proTweets.add(tw);
         //common words in arguments against
         else if (has(text,"inefficien","waste","against"))
            againstTweets.add(tw);
      }     
   }
   
   //returns true if text contains any of the keywords
   private boolean has(String text,String...keywords)
   {
      for (String s:keywords)
      {
         if(text.toLowerCase().contains(s))
            return true;
      }
      return false;
   }
   
   //returns text of original tweet given first 20 (or less) chars of tweet if in ProTweets
   private String searchPro(String text,String add)
   {
      String othertext;
      for(int ii=0; ii<proTweets.size(); ii++)//search proTweets
      {
         othertext=proTweets.get(ii).getText();
         //if tweet has same first 20 chars (or as many as exist if less than 20 long)
         if ((othertext.length()>=20&&othertext.substring(0,20).equals(text))||(othertext.length()<20&&othertext.equals(text)))
         {
            //return the text added in RT plus text of original tweet
            return add+othertext;
         }
      }
      return "";//if made through all trweets w/o finding
   }
   
   //returns text of original tweet given first 20 (or less) chars of tweet if in againstTweets
   private String searchAgainst(String text,String add)
   {
      String othertext;
      for(int ii=0; ii<againstTweets.size(); ii++)//search againstTweets
      {
         othertext=againstTweets.get(ii).getText();
         //if tweet has same first 20 chars (or as many as exist if less than 20 long)
         if ((othertext.length()>=20&&othertext.substring(0,20).equals(text))||(othertext.length()<20&&othertext.equals(text)))
         {
            //sets the text to the text added in RT plus text of original tweet
            return add+" "+othertext;
         }
      }
      return "";//if made through all trweets w/o finding
   }
   
   //converts a string to an int
   //precondition: s is only digits
   private int toInt(String s)
   {
      Scanner scan=new Scanner(s);
      return scan.nextInt();
   }
}