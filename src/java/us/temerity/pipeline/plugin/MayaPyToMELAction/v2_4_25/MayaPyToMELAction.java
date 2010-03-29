// $Id: BaseBuilder.java,v 1.33 2007/11/01 19:08:53 jesse Exp $

package us.temerity.pipeline.plugin.MayaPyToMELAction.v2_4_25;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   P Y   T O   M E L   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Create a MEL wrapper for Maya python scripts. <p>
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order<BR>
 *   <DIV style="margin-left: 40px;">
 *     The order in which Python scripts should be combined into a MEL script.  If two scripts
 *     have the same order, then no guarantees are made about the order in which they are 
 *     included in the MEL script.
 *   </DIV> <BR>
 * </DIV><p>
 */
public 
class MayaPyToMELAction
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructor.
   */
  public
  MayaPyToMELAction() 
  {
    super("MayaPyToMEL", new VersionID("2.4.25"), "Temerity",
          "Create a MEL wrapper for Maya python scripts.");
    
    
    underDevelopment();
    addSupport(OsType.Windows);
    addSupport(OsType.MacOS);
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

  @Override
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();

    {
      ActionParam param = 
        new IntegerActionParam
          (aOrder, 
           "The order in which Python scripts should be combined into a MEL script.", 
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
    Path targetPath = getPrimaryTargetPath(agenda, "mel", "the mel script"); 
    
    MappedSet<Integer, String> pythonScripts = new MappedSet<Integer, String>();
    
    for (String sourceName : agenda.getSourceNames()) {
      {
        FileSeq seq = agenda.getPrimarySource(sourceName);
        if (hasSourceParams(sourceName)) {
          if (!seq.isSingle())
            throw new PipelineException
            ("Only single frame nodes can be sources of this action.");
          Integer order = getSourceIntegerParamValue(sourceName, aOrder);
          if (order != null) {
            String suffix = seq.getFilePattern().getSuffix();
            if (suffix == null || !suffix.equals("py"))
              throw new PipelineException("All used sources must have the py suffix.");
            String sname = new Path(sourceName).getParentPath() + seq.getPath(0).toString();
            pythonScripts.put(order, sname);
          }
        }
      }
      
      for (FileSeq seq: agenda.getSecondarySources(sourceName)) {
        if (!seq.isSingle())
          throw new PipelineException
            ("Only single frame nodes can be sources of this action.");
        Integer order = 
          getSecondarySourceIntegerParamValue(sourceName, seq.getFilePattern(), aOrder);
        if (order != null) {
          String suffix = seq.getFilePattern().getSuffix();
          if (suffix == null || !suffix.equals("py"))
            throw new PipelineException("All used sources must have the py suffix.");
          String sname = new Path(sourceName).getParentPath() + seq.getPath(0).toString();
          pythonScripts.put(order, sname);
        }
      }
    }
    
    File melScript = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(melScript);
      
      out.write("string $WORKING = getenv(\"WORKING\");\n");
      
      for (Integer order : pythonScripts.keySet()) {
        for (String fileName : pythonScripts.get(order)) {
          out.write
          ("{\n" + 
           "  string $fileName = $WORKING + \"\"; \n" + 
           "  python(\"source = open('\" + $fileName + \"', 'rU')\");\n" + 
           "  python(\"exec source\");\n" + 
           "  python(\"source.close()\");\n" + 
          "}\n\n");
        }
      }
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write temporary MEL script file (" + melScript + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }

    
    return createTempCopySubProcess(agenda, melScript, targetPath, outFile, errFile);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8357225540371289282L;

  private static final String aOrder = "Order";
}
