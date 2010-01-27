// $Id: TimeChecker.java,v 1.2 2009/06/24 02:42:36 jim Exp $

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   D E A D L O C K E R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */ 
public 
class DeadLocker
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
      if(args.length != 1) {
	System.out.print
	  ("usage: DeadLocker num-threads\n" + 
	   "\n" + 
	   "  Demonstrate the evil deadlocking loophole in try/catch/finally blocks!"); 
	System.exit(1);
      }

      int numThreads = Integer.parseInt(args[0]);

      ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

      LinkedList<DeadLocker> tasks = new LinkedList<DeadLocker>();

      int wk; 
      for(wk=0; wk<numThreads; wk++) {
        DeadLocker task = new DeadLocker(wk, lock, wk%3 == 0);
        task.start();
        tasks.add(task);
      }

      for(DeadLocker task : tasks) {
        try {
          System.out.print("Waiting for Task [" + task.getID() + "]...\n");
          task.join();
        }
        catch(InterruptedException ex) {
        }
      }
    }
    catch(Exception ex) {
      System.out.print("INTERNAL-ERROR:\n");
      ex.printStackTrace(System.out);
      System.exit(1);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   T A S K S                                                                            */
  /*----------------------------------------------------------------------------------------*/

  public 
  DeadLocker
  (
   int id, 
   ReentrantReadWriteLock  lock, 
   boolean isWrite
  ) 
  {
    super("DeadLocker[" + id + "]"); 

    pID = id;
    pLock = lock; 
    pIsWrite = isWrite;
  }

  public int
  getID() 
  {
    return pID; 
  }
  
  public void 
  run() 
  {
    System.out.print
      ("Acquiring " + (pIsWrite ? "Write" : "Read") + "Lock [" + pID + "]...\n"); 

    if(pIsWrite) 
      pLock.writeLock().lock();
    else 
      pLock.readLock().lock();      
    try {
      System.out.print("Locked [" + pID + "]...\n"); 
      
      try {
        Thread.sleep(pID*1000); 
      }
      catch(InterruptedException ex) {
      }
      
      throw new PipelineException("Something is busted..."); 
    }
    catch(Exception ex2) {
      Integer foo = null;
      foo.toString();
    }
    finally {
      Integer bar = null;
      bar.toString();

      if(pIsWrite) 
        pLock.writeLock().unlock();
      else  
        pLock.readLock().unlock();  

      System.out.print
        ("Released " + (pIsWrite ? "Write" : "Read") + "Lock [" + pID + "]...\n");  
    }
  }

  private int  pID; 
  private ReentrantReadWriteLock  pLock;
  private boolean  pIsWrite;
}


