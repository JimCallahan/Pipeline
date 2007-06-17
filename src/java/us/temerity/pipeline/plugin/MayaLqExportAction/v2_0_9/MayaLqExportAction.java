// $Id: MayaLqExportAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaLqExportAction.v2_0_9;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   L Q   E X P O R T  A C T I O N                                               */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports RIB input files for RenderMan based renderers from a Maya scene using 
 * LiquidMaya. <P> 
 * 
 * Uses the LiquidMaya plugin to generate RIBs files for the render camera as the primary
 * target file sequence. If the Maya scene contains lights with use depth maps or deep shadow
 * maps, than secondary sequence RIBs will also be generated for each of these lights.  When
 * shadow RIBs are generated, a set of RIBs containing geometry shared by all of the shadow 
 * RIBs will also be exported. <P> 
 * 
 * The name of the main camera RIBs will match the primary file sequence.  Depth map shadow
 * secondary sequence RIBs will be named:<BR>
 * <UL>
 *   <LI><I>prefix</I>_<I>lightShape</I>SHD.#.rib
 * </UL>
 * Where (<I>prefix</I>) is the same as the primary file sequence prefix and 
 * (<I>lightShape</I>) matches the name of the light source shape node in the Maya scene.  
 * Deep shadow secondary sequence RIBs will be named:<BR>
 * <UL>
 *   <LI><I>prefix</I>_<I>lightShape</I>DSH.#.rib
 * </UL>
 * The archive RIBs containing the geometry shared by all depth map and deep shadow RIBs will
 * be named: <BR>
 * <UL>
 *   <LI><I>prefix</I>_SHADOWBODY.#.rib
 * </UL> <BR>
 * 
 * See the <A href="http://liquidmaya.sourceforge.net/liquidwiki">LiquidMaya</A> 
 * documentation for details.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Maya scene file. <BR> 
 *   </DIV> <BR>
 * 
 *   <DIV style="margin-left: 40px;">
 *   Image Width <BR>
 *   <DIV style="margin-left: 40px;">
 *     The horizontal resolution of the output image in pixels. <BR>
 *   </DIV> <BR>
 *  
 *   Image Height <BR>
 *   <DIV style="margin-left: 40px;">
 *     The vertical resolution of the output image in pixels. <BR>
 *   </DIV> <BR> 
 *  
 *   Pixel Aspect Ratio <BR>
 *   <DIV style="margin-left: 40px;">
 *     Ratio of pixel height to pixel width. <BR>
 *   </DIV> <BR>
 *   
 *   <I>Camera Controls</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Render Camera <BR>
 *     <DIV style="margin-left: 40px;">
 *       The name of the shape node in the Maya scene for the camera being rendered.
 *     </DIV> <BR>
 *   
 *     Depth of Field <BR>
 *     <DIV style="margin-left: 40px;">
 *       Whether to enable depth of field.
 *     </DIV><P> 
 * 
 *     <I>Motion Blur</I> <BR>
 *     <DIV style="margin-left: 40px;">
 *       Transformation Blur <BR>
 *       <DIV style="margin-left: 40px;">
 *         Include transformation blur motion blocks. 
 *       </DIV><BR>
 *   
 *       Deformation Blur <BR>
 *       <DIV style="margin-left: 40px;">
 *         Include deformation blur motion blocks. 
 *       </DIV><BR>
 *   
 *       Camera Blur <BR>
 *       <DIV style="margin-left: 40px;">
 *         Include motion blocks for the camera. 
 *       </DIV><BR>
 *   
 *       Shutter Timing <BR>
 *       <DIV style="margin-left: 40px;">
 *         How shutter timing relates to frame timing.
 *         <UL>
 *           <LI> Open on Frame
 *           <LI> Center on Frame
 *           <LI> Center Between Frames
 *           <LI> Close on Next Frame
 *         </UL>
 *       </DIV><BR>
 *   
 *       Shutter Relative <BR>
 *       <DIV style="margin-left: 40px;">
 *         ???
 *       </DIV><BR>
 *   
 *       Motion Samples <BR>
 *       <DIV style="margin-left: 40px;">
 *         The number of samples per motion block.
 *       </DIV><BR>
 *   
 *       Motion Factor <BR>
 *       <DIV style="margin-left: 40px;">
 *         The factor used to reduce shading rate on objects in motion.
 *       </DIV>
 *     </DIV> <P>
 *   </DIV> <P>
 * 
 *   <I>Export Details</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Export Mode <BR>
 *     <DIV style="margin-left: 40px;">
 *       The RIB generation mode. 
 *       <UL>
 *         <LI> Scene - A complete scene with display, lights and objects. 
 *         <LI> Archive - A RIB fragment suitable for reinclusion using ReadArchive. 
 *       </UL>
 *     </DIV><BR>
 * 
 *     Export Set
 *     <DIV style="margin-left: 40px;">
 *       The name of the Maya Set used to select the DAG nodes to export from the Maya scene. 
 *       If unset, then the entire scene will be exported.
 *     </DIV><BR>
 * 
 *     Surface Shaders
 *     <DIV style="margin-left: 40px;">
 *       Whether to include surface shaders in the exported RIB. 
 *     </DIV><BR>
 * 
 *     Displacement Shaders
 *     <DIV style="margin-left: 40px;">
 *       Whether to include displacement shaders in the exported RIB. 
 *     </DIV><BR>
 * 
 *     Light Shaders
 *     <DIV style="margin-left: 40px;">
 *       Whether to include light shaders in the exported RIB. 
 *     </DIV><BR>
 * 
 *     Volume Shaders
 *     <DIV style="margin-left: 40px;">
 *       Whether to include volume shaders in the exported RIB. 
 *     </DIV><BR>
 *   </DIV><P>
 * 
 *   <I>Extra RIB</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Before Frame Begin
 *     <DIV style="margin-left: 40px;">
 *       The node containing RIB to insert before the FrameBegin request.
 *     </DIV><BR>
 * 
 *     Before World Begin
 *     <DIV style="margin-left: 40px;">
 *       The node containing RIB to insert before the WorldBegin request.
 *     </DIV><BR>
 * 
 *     Before Frame Begin
 *     <DIV style="margin-left: 40px;">
 *       The node containing RIB to insert before the FrameBegin request.
 *     </DIV><BR>
 * 
 *     Before Frame Begin
 *     <DIV style="margin-left: 40px;">
 *       The node containing RIB to insert before the FrameBegin request.
 *     </DIV><BR>
 *   </DIV><P>
 * 
 *   <I>Output</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     OutputFormat <BR>
 *     <DIV style="margin-left: 40px;">
 *       The format of the output RIB file: <BR>
 *       <UL>
 *         <LI>ASCII - Generates plain-text RIB.
 *         <LI>Binary - Generates binary RIB. 
 *       </UL>
 *     </DIV> <BR>
 * 
 *     Compression <BR>
 *     <DIV style="margin-left: 40px;">
 *       The compression method to use for the output RIB file:<BR>
 *       <UL>
 *         <LI>None - Uncompressed. 
 *         <LI>GZip - Use gzip(1) to compress the output RIB.
 *       </UL>
 *     </DIV> <BR>
 *   </DIV><P>
 * 
 *   <I>MEL Scripts</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Pre Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The source node which contains the MEL script to evaluate before exporting begins. 
 *     </DIV> <BR>
 *   
 *     Post Export MEL <BR>
 *     <DIV style="margin-left: 40px;">
 *       The source node which contains the MEL script to evaluate after exporting ends. <BR>
 *     </DIV> 
 *   </DIV> 
 * </DIV> <P> 
 */
public
class MayaLqExportAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaLqExportAction() 
  {
    super("MayaLqExport", new VersionID("2.0.9"), "Temerity",
	  "Exports RIB input files for RenderMan based renderers from a Maya scene " + 
	  "using LiquidMaya.");
    
    underDevelopment();

    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene,
	 "The source Maya scene node.", 
	 null);
      addSingleParam(param);
    }

    /* image resolution */ 
    {
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aImageWidth,
	   "The horizontal resolution of the output image in pixels.", 
	   640);
	addSingleParam(param);
      }
      
      {
	ActionParam param = 
	  new IntegerActionParam
	  (aImageHeight,
	   "The vertical resolution of the output image in pixels.", 
	   480);
	addSingleParam(param);
      }
    
      {
	ActionParam param = 
	  new DoubleActionParam
	  (aPixelAspectRatio,
	   "Ratio of pixel height to pixel width.", 
	   1.0);
	addSingleParam(param);
      }

      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("320x240");
	choices.add("640x480");
	choices.add("1k Square");
	choices.add("2k Square");
	choices.add("3k Square");
	choices.add("4k Square");
	choices.add("CCIR PAL/Quantel PAL");
	choices.add("CCIR 601/Quantel NTSC");
	choices.add("Full 1024");
	choices.add("Full 1280/Screen");
	choices.add("HD 720");
	choices.add("HD 1080");
	choices.add("NTSC 4d");
	choices.add("PAL 768");
	choices.add("PAL 780");
	choices.add("Targa 486");
	choices.add("Target NTSC");
	choices.add("Targa PAL");

	addPreset("ImageResolution", choices);

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       320);
	  values.put(aImageHeight,      240);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "320x240", values);
	}
      
	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       640);
	  values.put(aImageHeight,      480);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "640x480", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      1024);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "1k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       2048);
	  values.put(aImageHeight,      2048);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "2k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       3072);
	  values.put(aImageHeight,      3072);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "3k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       4096);
	  values.put(aImageHeight,      4096);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "4k Square", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       720);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 1.066);
	
	  addPresetValues("ImageResolution", "CCIR PAL/Quantel PAL", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       720);
	  values.put(aImageHeight,      486);
	  values.put(aPixelAspectRatio, 0.900);
	
	  addPresetValues("ImageResolution", "CCIR 601/Quantel NTSC", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1024);
	  values.put(aImageHeight,      768);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "Full 1024", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1280);
	  values.put(aImageHeight,      1024);
	  values.put(aPixelAspectRatio, 1.066);
	
	  addPresetValues("ImageResolution", "Full 1280/Screen", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1280);
	  values.put(aImageHeight,      720);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "HD 720", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       1920);
	  values.put(aImageHeight,      1080);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "HD 1080", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       646);
	  values.put(aImageHeight,      485);
	  values.put(aPixelAspectRatio, 1.001);
	
	  addPresetValues("ImageResolution", "NTSC 4d", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       768);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 1.0);
	
	  addPresetValues("ImageResolution", "PAL 768", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       780);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 0.984);
	
	  addPresetValues("ImageResolution", "PAL 780", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      486);
	  values.put(aPixelAspectRatio, 1.265);
	
	  addPresetValues("ImageResolution", "Targa 486", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      482);
	  values.put(aPixelAspectRatio, 1.255);
	
	  addPresetValues("ImageResolution", "Target NTSC", values);
	}

	{
	  TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	  values.put(aImageWidth,       512);
	  values.put(aImageHeight,      576);
	  values.put(aPixelAspectRatio, 1.500);
	
	  addPresetValues("ImageResolution", "Targa PAL", values);
	}
      }
    }


    /* camera controls */ 
    {
      {
	ActionParam param = 
	  new StringActionParam
	  (aRenderCamera, 
	   "The name of the shape node in the Maya scene for the camera being rendered.",
	   "perspShape");
	addSingleParam(param);
      }
    
      /* motion blur */ 
      {
	{
	  ActionParam param = 
	    new BooleanActionParam
	    (aTransformationBlur,
	     "Include transformation blur motion blocks.", 
	     false);
	  addSingleParam(param);
	}
      
	{
	  ActionParam param = 
	    new BooleanActionParam
	    (aDeformationBlur,
	     "Include deformation blur motion blocks.", 
	     false);
	  addSingleParam(param);
	}
      
	{
	  ActionParam param = 
	    new BooleanActionParam
	    (aCameraBlur,
	     "Include motion blocks for the camera.", 
	     false);
	  addSingleParam(param);
	}
      
	{
	  ArrayList<String> choices = new ArrayList<String>();
	  choices.add("Open on Frame");
	  choices.add("Center on Frame");
	  choices.add("Center Between Frames");
	  choices.add("Close on Next Frame");
	
	  ActionParam param = 
	    new EnumActionParam
	    (aShutterTiming, 
	     "How shutter timing relates to frame timing.", 
	     "Open on Frame", choices);
	  addSingleParam(param);
	} 
      
	{
	  ActionParam param = 
	    new BooleanActionParam
	    (aShutterRelative,
	     "Shutter relative motion blocks.", 
	     false);
	  addSingleParam(param);
	}

	{
	  ActionParam param = 
	    new IntegerActionParam
	    (aMotionSamples,
	     "The number of samples per motion block.", 
	     2);
	  addSingleParam(param);
	}

	{
	  ActionParam param = 
	    new DoubleActionParam
	    (aMotionFactor, 
	     "The factor used to reduce shading rate on objects in motion.", 
	     1.0);
	  addSingleParam(param);
	}
      }
      
      /* depth of field */ 
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aDepthOfField, 
	   "Whether to enable depth of field.", 
	   false);
	addSingleParam(param);
      }
    }

    /* export details */ 
    {
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("Scene");
	choices.add("Archive");
	
	ActionParam param = 
	new EnumActionParam
	  (aExportMode, 
	   "The RIB generation mode.",
	   "Scene", choices);
	addSingleParam(param);
      } 

      {
	ActionParam param = 
	  new StringActionParam
	  (aExportSet, 
	   "The name of the Maya Set used to select the DAG nodes to export from the " +
	   "Maya scene. If unset, then the entire scene will be exported.", 
	   null);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aSurfaceShaders, 
	   "Whether to include surface shaders in the exported RIB.", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aDisplacementShaders, 
	   "Whether to include displacement shaders in the exported RIB.", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aLightShaders, 
	   "Whether to include light shaders in the exported RIB.", 
	   true);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aVolumeShaders, 
	   "Whether to include volume shaders in the exported RIB.", 
	   true);
	addSingleParam(param);
      }
    }

    /* extra RIB */ 
    {
      {
	ActionParam param = 
	  new LinkActionParam
	  (aBeforeFrameBegin,
	   "The node containing RIB to insert before the FrameBegin request.",
	   null);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new LinkActionParam
	  (aBeforeWorldBegin,
	   "The node containing RIB to insert before the WorldBegin request.",
	   null);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new LinkActionParam
	  (aAfterWorldBegin,
	   "The node containing RIB to insert after the WorldBegin request.",
	   null);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new LinkActionParam
	  (aBeforePrimsBegin,
	   "The node containing RIB to insert before any primitive requests.",
	   null);
	addSingleParam(param);
      }
    }

    /* output */ 
    {
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("ASCII");
	choices.add("Binary");
	
	ActionParam param = 
	  new EnumActionParam
	  (aOutputFormat, 
	   "The format of the output RIB file.",
	   "ASCII", choices);
	addSingleParam(param);
      } 
      
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("None");
	choices.add("Gzip");
	
	ActionParam param = 
	  new EnumActionParam
	  (aCompression, 
	   "The compression method to use for the output RIB file.",
	   "None", choices);
	addSingleParam(param);
      } 
    }

    /* MEL scripts */ 
    {
      {
	ActionParam param = 
	  new LinkActionParam
	  (aPreExportMEL,
	   "The pre-export MEL script.", 
	   null);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new LinkActionParam
	  (aPostExportMEL,
	   "The post-export MEL script.", 
	   null);
	addSingleParam(param);
      }
    }

    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);

      layout.addSeparator(); 
      layout.addEntry("ImageResolution");
      layout.addEntry(aImageWidth);
      layout.addEntry(aImageHeight);
      layout.addEntry(aPixelAspectRatio);

      {
	LayoutGroup cam = new LayoutGroup
	  ("Camera Controls", 
	   "Controls over various camera related settings.", 
	   false);
	cam.addEntry(aRenderCamera);
	cam.addSeparator(); 
	cam.addEntry(aDepthOfField);

	{
	  LayoutGroup group = new LayoutGroup
	    ("Motion Blur", 
	     "Controls over motion blur and shutter settings.", 
	     true);
	  group.addEntry(aTransformationBlur);
	  group.addEntry(aDeformationBlur);
	  group.addSeparator();
	  group.addEntry(aCameraBlur);
	  group.addEntry(aShutterTiming);
	  group.addEntry(aShutterRelative);
	  group.addSeparator();
	  group.addEntry(aMotionSamples);
	  group.addEntry(aMotionFactor);
	  
	  cam.addSubGroup(group);
	}

	layout.addSubGroup(cam);
      }
      
      {
	LayoutGroup group = new LayoutGroup
	  ("Export Details", 
	   "Controls over the RIB fragments exported.", 
	   false);
	group.addEntry(aExportMode);
	group.addEntry(aExportSet);
	group.addSeparator();
	group.addEntry(aSurfaceShaders);   
	group.addEntry(aDisplacementShaders);   
	group.addEntry(aLightShaders);   
	group.addEntry(aVolumeShaders);   
	
	layout.addSubGroup(group);
      }

      {
	LayoutGroup group = new LayoutGroup
	  ("Extra RIB", 
	     "Additional RIB fragments to be inserted into the exported RIB files.", 
	   false);
	group.addEntry(aBeforeFrameBegin);
	group.addEntry(aBeforeWorldBegin);   
	group.addEntry(aAfterWorldBegin);   
	group.addEntry(aBeforePrimsBegin);   
	
	layout.addSubGroup(group);
      }

      {
	LayoutGroup group = new LayoutGroup
	  ("Output", 
	   "Controls over output format.", 
	   false);
	group.addEntry(aOutputFormat);
	group.addEntry(aCompression);   

	layout.addSubGroup(group);
      }

      {
	LayoutGroup group = new LayoutGroup
	  ("MEL Scripts", 
	   "MEL scripts run at various stages of the exporting process.", 
	   false);
	group.addEntry(aPreExportMEL); 
	group.addEntry(aPostExportMEL);

	layout.addSubGroup(group);
      }

      setSingleLayout(layout);
    }
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
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */ 
    FileSeq cameraRIBs = null;
    FileSeq shadowBodyRIBs = null;
    ArrayList<FileSeq> lightRIBs = new ArrayList<FileSeq>();
    String camera = null;
    Path scene = null;
    boolean isArchive = false;
    Path preExport = null;
    Path postExport = null;
    {
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("rib") || !fseq.hasFrameNumbers())	   
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") must contain one or more RIB " + 
	     "files (.rib) with frame numbers!");

	cameraRIBs = fseq;
      }

      for(FileSeq fseq : agenda.getSecondaryTargets()) {
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("rib") || !fseq.hasFrameNumbers())	   
	  throw new PipelineException
	    ("The secondary file sequence (" + fseq + ") must contain one or more RIB " + 
	     "files (.rib) with frame numbers!");

	String prefix = fpat.getPrefix();
	if(!prefix.startsWith(cameraRIBs.getFilePattern().getPrefix() + "_")) 
	  throw new PipelineException
	    ("The secondary RIB file sequence (" + fseq + ") did not have the same " +
	     "prefix as the primary RIB file sequence (" + cameraRIBs + ")!");

	if(prefix.endsWith("SHADOWBODY")) {
	  if(shadowBodyRIBs != null) 
	    throw new PipelineException
	      ("Somehow there was more than one SHADOWBODY secondary RIB file sequence!");
	  shadowBodyRIBs = fseq;
	}
	else if(prefix.endsWith("DSH") || prefix.endsWith("SHD")) {
	  lightRIBs.add(fseq);
	}
	else {
	  throw new PipelineException
	    ("Unexpected secondary RIB file sequence (" + fseq + ") encountered!");
	}
      }

      {
	String sname = (String) getSingleParamValue(aMayaScene); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Maya Scene node (" + sname + ") was not one of the source " + 
	       "nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || 
	     (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
	    throw new PipelineException
	      ("The MayaLqExport Action requires that the source node specified by the " +
	       "Maya Scene parameter (" + sname + ") must have a single Maya scene file " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  scene = new Path(PackageInfo.sProdPath,
			   snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
	else {
	  throw new PipelineException
	    ("The MayaLqExport Action requires the Maya Scene parameter to be set!");
	}
      }      

      {
	camera = (String) getSingleParamValue(aRenderCamera); 
	if((camera == null) || (camera.length() == 0)) 
	  throw new PipelineException
	    ("The MayaLqExport Action requires that the Render Camera must be specified!");
      }

      {
	EnumActionParam param = (EnumActionParam) getSingleParam(aExportMode);
	if((param == null) || (param.getIndex() == -1))
	  throw new PipelineException
	    ("The ExportMode was illegal!");

	isArchive = (param.getIndex() == 1); 
      }
      
      {
	String sname = (String) getSingleParamValue(aPreExportMEL); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Pre Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaLqExport Action requires that the source node specified by the " +
	       "Pre Export MEL parameter (" + sname + ") must have a single MEL script " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  preExport = new Path(PackageInfo.sProdPath,
			       snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
      }

      {
	String sname = (String) getSingleParamValue(aPostExportMEL); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Post Export MEL node (" + sname + ") was not one of the " + 
	       "source nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("mel"))) 
	    throw new PipelineException
	      ("The MayaLqExport Action requires that the source node specified by the " +
	       "Post Export MEL parameter (" + sname + ") must have a single MEL script " + 
	       "as its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  postExport = new Path(PackageInfo.sProdPath,
				snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	}
      }
    }

    /* create a temporary MEL script */ 
    Path tempRIB = getTempPath(agenda);
    File mel = createTemp(agenda, 0644, "mel");
    try {      
      FileWriter out = new FileWriter(mel);

      out.write("//setProject(\"" + tempRIB.toOsString() + "\");\n" + 
		"\n" + 
		"if(! `pluginInfo -q -l liquid`)\n" + 
		"  loadPlugin -qt \"liquid.so\";\n" +
		"liquidStartup;\n\n");
		
      if(preExport != null) 
	out.write("source \"" + preExport.toOsString() + "\";\n\n");

      /* image resolution */ 
      {
	Integer width = (Integer) getSingleParamValue(aImageWidth); 
	if((width == null) || (width <= 0)) 
	  throw new PipelineException
	    ("The value of ImageWidth (" + width + ") was illegal!");
	
	Integer height = (Integer) getSingleParamValue(aImageHeight); 
	if((height == null) || (height <= 0)) 
	  throw new PipelineException
	    ("The value of ImageHeight (" + height + ") was illegal!");

	Double ratio = (Double) getSingleParamValue(aPixelAspectRatio);
	if((ratio == null) || (ratio <= 0.0)) 
	  throw new PipelineException
	    ("The value of PixelAspectRatio (" + ratio + ") was illegal!");
	
	out.write("setAttr \"liquidGlobals.xResolution\" " + width + ";\n" + 
		  "setAttr \"liquidGlobals.yResolution\" " + height + ";\n" + 
		  "setAttr \"liquidGlobals.pixelAspectRatio\" " + ratio + ";\n");
      }
	
      /* animation */ 
      {
	FilePattern fpat = cameraRIBs.getFilePattern();
	FrameRange range = cameraRIBs.getFrameRange();

	out.write("setAttr \"liquidGlobals.doAnimation\" true;\n" + 
		  "setAttr \"liquidGlobals.startFrame\" " + range.getStart() + ";\n" + 
		  "setAttr \"liquidGlobals.endFrame\" " + range.getEnd() + ";\n" + 
		  "setAttr \"liquidGlobals.frameStep\" " + range.getBy() + ";\n");
	
	if(fpat.getPadding() < 2) {
	  out.write("setAttr \"liquidGlobals.doPadding\" false;\n" +
		    "setAttr \"liquidGlobals.padding\" 0;\n");
	}
	else {
	  out.write("setAttr \"liquidGlobals.doPadding\" true;\n" + 
		    "setAttr \"liquidGlobals.padding\" " + fpat.getPadding() + ";\n");
	}
      }

      /* camera controls */ 
      {
	out.write("setAttr -type \"string\" \"liquidGlobals.renderCamera\" " + 
		  "\"" + camera + "\";\n" + 
		  "setAttr \"liquidGlobals.rotateCamera\" false;\n");

	{
	  Boolean tf = (Boolean) getSingleParamValue(aDepthOfField);
	  out.write("setAttr \"liquidGlobals.depthOfField\" " + 
		    ((tf != null) && tf) + ";\n");
	}	

	/* motion blur */ 
	{
	  Boolean tf = (Boolean) getSingleParamValue(aTransformationBlur);
	  out.write("setAttr \"liquidGlobals.transformationBlur\" " + 
		    ((tf != null) && tf) + ";\n");
	}

	{
	  Boolean tf = (Boolean) getSingleParamValue(aDeformationBlur);
	  out.write("setAttr \"liquidGlobals.deformationBlur\" " + 
		    ((tf != null) && tf) + ";\n");
	}

	{
	  Boolean tf = (Boolean) getSingleParamValue(aCameraBlur);
	  out.write("setAttr \"liquidGlobals.cameraBlur\" " + 
		    ((tf != null) && tf) + ";\n");
	}

	{
	  EnumActionParam param = (EnumActionParam) getSingleParam(aShutterTiming);
	  if((param == null) || (param.getIndex() == -1))
	    throw new PipelineException
	      ("The ShutterTiming was illegal!");

	  out.write("setAttr \"liquidGlobals.shutterConfig\" " + param.getIndex() + ";\n");
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue(aShutterRelative);
	  out.write("setAttr \"liquidGlobals.relativeMotion\" " + 
		    ((tf != null) && tf) + ";\n");
	}
	
	Integer samples = (Integer) getSingleParamValue(aMotionSamples); 
	if((samples == null) || (samples < 2)) 
	  throw new PipelineException
	    ("The value of MotionSamples (" + samples + ") was illegal!");

	Double factor = (Double) getSingleParamValue(aMotionFactor);
	if((factor == null) || (factor <= 0.0)) 
	  throw new PipelineException
	    ("The value of MotionFactor (" + factor + ") was illegal!");
      }

      /* RIB file naming */ 
      out.write
	("setAttr -type \"string\" liquidGlobals.ribName " + 
	 "\"" + cameraRIBs.getFilePattern().getPrefix() + "\";\n" +
	 "setAttr \"liquidGlobals.beautyRibHasCameraName\" false;\n" +
	 "setAttr -type \"string\" \"liquidGlobals.shotName\" \"\";\n" + 
	 "setAttr -type \"string\" \"liquidGlobals.shotVersion\" \"\";\n");
      
      /* directories */ 
      out.write
	("setAttr -type \"string\" \"liquidGlobals.pictureDirectory\" " + 
	 "\"" + tempRIB.toOsString() + "\";\n" +
	 "setAttr -type \"string\" \"liquidGlobals.textureDirectory\" " + 
	 "\"" + tempRIB.toOsString() + "\";\n" +
	 "setAttr -type \"string\" \"liquidGlobals.ribDirectory\" " + 
	 "\"" + tempRIB.toOsString() + "\";\n" +
	 "setAttr -type \"string\" \"liquidGlobals.shaderDirectory\" " + 
	 "\"" + tempRIB.toOsString() + "\";\n" +
	 "setAttr -type \"string\" \"liquidGlobals.tempDirectory\" " + 
	 "\"" + tempRIB.toOsString() + "\";\n" + 
	 "setAttr \"liquidGlobals.createOutputDirectories\" false;\n");

      /* search paths */ 
      out.write
	("setAttr -type \"string\" \"liquidGlobals.shaderPath\" \"&\";\n" +
	 "setAttr -type \"string\" \"liquidGlobals.texturePath\" \"&\";\n" +
	 "setAttr -type \"string\" \"liquidGlobals.archivePath\" \"&\";\n" +
	 "setAttr -type \"string\" \"liquidGlobals.proceduralPath\" \"&\";\n");
      
      /* display */ 
      out.write
	("setAttr \"liquidGlobals.outputHeroPass\" true;\n" + 
	 "setAttr \"liquidGlobals.outputShadowPass\" false;\n" + 
	 "setAttr -type \"string\" liquidGlobals.ddImageType[0] \"file\"\n;" + 
	 "setAttr -type \"string\" liquidGlobals.ddImageMode[0] \"rgba\"\n;" + 
	 "setAttr \"liquidGlobals.beautyRibHasCameraName\" false;\n");

      /* before & after RIB (ignored) */ 
      out.write
	("setAttr -type \"string\" \"liquidGlobals.preframeMel\" \"\";\n" + 
	 "setAttr -type \"string\" \"liquidGlobals.postframeMel\" \"\";\n"); 

      /* export details */ 
      {
	out.write("setAttr \"liquidGlobals.exportReadArchive\" " + isArchive + ";\n");

	{
	  Boolean tf = (Boolean) getSingleParamValue(aSurfaceShaders); 
	  out.write("setAttr \"liquidGlobals.ignoreSurfaces\" " + 
		    ((tf == null) || !tf) + ";\n");
	}
      
	{
	  Boolean tf = (Boolean) getSingleParamValue(aDisplacementShaders); 
	  out.write("setAttr \"liquidGlobals.ignoreDisplacements\" " + 
		    ((tf == null) || !tf) + ";\n");
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue(aLightShaders); 
	  out.write("setAttr \"liquidGlobals.ignoreLights\" " + 
		    ((tf == null) || !tf) + ";\n");
	}
	
	{
	  Boolean tf = (Boolean) getSingleParamValue(aVolumeShaders); 
	  out.write("setAttr \"liquidGlobals.ignoreVolumes\" " + 
		    ((tf == null) || !tf) + ";\n");
	}
      }

      /* extra RIB */ 
      {
	
	// setAttr ".prfb" -type "string" "# before frame begin";
	// setAttr ".prw"  -type "string" "# before world begin";
	// setAttr ".pow"  -type "string" "# after world begin";
	// setAttr ".prg"  -type "string" "# before primitives";
	
	
      }

      /* RIB format */ 
      out.write("setAttr \"liquidGlobals.binaryOutput\" false;\n" +
		"setAttr \"liquidGlobals.compressedOutput\" false;\n");

      /* job options */ 
      out.write("setAttr \"liquidGlobals.justRib\" true;\n" +
		"setAttr \"liquidGlobals.launchRender\" false;\n" +
		"setAttr \"liquidGlobals.useRenderScript\" false;\n");

      /* export the RIBs */ 
      {
	String exportSet = (String) getSingleParamValue(aExportSet); 
	if((exportSet != null) && (exportSet.length() > 0)) {
	  out.write("select -r \"" + exportSet + "\";\n" + 
		    "liquid -GL -selected;\n");
	}
	else {
	  out.write("liquid -GL;\n"); 
	}
      }

      if(postExport != null) 
	out.write("source \"" + postExport.toOsString() + "\";\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary MEL script file (" + mel + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create a temporary script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      
      out.write("#!/bin/bash\n\n" + 
		"echo Exporting Temporary RIBs...\n" +
		"maya -batch -script " + mel + " -file " + scene + "\n" + 
		"echo Done.\n" + 
		"echo\n" + 
		"\n" + 
		"echo Creating Final RIBs...\n");

      
      String liquidHome = agenda.getEnvironment().get("LIQUIDHOME");
      if(liquidHome == null) 
	throw new PipelineException
	  ("Somehow LIQUIDHOME was not defined in the toolset environment!");

      String liqshaders = (liquidHome + "/shaders");

      String liqrman = agenda.getEnvironment().get("LIQRMAN");
      if(liqrman == null) 
	throw new PipelineException
	  ("Somehow LIQRMAN was not defined in the toolset environment!");

      Boolean compress = false;
      {
	EnumActionParam param = (EnumActionParam) getSingleParam(aCompression);
	if((param == null) || (param.getIndex() == -1))
	  throw new PipelineException
	    ("The Compression was illegal!");

	compress = (param.getIndex() == 1);
      }

      Boolean binary = false;
      {
	EnumActionParam param = (EnumActionParam) getSingleParam(aOutputFormat);
	if((param == null) || (param.getIndex() == -1))
	  throw new PipelineException
	    ("The OutputFormat was illegal!");

	binary = (param.getIndex() == 1);
      }

      Path working = new Path(PackageInfo.sWorkPath, 
			      nodeID.getAuthor() + "/" + nodeID.getView());

      Path npath = new Path(nodeID.getName());
      String relParent = npath.getParent().substring(1); 

      for(Path rpath : cameraRIBs.getPaths()) {
	Path path = new Path(tempRIB, rpath);
	out.write
	  ("cat " + path.toOsString() + " " + 
	   "| sed -e '/#    Scene :/ s|" + working.toOsString() + "|$WORKING|g' " + 
	   "| sed -e '/Option \"searchpath\" \"shader\"/ " + 
	     "s|:" + liqshaders + "||g' " +  
	   "| sed -e '/Option \"searchpath\"/ s|:" + tempRIB.toOsString() + "/||g' " + 
	   "| sed -e '/Display/ s|" + tempRIB.toOsString() + "/||g' " + 
	   "| sed -e 's|" + working.toOsString() + "/||g' ");
	formatRIBs(out, liqrman, compress, binary, path, rpath);
      }

      for(Path rpath : shadowBodyRIBs.getPaths()) {
	Path path = new Path(tempRIB, rpath);
	out.write("cat " + path.toOsString() + " ");
	formatRIBs(out, liqrman, compress, binary, path, rpath);
      }

      for(FileSeq fseq : lightRIBs) {
	for(Path rpath : fseq.getPaths()) {
	  Path path = new Path(tempRIB, rpath);
	  out.write
	    ("cat " + path.toOsString() + " " + 
	     "| sed -e '/#    Scene :/ s|" + working.toOsString() + "|$WORKING|g' " + 
	     "| sed -e '/Option \"searchpath\" \"shader\"/ " + 
	       "s|:" + liqshaders + "||g' " +  
	     "| sed -e '/Option \"searchpath\"/ s|:" + tempRIB.toOsString() + "/||g' " + 
	     "| sed -e '/Display/ s|" + tempRIB.toOsString() + "/||g' " + 
	     "| sed -e '/ReadArchive/ " + 
	       "s|" + tempRIB.toOsString() + "|" + relParent + "|g' ");
	  formatRIBs(out, liqrman, compress, binary, path, rpath);
	}
      }

      out.write("echo ALL DONE.\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.getPath());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }

  /**
   * Generate a shell script fragment which converts, compresses and copies a temporary 
   * RIB file to the target location.
   */ 
  private void 
  formatRIBs
  (
   FileWriter out, 
   String liqrman, 
   boolean compress, 
   boolean binary, 
   Path path, 
   Path rpath
  ) 
    throws PipelineException, IOException 
  {
    if(!compress && !binary) {
      out.write("> " + rpath.toOsString() + "\n\n");
    }
    else if(liqrman.startsWith("prman")) {
      out.write("> " + path.toOsString() + ".tmp\n" + 
		"catrib");
      
      if(compress) 
	out.write(" -gzip");
      
      if(binary) 
	out.write(" -binary");
      
      out.write(" -o " + rpath.toOsString() + " " + path.toOsString() + ".tmp\n\n"); 
    }
    else if(liqrman.equals("3delight")) {
      out.write("> " + path.toOsString() + ".tmp\n" + 
		"renderdl -noinit -catrib");
      
      if(compress) 
	out.write(" -gzip");
      
      if(binary) 
	out.write(" -binary");
      
      out.write(path.toOsString() + ".tmp > " + rpath.toOsString() + "\n\n");
    }
    else {
      if(binary) 
	throw new PipelineException
	  ("Binary RIB output not supported for the (" + liqrman + ") renderer!");
      
      if(compress) 
	out.write(" | gzip --stdout --best ");
      
      out.write("> " + rpath.toOsString() + "\n\n");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7525138567087491948L; 

  private static final String aMayaScene           = "MayaScene";
  private static final String aRenderPass          = "RenderPass";

  private static final String aImageWidth          = "ImageWidth";
  private static final String aImageHeight         = "ImageHeight";
  private static final String aPixelAspectRatio    = "PixelAspectRatio";

  private static final String aRenderCamera        = "RenderCamera";
  private static final String aDepthOfField        = "DepthOfField";
  private static final String aTransformationBlur  = "TransformationBlur";
  private static final String aDeformationBlur     = "DeformationBlur";
  private static final String aCameraBlur          = "CameraBlur";
  private static final String aShutterTiming       = "ShutterTiming";
  private static final String aShutterRelative     = "ShutterRelative";
  private static final String aMotionSamples       = "MotionSamples";
  private static final String aMotionFactor        = "MotionFactor";

  private static final String aExportMode          = "ExportMode";
  private static final String aExportSet           = "ExportSet";
  private static final String aSurfaceShaders      = "SurfaceShaders";
  private static final String aDisplacementShaders = "DisplacementShaders";
  private static final String aLightShaders        = "LightShaders";
  private static final String aVolumeShaders       = "VolumeShaders";

  private static final String aBeforeFrameBegin    = "BeforeFrameBegin";
  private static final String aBeforeWorldBegin    = "BeforeWorldBegin";
  private static final String aAfterWorldBegin     = "AfterWorldBegin";
  private static final String aBeforePrimsBegin    = "BeforePrimsBegin";

  private static final String aOutputFormat        = "OutputFormat";
  private static final String aCompression         = "Compression";

  private static final String aPreExportMEL        = "PreExportMEL";
  private static final String aPostExportMEL       = "PostExportMEL";

}

