// $Id: GimpDiffComparator.java,v 1.1 2004/07/18 21:38:18 jim Exp $

package us.temerity.pipeline.plugin;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G I M P   C O M P A R A T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The 
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
    super("GimpDiff", 
	  "Image comparison using GIMP.", 
	  "gimp");
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
   *   The controlling <CODE>SubProcess</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the comparator.
   * 
   * @see SubProcess
   */  
  public SubProcess
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
      File sdir = new File("/usr/tmp/pipeline");
      sdir.mkdir();
      
      script = File.createTempFile("GimpDiff-", ".scm", sdir);
      FileCleaner.add(script);

      String text = 
	("(define (gimp-diff-comparator)\n" +
	 "(let* ((imgA (car (gimp-file-load 0\n" + 
	 "                     \"" + fileA + "\"\n" + 
	 "                     \"" + fileA + "\")))\n" +
	 "       (imgB (car (gimp-file-load 0\n" + 
	 "                     \"" + fileB + "\"\n" + 
	 "                     \"" + fileB + "\")))\n" +
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

    ArrayList<String> args = new ArrayList<String>();
    args.add("-b");
    args.add("(load \"" + script + "\")");

    SubProcess proc = new SubProcess(getName(), getProgram(), args, env, dir);
    proc.start();

    return proc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = -3650862921762683348L;

}


