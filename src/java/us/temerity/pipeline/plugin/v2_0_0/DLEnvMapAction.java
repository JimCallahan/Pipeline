// $Id: DLEnvMapAction.java,v 1.1 2005/07/23 21:57:58 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   E N V   M A P   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates optimized 3Delight latitude/longitude environment maps from source images. 
 * 
 * Converts the images which make up the primary file sequence of one of the source
 * nodes into the environment maps which make up the primary file sequence of this node. <P>
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for 
 * <A href="http://www.3delight.com/ZDoc/3delight_12.html"><B>tdlmake</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images files to convert. <BR> 
 *   </DIV> <BR>
 * 
 * 
 *   Quality <BR>
 *   <DIV style="margin-left: 40px;">
 *     The strategy for selecting the source image when downsampling mipmap levels:
 *     <UL>
 *       <LI> Low - Use previous mipmap level.
 *       <LI> Medium - Use 2nd previous mipmap level.
 *       <LI> High - Use 4th previous mipmap level.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Filter <BR>
 *   <DIV style="margin-left: 40px;">
 *     The filter used to downsample the source images to generate the mipmap levels.
 *     <UL>
 *       <LI> Box 
 *       <LI> Triangle 
 *       <LI> Gaussian  
 *       <LI> Catmul-Rom 
 *       <LI> Bessel 
 *       <LI> Sinc 
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Filter Window <BR>
 *   <DIV style="margin-left: 40px;">
 *     Windowing function used to soften boundry of Bessel or Sinc filters. 
 *     Ignored for all other filters.
 *     <UL>
 *       <LI> Lanczos
 *       <LI> Hamming
 *       <LI> Hann
 *       <LI> Blackman
 *       <LI> None
 *     </UL>
 *   </DIV> <BR>
 * 
 *   S Filter Width <BR>
 *   T Filter Width <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the width (diameter) of the downsizing filter in the S/T directions.
 *   </DIV> <BR>
 * 
 *   Blur Factor <BR>
 *   <DIV style="margin-left: 40px;">
 *     Scale factor applied to the filter function.
 *   </DIV> <BR>
 * 
 * 
 *   S Mode <BR>
 *   T Mode <BR>
 *   <DIV style="margin-left: 40px;">
 *     The method used to lookup texels outside the [0,1] S/T coordinate range: 
 *     <UL>
 *       <LI>Black - Use black for all values outside [0,1] range.
 *       <LI>Clamp - Use border texel color for values outside [0,1] range.
 *       <LI>Peridoc - Tiles environment maps outside [0,1] range.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Flip <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether and how to flip the output environment maps:
 *     <UL>
 *       <LI> None - No flipping is performed.
 *       <LI> S-Only - Flip image in the S direction.
 *       <LI> T-Only - Flip image in the T direction.
 *       <LI> Both - Flip in both the S and T directions. 
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output environment maps.<BR>
 *   </DIV> <BR>
 * </DIV>
 */
public
class DLEnvMapAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DLEnvMapAction() 
  {
    super("DLEnvMap", new VersionID("2.0.0"), 
	  "Generates optimized 3Delight latitude/longitude environment maps from " + 
	  "source images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node which contains the image files to convert.",
	 null);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Low");
      choices.add("Medium");
      choices.add("High");

      ActionParam param = 
	new EnumActionParam
	("Quality", 
	 "The strategy for selecting the source image when downsampling mipmap levels.",
	 "Medium", choices);
      addSingleParam(param);
    }       

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Box");
      choices.add("Triangle");
      choices.add("Gaussian");
      choices.add("Catmul-Rom");
      choices.add("Bessel");
      choices.add("Sinc");

      ActionParam param = 
	new EnumActionParam
	("Filter", 
	 "The filter used to downsample the source images to generate the mipmap levels.",
	 "Sinc", choices);
      addSingleParam(param);
    }   

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Lanczos");
      choices.add("Hamming");
      choices.add("Hann");
      choices.add("Blackman");
      choices.add("None");

      ActionParam param = 
	new EnumActionParam
	("FilterWindow", 
	 "Windowing function used to soften boundry of Bessel or Sinc filters.<BR>" +
	 "Ignored for all other filters.",
	 "Lanczos", choices);
      addSingleParam(param);
    }   
    
    {
      ActionParam param = 
	new DoubleActionParam
	("SFilterWidth", 
	 "Overrides the width (diameter) of the downsizing filter in the S direction.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("TFilterWidth", 
	 "Overrides the width (diameter) of the downsizing filter in the T direction.", 
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	("BlurFactor", 
	 "Scale factor applied to the filter function.", 
	 1.0);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Black");
      choices.add("Clamp");
      choices.add("Periodic");

      {
	ActionParam param = 
	  new EnumActionParam
	  ("SMode", 
	   "The method used to lookup texels outside the [0,1] S-coordinate range.",
	   "Periodic", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new EnumActionParam
	  ("TMode", 
	   "The method used to lookup texels outside the [0,1] T-coordinate range.",
	   "Periodic", choices);
	addSingleParam(param);
      }
	
      addPreset("Mode", choices);
      
      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("SMode", "Black");
	values.put("TMode", "Black");
	
	addPresetValues("Mode", "Black", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("SMode", "Clamp");
	values.put("TMode", "Clamp");
	
	addPresetValues("Mode", "Clamp", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("SMode", "Periodic");
	values.put("TMode", "Periodic");
	
	addPresetValues("Mode", "Periodic", values);
      }
    }   
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("S-Only");
      choices.add("T-Only");
      choices.add("Both"); 

      ActionParam param = 
	new EnumActionParam
	("Flip", 
	 "Whether and how to flip the output environment maps.",
	 "None", choices);
      addSingleParam(param);
    }   

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("LZW");
      choices.add("Deflate");
      choices.add("PackBits");
      choices.add("None");

      ActionParam param = 
	new EnumActionParam
	("Compression", 
	 "The compression method to use for the output TIF images.",
	 "LZW", choices);
      addSingleParam(param);
    }      
    
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ImageSource");

      {
	LayoutGroup filter = new LayoutGroup
	  ("Filtering", "Mipmap level generation filtering controls.", true);
	filter.addEntry("Quality");
	filter.addSeparator();      
	filter.addEntry("Filter");
	filter.addEntry("FilterWindow");
	filter.addEntry("BlurFactor");
	filter.addSeparator();      
	filter.addEntry("SFilterWidth");
	filter.addEntry("TFilterWidth");

	layout.addSubGroup(filter);
      }

      {
	LayoutGroup output = new LayoutGroup
	  ("Output", "Environment map output and interpretation controls.", true);
	output.addEntry("Mode");
	output.addEntry("SMode");
	output.addEntry("TMode");
	output.addSeparator();
	output.addEntry("Flip");
	output.addEntry("Compression");
	
	layout.addSubGroup(output);	
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

    /* file sequence checks */ 
    File fromPath = null;
    FileSeq fromSeq = null;
    FileSeq toSeq = null;
    {
      {    
	String sname = (String) getSingleParamValue("ImageSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Image Source (" + sname + ") was not one of the source nodes!");
	
	fromSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromPath = new File(PackageInfo.sProdDir, snodeID.getWorkingParent().getPath());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("tdl"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain 3Delight " + 
	     "environment maps (.tdl)!");
	
	toSeq = fseq;
      }

      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + fromSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + toSeq + ")!");
    }

    ArrayList<String> args = new ArrayList<String>();
    args.add("-envlatl");
    
    {
      args.add("-quality");
      EnumActionParam param = (EnumActionParam) getSingleParam("Quality");
      switch(param.getIndex()) {
      case 0:
	args.add("low");
	break;

      case 1:
	args.add("medium");
	break;

      case 2:
	args.add("high");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Quality value!");	
      }
    }

    boolean windowedFilter = false;
    {
      args.add("-filter");
      EnumActionParam param = (EnumActionParam) getSingleParam("Filter");
      switch(param.getIndex()) {
      case 0:
	args.add("box");
	break;

      case 1:
	args.add("triangle");
	break;

      case 2:
	args.add("gaussian");
	break;
	
      case 3:
	args.add("catmull-rom");
	break;

      case 4:
	args.add("bessel");
	windowedFilter = true;
	break;
	
      case 5:
	args.add("sinc");
	windowedFilter = true;
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Filter value!");	
      }
    }

    if(windowedFilter) {
      EnumActionParam param = (EnumActionParam) getSingleParam("FilterWindow");
      switch(param.getIndex()) {
      case 0:
	args.add("-window");
	args.add("lanczos");
	break;

      case 1:
	args.add("-window");
	args.add("hamming");
	break;

      case 2:
	args.add("-window");
	args.add("hann");
	break;
	
      case 3:
	args.add("-window");
	args.add("blackman");
	break;

      case 5:
	break;	
	
      default:
	throw new PipelineException
	  ("Illegal Quality value!");	
      }
    }
    
    Double swidth = (Double) getSingleParamValue("SFilterWidth");
    if(swidth != null) {
      if(swidth < 0.0) 
	throw new PipelineException
	  ("The S Filter Width (" + swidth + ") cannot be negative!");

      args.add("-swidth");
      args.add(swidth.toString());
    }

    Double twidth = (Double) getSingleParamValue("TFilterWidth");
    if(twidth != null) {
      if(twidth < 0.0) 
	throw new PipelineException
	  ("The T Filter Width (" + twidth + ") cannot be negative!");

      args.add("-twidth");
      args.add(twidth.toString());
    }

    Double blur = (Double) getSingleParamValue("BlurFactor");
    if(blur != null) {
      if(blur < 0.0) 
	throw new PipelineException
	  ("The Blur Factor (" + blur + ") cannot be negative!");

      args.add("-blur");
      args.add(blur.toString());
    }

    {
      args.add("-smode");
      EnumActionParam param = (EnumActionParam) getSingleParam("SMode");
      switch(param.getIndex()) {
      case 0:
	args.add("black");
	break;

      case 1:
	args.add("clamp");
	break;

      case 2:
	args.add("periodic");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal S Mode value!");
      }
    }
    
    {
      args.add("-tmode");
      EnumActionParam param = (EnumActionParam) getSingleParam("TMode");
      switch(param.getIndex()) {
      case 0:
	args.add("black");
	break;

      case 1:
	args.add("clamp");
	break;

      case 2:
	args.add("periodic");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal T Mode value!");
      }
    }
    
    {
      EnumActionParam param = (EnumActionParam) getSingleParam("Flip");
      switch(param.getIndex()) {
      case 0:
	break;

      case 1:
	args.add("-flips");
	break;

      case 2:
	args.add("-flipt");
	break;
	
      case 3:
	args.add("-flipst");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Flip value!");
      }
    }

    {
      EnumActionParam param = (EnumActionParam) getSingleParam("Compression");
      switch(param.getIndex()) {
      case 0:
	args.add("-lzw");
	break;

      case 1:
	args.add("-deflate");
	break;

      case 2:
	args.add("-packbits");
	break;
	
      case 3:
	args.add("-c-");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Compression value!");	
      }
    }
    
    if(toSeq.numFrames() == 1) {
      try {
	args.add(fromPath + "/" + fromSeq.getFile(0));
	args.add(toSeq.getFile(0).toString());
	
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   "tdlmake", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	   outFile, errFile);
      }
      catch(Exception ex) {
	throw new PipelineException
	  ("Unable to generate the SubProcess to perform this Action!\n" +
	   ex.getMessage());
      }
    }
    else {
      File script = createTemp(agenda, 0755, "bash");
      try {      
	FileWriter out = new FileWriter(script);

	String cmdopts = null;
	{
	  StringBuffer buf = new StringBuffer();
	  buf.append("tdlmake");
	  for(String arg : args) 
	    buf.append(" " + arg);
	  cmdopts = buf.toString();
	}
      
	out.write("#!/bin/bash\n\n");

	ArrayList<File> fromFiles = fromSeq.getFiles();
	ArrayList<File> toFiles   = toSeq.getFiles();
	int wk;
	for(wk=0; wk<fromFiles.size(); wk++) 
	  out.write(cmdopts + " " + fromPath + "/" + fromFiles.get(wk) + " " + 
		    toFiles.get(wk) + "\n");
	
	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to write temporary script file (" + script + ") for Job " + 
	   "(" + agenda.getJobID() + ")!\n" +
	   ex.getMessage());
      }

      try {
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   script.getPath(),  new ArrayList<String>(), 
	   agenda.getEnvironment(), agenda.getWorkingDir(), 
	   outFile, errFile); 
      }
      catch(Exception ex) {
	throw new PipelineException
	  ("Unable to generate the SubProcess to perform this Action!\n" +
	   ex.getMessage());
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //  private static final long serialVersionUID = 

}

