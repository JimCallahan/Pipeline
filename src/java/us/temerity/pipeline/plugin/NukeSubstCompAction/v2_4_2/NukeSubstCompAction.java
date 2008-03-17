// $Id: NukeSubstCompAction.java,v 1.2 2008/03/17 23:00:21 jim Exp $

package us.temerity.pipeline.plugin.NukeSubstCompAction.v2_4_2;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.regex.*; 
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   S U B S T   C O M P   A C T I O N                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Dynamically assembles a Nuke script by replacing specific Nuke nodes in a master source 
 * Nuke script with Nuke script fragments provided by other source scripts and then optionally
 * executes the resulting script with Nuke.<P> 
 * 
 * The resulting dynamic Nuke script should contain a Write node for the primary target 
 * file sequence of the node using this action (Process or Substitute Mode).  Alternatively, 
 * this action can append a Write node to the dynamically generated script which will write 
 * the primary target file sequence images (Append & Process Mode).  In either case, this 
 * action only supports a single target file sequence.  See the NukeComp action for an 
 * alternative way of executing Nuke scripts that does support multiple target image 
 * sequences.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Mode <BR> 
 *   The mode of operation for this action: 
 *   <DIV style="margin-left: 40px;">
 *     Append & Process - Append a Nuke Write node to the end of dynamically generated 
 *     Nuke script and them execute it with Nuke to generate the primary target image 
 *     sequence for the node. <P> 
 *     Process - Dynamically generated a Nuke script with all replacements applied and 
 *     execute it with Nuke.  The resulting script should contain a Nuke Write node which 
 *     generates the primary target image sequence for the node. <P> 
 *     Substitute - Dynamically generate a Nuke script with all replacements applied as 
 *     the target file sequence of the node, but do not execute it with Nuke.
 *   </DIV> <BR>
 * 
 *   Master Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the master Nuke script which will be modified by 
 *     replacing some of its Nuke nodes with script fragments from the other source nodes.
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
 *   Replace Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node sequence which sets this parameter should contain a Nuke script
 *     fragment without headers or Root nodes.  This parameter determines the name of the 
 *     Nuke node within the Master Script which will be replaced by this fragment. If this 
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
class NukeSubstCompAction
  extends NukeActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NukeSubstCompAction() 
  {
    super("NukeSubstComp", new VersionID("2.4.2"), "Temerity",
          "Dynamically assembles a Nuke script by replacing specific Nuke nodes in a " + 
          "master source Nuke script with Nuke script fragments provided by other source " + 
          "scripts and then optionally executes the resulting script with Nuke."); 
    
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add(aAppendAndProcess); 
      choices.add(aProcess); 
      choices.add(aSubstitute); 

      ActionParam param = 
        new EnumActionParam
        (aMode,	
	 "The mode of operation for this action.", 
         aAppendAndProcess, choices);
      addSingleParam(param);
    }

    {
      ActionParam param = 
        new LinkActionParam
        (aMasterScript,
         "The source which contains the master Nuke script which will be modified by " + 
         "replacing some of its Nuke nodes with script fragments from the other source " + 
         "nodes.",
         null);
      addSingleParam(param);
    } 

    addExtraOptionsParam(); 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aMode);     
      layout.addEntry(aMasterScript);     
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

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
        (aReplaceName, 
         "Each source node sequence which sets this parameter should contain a Nuke script " +
         "fragment without headers or Root nodes.  This parameter determines the name of " + 
         "the Nuke node within the Master Script which will be replaced by this fragment. " + 
         "If this parameter is not set for a source node file sequence, it will be ignored.",
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
    /* mode of operation */ 
    String mode = getSingleStringParamValue(aMode); 
    if(!mode.equals(aAppendAndProcess) && !mode.equals(aProcess) && !mode.equals(aSubstitute))
      throw new PipelineException
        ("Unknown value (" + mode + ") for the " + aMode + " parameter!"); 
    
    /* the target images file pattern in Nuke notation */ 
    String targetNukePattern = null; 
    if(mode.equals(aAppendAndProcess) || mode.equals(aProcess)) { 
      if(!agenda.getSecondaryTargets().isEmpty()) 
        throw new PipelineException
          ("The " + getName() + " action does not support secondary target file sequences!"); 
      
      targetNukePattern = toNukeFilePattern(agenda.getPrimaryTarget().getFilePattern());
    }

    /* the target Nuke script path */ 
    Path targetNukePath = null;
    if(mode.equals(aSubstitute)) {
      targetNukePath = 
        getPrimaryTargetPath(agenda, NukeActionUtils.getNukeExtensions(), "Nuke Script");
    }

    /* the master Nuke script */
    Path masterScript = null;
    {
      ArrayList<String> suffixes = new ArrayList<String>();
      suffixes.add("nk");
      suffixes.add("nuke");

      masterScript = getPrimarySourcePath(aMasterScript, agenda, suffixes, "Nuke script");
      if(masterScript == null) 
        throw new PipelineException
          ("The " + aMasterScript + " node was not specified!");
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
    TreeMap<String,Path> sourceScriptPaths = new TreeMap<String,Path>();
    {
      for(String sname : agenda.getSourceNames()) {
        if(hasSourceParams(sname)) {
          FileSeq fseq = agenda.getPrimarySource(sname);
          String rname = (String) getSourceParamValue(sname, aReplaceName);
          addScriptPaths(agenda, sname, fseq, rname, sourceScriptPaths);
        }

        for(FileSeq fseq : agenda.getSecondarySources(sname)) {
          FilePattern fpat = fseq.getFilePattern();
          if(hasSecondarySourceParams(sname, fpat)) {
            String rname = (String) getSecondarySourceParamValue(sname, fpat, aReplaceName);
            addScriptPaths(agenda, sname, fseq, rname, sourceScriptPaths);
          }
        }
      }
    }

    /* create a temporary Nuke script by replacing selected nodes from the master Nuke script
       with the associated Nuke script fragments.  For all Nuke scripts processed: 
       + insure that all file paths are prefixed with "WORKING" (see above) 
       + replace file path for Write nodes which match the target file sequences with 
           local paths to insure that the target files are generated properly (Windows) 
       + disable all Write nodes which don't match any target file sequence, except in 
           Substitute Mode
       + optionally, append a Write node for the primary file sequence at the end  */ 
    File temp = createTemp(agenda, "nk"); 
    try {
      FileWriter out = new FileWriter(temp); 
      try {
        /* process the master Nuke script */ 
        BufferedReader master = new BufferedReader(new FileReader(masterScript.toFile())); 

        int lnum = 0;
        try { 
          String typeName = null;
          String nodeName = null; 
          String nodeFile = null; 
          StringBuilder buf = null; 
          while(true) {
            String line = master.readLine();
            if(line == null) 
              break;
            lnum++;

            /* if we are not currently processing a node block... */ 
            if(buf == null) {
              typeName = null;
              nodeName = null;
              nodeFile = null;

              /* is this the start of a new block? 
                   if not, just echo the line to the output script */ 
              Matcher m = sNodeStart.matcher(line);
              if(m.matches()) {   
                typeName = m.group(1);
                if((typeName == null) || (typeName.length() == 0)) 
                  throw new PipelineException
                    ("Syntax Error in (" + masterScript + ") at line [" + lnum + "]:  " + 
                     "Illegal start of node block.\n  " + line);

                buf = new StringBuilder();
                buf.append(line + "\n");
              }
              else {
                out.write(line + "\n"); 
              }
            }
            
            /* we are in the middle of processing a node block... */ 
            else {
              boolean replaceLine = false;

              Matcher nameMatcher = sNodeName.matcher(line);
              Matcher fileMatcher = sNodeFile.matcher(line);
            
              /* is this the "name" property? */ 
              if(nameMatcher.matches()) {
                nodeName = nameMatcher.group(1);
                if((nodeName == null) || (nodeName.length() == 0))
                  throw new PipelineException
                    ("Syntax Error in (" + masterScript + ") at line [" + lnum + "]:  " + 
                     "Illegal node name specification.\n  " + line);                
              }

              /* or is this the "file" property of a Read or Write node? */ 
              else if(fileMatcher.matches() && 
                      (typeName.equals("Read") || typeName.equals("Write"))) {
                nodeFile = fileMatcher.group(1);
                String fixed = replaceFile(masterScript, typeName, targetNukePattern, 
                                           lnum, line, nodeFile); 
                if(fixed != null) {
                  buf.append(fixed); 
                  replaceLine = true;
                }
              }
              
              if(!replaceLine) 
                buf.append(line + "\n"); 
              
              /* have we just finished a node block? */ 
              if(line.equals("}")) {
                Path spath = null; 
                if(nodeName != null) 
                  spath = sourceScriptPaths.get(nodeName); 
                
                if(spath != null) {
                  BufferedReader in = new BufferedReader(new FileReader(spath.toFile())); 

                  int rlnum = 0;
                  try {
                    String rtypeName = null;
                    StringBuilder rbuf = null; 
                    while(true) {
                      String rline = in.readLine();
                      if(rline == null) 
                        break;
                      rlnum++;
                      
                      /* if we are not currently processing a node block... */ 
                      if(rbuf == null) {
                      
                        /* is this the start of a new block? 
                             if not, just echo the line to the output script */ 
                        Matcher m = sNodeStart.matcher(rline);
                        if(m.matches()) {   
                          rtypeName = m.group(1);
                          if((rtypeName == null) || (rtypeName.length() == 0)) 
                            throw new PipelineException
                              ("Syntax Error in (" + spath + ") at line [" + rlnum + "]:  " + 
                               "Illegal start of node block.\n  " + rline);
                          
                          rbuf = new StringBuilder();
                          rbuf.append(rline + "\n");
                        }
                        else {
                          out.write(rline + "\n"); 
                        }
                      }

                      /* we are in the middle of processing a node block... */ 
                      else {
                        boolean rreplaceLine = false;

                        /* is this the "file" property of a Read or Write node? */ 
                        Matcher m = sNodeFile.matcher(rline);
                        if(m.matches() && 
                           (rtypeName.equals("Read") || rtypeName.equals("Write"))) {
                          String nfile = fileMatcher.group(1);
                          String fixed = replaceFile(spath, rtypeName, targetNukePattern, 
                                                     rlnum, rline, nfile); 
                          if(fixed != null) {
                            rbuf.append(fixed); 
                            rreplaceLine = true;
                          }
                        }

                        if(!rreplaceLine) 
                          rbuf.append(rline + "\n"); 

                        /* have we just finished a node block? */ 
                        if(rline.equals("}")) {
                          out.write(rbuf.toString()); 
                          rbuf = null;
                        }
                      }
                    }
                  }
                  finally {
                    in.close();
                  }

                  /* toss the original matched node */ 
                  buf = null;
                }
                else {
                  out.write(buf.toString()); 
                  buf = null;
                }
              }
            }
          }
        }
        finally {
          master.close();
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
    if(mode.equals(aSubstitute)) {
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
   String rname, 
   TreeMap<String,Path> sourceScriptPaths
  )
    throws PipelineException 
  {
    if(rname == null) 
      throw new PipelineException
        ("No " + aReplaceName + " parameter was specified for the source file sequence " + 
         "(" + fseq + ")!");

    if(sourceScriptPaths.containsKey(rname)) 
      throw new PipelineException
        ("Only one source file sequence can set the " + aReplaceName + " parameter to " + 
         "the Nuke node named (" + rname + ") in the master Nuke script!"); 

    String suffix = fseq.getFilePattern().getSuffix();
    if(!fseq.isSingle() || 
       (suffix == null) || !(suffix.equals("nk") || suffix.equals("nuke")))
      throw new PipelineException
        ("The " + getName() + " Action requires that the file sequence (" + fseq + ") of " + 
         "the source node (" + sname + ") selected for evaluation must be a single Nuke " + 
         "script fragment!"); 

    sourceScriptPaths.put(rname, getWorkingNodeFilePath(agenda, sname, fseq));
  }

  /**
   * A helper method to validate and optionally modify "file" paths in Nuke Read/Write nodes.
   */ 
  private String
  replaceFile
  (
   Path script, 
   String typeName, 
   String nukePat, 
   int lnum, 
   String line, 
   String file
  ) 
    throws PipelineException 
  {
    if((file == null) || (file.length() == 0))
      throw new PipelineException
        ("Syntax Error in (" + script + ") at line [" + lnum + "]:  " + 
         "Illegal node file specification.\n  " + line);

    if(!file.startsWith("WORKING/")) 
      throw new PipelineException
        ("Non-portable file path (" + file + ") detected in Nuke script " + 
         "(" + script + ")!  All Read/Write node file paths should be " + 
         "prefixed with \"WORKING\" in order to insure portability between " + 
         "different artists and operating systems.  You must fix any " + 
         "non-portable file paths before execution of this Nuke script will " + 
         "be successful."); 
                
    if(typeName.equals("Write")) {
      Path path = new Path(file.substring(7));
      String npat = path.getName();
      if(nukePat.equals(npat)) {
        return (" file ./" + npat + "\n"); 
      }
    }
    
    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static Pattern sNodeStart = 
    Pattern.compile("([A-Z][A-Za-z0-9]*)[ \\t]+\\{"); 

  private static Pattern sNodeName = 
    Pattern.compile("[ \\t]*name[ \\t]+([A-Za-z0-9_/.\\-]+)"); 

  private static Pattern sNodeFile = 
    Pattern.compile("[ \\t]*file[ \\t]+([A-Za-z0-9_/.\\-]+)"); 

  private static final long serialVersionUID = -9042561844095041171L;

  public static final String aMode              = "Mode";
  public static final String aAppendAndProcess  = "Append & Process";
  public static final String aProcess           = "Process";
  public static final String aSubstitute        = "Substitute";
  public static final String aMasterScript      = "MasterScript"; 
  public static final String aReplaceName       = "ReplaceName"; 

}

