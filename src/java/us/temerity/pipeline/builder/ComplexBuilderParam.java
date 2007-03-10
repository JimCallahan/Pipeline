package us.temerity.pipeline.builder;

import java.util.TreeSet;

/*------------------------------------------------------------------------------------------*/
/*   C O M P L E X   B U I L D E R   P A R A M                                              */
/*------------------------------------------------------------------------------------------*/

/**
 *
 */
public interface ComplexBuilderParam
  extends BuilderParam
{
  public TreeSet<String>
  listOfKeys
  ();
  
  public void
  valueFromString
  (
    String key,
    String value
  );
}
