// $Id: MayaTestBuildAction.java,v 1.1 2007/02/13 05:27:17 jesse Exp $

package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E F E R E N C E   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new Maya scene from component scenes. <P> 
 * 
 * A new empty scene is first created.  The component scenes are either referenced as Maya 
 * references or directly imported depending on a per-source parameter from each source 
 * node who's primary file sequence is a Maya scene file ("ma" or "mb").  
 * 
 * At each stage in the process, an optional MEL script may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the Intial MEL, Model MEL and Final MEL single valued
 * parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 * 
 *   Linear Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The linear unit that the generated scene will use. 
 *   </DIV> <BR>
 * 
 *   Angular Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The angular unit that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Time Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The unit of time and frame rate that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Start Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *     The first frame of the built Maya scene
 *   </DIV> <BR>
 *   
 *   End Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *     The last frame of the built Maya scene
 *   </DIV> <BR>
 *   
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate just after scene creation
 *      and before importing any models.
 *   </DIV> <BR>
 * 
 *   Model MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after importing all models,
 *      but before saving the generated Maya scene.
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after saving the generated 
 *      Maya scene. <BR> 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Prefix Name <BR>
 *   <DIV style="margin-left: 40px;">
 *      The namespace prefix for the referenced scene in Maya instead of the filename.
 *   </DIV> <BR>
 *   
 *    Build Type<BR>
 *   <DIV style="margin-left: 40px;">
 *      Tells Maya to either Import or Reference the model.
 *   </DIV> <BR>
 *   
 *   Use Namespace<BR>
 *   <DIV style="margin-left: 40px;">
 *      Should Maya use the namespace flag to do the import.  
 *      Highly recomended to turn this on.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MayaTestBuildAction
  extends BaseAction
{  

/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
public
  MayaTestBuildAction() 
  {
    super("MayaTestBuild", new VersionID("1.0.0"), "SCEA",
	  "Builds a Maya scene from component scenes.");
    
    underDevelopment();  //use this so that this plugin can be reloaded...
    
    {
    	ActionParam param = new LinkActionParam ("InitialMEL", "The MEL script to evaluate after scene creation and before importing models.", null);
    	addSingleParam(param);
    }
    
    {
    	ActionParam param = new LinkActionParam ("ModelMEL", "The MEL script to evaluate after importing models but before saving the scene.", null);
    	addSingleParam(param);
    }
    
    {
    	ActionParam param = new LinkActionParam ("FinalMEL", "The MEL script to evaluate after saving the scene.", null);
    	addSingleParam(param);
    }
    
    {
    	ArrayList<String> choices = new ArrayList<String>();
    	choices.add("millimeter");
    	choices.add("centimeter");
    	choices.add("meter");
    	choices.add("inch");
    	choices.add("foot");
    	choices.add("yard");
      
    	ActionParam param = new EnumActionParam (aLinearUnits, "The linear units format the constructed maya scene should use.", "centimeter", choices);
    	addSingleParam(param);
    }
    
    {
    	ArrayList<String> choices = new ArrayList<String>();
    	choices.add("degrees");
    	choices.add("radians");
      
    	ActionParam param = new EnumActionParam (aAngularUnits, "The angular units format the constructed maya scene should use.", "degrees", choices);
    	addSingleParam(param);
    }
    
    {
    	ArrayList<String> choices = new ArrayList<String>();
    	choices.add("15 fps");
    	choices.add("Film (24 fps)");
    	choices.add("PAL (25 fps)");
    	choices.add("NTSC (30 fps)");
    	choices.add("Show (48 fps)");
    	choices.add("PAL Field (50 fps)");
    	choices.add("NTSC Field (60 fps)");
    	choices.add("milliseconds");
    	choices.add("seconds");
    	choices.add("minutes");
    	choices.add("hours");
    	choices.add("2fps");
    	choices.add("3fps");
    	choices.add("4fps");
    	choices.add("5fps");
    	choices.add("6fps");
    	choices.add("8fps");
    	choices.add("10fps");
    	choices.add("12fps");
    	choices.add("16fps");
    	choices.add("20fps");
    	choices.add("40fps");
    	choices.add("75fps");
    	choices.add("80fps");
    	choices.add("100fps");
    	choices.add("120fps");
    	choices.add("150fps");
    	choices.add("200fps");
    	choices.add("240fps");
    	choices.add("250fps");
    	choices.add("300fps");
    	choices.add("375fps");
    	choices.add("400fps");
    	choices.add("500fps");
    	choices.add("600fps");
    	choices.add("750fps");
    	choices.add("1200fps");
    	choices.add("1500fps");
    	choices.add("2000fps");
    	choices.add("3000fps");
    	choices.add("6000fps");
           
    	ActionParam param = new EnumActionParam (aTimeUnits, "The time format the constructed maya scene should use.", "Film (24 fps)", choices);
    	addSingleParam(param);
    }
    
    {
    	ActionParam param = new IntegerActionParam (aStartFrame, "The start frame for the scene.", 1);
    	addSingleParam(param);
    }
    
    {
    	ActionParam param = new IntegerActionParam (aEndFrame, "The endframe for the scene.", 10);
    	addSingleParam(param);
    }
    
    {
    	ActionParam param = new StringActionParam(aComments,"Some comments.", "");
    	addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aLinearUnits);
      layout.addEntry(aAngularUnits);
      layout.addEntry(aTimeUnits);
      layout.addSeparator();
      layout.addEntry(aStartFrame);
      layout.addEntry(aEndFrame);
      layout.addSeparator();
      layout.addEntry("InitialMEL");
      layout.addEntry("ModelMEL");
      layout.addEntry("FinalMEL");
      layout.addSeparator();
      layout.addEntry(aComments);
      

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
    //addSupport(OsType.Windows);   // SHOULD WORK, BUT UNTESTED
  }



  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,ActionParam> getInitialSourceParams()
  {
	  TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

	  {
		  ActionParam param = new StringActionParam ("PrefixName", "The namespace prefix for the referenced scene in Maya instead of the filename.", null);
		  params.put(param.getName(), param);
	  }
    
	  {
		  ActionParam param = new BooleanActionParam (aNameSpace, "Should Maya use the namespace flag to do the import. Highly recomended to turn " + "this on.", true);
		  params.put(param.getName(), param);
	  }
    
	  {
		  ArrayList<String> choices = new ArrayList<String>();
		  choices.add("Import");
		  choices.add("Reference");
		  ActionParam param = new EnumActionParam (aBuildType, "Should Maya Import or Reference the source.", "Reference", choices);  	 
		  params.put(param.getName(), param);
	  }

	  return params;
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
  public SubProcessHeavy prep(ActionAgenda agenda, File outFile, File errFile) throws PipelineException
  {
    /* sanity checks */ 
    Path initialMel = null;
    Path modelMel = null;
    Path finalMel = null;
    TreeMap<String,Path> modelPaths = new TreeMap<String,Path>();
    Path saveScene = null;
    String linearUnit;
    int angularIndex;
    int timeIndex;
    boolean isAscii = false;
    
    {
      /* MEL script filenames */ 
      initialMel = getMelPath("InitialMEL", "Initial MEL", agenda);
      modelMel   = getMelPath("ModelMEL", "Model MEL", agenda);
      finalMel   = getMelPath("FinalMEL", "Final MEL", agenda);

      /* model filenames */ 
      for(String sname : agenda.getSourceNames())
      {
    	  FileSeq fseq = agenda.getPrimarySource(sname);
    	  String suffix = fseq.getFilePattern().getSuffix();
    	  
    	  if(fseq.isSingle() && (suffix != null))
    	  {
    		  if(suffix.equals("ma") || suffix.equals("mb"))
    		  {
    			  Path npath = new Path(sname);
    			  modelPaths.put(sname, new Path(npath.getParentPath(), fseq.getPath(0)));
    		  }
    	  }
      }

      /* the generated Maya scene filename */ 
      {
    	  FileSeq fseq = agenda.getPrimaryTarget();
    	  String suffix = fseq.getFilePattern().getSuffix();
    	  if(!fseq.isSingle() || (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
    		  throw new PipelineException
    		  ("The MayaReference Action requires that the primary target file sequence " + 
    		  "must be a single Maya scene file."); 
	
    	  isAscii = suffix.equals("ma");
    	  saveScene = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent() + "/" + fseq.getPath(0));
      }
      
      {
    	  linearUnit = (String) getSingleParamValue(aLinearUnits);
    	  if(linearUnit == null)
    		  throw new PipelineException ("The MayaCollate Action requires a valid linear units value!");
      }
      
      {
    	  angularIndex = ((EnumActionParam) getSingleParam(aAngularUnits)).getIndex() ;
    	  if (angularIndex < 0)
    		  throw new PipelineException ("The MayaCollate Action requires a valid angular units value!");
      }
      
      {
    	  timeIndex = ((EnumActionParam) getSingleParam(aTimeUnits)).getIndex() ;
    	  if (angularIndex < 0)
    		  throw new PipelineException ("The MayaCollate Action requires a valid time units value!");
    	  // adding one so it matches the maya convention and I can just steal their code.
    	  timeIndex++;
      }
    }

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, 0755, "mel");
    try
    {      
      FileWriter out = new FileWriter(script);

      /* a workaround needed in "maya -batch" mode */ 
      out.write("// WORK AROUNDS:\n" + 
    		    "lightlink -q;\n\n");
      
      /* load the animImportExport plugin */ 
      out.write("loadPlugin \"animImportExport.so\";\n\n");

      /* rename the current scene as the output scene */ 
      out.write("// SCENE SETUP\n" + 
    		    "file -rename \"" + saveScene + "\";\n" + 
    		    "file -type \"" + (isAscii ? "mayaAscii" : "mayaBinary") + "\";\n\n");
      
      out.write("// UNIT SETUP\n" +
    		    "changeLinearUnit(\""+ linearUnit  +"\");\n");
      
      if (angularIndex == 0)
    	  out.write("currentUnit -a degree;\n");
      else
    	  out.write("currentUnit -a radian;\n");

      writeTimeUnit(timeIndex, out);

      /* the initial MEL script */ 
      if(initialMel != null)
      {
    	  out.write("// INTITIAL MEL\n" + "source \"" + initialMel + "\";\n\n");
      }
      
      /* the model file reference imports */ 
      for(String sname : modelPaths.keySet())
      {
    	  Path mpath = modelPaths.get(sname);

    	  String nspace = null;
    	  if(getSourceParam(sname, "PrefixName") != null)
    		  nspace = (String) getSourceParamValue(sname, "PrefixName");

    	  if(nspace == null)
    	  {
    		  Path npath = new Path(sname);
    		  nspace = npath.getName();
    	  }
	
    	  String format = "";
    	  {
    		  String fname = mpath.getName();
    		  if(fname.endsWith("ma")) 
    			  format = "  -type \"mayaAscii\"\n";
    		  else if(fname.endsWith("mb")) 
    			  format = "  -type \"mayaBinary\"\n";
    	  }
	
    	  String type = (String) getSourceParamValue(sname, aBuildType);
    	  boolean namespace = (Boolean) getSourceParamValue(sname, aNameSpace);

    	  String prefixString = null;
    	  if(namespace)
    		  prefixString = "  -namespace \"" + nspace + "\"\n";
    	  else
    		  prefixString = "  -rpr \"" + nspace + "\"\n";
	
    	  String command = null;
    	  if(type.equals("Import"))
    	  {
    		  command = "  -import\n";
    		  /*  If this is an import without a namespace, we don't want any prefix at all */
    		  if(!namespace)
    			  prefixString = null;
    	  }
    	  else if(type.equals("Reference"))
    		  command = "  -reference\n";
    	  else
    	  {
    		  throw new PipelineException("Somehow the value of the " + aBuildType
    				  + "source parameter for (" + sname
    				  + ") was set to a value that should never exist.");
    	  }
	
    	  out.write ("// MODEL: " + sname + "\n" + 
    			  	 "print \"Importing Reference Model: " + mpath + "\\n\";\n" + 
    			  	 "file\n" + command + format );
    	  
    	  if (prefixString != null)
    		  out.write(prefixString);
    	  out.write ("  -options \"v=0\"\n" + "  \"$WORKING" + mpath + "\";\n" + "\n\n");
      }
      
      /* the model MEL script */ 
      if(modelMel != null)
      {
    	  out.write("// MODEL MEL\n" + "source \"" + modelMel + "\";\n\n");
      }
      
      { //doing the time setting stuff.
    	  Integer start = (Integer) getSingleParamValue(aStartFrame);
    	  Integer end = (Integer) getSingleParamValue(aEndFrame);

    	  if(start != null)
    	  {
    		  out.write("playbackOptions -e -min " + start + ";\n");
    		  out.write("playbackOptions -e -ast " + start + ";\n");
    	  }
    	  
    	  if(end != null)
    	  {
    		  out.write("playbackOptions -e -max " + end + ";\n");
    		  out.write("playbackOptions -e -aet " + end + ";\n");
    	  }
      }

      /* save the file */ 
      out.write("// SAVE\n" + "print \"Saving Scene: " + saveScene + "\\n\";\n" + "file -save;\n");

      /* the final MEL script */ 
      if(finalMel != null)
      {
    	  out.write("// FINAL MEL\n" + "source \"" + finalMel + "\";\n\n");
      }

      out.write("print \"ALL DONE.\\n\";\n");
      out.close();
    }
    
    catch(IOException ex)
    {
      throw new PipelineException ("Unable to write temporary MEL script file (" + script + ") for Job " + "(" + agenda.getJobID() + ")!\n" + ex.getMessage());
    }

    /* create the process to run the action */ 
    try
    {
    	ArrayList<String> args = new ArrayList<String>();
    	args.add("-batch");
    	args.add("-script");
    	args.add(script.getPath());
      
    	String program = "maya";
 
    	if(PackageInfo.sOsType == OsType.Windows) 
    		program = (program + ".exe");

    	/* added custom Mental Ray shader path to the environment */ 
    	Map<String, String> env = agenda.getEnvironment();
    	Map<String, String> nenv = env;
    	String midefs = env.get("PIPELINE_MI_SHADER_PATH");
    	if(midefs != null)
    	{
    		nenv = new TreeMap<String, String>(env);
    		Path dpath = new Path(new Path(agenda.getWorkingDir()), midefs);
    		nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
    	}

      return new SubProcessHeavy (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), program, args, nenv, agenda.getWorkingDir(), outFile, errFile);
    }
    catch(Exception ex) 
    {
    	throw new PipelineException ("Unable to generate the SubProcess to perform this Action!\n" + ex.getMessage());
    }
  }

  /**
   * Get the abstract path to the MEL file specified by the given parameter.
   * 
   * @param pname
   *   The name of the single valued MEL parameter.
   * 
   * @param title
   *   The title of the parameter in exception messages.
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @return 
   *   The MEL file or <CODE>null</CODE> if none was specified.
   */ 
  private Path getMelPath (String pname, String title, ActionAgenda agenda) throws PipelineException 
  {
	  Path script = null; 
	  String mname = (String) getSingleParamValue(pname); 
	  if(mname != null)
	  {
		  FileSeq fseq = agenda.getPrimarySource(mname);
		  if(fseq == null) 
			  throw new PipelineException ("Somehow the " + title + " node (" + mname + ") was not one of the " + "source nodes!");
      
		  String suffix = fseq.getFilePattern().getSuffix();

		  if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mel")) 
			  throw new PipelineException ("The MayaCollate Action requires that the source node specified by the " + title + " parameter (" + mname + ") must have a single MEL file as its " + "primary file sequence!");
      
		  NodeID mnodeID = new NodeID(agenda.getNodeID(), mname);
		  script = new Path(PackageInfo.sProdPath, mnodeID.getWorkingParent() + "/" + fseq.getPath(0)); 
    }

    return script;	      
  }

  /**
   * Writes out the correct line of Mel to set the time unit, based on the timeIndex value
   * @param timeIndex
   * 	The index into the Enum Parameter for the time unit, 
   * 	incremented by one to match the borrowed mel.
   * @param out
   * 	The FileWriter to print the mel line to.
   * @throws IOException
   */
  private void writeTimeUnit(int timeIndex, FileWriter out) throws IOException
  {
    switch (timeIndex) {
    case 1: 
      out.write("currentUnit -t game -updateAnimation true;\n"); 
      break;
    case 2: 
      out.write("currentUnit -t film -updateAnimation true;\n"); 
      break;
    case 3: 
      out.write("currentUnit -t pal -updateAnimation true;\n"); 
      break;
    case 4: 
      out.write("currentUnit -t ntsc -updateAnimation true;\n"); 
      break;
    case 5: 
      out.write("currentUnit -t show -updateAnimation true;\n"); 
      break;
    case 6: 
      out.write("currentUnit -t palf -updateAnimation true;\n"); 
      break;
    case 7: 
      out.write("currentUnit -t ntscf -updateAnimation true;\n"); 
      break;
    case 8: 
      out.write("currentUnit -t millisec -updateAnimation true;\n"); 
      break;
    case 9: 
      out.write("currentUnit -t sec -updateAnimation true;\n"); 
      break;
    case 10: 
      out.write("currentUnit -t min -updateAnimation true;\n"); 
      break;
    case 11: 
      out.write("currentUnit -t hour -updateAnimation true;\n"); 
      break;
    case 12: 
      out.write("currentUnit -t 2fps -updateAnimation true;\n"); 
      break;
    case 13: 
      out.write("currentUnit -t 3fps -updateAnimation true;\n"); 
      break;
    case 14: 
      out.write("currentUnit -t 4fps -updateAnimation true;\n"); 
      break;
    case 15: 
      out.write("currentUnit -t 5fps -updateAnimation true;\n"); 
      break;
    case 16: 
      out.write("currentUnit -t 6fps -updateAnimation true;\n"); 
      break;
    case 17: 
      out.write("currentUnit -t 8fps -updateAnimation true;\n"); 
      break;
    case 18: 
      out.write("currentUnit -t 10fps -updateAnimation true;\n"); 
      break;
    case 19: 
      out.write("currentUnit -t 12fps -updateAnimation true;\n"); 
      break;
    case 20: 
      out.write("currentUnit -t 16fps -updateAnimation true;\n"); 
      break;
    case 21: 
      out.write("currentUnit -t 20fps -updateAnimation true;\n"); 
      break;
    case 22: 
      out.write("currentUnit -t 40fps -updateAnimation true;\n"); 
      break;
    case 23: 
      out.write("currentUnit -t 75fps -updateAnimation true;\n"); 
      break;
    case 24: 
      out.write("currentUnit -t 80fps -updateAnimation true;\n"); 
      break;
    case 25: 
      out.write("currentUnit -t 100fps -updateAnimation true;\n"); 
      break;
    case 26: 
      out.write("currentUnit -t 120fps -updateAnimation true;\n"); 
      break;
    case 27: 
      out.write("currentUnit -t 125fps -updateAnimation true;\n"); 
      break;
    case 28: 
      out.write("currentUnit -t 150fps -updateAnimation true;\n"); 
      break;
    case 29: 
      out.write("currentUnit -t 200fps -updateAnimation true;\n"); 
      break;
    case 30: 
      out.write("currentUnit -t 240fps -updateAnimation true;\n"); 
      break;
    case 31: 
      out.write("currentUnit -t 250fps -updateAnimation true;\n"); 
      break;
    case 32: 
      out.write("currentUnit -t 300fps -updateAnimation true;\n"); 
      break;
    case 33: 
      out.write("currentUnit -t 375fps -updateAnimation true;\n"); 
      break;
    case 34: 
      out.write("currentUnit -t 400fps -updateAnimation true;\n"); 
      break;
    case 35: 
      out.write("currentUnit -t 500fps -updateAnimation true;\n"); 
      break;
    case 36: 
      out.write("currentUnit -t 600fps -updateAnimation true;\n"); 
      break;
    case 37: 
      out.write("currentUnit -t 750fps -updateAnimation true;\n"); 
      break;
    case 38: 
      out.write("currentUnit -t 1200fps -updateAnimation true;\n"); 
      break;
    case 39: 
      out.write("currentUnit -t 1500fps -updateAnimation true;\n"); 
      break;
    case 40: 
      out.write("currentUnit -t 2000fps -updateAnimation true;\n"); 
      break;
    case 41: 
      out.write("currentUnit -t 3000fps -updateAnimation true;\n"); 
      break;
    case 42: 
      out.write("currentUnit -t 6000fps -updateAnimation true;\n"); 
      break;
    }
    out.write("optionVar\n" +
	"\t-fv playbackMin `playbackOptions -q -min`\n" +
    	"\t-fv playbackMax `playbackOptions -q -max`;\n");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final String aLinearUnits = "LinearUnits";
  private static final String aAngularUnits = "AngularUnits";
  private static final String aTimeUnits = "TimeUnits";
  private static final String aNameSpace = "NameSpace";
  private static final String aBuildType = "BuildType";
  
  private static final String aStartFrame = "StartFrame";
  private static final String aEndFrame = "EndFrame";
  private static final String aComments = "Comments";
  private static final long serialVersionUID = 6605278336941292789L;

}

