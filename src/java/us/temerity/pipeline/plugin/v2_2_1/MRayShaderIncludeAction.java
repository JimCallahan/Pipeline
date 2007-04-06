// $Id: MRayShaderIncludeAction.java,v 1.3 2007/04/06 21:16:27 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   S H A D E R   I N C L U D E   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a Mental Ray input file which specifies the locations of a set of shader 
 * definition files and compiled shader dynamic libraries. <P> 
 * 
 * Generates a single Mental Ray input file (.mi) as the primary file sequence of the target 
 * node. The primary file sequence of each source node should contain either a Mental Ray 
 * shader definition file (.mi) or the dynamic library (.so|.dll) of a compiled Mental Ray 
 * shader.  This Action generates "$include" or "link" statements for each of these source 
 * files suitable to initialize the source Mental Ray custom shaders during rendering.<P> 
 * 
 * Normally, the dynamic shader libraries are assumed to have been compiled for a single 
 * operating system which must also be used by Mental Ray renders using the generated primary
 * target MI file.  If the MultiOS parameter is set, then the dynamic libraries are processed
 * in a more sophisticated manner.  Using MultiOS, several alternative dynamic libraries may 
 * be supplied for each operating system supported.  The fully resolved node names of these
 * alternative dynamic libraries must be identical except that the last directory component
 * must match one of the supported operating system types such as: Unix, MacOS or Windows 
 * (see {@link OsType} for details).  When MultiOS is set, only one "link" statement will
 * be written for each set of dynamic libraries which meet this criteria.  The operating 
 * system type component of the path will be replaced by the enviromental variable
 * "PIPELINE_OSTYPE" in these link statements.  This variable is dynamically added to the 
 * Toolset environment used by jobs and therefore will be properly resolved to the correct
 * OS specific dynamic shader library when the job is run.<P> 
 * 
 * If the target node also has a Mental Ray input file (.mi) as a secondary file sequence, 
 * then the contents of all of the input shader definition files will be concatenated to 
 * generate this secondary target file.  Typically, this file is generate to provide Maya
 * with the shader definitions it needs to properly initialize a scene containing these
 * shaders.  If used for this purpose, the name of the secondary file sequence should 
 * match the value of PIPELINE_MI_SHADER_PATH defined in the Toolset environment used to 
 * run Maya.  The Maya related Editor plugins used this variable to properly setup the 
 * Maya environment for Mental Ray custom shaders.  The standard name for this secondary 
 * file is "mi_shader_defs.mi".
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Multi OS <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether generate an MI file capable of being used on several different operating
 *     systems by replacing occurances of directories with names which match one of the
 *     defined operating system types with the PIPELINE_OSTYPE environmental variable.
 *   </DIV> <BR> 
 * </DIV> <P> 
 * 
 * By default, the "python" program is used by this action to copy the target files. 
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.  
 */
public
class MRayShaderIncludeAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MRayShaderIncludeAction() 
  {
    super("MRayShaderInclude", new VersionID("2.2.1"), "Temerity",
	  "Generates a Mental Ray input file which specifies the locations of a set of " + 
          "shader definition files and compiled shader dynamic libraries.");

    {
      ActionParam param = 
	new BooleanActionParam
	(aMultiOS,
	 "Whether generate an MI file capable of being used on several different operating " +
         "systems by replacing occurances of directories with names which match one of the " +
         "defined operating system types with the PIPELINE_OSTYPE environmental variable.", 
	 false);
      addSingleParam(param);
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
    /* target MI file */ 
    Path targetPath = getPrimaryTargetPath(agenda, "mi", "Mental Ray input file"); 
    
    /* optional shader defs MI file */ 
    Path targetDefsPath = null;
    {
      SortedSet<FileSeq> fseqs = agenda.getSecondaryTargets(); 
      if(fseqs.size() > 1) 
        throw new PipelineException
          ("Only one secondary combined shader definition file can be generated by the " + 
           getName() + " Action!"); 
      
      if(fseqs.size() == 1) {
        FileSeq fseq = fseqs.first();
        String suffix = fseq.getFilePattern().getSuffix();
        if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mi")) {
          throw new PipelineException
            ("The " + getName() + " Action requires that the secondary target file " + 
             "sequence must be a single Mental Ray input file!");
        }
            
        targetDefsPath = new Path(agenda.getTargetPath(), fseq.getPath(0));
      }
    }

    /* collect the set of shader definition files and DSO's */ 
    TreeSet<Path> defs = new TreeSet<Path>();
    TreeSet<Path> libs = new TreeSet<Path>();
    for(String sname : agenda.getSourceNames()) {
      FileSeq fseq = agenda.getPrimarySource(sname);
      FilePattern fpat = fseq.getFilePattern();
      String suffix = fpat.getSuffix();
      if(!fseq.isSingle() || (suffix == null) || 
         !(suffix.equals("mi") || suffix.equals("so") || suffix.equals("dll")))
	throw new PipelineException
	  ("The " + getName() + " Action requires that the source node (" + sname + ") " + 
	   "must have either a single Mental Ray shader definition file (.mi) or a " + 
	   "compiled shader dynamic library (.so|.dll) file as its primary file sequence!");
      
      Path spath = new Path(sname);	
      Path ppath = spath.getParentPath();
      if(suffix.equals("mi")) {
        defs.add(new Path(ppath, fseq.getPath(0)));
      }
      else if(getSingleBooleanParamValue(aMultiOS)) {
        FilePattern npat = new FilePattern(fpat.getPrefix(), fpat.getPadding(), "so");
        FileSeq nseq = new FileSeq(npat, fseq.getFrameRange());
        libs.add(new Path(ppath.getParentPath(), "$PIPELINE_OSTYPE/" + nseq.getPath(0)));
      }
      else {
        libs.add(new Path(ppath, fseq.getPath(0)));
      }
    }

    /* create a temporary file which will be copied to the primary target */ 
    Path targetTemp = new Path(createTemp(agenda, "mi"));
    try {
      FileWriter out = new FileWriter(targetTemp.toFile());

      for(Path lpath : libs) 
        out.write("link \"$WORKING" + lpath + "\"\n");
      
      out.write("\n");

      for(Path dpath : defs)
        out.write("$include \"$WORKING" + dpath + "\"\n");

      out.close();
    } 
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary output file (" + targetTemp + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" + 
         ex.getMessage());
    }

    /* create a temporary file which will (optoinally) be copied to the secondary target */ 
    Path targetDefsTemp = null; 
    if(targetDefsPath != null) {
      targetDefsTemp = new Path(createTemp(agenda, "mi"));
      try {
        FileWriter out = new FileWriter(targetDefsTemp.toFile());

        NodeID nodeID = agenda.getNodeID();
        for(Path dpath : defs) {
          out.write("# Copied from: $WORKING" + dpath + "\n");
          
          Path path = new Path(PackageInfo.sWorkPath, 
                               nodeID.getAuthor() + "/" + nodeID.getView() + dpath);

          File file = path.toFile();
          if(!file.exists())
            throw new PipelineException
              ("The source MI file (" + path + ") does not exist!");

          try {
            BufferedReader in = new BufferedReader(new FileReader(file));

            while(true) {
              String line = in.readLine();
              if(line == null) 
                break;
            
              out.write(line + "\n");
            }

            in.close();
          }
          catch(IOException ex) {
            throw new PipelineException
              ("The source MI file (" + path + ") could not be opened for reading:\n  " + 
               ex.getMessage());
          }

          out.write("\n\n");
        }
        
        out.close();
      } 
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary output file (" + targetDefsTemp + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" + 
           ex.getMessage());
      }
    }
    
    /* create a temporary Python script to copy the temporary files to the target location */ 
    File script = createTemp(agenda, "py"); 
    try {
      FileWriter out = new FileWriter(script);

      /* import modules */ 
      out.write("import shutil\n");

      /* copy the primary target */ 
      out.write("shutil.copy('" + targetTemp + "', '" + targetPath + "')\n\n");

      /* copy the (optional) secondary target */ 
      if(targetDefsPath != null) 
        out.write("shutil.copy('" + targetDefsTemp + "', '" + targetDefsPath + "')\n\n");

      out.write("print 'ALL DONE.'\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write the temporary Python script file (" + script + ") for Job " + 
           "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, script, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3276762731707682630L;

  public static final String aMultiOS = "MultiOS"; 
  
}

