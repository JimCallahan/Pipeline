// $Id: DsmToTifAction.java,v 1.1 2005/05/11 01:10:10 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D S M   T O   T I F   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts a depth slice from a deep shadow map (DSM) into a TIF image suitable for 
 * display. <P>
 * 
 * Converts the DSMs (.shd) which make up the primary file sequence of one of the source
 * nodes into the TIFF images which make up the primary file sequence of this node. 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for 
 * <A href="http://www.3delight.com/ZDoc/3delight_13.html"><B>dsm2tif</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   DSM Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the DSM files to convert. <BR> 
 *   </DIV> 
 * 
 *   Sample Method <BR>
 *   <DIV style="margin-left: 40px;">
 *     The depth sampling method: <BR>
 *     <UL>
 *       <LI>Relative - Each pixel is evaluated at a relative depth between the minimum and 
 *           maximum depth values for the sampled pixel.
 *       <LI>Absolute - Each pixel is evaluated a depth between the minimum and maximim 
 *           depth values of the entire DSM.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Sample Depth <BR>
 *   <DIV style="margin-left: 40px;">
 *     The sample depth [0,1].  The interpretation of this depth depends on the Sample Method.
 *   </DIV> <BR>
 * 
 *   Shadow Bias <BR>
 *   <DIV style="margin-left: 40px;">
 *     The shadow bias used to avoid self occulsion when evaluating the DSM.
 *   </DIV> <BR>
 * 
 *   Mipmap Level <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the mipmap level of the DSM to evaluate.
 *   </DIV> <BR>
 * 
 *   Output Value <BR>
 *   <DIV style="margin-left: 40px;">
 *     The meaning of color values in the output TIF images:<BR>
 *     <UL>
 *       <LI> Visibility - White means unocculded, black means fully occluded.
 *       <LI> Opacity - White means fully occluded, black meand unoccluded.
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Pixel Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The pixel data type of the output TIF images.<BR>
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output TIF images.<BR>
 *   </DIV> <BR>
 * </DIV>
 */
public
class DsmToTifAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DsmToTifAction() 
  {
    super("DsmToTif", new VersionID("1.0.0"), 
	  "Converts a depth slice from a deep shadow map (DSM) into a TIF image " + 
	  "suitable for display.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("DSMSource",
	 "The source node containing the deep shadow maps (DSM) to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Absolute");
      choices.add("Relative");

      ActionParam param = 
	new EnumActionParam
	("SampleMethod", 
	 "The depth sampling method.", 
	 "Absolute", choices);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	("SampleDepth", 
	 "The sample depth [0,1].  The interpretation of this depth depends on the " + 
	 "Sample Method.", 
	 1.0);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new DoubleActionParam
	("ShadowBias", 
	 "The shadow bias used to avoid self occulsion when evaluating the DSM.", 
	 0.015); 
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("MipmapLevel", 
	 "Specifies the mipmap level of the DSM to evaluate.", 
	 0);
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Visibility");
      choices.add("Opacity");

      ActionParam param = 
	new EnumActionParam
	("OutputValue", 
	 "The meaning of color values in the output TIF images.",
	 "Visibility", choices);
      addSingleParam(param);
    }      

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("8-Bit");
      choices.add("16-Bit");
      choices.add("32-Bit");
      choices.add("Float");

      ActionParam param = 
	new EnumActionParam
	("PixelType",
	 "The pixel data type of the output TIF images.", 
	 "8-Bit", choices);
      addSingleParam(param);
    }      

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("LZW");
      choices.add("Deflate");
      choices.add("PackBits");
      choices.add("LogLuv");

      ActionParam param = 
	new EnumActionParam
	("Compression", 
	 "The compression method to use for the output TIF images.",
	 "LZW", choices);
      addSingleParam(param);
    }      
    
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("DSMSource");
      layout.addSeparator();
      layout.addEntry("SampleMethod");
      layout.addEntry("SampleDepth");
      layout.addSeparator();
      layout.addEntry("ShadowBias");
      layout.addEntry("MipmapLevel");
      layout.addSeparator();
      layout.addEntry("OutputValue");
      layout.addEntry("PixelType");   
      layout.addEntry("Compression");   

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
	String sname = (String) getSingleParamValue("DSMSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The DSM Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the DSM Source (" + sname + ") was not one of the source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("shd")) 
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain deep shadow " + 
	     "maps (.shd)!");

	fromSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromPath = new File(PackageInfo.sProdDir, snodeID.getWorkingParent().getPath());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !(suffix.equals("tiff") || suffix.equals("tif")))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain TIF images!");
	
	toSeq = fseq;
      }

      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + fromSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + toSeq + ")!");
    }

    ArrayList<String> args = new ArrayList<String>();

    {
      Double depth = (Double) getSingleParamValue("SampleDepth");
      if(depth == null) 
	throw new PipelineException
	  ("The Sample Depth not specified!");
	
      if((depth < 0.0) || (depth > 1.0)) 
	throw new PipelineException
	  ("The Sample Depth value (" + depth + ") was outside the valid range: [0,1]!");

      EnumActionParam param = (EnumActionParam) getSingleParam("SampleMethod");
      switch(param.getIndex()) {
      case 0:
	args.add("-Z");
	break;
	
      case 1:
	args.add("-z");
	break;

      default:
	throw new PipelineException
	  ("Illegal Sample Method!");
      }
      
      args.add(depth.toString());
    }

    Double bias = (Double) getSingleParamValue("ShadowBias");
    if(bias != null) {
      args.add("-bias");
      args.add(bias.toString());
    }

    Integer level = (Integer) getSingleParamValue("MipmapLevel");
    if(level != null) {
      args.add("-mipmap");
      args.add(level.toString());
    }

    {
      EnumActionParam param = (EnumActionParam) getSingleParam("OutputValue");
      switch(param.getIndex()) {
      case 0:
	break;

      case 1:
	args.add("-opacity");
	break;

      default:
	throw new PipelineException
	  ("Illegal Output Value!"); 
      }
    }

    boolean isFloat = false;
    {
      EnumActionParam param = (EnumActionParam) getSingleParam("PixelType");
      switch(param.getIndex()) {
      case 0:
	args.add("-8");
	break;

      case 1:
	args.add("-16");
	break;

      case 2:
	args.add("-32");
	break;

      case 3:
	args.add("-float"); 
	isFloat = true;
	break;

      default:
	throw new PipelineException
	  ("Illegal Pixel Type!"); 
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
	if(!isFloat) 
	  throw new PipelineException
	    ("The LogLuv compression method can only be used with the Float pixel type!");
	args.add("-logluv");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Compression method!");
      }
    }
    
    if(toSeq.numFrames() == 1) {
      try {
	args.add(fromPath + "/" + fromSeq.getFile(0));
	args.add(toSeq.getFile(0).toString());
	
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   "dsm2tif", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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
	  buf.append("dsm2tif");
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

  private static final long serialVersionUID = 8894009513023780712L;

}

