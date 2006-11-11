// $Id: TestQueueOpsApp.java,v 1.1 2006/11/11 20:44:09 jim Exp $

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;
import us.temerity.pipeline.glue.*;

import java.io.*; 
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   T E S T   o f   Q U E U E   O P S                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class TestQueueOpsApp
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
      TestQueueOpsApp app = new TestQueueOpsApp(args);
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
  TestQueueOpsApp
  (
   String[] args 
  ) 
  {
    pJobIDs = new TreeSet<Long>();
    int wk;
    for(wk=0; wk<args.length; wk++) 
      pJobIDs.add(Long.parseLong(args[wk]));
  }

  public void 
  run() 
    throws PipelineException, InterruptedException
  {
    QueueMgrClient qclient = new QueueMgrClient();

    System.out.print("Pausing Jobs...\n");
    qclient.pauseJobs(pJobIDs);

    Thread.sleep(15000);
    System.out.print("Resuming Jobs...\n");
    qclient.resumeJobs(pJobIDs);
    
    Thread.sleep(15000);
    System.out.print("Killing Jobs...\n");
    qclient.killJobs(pJobIDs);
  }

  
  private TreeSet<Long>  pJobIDs;
}
