import csv
import random
import numpy as np
import matplotlib.pyplot as plt
import time
from itertools import *

with open("european_cities.csv", "r") as f:
    data = list(csv.reader(f, delimiter=';'))

def perms(n):
    s = []
    res = []
    # make a list containing every number from 2 to n. by not adding the first city we reduce the calculation time
    # to (n-1)!. we dont need n! because that would produce n times more answers that are basically the same just shifted
    for i in range(2,n+1):
        s.append(i)
    count = 2;
    # since we know that permutations' output is given in a systematic way we can figure out that
    # for every permutations where the last number is bigger than the first. we have already generated a backwards version
    # and every backwards version of a permuation will gave the same answer
    for i in (permutations(s,n-1)):
        if(count != i[0]):
            count+=1
        if count < i[len(i)-1]:
            #adding the first city in front and back, then we will have a functional route for the salesman
            res.append(list(('1',)+i+('1',)))
    return res

def hillperms(n, draw):
    s=[]
    res = []
    for i in range(2,n+1):
        s.append(i)
    #shuffles the original permutations draw times to give a random starting point for hillclimber and genetic to run
    for i in range(draw):
        temp = s
        random.shuffle(temp)
        res.append([1]+temp+[1])
    return res

#reads the permuation into the data file to figure out the distance
def calc(lst,i,n):
        j = 0
        dist = 0.0
        while(j < n-1):
            fra = int(i[j])
            til = int(i[j+1])
            dist += float(lst[fra][til-1])
            j+=1
        return dist

def exhaust(lst,perms):
    best = float("inf")
    n = len(perms[0])
    #checks every alternative that perms have generated
    for i in perms:
        dist = calc(lst,i,n)
        if dist < best:
            best = dist
            r = i
        res = (best,r)
    return res

def printroad(data,perm):
    str = ""
    for i in perm:
        str += data[0][int(i)-1]+"->"
    str = str[:-2]
    print "Vei: "+str


def rec(data,perm,start,end):
    dist = calc(data,perm,len(perm))
    bestdist = dist
    #using the [:] to get a new version of the list that is not allocated at the same place os the original list
    bestperm = perm[:]
    #switches the place between the start and i value to check if result is better, then lastly keeping the best result with the permutation
    for i in range(start+1,end):
        temp = perm[:]
        tempindex = temp[start]
        temp[start] = temp[i]
        temp[i] = tempindex
        tempdist = calc(data,temp,len(perm))
        if(bestdist>tempdist):
            bestperm = temp[:]
            bestdist = tempdist
    if(start+1 < end):
        bestperm = rec(data,bestperm,start+1,end)
    return bestperm

def stat(s):
    print "max value: ",max(s)
    print "min value: ",min(s)
    mean = 0
    for i in s:
        mean += i
    mean = mean/len(s)
    print "mean value: ",mean
    sum = 0
    for i in s:
        sum += (i-mean)**2
    sum = np.sqrt(sum/(len(s)-1))
    print "standard deviation: ",sum

def hill(data, perm, draw):
    res = []
    for i in range(draw):
        temp = rec(data,perm[i],1,len(perm[i])-1)
        res.append(calc(data,temp,len(temp)))
    return res

def inversionmutate(perm):
    x = random.randint(1,len(perm)-2)
    y = random.randint(x,len(perm)-2)
    res = []
    #adds the first numbers that are not being inversed, then the inverse, then rest of the elements
    for i in range(x):
        res.append(perm[i])
    for i in range(y,x-1,-1):
        res.append(perm[i])
    for i in range(y+1,len(perm)):
        res.append(perm[i])
    return res

def insertmutate(perm):
    length = len(perm)
    a = random.randint(1,length-2)
    b = random.randint(1,length-2)
    temp = perm[:]
    c = temp.pop(a)
    temp.insert(b,c)
    return temp

def pmxfill(perm1,perm2,perm,ind):
    for i in range(1,len(perm)-1):
        #checks what number is used instead of perm2 by index, then looks for the spot that number is stored in perm 2
        #to check if that spot is free
        #if not run recursive on that number
        if(perm2[i] == ind):
            if(perm[i] == 0):
                return i
            else:
                 return pmxfill(perm1,perm2,perm,perm1[i])

#decided to use pmx since it does not have any risk of adding multiple of the same number in the new result
#which suits perfect for permutations
def pmx(perm1, perm2):
    n = len(perm1)-1
    #picks a start to finish to add from first permutation
    randstart = random.randint(1,n-2)
    randend = random.randint(randstart+1,n-1)
    perm = [0] * (n+1)
    for i in range(randstart,randend+1):
        perm[i] = perm1[i]
    #needs to check if the number has already been added by permutation 1
    for i in range(randstart,randend+1):
        hasBeen = False
        for j in range(randstart,randend+1):
            if(perm2[i] == perm1[j]):
                hasBeen = True
                break
        #if not used he uses the pmxalgoritm which is made in pmxfill to find the suitable spot for him
        if(hasBeen == False):
            k = pmxfill(perm1,perm2,perm,perm1[i])
            perm[k] = perm2[i]
    #lastly adds the elements that are not yet used
    for i in range(0,n+1):
        if(perm[i] == 0):
            perm[i] = perm2[i]
    return perm

def parentSel(data,s):
    #using ranked selection to distribute chance of getting picked
    Sum = float(sum(range(len(s))))
    sel = []
    for i in s:
        sel.append(i[:])
    for i in range(len(s)):
        res = calc(data,s[i],len(s[i]))
        sel[i].append(res)
    #sorts the list based on distance values in a descending order
    sel.sort(key=lambda l:l[-1], reverse=True)
    j = 0
    #adds ranks to each permutation based on fitness (distance)
    for i in sel:
        i.append(j)
        j+=1
    #adds a percantage based on rank, and adds percantage of previous
    sel[0].append(sel[0][-1]/Sum)
    for i in range(1,len(sel)):
        sel[i].append(sel[i][-1]/Sum+sel[i-1][-1])
    x =  random.uniform(0,1)
    j= 0
    while(sel[j+1][-1] < x):
        j+=1
    return s[j]

def gen(data,n,pop,gen,pMut,pPMX,elite,x):
    s = hillperms(n,pop)
    nMut = int(round(pop*pMut))
    nPMX = int(round(pop*pPMX))
    for k in range(gen):
        offspring = []
        for i in range(nMut):
            kid = inversionmutate(parentSel(data,s))
            offspring.append(kid)
        for i in range(nPMX):
            kid =  pmx(parentSel(data,s),parentSel(data,s))
            offspring.append(kid)
        stemp = s[:]
        for j in range(elite):
            best = calc(data,stemp[0],len(stemp[0]))
            permtemp = stemp[0]
            for i in range(1,len(stemp)):
                temp = calc(data,stemp[i],len(stemp[i]))
                if(temp<best):
                    best = temp
                    permtemp = stemp[i]
            offspring.append(permtemp)
            stemp.remove(permtemp)
        for i in range(pop-nMut-nPMX-elite):
            temp = parentSel(data,stemp)
            offspring.append(temp)
            stemp.remove(temp)
        s = offspring[:]
        best = calc(data,s[0],len(s[0]))
        for i in range(1,len(s)):
            temp = calc(data,s[i],len(s[i]))
            if(temp<best):
                best = temp
        x[k] += best
    return s


n = 6
n2 = 10
for i in range(n,n2+1):
    time0 = time.time()
    s = perms(i)
    best = exhaust(data,s)[0]
    res = exhaust(data,s)[1]
    print "Exhaustive search for ",i, " cities"
    print best
    time1 = time.time()
    print "Time for ",i," cities: ",time1-time0,"\n"
printroad(data,res)
print ""


draw = 20
time0 = time.time()
s = hillperms(10,draw)
res = hill (data,s,draw)
print "Hillclimbing with 10 cities"
stat(res)
time1 = time.time()
print "Time: ",time1-time0,"\n"
time0 = time.time()
s = hillperms(24,draw)
res = hill(data,s,draw)
print "Hillclimbing with 24 cities"
stat(res)
time1 = time.time()
print "Time: ",time1-time0,"\n"

# my program assumes that you make an offspring or else the whole thing will crash
# also no offspring would result in a not so good genetic algorithm wouldnt it?
#use values between 0 and 1 to decid how much of a percent the new generation will be entirely of
#offspring. also dont make pmut +ppmx > 1, this will result in making more kids than the population contains
pmut = 0.25
ppmx = 0.25
pop = [25,50,100]
cities = [10,24]
generations = 50
runs = 20
x =[0]*generations
y = np.linspace(0,generations,generations)
for k in cities:
    for j in pop:
        for l in range(runs):
            time1 = time.time()
            #makes sure that 10 percent of the best parents survive the generation
            elite = int(1/10.*j)
            res = gen(data,k,j,generations,pmut,ppmx,elite,x)
            res2 = []
            for i in res:
                res2.append(calc(data,i,len(i)))
            time0 = time.time()-time1
        for l in range(len(x)):
            x[l] = x[l]/runs
        plt.plot(y,x)
        print "Genetic Algorithm with ",k," cities and a population of ",j
        stat(x)
        print "Time: ",time0, "\n"
    plt.title("Average result per generation")
    plt.xlabel("Generations")
    plt.ylabel("Distance")
    plt.legend(pop)
    plt.show()
