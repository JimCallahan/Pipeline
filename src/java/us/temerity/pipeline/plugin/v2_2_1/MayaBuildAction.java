// $Id: MayaBuildAction.java,v 1.4 2007/03/24 03:02:13 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   B U I L D   A C T I O N                                                      */
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
 *     The start frame of the generated Maya scene.
 *   </DIV> <BR>
 *   
 *   End Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *     The end frame of the generated Maya scene.
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
 *      The source node containing the MEL script to evaluate after importing all models
 *      but before saving the generated Maya scene.
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after saving the generated 
 *      Maya scene. 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Build Type<BR>
 *   <DIV style="margin-left: 40px;">
 *     The method Maya should use to merge the data from the source scene into the
 *     generated scene.
 *   </DIV> <BR>
 *   
 *   Name Space<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether Maya should create a namespace for the imported/referenced scene.
 *     This option is highly recomended to avoid name clashes.
 *   </DIV> <BR>
 * 
 *   Prefix Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The namespace prefix to use for the imported/referenced Maya scene inside the 
 *     generated Maya scene.  If unset, the namespace is based on the filename.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MayaBuildAction
  extends MayaAction
{  

/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public
  MayaBuildAction() 
  {
    super("MayaBuild", new VersionID("2.2.1"), "Temerity",
	  "Builds a Maya scene from component scenes.");

    addUnitsParams();
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aStartFrame,
	 "The start frame of the generated Maya scene.", 
	 1);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new IntegerActionParam
	(aEndFrame,
	 "The end frame of the generated Maya scene.", 
	 24);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aInitialMEL,
	 "The source node containing the MEL script to evaluate just after scene creation " + 
         "and before importing any models.",
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aModelMEL,
	 "The source node containing the MEL script to evaluate after importing all models " +
         "but before saving the generated Maya scene.",
	 null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aFinalMEL,
	 "The source node containing the MEL script to evaluate after saving the generated " +
         "Maya scene.", 
	 null);
      addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      addUnitsParamsToLayout(layout); 
      layout.addSeparator();
      layout.addEntry(aStartFrame);
      layout.addEntry(aEndFrame);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aModelMEL);
      layout.addEntry(aFinalMEL);
      
      setSingleLayout(layout);
    }

    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add(aBuildType);
      layout.add(aNameSpace);
      layout.add(aPrefixName);

      setSourceLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Import");
      choices.add("Reference");

      ActionParam param = 
	new EnumActionParam
	(aBuildType,
	 "The method Maya should use to merge the data from the source scene into the " + 
         "generated scene.", 
         "Reference", choices);  	 
      params.put(param.getName(), param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aNameSpace,
	 "Whether Maya should create a namespace for the imported/referenced scene.  " + 
         "This option is highly recomended to avoid name clashes.",
	 true);
      params.put(param.getName(), param);
    }
    
    {
      ActionParam param = 
	new StringActionParam
	(aPrefixName,
	 "The namespace prefix to use for the imported/referenced Maya scene inside the " +
         "generated Maya scene.  If unset, the namespace is based on the filename.",
	 null);
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
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  {
    /* MEL script paths */ 
    Path initialMEL = getMelScriptSourcePath(aInitialMEL, agenda);
    Path modelMEL   = getMelScriptSourcePath(aModelMEL, agenda);
    Path finalMEL   = getMelScriptSourcePath(aFinalMEL, agenda);

    /* model filenames */ 
    TreeMap<String,Path> modelPaths = new TreeMap<String,Path>();
    for(String sname : agenda.getSourceNames()) {
      FileSeq fseq = agenda.getPrimarySource(sname);
      String suffix = fseq.getFilePattern().getSuffix();
      if(fseq.isSingle() && (suffix != null)) {
        if(suffix.equals("ma") || suffix.equals("mb")) {
          Path npath = new Path(sname);
          modelPaths.put(sname, new Path(npath.getParentPath(), fseq.getPath(0)));
        }
      }
    }

    /* the target Maya scene */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(script);

      /* a workaround needed in "maya -batch" mode */ 
      out.write("// WORK AROUNDS:\n" + 
		"lightlink -q;\n\n");
      
      /* rename the current scene as the output scene */ 
      out.write("// SCENE SETUP\n" + 
		"file -rename \"" + targetScene + "\";\n" + 
                "file -type \"" + sceneType + "\";\n\n");

      out.write(genUnitsMEL());

      /* the initial MEL script */ 
      if(initialMEL != null) 
	out.write("// INITIAL SCRIPT\n" + 
                  "print \"Initial Script: " + initialMEL + "\\n\";\n" +
		  "source \"" + initialMEL + "\";\n\n");
      
      /* the model file reference imports */ 
      for(String sname : modelPaths.keySet()) {
	Path mpath = modelPaths.get(sname);

        String buildType = (String) getSourceParamValue(sname, aBuildType);
        if(buildType == null) 
          throw new PipelineException
            ("The value of the " + aBuildType + " source parameter for node " + 
             "(" + sname + ") was not set!"); 

	out.write("// MODEL: " + sname + "\n" + 
                  "print \"" + buildType + " Model: " + mpath + "\\n\";\n" + 
                  "file\n");
        
	String nspace = (String) getSourceParamValue(sname, aPrefixName);
	if(nspace == null) {
	  Path npath = new Path(sname);
	  nspace = npath.getName();
	}

        boolean useNSpace = false;
        {
          Boolean tf = (Boolean) getSourceParamValue(sname, aNameSpace);
          useNSpace = ((tf != null) && tf);
        }
	
        if(buildType.equals("Import")) {
          out.write("  -import\n");
             
          if(useNSpace) 
            out.write("  -namespace \"" + nspace + "\"\n");
        }
        else if(buildType.equals("Reference")) {
          out.write("  -reference\n");
          
          if(useNSpace)
            out.write("  -namespace \"" + nspace + "\"\n");
          else
            out.write("  -renamingPrefix \"" + nspace + "\"\n");
        }
        else {
          throw new PipelineException
            ("Unknown value for the " + aBuildType + " source parameter for node " + 
             "(" + sname + ") encountered!");
        }
          
	{
	  String fname = mpath.getName();
	  if(fname.endsWith("ma")) 
            out.write("  -type \"mayaAscii\"\n");
	  else if(fname.endsWith("mb")) 
	    out.write("  -type \"mayaBinary\"\n");
          else 
            throw new PipelineException
              ("Unkwnown Maya scene format for source file (" + mpath + ")!");
	}
          
	out.write("  -options \"v=0\"\n" + 
                  "  \"$WORKING" + mpath + "\";\n\n"); 
      }
      
      /* the model MEL script */ 
      if(modelMEL != null) 
	out.write("// MODEL SCRIPT\n" + 
                  "print \"Model Script: " + modelMEL + "\\n\";\n" +
		  "source \"" + modelMEL + "\";\n\n");
      
      /* set the time options */ 
      {	
        out.write("// TIME RANGE\n");

        Integer start = (Integer) getSingleParamValue(aStartFrame);
        if(start != null) {
          out.write("playbackOptions -e -min " + start + ";\n");
          out.write("playbackOptions -e -ast " + start + ";\n");
        }
        
        Integer end = (Integer) getSingleParamValue(aEndFrame);
        if(end != null) {
          out.write("playbackOptions -e -max " + end + ";\n");
          out.write("playbackOptions -e -aet " + end + ";\n");
        }

        out.write("\n"); 
      }

      /* save the file */ 
      out.write("// SAVE\n" + 
		"print \"Saving Generated Scene: " + targetScene + "\\n\";\n" + 
		"file -save;\n\n");

      /* the final MEL script */ 
      if(finalMEL != null) 
	out.write("// FINAL SCRIPT\n" + 
                  "print \"Final Script: " + finalMEL + "\\n\";\n" +
		  "source \"" + finalMEL + "\";\n\n");

      out.write("print \"ALL DONE.\\n\";\n");
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */
    return createMayaSubProcess(null, script, true, agenda, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8720905018603466792L;
 
  public static final String aInitialMEL = "InitialMEL";
  public static final String aModelMEL   = "ModelMEL";
  public static final String aFinalMEL   = "FinalMEL";
  public static final String aBuildType  = "BuildType";
  public static final String aNameSpace  = "NameSpace";
  public static final String aPrefixName = "PrefixName";
  public static final String aStartFrame = "StartFrame";
  public static final String aEndFrame   = "EndFrame";
  
}
