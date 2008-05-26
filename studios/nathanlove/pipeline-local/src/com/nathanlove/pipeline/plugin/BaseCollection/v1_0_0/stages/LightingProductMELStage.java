// $Id: LightingProductMELStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   L I G H T I N G   P R O D U C T   M E L   S T A G E                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Stage to make a the MEL script that builds the lighting product scene.
 */
public 
class LightingProductMELStage
  extends FileWriterStage
{
  public 
  LightingProductMELStage
  (
    StageInformation info,
    UtilContext context,
    MasterMgrClient client,
    String nodeName
  )
    throws PipelineException
  {
    super("LightingProductMEL",
      "Stage to make a the MEL script that builds the lighting product scene",
      info,
      context,
      client,
      nodeName,
      "mel",
      StageFunction.aScriptFile);
    
    String s = "string $refs[] = `file -q -r`;\n" + 
    		"string $file;\n" + 
    		"for ($file in $refs) {\n" + 
    		"  string $space = `file -q -ns $file`;\n" + 
    		"  if ($space == \"pre\")\n" + 
    		"    file -ir $file;\n" + 
    		"}\n" + 
    		"$refs = `file -q -r`;\n" + 
    		"for ($file in $refs) {\n" + 
    		"  string $space = `file -q -ns $file`;\n" + 
    		"  if (endsWith($space, \"_a\"))\n" + 
    		"    file -ir $file;\n" + 
    		"}";
    setFileContents(s);
  }
  private static final long serialVersionUID = 7736966392161690412L;
}
