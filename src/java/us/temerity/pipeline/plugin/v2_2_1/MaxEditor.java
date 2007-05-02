// $Id: MaxEditor.java,v 1.4 2007/05/02 03:18:22 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A X   E D I T O R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The 3D modeling, animation and rendering application from Autodesk. <P> 
 */
public
class MaxEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MaxEditor()
  {
    super("3dsMax", new VersionID("2.2.1"), "Temerity",
	  "3D modeling and animation software from Autodesk.",
	  "3dsmax");

    removeSupport(OsType.Unix);
    addSupport(OsType.Windows);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
   * 
   * The default implementation executes the editor program obtained with {@link #getProgram 
   * getProgram} method under the given environment.  Subclasses should override this method 
   * if more specialized behavior or different command line arguments are needed in order to 
   * launch the editor for the given file sequence. <P> 
   * 
   * @param author
   *   The name of the user owning the files. 
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   * 
   * @see SubProcessLight
   */  
  public SubProcessLight
  prep
  (
   String author, 
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    if(!fseq.isSingle())
      throw new PipelineException
        ("The " + getName() + " Editor can only edit a single file at a time!");

    try {
      /* create a temporary MAXScript to setup the project paths */ 
      File script = createTemp("ms");
      try {
        FileWriter out = new FileWriter(script); 
        
        Path current = new Path(dir);
        if(!current.getName().equals("scenes"))
          throw new PipelineException
            ("The " + getName() + " Editor requires that the 3dsmax scene live in a " + 
             "directory named (scenes) in order for the project paths to be set correctly!");

        Path project = current.getParentPath();
        out.write("pathConfig.setCurrentProjectFolder " + escPath(project) + "\n");

        out.close();
      } 
      catch (IOException ex) {
        throw new PipelineException
          ("Unable to write temporary MAXScript file (" + script + ") required by " + 
           "the " + getName() + " Editor!\n" + 
           ex.getMessage());
      }

      /* command line arguments */ 
      ArrayList<String> args = new ArrayList<String>();
      args.add("-U");
      args.add("MAXScript");
      args.add(script.toString());
      args.add(fseq.getPath(0).toOsString());
      
      return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to launch this Editor!\n" +
	 ex.getMessage());
    }       
  }

  /** 
   * This implementation always throws a PipelineException, to insure that the {@link #prep
   * prep} method is used for this Editor instead of this deprecated method.
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the editor.
   */  
  @SuppressWarnings("deprecation")
  @Deprecated
  public SubProcessLight
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    throw new PipelineException
      ("This method should never be called since the prep() method does not return (null)!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a MAXScript string to represent the given file path.
   */ 
  private String
  escPath
  (
   Path path
  ) 
  {
    return ("\"" + path.toOsString().replaceAll("\\\\", "\\\\\\\\") + "\"");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4942087187245234995L;

}


