import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.Arrays;

class Oblig2{
  private static int n, seed, antallTraader = Runtime.getRuntime().availableProcessors();
  private static double time;
  private static double[] timeS1 = new double[7];
  private static double[] timeS2 = new double[7];
  private static double[] timeS3 = new double[7];
  private static double[] timeP1 = new double[7];
  private static double[] timeP2 = new double[7];
  private static double[] timeP3 = new double[7];
  private static double[][] a,b,at,bt,c;
  private static final int toMilli = 1000000;
  private static final CyclicBarrier cb = new CyclicBarrier(antallTraader+1);

  public static void main(String[] args){
    //leser inn storrelse paa matrise
    try{
      n = Integer.parseInt(args[0]);
      if (n<antallTraader){
        System.out.println("Warning: more threads than n. Have to tune it down");
        antallTraader = n;
      }
      seed = Integer.parseInt(args[1]);
    }
    catch(Exception e){
      System.out.println("Proper Use: java Oblig2 n seed");
      return;
    }
    a  = new double[n][n];
    b  = new double[n][n];

    //generer matriser
    a = Oblig2Precode.generateMatrixA(seed,n);
    b = Oblig2Precode.generateMatrixB(seed,n);
    for(int z=0;z<7;z++){
      at = new double[n][n];
      bt = new double[n][n];
      c = new double[n][n];
      // loser sekvensiellt paa alle 3 maater
      time = System.nanoTime();
      transpose(b,bt,0,n);
      calcABT(0,n);
      timeS1[z] = (System.nanoTime()-time)/toMilli;
      Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.SEQ_B_TRANSPOSED, c);
      c = new double[n][n];

      time = System.nanoTime();
      transpose(a,at,0,n);
      calcATB(0,n);
      timeS2[z] = (System.nanoTime()-time)/toMilli;
      Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.SEQ_A_TRANSPOSED, c);
      c = new double[n][n];

      time = System.nanoTime();
      calcAB(0,n);
      timeS3[z] = (System.nanoTime()-time)/toMilli;
      Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.SEQ_NOT_TRANSPOSED, c);
      //toemmer matrisene slik at parallell kan gjoere sitt forsoek aerlig
      c = new double[n][n];
      at = new double[n][n];
      bt = new double[n][n];
      //loeser parallell
      startThreads();
      try{
        //tid for ABT
        time = System.nanoTime();
        cb.await();
        cb.await();
        timeP1[z] = (System.nanoTime()-time)/toMilli;
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.PARA_B_TRANSPOSED, c);
        c = new double[n][n];
        cb.await();

        //tid for ATB
        time = System.nanoTime();
        cb.await();
        cb.await();
        timeP2[z] = (System.nanoTime()-time)/toMilli;
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.PARA_A_TRANSPOSED, c);
        c = new double[n][n];
        cb.await();

        //tid for AB
        time = System.nanoTime();
        cb.await();
        timeP3[z] = (System.nanoTime()-time)/toMilli;
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.PARA_NOT_TRANSPOSED, c);
      }
      catch(InterruptedException e){
        System.out.println("Interrupted");
      }
      catch(BrokenBarrierException e){
        System.out.println("Broken Barrier");
      }
      Arrays.sort(timeS1);
      Arrays.sort(timeS2);
      Arrays.sort(timeS3);
      Arrays.sort(timeP1);
      Arrays.sort(timeP2);
      Arrays.sort(timeP3);
    }
    System.out.println("     Sequential time    Parallel time          Speedup");
    System.out.printf("ABT %16f %16f %16f\n",timeS1[3],timeP1[3],timeS1[3]/timeP1[3]);
    System.out.printf("ATB %16f %16f %16f\n",timeS2[3],timeP2[3],timeS2[3]/timeP2[3]);
    System.out.printf("AB  %16f %16f %16f\n",timeS3[3],timeP3[3],timeS3[3]/timeP3[3]);
  }
  public static void startThreads(){
    int radPer = n/antallTraader;
    int rest = n%antallTraader;
    int start = 0;
    for(int i = 0;i < antallTraader; i++){
      if(rest!= 0){
        new Thread(new Worker(i,start,start+radPer+1)).start();
        rest--;
        start+=radPer+1;
      }
      else{
        new Thread(new Worker(i,start,start+radPer)).start();
        start += radPer;
      }
    }
  }
  public static void transpose(double[][] a,double[][] at,int start, int end){
    for(int i = start;i<end;i++){
      for(int j = 0;j<n;j++){
        at[i][j] = a[j][i];
      }
    }
  }
  public static void calcABT(int start, int end){
    double sum = 0;
    for(int i = start;i<end;i++){
      for(int j = 0;j<n;j++){
        for(int k = 0;k<n;k++){
          sum+= a[i][k]*bt[j][k];
        }
        c[i][j] = sum;
        sum = 0;
      }
    }
  }
  public static void calcATB(int start, int end){
    double sum = 0;
    for(int i = start;i<end;i++){
      for(int j = 0;j<n;j++){
        for(int k = 0;k<n;k++){
          sum+= at[k][i]*b[k][j];
        }
        c[i][j] = sum;
        sum = 0;
      }
    }
  }
  public static void calcAB(int start, int end){
    double sum = 0;
    for(int i = start;i<end;i++){
      for(int j = 0;j<n;j++){
        for(int k = 0;k<n;k++){
          sum+= a[i][k]*b[k][j];
        }
        c[i][j] = sum;
        sum = 0;
      }
    }
  }

  static class Worker implements Runnable{
    int ind, start, end;
    Worker(int ind,int start, int end){
      this.ind = ind;
      this.start = start;
      this.end = end;
    }
    public void run(){
      try{
        transpose(b,bt,start,end);
        //every thread has to wait for everyone to do their part of transposing
        cb.await();
        calcABT(start,end);
        cb.await();
        cb.await();

        transpose(a,at,start,end);
        cb.await();
        calcATB(start,end);
        cb.await();
        cb.await();

        calcAB(start,end);
        cb.await();
      }
      catch(InterruptedException e){
        System.out.println("Interrupted");
      }
      catch(BrokenBarrierException e){
        System.out.println("Broken Barrier");
      }
    }
  }
}
