// $Id: PluginInputStream.java,v 1.3 2005/01/22 06:10:09 jim Exp $

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
    if(desc.getName().equals("us.temerity.pipeline.NodeCommon") ||
       desc.getName().equals("us.temerity.pipeline.QueueJob")) {       
      try {
	PluginMgrClient.getInstance().update();
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Warning,
	   ex.getMessage());
      }
    }

    return super.resolveClass(desc);
  }
}



