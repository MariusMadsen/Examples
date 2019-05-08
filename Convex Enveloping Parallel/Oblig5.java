import java.util.Arrays;
class Oblig5{
  public static int n,k,MAX_X,MAX_Y,MIN_X,MIN_Y;
  public static int[] N = {1000,10000,100000,1000000,10000000,100000000};
  public static int median = 7;
  public static int[] x,y;
  public static double time;
  public static double[] timeS, timeP;
  public static void main(String[] args){
    timeS = new double[median];
    timeP = new double[median];
    System.out.println("_____________________________________________________");
    System.out.println("|n          | Sek time(ms) | Par time(ms) | Speedup  |");
    for(int nn:N){
      n = nn;// if i use int n in forloop the TegnUt algorithm wont draw points properly
      Oblig5 d = new Oblig5();
      x = new int[n];
      y = new int[n];
      NPunkter17 p = new NPunkter17(n);
      p.fyllArrayer(x,y);
      IntList listS = new IntList();
      IntList listP = new IntList();
      for(int t = 0;t<median;t++){
        try{
          k = Integer.parseInt(args[0]);
        }
        catch(Exception e){
          System.out.println("Proper Use: java Oblig5 k");
          return;
        }

        IntList remaingingInds = new IntList();
        time = System.nanoTime();
        for(int i = 0;i<n;i++){
          remaingingInds.add(i);
        }
        Sek sek = new Sek(x,y,remaingingInds);
        timeS[t] = (System.nanoTime()-time)/1000000;
        listS = sek.list;

        time = System.nanoTime();
        Par par = new Par(n,k,x,y);
        timeP[t] = (System.nanoTime()-time)/1000000;
        listP = par.list;

        Arrays.sort(timeP);
        Arrays.sort(timeS);
        /*MIN_Y = sek.MIN_Y; //if you want to draw
        MAX_Y = sek.MAX_Y;
        MAX_X = sek.MAX_Y;
        MIN_X = sek.MIN_Y;
        new TegnUt(d,listP);*/
      }
      System.out.printf("| %9d | %12.2f | %12.2f | %8.2f |\n",n,timeS[median/2],timeP[median/2],timeS[median/2]/timeP[median/2]);
    }
    System.out.println("|___________|______________|______________|__________|");

  }
}
