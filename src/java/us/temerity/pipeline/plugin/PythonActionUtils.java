// $Id: PythonActionUtils.java,v 1.3 2007/04/30 20:51:40 jim Exp $

package us.temerity.pipeline.plugin;

import  us.temerity.pipeline.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P Y T H O N   A C T I O N   U T I L S                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of node Action plugins which use Python.<P> 
 * 
 * This class provides convenience methods which make it easier to write Action plugins 
 * which create and execute dynamic Python scripts.
 */
public 
class PythonActionUtils
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct with the given name, version, vendor and description. 
   * 
   * @param name 
   *   The short name of the action.  
   * 
   * @param vid
   *   The action plugin revision number.
   * 
   * @param vendor
   *   The name of the plugin vendor.
   * 
   * @param desc 
   *   A short description of the action.
   */ 
  protected
  PythonActionUtils
  (
   String name,  
   VersionID vid,
   String vendor, 
   String desc
  ) 
  {
    super(name, vid, vendor, desc);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R O G R A M   L O O K U P                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate the name of the Python interpreter to use based on the Toolset environment and 
   * current operating system type.<P> 
   * 
   * If the environmental variable PYTHON_BINARY is defined, its value will be used as the 
   * name of the python executable instead of the "python".  On Windows, this program name
   * should include the ".exe" extension.
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   */ 
  public static final String
  getPythonProgram
  (
   ActionAgenda agenda   
  ) 
    throws PipelineException
  {
    return getPythonProgram(agenda.getEnvironment()); 
  }

  /**
   * Generate the name of the Python interpreter to use based on the Toolset environment and 
   * current operating system type.<P> 
   * 
   * If the environmental variable PYTHON_BINARY is defined, its value will be used as the 
   * name of the python executable instead of the "python".  On Windows, this program name
   * should include the ".exe" extension.
   * 
   * @param env  
   *   The environment used to lookup PYTHON_BINARY.
   */
  public static final String
  getPythonProgram
  (
    Map<String,String> env
  ) 
    throws PipelineException
  {
    String python = env.get("PYTHON_BINARY");
    if((python != null) && (python.length() > 0)) 
      return python; 
    
    if(PackageInfo.sOsType == OsType.Windows) 
      return "python.exe";

    return "python";
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P Y T H O N   C O D E   G E N E R A T I O N                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Generate a string containing spaces for the given Python indentation level.
   */ 
  public static final String
  getPythonIndent
  (
   int level
  ) 
  {
    StringBuilder buf = new StringBuilder(); 
    int wk;
    for(wk=0; wk<level; wk++) 
      buf.append("  ");
    return buf.toString();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a "launch" Python function declaration.<P> 
   * 
   * This launch function uses Python's os.spawn function to start an OS subprocess and 
   * wait for it complete.  If the subprocess returns an non-zero exit code, the launch
   * function calls sys.exit with an appropriate error message.<P> 
   * 
   * This method is provided as a convienence for writing dynamically generated Python 
   * scripts in a subclasses {@link #prep prep} method which run multiple subprocesses. 
   * By using "launch", you get standardized progress messages and error handling for 
   * free. <P> 
   * 
   * The usage is: <P> 
   * <CODE>
   *   launch(<I>program</I>, <I>args</I>)
   * </CODE><P> 
   * 
   * Where <I>program</I> is the executable name and <I>args</I> is the list of command
   * line arguments.  The program will be found using PATH from the Toolset environment
   * used to launch the Python interpretor.
   */ 
  public static final String 
  getPythonLaunchHeader() 
  {
    return 
      ("import subprocess;\n" +
       "import sys;\n\n" +
       "def launch(program, args):\n" +
       "  a = [program] + args\n" +
       "  print('RUNNING: ' + ' '.join(a))\n" +
       "  sys.stdout.flush()\n" + 
       "  result = subprocess.call(a)\n" +
       "  if result != 0:\n" +
       "    sys.exit('  FAILED: Exit Code = ' + str(result));\n\n");
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Verify the existence of each of the given files.<P> 
   * 
   * The generated code requires the "os" package to have been imported previously.
   * 
   * @param path
   *   The abstract file system path to the file to check. 
   * 
   * @param title
   *   What to call the file being validated in progress and error messages.
   */ 
  public static final String
  getPythonFileVerify
  (
   Path path, 
   String title
  ) 
  {
    return getPythonFileVerify(path, title, true, 0); 
  }

  /**
   * Verify the existence of each of the given files.<P> 
   * 
   * The generated code requires the "os" package to have been imported previously.
   * 
   * @param path
   *   The abstract file system path to the file to check. 
   * 
   * @param title
   *   What to call the file being validated in progress and error messages.
   * 
   * @param nonEmpty
   *   Whether to verify that the file is not zero-length as well.
   * 
   * @param indent
   *   The base indentation level of the generated Python code.
   */ 
  public static final String
  getPythonFileVerify
  (
   Path path, 
   String title, 
   boolean nonEmpty, 
   int indent
  ) 
  {
    ArrayList<Path> paths = new ArrayList<Path>();
    paths.add(path); 

    return getPythonFileVerify(path, title, nonEmpty, indent);
  }

  /**
   * Verify the existence of each of the files in the given file sequence.<P> 
   * 
   * The generated code requires the "os" package to have been imported previously.
   * 
   * @param fseq
   *   The sequence of files to check. 
   * 
   * @param title
   *   What to call the files being validated in progress and error messages.
   */ 
  public static final String
  getPythonFileVerify
  (
   FileSeq fseq,
   String title
  ) 
  {
    return getPythonFileVerify(fseq.getPaths(), title, true, 0); 
  }

  /**
   * Verify the existence of each of the files in the given file sequence.<P> 
   * 
   * The generated code requires the "os" package to have been imported previously.
   * 
   * @param fseq
   *   The sequence of files to check. 
   * 
   * @param title
   *   What to call the files being validated in progress and error messages.
   * 
   * @param nonEmpty
   *   Whether to verify that the files are not zero-length as well.
   * 
   * @param indent
   *   The base indentation level of the generated Python code.
   */ 
  public static final String
  getPythonFileVerify
  (
   FileSeq fseq,
   String title, 
   boolean nonEmpty, 
   int indent
  ) 
  {
    return getPythonFileVerify(fseq.getPaths(), title, nonEmpty, indent);
  }

  /**
   * Verify the existence of each of the given files.<P> 
   * 
   * The generated code requires the "os" package to have been imported previously.
   * 
   * @param paths
   *   The abstract file system paths to the file to check. 
   * 
   * @param title
   *   What to call the files being validated in progress and error messages.
   */ 
  public static final String
  getPythonFileVerify
  (
   ArrayList<Path> paths, 
   String title
  ) 
  {
    return getPythonFileVerify(paths, title, true, 0); 
  }

  /**
   * Verify the existence of each of the given files.<P> 
   * 
   * The generated code requires the "os" package to have been imported previously.
   * 
   * @param paths
   *   The abstract file system paths to the file to check. 
   * 
   * @param title
   *   What to call the files being validated in progress and error messages.
   * 
   * @param nonEmpty
   *   Whether to verify that the files are not zero-length as well.
   * 
   * @param indent
   *   The base indentation level of the generated Python code.
   */ 
  public static final String
  getPythonFileVerify
  (
   ArrayList<Path> paths, 
   String title, 
   boolean nonEmpty, 
   int indent
  ) 
  {
    String i = getPythonIndent(indent); 

    StringBuilder buf = new StringBuilder(); 
    for(Path path : paths) {
      buf.append(i + "if os.access('" + path + "', os.F_OK):\n"); 

      if(nonEmpty) 
        buf.append(i + "  statinfo = os.stat('" + path + "')\n" + 
                   i + "  if statinfo.st_size == 0:\n" + 
                   i + "    sys.exit('" + title + " Empty: " + path + "')\n");
      
      buf.append(i + "  print '" + title + " Validated: " + path + "'\n" + 
                 i + "else:\n" +
                 i + "  sys.exit('" + title + " Missing: " + path + "')\n\n");
    }
    
    return buf.toString();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B P R O C E S S   C R E A T I O N                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method which executes a temporary Python script.
   * 
   * This method will handle specifying the Python binary in an Toolset controlled and OS 
   * specific manner (see {@link #getPythonProgram getPythonProgram}).  This method also
   * properly specifies the process owner, title, environment and working directory for the 
   * Python process. 
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param script
   *   The temporary Python script file to execute.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public final SubProcessHeavy
  createPythonSubProcess
  (
   ActionAgenda agenda,
   File script, 
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    return createPythonSubProcess(agenda, script, null, null, outFile, errFile);
  }
  
  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method which executes a temporary Python script.
   * 
   * This method will handle specifying the Python binary in an Toolset controlled and OS 
   * specific manner (see {@link #getPythonProgram getPythonProgram}).  This method also
   * properly specifies the process owner, title, environment and working directory for the 
   * Python process.  Additional command-line arguments for Python can be specified using a
   * non-null <CODE>args</CODE> parameter. The default Toolset generated environment can be 
   * overridden if specified using a non-null <CODE>env</CODE> parameter.<P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param script
   *   The temporary Python script file to execute.
   * 
   * @param args  
   *   Additional Python command line arguments to specify before the script or 
   *   <CODE>null</CODE> for no additional arguments.
   * 
   * @param env  
   *   The environment under which the OS level process is run or 
   *   <CODE>null</CODE> to use the environment defined by the ActionAgenda.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public final SubProcessHeavy
  createPythonSubProcess
  (
   ActionAgenda agenda,
   File script, 
   ArrayList<String> args,
   Map<String,String> env,  
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    try {
      String owner = agenda.getSubProcessOwner();
      String title = getName() + "-" + agenda.getJobID(); 

      ArrayList<String> nargs = new ArrayList<String>();
      if(args != null) 
        nargs.addAll(args); 
      nargs.add(script.getPath());

      Map<String,String> nenv = env;
      if(nenv == null) 
        nenv = agenda.getEnvironment();

      return new SubProcessHeavy(owner, title, getPythonProgram(nenv), nargs, 
                                 nenv, agenda.getTargetPath().toFile(), 
                                 outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }
    
  /** 
   * A convienence method for creating the {@link SubProcessHeavy} instance to be returned
   * by the {@link #prep prep} method which uses Python to execute a series of related 
   * subprocesses. <P> 
   * 
   * This method is useful when the Action will be running subprocesses of the form:<P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   program [args] source-file1 target-file1
   *   program [args] source-file2 target-file2
   *   ...
   *   program [args] source-fileN target-fileN
   * </DIV> <P>
   * 
   * Where the program and optional arguments are identical and the source and target 
   * file sequences have a one-to-one relationship.  If only one source/target file exists
   * in the agenda for the job, the program will be run directly.  Otherwise, a temporary
   * Python script will be created which runs the program for each source/target file pair
   * with the supplied arguments. <P> 
   * 
   * The caller is reponsible for handling any differences in program name between operating
   * systems, but this method will handle specifying the process owner, title, environment.  
   * The default Toolset generated environment can be overridden if specified using a 
   * non-null <CODE>env</CODE> parameter.  This method will also handle specifying the Python 
   * binary in an Toolset controlled and OS specific manner (see {@link #getPythonProgram 
   * getPythonProgram}).  
   * 
   * @param agenda
   *   The agenda to be accomplished by the Action.
   * 
   * @param program  
   *   The name of program to execute for each source/target file pair. 
   * 
   * @param args  
   *   The common command line arguments used by each invocation of the given program.
   * 
   * @param sourcePath
   *   The abstract filesystem path to the directory containing the source file sequence.
   * 
   * @param sourceSeq
   *   The source file sequence.
   * 
   * @param targetSeq
   *   The target file sequence.
   * 
   * @param env  
   *   The environment under which the OS level process is run or 
   *   <CODE>null</CODE> to use the environment defined by the ActionAgenda.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   */
  public final SubProcessHeavy
  createPythonSubProcess
  (
   ActionAgenda agenda,
   String program, 
   ArrayList<String> args,
   Path sourcePath, 
   FileSeq sourceSeq, 
   FileSeq targetSeq, 
   Map<String,String> env,  
   File outFile, 
   File errFile    
  )  
    throws PipelineException
  {
    /* run directly for single frame cases */ 
    if(targetSeq.numFrames() == 1) {
      Path spath = new Path(sourcePath, sourceSeq.getPath(0));
      args.add(spath.toOsString());

      Path tpath = new Path(agenda.getTargetPath(), targetSeq.getPath(0));
      args.add(tpath.toOsString());
      
      return createSubProcess(agenda, program, args, outFile, errFile);
    }

    /* for multiple frames, build a Python script */ 
    else { 
      File script = createTemp(agenda, "py"); 
      try {
	FileWriter out = new FileWriter(script);

        /* include the "launch" method definition */ 
        out.write(getPythonLaunchHeader()); 
        
        /* construct to common command-line arguments */  
        String common = null;
        {
          StringBuilder buf = new StringBuilder();

          buf.append("launch('" + program + "', [");
          
          boolean first = true;
          for(String arg : args) {
            if(!first) 
              buf.append(", ");
            first = false;
            buf.append("'" + arg + "'");
          }

          if(!args.isEmpty()) 
            buf.append(", ");
       
          common = buf.toString();
        }

        /* convert the frames */ 
        {
          ArrayList<Path> sourcePaths = sourceSeq.getPaths();
          ArrayList<Path> targetPaths = targetSeq.getPaths();
          int wk;
          for(wk=0; wk<sourcePaths.size(); wk++) {
            Path spath = new Path(sourcePath, sourcePaths.get(wk));
            Path tpath = new Path(agenda.getTargetPath(), targetPaths.get(wk));
            out.write(common + "'" + spath + "', '" + tpath + "'])\n");
          }
        }
        
        out.write("\n" + 
                  "print 'ALL DONE.'\n");
        
        out.close();
      } 
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write the temporary Python script file (" + script + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
           ex.getMessage());
      }

      /* create the process to run the action */ 
      return createPythonSubProcess(agenda, script, null, env, outFile, errFile); 
    }   
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9075037135637840812L;
  
}



