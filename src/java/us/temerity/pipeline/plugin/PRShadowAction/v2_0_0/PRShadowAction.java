// $Id: PRShadowAction.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.PRShadowAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P R   S H A D O W   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generate optimized PhotoRealistic RendreMan shadow maps from ZFile depth maps. <P> 
 * 
 * Converts the images which make up the primary file sequence of one of the source
 * nodes into the texture maps which make up the primary file sequence of this node. <P>
 * 
 * See the <A href="https://renderman.pixar.com/products/tools/rps.html">RenderMan ProServer</A>
 * documentation for details about <B>txmake</B>(1). <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   ZFile Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the ZFile depth maps to convert. <BR> 
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Bit Depth <BR>
 *   <DIV style="margin-left: 40px;">
 *     The storage size of texels in the generated texture map.
 *     <UL>
 *       <LI> Byte - 8-bit integer.
 *       <LI> Short - 16-bit integer.
 *       <LI> Float - 32-bit floating point.
 *       <LI> Lossy - compressed floating point.
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Texture Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The file format of the generated texture map.
 *     <UL>
 *       <LI> Pixar
 *       <LI> TIFF
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 * 
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV>
 */
public
class PRShadowAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PRShadowAction() 
  {
    super("PRShadow", new VersionID("2.0.0"), "Temerity", 
	  "Generate optimized PhotoRealistic RendreMan shadow maps from ZFile depth maps.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ZFileSource",
	 "The source node which contains the image files to convert.",
	 null);
      addSingleParam(param);
    }


    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Byte");
      choices.add("Short");
      choices.add("Float");
      choices.add("Lossy");

      ActionParam param = 
	new EnumActionParam
	("BitDepth", 
	 "The storage size of texels in the generated texture map.",
	 "Float", choices);
      addSingleParam(param);
    }   
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Pixar");
      choices.add("TIFF");

      ActionParam param = 
	new EnumActionParam
	("TextureFormat", 
	 "The file format of the generated texture map.",
	 "TIFF", choices);
      addSingleParam(param);
    }   


    {
      ActionParam param = 
	new StringActionParam
	("ExtraOptions",
	 "Additional command-line arguments.", 
	 null);
      addSingleParam(param);
    }


    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ZFileSource");
      layout.addSeparator();      
      layout.addEntry("ExtraOptions");

      {
	LayoutGroup output = new LayoutGroup
	  ("Output", "Texture output and interpretation controls.", true);
	output.addEntry("BitDepth");
	output.addEntry("TextureFormat");
	
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
	String sname = (String) getSingleParamValue("ZFileSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The ZFile Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the ZFile Source (" + sname + ") was not one of the source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("z")) 
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain depth maps (.z)!");

	fromSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromPath = new File(PackageInfo.sProdDir, 
			    snodeID.getWorkingParent().toFile().getPath());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !(suffix.equals("tex") || suffix.equals("shd")))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain PhotoRealistic " + 
	     "RenderMan shadow maps (.shd|.tex)!");
	
	toSeq = fseq;
      }

      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + fromSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + toSeq + ")!");
    }

    ArrayList<String> args = new ArrayList<String>();
    args.add("-shadow");

    {
      EnumActionParam param = (EnumActionParam) getSingleParam("BitDepth");
      switch(param.getIndex()) {
      case 0:
	break;

      case 1:
	args.add("-short");
	break;

      case 2:
	args.add("-float");
	break;
	
      case 3:
	args.add("-float");
	args.add("-lossy");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Filter Pattern value!");	
      }
    }

    {
      args.add("-format");
      EnumActionParam param = (EnumActionParam) getSingleParam("TextureFormat");
      switch(param.getIndex()) {
      case 0:
	args.add("pixar");
	break;

      case 1:
	args.add("tiff");
	break;

      default:
	throw new PipelineException
	  ("Illegal Texture Format value!");	
      }
    }

    addExtraOptions(args);

    if(toSeq.numFrames() == 1) {
      try {
	args.add(fromPath + "/" + fromSeq.getFile(0));
	args.add(toSeq.getFile(0).toString());
	
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   "txmake", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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
	  StringBuilder buf = new StringBuilder();
	  buf.append("txmake");
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

  /**
   * Append any additional command-line arguments.
   */ 
  private void 
  addExtraOptions
  (
   ArrayList<String> args
  ) 
    throws PipelineException
  {
    String extra = (String) getSingleParamValue("ExtraOptions");
    if(extra == null) 
      return;

    String parts[] = extra.split("\\p{Space}");
    int wk;
    for(wk=0; wk<parts.length; wk++) {
      if(parts[wk].length() > 0) 
	args.add(parts[wk]);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3230202554361895895L;

}

