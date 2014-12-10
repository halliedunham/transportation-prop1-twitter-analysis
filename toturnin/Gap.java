import java.lang.Math;
import java.util.*;
public class Gap
{
   //known point before gap
   private int A;
   //known point after gap
   private int B;
   //an array with the number of estimated tweets on each interval of the gap
   int[] estimatedTweets;
   //three y values before gap in order
   private int ya1;
   private int ya2;
   private int ya3;
   //three y values after gap in order
   private int yb1;
   private int yb2;
   private int yb3;
   
   public Gap(int start, int end, int ya1, int ya2, int ya3, int yb1, int yb2, int yb3)
   {
      A=start-1;
      B=end+1;
      this.ya1=ya1;
      this.ya2=ya2;
      this.ya3=ya3;
      this.yb1=yb1;
      this.yb2=yb2;
      this.yb3=yb3;
      estimatedTweets=estimateTweets(findCoeffs(), start, end);
   }
   
   public int[] getEstimate()
   {
      return estimatedTweets;
   }
   
   public int getStartTime()
   {
      return A+1;
   }
   
   public int getEndTime()
   {
      return B-1;
   }
   
   //inputs each t between start and end inclusive to the cubic defined by coeffs,
   //rounds down to an int, and stores in array
   private int[] estimateTweets(double[] coeffs, int start, int end)
   {
      int[] estimatedTweets=new int[end-start+1];
      int y=0;
      //loop each t in gap
      for (int t=start; t<=end; t++)
      {
         //add terms
         for (int co=0;co<coeffs.length; co++)
         {
            y+=coeffs[co]*Math.pow(t,co);
         }
         //store in array
         estimatedTweets[t-start]=y;
         y=0;
      }
      return estimatedTweets;
   }
   
   //calculates 4 coefficiants for cubic approximation and returns them in array
   private double[] findCoeffs()
   {
      //array to store in at end of method
      double[] coeffs=new double[4];
      //matrices for system, matrix*coeffs=vector
      double[][] matrix=makeSystemMatrix();
      double[] vector=makeSumVector();
      //used for the ratio between nth col of nth row that is being subtracted from each row to make their nth col=0
      double factor;
      //loop through rows to use to make all rest 0 in the rowth column
      for (int row=0; row<matrix.length; row++)
      {
         //loop through row to subtract from
         for (int otherrow=0; otherrow<matrix.length; otherrow++)
         {
            if (row!=otherrow)
            {
               //System.out.print("row/col: "+row+"matrix value: "+matrix[row][row]);
               factor=matrix[otherrow][row]/matrix[row][row];
               //subtract the cols of row from the cols of otherrow
               for (int col=0; col<matrix[0].length; col++)
               {
                  matrix[otherrow][col]-=factor*matrix[row][col];
               }
               //same on other side of equation
               vector[otherrow]-=factor*vector[row];
            }
         }
      }
      //solve the matix equation
      for (int row=0; row<coeffs.length; row++)
      {
         coeffs[row]=vector[row]/matrix[row][row];
      }
      return coeffs;
   }
   
   //makes a matrix  that when multiplied by the coeff vector gives the sum vector,
   //determined by hand based on values and derivatives on either side of gap
   private double[][] makeSystemMatrix()
   {
      double[][] matrix=new double[4][4];
      //coeffs for equations based on y values
      for (int col=0; col<matrix[0].length; col++)
      {
         matrix[0][col]=1*Math.pow(A, col);
         matrix[1][col]=1*Math.pow(B, col);
      }
      //coeffs for equations based on derivs
      for (int col=0; col<matrix[2].length; col++)
      {
         matrix[2][col]=col*Math.pow(A, col-1);
         matrix[3][col]=col*Math.pow(B, col-1);
      }
      return matrix;
   }
   
   private double[] makeSumVector()
   {
      //first 2 rows are sums for equations based on y values
      //2nd 2 rows are sums for equations based on derivs
      double[] vector={ya1,yb1,ya3-ya1,yb3-yb1};
      return vector;
   }
      
   
   private double get1DerivA()
   {
      return (ya3-ya1)/2;//aprox slope before gap
   }
   
   private double get1DerivB()
   {
      return (yb3-yb1)/2;//aprox slope after gap
   }
   
   private double get2DerivA()
   {
      return ((ya3-ya2)-(ya2-ya1))/2;//aprox 2nd derivative before gap
   }
   
   private double get2DerivB()
   {
      return ((yb3-yb2)-(yb2-yb1))/2;//aprox 2nd derivative after gap
   }
}