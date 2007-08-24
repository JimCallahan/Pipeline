package us.temerity.pipeline.builder.maya2mr.v2_3_2;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D S   S H O T   N A M E S                                                      */
/*------------------------------------------------------------------------------------------*/

public 
interface BuildsShotNames
{
  public String
  getMovieName();
  
  public String
  getSequenceName();
  
  public String
  getShotName();
  
  /*---Layout--------------------------------------------------------------------------------*/
  
  /**
   * The node with the animated camera in it.
   */
  public String
  getCameraNodeName();
  
  /**
   * The layout node that a user will edit to create the layout.
   */
  public String
  getLayoutEditNodeName();
  
  /**
   * A copy of the layout node that is going to be used to create the animation scene. 
   */
  public String
  getLayoutProductNodeName();

  /**
   * A node containing all of the exported animation for the layout. 
   */
  public String
  getLayoutExportPrepareNodeName();
  
  /**
   * A node containing a copy of the exported animation from the layout for creating the
   * animation scene. 
   */
  public String
  getLayoutExportProductNodeName();
  
  /**
   * A node containing the exported animation from the layout for a particular character. 
   */
  public String
  getLayoutExportPrepareNodeName
  (
    String assetName  
  );
  
  
  /**
    * A node containing a copy of the exported animation from the layout for creating the
   * animation scene. 
   */
  public String
  getLayoutExportProductNodeName
  (
    String assetName  
  );
  
  
  /**
   * The node where the layout animation is reapplied to test the exported animation
   * and create a render for approval. 
   */
  public String
  getLayoutBuildNodeName();
  
  /**
   * The rendered (or playblasted) images for layout submission. 
   */
  public String
  getLayoutImgNodeName();
  
  /**
   * The thumbnail image for the playblast images. 
   */
  public String
  getLayoutThumbNodeName();
  
  /**
   * The layout submission node.
   */
  public String
  getLayoutSubmitNodeName();

  /**
   * The layout approve node. 
   */
  public String
  getLayoutApproveNodeName();

  /*---Anim----------------------------------------------------------------------------------*/

  /**
   * A node per asset that represents source motion capture data being brought in as
   * FBX files.
   */
  public String
  getAnimSourceFBXNodeName
  (
    String assetName
  );
  
  /**
   * A node per asset that represents the source FBX data converted into maya
   * animation curves.
   */
  public String
  getAnimSourceCurvesNodeName
  (
    String assetName
  );
  
  /**
   * The node that a user will edit to create the animation or the node that is build from
   * component curve files if the animation is all coming from fbx files.
   */
  public String
  getAnimEditNodeName();
  
  /**
   * A copy of the animation node that is going to be used to create the switch lighting scene. 
   */
  public String
  getAnimProductNodeName();
  
  /**
   * A node containing all of the exported animation from the animation scene. 
   */
  public String
  getAnimExportPrepareNodeName();

  /**
   * A node containing all of the exported animation from the animation scene that is
   * used to build the prelight scene. 
   */
  public String
  getAnimExportProductNodeName();

  /**
   * The node where the animation is reapplied to test the exported animation
   * and create a render for approval. 
   */
  public String
  getAnimBuildNodeName();
  
  /**
   * A render (or playblast) of the animation that is submitted for approval. 
   */
  public String
  getAnimImgNodeName();
  
  /**
   * A thumbnail of the images.
   */
  public String
  getAnimThumbNodeName();
  
  /**
   *  The submit node for the animation task. 
   */
  public String
  getAnimSubmitNodeName();
  
  /**
   *  The approve node for the animation task. 
   */
  public String
  getAnimApproveNodeName();
  
  /**
   * A node containing one asset's exported animation from the animation scene. 
   * 
   * @param assetName
   *   The asset that this node will correspond to.
   */
  public String
  getAnimExportPrepareNodeName
  (
    String assetName
  );
  
  /**
   * A node containing one asset's exported animation from the animation scene that
   * is used to build the lighting scene and the individual mi exporting scenes. 
   * 
   * @param assetName
   *   The asset that this node will correspond to.
   */
  public String
  getAnimExportProductNodeName
  (
    String assetName
  );
  
  /**
   * A node built by combining exported animation with a single hi-rez model file for
   * exporting out mi files.
   * 
   * @param assetName
   *   The asset that this node will correspond to.
   */
  public String
  getAnimModelExportNodeName
  (
    String assetName
  );
  
  /**
   * A node containing the mi file data for one asset made from the animation and
   * the model. 
   * 
   * @param assetName
   *   The asset that this node will correspond to.
   */
  public String
  getGeoMiNodeName
  (
    String assetName
  );

  /**
   * A node containing the mi file data for the camera made from the animation scene.
   */
  public String
  getCameraMiNodeName();
  
  /**
   * A node built by applying exported animation to lighting models.  This node
   * is referenced by the lighting scene.
   */  
  public String
  getPreLightNodeName();
  
  /**
   * A scene specific gathering MEL script that is run on the prelight as it is being 
   * built.  Allows shot specific overrides. 
   */
  public String
  getPreLightMELNodeName();
  
  /**
   * The node which the artist edits to add lighting to the shot. 
   */
  public String
  getLightEditNodeName();
  
  /**
   *  The rendered images which are submitted for approval of the lighting scene. 
   */
  public String
  getLightImagesNodeName();
  
  /**
   * A thumbnail of the images.
   */
  public String
  getLightThumbNodeName();

  /**
   * The submit node for the lighting task.
   */
  public String
  getLightSubmitNodeName();
  
  /**
   * The approve node for the lighting task.
   */
  public String
  getLightApproveNodeName();
  
  /**
   * Generated on lighting approval.  This node is used to render all the final
   * render passes that depend upon lighting. 
   */
  public String
  getFinalLightNodeName();

  /**
   * The instance group nodes for each geometry mi that is coming from animation. 
   * 
   * @param assetName
   *   The asset that this node will correspond to.
   */
  public String
  getGeoInstMiNodeName
  (
    String assetName
  );
  
  /**
   * The mi file containing the light data from the lighting scene. 
   */
  public String
  getLightMiNodeName
  ();

  /**
   * The mi file containing the exported shaders for the specified asset
   * 
   * @param assetName
   *   The asset that this node will correspond to.
   */
  public String
  getShaderMiNodeName
  (
    String assetName
  );
  
  /**
   * The mi file containing the camera override mi file.
   */
  public String
  getCamOverrideMiNodeName();
  
  /**
   * The mi file containing the render options mi file.
   */
  public String
  getOptionsMiNodeName();
}