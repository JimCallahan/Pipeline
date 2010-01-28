// $Id: TimeChecker.java,v 1.2 2009/06/24 02:42:36 jim Exp $

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   W A I T   L O C K E R                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Test of how read/write locks block each other.
 */ 
public 
class WaitLocker
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
	  ("usage: WaitLocker fair|unfair num-threads iterations\n" + 
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

      LinkedList<WaitLocker> tasks = new LinkedList<WaitLocker>();
      Random rand = new Random(System.currentTimeMillis()); 
      int wk; 
      for(wk=0; wk<numThreads; wk++) {
        WaitLocker task = new WaitLocker(wk, iterations, lock, wk == 0);
        task.start();
        tasks.add(task);
      }

      for(WaitLocker task : tasks) {
        try {
          System.out.print("Waiting for " + task.getTitle() + " Thread to Finish...\n");
          task.join();
        }
        catch(InterruptedException ex) {
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
  WaitLocker
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
        }
      }
    
      System.out.print(getStamp() + "  Acquiring " + getTitle() + "...\n"); 

      if(pIsWrite) 
        pLock.writeLock().lock();
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
  }

  private int                     pID; 
  private int                     pIter;
  private ReentrantReadWriteLock  pLock;
  private boolean                 pIsWrite;
  private Random                  pRandom; 
}


