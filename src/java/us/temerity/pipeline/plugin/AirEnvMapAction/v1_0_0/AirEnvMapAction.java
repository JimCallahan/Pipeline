// $Id: AirEnvMapAction.java,v 1.1 2007/06/17 15:34:38 jim Exp $

package us.temerity.pipeline.plugin.AirEnvMapAction.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   E N V   M A P   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generate optimized AIR latitude/longitude environment maps from source images. <P> 
 * 
 * Converts the images which make up the primary file sequence of one of the source
 * nodes into the environment maps which make up the primary file sequence of this node. <P>
 * 
 * See the <A href="http://www.sitexgraphics.com/html/air.html">AIR</A> documentation for 
 * <A href="http://www.sitexgraphics.com/air.pdf"><B>mktex</B></A>(1) for details. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images files to convert. <BR> 
 *   </DIV> <BR>
 * 
 *   Input Format <BR>
 *   <DIV style="margin-left: 40px;">
 *     The geometric format of the source images:
 *     <UL>
 *       <LI> Lat/Long - Rectangular latitude/longitude images.
 *       <LI> Angular - Angular images acquired by a light probe.
 *     </UL>
 *   </DIV> <BR>
 *   
 *   S Mode <BR>
 *   T Mode <BR>
 *   <DIV style="margin-left: 40px;">
 *     The method used to lookup texels outside the [0,1] S/T coordinate range: 
 *     <UL>
 *       <LI>Black - Use black for all values outside [0,1] range.
 *       <LI>Clamp - Use border texel color for values outside [0,1] range.
 *       <LI>Peridoc - Tiles texture outside [0,1] range.
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
 *   Rotate <BR>
 *   <DIV style="margin-left: 40px;">
 *     Rotate the output environment map the given angle. <BR>
 *   </DIV> <BR>
 * 
 *   Tile Size <BR>
 *   <DIV style="margin-left: 40px;">
 *     The mipmap level tile size.", 
 *   </DIV> <BR>
 * 
 *   Pixel Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The pixel data type of the output environment maps.<BR>
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whther to compress the output environment maps.<BR>
 *   </DIV> <BR>
 * </DIV>
 */
public
class AirEnvMapAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirEnvMapAction() 
  {
    super("AirEnvMap", new VersionID("1.0.0"), "Temerity", 
	  "Generates optimized AIR latitude/longitude environment maps from source images.");
    
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
      choices.add("Lat/Long");
      choices.add("Angular");

      ActionParam param = 
	new EnumActionParam
	("InputFormat", 
	 "The geometric format of the source images.",
	 "Lat/Long", choices);
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
      choices.add("None");
      choices.add("90 deg");
      choices.add("180 deg");
      choices.add("270 deg"); 

      ActionParam param = 
	new EnumActionParam
	("Rotate", 
	 "Rotate the output environment map the given angle.", 
	 "None", choices);
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
	 "32x32", choices);
      addSingleParam(param);
    }     

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("8-Bit");
      choices.add("16-Bit");
      choices.add("Float");

      ActionParam param = 
	new EnumActionParam
	("PixelType",
	 "The pixel data type of the output text.", 
	 "8-Bit", choices);
      addSingleParam(param);
    }     

    {
      ActionParam param = 
	new BooleanActionParam
	("Compression", 
	 "Whther to compress the output environment maps.",
	 true);
      addSingleParam(param);
    }      
    
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ImageSource");
      layout.addEntry("InputFormat");
      layout.addSeparator();
      layout.addEntry("Mode");
      layout.addEntry("SMode");
      layout.addEntry("TMode");
      layout.addSeparator();
      layout.addEntry("Flip");
      layout.addEntry("Rotate");
      layout.addSeparator();
      layout.addEntry("TileSize");	
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
	if((suffix == null) || !suffix.equals("tx"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain AIR " + 
	     "environment maps (.tx)!");
	
	toSeq = fseq;
      }

      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + fromSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + toSeq + ")!");
    }

    ArrayList<String> args = new ArrayList<String>();

    {
      EnumActionParam param = (EnumActionParam) getSingleParam("InputFormat");
      switch(param.getIndex()) {
      case 0:
	args.add("-envlatl");
	break;

      case 1:
	args.add("-angular");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Input Format value!");
      }
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
	args.add("-flipx");
	break;

      case 2:
	args.add("-flipy");
	break;
	
      case 3:
	args.add("-flipx");
	args.add("-flipy");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Flip value!");
      }
    }

    {
      EnumActionParam param = (EnumActionParam) getSingleParam("Rotate");
      switch(param.getIndex()) {
      case 0:
	break;

      case 1:
	args.add("-rot");
	args.add("90");
	break;

      case 2:
	args.add("-rot");
	args.add("180");
	break;
	
      case 3:
	args.add("-rot");
	args.add("270");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Rotate value!");
      }
    }

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
	args.add("-float");
	break;

      default:
	throw new PipelineException
	  ("Illegal Pixel Type value!");
      }
    }

    Boolean compress = (Boolean) getSingleParamValue("Compression");
    if((compress == null) || !compress) 
      args.add("-u");
    
    if(toSeq.numFrames() == 1) {
      try {
	args.add(fromPath + "/" + fromSeq.getFile(0));
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
	  StringBuilder buf = new StringBuilder();
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

  private static final long serialVersionUID = -4622859750462662503L;

}

