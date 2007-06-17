// $Id: MayaReplaceRefAction.java,v 1.1 2007/06/17 15:34:44 jim Exp $

package us.temerity.pipeline.plugin.MayaReplaceRefAction.v2_2_1;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E P L A C E   R E F   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Replaces specific references in the source Maya scene to generate a new target Maya 
 * scene.<P>
 * 
 * This action is typically used to switch out low resolution or other stand-in characters, 
 * props or other geometry in a layout or animation scene with high resolution render 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Maya Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The Maya scene which will have its references replaced.
 *   </DIV> <BR>
 * 
 *   Response <BR>
 *   <DIV style="margin-left: 40px;">
 *     What this Action does when it encounters a reference that is not being replaced.
 *     <ul>
 *     <li>Ignore will cause the Action to ignore any reference not being replaced.
 *     <li>Replace will cause the Action to remove any reference not being replaced.
 *     </ul> 
 *   </DIV> <BR>
 * 
 *   Pre Replace MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate before replacing the references. 
 *   </DIV> <BR>
 * 
 *   Post Replace MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *     The MEL script to evaluate after replacing the references. 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   NameSpace<BR>
 *   <DIV style="margin-left: 40px;">
 *     The namespace of the reference to replace.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public class 
MayaReplaceRefAction
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MayaReplaceRefAction() 
  {
    super("MayaReplaceRef", new VersionID("2.2.1"), "Temerity", 
	  "Replaces specific references in the source Maya scene to generate a new " +
	  "target Maya scene.");

    underDevelopment();
    
    {
      ActionParam param = 
	new LinkActionParam
	(aMayaScene, 
	 "The source Maya scene node.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ActionParam param = 
	new LinkActionParam
	(aPreReplaceMEL,
	 "The MEL script to evaluate before replacing the references.", 
	 null); 
      addSingleParam(param);
    }
      
    {
      ActionParam param = 
	new LinkActionParam
	(aPostReplaceMEL,
	 "The MEL script to evaluate after replacing the references.", 
	 null); 
      addSingleParam(param);
    }
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aIgnore);
      choices.add(aRemove);
      
      ActionParam param = 
	new EnumActionParam
	(aResponse,
	 "The action to be taken when a non-replaced reference is found.", 
	 aIgnore,
	 choices); 
      addSingleParam(param);
    }

    {    
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMayaScene);
      layout.addEntry(aResponse);
      layout.addSeparator();
      layout.addEntry(aPreReplaceMEL);
      layout.addEntry(aPostReplaceMEL);

      setSingleLayout(layout);
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
      ActionParam param = 
	new StringActionParam
	(aNameSpace, 
	 "The namespace of the reference to replace.", 
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
    Path preReplaceMEL  = getMelScriptSourcePath(aPreReplaceMEL, agenda);
    Path postReplaceMEL = getMelScriptSourcePath(aPostReplaceMEL, agenda);
    
    /* the source Maya scene */ 
    Path sourceScene = getPrimarySourcePath(aMayaScene, agenda, "ma", "Maya scene file");
    if(sourceScene == null) 
      throw new PipelineException("The MayaScene node was not specified!");

    /* the target Maya scene */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);
    String response = getSingleStringParamValue(aResponse);
    boolean remove = response.equals(aRemove);
    
    /* lookup reference replacement paths */ 
    TreeMap<String,Path> replacePaths = new TreeMap<String,Path>();
    {
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  String rname = (String) getSourceParamValue(sname, aNameSpace);
	  if (rname == null)
	    throw new PipelineException
	      ("Source (" + sname + ") has source parameters, but had a (null) value for " +
	       "its namespace.");
          addReplacePaths(rname, sname, fseq, replacePaths);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
            String rname = (String) getSecondarySourceParamValue(sname, fpat, aNameSpace);
            addReplacePaths(rname, sname, fseq, replacePaths);
	  }
	}
      }
    }

    /* create a temporary pre-replace MEL script */ 
    Path preMEL = null;
    Path preTarget = null;
    if(preReplaceMEL != null) {
      preMEL = new Path(createTemp(agenda, "mel"));
      preTarget = new Path(getTempPath(agenda), "PreTarget.ma");
      try {      
        FileWriter out = new FileWriter(preMEL.toFile());
        
        out.write
          ("// PRE-EXPORT SCRIPT\n" + 
           "print \"Pre-Replace Script: " + preReplaceMEL + "\\n\";\n" +
           "source \"" + preReplaceMEL + "\";\n" +
           "file -rename \"" + preTarget + "\";\n" + 
           "file -type \"mayaAscii\";\n" + 
           "file -save;\n");
           
        out.close();
      } 
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write the temporary MEL script file (" + preReplaceMEL + ") " + 
           "for Job (" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }
    }
       
    /* create a temporary post-replace MEL script */ 
    Path postMEL = null;
    if(postReplaceMEL != null || remove) {
      postMEL = new Path(createTemp(agenda, "mel"));
      try {      
        FileWriter out = new FileWriter(postMEL.toFile());
        
        if (remove) {
          out.write("{\n" +
          	    "string $namespaces[] = {");
          LinkedList<String> names = new LinkedList<String>(replacePaths.keySet());
          for (int i = 0; i < names.size(); i ++) {
            out.write("\"" + names.get(i) + "\"");
            if (i < names.size() - 1)
              out.write(",");
          }
          out.write("};\n");
          out.write("string $files[] = `file -q -r`;\n" +
          	    "string $file;\n" +
          	    "for ($file in $files)\n" +
          	    "{\n" +
          	    "  string $space = `file -q -ns $file`;\n" +
          	    "  if (stringArrayCount($space, $namespaces) == 0)\n" +
          	    "    file -rr $file;\n" +
          	    "}\n" +
          	    "}\n");
        }
        
        if (postReplaceMEL != null) {
          out.write
            ("// POST-EXPORT SCRIPT\n" + 
             "print \"Post-Replace Script: " + postReplaceMEL + "\\n\";\n" +
             "source \"" + postReplaceMEL + "\";\n");
        }
        
        out.write
          ("file -rename \"" + targetScene + "\";\n" + 
           "file -type \"" + sceneType + "\";\n" + 
           "file -save;\n");
      
        out.close();
      } 
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write the temporary MEL script file (" + postReplaceMEL + ") " + 
           "for Job (" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }
    }
    
    /* create a temporary python script used to export the shaders */ 
    File script = createTemp(agenda, "py");
    try {
      FileWriter out = new FileWriter(script); 

      /* include the "launch" method definition */ 
      out.write(getPythonLaunchHeader()); 
      
      /* run pre-replace MEL script */ 
      Path replaceSource = sourceScene;
      if(preMEL != null) {
        out.write(createMayaPythonLauncher(sourceScene, preMEL));
        replaceSource = preTarget;
      }

      /* replace source scene */ 
      Path replaceTarget = targetScene;
      if(!replacePaths.isEmpty())  {
        if(postMEL != null) 
          replaceTarget = new Path(getTempPath(agenda), "PostTarget.ma");

        out.write
          ("\n" + 
           "print('REPLACING REFS...')\n" +
           "source = open('" + replaceSource + "', 'rU')\n" + 
           "target = open('" + replaceTarget + "', 'w')\n" + 
           "try:\n" + 
           "  for line in source:\n" + 
           "    if line.startswith('file -r'):\n");
        
        //String idt = "      ";

        boolean first = true;
        for(String nm : replacePaths.keySet()) {
          Path rpath = replacePaths.get(nm);

          String el = first ? "" : "el";
          first = false;
          
          String prefix = ("file -rdi 1 -ns \"" + nm + "\" -rfn ");  
          out.write
            ("      " + el + "if line.startswith('" + prefix + "'):\n" + 
             "        target.write('" + prefix + "\"" + nm + "RN\" " + 
                                   "\"$WORKING" + rpath + "\";\\n')\n");
        }
        
        for(String nm : replacePaths.keySet()) {
          Path rpath = replacePaths.get(nm);
          
          String prefix = ("file -r -ns \"" + nm + "\" -dr 1 -rfn "); 
          out.write
            ("      elif line.startswith('" + prefix + "'):\n" + 
             "        target.write('" + prefix + "\"" + nm + "RN\" " + 
                                   "\"$WORKING" + rpath + "\";\\n')\n");
        }
        
        out.write
          ("      else:\n" +
           "        target.write(line)\n" + 
           "    else:\n" +
           "      target.write(line)\n" + 
           "finally:\n" + 
           "  target.close()\n" + 
           "  source.close()\n\n"); 
      }
        
      /* run post-replace MEL script */ 
      if(postMEL != null) 
        out.write(createMayaPythonLauncher(replaceTarget, postMEL));
    
      out.write("\n" + 
                "print 'ALL DONE.'\n");

      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
	("Unable to write temporary Python script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the replacement path. 
   */ 
  private void
  addReplacePaths
  (
   String rname, 
   String sname, 
   FileSeq fseq, 
   TreeMap<String,Path> replacePaths
  )
    throws PipelineException 
  {
    if(replacePaths.containsKey(rname)) 
      throw new PipelineException
        ("The reference (" + rname + ") can only be replaced by one source Maya scene!  " + 
         "The file sequence (" + replacePaths.get(rname) + ") has already been specified " + 
         "as the replacement."); 

    String suffix = fseq.getFilePattern().getSuffix();
    if(!fseq.isSingle() || 
       (suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
      throw new PipelineException
        ("The " + getName() + " Action requires that the source file sequence " + 
         "(" + fseq + ") associated with source node (" + sname + ") is a single Maya " + 
         "scene file!"); 
      
    Path spath = new Path(sname); 
    replacePaths.put(rname, new Path(spath.getParentPath(), fseq.getPath(0)));
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2606598485957765617L;

  public static final String aMayaScene      = "MayaScene"; 
  public static final String aPreReplaceMEL  = "PreReplaceMEL"; 
  public static final String aPostReplaceMEL = "PostReplaceMEL"; 
  public static final String aNameSpace      = "NameSpace";
  public static final String aResponse       = "Response";
  public static final String aRemove         = "Remove";
  public static final String aIgnore         = "Ignore";

}
