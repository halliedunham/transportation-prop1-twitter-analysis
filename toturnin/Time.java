public class Time implements Comparable<Time>
{
   private int day;
   private int hour;
   private int min;
   public Time(int d, int h, int m)
   {
      day=d;
      hour=h;
      min=m;
   }
   public int getDay()
   {
      return day;
   }
   public int getHour()
   {
      return hour;
   }
   public int getMin()
   {
      return min;
   }
   public String toString()
   {
      return "April "+day+" "+hour+":"+min;
   }
   public int compareTo(Time other) //returns negative number if before other, positive if after, 0 if same minute
   {
      if (day-other.getDay()<0)
         return -1;
      if (day-other.getDay()>0)
         return 1;
      else //if day same check hour
      {
         if (hour-other.getHour()<0)
            return -1;
         if (hour-other.getHour()>0)
            return 1;
         else //if hour same check minute
            return min-other.getMin();
      }
   }
      

            
}