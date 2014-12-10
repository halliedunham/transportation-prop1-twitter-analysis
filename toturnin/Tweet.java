public class Tweet implements Comparable<Tweet>
{
   private String text;
   private boolean RT;
   private String RTfrom;
   private Time time;
   private String userHandle;
   
   public Tweet(String s, boolean rt, String from, Time t, String u)
   {
      text=s.toLowerCase();
      RT=rt;
      RTfrom=from;
      time=t;
      userHandle=u;
   }
   
   public String getText()
   {
      return text;
   }
   public boolean isRT()
   {
      return RT;
   }
   public String getRTfrom()
   {
      return RTfrom;
   }
   public Time getTime()
   {
      return time;
   }
   //returns number of hours after the first hour of April 1st
   public int simpleTime()
   {
      return time.getDay()*24+time.getHour();
   }
   public String getUser()
   {
      return userHandle;
   }
   public int compareTo(Tweet other) //returns negative number if before other, positive if after, 0 if same minute
   {
      return time.compareTo(other.getTime());
   }
}