// $Id: MayaRenderGlobalsAction.java,v 1.1 2004/11/19 06:45:56 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E N D E R   G L O B A L S   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a MEL script which when executed by Maya will set many of the most useful global 
 * rendering parameters of the Maya scene. <P> 
 * 
 * This generated MEL script can then be used as the Pre Render script of a 
 * {@link MayaRenderAction MayaRender} action to allow control over rendering parameters
 * directly from Pipeline without the need to reopen Maya. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
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
 *   </DIV> 
 * </DIV>
 */
public
class MayaRenderGlobalsAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaRenderGlobalsAction() 
  {
    super("MayaRenderGlobals", new VersionID("1.0.0"), 
	  "Creates a MEL script which sets the render globals of a Maya scene.");
    
    {
      ActionParam param = 
	new IntegerActionParam
	("ImageWidth",
	 "The horizontal resolution of the output image in pixels.", 
	 640);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	("ImageHeight",
	 "The vertical resolution of the output image in pixels.", 
	 480);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	("PixelAspectRatio",
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
	values.put("ImageWidth",       320);
	values.put("ImageHeight",      240);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "320x240", values);
      }
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       640);
	values.put("ImageHeight",      480);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "640x480", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       1024);
	values.put("ImageHeight",      1024);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "1k Square", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       2048);
	values.put("ImageHeight",      2048);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "2k Square", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       3072);
	values.put("ImageHeight",      3072);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "3k Square", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       4096);
	values.put("ImageHeight",      4096);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "4k Square", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       720);
	values.put("ImageHeight",      576);
	values.put("PixelAspectRatio", 1.066);
	
	addPresetValues("ImageResolution", "CCIR PAL/Quantel PAL", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       720);
	values.put("ImageHeight",      486);
	values.put("PixelAspectRatio", 0.900);
	
	addPresetValues("ImageResolution", "CCIR 601/Quantel NTSC", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       1024);
	values.put("ImageHeight",      768);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "Full 1024", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       1280);
	values.put("ImageHeight",      1024);
	values.put("PixelAspectRatio", 1.066);
	
	addPresetValues("ImageResolution", "Full 1280/Screen", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       1280);
	values.put("ImageHeight",      720);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "HD 720", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       1920);
	values.put("ImageHeight",      1080);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "HD 1080", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       646);
	values.put("ImageHeight",      485);
	values.put("PixelAspectRatio", 1.001);
	
	addPresetValues("ImageResolution", "NTSC 4d", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       768);
	values.put("ImageHeight",      576);
	values.put("PixelAspectRatio", 1.0);
	
	addPresetValues("ImageResolution", "PAL 768", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       780);
	values.put("ImageHeight",      576);
	values.put("PixelAspectRatio", 0.984);
	
	addPresetValues("ImageResolution", "PAL 780", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       512);
	values.put("ImageHeight",      486);
	values.put("PixelAspectRatio", 1.265);
	
	addPresetValues("ImageResolution", "Targa 486", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       512);
	values.put("ImageHeight",      482);
	values.put("PixelAspectRatio", 1.255);
	
	addPresetValues("ImageResolution", "Target NTSC", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("ImageWidth",       512);
	values.put("ImageHeight",      576);
	values.put("PixelAspectRatio", 1.500);
	
	addPresetValues("ImageResolution", "Targa PAL", values);
      }
    }
    

    // ...



    {
      LayoutGroup layout = new LayoutGroup("ActionParameters", true);
      layout.addEntry("ImageResolution");
      layout.addEntry("ImageWidth");
      layout.addEntry("ImageHeight");
      layout.addSeparator(); 
      layout.addEntry("PixelAspectRatio");

      setSingleLayout(layout);
    }

      // ...

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
    /* sanity checks */ 
    NodeID nodeID = agenda.getNodeID();
    FileSeq fseq = agenda.getPrimaryTarget();
    if(!fseq.isSingle() || !fseq.getFilePattern().getSuffix().equals("mel"))
      throw new PipelineException
	("The MayaRenderGlobals Action requires that primary target file sequence must " + 
	 "be a single MEL script!"); 

    /* create a temporary shell script */ 
    File script = createTemp(agenda, 0644, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      
      File target = new File(PackageInfo.sProdDir, 
			     nodeID.getWorkingParent() + "/" + fseq.getFile(0));

      out.write("cat > " + target + " <<EOF\n");

      /* image resolution */ 
      {
	Integer width  = (Integer) getSingleParamValue("ImageWidth"); 
	if((width == null) || (width <= 0)) 
	  throw new PipelineException
	    ("The ImageWidth (" + width + ") was illegal!");

	Integer height = (Integer) getSingleParamValue("ImageHeight"); 
	if((height == null) || (height <= 0)) 
	  throw new PipelineException
	    ("The ImageHeight (" + height + ") was illegal!");

	Double ratio = (Double) getSingleParamValue("PixelAspectRatio");
	if((ratio == null) || (ratio <= 0.0)) 
	  throw new PipelineException
	    ("The PixelAspectRatio (" + ratio + ") was illegal!");

	double deviceRatio = (width.doubleValue() / height.doubleValue()) * ratio;

	out.write
	  ("// IMAGE RESOLUTION\n" + 
	   "setAttr \"defaultResolution.aspectLock\" 0;\n" + 
	   "setAttr \"defaultResolution.width\" " + width + ";\n" + 
	   "setAttr \"defaultResolution.height\" " + height + ";\n" + 
	   "setAttr \"defaultResolution.aspectLock\" 0;\n" + 
	   "setAttr \"defaultResolution.deviceAspectRatio\" " + deviceRatio + ";\n\n");
      }


      // ...
      


      out.write("EOF\n");
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write the target MEL script file (" + script + ") for Job " + 
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6216311731394337147L;

}

