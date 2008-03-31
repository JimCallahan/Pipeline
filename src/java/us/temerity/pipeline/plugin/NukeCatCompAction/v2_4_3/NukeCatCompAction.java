// $Id: NukeCatCompAction.java,v 1.1 2008/03/31 04:39:39 jim Exp $

package us.temerity.pipeline.plugin.NukeCatCompAction.v2_4_3;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   C A T   C O M P   A C T I O N                                                */
/*------------------------------------------------------------------------------------------*/

/** 
 * Dynamically assemples a Nuke script by concatenates several Nuke script fragments and
 * then executing the resulting script with Nuke.<P> 
 * 
 * The resulting dynamic Nuke script should contain a Write node for the primary target 
 * file sequence of the node using this action.  Alternatively, this action can append a
 * Write node to the dynamically generated script which will write the primary target 
 * file sequence images.  In either case, this action only supports a single target file 
 * sequence.  See the NukeComp action for an alternative way of executing Nuke scripts that
 * does support multiple target image sequences.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Mode <BR> 
 *   The mode of operation for this action: 
 *   <DIV style="margin-left: 40px;">
 *     Append & Process - Append a Nuke Write node to the end of dynamically generated 
 *     Nuke script and then execute it with Nuke to generate the primary target image 
 *     sequence for the node. <P> 
 *     Process - Dynamically generated a Nuke script by concatenating the source Nuke script
 *     fragments and execute it with Nuke.  The resulting script should contain a Nuke Write 
 *     node which generates the primary target image sequence for the node. If a Nuke Write 
 *     node is encountered which uses the string "@PL_TARGET@" as its file option, this will 
 *     be replaced with the full path to the actual primary target image sequence for the 
 *     node.<P> 
 *     Concatenate - Dynamically generate a Nuke script by concatenating the source Nuke 
 *     script fragments as the target file sequence of the node, but do not execute it with 
 *     Nuke.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments for Nuke. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain a Nuke script
 *     fragment without headers or Root nodes.  This parameter determines the order in 
 *     which these Nuke script fragments are concatenated together and evaluated.  If this 
 *     parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * By default, this Action executes the "Nuke4.6" binary.  This can be overridden by 
 * specifying an alternate binary with the NUKE_BINARY environmental variable in the 
 * Toolset used to run this Action plugin. On Windows, the Nuke binary name should 
 * include the ".exe" extension.<P> 
 *
 * All Read/Write nodes should have absolute file paths relative to the root working 
 * directory which start with the string "WORKING" in order to support portability of 
 * Nuke scripts between artists and operation sytems.  To enable the "WORKING" prefix to 
 * be expanded to the value of the WORKING environmental variable in these file paths, 
 * a "init.tcl" script as been provided with Pipeline in the "app-extra/nuke" directory 
 * where Pipeline is installed at your site.  You must either copy this script into the 
 * "plugin/user" directory of your Nuke installation or add Pipeline's "app-extra/nuke" 
 * directory to the NUKE_PATH defined in the Toolset using this plugin.
 */
public
class NukeCatCompAction
  extends NukeActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeCatCompAction() 
  {
    super("NukeCatComp", new VersionID("2.4.3"), "Temerity",
	  "Dynamically assemples a Nuke script by concatenates several Nuke script " + 
          "fragments and then executing the resulting script with Nuke.");
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aAppendAndProcess); 
      choices.add(aProcess); 
      choices.add(aConcatenate); 

      ActionParam param = 
        new EnumActionParam
        (aMode,	
	 "The mode of operation for this action.", 
         aAppendAndProcess, choices);
      addSingleParam(param);
    }

    addExtraOptionsParam(); 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMode);     
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

      setSingleLayout(layout);  
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

    underDevelopment(); 
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
	new IntegerActionParam
	(aOrder, 
	 "Each source node sequence which sets this parameter should contain a Nuke script " +
         "fragment without headers or Root nodes.  This parameter determines the order in " + 
         "which these Nuke script fragments are concatenated together and evaluated.  If " + 
         "this parameter is not set for a source node file sequence, it will be ignored.", 
	 100);
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
    /* mode of operation */ 
    String mode = getSingleStringParamValue(aMode); 
    if(!mode.equals(aAppendAndProcess) && 
       !mode.equals(aProcess) && 
       !mode.equals(aConcatenate))
      throw new PipelineException
        ("Unknown value (" + mode + ") for the " + aMode + " parameter!"); 

    /* the target images file pattern in Nuke notation */ 
    String targetNukePattern = null; 
    if(mode.equals(aAppendAndProcess) || mode.equals(aProcess)) { 
      if(!agenda.getSecondaryTargets().isEmpty()) 
        throw new PipelineException
          ("The NukeCatComp action does not support secondary target file sequences!"); 
      
      targetNukePattern = toNukeFilePattern(agenda.getPrimaryTarget().getFilePattern());
    }

    /* the target Nuke script path */ 
    Path targetNukePath = null;
    if(mode.equals(aConcatenate)) {
      targetNukePath = 
        getPrimaryTargetPath(agenda, NukeActionUtils.getNukeExtensions(), "Nuke Script");
    }

    /* optionally generate the Write node Nuke script fragment for the target images */ 
    String writeNode = null; 
    if(mode.equals(aAppendAndProcess)) {
      Path tpath = agenda.getTargetPath();
      FilePattern fpat = agenda.getPrimaryTarget().getFilePattern(); 

      StringBuilder buf = new StringBuilder(); 
      buf.append("Write {\n" + 
                 " file " + tpath + "/" + toNukeFilePattern(fpat) + "\n"); 

      String suffix = fpat.getSuffix();
      if((suffix != null) && (suffix.equals("tif") || suffix.equals("tiff"))) 
        buf.append(" file_type tiff\n" + 
                   " compression LZW\n");

      buf.append("}\n");
      
      writeNode = buf.toString();
    }

    /* the Nuke script fragments to evaluate */ 
    MappedLinkedList<Integer,Path> sourceScriptPaths = new MappedLinkedList<Integer,Path>();
    {
      for(String sname : agenda.getSourceNames()) {
	if(hasSourceParams(sname)) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  Integer order = (Integer) getSourceParamValue(sname, aOrder);
	  addScriptPaths(agenda, sname, fseq, order, sourceScriptPaths);
	}

	for(FileSeq fseq : agenda.getSecondarySources(sname)) {
	  FilePattern fpat = fseq.getFilePattern();
	  if(hasSecondarySourceParams(sname, fpat)) {
	    Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
            addScriptPaths(agenda, sname, fseq, order, sourceScriptPaths);
	  }
	}
      }

      if(sourceScriptPaths.isEmpty()) 
	throw new PipelineException
	  ("No Nuke scripts where specified using the per-source Order parameter!"); 
    }

    /* create a temporary Nuke script composed of all of the script fragments in the right
       order which have been also modified to: 
       + insure that all file paths are prefixed with "WORKING" (see above) 
       + replace file path for Write nodes which match the target file sequences with 
           local paths to insure that the target files are generated properly (Windows) 
       + disable all Write nodes which don't match any target file sequence, except in 
           Substitute Mode
       + replace instanced of "@PL_TARGET@" with the primary image file sequence if in 
           Process Mode
       + optionally, append a Write node for the primary file sequence at the end  */ 
    File temp = createTemp(agenda, "nk");
    try {
      FileWriter out = new FileWriter(temp); 
      try {
        /* process the Nuke script fragments in Order */ 
        for(LinkedList<Path> paths : sourceScriptPaths.values()) {
          for(Path spath : paths) {
            BufferedReader in = new BufferedReader(new FileReader(spath.toFile())); 

            int lnum = 1;
            try { 
              boolean isRead     = false;
              boolean isWrite    = false;
              boolean isDisabled = false;
              while(true) {
                String line = in.readLine();
                if(line == null) 
                  break;
              
                boolean wasWritten = false;
                if(line.startsWith("Read {"))
                  isRead = true;
                else if(line.startsWith("Write {"))
                  isWrite = true;
                else if(line.startsWith("}")) {
                  if(isDisabled) 
                    out.write(" disable true\n");
                  isRead     = false;
                  isWrite    = false;
                  isDisabled = false;            
                }
                else if((isRead || isWrite) && line.startsWith(" file ")) {
                  String file = line.substring(6);
                  if(!file.startsWith("WORKING/")) 
                    throw new PipelineException
                      ("Non-portable file path (" + file + ") detected in Nuke script " + 
                       "(" + spath + ")!  All Read/Write node file paths should be " + 
                       "prefixed with \"WORKING\" in order to insure portability between " + 
                       "different artists and operating systems.  You must fix any " + 
                       "non-portable file paths before execution of this Nuke script will " + 
                       "be successful."); 
            
                  if(isWrite) {
                    Path path = new Path(file.substring(7));
                    String npat = path.getName();
                    if((targetNukePattern != null) && targetNukePattern.equals(npat)) {
                      out.write(" file ./" + npat + "\n"); 
                      wasWritten = true;
                    }
                    else if(!mode.equals(aConcatenate)) {
                      isDisabled = true;
                    }
                  }
                }

                if(!wasWritten) 
                  out.write(line + "\n");
              }         
            }
            finally {
              in.close();
            }
          }
        }

        /* add the optional Write node to the end */ 
        if(writeNode != null) 
          out.write(writeNode); 
      }
      finally {
        out.close();
      }
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary Nuke script file (" + temp + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
      
    /* create the process to run the action */ 
    if(mode.equals(aConcatenate)) {
      return createTempCopySubProcess(agenda, temp, targetNukePath, outFile, errFile);
    }
    else {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-nx"); 
      args.addAll(getExtraOptionsArgs());
      args.add(temp.toString()); 
      args.add(toNukeFrameRange(agenda.getPrimaryTarget().getFrameRange()));

      return createSubProcess(agenda, getNukeProgram(agenda), args, agenda.getEnvironment(), 
                              agenda.getTargetPath().toFile(), outFile, errFile);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating Nuke script filenames.
   */ 
  private void 
  addScriptPaths
  (
   ActionAgenda agenda, 
   String sname, 
   FileSeq fseq, 
   Integer order, 
   MappedLinkedList<Integer,Path> sourceScriptPaths
  )
    throws PipelineException 
  {
    if(order == null) 
      return;

    String suffix = fseq.getFilePattern().getSuffix();
    if(!fseq.isSingle() || 
       (suffix == null) || !(suffix.equals("nk") || suffix.equals("nuke")))
      throw new PipelineException
        ("The " + getName() + " Action requires that the file sequence (" + fseq + ") of " + 
         "the source node (" + sname + ") selected for evaluation must be a single Nuke " + 
         "script fragment!"); 
      
    sourceScriptPaths.put(order, getWorkingNodeFilePath(agenda, sname, fseq));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6218769338307794469L;

  public static final String aMode              = "Mode";
  public static final String aAppendAndProcess  = "Append & Process";
  public static final String aProcess           = "Process";
  public static final String aConcatenate       = "Concatenate";
  public static final String aOrder             = "Order"; 

}

