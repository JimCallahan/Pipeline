// $Id: AttachGeoCacheStage.java,v 1.2 2008/06/15 17:31:10 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A T T A C H   G E O   C A C H E   S T A G E                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the MayaAttachGeoCache action
 */ 
public 
class AttachGeoCacheStage 
  extends MELFileStage
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new stage.
   * 
   * @param stageInfo
   *   Class containing basic information shared among all stages.
   * 
   * @param context
   *   The {@link UtilContext} that this stage acts in.
   * 
   * @param client
   *   The instance of Master Manager that the stage performs all its actions in.
   * 
   * @param nodeName
   *   The name of the node that is to be created.
   * 
   * @param cacheName
   *   The name of the geometry cache file node. 
   * 
   * @param cacheChannel
   *   The name of the channel in the cache file to attach to the shape node.
   * 
   * @param geometryName
   *   The name of the Maya Shape node to attach to the cache.
   * 
   * @param postCacheMEL
   *   The name of the post attach MEL script. 
   */
  public
  AttachGeoCacheStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String cacheName, 
   String cacheChannel, 
   String geometryName, 
   String postCacheMEL
  )
    throws PipelineException
  {
    super("AttachGeoCache", 
	  "Creates a node which uses the MayaAttachGeoCache action.", 
	  stageInfo, context, client, 
	  nodeName, 
	  null, 
	  new PluginContext("MayaAttachGeoCache", "Temerity", 
			    new Range<VersionID>(new VersionID("2.4.3"), null))); 	

    addLink(new LinkMod(cacheName, LinkPolicy.Dependency));
    addSingleParamValue("CacheFile", cacheName); 

    addSingleParamValue("CacheChannel", cacheChannel); 
    addSingleParamValue("GeometryName", geometryName); 

    addLink(new LinkMod(postCacheMEL, LinkPolicy.Dependency));
    addSingleParamValue("PostCacheMEL", postCacheMEL); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -2749834843702753005L;

}
