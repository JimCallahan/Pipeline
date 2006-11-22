// $Id: GelEnvProbeAction.java,v 1.4 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   G E L   E N V   P R O B E   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generate optimized Gelato cube faced environment maps from source spherical 
 * lightprobe images.
 * 
 * Converts the light probe images which make up the primary file sequence of one of the 
 * source nodes into the environment maps which make up the primary file sequence of this 
 * node. <P>
 * 
 * See the Gelato documentation for details about <B>maketx</B>(1). <P> 
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
 *   Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The image format of the generated environment maps:
 *     <UL>
 *       <LI> TIFF 
 *       <LI> OpenEXR
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Resize <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to resize the input image resolution to the nearest power of two.
 *   </DIV> <BR>
 * 
 *   Tile Size <BR>
 *   <DIV style="margin-left: 40px;">
 *     The mipmap level tile size.", 
 *   </DIV> <BR>
 * 
 *   Planar Config <BR>
 *   <DIV style="margin-left: 40px;">
 *     The planar configuration of the output environment maps:
 *     <UL>
 *       <LI> Contiguous
 *       <LI> Seperate
 *     </UL>
 *   </DIV> <P>
 * 
 * 
 *   Debug DSO <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to print additional information about DSO loading.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV>
 */
public
class GelEnvProbeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GelEnvProbeAction() 
  {
    super("GelEnvProbe", new VersionID("2.0.0"), "Temerity",
	  "Generates optimized Gelato cube faced environment maps from source spherical " +
	  "lightprobe images.");
    
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
      choices.add("TIFF");
      choices.add("OpenEXR");

      ActionParam param = 
	new EnumActionParam
	("Format",
	 "The image format of the generated environment maps.",
	 "TIFF", choices);
      addSingleParam(param);
    }     

    {
      ActionParam param = 
	new BooleanActionParam
	("Resize",
	 "Whether to resize the input image resolution to the nearest power of two.", 
	 true);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("-");
      choices.add("8x8");
      choices.add("16x16");
      choices.add("32x32");
      choices.add("64x64");
      choices.add("128x128");

      ActionParam param = 
	new EnumActionParam
	("TileSize",
	 "The mipmap level tile size.", 
	 "-", choices);
      addSingleParam(param);
    }     

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Contiguous");
      choices.add("Seperate");

      ActionParam param = 
	new EnumActionParam
	("PlanarConfig",
	 "The planar configuration of the output environment maps.",
	 "Contiguous", choices);
      addSingleParam(param);
    }     

    

    {
      ActionParam param = 
	new BooleanActionParam
	("DebugDSO",
	 "Whether to print additional information about DSO loading.", 
	 false);
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
      layout.addEntry("ImageSource");
      layout.addSeparator();
      layout.addEntry("Format");
      layout.addEntry("Resize");
      layout.addEntry("TileSize");
      layout.addEntry("PlanarConfig");
      layout.addSeparator();
      layout.addEntry("DebugDSO");
      layout.addEntry("ExtraOptions");
	
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
	fromPath = new File(PackageInfo.sProdDir, 
			    snodeID.getWorkingParent().toFile().getPath());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("gtx"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain Gelato " + 
	     "textures (.gtx)!");
	
	toSeq = fseq;
      }

      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + fromSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + toSeq + ")!");
    }

    ArrayList<String> args = new ArrayList<String>();

    args.add("-lightprobe");

    {
      args.add("-format");
      EnumActionParam param = (EnumActionParam) getSingleParam("Format");
      switch(param.getIndex()) {
      case 0:
	args.add("tiff");
	break;

      case 1:
	args.add("OpenEXR");
	break;

      default:
	throw new PipelineException
	  ("Illegal Format value!");
      }
    }
    
    Boolean resize = (Boolean) getSingleParamValue("Resize");
    if((resize == null) || !resize) 
      args.add("-noresize");

    {
      EnumActionParam param = (EnumActionParam) getSingleParam("PlanarConfig");
      switch(param.getIndex()) {
      case 0:
	break;

      case 1:
	args.add("-separate");
	break;

      default:
	throw new PipelineException
	  ("Illegal Planar Config value!");
      }
    }
    
    {
      EnumActionParam param = (EnumActionParam) getSingleParam("TileSize");
      switch(param.getIndex()) {
      case 0:
	break;
	
      case 1:
	args.add("-tilesize");
	args.add("8");
	break;

      case 2:
	args.add("-tilesize");
	args.add("16");
	break;

      case 3:
	args.add("-tilesize");
	args.add("32");
	break;
	
      case 4:
	args.add("-tilesize");
	args.add("64");
	break;
	
      case 5:
	args.add("-tilesize");
	args.add("128");
	break;

      default:
	throw new PipelineException
	  ("Illegal Tile Size value!");
      }
    }

    Boolean debug = (Boolean) getSingleParamValue("DebugDSO");
    if((debug != null) && debug) 
      args.add("-debugdso");
    
    addExtraOptions(args);
    
    if(toSeq.numFrames() == 1) {
      try {
	args.add(fromPath + "/" + fromSeq.getFile(0));
	args.add("-o");
	args.add(toSeq.getFile(0).toString());
	
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   "maketx", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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
	  buf.append("maketx");
	  for(String arg : args) 
	    buf.append(" " + arg);
	  cmdopts = buf.toString();
	}
      
	out.write("#!/bin/bash\n\n");

	ArrayList<File> fromFiles = fromSeq.getFiles();
	ArrayList<File> toFiles   = toSeq.getFiles();
	int wk;
	for(wk=0; wk<fromFiles.size(); wk++) 
	  out.write(cmdopts + " " + fromPath + "/" + fromFiles.get(wk) + " -o " + 
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

  private static final long serialVersionUID = -7168587445552407809L;

}

