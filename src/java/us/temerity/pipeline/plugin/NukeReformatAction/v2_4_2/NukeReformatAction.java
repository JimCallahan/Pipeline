// $Id: NukeReformatAction.java,v 1.3 2008/03/17 23:00:04 jim Exp $

package us.temerity.pipeline.plugin.NukeReformatAction.v2_4_2;

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
 * which can modify the resolution and orientation of a set of source images.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Mode <BR>
 *   The mode of operation for this action: 
 *   <DIV style="margin-left: 40px;">
 *     Process - Dynamically creates and executes a Nuke script which reformats the images
 *     specified by Image Source to generate the target images.<P> 
 *     Read & Reformat - Generates a target Nuke script which reads and reformats the images 
 *     specified by Image Source.<P> 
 *     Reformat - Generates a target Nuke script which contains a single Nuke Reformat node.
 *   </DIV> <BR>
 * 
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     When using Process or Read & Reformat mode, this parameter specifies the source 
 *     node containing the images to reformat. Ignored in Reformat mode.
 *   </DIV> <BR>
 *   <P>  
 * 
 *   
 *   Output Resolution <BR>
 *   <DIV style="margin-left: 40px;">
 *     The width and height in pixels of the output images when Nuke's proxy mode is OFF.
 *   </DIV> <BR>
 * 
 *   Output Aspect <BR>
 *   <DIV style="margin-left: 40px;">
 *     The pixel aspect ration of the output images when Nuke's proxy mode is OFF.
 *   </DIV> <BR>
 * 
 *   Output Res Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     If specified, the Output Resolution is determined by the size of the images 
 *     associated with the given source node.
 *   </DIV> <BR>
 *   <P>  
 * 
 * 
 *   Proxy Resolution <BR>
 *   <DIV style="margin-left: 40px;">
 *     The width and height in pixels of the output images when Nuke's proxy mode is ON.
 *   </DIV> <BR>
 * 
 *   Proxy Aspect <BR>
 *   <DIV style="margin-left: 40px;">
 *     The pixel aspect ration of the output images when Nuke's proxy mode is ON.
 *   </DIV> <BR>
 * 
 *   Proxy Res Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     If specified, the Proxy Resolution is determined by the size of the images 
 *     associated with the given source node.
 *   </DIV> <BR>
 *   <P>  
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
 * This action uses the Imagemagick identify(1) utility to analyze the source images. <P> 
 * 
 * By default, the "python" program may be used by this action to run identify(1), 
 * construct a dynamic Nuke script and optionally run Nuke itself on this script.
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name. <P> 
 * 
 * By default, this Action executes the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Action plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 */
public 
class NukeReformatAction
  extends PythonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeReformatAction() 
  {
    super("NukeReformat", new VersionID("2.4.2"), "Temerity", 
	  "Provides control over generating and/or running a Nuke script containing a " + 
          "Reformat node which can modify the resolution and orientation of a set of " + 
          "source images."); 
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aProcess);
      choices.add(aReadAndReformat);
      choices.add(aReformat); 

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
	 "When using Process or Read & Reformat mode, this parameter specifies the " + 
         "source node containing the images to reformat.  Ignored in Reformat mode.", 
	 null);
      addSingleParam(param);
    } 


    {
      ActionParam param = 
	new LinkActionParam
	(aOutputResSource,
	 "If specified, the Output Resolution is determined by the size of the images " +
         "associated with the given source node.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new Tuple2iActionParam
	(aOutputResolution,
	 "The width and height in pixels of the output images when Nuke's proxy mode " + 
         "is OFF.", 
	 new Tuple2i(-1, -1));
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	(aOutputAspect,
	 "The pixel aspect ratio of the output images when Nuke's proxy mode is OFF.", 
	 1.0);
      addSingleParam(param);
    } 

    
    {
      ActionParam param = 
	new LinkActionParam
	(aProxyResSource,
	 "If specified, the Proxy Resolution is determined by the size of the images " +
         "associated with the given source node.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new Tuple2iActionParam
	(aProxyResolution,
	 "The width and height in pixels of the output images when Nuke's proxy mode " + 
         "is ON.", 
	 new Tuple2i(-1, -1)); 
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	(aProxyAspect,
	 "The pixel aspect ration of the output images when Nuke's proxy mode is ON.", 
	 1.0);
      addSingleParam(param);
    } 


    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aRootFormat);  
      choices.add(aPCVideo);      
      choices.add(aNTSC);         
      choices.add(aPAL);          
      choices.add(aHD);           
      choices.add(aNTSC169);      
      choices.add(aPAL169);       
      choices.add(aSuper1K);      
      choices.add(aCine1K);       
      choices.add(aSuper2K);      
      choices.add(aCine2K);       
      choices.add(aSuper4K);      
      choices.add(aCine4K);       
      choices.add(aSquare256);    
      choices.add(aSquare512);    
      choices.add(aSquare1K);     
      choices.add(aSquare2K);     
      
      addPreset(aOutputFormats, choices);

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(-1, -1)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aRootFormat, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(640, 480)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aPCVideo, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(720, 480)); 
        values.put(aOutputAspect, 0.9);
	
        addPresetValues(aOutputFormats, aNTSC, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(720, 576)); 
        values.put(aOutputAspect, 1.067);
	
        addPresetValues(aOutputFormats, aPAL, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(1920, 1080)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aHD, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(720, 480)); 
        values.put(aOutputAspect, 1.2);
	
        addPresetValues(aOutputFormats, aNTSC169, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(720, 576)); 
        values.put(aOutputAspect, 1.422);
	
        addPresetValues(aOutputFormats, aPAL169, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(1024, 778)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aSuper1K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(914, 778)); 
        values.put(aOutputAspect, 2.0);
	
        addPresetValues(aOutputFormats, aCine1K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(2048, 1556)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aSuper2K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(1828, 1556)); 
        values.put(aOutputAspect, 2.0);
	
        addPresetValues(aOutputFormats, aCine2K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(4096, 3112)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aSuper4K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(3656, 3112)); 
        values.put(aOutputAspect, 2.0);
	
        addPresetValues(aOutputFormats, aCine4K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(256, 256)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aSquare256, values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(512, 512)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aSquare512, values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(1024, 1024)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aSquare1K, values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aOutputResolution, new Tuple2i(2048, 22048)); 
        values.put(aOutputAspect, 1.0);
	
        addPresetValues(aOutputFormats, aSquare2K, values);
      }

      addPreset(aProxyFormats, choices);

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(-1, -1)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aRootFormat, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(640, 480)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aPCVideo, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(720, 480)); 
        values.put(aProxyAspect, 0.9);
	
        addPresetValues(aProxyFormats, aNTSC, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(720, 576)); 
        values.put(aProxyAspect, 1.067);
	
        addPresetValues(aProxyFormats, aPAL, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(1920, 1080)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aHD, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(720, 480)); 
        values.put(aProxyAspect, 1.2);
	
        addPresetValues(aProxyFormats, aNTSC169, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(720, 576)); 
        values.put(aProxyAspect, 1.422);
	
        addPresetValues(aProxyFormats, aPAL169, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(1024, 778)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aSuper1K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(914, 778)); 
        values.put(aProxyAspect, 2.0);
	
        addPresetValues(aProxyFormats, aCine1K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(2048, 1556)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aSuper2K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(1828, 1556)); 
        values.put(aProxyAspect, 2.0);
	
        addPresetValues(aProxyFormats, aCine2K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(4096, 3112)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aSuper4K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(3656, 3112)); 
        values.put(aProxyAspect, 2.0);
	
        addPresetValues(aProxyFormats, aCine4K, values);
      }

      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(256, 256)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aSquare256, values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(512, 512)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aSquare512, values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(1024, 1024)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aSquare1K, values);
      }
      
      {
        TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
        values.put(aProxyResolution, new Tuple2i(2048, 22048)); 
        values.put(aProxyAspect, 1.0);
	
        addPresetValues(aProxyFormats, aSquare2K, values);
      }
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
      layout.addSeparator(); 
      layout.addEntry(aOutputFormats);
      layout.addEntry(aOutputResSource);
      layout.addEntry(aOutputResolution); 
      layout.addEntry(aOutputAspect);
      layout.addSeparator(); 
      layout.addEntry(aProxyFormats);
      layout.addEntry(aProxyResSource);
      layout.addEntry(aProxyResolution); 
      layout.addEntry(aProxyAspect);
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
    if(!mode.equals(aProcess) && !mode.equals(aReadAndReformat) && !mode.equals(aReformat)) 
      throw new PipelineException
        ("Unknown value (" + mode + ") for the " + aMode + " parameter!"); 

    /* target Nuke script */ 
    Path nukePath = null;
    if(mode.equals(aReadAndReformat) || mode.equals(aReformat)) {
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
    if(mode.equals(aProcess) || mode.equals(aReadAndReformat)) {
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

    /* output format */ 
    Tuple2i outputRes = getSingleTuple2iParamValue(aOutputResolution, true); 
    Double outputAspect = getSingleDoubleParamValue(aOutputAspect); 
    Path outputResPath = null;
    FileSeq outputResSeq = null;
    {
      String sname = getSingleStringParamValue(aOutputResSource); 
      if(sname != null) {      
        FileSeq fseq = agenda.getPrimarySource(sname);
        if(fseq == null) 
          throw new PipelineException
            ("Somehow the " + aOutputResSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
      
        outputResSeq = fseq;
      
        NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
        outputResPath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
    }

    /* proxy format */ 
    Tuple2i proxyRes = getSingleTuple2iParamValue(aProxyResolution, true); 
    Double proxyAspect = getSingleDoubleParamValue(aProxyAspect); 
    Path proxyResPath = null;
    FileSeq proxyResSeq = null;
    {
      String sname = getSingleStringParamValue(aProxyResSource); 
      if(sname != null) {      
        FileSeq fseq = agenda.getPrimarySource(sname);
        if(fseq == null) 
          throw new PipelineException
            ("Somehow the " + aProxyResSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
      
        proxyResSeq = fseq;
      
        NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
        proxyResPath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
    }


    /* the Read node */ 
    ArrayList<String> readNode = new ArrayList<String>(); 
    if(mode.equals(aProcess) || mode.equals(aReadAndReformat)) {
      FilePattern fpat = sourceSeq.getFilePattern(); 
      FrameRange range = sourceSeq.getFrameRange(); 
      
      readNode.add("Read {"); 
      readNode.add(" inputs 0");
      readNode.add(" file " + sourcePath + "/" + NukeActionUtils.toNukeFilePattern(fpat)); 

      if(range != null) {
        readNode.add(" first " + range.getStart()); 
        readNode.add(" last " + range.getEnd());
      }
      
      readNode.add("}");
    }
      
    /* the Reformat node options */ 
    ArrayList<String> reformatOptions = new ArrayList<String>(); 
    {
      String resizeType = getSingleStringParamValue(aResizeType); 
      if(resizeType != null) 
        reformatOptions.add(" resize " + resizeType.toLowerCase()); 
      
      reformatOptions.add(" center " + getSingleBooleanParamValue(aCenter));
      reformatOptions.add(" flip " + getSingleBooleanParamValue(aFlip));
      reformatOptions.add(" flop " + getSingleBooleanParamValue(aFlop));
      reformatOptions.add(" turn " + getSingleBooleanParamValue(aTurn)); 
      
      String filter = getSingleStringParamValue(aFilter); 
      if(filter != null) 
        reformatOptions.add(" filter " + filter); 
      
      reformatOptions.add(" clamp " + getSingleBooleanParamValue(aClamp));
      reformatOptions.add(" crop " + getSingleBooleanParamValue(aCrop));
      reformatOptions.add(" pbb " + getSingleBooleanParamValue(aPreserveBBox)); 
    }
      
    /* the Write node */ 
    ArrayList<String> writeNode = new ArrayList<String>(); 
    if(mode.equals(aProcess)) {
      FilePattern fpat = targetSeq.getFilePattern(); 

      writeNode.add("Write {");
      writeNode.add(" file " + targetPath + "/" + NukeActionUtils.toNukeFilePattern(fpat)); 
      
      String suffix = fpat.getSuffix();
      if((suffix != null) && (suffix.equals("tif") || suffix.equals("tiff"))) {
        writeNode.add(" file_type tiff");
        writeNode.add(" compression LZW");
      }

      writeNode.add("}");
    }
    
    /* do we need to first run identify(1) to lookup the image resolutions?
         if so, this means we need to wrap the whole thing in a python script... */ 
    if((outputResPath != null) || (proxyResPath != null)) {

      /* since Windows scripting is lame, 
           lets use Python to run identify(1) and piece the Nuke script together... */ 
      Path nukeScript = new Path(getTempPath(agenda), "NukeReformat.nk");
      File pythonScript = createTemp(agenda, "py"); 
      try {
        FileWriter out = new FileWriter(pythonScript);

        /* import modules */
        out.write("import os;\n" + 
                  "import shutil;\n");

        /* include the "launch" method definition */ 
        out.write(PythonActionUtils.getPythonLaunchHeader()); 
          
        /* collect output resolution information with identify(1) */ 
        if(outputResPath != null) {
          out.write 
            ("args1 = ['identify', '-format', '%w %h', '" + outputResPath + "/" + 
             outputResSeq.getPath(0) + "']\n" + 
             "print('RUNNING: ' + ' '.join(args1))\n" +
             "p1 = subprocess.Popen(args1, stdout=subprocess.PIPE)\n" +
             "result1 = p1.wait()\n" + 
             "if result1 != 0:\n" +
             "  sys.exit('Unable to identify: " + outputResPath + "/" + 
               outputResSeq.getPath(0) + "\\n  Exit Code = ' + str(result1) + '\\n')\n" + 
             "str1 = p1.stdout.read()\n" + 
             "outputX = str1.split()[0]\n" + 
             "outputY = str1.split()[1]\n" + 
             "outputFormat = ' full_format \"' + outputX + ' ' + outputY + ' 0 0 ' + " + 
             "outputX + ' ' + outputY + ' " + outputAspect + " \"\\n'\n\n");
        }
        else {
          if((outputRes != null) && ((outputRes.x() > 0) && (outputRes.y() > 0))) {
            out.write
              ("outputFormat = 'full_format \"" + outputRes.x() + " " + outputRes.y() +
               " 0 0 " + outputRes.x() + " " + outputRes.y() + " " + outputAspect + 
               " \"\\n'\n\n");
          }
          else {
            out.write("outputFormat = ''\n\n"); 
          }
        }

        /* collect proxy resolution information with identify(1) */ 
        if(proxyResPath != null) {
          out.write 
            ("args1 = ['identify', '-format', '%w %h', '" + proxyResPath + "/" + 
             proxyResSeq.getPath(0) + "']\n" + 
             "print('RUNNING: ' + ' '.join(args1))\n" +
             "p1 = subprocess.Popen(args1, stdout=subprocess.PIPE)\n" +
             "result1 = p1.wait()\n" + 
             "if result1 != 0:\n" +
             "  sys.exit('Unable to identify: " + proxyResPath + "/" + 
               proxyResSeq.getPath(0) + "\\n  Exit Code = ' + str(result1) + '\\n')\n" + 
             "str1 = p1.stdout.read()\n" + 
             "proxyX = str1.split()[0]\n" + 
             "proxyY = str1.split()[1]\n" + 
             "proxyFormat = ' proxy_format \"' + proxyX + ' ' + proxyY + ' 0 0 ' + " + 
             "proxyX + ' ' + proxyY + ' " + proxyAspect + " \"\\n'\n\n");
        }
        else {
          if((proxyRes != null) && ((proxyRes.x() > 0) && (proxyRes.y() > 0))) {
            out.write
              ("proxyFormat = 'full_format \"" + proxyRes.x() + " " + proxyRes.y() +
               " 0 0 " + proxyRes.x() + " " + proxyRes.y() + " " + proxyAspect + 
               " \"\\n'\n\n");
          }
          else {
            out.write("proxyFormat = ''\n\n"); 
          }
        }

        out.write
          ("out = open('" + nukeScript + "', 'w', 1024)\n" + 
           "try:\n"); 

        for(String line : readNode) 
          out.write("  out.write('" + line + "\\n')\n");

        out.write("  out.write('Reformat {\\n')\n" + 
                  "  out.write(outputFormat)\n" + 
                  "  out.write(proxyFormat)\n"); 

        for(String line : reformatOptions) 
          out.write("  out.write('" + line + "\\n')\n");
        
        out.write("  out.write('}\\n')\n"); 

        for(String line : writeNode) 
          out.write("  out.write('" + line + "\\n')\n");

        out.write("finally:\n" + 
                  "    out.close()\n\n"); 

        if(mode.equals(aProcess)) {
          out.write("launch('" + NukeActionUtils.getNukeProgram(agenda) + "', ['-nx', "); 

          for(String extra : getExtraOptionsArgs())
            out.write("'" + extra + "', "); 

          out.write("'" + nukeScript + "', '" + 
                    NukeActionUtils.toNukeFrameRange(sourceSeq.getFrameRange()) + "'])\n");
        }
        else {
          out.write("shutil.copy('" + nukeScript + "', '" + nukePath + "')\n\n"); 
        }

        out.write("print 'ALL DONE.'\n");

        out.close();
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary Python script file (" + pythonScript + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }

      return createPythonSubProcess(agenda, pythonScript, outFile, errFile);
    }
    
    /* otherwise, we already know the resolutions */ 
    else {
      /* create a temporary Nuke script */ 
      Path script = new Path(createTemp(agenda, "nk"));
      try {    
        FileWriter out = new FileWriter(script.toFile()); 
      
        /* read the source images in */
        for(String line : readNode) 
          out.write(line + "\n"); 

        /* reformat the images */ 
        {
          out.write("Reformat {\n"); 

          if((outputRes != null) && (outputRes.x() > 0) && (outputRes.y() > 0)) {
            if(outputAspect == null) 
              throw new PipelineException
                ("No " + aOutputAspect + " was specified!") ;
            
            out.write(" full_format \"" + outputRes.x() + " " + outputRes.y() + " 0 0 " + 
                      outputRes.x() + " " + outputRes.y() + " " + outputAspect + " \"\n"); 
          }
          
          if((proxyRes != null) && ((proxyRes.x() > 0) && (proxyRes.y() > 0))) {
            if(proxyAspect == null) 
              throw new PipelineException
                ("No " + aProxyAspect + " was specified!") ;
            
            out.write(" proxy_format \"" + proxyRes.x() + " " + proxyRes.y() + " 0 0 " + 
                      proxyRes.x() + " " + proxyRes.y() + " " + proxyAspect + " \"\n"); 
          }

          for(String line : reformatOptions) 
            out.write(line + "\n"); 
          
          out.write("}\n"); 
        }

        /* write the reformatted images out */
        for(String line : writeNode) 
          out.write(line + "\n"); 
      
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
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4414085432884796176L;

  public static final String aMode            = "Mode"; 
  public static final String aProcess         = "Process"; 
  public static final String aReadAndReformat = "Read & Reformat"; 
  public static final String aReformat        = "Reformat"; 

  public static final String aImageSource = "ImageSource";
     
  public static final String aRootFormat = "Root Format";
  public static final String aPCVideo    = "PC Video";
  public static final String aNTSC       = "NTSC";
  public static final String aPAL        = "PAL";
  public static final String aHD         = "HD";
  public static final String aNTSC169    = "NTSC 16:9";
  public static final String aPAL169     = "PAL 16:9";
  public static final String aSuper1K    = "1K Super 35 (full-ap)";
  public static final String aCine1K     = "1K Cinemascope";
  public static final String aSuper2K    = "2K Super 35 (full-ap)";
  public static final String aCine2K     = "2K Cinemascope";
  public static final String aSuper4K    = "4K Super_35 (full-ap)";
  public static final String aCine4K     = "4K Cinemascope";
  public static final String aSquare256  = "Square 256";
  public static final String aSquare512  = "Square 512";
  public static final String aSquare1K   = "Square 1K";
  public static final String aSquare2K   = "Square 2K";

  public static final String aOutputFormats    = "OutputFormats"; 
  public static final String aOutputResolution = "OutputResolution";   
  public static final String aOutputAspect     = "OutputAspect";   
  public static final String aOutputResSource  = "OutputResSource";  

  public static final String aProxyFormats    = "ProxyFormats"; 
  public static final String aProxyResolution = "ProxyResolution";   
  public static final String aProxyAspect     = "ProxyAspect";   
  public static final String aProxyResSource  = "ProxyResSource";   

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
