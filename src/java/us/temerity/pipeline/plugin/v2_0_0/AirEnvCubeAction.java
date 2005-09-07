// $Id: AirEnvCubeAction.java,v 1.3 2005/09/07 19:17:08 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   E N V   C U B E   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generate an optimized AIR cubic environment map from six direction source images.
 * 
 * Converts the size images [+x, -x, +y, -y, +z, -z] which make up the primary file 
 * sequence of one of the source nodes into the single cubic environment map which is the 
 * single member of the primary file sequence of this node. <P> 
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
 *   Tile Size <BR>
 *   <DIV style="margin-left: 40px;">
 *     The mipmap level tile size.", 
 *   </DIV> <BR>
 * 
 *   Pixel Type <BR>
 *   <DIV style="margin-left: 40px;">
 *     The pixel data type of the output texture maps.<BR>
 *   </DIV> <BR>
 * 
 *   Compression <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whther to compress the output texture maps.<BR>
 *   </DIV> <BR>
 * </DIV>
 */
public
class AirEnvCubeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirEnvCubeAction() 
  {
    super("AirEnvCube", new VersionID("2.0.0"), "Temerity", 
	  "Generate an optimized AIR cubic environment map from six direction " + 
	  "source images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node which contains the six image files [+x, -x, +y, -y, +z, -z] " + 
	 "to convert.",
	 null);
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
	 "Whether to compress the output texture maps.",
	 true);
      addSingleParam(param);
    }      
    
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ImageSource");
      layout.addSeparator();
      layout.addEntry("Mode");
      layout.addEntry("SMode");
      layout.addEntry("TMode");
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
    File target = null;
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
	
	if(fseq.numFrames() != 6)
	  throw new PipelineException
	    ("The source file sequence (" + fseq + ") must contain exactly six " + 
	     "[+x, -x, +y, -y, +z, -z] images!");

	fromSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromPath = new File(PackageInfo.sProdDir, snodeID.getWorkingParent().getPath());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("tx"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain an AIR " + 
	     "environment map (.tx)!");
	
	if(fseq.numFrames() != 1)
	  throw new PipelineException
	    ("The target file sequence (" + fseq + ") must be a single environment map!");
	
	target = fseq.getFile(0);
      }
    }

    ArrayList<String> args = new ArrayList<String>();
    args.add("-envcube");
    
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


    try {
      for(File source : fromSeq.getFiles()) 
	args.add(fromPath + "/" + source); 
      args.add(target.toString());
      
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



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2005850027282542433L;

}

