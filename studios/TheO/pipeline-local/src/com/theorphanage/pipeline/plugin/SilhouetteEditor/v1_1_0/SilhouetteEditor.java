// $Id: SilhouetteEditor.java,v 1.1 2008/02/07 10:11:18 jesse Exp $

package com.theorphanage.pipeline.plugin.SilhouetteEditor.v1_1_0;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   S I L H O U E T T E   E D I T O R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The Silhouette roto editor.
 */
public
class SilhouetteEditor
  extends BaseEditor
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  SilhouetteEditor()
  {
    super("Silhouette", new VersionID("1.1.0"), "TheO", 
          "The Silhouette Editor.", 
          "Silhouette");

    underDevelopment();
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
  @Override
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
    File script = createTemp("sfx");
    
    try {
      BufferedReader in = new BufferedReader(new FileReader(fseq.getFile(0)));
      BufferedWriter out = new BufferedWriter(new FileWriter(script));
      String line = in.readLine();

      String workingStart = PackageInfo.sWorkPath.toOsString().replaceAll("\\\\", "/");
      String currentWorking = env.get("WORKING").replaceAll("\\\\", "/");
      ArrayList<String> pieces = PackageInfo.sWorkPath.getComponents();

      while (line != null) {
        if (line.contains("<Path>") || line.contains("<Directory>") ) {
          line = line.replaceAll("\\\\", "/");
          if (line.contains(workingStart)) {
            String buffer[] = line.split(workingStart);
            String end = buffer[buffer.length -1];
            String buffer2[] = end.split("/");
            String user = buffer2[1];
            String view = buffer2[2];
            Path oldPath = new Path(new Path(PackageInfo.sWorkPath, user), view);
            String toReplace = oldPath.toOsString().replaceAll("\\\\", "/");
            line = line.replaceAll(toReplace, currentWorking);          
          }
        }
        out.write(line + "\n");
        line = in.readLine();
      }
      in.close();
      out.close();
    } catch (FileNotFoundException e) {
      throw new PipelineException(e);
    } catch (IOException e) {
      throw new PipelineException(e);
    } catch (NullPointerException e) {
      throw new PipelineException(e);
    }catch (ArrayIndexOutOfBoundsException e) {
      throw new PipelineException(e);
    }
    
    {
      ArrayList<String> args = new ArrayList<String>();

      File copyScript = createTemp("bat");
      try {
        BufferedWriter out = new BufferedWriter(new FileWriter(copyScript));
        out.write("copy " + script.getAbsolutePath() + " " + fseq.getFile(0).getAbsolutePath() + "\n" );
        out.close();
      } catch (FileNotFoundException e) {
        throw new PipelineException(e);
      } catch (IOException e) {
        throw new PipelineException(e);
      }
      try {
        SubProcessLight copy = new SubProcessLight("SilhouetteEditor-Copy", copyScript.getAbsolutePath(), args, env, dir);
        copy.start();
        copy.join();
      } catch (InterruptedException e) {
        throw new PipelineException(e);
      }
    }
    
    ArrayList<String> args = new ArrayList<String>();
    args.add(fseq.getFile(0).getAbsolutePath());
  
    return new SubProcessLight(getName(), getProgram(), args, env, dir);
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7297544670239729496L;
}


