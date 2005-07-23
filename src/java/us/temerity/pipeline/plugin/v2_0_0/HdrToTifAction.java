// $Id: HdrToTifAction.java,v 1.1 2005/07/23 21:57:58 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H D R   T O   T I F   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Converts high dynamic range (HDR) images into low dynamic range TIFF images suitable
 * for display by common images viewers. <P> 
 * 
 * Converts the HDR images which make up the primary file sequence of one of the source
 * nodes into the TIFF images which make up the primary file sequence of this node. <P>
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for 
 * <A href="http://www.3delight.com/ZDoc/3delight_14.html"><B>hdri2tif</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   HDR Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the HDR files to convert. <BR> 
 *   </DIV> 
 * 
 *   Middle Gray <BR>
 *   <DIV style="margin-left: 40px;">
 *     The middle gray value of the image. <BR> 
 *   </DIV> <BR>
 * 
 *   Scales <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of gaussian convolutions to use when computing luminance information 
 *     for each pixel. <BR> 
 *   </DIV> <BR>
 * 
 *   Sharpness <BR>
 *   <DIV style="margin-left: 40px;">
 *     The gaussian convolution sharpness. <BR>
 *   </DIV> <BR>
 * 
 *   Gamma <BR>
 *   <DIV style="margin-left: 40px;">
 *     The post conversion gamma correction factor. <BR> 
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     The compression method to use for the output TIF images.<BR>
 *   </DIV> <BR>
 * </DIV>
 */
public
class HdrToTifAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HdrToTifAction() 
  {
    super("HdrToTif", new VersionID("2.0.0"), 
	  "Converts high dynamic range (HDR) images into low dynamic range TIF images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("HDRSource",
	 "The source node containing the high dynamic range (HDR) images to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("MiddleGray", 
	 "The middle gray value of the image.", 
	 0.18);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("Scales", 
	 "The number of gaussian convolutions to use when computing luminance " + 
	 "information for each pixel.",
	 8);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("Sharpness", 
	 "The gaussian convolution sharpness.",
	 8);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	("Gamma", 
	 "The post conversion gamma correction factor.",
	 1.0);
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
      layout.addEntry("HDRSource");
      layout.addSeparator();
      layout.addEntry("MiddleGray");
      layout.addEntry("Scales");
      layout.addEntry("Sharpness");
      layout.addSeparator();
      layout.addEntry("Gamma");
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
	String sname = (String) getSingleParamValue("HDRSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The HDR Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the HDR Source (" + sname + ") was not one of the source nodes!");
	
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
    args.add("-verbose");

    Double gray = (Double) getSingleParamValue("MiddleGray");
    if(gray != null) {
      if((gray < 0.0) || (gray > 1.0)) 
	throw new PipelineException
	  ("The Middle Gray value (" + gray + ") was outside the valid range: [0,1]!");
      args.add("-key");
      args.add(gray.toString());
    }
    
    Integer scales = (Integer) getSingleParamValue("Scales");
    if(scales != null) {
      args.add("-nscales");
      args.add(scales.toString());
    }

    Integer sharp = (Integer) getSingleParamValue("Sharpness");
    if(sharp != null) {
      args.add("-sharpness");
      args.add(sharp.toString());
    }
    
    Double gamma = (Double) getSingleParamValue("Gamma");
    if(gamma != null) {
      args.add("-gamma");
      args.add(gamma.toString());
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
	   "hdri2tif", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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
	  buf.append("hdri2tif");
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

  //private static final long serialVersionUID = 

}

