// $Id: TimeChecker.java,v 1.2 2009/06/24 02:42:36 jim Exp $

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   W A I T   T R Y   L O C K E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Test of how read/write locks block each other.
 */ 
public 
class WaitTryLocker
  extends Thread
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The top-level function.
   */ 
  public static void 
  main
  (
   String[] args 
  )
  {
    try {
      if(args.length != 3) {
	System.out.print
	  ("usage: WaitTryLocker fair|unfair num-threads iterations\n" + 
	   "\n" + 
	   "  Test of how read/write locks block each other."); 
	System.exit(1);
      }

      boolean isFair = args[0].equals("fair");
      int numThreads = Integer.parseInt(args[1]);
      int iterations = Integer.parseInt(args[2]);

      ReentrantReadWriteLock lock = new ReentrantReadWriteLock(isFair);

      System.out.print
        ("------------------------------------------------\n" + 
         "  " + (isFair ? "Fair" : "Unfair") + " Requested Policy\n" +
         "  " + (lock.isFair() ? "Fair" : "Unfair") + " Actual Policy\n" +
         "  1 Write Lock, " + (numThreads-1) + " Read Locks\n" + 
         "  " + iterations + " Iterations\n" +
         "------------------------------------------------\n");

      LinkedList<WaitTryLocker> tasks = new LinkedList<WaitTryLocker>();
      Random rand = new Random(System.currentTimeMillis()); 
      int wk; 
      for(wk=0; wk<numThreads; wk++) {
        WaitTryLocker task = new WaitTryLocker(wk, iterations, lock, wk == 0);
        task.start();
        tasks.add(task);
      }

      for(WaitTryLocker task : tasks) {
        try {
          System.out.print("Waiting for " + task.getTitle() + " Thread to Finish...\n");
          task.join();
        }
        catch(InterruptedException ex) {
          System.err.print(Exceptions.getFullMessage("Interrupted!", ex) + "\n");
        }
      }
    }
    catch(Exception ex) {
      System.err.print(Exceptions.getFullMessage(ex) + "\n");
      System.exit(1);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   T A S K S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  public 
  WaitTryLocker
  (
   int id, 
   int iter, 
   ReentrantReadWriteLock  lock, 
   boolean isWrite
  ) 
  {
    super("Lock[" + id + "]"); 

    pID = id;
    pIter = iter; 
    pLock = lock; 
    pIsWrite = isWrite;
    pRandom = new Random(System.currentTimeMillis()*id); 
  }

  public int
  getID() 
  {
    return pID; 
  }
  
  public String
  getTitle()
  {
    return ((pIsWrite ? "Write" : "Read") + getName()); 
  }

  public String
  getStamp() 
  {
    long now = System.currentTimeMillis(); 
    return (now + " " + TimeStamps.format(now));
  }

  public void 
  run() 
  {
    System.out.print(getStamp() + "  STARTED " + getTitle() + " THREAD.\n"); 

    int i;
    for(i=0; i<pIter; i++) {
      if(pIsWrite) {
        int nap = pRandom.nextInt(5000);
        System.out.print
          (getStamp() + "  Sleeping " + getTitle() + " for " + nap + "ms\n"); 
        try {
          Thread.sleep(nap); 
        }
        catch(InterruptedException ex) {
          System.err.print(Exceptions.getFullMessage("Interrupted!", ex) + "\n");
        }
      }
    
      System.out.print(getStamp() + "  Acquiring " + getTitle() + "...\n"); 

      if(pIsWrite) {
        ReentrantReadWriteLock.WriteLock wlock = pLock.writeLock();
        try {
          while(!(wlock.tryLock() || wlock.tryLock(1000, TimeUnit.MILLISECONDS))) 
            System.out.print(getStamp() + "  Failed to Acquire " + getTitle() + "\n"); 
        }
        catch(InterruptedException ex) {
          System.err.print(Exceptions.getFullMessage("Interrupted!", ex) + "\n");
        }
      }
      else 
        pLock.readLock().lock();      
      try {
        System.out.print(getStamp() + "    Locked " + getTitle() + "\n"); 
        
        {
          int nap = pRandom.nextInt(15000);
          System.out.print
            (getStamp() + "    Sleeping " + getTitle() + " for " + nap + "ms\n"); 
          try {
            Thread.sleep(nap); 
          }
          catch(InterruptedException ex) {
            System.err.print(Exceptions.getFullMessage("Interrupted!", ex) + "\n");
          }
        }
      }
      catch(Exception ex) {
        System.err.print(Exceptions.getFullMessage(ex) + "\n");
      }
      finally {
        if(pIsWrite) 
          pLock.writeLock().unlock();
        else  
          pLock.readLock().unlock();  
      
        System.out.print(getStamp() + "  Released " + getTitle() + "\n"); 
      }
    }

    System.out.print(getStamp() + "  FINISHED " + getTitle() + " THREAD.\n"); 
  }

  private int                     pID; 
  private int                     pIter;
  private ReentrantReadWriteLock  pLock;
  private boolean                 pIsWrite;
  private Random                  pRandom; 
}


