// $Id: TestReadWriteLockApp.java,v 1.1 2004/03/07 02:47:50 jim Exp $

import us.temerity.pipeline.*;

import java.util.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   T E S T   R E A D   W R I T E   L O C K                                                */
/*------------------------------------------------------------------------------------------*/

public
class TestReadWriteLockApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   M A I N                                                                              */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args 
  )
  {
    try {
      TestReadWriteLockApp app = new TestReadWriteLockApp();
      app.run();
    } 
    catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    } 
 
    System.exit(0);
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  TestReadWriteLockApp()
  { 
    pLock     = new ReentrantReadWriteLock();
    pValue    = 1000000;
    pMaxValue = 1000010;
    pFactors  = new ArrayList<Long>();
  }
  
  public void 
  run() 
  {
    ArrayList<Thread> threads = new ArrayList<Thread>();

    int wk;
    for(wk=0; wk<5; wk++) {

      int rk;
      for(rk=0; rk<20; rk++) {
	ReadThread thread = new ReadThread();
	threads.add(thread);
	thread.start();
      }
      
       WriteThread thread = new WriteThread();
       threads.add(thread);
       thread.start();
    }

    for(Thread thread : threads) {
      try {
	thread.join();
      }
      catch(InterruptedException ex) {
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected class 
  BaseThread
    extends Thread
  {
    BaseThread
    (
     String title
    )
    {
      pTitle = title;

      pRandom = new Random();
      
      pTotalWait = 0;
      pMaxWait   = 0;
      pWaitCnt   = 0;
    }

    public void 
    nap()
    {
      try {
	int nap = pRandom.nextInt(100);
// 	System.out.print("ReadThread [" + Thread.currentThread().getId() + 
// 			 "] sleeping for (" + nap + " msec)\n");
	sleep(nap);
      }
      catch(InterruptedException ex) {
      }
    }

    public void 
    printData() 
    {
     //  StringBuffer buf = new StringBuffer();

//       buf.append(pTitle + "Thread [" + Thread.currentThread().getId() + "]:\n" + 
// 		 "  Value   = " + pValue + "\n" + 
// 		 "  Factors = ");
//       for(Long factor : pFactors) 
// 	buf.append(factor + " ");
      
//       System.out.print(buf.toString() + "\n");
    }

    public void 
    printWaitStats() 
    {
      System.out.print(pTitle + "Thread [" + Thread.currentThread().getId() + "]:\n" + 
		       "  Average Wait = " + (pTotalWait / pWaitCnt) + "\n" + 
		       "  Maximum Wait = " + pMaxWait + "\n");
    }

    public void 
    updateWait
    (
     long wait
    ) 
    {
     //  System.out.print(pTitle + "Thread [" + Thread.currentThread().getId() + 
// 		       "] waited for (" + wait + " msec) to aquire the lock.\n");

      pTotalWait += wait;
      pMaxWait = Math.max(pMaxWait, wait);
      pWaitCnt++;
    }
    
 
    private String  pTitle;
    private Random  pRandom;

    private long    pTotalWait;
    private long    pMaxWait;
    private long    pWaitCnt;
  }



  protected class
  ReadThread 
    extends BaseThread
  {
    ReadThread()
    {
      super("Read");
    }

    public void 
    run() 
    {
      while(true) {
	nap();

	/* read and print the data */ 
	long start = (new java.util.Date()).getTime();
	pLock.readLock().lock();
	try {
	  updateWait((new java.util.Date()).getTime() - start);
	  printData();

	  if(pValue > pMaxValue) {
	    printWaitStats();
	    return;
	  }
	}
	finally {
	  pLock.readLock().unlock();
	}
      }
    }
  }


  protected class
  WriteThread 
    extends BaseThread
  {
    WriteThread()
    {
      super("Write");
    }

    public void 
    run() 
    {
      while(true) {
	nap();

	/* update and print the data */ 
	long start = (new java.util.Date()).getTime();
	pLock.writeLock().lock();
	try {
	  updateWait((new java.util.Date()).getTime() - start);

	  /* increment pValue and compute its factors */ 
	  ArrayList<Long> factors = null;
	  {
	    pValue++;

	    pFactors = new ArrayList<Long>();
	    long factor;
	    for(factor=1; factor<=(pValue/2); factor++) {
	      if((pValue % factor) == 0) 
		pFactors.add(factor);
	    }
	    pFactors.add(pValue);
	  }
	  
	  printData();
	  
	  if(pValue > pMaxValue) {
	    printWaitStats();
	    return;
	  }
	}
	finally {
	  pLock.writeLock().unlock();
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private ReentrantReadWriteLock pLock;
  private long             pValue;
  private long             pMaxValue;
  private ArrayList<Long>  pFactors;

}


