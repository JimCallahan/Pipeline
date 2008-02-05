package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.UtilContext;

/*------------------------------------------------------------------------------------------*/
/*   D E F A U L T   S H O T   N A M E S                                                    */
/*------------------------------------------------------------------------------------------*/

public class DefaultShotNames
  extends BaseNames
  implements BuildsShotNames
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  DefaultShotNames
  (
    String project,
    boolean useMovie,
    boolean existingDirs,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderInfo
  ) 
    throws PipelineException
  {
    super("DefaultShotNames",
          "The basic naming class for a shot provided by Temerity",
          mclient,
          qclient);
    
    pBuilderInfo = builderInfo;
    pProject = project;
    pUseMovie = useMovie;
    pExistingDirs = existingDirs;
    
    if (existingDirs) {
      if (useMovie) {
	TreeMap<String, ArrayList<String>> values = getMoviesAndSequences(pProject);
	UtilityParam param =
	  new TreeMapUtilityParam
	  (aLocation, 
	   "The Movie and Sequence the shot should be located in.",
	   aMovieName,
	   "The Movie the created shot should be in.",
	   aSequenceName,
	   "The Sequence the created shot should be in.", 
	   values);
	addParam(param);
      }
      else {
	ArrayList<String> seqs = pBuilderInfo.getSequenceList(pProject, null);
	UtilityParam param =
	  new EnumUtilityParam
	  (aSequenceName,
	   "The Sequence the created shot should be in.",
	   seqs.get(0),
	   seqs);
	addParam(param);
      }
    }
    else {
      if (useMovie) {
	UtilityParam param =
	  new StringUtilityParam
	  (aMovieName,
	   "The Movie the created shot should be in.",
	   null);
	addParam(param);
      }
      UtilityParam param =
	new StringUtilityParam
	(aSequenceName,
	 "The Sequence the created shot should be in.",
	 null);
      addParam(param);
    }
    {
      UtilityParam param =
	new StringUtilityParam
	(aShotName,
	 "The name of the shot to be created.",
	 null);
      addParam(param);
    }
    
    {
      UtilityParam param =
	new BooleanUtilityParam
	(aApprovalFormat,
	 "Is this namer being used in the approval format?",
	 true);
      addParam(param);
    }
  }
  
  protected TreeMap<String, ArrayList<String>>
  getMoviesAndSequences
  (
    String project
  ) 
    throws PipelineException
  {
    TreeMap<String, ArrayList<String>> toReturn = new TreeMap<String, ArrayList<String>>();
    for (String movie : pBuilderInfo.getMovieList(project)) {
      toReturn.put(movie, pBuilderInfo.getSequenceList(project, movie));
    }
    return toReturn;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void generateNames()
    throws PipelineException
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    pApprovalMode = getBooleanParamValue(new ParamMapping(aApprovalFormat));
    
    if (pExistingDirs) {
      if (pUseMovie) {
	pMovieName = 
	  getStringParamValue(new ParamMapping(aLocation, ComplexParam.listFromObject(aMovieName)));
	pSeqName = 
	  getStringParamValue(new ParamMapping(aLocation, ComplexParam.listFromObject(aSequenceName)), false);
      }
      else
	pSeqName = getStringParamValue(new ParamMapping(aSequenceName));
    }
    else {
      if (pUseMovie)
	pMovieName = getStringParamValue(new ParamMapping(aMovieName));
      pSeqName = getStringParamValue(new ParamMapping(aSequenceName), false);
    }
    
    pShotName = getStringParamValue(new ParamMapping(aShotName), false);
    
    pNamePrefix = pSeqName + "_" + pShotName + "_";
    
    if (pUseMovie && pMovieName == null)
      throw new PipelineException
        ("The Movie Name parameter cannot be null if Use Movie has been activated");
    
    if (pMovieName != null)
      pShotPath = 
	new Path(new Path(new Path(new Path("/projects/" + pProject + "/prod/"), pMovieName), pSeqName), pShotName);
    else
      pShotPath = 
	new Path(new Path(new Path("/projects/" + pProject + "/prod/"), pSeqName), pShotName);
    
    String edit = getEditDirectory();
    String submit = getSubmitDirectory();
    String approve = getApproveDirectory();
    String prepare = getPrepareDirectory();
    String product = getProductDirectory();
    String thumb = getThumbnailDirectory();
    String source = getSourceDirectory();
    
    if (pApprovalMode);
    {
      pLayStart    = new Path(pShotPath, "anim");
      pLayEdit     = new Path(pLayStart, edit);
      pLaySubmit   = new Path(pLayStart, submit);
      pLayPrepare  = new Path(pLaySubmit, prepare);
      pLayApprove  = new Path(pLayStart, approve);
      pLayProduct  = new Path(pLayApprove, product);
      pLayThumb    = new Path(pLaySubmit, thumb);
      
      pAnimStart   = new Path(pShotPath, "anim");
      pAnimEdit    = new Path(pAnimStart, edit);
      pAnimSource  = new Path(pAnimStart, source);
      pAnimSubmit  = new Path(pAnimStart, submit);
      pAnimPrepare = new Path(pAnimSubmit, prepare);
      pAnimApprove = new Path(pAnimStart, approve);
      pAnimProduct = new Path(pAnimApprove, product);
      pAnimThumb   = new Path(pAnimSubmit, thumb);
      
      pLgtStart    = new Path(pShotPath, "lgt");
      pLgtEdit     = new Path(pLgtStart, edit);
      pLgtSubmit   = new Path(pLgtStart, submit);
      pLgtPrepare  = new Path(pLgtSubmit, prepare);
      pLgtApprove  = new Path(pLgtStart, approve);
      pLgtProduct  = new Path(pLgtApprove, product);
      pLgtThumb    = new Path(pLgtSubmit, thumb);
    }
  }

  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D A B L E                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected String
  getEditDirectory()
  {
    return "edit";
  }
  
  protected String
  getSubmitDirectory()
  {
    return "submit";
  }
  
  protected String
  getApproveDirectory()
  {
    return "approve";
  }
  
  protected String
  getPrepareDirectory()
  {
    return "prepare";
  }
  
  protected String
  getProductDirectory()
  {
    return "product";
  }
  
  protected String
  getSourceDirectory()
  {
    return "src";
  }
  
  protected String
  getThumbnailDirectory()
  {
    return "thumb";
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  public String
  getMovieName()
  {
    return pMovieName;
  }
  
  public String
  getSequenceName()
  {
    return pSeqName;
  }
  
  public String
  getShotName()
  {
    return pShotName;  
  }
  
  /*---Stuff---------------------------------------------------------------------------------*/
  
  /**
   * A node that contains information about the shot.
   */
  public String
  getShotInfoNodeName()
  {
    return null;
  }
  
  
  /*---Layout--------------------------------------------------------------------------------*/
  
  /**
   * The node with the animated camera in it.
   */
  public String
  getCameraNodeName()
  {
    return new Path(pLayEdit, pNamePrefix + "camera").toString();
  }
  
  /**
   * The node that a user will edit to create the layout.
   */
  public String
  getLayoutEditNodeName()
  {
    return new Path(pLayEdit, pNamePrefix + "layout").toString();
  }
  
  /**
   * A copy of the layout node that is going to be used to create the animation scene. 
   */
  public String
  getLayoutProductNodeName()
  {
    return new Path(pLayProduct, pNamePrefix + "layout").toString();
  }
  
  /**
   * A node containing all of the exported animation for the layout. 
   */
  public String
  getLayoutExportPrepareNodeName()
  {
    return new Path(pLayPrepare, pNamePrefix + "data").toString();
  }
  
  /**
   * A node containing a copy of the exported animation for the layout for creating the
   * animation scene. 
   */
  public String
  getLayoutExportProductNodeName()
  {
    return new Path(pLayProduct, pNamePrefix + "data").toString();
  }
  
  /**
   * The node where the layout animation is reapplied to test the exported animation
   * and create a render for approval. 
   */
  public String
  getLayoutBuildNodeName()
  {
    return new Path(pLayPrepare, pNamePrefix + "layout").toString();
  }
  
  /**
   * A node containing the exported animation from the layout for a particular character. 
   */
  public String
  getLayoutExportPrepareNodeName
  (
    String assetName  
  )
  {
    return new Path(new Path(pLayPrepare, "data"), pNamePrefix + assetName).toString();
  }
  
  /**
    * A node containing a copy of the exported animation from the layout for creating the
   * animation scene. 
   */
  public String
  getLayoutExportProductNodeName
  (
    String assetName  
  )
  {
    return new Path(new Path(pLayProduct, "data"), pNamePrefix + assetName).toString();
  }
  
  /**
   * The rendered (or playblasted) images for layout submission. 
   */
  public String
  getLayoutImgNodeName()
  {
    return new Path(pLayPrepare, pNamePrefix + "img").toString();
  }
  
  /**
   * The thumbnail image for the playblast images. 
   */
  public String
  getLayoutThumbNodeName()
  {
    return new Path(pLayThumb, pNamePrefix + "thumb").toString();
  }
  
  /**
   * The layout submission node.
   */
  public String
  getLayoutSubmitNodeName()
  {
    return new Path(pLaySubmit, pNamePrefix + "submit").toString();
  }
  
  /**
   * The layout approve node. 
   */
  public String
  getLayoutApproveNodeName()
  {
    return new Path(pLayApprove, pNamePrefix + "approve").toString();
  }
  

  /*---Anim----------------------------------------------------------------------------------*/

  /**
   * A node per asset that represents source motion capture data being brought in as
   * FBX files.
   */
  public String
  getAnimSourceFBXNodeName
  (
    String assetName
  )
  {
    return new Path(pAnimSource, pNamePrefix + assetName).toString();
  }
  
  /**
   * A node per asset that represents the source FBX data converted into maya
   * animation curves.
   */
  public String
  getAnimSourceCurvesNodeName
  (
    String assetName
  )
  {
    return new Path(pAnimSource, pNamePrefix + assetName + "_crv").toString();
  }
   
  
  /**
   * The node that a user will edit to create the animation or the node that is build from
   * component curve files if the animation is all coming from fbx files.
   */
  public String
  getAnimEditNodeName()
  {
    return new Path(pAnimEdit, pNamePrefix + "anim").toString();
  }
  
  /**
   * A copy of the animation node that is going to be used to create the switch lighting scene. 
   */
  public String
  getAnimProductNodeName()
  {
    return new Path(pAnimProduct, pNamePrefix + "anim").toString();
  }
  
  /**
   * A node containing all of the exported animation from the animation scene. 
   */
  public String
  getAnimExportPrepareNodeName()
  {
    return new Path(pAnimPrepare, pNamePrefix + "data").toString();
  }

  /**
   * A node containing all of the exported animation from the animation scene that is
   * used to build the prelight scene. 
   */
  public String
  getAnimExportProductNodeName()
  {
    return new Path(pAnimProduct, pNamePrefix + "data").toString();
  }

  /**
   * The node where the animation is reapplied to test the exported animation
   * and create a render for approval. 
   */
  public String
  getAnimBuildNodeName()
  {
    return new Path(pAnimPrepare, pNamePrefix + "anim").toString();
  }
  
  /**
   * A render (or playblast) of the animation that is submitted for approval. 
   */
  public String
  getAnimImgNodeName()
  {
    return new Path(pAnimPrepare, pNamePrefix + "img").toString();
  }
  
  /**
   * A thumbnail of the images.
   */
  public String
  getAnimThumbNodeName()
  {
    return new Path(pAnimThumb, pNamePrefix + "thumb").toString();
  }
  
  /**
   *  The submit node for the animation task. 
   */
  public String
  getAnimSubmitNodeName()
  {
    return new Path(pAnimSubmit, pNamePrefix + "submit").toString();
  }
  
  /**
   *  The approve node for the animation task. 
   */
  public String
  getAnimApproveNodeName()
  {
    return new Path(pAnimApprove, pNamePrefix + "approve").toString();
  }
  
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
  )
  {
    return new Path(new Path(pAnimPrepare, "data"), assetName).toString();
  }
  
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
  )
  {
    return new Path(new Path(pAnimProduct, "data"), assetName).toString();
  }
  
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
  )
  {
    return new Path(new Path(pAnimProduct, "build"), assetName).toString();
  }
  
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
  )
  {
    return new Path(new Path(pAnimProduct, "mi"), pNamePrefix + assetName).toString();
  }
  
  /**
   * A node containing the mi file data for the camera made from the animation scene.
   */
  public String
  getCameraMiNodeName()
  {
    return new Path(new Path(pAnimProduct, "mi"), pNamePrefix + "cam").toString();
  }
  
  /**
   * A node built by applying exported animation to lighting models.  This node
   * is referenced by the lighting scene.
   */  
  public String
  getPreLightNodeName()
  {
    return new Path(pAnimProduct, pNamePrefix + "preLgt").toString();
  }
  
  /**
   * A scene specific gathering MEL script that is run on the prelight as it is being 
   * built.  Allows shot specific overrides. 
   */
  public String
  getPreLightMELNodeName()
  {
    return new Path(pAnimProduct, pNamePrefix + "preScript").toString();
  }
  
  /*---Light---------------------------------------------------------------------------------*/
  
  /**
   * The node which the artist edits to add lighting to the shot. 
   */
  public String
  getLightEditNodeName()
  {
    return new Path(pLgtEdit, pNamePrefix + "lgt").toString();
  }
  
  /**
   *  The rendered images which are submitted for approval of the lighting scene. 
   */
  public String
  getLightImagesNodeName()
  {
    return new Path(pLgtPrepare, pNamePrefix + "img").toString();
  }

  /**
   * The submit node for the lighting task.
   */
  public String
  getLightSubmitNodeName()
  {
    return new Path(pLgtSubmit, pNamePrefix + "submit").toString();
  }
  
  /**
   * The approve node for the lighting task.
   */
  public String
  getLightApproveNodeName()
  {
    return new Path(pLgtApprove, pNamePrefix + "approve").toString();
  }

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
  )
  {
    return new Path(new Path(pLgtSubmit, "mi"), pNamePrefix + assetName).toString();
  }
  
  /**
   * The mi file containing the light data from the lighting scene. 
   */
  public String
  getLightMiNodeName
  ()
  {
    return new Path(new Path(pLgtSubmit, "mi"), pNamePrefix + "lgt").toString();
  }

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
  )
  {
    return new Path(new Path(pLgtSubmit, "mi"), pNamePrefix + assetName + "_shd").toString();
  }
  
  /**
   * The mi file containing the camera override mi file.
   */
  public String
  getCamOverrideMiNodeName()
  {
    return new Path(new Path(pLgtSubmit, "mi"), pNamePrefix + "camOver").toString();
  }
  
  /**
   * The mi file containing the render options mi file.
   */
  public String
  getOptionsMiNodeName()
  {
    return new Path(new Path(pLgtSubmit, "mi"), pNamePrefix + "opt").toString();
  }
  
  /**
   * Generated on lighting approval.  This node is used to render all the final
   * render passes that depend upon lighting. 
   */
  public String
  getFinalLightNodeName()
  {
    return new Path(pLgtProduct, pNamePrefix + "lgt").toString();
  }
  
  /**
   * A thumbnail of the images.
   */
  public String
  getLightThumbNodeName()
  {
    return new Path(pLgtThumb, pNamePrefix + "thumb").toString();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aLocation = "Location";
  public final static String aMovieName = "MovieName";
  public final static String aSequenceName = "SequenceName";
  public final static String aShotName = "ShotName";

  public final static String aApprovalFormat = "ApprovalFormat";
  
  private static final long serialVersionUID = 8920324273008970341L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private AnswersBuilderQueries pBuilderInfo;
  
  private String pProject;
  
  private String pMovieName;
  private String pSeqName;
  private String pShotName;
  private boolean pApprovalMode;
  private boolean pUseMovie;
  private boolean pExistingDirs;
  
  private String pNamePrefix;
  
  // Paths
  private Path pShotPath;

  private Path pLayStart;
  private Path pLayEdit;
  private Path pLayPrepare;
  private Path pLaySubmit;
  private Path pLayApprove;
  private Path pLayProduct;
  private Path pLayThumb;
  
  private Path pAnimStart;
  private Path pAnimEdit;
  private Path pAnimSource;
  private Path pAnimPrepare;
  private Path pAnimSubmit;
  private Path pAnimApprove;
  private Path pAnimProduct;
  private Path pAnimThumb;
  
  private Path pLgtStart;
  private Path pLgtEdit;
  private Path pLgtPrepare;
  private Path pLgtSubmit;
  private Path pLgtApprove;
  private Path pLgtProduct;
  private Path pLgtThumb;
  
}
