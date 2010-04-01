// $Id: CanOffline.java,v 1.1 2009/12/11 01:24:23 jim Exp $

import java.util.*;
import java.io.*;
import java.text.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   I N S E R T   S I T E   V E R S I O N                                                  */
/*------------------------------------------------------------------------------------------*/

public class
InsertSiteVersion
{
  public static void 
  main
  (
   String[] args  
  )
  {
    boolean success = false;
    try {
      if(args.length != 1)
        usage(); 

      Path tarPath = new Path(args[0]); 

      PluginMgrClient.init(true);
      MasterMgrClient client = new MasterMgrClient();
      
      boolean isInserted = client.isSiteVersionInserted(tarPath); 
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Info,
         "IsInserted: " + isInserted); 

      client.insertSiteVersion(tarPath); 

      success = true;
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
      ("usage: InsertSiteVersion version.tar\n"  + 
       "\n" +
       "Insert a previously extracted node version into the database.\n\n"); 
  }
}
  
