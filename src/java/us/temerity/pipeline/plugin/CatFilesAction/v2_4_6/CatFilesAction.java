// $Id: CatFilesAction.java,v 1.2 2009/09/16 15:56:45 jesse Exp $

package us.temerity.pipeline.plugin.CatFilesAction.v2_4_6;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   C A T   F I L E S   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Concatenates generic files. <P> 
 * 
 * All of the dependencies of the target node which set the Order per-source sequence 
 * parameter will be processed to produce the files associated with the target node. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Unique Order<BR>
 *   <DIV style="margin-left: 40px;">
 *     When set to true, each source must be assigned a unique order.  When set to false, 
 *     multiple nodes can share the same order, but no guarantees can be made about which
 *     will be included first.
 *   </DIV> 
 * </DIV> <P>
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     This parameter determines the order in which the input source files are processed. 
 *     If this parameter is not set for a source node file sequence, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 * 
 * By default, the "python" program is used by this action when running on Windows to perform
 * the file concatenation.  An alternative program can be specified by setting PYTHON_BINARY 
 * in the Toolset environment to the name of the Python interpertor this Action should use.  
 * When naming an alternative Python interpretor under Windows, make sure to include the 
 * ".exe" extension in the program name.
 */
public
class CatFilesAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  CatFilesAction() 
  {
    super("CatFiles", new VersionID("2.4.6"), "Temerity", 
          "Concatenates generic files.");
    
    {
      ActionParam param = 
        new BooleanActionParam
        (aUniqueOrder,
         "When set to true, each source must be assigned a unique order", 
         true);
      addSingleParam(param);
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
  @Override
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  @Override
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();
    
    {
      ActionParam param = 
        new IntegerActionParam
        (aOrder, 
         "The order in which the input source files are processed.", 
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
  @Override
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  { 
    pUniqueOrder = getSingleBooleanParamValue(aUniqueOrder);
    
    /* get the ordered source file paths */
    MappedArrayList<Integer, ArrayList<Path>> sourcePaths = 
      new MappedArrayList<Integer, ArrayList<Path>>(); 
//    MappedArrayList<Integer,Path> sourcePaths = new MappedArrayList<Integer,Path>();
    {
      int numFrames = agenda.getPrimaryTarget().numFrames();
      for(String sname : agenda.getSourceNames()) {
        if(hasSourceParams(sname)) {
          FileSeq fseq = agenda.getPrimarySource(sname);
          Integer order = (Integer) getSourceParamValue(sname, aOrder);
          addSourcePaths(agenda, numFrames, sname, fseq, order, sourcePaths);
        }
        
        for(FileSeq fseq : agenda.getSecondarySources(sname)) {
          FilePattern fpat = fseq.getFilePattern();
          if(hasSecondarySourceParams(sname, fpat)) {
            Integer order = (Integer) getSecondarySourceParamValue(sname, fpat, aOrder);
            addSourcePaths(agenda, numFrames, sname, fseq, order, sourcePaths);
          }
        }
      }

      if(sourcePaths.isEmpty()) 
        throw new PipelineException
          ("No source files where specified using the per-source Order parameter!");
    }
    
    /* create the process to run the action */ 
    try {     
      String program = null;
      ArrayList<String> args = new ArrayList<String>();
  
      /* since Windows scripting is lame, lets use Python... */ 
      if(PackageInfo.sOsType == OsType.Windows) { 
        File script = createTemp(agenda, "py"); 
        try {
          FileWriter out = new FileWriter(script);
        
          out.write
            ("def catfiles(fname, out):\n" + 
             "    f = open(fname, 'rb', 1024)\n" + 
             "    try:\n" + 
             "        while 1:\n" + 
             "            buf = f.read(1024)\n" + 
             "            if(buf == ''):\n" + 
             "                break\n" + 
             "            out.write(buf)\n" + 
             "    finally:\n" + 
             "        f.close()\n\n"); 

          int wk=0;
          for(Path target : agenda.getPrimaryTarget().getPaths()) {
            Path tpath = new Path(agenda.getTargetPath(), target); 

            out.write("out = open('" + tpath + "', 'wb', 1024)\n" + 
                      "try:\n");
            
            for (ArrayList<ArrayList<Path>> each : sourcePaths.values()) 
            for(ArrayList<Path> paths : each ) {
              if(paths.size() == 1)
                out.write("    catfiles('" + paths.get(0) + "', out)\n");
              else 
                out.write("    catfiles('" + paths.get(wk) + ", out)\n");
            }

            out.write("finally:\n" + 
                      "    out.close()\n\n"); 
            
            wk++;
          }

          out.close();
        }
        catch(IOException ex) {
          throw new PipelineException
            ("Unable to write temporary script file (" + script + ") for Job " + 
             "(" + agenda.getJobID() + ")!\n" +
             ex.getMessage());
        }

        program = getPythonProgram(agenda);
        args.add(script.toString());
      }
      
      /* much simpler and faster on a Unix-like system! */ 
      else {
        File script = createTemp(agenda, 0755, "bash");
        try {      
          FileWriter out = new FileWriter(script);
          
          int wk=0;
          for(Path target : agenda.getPrimaryTarget().getPaths()) {
            out.write("cat"); 
            
            for (ArrayList<ArrayList<Path>> each : sourcePaths.values()) 
            for(ArrayList<Path> paths : each ) {
              if(paths.size() == 1)
                out.write(" " + paths.get(0).toOsString());
              else 
                out.write(" " + paths.get(wk).toOsString());
            }
            
            out.write(" > " + target.toOsString() + "\n");
            
            wk++;
          }
          
          out.close();
        }
        catch(IOException ex) {
          throw new PipelineException
            ("Unable to write temporary script file (" + script + ") for Job " + 
             "(" + agenda.getJobID() + ")!\n" +
             ex.getMessage());
        }

        program = "bash";
        args.add(script.getPath());
      }

      return createSubProcess(agenda, program, args, null, outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
        ("Unable to generate the SubProcess to perform this Action!\n" +
         ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A helper method for generating source filenames.
   */ 
  private void 
  addSourcePaths
  (
    ActionAgenda agenda, 
    int numFrames, 
    String sname, 
    FileSeq fseq, 
    Integer order, 
    MappedArrayList<Integer, ArrayList<Path>> sourcePaths
  )
    throws PipelineException 
  {
    if(order == null) 
      return;

    if(sourcePaths.containsKey(order) && pUniqueOrder) 
      throw new PipelineException
        ("The Order per-source parameter for file sequence (" + fseq + ") of source node " + 
         "(" + sname + ") was not unique!");
    
    if((fseq.numFrames() != 1) && (fseq.numFrames() != numFrames))
      throw new PipelineException
        ("The file sequence (" + fseq + ") associated with source node (" + sname + ") " + 
         "must have the contain the same number of files as the target sequence or " + 
         "exactly one file.");
    
    ArrayList<Path> toAdd = new ArrayList<Path>();
    
    for(Path path : getWorkingNodeFilePaths(agenda, sname, fseq))
      toAdd.add(path);
    
    sourcePaths.put(order, toAdd);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private boolean pUniqueOrder;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3145194261106753554L;

  public static final String aOrder = "Order";
  public static final String aUniqueOrder = "UniqueOrder";
}
