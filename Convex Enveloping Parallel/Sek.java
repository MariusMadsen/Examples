class Sek{
  IntList list,remaingingInds;
  int[] x,y;
  int MAX_X,MAX_Y,MIN_Y,MIN_X;
  Sek(int[] x,int[] y,IntList remaingingInds){
    this.list = new IntList();//this will contain the final list
    this.remaingingInds = remaingingInds;
    this.x = x;
    this.y = y;
    conv();
  }
  void conv(){
    int p1= 0,p2 = 0;//to find the uttermost x values on both sides
    MIN_X = x[0];
    MAX_X = x[0];
    MIN_Y = y[0];
    MAX_Y = y[0];
    for (int i = 1;i<x.length;i++){//finding max and min values
      if(x[i]<MIN_X){
        MIN_X = x[i];
        p1 = i;
      }
      if(x[i]>MAX_X){
        MAX_X = x[i];
        p2 = i;
      }
      if(y[i]<MIN_Y){
        MIN_Y = y[i];
      }
      if(y[i]>MAX_Y){
        MAX_Y = y[i];
      }
    }
    list.add(p1);//adding the point to left first
    getPoints(p1,p2,0,remaingingInds,0);//last index represents which half it is looking throug, 0 being bottom and 1 top
    list.add(p2);//after first getpoints finishes, every point in bottom half except p2 is added
    getPoints(p1,p2,0,remaingingInds,1);//adds top bart points in order
  }
  void getPoints(int minI, int maxI, int pointind,IntList remaingingInds, int side){
    int point = 0;
    int temp;
    boolean fant = false;//boolean to keep track of if it found new points
    IntList online = new IntList();
    IntList newList = new IntList();
    for(int j = 0;j<remaingingInds.size();j++){
      int i = remaingingInds.data[j];//remainingInds keeps track of indesxes to look through
      temp = (y[minI]-y[maxI])*x[i] + (x[maxI]-x[minI])*y[i] + y[maxI]*x[minI] - y[minI]*x[maxI];
      if(minI == i || maxI == i){
        continue;//skips if the indexes is the uttermost points;
      }
      if(side != 0){
        temp = -temp;//side != 0 means it is looking through the top part. therefore simply changing the sign will make it check opposite side
      }
      if(temp<0){
        newList.add(i);//new list stores all valuables that need to be reevaluated in next
      }
      if(temp <point){
        fant = true;
        point = temp;
        pointind = i;
      }
      else if(temp == point){
        online.add(i);//if the point is on line it will be added to this to later be sorted with insertion.
      }
    }
    if(fant && side == 0){//bottomhalf
      getPoints(minI,pointind,pointind,newList,side);
      list.add(pointind);
      getPoints(pointind,maxI,pointind,newList,side);
    }
    else if(fant){//tophalf
      getPoints(pointind,maxI,pointind,newList,side);
      list.add(pointind);
      getPoints(minI,pointind,pointind,newList,side);
    }
    else if(online.size() != 0){
      checkline(online,side);
    }
  }
  void checkline(IntList online,int side){
    //insertion sort
    int t,k;
    for(int i = 0;i<online.size()-1;i++){
      t = online.data[i+1];
      k = i;
      while(k>=0 && x[online.data[k]]>x[t]){
        online.data[k+1] = online.data[k];
        k--;
      }
      online.data[k+1]=t;
    }
    if(side == 0){
      for(int i = 0; i< online.size();i++){
        list.add(online.data[i]);
      }
    }
    else{
      for(int i = online.size()-1; i>= 0;i--){
        list.add(online.data[i]);
      }
    }
  }
}
