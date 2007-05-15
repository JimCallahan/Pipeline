package us.temerity.pipeline.builder.stages;

import java.util.ArrayList;

import us.temerity.pipeline.FrameRange;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.names.BuildsAssetNames;

/**
 * A leaf stage used in the ShotBuilder that builds the animation node.
 * <P>
 * Builds the animation scene. Links all the models, the camera, sets the frame range, etc.
 */
public 
class ShotBuilderAnimStage
  extends MayaBuildStage
{
  public
  ShotBuilderAnimStage
  (
    UtilContext context, 
    MayaContext mayaContext,
    String nodeName,
    ArrayList<BuildsAssetNames> assets,
    String cameraNode,
    FrameRange range,
    boolean importCamera
  ) 
    throws PipelineException
  {
    super("ShotBuilderAnim", "Stage to build the animation scene", 
          context, mayaContext, 
          nodeName, true);
    if (range != null)
      setFrameRange(range);
    setUnits();
    if (cameraNode != null) {
      if (importCamera)
	setupLink(cameraNode, "camera", getImport(), true);
      else
	setupLink(cameraNode, "camera", getReference(), true);
    }
    for (BuildsAssetNames asset : assets) {
      String assetName = asset.getLowRezFinalNodeName();
      String namespace = asset.getNameSpace();
      setupLink(assetName, namespace, getReference(), true);
    }
  }
}
