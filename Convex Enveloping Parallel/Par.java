import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

class Par{
  IntList list;
  IntList[] lists;
  int n,k;
  int[] x,y;
  int MAX_X,MAX_Y,MIN_Y,MIN_X;
  CyclicBarrier cb;
  Par(int n,int k,int[] x,int[] y){
    this.list = new IntList();
    this.n = n;
    this.k = k;
    this.x = x;
    this.y = y;
    cb = new CyclicBarrier(k+1);
    startThreads();
  }
  void startThreads(){
    int start = 0;
    int numPer = n/k;
    int remainer = n%k;

    lists = new IntList[k];
    for(int i = 0; i<k;i++){
      lists[i] = new IntList();
      if(remainer != 0){
        new Thread(new Worker(i,start,start+numPer+1)).start();
        start+=numPer+1;
        remainer--;
      }
      else{
        new Thread(new Worker(i,start,start+numPer)).start();
        start+=numPer;
      }
    }
    try{
      cb.await();
      cb.await();
    }
    catch(BrokenBarrierException e){
      System.out.println("BrokenBarrierException");
    }
    catch(InterruptedException e){
      System.out.println("InterruptedException");
    }
  }

  class Worker implements Runnable{
    int ind,start,end;
    Worker(int ind,int start,int end){
      this.ind = ind;
      this.start = start;
      this.end = end;
    }
    public void run(){
      IntList remaingingInds = new IntList();
      for(int i = start;i<end;i++){
        remaingingInds.add(i);
      }
      Sek sek = new Sek(x,y,remaingingInds);//Each thread executes its part of the array sequentally
      lists[ind] = sek.list;//their result is stored in here
      try{
        cb.await();
        if(ind == 0){
          list = lists[0];
          for(int i = 1;i<k;i++){//thread 0 will add every solution to his
            list.append(lists[i]);
          }
          sek = new Sek(x,y,list);// then do it sequeantally again
          list = sek.list;
        }
        cb.await();
      }
      catch(BrokenBarrierException e){
        System.out.println("BrokenBarrierException");
      }
      catch(InterruptedException e){
        System.out.println("InterruptedException");
      }
    }


  }
}
