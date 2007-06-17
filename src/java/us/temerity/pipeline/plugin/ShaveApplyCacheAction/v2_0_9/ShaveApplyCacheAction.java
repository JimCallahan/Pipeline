/**
 * 
 */
package us.temerity.pipeline.plugin.ShaveApplyCacheAction.v2_0_9;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A V E   A P P L Y   C A C H E   A C T I O N                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Attempts to apply all the linked shave cache nodes to a maya scene. <P> 
 * 
 * Note that Shave and a Haircut only allows you one directory as the path for a cache.  If
 * you attempt to link in multiple shave caches in different directories, this Action will
 * throw an error if you attempt to load multiple caches from different directories.  The
 * Action identifies caches from their name, which must start with <code>shaveStatFile_</code>
 * and end with the <code>stat</code> suffix.  Any nodes which do not match this pattern
 * will not be considered cache nodes. 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source Maya scene node that has to contain the necessary Shave nodes.
 *   </DIV> <BR>
 *   
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate just after the scene is opened
 *      and before the cache is generated. <BR>
 *   </DIV> <BR>
 *   
 *   Cache MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after the cache is 
 *      generated. <BR>
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after the cache has 
 *      been created. <BR> 
 *   </DIV> 
 * </DIV> <P>  
 */
public class ShaveApplyCacheAction
  extends BaseAction
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public ShaveApplyCacheAction()
  {
    super("ShaveApplyCache", new VersionID("2.0.9"), 
      "Temerity", "Applys linked Shave and a Haircut caches to a maya scene.");
    
    underDevelopment();
    
    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene,
	  "The Maya scene to apply the caches to.", 
	  null);
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aInitialMEL,
	 "The MEL script to evaluate after opening the scene and before applying the caches.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aCacheMEL,
	 "The MEL script to evaluate after loading the caches.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new LinkActionParam
	(aFinalMEL,
	 "The MEL script to evaluate after saving the scene.", 
	 null);
      addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aCacheMEL);
      layout.addEntry(aFinalMEL);

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
    Path sourceNodePath = null;
    NodeID nodeID = agenda.getNodeID();
    Path initialMel = null;
    Path cacheMel = null;
    Path finalMel = null;
    boolean isAscii;
    Path finalScene;

    {
      /* MEL script filenames */
      initialMel = getMelPath(aInitialMEL, "Initial MEL", agenda);
      cacheMel = getMelPath(aCacheMEL, "Initial MEL", agenda);
      finalMel = getMelPath(aFinalMEL, "Final MEL", agenda);
    }

    {
      String name = (String) getSingleParamValue(aMayaScene);
      NodeID sNodeID = new NodeID(nodeID, name);
      FileSeq fseq = agenda.getPrimarySource(name); 
      if (fseq == null)
	throw new PipelineException("The value of the Maya Scene paramter cannot be null");
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null) || 
	!(suffix.equals("ma") || suffix.equals("mb"))) 
	throw new PipelineException
	("The SyflexCacheAction Action requires that the Maya Scene" + 
	"must be a single Maya scene file."); 

      sourceNodePath = new Path(PackageInfo.sProdPath, 
	sNodeID.getWorkingParent() + "/" + fseq.getPath(0));
    }
    
    /* the generated Maya scene filename */ 
    {
      FileSeq fseq = agenda.getPrimaryTarget();
      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || 
	(suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
	throw new PipelineException
	("The ShaveApplyCache Action requires that the primary target file sequence must " + 
	 "be a single Maya scene file."); 

      isAscii = suffix.equals("ma");
      finalScene = new Path(PackageInfo.sProdPath,
	agenda.getNodeID().getWorkingParent() + "/" + fseq.getFile(0));
    }
    
    
    //cacheNodes
    Path cachePath = null;
    TreeSet<String> cacheObjects = new TreeSet<String>();
    {
      for(String sourceName : agenda.getSourceNames()) {
	Path sourcePath = new Path(sourceName);
	String objectPart = sourcePath.getName();
	Path pathPart = sourcePath.getParentPath();
	
	if(objectPart.startsWith("shaveStatFile_")) {
	  if (cachePath == null)
	    cachePath = pathPart;
	  else {
	    if(!cachePath.equals(pathPart)) {
	      throw new PipelineException
	        ("Shave and a Haircut does not allow more than one cache path.\n" +
	         "This node has linked cache nodes with the paths\n (" + cachePath + ")\n" +
	         "and (" + pathPart + ")");
	    }
	  }
	  objectPart = objectPart.replaceFirst("shaveStatFile_", "");
	  String buffer[] = objectPart.split("_");
	  int size = buffer.length;
	  String shaveName = "";
	  for(int i = 0; i < size; i++) {
	    String part = buffer[i];
	    shaveName = part;
	    if(i != (size - 1))
	      shaveName += ":";
	  }
	  cacheObjects.add(shaveName);
	}
      }
    }
    
//    if (cachePath == null)
//      throw new PipelineException("Could not find a cache path in here " + getSourceNames());
    
    File script = createTemp(agenda, 0755, "mel");
    try {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(script)));
      
      out.println("if (!`pluginInfo -q -l EnvPathConvert`)");
      out.write("     loadPlugin \"EnvPathConvert\";\n\n");
      
      /* rename the current scene as the output scene */ 
      out.write("// SCENE SETUP\n" + 
	"file -rename \"" + finalScene + "\";\n" + 
	"file -type \"" + (isAscii ? "mayaAscii" : "mayaBinary") + "\";\n\n");


      /* the initial MEL script */ 
      if(initialMel != null) {
	out.write("// INTITIAL MEL\n" + 
	  "source \"" + initialMel + "\";\n\n");
      }
      
      out.println("string $envNode = `createNode envPathConvert`;");
      String cacheString =  "$WORKING" + cachePath.toString();
      out.println("setAttr -type \"string\" ($envNode + \".envPath\") \""+ cacheString+"\";");
      out.println("connectAttr -f ($envNode + \".absPath\") shaveGlobals.tmpDir;");
      
      for(String shaveObject : cacheObjects) {
	String objAndAttr = shaveObject + ".rd";
	out.println("if (`objExists " + shaveObject + "`)");
	out.println("     catch(`setAttr " + objAndAttr + " 2`);");
      }

      /* the cacheMEL script */ 
      if(initialMel != null) {
	out.write("// CACHE MEL\n" + 
	  "source \"" + cacheMel + "\";\n\n");
      }

      /* save the file */ 
      out.write("// SAVE\n" + 
	"print \"Saving Scene: " + finalScene+ "\\n\";\n" + 
      "file -save;\n");


      /* the final MEL script */ 
      if(finalMel != null) {
	out.write("// FINAL MEL\n" + 
	  "source \"" + finalMel + "\";\n\n");
      }

      out.close();
      
    } catch (IOException ex) {
      throw new PipelineException(
	"Unable to write temporary MEL script file (" + script
	+ ") for Job " + "(" + agenda.getJobID() + ")!\n"
	+ ex.getMessage());
    }
    /* create the process to run the action */
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-batch");
      args.add("-script");
      args.add(script.getPath());
      args.add("-file");
      args.add(sourceNodePath.toOsString());

      String program = "maya";
      if (PackageInfo.sOsType == OsType.Windows)
	program = (program + ".exe");

      /* added custom Mental Ray shader path to the environment */
      Map<String, String> env = agenda.getEnvironment();
      Map<String, String> nenv = env;
      String midefs = env.get("PIPELINE_MI_SHADER_PATH");
      if (midefs != null) {
	nenv = new TreeMap<String, String>(env);
	Path dpath = new Path(new Path(agenda.getWorkingDir()), midefs);
	nenv.put("MI_CUSTOM_SHADER_PATH", dpath.toOsString());
      }

      return new SubProcessHeavy
      (agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	program, args, nenv, agenda.getWorkingDir(), outFile, errFile);
    } 
    catch (Exception ex) {
      throw new PipelineException
      ("Unable to generate the SubProcess to perform this Action!\n"
	+ ex.getMessage());
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
  private Path
  getMelPath
  (
    String pname, 
    String title, 
    ActionAgenda agenda
  ) 
  throws PipelineException 
  {
    Path script = null; 
    String mname = (String) getSingleParamValue(pname); 
    if(mname != null) {
      FileSeq fseq = agenda.getPrimarySource(mname);
      if(fseq == null) 
	throw new PipelineException
	("Somehow the " + title + " node (" + mname + ") was not one of the " + 
	"source nodes!");

      String suffix = fseq.getFilePattern().getSuffix();
      if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mel")) 
	throw new PipelineException
	("The ShaveApplyCache Action requires that the source node specified by the " + 
	  title + " parameter (" + mname + ") must have a single MEL file as its " + 
	"primary file sequence!");

      NodeID mnodeID = new NodeID(agenda.getNodeID(), mname);
      script = new Path(PackageInfo.sProdPath, 
	mnodeID.getWorkingParent() + "/" + fseq.getFile(0)); 
    }

    return script;		       
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final String aMayaScene = "MayaScene";
  private static final String aInitialMEL = "InitialMEL";
  private static final String aCacheMEL = "CacheMEL";
  private static final String aFinalMEL = "FinalMEL";
  private static final long serialVersionUID = 3563636271192494415L;
}
