// $Id: PxEnvCubeAction.java,v 1.1 2007/06/17 15:34:45 jim Exp $

package us.temerity.pipeline.plugin.PxEnvCubeAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P X   E N V   M A P   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generate optimized Pixie cube faced environment maps from six directional source 
 * images. <P> 
 * 
 * Converts the six images [+x, -x, +y, -y, +z, -z] which make up the primary file 
 * sequence of one of the source nodes into the single cubic environment map which is the 
 * single member of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://pixie.sourceforge.net/">Pixie</A> documentation for details 
 * about <B>texmake</B>(1). <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images files to convert. <BR> 
 *   </DIV> <BR>
 *   <BR>
 *
 *   Field Of View <BR>
 *   <DIV style="margin-left: 40px;">
 *     The field-of-view (in degrees) of each environment cube face.
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Filter <BR>
 *   <DIV style="margin-left: 40px;">
 *     The filter used to downsample the source images to generate the mipmap levels.
 *     <UL>
 *       <LI> Box 
 *       <LI> Triangle 
 *       <LI> Gaussian  
 *       <LI> Catmul-Rom 
 *       <LI> Sinc 
 *     </UL>
 *   </DIV> <BR>
 * 
 *   S Filter Width <BR>
 *   T Filter Width <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the width (diameter) of the downsizing filter in the S/T directions.
 *   </DIV> <BR>
 *   <BR>
 * 
 *   
 *   S Mode <BR>
 *   T Mode <BR>
 *   <DIV style="margin-left: 40px;">
 *     The method used to lookup texels outside the [0,1] S/T coordinate range: 
 *     <UL>
 *       <LI>Black - Use black for all values outside [0,1] range.
 *       <LI>Clamp - Use border texel color for values outside [0,1] range.
 *       <LI>Periodic - Tiles texture outside [0,1] range.
 *       <LI>Non-Periodic - ???
 *       <LI>No Wrap - ???
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Tile Size <BR>
 *   <DIV style="margin-left: 40px;">
 *     The mipmap level tile size.", 
 *   </DIV> <BR>
 * </DIV>
 */
public
class PxEnvCubeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PxEnvCubeAction() 
  {
    super("PxEnvCube", new VersionID("2.0.0"), "Temerity", 
	  "Generate optimized Pixie cube faced environment maps from six directional " + 
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
      ActionParam param = 
	new DoubleActionParam
	("FieldOfView", 
	 "The field-of-view (in degrees) of each environment cube face.", 
	 90.0);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Box");
      choices.add("Triangle");
      choices.add("Gaussian");
      choices.add("Catmul-Rom");
      choices.add("Sinc");

      ActionParam param = 
	new EnumActionParam
	("Filter", 
	 "The filter used to downsample the source images to generate the mipmap levels.",
	 "Sinc", choices);
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
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Black");
      choices.add("Clamp");
      choices.add("Periodic");
      choices.add("Non-Periodic");
      choices.add("No Wrap");

      {
	ActionParam param = 
	  new EnumActionParam
	  ("SMode", 
	   "The method used to lookup texels outside the [0,1] S-coordinate range.",
	   "Black", choices);
	addSingleParam(param);
      }

      {
	ActionParam param = 
	  new EnumActionParam
	  ("TMode", 
	   "The method used to lookup texels outside the [0,1] T-coordinate range.",
	   "Black", choices);
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

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("SMode", "Non-Periodic");
	values.put("TMode", "Non-Periodic");
	
	addPresetValues("Mode", "Non-Periodic", values);
      }

      {
	TreeMap<String,Comparable> values = new TreeMap<String,Comparable>();
	values.put("SMode", "No Wrap");
	values.put("TMode", "No Wrap");
	
	addPresetValues("Mode", "No Wrap", values);
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
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ImageSource");
      layout.addSeparator();      
      layout.addEntry("FieldOfView");

      {
	LayoutGroup filter = new LayoutGroup
	  ("Filtering", "Mipmap level generation filtering controls.", true);
	filter.addEntry("Filter");
	filter.addSeparator();      
	filter.addEntry("SFilterWidth");
	filter.addEntry("TFilterWidth");

	layout.addSubGroup(filter);
      }

      {
	LayoutGroup output = new LayoutGroup
	  ("Output", "Texture output and interpretation controls.", true);
	output.addEntry("Mode");
	output.addEntry("SMode");
	output.addEntry("TMode");
	output.addSeparator();
	output.addEntry("TileSize");
	
	layout.addSubGroup(output);	
      }

      setSingleLayout(layout);   
    }

    underDevelopment();
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
	fromPath = new File(PackageInfo.sProdDir, 
			    snodeID.getWorkingParent().toFile().getPath());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("tex"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a Pixie " + 
	     "textures (.tex)!");
	
	if(fseq.numFrames() != 1)
	  throw new PipelineException
	    ("The target file sequence (" + fseq + ") must be a single environment map!");
	
	target = fseq.getFile(0);
      }
    }

    ArrayList<String> args = new ArrayList<String>();
    args.add("-envcube");

    {
      Double fov = (Double) getSingleParamValue("FieldOfView");
      if(fov != null) {
	if(fov < 0.0) 
	  throw new PipelineException
	    ("The Field Of View (" + fov + ") cannot be negative!");

	args.add("-fov");
	args.add(fov.toString());
      }
    }

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
	args.add("sinc");
	break;
	
      default:
	throw new PipelineException
	  ("Illegal Filter value!");	
      }
    }

    Double swidth = (Double) getSingleParamValue("SFilterWidth");
    if(swidth != null) {
      if(swidth < 0.0) 
	throw new PipelineException
	  ("The S Filter Width (" + swidth + ") cannot be negative!");

      args.add("-sfilterwidth");
      args.add(swidth.toString());
    }

    Double twidth = (Double) getSingleParamValue("TFilterWidth");
    if(twidth != null) {
      if(twidth < 0.0) 
	throw new PipelineException
	  ("The T Filter Width (" + twidth + ") cannot be negative!");

      args.add("-tfilterwidth");
      args.add(twidth.toString());
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
	
      case 3:
	args.add("nonperiodic");
	break;
	
      case 4:
	args.add("nowrap");
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
	
      case 3:
	args.add("nonperiodic");
	break;
	
      case 4:
	args.add("nowrap");
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

    try {
      for(File source : fromSeq.getFiles())
	args.add(fromPath + "/" + source); 

	  //      ArrayList<File> sources = fromSeq.getFiles();
//       args.add(fromPath + "/" + sources.get(0)); 
//       args.add(fromPath + "/" + sources.get(2)); 
//       args.add(fromPath + "/" + sources.get(4)); 
//       args.add(fromPath + "/" + sources.get(1)); 
//       args.add(fromPath + "/" + sources.get(3)); 
//       args.add(fromPath + "/" + sources.get(5)); 

      args.add(target.toString());

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "texmake", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -7623441811283517123L;

}

