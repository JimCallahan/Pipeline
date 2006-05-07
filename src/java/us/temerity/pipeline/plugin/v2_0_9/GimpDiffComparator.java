// $Id: GimpDiffComparator.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_9;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G I M P   C O M P A R A T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the difference between two versions of an image using GIMP.
 */
public
class GimpDiffComparator
  extends BaseComparator
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  GimpDiffComparator()
  {
    super("GimpDiff", new VersionID("2.0.9"), "Temerity", 
	  "Image comparison using GIMP.", 
	  "gimp");

    addSupport(OsType.MacOS); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch GIMP with a script which will compare the given two images using
   * layers. <P> 
   * 
   * @param fileA
   *   The absolute path to the first file.
   * 
   * @param fileB
   *   The absolute path to the second file.
   * 
   * @param env  
   *   The environment under which the comparator is run.  
   * 
   * @param dir  
   *   The working directory where the comparator is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the comparator.
   * 
   * @see SubProcessLight
   */  
  public SubProcessLight
  launch
  (
   File fileA, 
   File fileB,
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    /* create temporary GIMP script */ 
    File script = null;
    try {
      Path spath = new Path(PackageInfo.sTempPath, "pipeline");
      File sdir = spath.toFile();
      sdir.mkdir();
      
      script = File.createTempFile("GimpDiff-", ".scm", sdir);
      FileCleaner.add(script);

      String pathA = fileA.toString(); 
      String pathB = fileB.toString(); 
      if(PackageInfo.sOsType == OsType.Windows) {
	pathA = pathA.replace("\\", "\\\\");
	pathB = pathB.replace("\\", "\\\\");
      }

      String text = 
	("(define (gimp-diff-comparator)\n" +
	 "(let* ((imgA (car (gimp-file-load 0\n" + 
	 "                     \"" + pathA + "\"\n" + 
	 "                     \"" + pathA + "\")))\n" +
	 "       (imgB (car (gimp-file-load 0\n" + 
	 "                     \"" + pathB + "\"\n" + 
	 "                     \"" + pathB + "\")))\n" +
	 "\n" +
	 "       (layersA (gimp-image-get-layers imgA))\n" +
	 "       (layerA (aref (cadr layersA) 0))\n" +
	 "\n" +
	 "       (layersB (gimp-image-get-layers imgB))\n" + 
	 "       (layerB (aref (cadr layersB) 0))\n" + 
	 "\n" +
	 "       (sizeX (car (gimp-drawable-width layerB)))\n" + 
	 "       (sizeY (car (gimp-drawable-height layerB)))\n" + 
	 "       (type (car (gimp-drawable-type layerB)))\n" + 
	 "\n" +
	 "       (layerAB (car (gimp-layer-new imgA sizeX sizeY 1 \n" + 
	 "                        \"Checked-In\" 100.0 0))))\n" + 
	 "\n" +
	 "  (gimp-image-add-layer imgA layerAB 1)\n" + 
	 "\n" + 
	 "  (gimp-layer-set-name layerA \"Working\")\n" + 
	 "  (gimp-layer-set-mode layerA 6)\n" + 
	 "\n" + 
	 "  (gimp-edit-copy layerB)\n" + 
	 "  (gimp-floating-sel-anchor (car (gimp-edit-paste layerAB 0)))\n" + 
	 
	 "  (gimp-display-new imgA)))\n\n" + 
	 "(gimp-diff-comparator)\n");

      FileWriter out = new FileWriter(script);
      out.write(text);	
      out.close();
    }
    catch (IOException ex) {
      throw new PipelineException
	("Unable to create the temporary script (" + script + ") for the GimpDiff " + 
	 "comparator plugin!");
    }

    String spath = script.toString(); 
    if(PackageInfo.sOsType == OsType.Windows) 
      spath = spath.toString().replace("\\", "\\\\");

    ArrayList<String> args = new ArrayList<String>();
    args.add("-b");
    args.add("(load \"" + spath + "\")");

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();

    return proc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2660052538888185485L; 

}


