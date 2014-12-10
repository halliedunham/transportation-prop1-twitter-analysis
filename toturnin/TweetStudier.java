import java.util.*;
import java.io.*;
import jxl.*;
import jxl.write.*;

public class TweetStudier
{
   public static void main(String[] args) throws Exception
   {
      //access the excel file with data
      Workbook wb1=Workbook.getWorkbook(new File("alltweets.xls"));
      Sheet sh1=wb1.getSheet(0);
      //separate into two ArrayLists of Tweets
      TweetFilter tf=new TweetFilter(sh1);
      Estimator est=new Estimator(tf.getPro(),tf.getAgainst());
      //makes two text files for tweets pro and against, so uncomment if you want to see how effective my filter is
      //testFilter(tf);
      //prints estimator data to console by hour if you want to see that
      //testEstimator(tf,est);
      
      //right now this is set to show april 20th-24th which is around vote 
      //and interesting to see how pro peaked before vote (22nd) 
      //and against peaked after prop 1 lost
      graphByHours(tf,est,480,599);
      //graphs whole time I collected (3rd-25th)
      graphByDays(tf,est);
      
   }
   
   //prints excel file that when graphed as stacked bar chart shows tweets per day
   //pro/against separate columns and measured with estimated on top in same column
   public static void graphByDays(TweetFilter tf, Estimator est) throws Exception
   {
      //make excel file can write to
      WritableWorkbook wb3=Workbook.createWorkbook(new File("tweetsperday.xls"));
      wb3.createSheet("sheet",0);
      WritableSheet sh3=wb3.getSheet(0);
      //make headers
      sh3.addCell(new Label(0,0,"day"));
      sh3.addCell(new Label(1,0,"pro measured"));
      sh3.addCell(new Label(2,0,"pro estimated"));
      sh3.addCell(new Label(3,0,"against measured"));
      sh3.addCell(new Label(4,0,"against estimated"));
      //get data arrays from estimator
      int[] proYs=est.getDayValues('a','y');
      int[] proAs=est.getDayValues('a','a');
      int[] againstYs=est.getDayValues('b','y');
      int[] againstAs=est.getDayValues('b','a');
      int first_day;
      int last_day;
      //first day is the first on either list
      if (est.getAStartTime()<=est.getBStartTime())
      {
         first_day=est.getAStartTime()/24;
      }
      else
      {
         first_day=est.getBStartTime()/24;
      }
      //last day is last on either list
      if (est.getAEndTime()>=est.getBEndTime())
      {
         last_day=est.getAEndTime()/24;
      }
      else
      {
         last_day=est.getBEndTime()/24;
      }
      //print data for each day, each day takes three rows
      for (int row=1; row<=3*(last_day-first_day+1); row+=3)
      {
         //print day label column
         sh3.addCell(new Label(0,row,""+(first_day+(row-1)/3)+"p"));
         sh3.addCell(new Label(0,row+1,""+(first_day+(row-1)/3)+"a"));
         //if day - start day (index) not neg and not day not past the end day
         if(first_day+(row-1)/3-est.getAStartTime()/24>=0&&first_day+(row-1)/3<=est.getAEndTime()/24)
         {
            //record measured and estimated for pro
            sh3.addCell(new jxl.write.Number(1,row,proYs[first_day+(row-1)/3-est.getAStartTime()/24]));
            sh3.addCell(new jxl.write.Number(2,row,proAs[first_day+(row-1)/3-est.getAStartTime()/24]));
         }
         //same as above but for against and one line bellow so bars display next to each other on chart
         if(first_day+(row-1)/3-est.getBStartTime()/24>=0&&first_day+(row-1)/3<=est.getBEndTime()/24)
         {
            sh3.addCell(new jxl.write.Number(3,row+1,againstYs[first_day+(row-1)/3-est.getBStartTime()/24]));
            sh3.addCell(new jxl.write.Number(4,row+1,againstAs[first_day+(row-1)/3-est.getBStartTime()/24]));
         }
      }
      //put all the data created above into the file and close to save memory
      wb3.write();
      wb3.close();
   }

   //prints excel file that when graphed as stacked bar chart shows tweets hour day
   //pro/against separate columns and measured with estimated on top in same column
   public static void graphByHours(TweetFilter tf, Estimator est, int hourToStartChart, int hourToEndChart) throws Exception
   {
      //make excel file can write to
      WritableWorkbook wb2=Workbook.createWorkbook(new File("tweetsperhour.xls"));
      wb2.createSheet("sheet",0);
      WritableSheet sh2=wb2.getSheet(0);
      //make headers
      sh2.addCell(new Label(0,0,"time"));
      sh2.addCell(new Label(1,0,"pro measured"));
      sh2.addCell(new Label(2,0,"pro estimated"));
      sh2.addCell(new Label(3,0,"against measured"));
      sh2.addCell(new Label(4,0,"against estimated"));
      //get data arrays from estimator
      int[] proYs=est.getAYValues();
      int[] proAs=est.getAYAdditions();
      int[] againstYs=est.getBYValues();
      int[] againstAs=est.getBYAdditions();
      int first_t;
      int last_t;
      //first day is first of either list
      if (est.getAStartTime()<=est.getBStartTime())
      {
         first_t=est.getAStartTime();
      }
      else
      {
         first_t=est.getBStartTime();
      }
      //last day is last of either list
      if (est.getAEndTime()>=est.getBEndTime())
      {
         last_t=est.getAEndTime();
      }
      else
      {
         last_t=est.getBEndTime();
      }
      //keep chart time param within total domain
      if(hourToStartChart<first_t)
         hourToStartChart=first_t;
      if(hourToEndChart>last_t)
         hourToEndChart=last_t;
      //print data for each hour specified by params, each hour takes three rows
      for (int row=1; row<=3*(hourToEndChart-hourToStartChart+1); row+=3)
      {
         //add time label column
         String date="";
         //only write in at beginning of each day
         if(((row-1)/3+hourToStartChart)%24==0)
         {
            date="April "+(((row-1)/3+hourToStartChart)/24);
         }
         sh2.addCell(new Label(0,row,date));
         //if index within bound
         if((row-1)/3-est.getAStartTime()+hourToStartChart>=0&&(row-1)/3+hourToStartChart<=est.getAEndTime())
         {
            //add pro data measured to col 1 and estimated to col 2
            sh2.addCell(new jxl.write.Number(1,row,proYs[(row-1)/3-est.getAStartTime()+hourToStartChart]));
            sh2.addCell(new jxl.write.Number(2,row,proAs[(row-1)/3-est.getAStartTime()+hourToStartChart]));
         }
         if((row-1)/3-est.getBStartTime()+hourToStartChart>=0&&(row-1)/3+hourToStartChart<=est.getBEndTime())
         {
            //add against data measured to col 3 and estimated to col 4 both on row below pro to make columns next to ech other on chart
            sh2.addCell(new jxl.write.Number(3,row+1,againstYs[(row-1)/3-est.getBStartTime()+hourToStartChart]));
            sh2.addCell(new jxl.write.Number(4,row+1,againstAs[(row-1)/3-est.getBStartTime()+hourToStartChart]));
         }
      }
      //write all data to the file and close
      wb2.write();
      wb2.close();
   }
   
   public static void testEstimator(TweetFilter tf, Estimator est)
   {
      //get data from estimator
      int[] proYs=est.getAYValues();
      int[] proAs=est.getAYAdditions();
      int[] againstYs=est.getBYValues();
      int[] againstAs=est.getBYAdditions();
      //tt is first time in either A or B arrays
      int tt;
      if (est.getAStartTime()<=est.getBStartTime())
      {
         tt=est.getAStartTime();
      }
      else
      {
         tt=est.getBStartTime();
      }
      //loops hours until later end time
      while (tt<=est.getAEndTime()||tt<=est.getBEndTime())
      {
         System.out.print("time "+tt);
         //if tt included in A array
         if(tt-est.getAStartTime()>=0&&tt<=est.getAEndTime())
         {
            //print pro measured (Y) and estimated to add (A)
            System.out.print(" pY "+proYs[tt-est.getAStartTime()]);
            System.out.print(" pA "+proAs[tt-est.getAStartTime()]);
         }
         //if tt included in B array
         if(tt-est.getBStartTime()>=0&&tt<=est.getBEndTime())
         {
            //print against measured (Y) and estimated to add (A)
            System.out.print(" aY "+againstYs[tt-est.getBStartTime()]);
            System.out.print(" aA "+againstAs[tt-est.getBStartTime()]);
         }
         System.out.println("");
         tt++;
      }
   }
   
   //makes 2 text files, one with pro tweets and one with against
   public static void testFilter(TweetFilter tf) throws Exception
   {
      Tweet tweet;
      //get pro array and print info of each tweet to protweets.txt
      ArrayList<Tweet> pro=tf.getPro();
      PrintStream pstream=new PrintStream(new File("protweets.txt"));
      for(int pp=0;pp<pro.size();pp++)
      {
         tweet=pro.get(pp);
         pstream.println("time: "+tweet.getTime().toString()+",\tuser: "+tweet.getUser()+",\tRT: "+tweet.isRT()+", text: "+tweet.getText());
      }
      //get against array and print info of each tweet to againsttweets.txt
      ArrayList<Tweet> against=tf.getAgainst();
      PrintStream astream=new PrintStream(new File("againsttweets.txt"));
      for(int aa=0;aa<against.size();aa++)
      {
         tweet=against.get(aa);
         astream.println("time: "+tweet.getTime().toString()+",\tuser: "+tweet.getUser()+",\tRT: "+tweet.isRT()+", text: "+tweet.getText());
      }
   }
}