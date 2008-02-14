// $Id: NukeRescaleAction.java,v 1.1 2008/02/14 18:46:01 jim Exp $

package us.temerity.pipeline.plugin.NukeRescaleAction.v2_4_2;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.plugin.*;

import java.io.*;
import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   N U K E   R E F O R M A T   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Provides control over generating and/or running a Nuke script containing a Reformat node
 * which can rescale the resolution and orientation of a set of source images.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Mode <BR>
 *   The mode of operation for this action: 
 *   <DIV style="margin-left: 40px;">
 *     Process - Dynamically creates and executes a Nuke script which rescales the images
 *     specified by Image Source to generate the target images.<P> 
 *     Read & Rescale - Generates a target Nuke script which reads and rescales the images 
 *     specified by Image Source.<P> 
 *     Rescale - Generates a target Nuke script which contains a single Nuke Reformat node.
 *   </DIV> <BR>
 * 
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     When using Process or Read & Rescale mode, this parameter specifies the source 
 *     node containing the images to rescale. Ignored in Rescale mode.
 *   </DIV> <BR>
 *   <P>  
 * 
 *   
 *   Scale <BR>
 *   <DIV style="margin-left: 40px;">
 *     The scaling factor to apply to the source images. 
 *   </DIV> <BR>
 * 
 * 
 *   Resize Type<BR>
 *   <DIV style="margin-left: 40px;">
 *     The direction which controls the scaling factor: 
 *     <DIV style="margin-left: 40px;">
 *       None - Don't change the pixels.<P> 
 *       Width - Scale to the image fills the output width.<P> 
 *       Height - Scale to the image fills the output height. <P> 
 *       Fit - Scale to the smaller of width or height.<P> 
 *       Fill - Scale to the larger of width or height.<P> 
 *       Distort - Non-uniform scale to match both width and height.
 *     </DIV> 
 *   </DIV> <BR>
 * 
 *   Center <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to translate the image to center it in the output.
 *   </DIV> <BR>
 * 
 *   Flip <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to mirror the image vertically.
 *   </DIV> <BR>
 * 
 *   Flop <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to mirror the image horizontally.
 *   </DIV> <BR>
 * 
 *   Turn <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to rotate the image 90 degrees.
 *   </DIV> <BR><P> 
 * 
 * 
 *   Filter <BR>
 *   <DIV style="margin-left: 40px;">
 *     The method used to process pixels from the input image to produce pixels in the 
 *     output image: 
 *     <DIV style="margin-left: 40px;">
 *       Impulse - No filtering, each output pixel is directly copied from an input pixel.<P>
 *       Cubic - Smooth interpolate between pixels.<P> 
 *       Keys - Cubic (a=0.5), aproximates sync.<P> 
 *       Simon - Cubic (a=0.75), continuous second derivative.<P> 
 *       Rifman - Cubic (a=1.0), lots of sharpening.<P> 
 *       Mitchell - Mix of sharpening and smoothing.<P> 
 *       Parzen - Aproximating B-spline.<P> 
 *       Notch - Hides moire patterns.
 *     </DIV>
 *   </DIV> <BR>
 * 
 *   Clamp <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to set negative intermediate and final results to zero.  This will remove 
 *     ringing around mattes when using a filter that has negative lobes.
 *   </DIV> <BR>
 * 
 *   Crop <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to set pixels outside the input format to black.
 *   </DIV> <BR>
 * 
 *   Preserve BBox<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to preserve pixels outside the output format bounding box.
 *   </DIV> <BR>
 *   <P> 
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments for Nuke. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * By default, this Action executes the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Action plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 */
public 
class NukeRescaleAction
  extends NukeActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeRescaleAction() 
  {
    super("NukeRescale", new VersionID("2.4.2"), "Temerity", 
	  "Provides control over generating and/or running a Nuke script containing a " + 
          "Reformat node which can rescale the resolution and orientation of a set of " + 
          "source images."); 
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aProcess);
      choices.add(aReadAndRescale);
      choices.add(aRescale); 

      ActionParam param = 
	new EnumActionParam
	(aMode,
	 "The mode of operation for this action.", 
         aProcess, choices);
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "When using Process or Read & Rescale mode, this parameter specifies the " + 
         "source node containing the images to rescale.  Ignored in Rescale mode.", 
	 null);
      addSingleParam(param);
    } 


    {
      ActionParam param = 
	new DoubleActionParam
	(aScale, 
	 "The scaling factor to apply to the source images.", 
	 1.0);
      addSingleParam(param);
    } 


    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aNone);   
      choices.add(aWidth);  
      choices.add(aHeight); 
      choices.add(aFit);    
      choices.add(aFill);   
      choices.add(aDistort);

      ActionParam param = 
	new EnumActionParam
	(aResizeType,
	 "The direction which controls the scaling factor.", 
         aWidth, choices);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new BooleanActionParam
	(aCenter,
	 "Whether to translate the image to center it in the output.", 
         true); 
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aFlip,
	 "Whether to mirror the image vertically.", 
         false); 
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aFlop,
	 "Whether to mirror the image horizontally.", 
         false); 
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aTurn,
	 "Whether to rotate the image 90 degrees.", 
         false); 
      addSingleParam(param);
    } 
    

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aImpulse);
      choices.add(aCubic);
      choices.add(aKeys); 
      choices.add(aSimon); 
      choices.add(aRifman); 
      choices.add(aMitchell); 
      choices.add(aParzen); 
      choices.add(aNotch); 

      ActionParam param = 
	new EnumActionParam
	(aFilter,
	 "The method used to process pixels from the input image to produce pixels in the " + 
         "output image.", 
         aCubic, choices);
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aClamp,
	 "Whether to set negative intermediate and final results to zero.  This will " + 
         "remove ringing around mattes when using a filter that has negative lobes.", 
         false); 
      addSingleParam(param);
    } 
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aCrop,
	 "Whether to set pixels outside the input format to black.", 
         false); 
      addSingleParam(param);
    } 
    
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aPreserveBBox, 
	 "Whether to preserve pixels outside the output format bounding box.", 
         false); 
      addSingleParam(param);
    } 
    
    addExtraOptionsParam(); 
    
    /* parameter layout */ 
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMode);   
      layout.addEntry(aImageSource);   
      layout.addEntry(aScale);
      layout.addSeparator(); 
      layout.addEntry(aResizeType);
      layout.addEntry(aCenter);
      layout.addEntry(aFlip);
      layout.addEntry(aFlop);
      layout.addEntry(aTurn);
      layout.addSeparator(); 
      layout.addEntry(aFilter);
      layout.addEntry(aClamp);
      layout.addEntry(aCrop);
      layout.addEntry(aPreserveBBox);
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

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
  @Override
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    /* mode of operation */ 
    String mode = getSingleStringParamValue(aMode); 
    if(!mode.equals(aProcess) && !mode.equals(aReadAndRescale) && !mode.equals(aRescale)) 
      throw new PipelineException
        ("Unknown value (" + mode + ") for the " + aMode + " parameter!"); 

    /* target Nuke script */ 
    Path nukePath = null;
    if(mode.equals(aReadAndRescale) || mode.equals(aRescale)) {
      nukePath = getPrimaryTargetPath(agenda, NukeActionUtils.getNukeExtensions(), 
                                      "Nuke Script");
    }

    /* target images */ 
    Path targetPath = null;
    FileSeq targetSeq = null;
    if(mode.equals(aProcess)) {
      targetPath = agenda.getTargetPath();
      targetSeq = agenda.getPrimaryTarget(); 
    }

    /* source images */ 
    Path fullSourcePath = null;
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    if(mode.equals(aProcess) || mode.equals(aReadAndRescale)) {
      String sname = getSingleStringParamValue(aImageSource); 
      if(sname == null) 
        throw new PipelineException
          ("The " + aImageSource + " was not set!");
      
      FileSeq fseq = agenda.getPrimarySource(sname);
      if(fseq == null) 
        throw new PipelineException
          ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
           "source nodes!");
      
      sourceSeq = fseq;
      
      Path spath = new Path(sname);
      sourcePath = new Path("WORKING" + spath.getParent());

      NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
      fullSourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
    }

    /* the Read node */ 
    String readNode = null; 
    if(mode.equals(aProcess) || mode.equals(aReadAndRescale)) {
      FilePattern fpat = sourceSeq.getFilePattern(); 
      FrameRange range = sourceSeq.getFrameRange(); 
      
      StringBuilder buf = new StringBuilder(); 
      buf.append
        ("Read {\n" + 
         " inputs 0\n" + 
         " file " + sourcePath + "/" + NukeActionUtils.toNukeFilePattern(fpat) + "\n");

      if(range != null) {
        buf.append(" first " + range.getStart() + "\n" + 
                   " last " + range.getEnd() + "\n"); 
      }
      
      buf.append("}\n"); 

      readNode = buf.toString(); 
    }
      
    /* the Reformat node options */ 
    String reformatOptions = null; 
    {
      StringBuilder buf = new StringBuilder(); 

      String resizeType = getSingleStringParamValue(aResizeType); 
      if(resizeType != null) 
        buf.append(" resize " + resizeType.toLowerCase() + "\n"); 
      
      buf.append(" center " + getSingleBooleanParamValue(aCenter) + "\n" + 
                 " flip " + getSingleBooleanParamValue(aFlip) + "\n" + 
                 " flop " + getSingleBooleanParamValue(aFlop) + "\n" + 
                 " turn " + getSingleBooleanParamValue(aTurn) + "\n"); 
  
      String filter = getSingleStringParamValue(aFilter); 
      if(filter != null) 
        buf.append(" filter " + filter + "\n"); 
      
      buf.append(" clamp " + getSingleBooleanParamValue(aClamp) + "\n" +
                 " crop " + getSingleBooleanParamValue(aCrop) + "\n" +
                 " pbb " + getSingleBooleanParamValue(aPreserveBBox) + "\n"); 

      reformatOptions = buf.toString(); 
    }
      
    /* the Write node */ 
    String writeNode = null;
    if(mode.equals(aProcess)) {
      FilePattern fpat = targetSeq.getFilePattern(); 

      StringBuilder buf = new StringBuilder(); 
      buf.append
        ("Write {\n" + 
         " file " + targetPath + "/" + NukeActionUtils.toNukeFilePattern(fpat) + "\n"); 

      String suffix = fpat.getSuffix();
      if((suffix != null) && (suffix.equals("tif") || suffix.equals("tiff"))) 
        buf.append(" file_type tiff\n" + 
                   " compression LZW\n");

      buf.append("}\n");
      
      writeNode = buf.toString();
    }
    
    /* create a temporary Nuke script */ 
    Path script = new Path(createTemp(agenda, "nk"));
    try {    
      FileWriter out = new FileWriter(script.toFile()); 
      
      /* read the source images in */
      if(readNode != null) 
        out.write(readNode); 

      /* reformat the images */ 
      out.write("Reformat {\n" + 
                " type scale\n" + 
                " scale " + getSingleDoubleParamValue(aScale) + "\n" + 
                reformatOptions + 
                "}\n"); 

      /* write the reformatted images out */
      if(writeNode != null) 
        out.write(writeNode); 
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary Nuke script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
    }
    
    /* run Nuke to process the images... */ 
    if(mode.equals(aProcess)) {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-nx"); 
      args.addAll(getExtraOptionsArgs());
      args.add(script.toString()); 
      args.add(NukeActionUtils.toNukeFrameRange(sourceSeq.getFrameRange()));
      
      return createSubProcess
        (agenda, NukeActionUtils.getNukeProgram(agenda), args, agenda.getEnvironment(), 
         agenda.getTargetPath().toFile(), outFile, errFile);
    }
    
    /* just copy the temporary Nuke script to the target location */ 
    else {
      return createTempCopySubProcess(agenda, script.toFile(), nukePath, outFile, errFile);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1234057653161328965L;

  public static final String aMode           = "Mode"; 
  public static final String aProcess        = "Process"; 
  public static final String aReadAndRescale = "Read & Rescale"; 
  public static final String aRescale        = "Rescale"; 

  public static final String aImageSource = "ImageSource";
  public static final String aScale       = "Scale"; 

  public static final String aResizeType = "ResizeType";  
  public static final String aNone       = "None";  
  public static final String aWidth      = "Width";  
  public static final String aHeight     = "Height";  
  public static final String aFit        = "Fit";   
  public static final String aFill       = "Fill";   
  public static final String aDistort    = "Distort";  

  public static final String aFilter   = "Filter";   
  public static final String aImpulse  = "Impulse"; 
  public static final String aCubic    = "Cubic";   
  public static final String aKeys     = "Keys";   
  public static final String aSimon    = "Simon";   
  public static final String aRifman   = "Rifman";   
  public static final String aMitchell = "Mitchell";   
  public static final String aParzen   = "Parzen";   
  public static final String aNotch    = "Notch";   

  public static final String aCenter = "Center";   
  public static final String aFlip   = "Flip";   
  public static final String aFlop   = "Flop";   
  public static final String aTurn   = "Turn"; 
  
  public static final String aClamp        = "Clamp";   
  public static final String aCrop         = "Crop";   
  public static final String aPreserveBBox = "PreserveBBox";   
}
