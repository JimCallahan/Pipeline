// $Id: ExplorerEditor.java,v 1.2 2007/05/19 13:34:51 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   E X P L O R E R   E D I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The Windows Explorer Graphical File Browser<p>
 * 
 * In order to use this Editor properly, the windows machine this is running on must have 
 * the following flag set: In Control Panel -> Folder Options -> View -> Advanced Settings, 
 * the "Launch folder windows in a separate process" option must be set to <code>true</code>.
 * If this is option is not set, then the Editor won't be able to properly initialize the 
 * environment from the Toolset.
 */
public class ExplorerEditor
  extends BaseEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public
  ExplorerEditor()
  {
    super("Explorer", new VersionID("2.2.1"), "Temerity",
	  "The Windows Explorer Graphical File Browser.", 
	  "explorer");

    removeSupport(OsType.Unix);
    addSupport(OsType.Windows); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public boolean 
  ignoreExitCode()
  {
    return true;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/


  /** 
   * Construct a {@link SubProcessLight} instance which when executed will launch an editor
   * program to view the given file sequence as arguments. <P> 
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
    
    ArrayList<String> args = new ArrayList<String>();
    args.add("/e," + dir.getPath());

    return new SubProcessLight(author, getName(), getProgram(), args, env, dir);
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
   *   The controlling <CODE>SubProcess</CODE> instance. 
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
      ("This launch() method should never be called since the prep() method returns " + 
       "a non-null SubProcessLight instance!");
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1349357120634567218L;

}
