import java.util.*;
public class Estimator
{
   int[] ayvalues;//num recorded tweets in each time interval A_START_TIME to A_END_TIME for position a
   int[] ayadditions;//num estimated a tweets in addtion to recorded with same interval
   int[] byvalues;//num recorded tweets in each time interval B_START_TIME to B_END_TIME for position b
   int[] byadditions;//num estimated b tweets in addtion to recorded with same interval
   private int A_START_TIME;//time of first tweet recorded with position a toward issue
   private int A_END_TIME;//time of last a tweet
   private int B_START_TIME;//time of first recorded tweet with position b toward issue
   private int B_END_TIME;//time of last b tweet
      
   //precondition: a has much more data in it than b so it determines the gaps
   //if they both had enough data would just need to use makeAAdditionsArray(gaps) on B as well
   public Estimator(ArrayList<Tweet> a, ArrayList<Tweet> b)
   {
      //the ArrayList as an intermediate step may seem unnecessary, but I created the y value arrays in these two steps
      //with the intention of later making the graphic be able to access the ArrayLost of actual tweets
      //and when the user would hover over a column on chart some texts of tweets would display
      //make A arrays
      ArrayList<ArrayList<Tweet>> atweetIntervals=divideIntervals(a,'a');
      ayvalues=makePointArray(atweetIntervals);
      ArrayList<Gap> gaps=findGaps(ayvalues);//gaps are determined by position a because it has much fuller data
      ayadditions=makeAAdditionsArray(gaps);
      //make B arrays
      ArrayList<ArrayList<Tweet>> btweetIntervals=divideIntervals(b,'b');
      byvalues=makePointArray(btweetIntervals);
      byadditions=makeBAdditionsArray(gaps);
   }
   
   //public meathod to create and return an array like ayvalues, ayadditions, byvalues, or byaddtions (depends on param c and type)
   //but with intervals as 1 day instead of 1 hour
   public int[] getDayValues(char c, char type)//c is a or b and type is y or a
   {
      int start;
      int end;
      int length;
      int[] y;
      if (c=='a')
      {
         start=A_START_TIME;
         end=A_END_TIME;
         length=ayvalues.length;
         if (type=='y')
            y=ayvalues;
         else //assume with be a
            y=ayadditions;
      }
      else//assuming would be b
      {
         start=B_START_TIME;
         end=B_END_TIME;
         length=byvalues.length;
         if (type=='y')
            y=byvalues;
         else //will be a
            y=byadditions;
      }
      int[] days=new int[end/24-start/24+1];//days from start to finish inclusive
      for (int ii=0; ii<length; ii++)//loop through all hours
      {
         //start+ii gives the time in hours,divide by 24 to get what day its in, 
         //subtract day of start time and that gives the index for that day.
         
         days[(start+ii)/24-start/24]+=y[ii];//adds tweets in all hours of each day
      }
      return days;
   }
   
   public int[] getAYValues()
   {
      return ayvalues;
   }
   
   public int[] getAYAdditions()
   {
      return ayadditions;
   }
   
   public int getAStartTime()
   {
      return A_START_TIME;
   }
   
   public int getAEndTime()
   {
      return A_END_TIME;
   }
   
   public int[] getBYValues()
   {
      return byvalues;
   }
   
   public int[] getBYAdditions()
   {
      return byadditions;
   }
   
   public int getBStartTime()
   {
      return B_START_TIME;
   }
   
   public int getBEndTime()
   {
      return B_END_TIME;
   }
   
   //uses ArrayList of Gaps to make an array with the estimated addtions for all gaps combined
   private int[] makeAAdditionsArray(ArrayList<Gap> gaps)
   {
      ayadditions=new int[ayvalues.length];
      Gap g;
      for (int gg=0; gg<gaps.size(); gg++)//loop gaps
      {
         g=gaps.get(gg);
         for (int ee=0; ee<g.getEstimate().length; ee++)//loop hours of gap
         {
            //System.out.println("gap "+gg+" interval "+(g.getStartTime()+ee));
            if (g.getEstimate()[ee]>0)
            {
               //System.out.println(g.getEstimate()[ee]);
               //if estimate is positive, set array value with corresponding time to the estimate for that hour
               ayadditions[g.getStartTime()+ee]=g.getEstimate()[ee];
            }
            //else
            //   System.out.println("0");
         }
      }
      return ayadditions;
   }
   
   //uses gaps found in A to estimate twets to add to B
   //B does not have enough data to determine gaps from itself
   private int[] makeBAdditionsArray(ArrayList<Gap> gaps)
   {
      byadditions=new int[byvalues.length];
      Gap g;
      int countAround=0;//to count the number of tweets found in 10 hours on either side of gap
      for (int gg=0; gg<gaps.size(); gg++)//loop gaps
      {
         g=gaps.get(gg);
         //add number of tweets in 10 hours before to countAround
         for (int tt=g.getStartTime()-1; tt>g.getStartTime()-10&&tt>=B_START_TIME; tt--)
         {
            if (byvalues[tt-B_START_TIME]!=0)
            {
               countAround++;
            }
         }
         //add number of tweets in 10 hours after to countAround
         for (int tt=g.getEndTime()+1; tt<g.getEndTime()+10&&tt<byvalues.length&&tt>B_START_TIME; tt++)
         {
            if (byvalues[tt-B_START_TIME]!=0)
            {
               countAround++;
            }
         }
         //estimates numToAdd to that ratio of tweets/time is same in gap as around gap
         int numToAdd=(int) (countAround*(double)(g.getEndTime()-g.getStartTime()+1)/20);//ratio between length of gap inclusive and how many hours around the gap I am looking at (20)
         Random r=new Random();
         for (int nn=0; nn<numToAdd; nn++)
         {
            //add tweet to random time within gap
            byadditions[r.nextInt(g.getEndTime()-g.getStartTime()+1)+g.getStartTime()-B_START_TIME]=1;
         }
      }
      return byadditions;
   }
   
   //creates an ArrayList of Gap objects for each part of data that appears to be a place where I missed collection
   private ArrayList<Gap> findGaps(int[] y)
   {
      boolean isGap=false;
      int gap_start=0;
      int gap_end=0;
      ArrayList<Gap> gaps=new ArrayList<Gap>();
      for (int x=0; x<y.length; x++)//loop through array with number of tweets per hour
      {
         if (y[x]==0||(isGap&&y[x]==1))//always could be part of gap if 0, could be if 1 if proceeded by a gap
         {
            if (!isGap)//if this is first sign of it being a gap
            {
               //if looking at previous indeces would not try to access a negative index of the array and 
               //if hour before appears to be a partial hour (less than half the average of the 2 hours before,
               //negative 1 accounts for for example the difference between 3 and 1 being less significant than 10 and 4)
               if ((x>2&&y[x-1]<(((y[x-2]+y[x-3])/2-1)/2)||(x>1&&x<3&&y[x-1]<(y[x-2]-1)/2)))
               {
                  //then previous index is part of gap
                  gap_start=x-1;
               }
               else
               {
                  gap_start=x;
               }
               isGap=true;
            }
         }
         else if (isGap&&x<y.length-2)//if proceeded by zeros/ones but is not zero or one and isnt to close to end of time
         {
            if (y[x]<((y[x+1]+y[x+2])/2-1)/2)//if hour after gap appears to be partial hour and part of gap, see above for reasoning
            {
               //then current index is part of gap
               gap_end=x;
            }
            else
            {
               gap_end=x-1;
            }
            //domain of gap is defined, now is it a real gap? if gap 5 ts or longer
            if (gap_end-gap_start>3)
            {
               int x1;
               int x2;
               if (gap_start<2)//if gap_start-2 index wouldn't work aka gap_start==1 (gap not possible at index 0 b/c can't be 0 b/c y starts on first tweet)
               {
                  //pretend index 2 before gap start is has same value as index 1 before
                  x2=gap_start-1;
               }
               else
               {
                  x2=gap_start-2;
               }
               if (gap_start<3)//if gap_start-3 index wouldn't work 
               {
                  //pretend index 3 before gap start is has same value as index 2 before
                  x1=x2;
               }
               else
               {
                  x1=gap_start-3;
               }
               //add the gap to the arraylist
               gaps.add(new Gap(gap_start, gap_end, y[x1], y[x2], y[gap_start-1], y[gap_end+1], y[gap_end+2], y[gap_end+3]));
            }
            //becomes not a gap anymore whether added or not
            isGap=false;
         }
         else
         {
            isGap=false;
         }
      }
      return gaps;
   }
   
   //simplifies the tweetIntervals to an int array with the number of tweets in each hour
   private int[] makePointArray(ArrayList<ArrayList<Tweet>> tweetIntervals)
   {
      int[] points=new int[tweetIntervals.size()];
      for (int pp=0; pp<points.length; pp++)
      {
         //the y value equals the number of tweets in that interval
         //System.out.print(tweetIntervals.get(pp).size()+",");
         points[pp]=tweetIntervals.get(pp).size();
      }
      return points;
   }
   
   //divides all tweets of one kind into arraylists for each hour and holds them all in another arraylist
   private ArrayList<ArrayList<Tweet>> divideIntervals(ArrayList<Tweet> tweets, char c)
   {
      ArrayList<ArrayList<Tweet>> dividedTweets=new ArrayList<ArrayList<Tweet>>();
      int start_time=tweets.get(0).simpleTime();//starts with the hour of the first tweet collected
      int end_time=tweets.get(tweets.size()-1).simpleTime();//end with the hour of the last tweet collected
      //makes start_time the first tweet occurence in either A or B
      if (c=='a')
      {
         A_START_TIME=start_time;
         A_END_TIME=end_time;
      }
      else //assuming it will be b
      {
         B_START_TIME=start_time;
         B_END_TIME=end_time;
      }
      //System.out.println("start "+start_time+" end "+end_time);
      //loops through hours betweet first and last time value inclusive
      for (int tt=start_time; tt<=end_time; tt++)
      {
         dividedTweets.add(new ArrayList<Tweet>());//makes an inner ArrayList for h=each hour
      }
      for (int jj=0; jj<tweets.size(); jj++)
      {
         //if tweet within time domain which it should be
         if (tweets.get(jj).simpleTime()>=start_time&&tweets.get(jj).simpleTime()<=end_time)
         {
            //add tweet to correct interval
            dividedTweets.get((tweets.get(jj).simpleTime()-start_time)).add(tweets.get(jj));
         }
      }
      return dividedTweets;
   }
}