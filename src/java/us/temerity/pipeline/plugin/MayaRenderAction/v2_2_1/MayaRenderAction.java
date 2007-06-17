// $Id: MayaRenderAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaRenderAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E N D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders a series of images from a source Maya scene source node. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source Maya scene node.
 *   </DIV> <BR>
 * 
 *   Camera Override <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the render camera (if set). <BR> 
 *   </DIV> <BR>
 * 
 *   Renderer <BR>
 *   <DIV style="margin-left: 40px;">
 *     The type of renderer used to render the images: Hardware, Software, Mental Ray or 
 *     Vector<BR>
 *   </DIV> <BR>
 * 
 *   Processors <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of processors to use (0 = all available). <BR> 
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * 
 *   Pre Render MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced before rendering begins. 
 *   </DIV> <BR>
 * 
 *   Post Render MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced after rendering ends.
 *   </DIV> <BR>
 * 
 *   Pre Layer MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced before rendering each layer. 
 *   </DIV> <BR>
 * 
 *   Post Layer MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced after rendering each layer.
 *   </DIV> <BR>
 *
 *   Pre Frame MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced before rendering each frame.
 *   </DIV> <BR>
 * 
 *   Post Frame MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to sourced after rendering each frame. 
 *     frame. <BR>
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MayaRenderAction
  extends MayaActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaRenderAction() 
  {
    super("MayaRender", new VersionID("2.2.1"), "Temerity",
	  "Renders a Maya scene.");
    
    {
      ActionParam param =
	new LinkActionParam
        (aMayaScene, 
         "The source Maya scene node.", 
         null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	(aCameraOverride,
	 "Overrides the render camera (if set).", 
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> names = new ArrayList<String>();
      names.add("Hardware");
      names.add("Software");
      names.add("Mental Ray");
      names.add("Vector");

      ActionParam param = 
	new EnumActionParam
	(aRenderer,
	 "The type of renderer to use.", 
	 "Software", names); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	(aProcessors, 
	 "The number of processors to use (0 = all available).", 
	 1);
      addSingleParam(param);
    }

    addExtraOptionsParam();

    {
      ActionParam param = 
	new LinkActionParam
	(aPreRenderMEL,
	 "The MEL script to sourced before rendering begins.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aPostRenderMEL,
	 "The MEL script to sourced after rendering ends.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aPreLayerMEL,
	 "The MEL script to sourced before rendering each layer.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aPostLayerMEL,
	 "The MEL script to sourced after rendering each layer.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aPreFrameMEL,
	 "The MEL script to sourced before rendering each frame.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aPostFrameMEL,
	 "The MEL script to sourced after rendering each frame.", 
	 null);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aCameraOverride);
      layout.addSeparator();
      layout.addEntry(aRenderer);
      layout.addEntry(aProcessors); 
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

      {
	LayoutGroup sub = 
          new LayoutGroup
	  ("MEL Scripts", 
	   "MEL scripts run at various stages of the rendering process.", 
	   true);
	sub.addEntry(aPreRenderMEL); 
	sub.addEntry(aPostRenderMEL);
	sub.addSeparator();
	sub.addEntry(aPreLayerMEL); 
	sub.addEntry(aPostLayerMEL); 
	sub.addSeparator();
	sub.addEntry(aPreFrameMEL); 
	sub.addEntry(aPostFrameMEL); 

	layout.addSubGroup(sub);
      }

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);   
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  { 
    /* the target image sequence */ 
    FileSeq target = null;
    {
      target = agenda.getPrimaryTarget();
      String suffix = target.getFilePattern().getSuffix();
      if(suffix == null) 
	throw new PipelineException
	  ("The target file sequence (" + target + ") must have a filename suffix!");

      if(!target.hasFrameNumbers())
        throw new PipelineException
          ("The " + getName() + " Action requires that the output images have frame " + 
           "numbers.");
    }

    /* the source Maya scene */ 
    Path sourceScene = getMayaSceneSourcePath(aMayaScene, agenda);
    if(sourceScene == null) 
      throw new PipelineException 
        ("A source MayaScene must be specified!"); 

    /* MEL script paths */ 
    Path preRenderMEL  = getMelScriptSourcePath(aPreRenderMEL, agenda);
    Path postRenderMEL = getMelScriptSourcePath(aPostRenderMEL, agenda);
    Path preLayerMEL   = getMelScriptSourcePath(aPreLayerMEL, agenda);
    Path postLayerMEL  = getMelScriptSourcePath(aPostLayerMEL, agenda);
    Path preFrameMEL   = getMelScriptSourcePath(aPreFrameMEL, agenda);
    Path postFrameMEL  = getMelScriptSourcePath(aPostFrameMEL, agenda);

    
    /* toolset environment */ 
    TreeMap<String,String> env = new TreeMap<String,String>(agenda.getEnvironment());

    /* renderer command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    {
      args.add("-renderer");

      String renderer = (String) getSingleParamValue(aRenderer); 
      switch(getSingleEnumParamIndex(aRenderer)) {
      case 0: // Hardware
	args.add("hw");
        break;

      case 1: // Software
	args.add("sw");
        break;

      case 2: // Mental Ray
	args.add("mr");
        break;

      case 3: // Vector
	args.add("vr");
        break;

      default:
	throw new PipelineException("Unsupported Renderer type!");
      }

      FrameRange range = target.getFrameRange();
      FilePattern fpat = target.getFilePattern();
      
      args.add("-s");
      args.add(String.valueOf(range.getStart()));
      args.add("-e");
      args.add(String.valueOf(range.getEnd()));
      args.add("-b");
      args.add(String.valueOf(range.getBy()));
	
      Path renderPath = agenda.getTargetPath(); 

      switch(getSingleEnumParamIndex(aRenderer)) {
      case 0:  // Hardware
      case 3:  // Vector
        /* renderers which don't support pre/post MEL scripts */ 
        {
          if(preRenderMEL != null) 
            throw new PipelineException
              ("The " + renderer + " renderer does not accept PreRenderMEL Scripts!");
          
          if(postRenderMEL != null) 
            throw new PipelineException
              ("The " + renderer + " renderer does not accept PostRenderMEL Scripts!");

          if(preLayerMEL != null) 
            throw new PipelineException
              ("The " + renderer + " renderer does not accept PreLayerMEL Scripts!");
          
          if(postLayerMEL != null) 
            throw new PipelineException
              ("The " + renderer + " renderer does not accept PostMEL Scripts!");
          
          if(preFrameMEL != null) 
            throw new PipelineException
              ("The " + renderer + " renderer does not accept PreFrameMEL Scripts!");
          
          if(postFrameMEL != null) 
            throw new PipelineException
              ("The " + renderer + " renderer does not accept PostFrameMEL Scripts!");
          
          if(fpat.getPadding() > 1) 
            throw new PipelineException
              ("The " + renderer + " renderer can only render files with unpadded " + 
               "frame numbers!");
        }
        break;
        
      default:
        /* renderers which do support pre/post MEL scripts */ 
        {
          /* hack to get around the broken "-rd" option for mental ray */ 
          if(getSingleEnumParamIndex(aRenderer) == 2) { // Mental Ray 
            File script = createTemp(agenda, "mel");
            try {      
              FileWriter out = new FileWriter(script);
	    
              if(preRenderMEL != null) 
                out.write("source \"" + preRenderMEL + "\";\n\n");
              
              out.write("workspace -rt \"images\" \"" + renderPath + "\";\n" + 
                        "workspace -rt \"depth\" \"" + renderPath + "\";\n");
              
              out.close();
            }
            catch(IOException ex) {
              throw new PipelineException
                ("Unable to write temporary MEL script file (" + script + ") for Job " + 
                 "(" + agenda.getJobID() + ")!\n" +
		 ex.getMessage());
            }

            args.add("-preRender");
            if(PackageInfo.sOsType == OsType.Windows) 
              args.add("\"source " + script.getName() + "\"");
            else 
              args.add("source " + script.getName());
          }
          else if(preRenderMEL != null) {
            args.add("-preRender");
            args.add(wrapperMEL(agenda, preRenderMEL));
          }
	
          if(postRenderMEL != null) {
            args.add("-postRender");
            args.add(wrapperMEL(agenda, postRenderMEL));
          }
          
          if(preLayerMEL != null) {
            args.add("-preLayer");
            args.add(wrapperMEL(agenda, preLayerMEL));
          }
          
          if(postLayerMEL != null) {
            args.add("-postLayer");
            args.add(wrapperMEL(agenda, postLayerMEL));
          }
          
          if(preFrameMEL != null) {
            args.add("-preFrame");
            args.add(wrapperMEL(agenda, preFrameMEL));
          }
          
          if(postFrameMEL != null) {
            args.add("-postFrame");
            args.add(wrapperMEL(agenda, postFrameMEL));
          }

          {
            Path tempPath = getTempPath(agenda);
            String ospath = env.get("MAYA_SCRIPT_PATH");
            if(ospath != null) {
              env.put("MAYA_SCRIPT_PATH", 
                      tempPath.toOsString() + File.pathSeparator + ospath);
            }
            else {
              env.put("MAYA_SCRIPT_PATH", tempPath.toOsString());
            }
          }
      
          args.add("-pad"); 
          args.add(String.valueOf(fpat.getPadding()));
        }
      }

      args.add("-fnc"); 
      args.add("3"); 

      args.add("-of");  
      args.add(fpat.getSuffix());

      args.add("-rd");
      args.add(renderPath.toOsString()); 

      {
        Path path = new Path(agenda.getNodeID().getName());
        args.add("-im");
        args.add(path.getName());      
      }
      
      {
	String camera = getSingleStringParamValue(aCameraOverride);
	if(camera != null) {
	  args.add("-cam");
	  args.add(camera);
	}
      }
      
      {
	Integer procs = (Integer) getSingleParamValue(aProcessors);
	if(procs != null) {
          switch(getSingleEnumParamIndex(aRenderer)) {
          case 1: // Software
            {
              if(procs < 0) 
                throw new PipelineException
                  ("The Software renderer requires that the Processors parameter is " + 
                   "non-negative.");
              
              args.add("-n"); 
              args.add(procs.toString());
            }
            break;

          case 2: // Mental Ray
            {  
              if((procs < 1) || (procs > 4)) 
                throw new PipelineException
                  ("The Mental Ray renderer requires that the Processors parameter is " + 
                   "in the range (1-4).");
	      
              args.add("-rt");
              args.add(procs.toString());
            }
          }
        }
      }

      if(getSingleEnumParamIndex(aRenderer) == 2) { // Mental Ray
	args.add("-v");
	args.add("3");
      }

      args.addAll(getExtraOptionsArgs());

      args.add(sourceScene.toOsString());
    }

    /* render program */ 
    String program = "Render";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "Render.exe";
      
    /* create the process to run the action */ 
    return createSubProcess(agenda, program, args, env, 
                            agenda.getTargetPath().toFile(), outFile, errFile); 
  }

  /**
   * Creates a temporary MEL script which simply sources the given script. <P> 
   * 
   * This allows MEL scripts with full path names and "-" characters to be passed as arguments
   * to the various pre/post MEL script options.  It also means that only the temporary 
   * directory for the job needs to be added to the MAYA_SCRIPT_PATH.
   * 
   * @param mel
   *   The MEL script to source.
   * 
   * @return
   *   The name of the temporary wrapper MEL script.
   */ 
  private String
  wrapperMEL
  (
   ActionAgenda agenda, 
   Path mel
  ) 
    throws PipelineException
  {
    File script = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(script);
      
      out.write("source \"" + mel + "\";\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    if(PackageInfo.sOsType == OsType.Windows) 
      return ("\"source " + script.getName() + "\"");
    return ("source " + script.getName());
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1263426474152735301L;

  public static final String aMayaScene      = "MayaScene";
  public static final String aCameraOverride = "CameraOverride";
  public static final String aRenderer       = "Renderer";
  public static final String aProcessors     = "Processors";
  public static final String aPreRenderMEL   = "PreRenderMEL";
  public static final String aPostRenderMEL  = "PostRenderMEL";
  public static final String aPreLayerMEL    = "PreLayerMEL";
  public static final String aPostLayerMEL   = "PostLayerMEL";
  public static final String aPreFrameMEL    = "PreFrameMEL";
  public static final String aPostFrameMEL   = "PostFrameMEL";

}

