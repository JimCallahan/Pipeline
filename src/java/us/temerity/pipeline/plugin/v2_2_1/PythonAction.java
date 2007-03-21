// $Id: PythonAction.java,v 1.2 2007/03/21 22:14:04 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P Y T H O N   A C T I O N                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Executes a Python script. <P> 
 * 
 * This action provides some specialized features to support executing Python scripts within
 * the Pipeline framework.  The supplied Python script need not be executable since this 
 * action must work on all operating systems.  Although Python scripts could be used with 
 * the {@link ScriptAction Script} action, this plugin is usually preferable since it is much
 * more convienient and provides controls over how the Python interpreter is invoked.  It is
 * also possible to write Python scripts using the "os" module which are truely platform
 * independent.<P> 
 * 
 * By default, the "python" program is used as the interpretor.  An alternative program can 
 * be specified by setting PYTHON_BINARY in the Toolset environment to the name of the Python
 * interpertor this Action should use.  On Windows, the name of the Python interpretor should 
 * include the ".exe" extension.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node who's primary file sequence is a single Python script.
 *   </DIV> <BR>
 * 
 *   Interpreter <BR>
 *   <DIV style="margin-left: 40px;">
 *     Which python interpreter to use to execute the script.
 *   </DIV> <BR>
 * 
 *   Optimization <BR>
 *   <DIV style="margin-left: 40px;">
 *     The level of compiler optimizations applied.      
 *   </DIV> <BR>
 * 
 *   Module Info <BR>
 *   <DIV style="margin-left: 40px;">
 *     The verbosity of information printed about the modules loaded.       
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * The supplied Python script can import a dynamically created module written by this Action 
 * into the job's temporary directory called "plprep".  This module defines some data 
 * structures containing a Python representation of the information passed to the 
 * {@link #prep prep} method.  This provides a convenient way of accessing this information 
 * within your Python scripts.  The "plprep" module provides the following data structures: 
 * 
 * <PRE>
 * 
 * ActionAgenda = { 'JobID'  : <I>jobid-number</I>,
 *                  'NodeID' : { 'Author' : '<I>user-name</I>',
 *                               'View'   : '<I>working-area</I>',
 *                               'Name'   : '/<I>full-path<I>/</I>node-name</I>' },
 *                  'PrimaryTarget'    : [ '<I>file1</I>', '<I>file2</I>', ... ],
 *                  'SecondaryTargets' : [ [ '<I>file1</I>', '<I>file2</I>', ... ],
 *                                         [ '<I>file1</I>', '<I>file2</I>', ... ], ... ], 
 *                  'PrimarySources' : 
 *                    { '/<I>full-path<I>/</I>source-node-name1</I>' : [ '<I>file1</I>', '<I>file2</I>', ... ],
 *                      ... }, 
 *                  'SecondarySources' : 
                      { '/<I>full-path<I>/</I>source-node-name1</I>' : [ [ '<I>file1</I>', '<I>file2</I>', ... ],
 *                                                                       [ '<I>file1</I>', '<I>file2</I>', ... ], ... ], 
 *                      ... },
 *                  'Toolset' : '070209' }
 *
 * ScriptSource = '/<I>full-path<I>/</I>script-node-name</I>'
 * 
 * OutFile = '/usr/tmp/pljobmgr/<I>jobid-number</I>/stdout'
 * ErrFile = '/usr/tmp/pljobmgr/<I>jobid-number</I>/stderr'
 * </PRE> 
 * 
 * On Windows, the STDOUT/STDERR file paths are written using the {@link Path} class instead
 * of {@link File} to avoid issues with backslashes.  A typical definition on Windows would
 * look like: 
 * 
 * <PRE>
 * OutFile = 'C:/WINDOWS/Temp/pljobmgr/<I>jobid-number</I>/stdout'
 * ErrFile = 'C:/WINDOWS/Temp/pljobmgr/<I>jobid-number</I>/stderr'
 * </PRE>
 */
public
class PythonAction 
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  PythonAction() 
  {
    super("Python", new VersionID("2.2.1"), "Temerity", 
          "Executes a Python script.");

    {
      ActionParam param = 
        new LinkActionParam
        (aScript, 
         "The node containing the Python script to execute.",
         null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("None");
      choices.add("Basic"); 
      choices.add("Aggressive");
      
      ActionParam param = 
        new EnumActionParam
        (aOptimization, 
         "The level of compiler optimizations applied.", 
         "None", choices);
      addSingleParam(param);
    }
 
    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Silent");
      choices.add("Loaded"); 
      choices.add("Searched");
      
      ActionParam param = 
        new EnumActionParam
        (aModuleInfo, 
         "The verbosity of information printed about the modules loaded.", 
         "Silent", choices);
      addSingleParam(param);
    }

    addExtraOptionsParam();

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aScript); 
      layout.addSeparator(); 
      layout.addEntry(aOptimization);
      layout.addEntry(aModuleInfo);
      layout.addSeparator(); 
      addExtraOptionsParamToLayout(layout); 

      setSingleLayout(layout);      
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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
    /* get the name of the executable script file */ 
    String scriptName = (String) getSingleParamValue(aScript); 
    if(scriptName == null) 
      throw new PipelineException
        ("The Script parameter was not set!");

    /* get the executable script path */ 
    Path scriptPath = 
      getSinglePrimarySourcePath(aScript, agenda, "py", "Python source (.py) file");

    /* create a temporary python prep script */ 
    Path scratch = getTempPath(agenda);
    Path plprep = new Path(scratch, "plprep.py"); 
    try {      
      FileWriter out = new FileWriter(plprep.toFile()); 

      out.write("ActionAgenda = { 'JobID' : " + agenda.getJobID() + ",");

      NodeID nodeID = agenda.getNodeID();
      out.write("'NodeID' : { 'Author' : '" + nodeID.getAuthor() + "', " + 
                               "'View' : '" + nodeID.getView() + "', " + 
                               "'Name' : '" + nodeID.getName() + "' }, " + 
                "'PrimaryTarget' : " + fileList(agenda.getPrimaryTarget()) + ", "); 
      
      {
        out.write("'SecondaryTargets' : [ "); 

        boolean first = true;
        for(FileSeq fseq : agenda.getSecondaryTargets()) {
          if(!first) 
            out.write(", ");
          first = false;

          out.write(fileList(fseq));
        }

        out.write(" ], "); 
      }

      Set<String> sources = agenda.getSourceNames();
      {
        out.write("'PrimarySources' : { "); 

        boolean first = true;
        for(String sname : sources) {  
          if(!first) 
            out.write(", ");
          first = false;

          out.write("'" + sname + "' : " + fileList(agenda.getPrimarySource(sname))); 
        }

        out.write(" }, "); 
      }

      {
        out.write("'SecondarySources' : { "); 

        boolean first = true;
        for(String sname : sources) {  
          if(!first) 
            out.write(", ");
          first = false;

          out.write("'" + sname + "' : [ "); 

          SortedSet<FileSeq> fseqs = agenda.getSecondarySources(sname);
          boolean first2 = true;
          for(FileSeq fseq : fseqs) { 
            if(!first2) 
              out.write(", ");
            first2 = false;
            
            out.write(fileList(fseq)); 
          }

          out.write(" ] "); 
        }

        out.write(" }, "); 
      }

      out.write("'Toolset' : '" + agenda.getToolset() + "' }\n" + 
                "ScriptSource = '" + scriptName + "'\n" + 
                "OutFile = '" + (new Path(outFile)) + "'\n" +
                "ErrFile = '" + (new Path(errFile)) + "'\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write dynamic Python module (" + plprep.toOsString() + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }

    /* create the process to run the action */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      {
        {
          EnumActionParam param = (EnumActionParam) getSingleParam(aOptimization); 
          if(param != null) {
            switch(param.getIndex()) {
            case 1:
              args.add("-O"); 
              break;
              
            case 2:
              args.add("-OO");
            }
          }
        }

        {
          EnumActionParam param = (EnumActionParam) getSingleParam(aModuleInfo);
          if(param != null) {
            switch(param.getIndex()) {
            case 1:
              args.add("-v"); 
              break;
              
            case 2:
              args.add("-vv");
            }
          }
        }

        args.addAll(getExtraOptionsArgs());
      }

      TreeMap<String,String> nenv = new TreeMap<String,String>(agenda.getEnvironment()); 
      nenv.put("PYTHONPATH", scratch.toOsString());

      return createPythonSubProcess(agenda, scriptPath.toFile(), args, nenv, 
                                    outFile, errFile);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Generate Python code for a list of files which make up the given file sequence.
   */
  private String 
  fileList
  (
   FileSeq fseq 
  ) 
  {
    StringBuilder buf = new StringBuilder();
    buf.append("[ ");

    boolean first = true;
    for(File file : fseq.getFiles()) {
      if(!first) 
        buf.append(", ");
      first = false;

      buf.append("'" + file + "'");
    }
    buf.append(" ]");

    return buf.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7647346918966305273L;

  private static final String aOptimization = "Optimization";
  private static final String aScript       = "Script"; 
  private static final String aModuleInfo   = "ModuleInfo"; 
  
}



