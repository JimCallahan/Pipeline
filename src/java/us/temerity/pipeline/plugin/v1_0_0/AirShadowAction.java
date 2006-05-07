// $Id: AirShadowAction.java,v 1.3 2006/05/07 21:30:13 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   S H A D O W   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Convert ZFile depth maps into optimized AIR shadow maps. <P> 
 * 
 * Converts the ZFile depth maps (.z) which make up the primary file sequence of one of the 
 * source nodes into shadow maps (.sm) which make up the primary file sequence of this node. 
 * Shadow maps may be built in two ways.  If the LinkRelationship between the target shadow
 * map node and the source depth map node is OneToOne, each shadow map is generated 
 * independently from the corresponding individual depth map.  If the LinkRelationship is 
 * All, then all source depth maps will be combined into a single shadow map which must be 
 * the only member of the target primary file sequence.  Up to (6) depth maps may be combined 
 * in this way. <P> 
 * 
 * See the <A href="http://www.sitexgraphics.com/html/air.html">AIR</A> documentation for 
 * <A href="http://www.sitexgraphics.com/air.pdf"><B>mktex</B></A>(1) for details. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   ZFile Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the ZFile depth maps to convert. <BR> 
 *   </DIV> 
 * 
 *   Tile Size <BR>
 *   <DIV style="margin-left: 40px;">
 *     The mipmap level tile size.", 
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whther to compress the output texture maps.<BR>
 *   </DIV> <BR>
 * </DIV>
 */
public
class AirShadowAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirShadowAction() 
  {
    super("AirShadow", new VersionID("1.0.0"), "Temerity", 
	  "Convert ZFile depth maps into optimized AIR shadow maps.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ZFileSource",
	 "The source node containing the ZFile depth maps to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("8x8");
      choices.add("16x16");
      choices.add("32x32");
      choices.add("64x64");
      choices.add("128x128");

      ActionParam param = 
	new EnumActionParam
	("TileSize",
	 "The mipmap level tile size.", 
	 "64x64", choices);
      addSingleParam(param);
    }     

    {
      ActionParam param = 
	new BooleanActionParam
	("Compression", 
	 "Whther to compress the output texture maps.",
	 true);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ZFileSource");
      layout.addSeparator();
      layout.addEntry("TileSize");
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
	if((suffix == null) || !suffix.equals("sm"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain optimized " + 
	     "AIR shadow maps (.sm)!");
	
	toSeq = fseq;
      }

      if(toSeq.numFrames() == 1) {
	if(fromSeq.numFrames() > 6) 
	  throw new PipelineException 
	    ("The source primary file sequence (" + fromSeq + ") cannot have more than " +
	     "(6) depthmaps when generating a single (" + toSeq + ") shadow map!");
      }
      else {
	if(fromSeq.numFrames() != toSeq.numFrames()) 
	  throw new PipelineException 
	    ("The source primary file sequence (" + fromSeq + ") does not have the same" + 
	     "number of frames as the target primary file sequence (" + toSeq + ")!");
      }
    }

    ArrayList<String> args = new ArrayList<String>();
    args.add("-shadow"); 

    {
      args.add("-tilesize");
      EnumActionParam param = (EnumActionParam) getSingleParam("TileSize");
      switch(param.getIndex()) {
      case 0:
	args.add("8");
	break;

      case 1:
	args.add("16");
	break;

      case 2:
	args.add("32");
	break;
	
      case 3:
	args.add("64");
	break;
	
      case 4:
	args.add("128");
	break;

      default:
	throw new PipelineException
	  ("Illegal Tile Size value!");
      }
    }

    Boolean compress = (Boolean) getSingleParamValue("Compression");
    if((compress == null) || !compress) 
      args.add("-u");


    if(toSeq.numFrames() == 1) {
      try {
	for(File file : fromSeq.getFiles())
	  args.add(fromPath + "/" + file);
	args.add(toSeq.getFile(0).toString());
	
	return new SubProcessHeavy
	  (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	   "mktex", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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
	  buf.append("mktex");
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

  private static final long serialVersionUID = -8869140949887866336L;

}

