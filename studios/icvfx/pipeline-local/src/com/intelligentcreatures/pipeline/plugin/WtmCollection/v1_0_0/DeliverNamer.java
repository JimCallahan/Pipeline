// $Id: DeliverNamer.java,v 1.1 2008/04/02 20:56:16 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   D E L I V E R   N A M E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides the names of nodes and node directories for a specific deliverable from 
 * a specific shot.
 */
public 
class DeliverNamer 
  extends ShotNamer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new namer.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param studioDefs 
   *   Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  public 
  DeliverNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    StudioDefinitions studioDefs
  )
    throws PipelineException
  {
    super("DeliverNamer", 
          "Provides the names of nodes and node directories for a specific deliverable " + 
	  "from a specific shot.", 
          mclient, qclient, studioDefs); 
    
    {
      UtilityParam param =
        new StringUtilityParam
        (aDeliverable,
         "The short name of the deliverable.",
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (aTaskType,
         "The type of task which created the deliverable.",
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (aSlatePrefix,
         "The node name prefix of the master slate creation Nuke script to use.", 
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (aFormatPrefix,
         "The node name prefix of the final image formatting Nuke script to use.", 
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (aCodecPrefix,
         "The node name prefix of the QuickTime codec settings file to use.", 
         null);
      addParam(param);
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Intialize any internal datastructures used by the naming methods based on the values
   * of the namer's parameters. 
   */ 
  @Override
  public void 
  generateNames() 
    throws PipelineException 
  {
    super.generateNames(); 

    pDeliverable  = getStringParamValue(new ParamMapping(aDeliverable));
    pTaskType     = TaskType.valueOf(TaskType.class, 
				     getStringParamValue(new ParamMapping(aTaskType)));

    pFormatPrefix = getStringParamValue(new ParamMapping(aFormatPrefix));
    pSlatePrefix  = getStringParamValue(new ParamMapping(aSlatePrefix));
    pSlateFormat  = joinNames(pSlatePrefix, pFormatPrefix);

    pCodecPrefix  = getStringParamValue(new ParamMapping(aCodecPrefix));

    {
      Path path = new Path(pBasePaths.get(pTaskType, NodePurpose.Prepare), "deliver");  
      pDeliverablePrepPath = new Path(path, pDeliverable); 
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The short name of the deliverable.
   */ 
  public String 
  getDeliverable() 
  {
    return pDeliverable;
  }

  /**
   * The node name prefix of the master slate creation Nuke script.
   */ 
  public String 
  getSlatePrefix() 
  {
    return pSlatePrefix;
  }

  /**
   * The node name prefix of the final image formatting Nuke script.
   */ 
  public String 
  getFormatPrefix() 
  {
    return pFormatPrefix;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   Q U I C K T I M E   D E L I V E R Y                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved name of the node containing the Nuke script to read the 
   * images being delivered. 
   */ 
  public String
  getReadDeliverableImagesNode() 
  {
    Path path = new Path(pDeliverablePrepPath, "read_images"); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the slate Nuke script with all
   * shot and deliverable substitutions applied.
   */ 
  public String
  getSlateNukeNode() 
  {
    Path path = new Path(pDeliverablePrepPath, joinNames(pSlateFormat, "slate")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the composited images with 
   * slates and overlays applied to the deliverable images.
   */ 
  public String
  getSlatedDeliverableImagesNode() 
  {
    Path path = new Path(pDeliverablePrepPath, 
			 new Path(AppDirs.Comp.toDirPath(), pSlateFormat));
    return path.toString(); 
  }

  /**
   * The QuickTime movie generated from the individual composited deliverable images.
   */ 
  public String
  getSlatedDeliverableQtNode() 
  {
    Path path = new Path(pDeliverablePrepPath, 
			 new Path(AppDirs.Comp.toDirPath(), 
				  new Path(AppDirs.QuickTime.toDirPath(), pSlateFormat)));
    return path.toString(); 
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The QuickTime movie of the deliverable images encoded with client specific codec 
   * settings.
   */ 
  public String 
  getDeliverableNode() 
  {
    Path path = new Path(pBasePaths.get(pTaskType, NodePurpose.Deliver), 
			 new Path(new Path(new Path(new Path(pSlatePrefix), 
						    pFormatPrefix), 
					   pCodecPrefix),
				  pDeliverable)); 
    return path.toString(); 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1494359398223833515L;

  public static final String aDeliverable   = "Deliverable";  
  public static final String aTaskType      = "TaskType";
  public static final String aSlatePrefix   = "SlatePrefix"; 
  public static final String aFormatPrefix  = "FormatPrefix";
  public static final String aCodecPrefix   = "CodecPrefix";

   

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The short name of the deliverable.
   */ 
  private String pDeliverable; 

  /**
   * The type of task the builder is constructing.
   */ 
  protected TaskType pTaskType; 

  /**
   * The node prefix of the master slate creation Nuke script, final image formatting Nuke 
   * script and combined slate format name. 
   */ 
  private String pSlatePrefix; 
  private String pFormatPrefix; 
  private String pSlateFormat; 
  
  /**
   * The node prefix of the QuickTime codec settings node.
   */ 
  private String pCodecPrefix;

  /**
   * The path to the root node directory for all deliverable related prepare nodes.
   */ 
  private Path pDeliverablePrepPath; 

}
