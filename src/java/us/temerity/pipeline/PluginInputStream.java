// $Id: PluginInputStream.java,v 1.12 2007/10/25 00:09:09 jim Exp $

package us.temerity.pipeline;

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   I N P U T   S T R E A M                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An object input stream which updates the loaded plugins before deserializing 
 * classes which have fields containing Pipeline plugins. <P> 
 * 
 * The {@link NodeCommon NodeCommon} and {@link QueueJob QueueJob} classes both contain
 * Action plugin instances which should be instantiated using the latest version of the 
 * plugin.  This class performs this plugin update only once for each network request
 * regardless of the number of instances of the Action plugin classes are contained in the 
 * request.  This single update also insures that all instances of an Action within a 
 * single request are of the same loaded class.
 */ 
public
class PluginInputStream
  extends ObjectInputStream
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  	
  /**
   * Creates an ObjectInputStream that reads from the specified InputStream.
   */ 
  public 
  PluginInputStream
  (
   InputStream in
  )
    throws IOException
  {
    super(in);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   I N P U T   S T R E A M   O V E R R I D E S                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Load the local class equivalent of the specified stream class description. 
   */ 
  protected Class<?> 
  resolveClass
  (
   ObjectStreamClass desc
  )
    throws IOException, ClassNotFoundException
  {
    String name = desc.getName();
    int wk;
    for(wk=0; wk<sClassNames.length; wk++) {
      if(name.equals(sClassNames[wk])) {
	try {
	  PluginMgrClient.getInstance().update();
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Warning,
	     ex.getMessage());
	}

	break;
      }
    }

    return super.resolveClass(desc);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String sClassNames[] = {
    "us.temerity.pipeline.Archive",
    "us.temerity.pipeline.NodeBundle",
    "us.temerity.pipeline.NodeCommon",
    "us.temerity.pipeline.NodeDetails",
    "us.temerity.pipeline.MasterExtensionConfig",
    "us.temerity.pipeline.QueueExtensionConfig",
    "us.temerity.pipeline.QueueJob",
    "us.temerity.pipeline.SuffixEditor",
    "us.temerity.pipeline.message.NodeGetAnnotationRsp", 
    "us.temerity.pipeline.message.NodeGetAnnotationsRsp", 
    "us.temerity.pipeline.message.NodeAddAnnotationReq", 
    "us.temerity.pipeline.message.MiscArchiveReq",
    "us.temerity.pipeline.message.FileArchiveReq",
    "us.temerity.pipeline.message.MiscRestoreReq", 
    "us.temerity.pipeline.message.FileExtractReq",
    "us.temerity.pipeline.message.JobEditAsReq"
  };

}



