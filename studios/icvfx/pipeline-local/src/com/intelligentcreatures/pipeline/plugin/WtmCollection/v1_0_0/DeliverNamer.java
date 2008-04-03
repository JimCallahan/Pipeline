// $Id: DeliverNamer.java,v 1.2 2008/04/03 01:34:28 jim Exp $

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
      Path path = new Path(pBasePaths.get(pTaskType, NodePurpose.Prepare), 
			   new Path(AppDirs.QuickTime.toDirPath(), "deliver"));
      pQtDeliverablePrepPath = new Path(path, pDeliverable); 
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
  /*   Q T   D E L I V E R                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved name of the node containing the Nuke script to read the 
   * images being delivered. 
   */ 
  public String
  getReadQtDeliverableImagesNode() 
  {
    Path path = new Path(pQtDeliverablePrepPath, "read_images"); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the slate Nuke script with all
   * shot and deliverable substitutions applied.
   */ 
  public String
  getQtDeliverSlateNukeNode() 
  {
    Path path = new Path(pQtDeliverablePrepPath, joinNames(pSlateFormat, "slate")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the composited images with 
   * slates and overlays applied to the deliverable images.
   */ 
  public String
  getQtDeliverSlatedImagesNode() 
  {
    Path path = new Path(pQtDeliverablePrepPath, 
			 new Path(AppDirs.Comp.toDirPath(), pSlateFormat));
    return path.toString(); 
  }

  /**
   * The QuickTime movie generated from the individual composited deliverable images.
   */ 
  public String
  getQtDeliverSlatedMovieNode() 
  {
    Path path = new Path(pQtDeliverablePrepPath, 
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
  getQtDeliverableNode() 
  {
    Path path = new Path(pBasePaths.get(pTaskType, NodePurpose.Deliver), 
			 new Path(AppDirs.QuickTime.toDirPath(), 
				  new Path(new Path(new Path(new Path(pSlatePrefix), 
							     pFormatPrefix), 
						    pCodecPrefix),
					   pDeliverable))); 
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
   * The path to the root node directory for all QuickTime deliverable related prepare nodes.
   */ 
  private Path pQtDeliverablePrepPath; 

}
