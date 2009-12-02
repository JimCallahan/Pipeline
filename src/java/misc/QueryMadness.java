// $Id: QueryMadness.java,v 1.1 2009/12/02 01:05:49 jim Exp $

import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E R Y   M A D N E S S                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Genrates extreme amounts of garbage on the Master Manager by running non-stop and even 
 * parallel archive queries for every node in the database.
 */ 
public class
QueryMadness
{
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   M E T H O D S                                                          */
  /*----------------------------------------------------------------------------------------*/

  public static void 
  main
  (
   String[] args  
  )
  {
    boolean success = false;
    try {
      if(args.length != 2) 
        usage(); 

      int numThreads = new Integer(args[0]);
      int numQueries = new Integer(args[1]);

      new QueryMadness(numThreads, numQueries);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));
    }
    finally {
      LogMgr.getInstance().cleanup();
    }

    System.exit(success ? 0 : 1);
  }

  private static void 
  usage()
    throws PipelineException
  {
    throw new PipelineException
      ("usage: QueryMadness num-threads queries-per-thread\n" + 
       "\n" +
       "Genrates extreme amounts of garbage on the Master Manager by running non-stop " +
       "and even parallel (num-threads) archive queries for every node in the database."); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new instance.
   **/
  public 
  QueryMadness
  (
   int numThreads, 
   int queries
  )
  {
    LinkedList<Worker> threads = new LinkedList<Worker>();

    int id;
    for(id=0; id<numThreads; id++) {
      Worker worker = new Worker(id, queries);
      threads.add(worker);
      worker.start();
    }

    for(Worker worker : threads) {
      try {
        worker.join();
      }
      catch(InterruptedException ex) {
      }
    }
    
    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       "All Done."); 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private class
  Worker
    extends Thread
  {
    public 
    Worker
    (
     int threadID, 
     int numQueries
    )
    {
      super("QueryMadness:Worker"); 

      pThreadID   = threadID;
      pNumQueries = numQueries;
    }

    @Override
    public void 
    run() 
    {
      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Info,
         "Thread [" + pThreadID + "]: Starting..."); 

      try {
        Random rand = new Random(System.currentTimeMillis()%pThreadID);
        sleep(rand.nextInt(1000));
      }
      catch(InterruptedException ex) {
      }

      MasterMgrClient client = new MasterMgrClient(); 
      try {
        int wk;
        for(wk=0; wk<pNumQueries; wk++) {
          LogMgr.getInstance().logAndFlush
            (LogMgr.Kind.Ops, LogMgr.Level.Info,
             "Thread [" + pThreadID + "]: Query " + wk);
          client.archiveQuery(null, 1000);
        }
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Ops, LogMgr.Level.Severe,
           ex.getMessage());
      }
      finally {
        client.disconnect(); 
      }

      LogMgr.getInstance().logAndFlush
        (LogMgr.Kind.Ops, LogMgr.Level.Info,
         "Thread [" + pThreadID + "]: Done."); 
    }

    private int pThreadID; 
    private int pNumQueries; 
  }
}
