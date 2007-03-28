/*
 * Created on Sep 12, 2006 Created by jesse For Use in us.temerity.pipeline.utils
 */
package us.temerity.pipeline.builder;

import java.io.Serializable;
import java.util.*;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   C O N T E X T                                                                */
/*------------------------------------------------------------------------------------------*/

public 
class MayaContext 
  implements Comparable<MayaContext>, Glueable, Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Creates a new MayaContext
   * 
   * @param angularUnit
   * 	The angular unit value
   * @param linearUnit
   * 	The linear unit value
   * @param timeUnit
   * 	The time unit value
   */
  public 
  MayaContext
  (
    String angularUnit, 
    String linearUnit, 
    String timeUnit
  )
  {
    if(!sAngularUnits.contains(angularUnit))
      throw new IllegalArgumentException
        ("Invalid value (" + angularUnit + ") passed in for Angular Units.");
    if(!sLinearUnits.contains(linearUnit))
      throw new IllegalArgumentException
        ("Invalid value (" + linearUnit + ") passed in for Linear Units.");
    if(!sTimeUnits.contains(timeUnit))
      throw new IllegalArgumentException
        ("Invalid value (" + timeUnit + ") passed in for Time Units.");
    pAngularUnit = angularUnit;
    pLinearUnit = linearUnit;
    pTimeUnit = timeUnit;
  }
  
  /**
   * Creates a new MayaContext
   */
  public 
  MayaContext() 
  {
    this("degrees","centimeter", "Film (24 fps)" );
  }

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * @return the AngularUnit
   */
  public String 
  getAngularUnit()
  {
    return pAngularUnit;
  }

  /**
   * @return the LinearUnit
   */
  public String 
  getLinearUnit()
  {
    return pLinearUnit;
  }

  /**
   * @return the TimeUnit
   */
  public String 
  getTimeUnit()
  {
    return pTimeUnit;
  }

  public static List<String> 
  getAngularUnits()
  {
    return Collections.unmodifiableList(sAngularUnits);
  }

  public static List<String> 
  getTimeUnits()
  {
    return Collections.unmodifiableList(sTimeUnits);
  }

  public static List<String> 
  getLinearUnits()
  {
    return Collections.unmodifiableList(sLinearUnits);
  }

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public int 
  compareTo
  (
    MayaContext that
  )
  {
    int value = this.pAngularUnit.compareTo(that.getAngularUnit());
    if(value == 0) {
      value = this.pLinearUnit.compareTo(that.getLinearUnit());
    }
    if(value == 0) {
      value = this.pTimeUnit.compareTo(that.getTimeUnit());
    }
    return value;
  }

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  fromGlue
  (
    GlueDecoder decoder
  ) 
  {
    pTimeUnit = (String) decoder.decode("TimeUnit");
    pAngularUnit = (String) decoder.decode("AngularUnit");
    pLinearUnit = (String) decoder.decode("LinearUnit");
  }

  public void 
  toGlue
  (
    GlueEncoder encoder
  ) 
  throws GlueException
  {
    encoder.encode("TimeUnit", pTimeUnit);
    encoder.encode("AngularUnit", pAngularUnit);
    encoder.encode("LinearUnit", pLinearUnit);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8729924189985169207L;

  private static ArrayList<String> sAngularUnits = new ArrayList<String>();
  private static ArrayList<String> sLinearUnits = new ArrayList<String>();
  private static ArrayList<String> sTimeUnits = new ArrayList<String>();
  {
    sAngularUnits.add("degrees");
    sAngularUnits.add("radians");

    sLinearUnits.add("millimeter");
    sLinearUnits.add("centimeter");
    sLinearUnits.add("meter");
    sLinearUnits.add("inch");
    sLinearUnits.add("foot");
    sLinearUnits.add("yard");

    sTimeUnits.add("15 fps");
    sTimeUnits.add("Film (24 fps)");
    sTimeUnits.add("PAL (25 fps)");
    sTimeUnits.add("NTSC (30 fps)");
    sTimeUnits.add("Show (48 fps)");
    sTimeUnits.add("PAL Field (50 fps)");
    sTimeUnits.add("NTSC Field (60 fps)");
    sTimeUnits.add("milliseconds");
    sTimeUnits.add("seconds");
    sTimeUnits.add("minutes");
    sTimeUnits.add("hours");
    sTimeUnits.add("2fps");
    sTimeUnits.add("3fps");
    sTimeUnits.add("4fps");
    sTimeUnits.add("5fps");
    sTimeUnits.add("6fps");
    sTimeUnits.add("8fps");
    sTimeUnits.add("10fps");
    sTimeUnits.add("12fps");
    sTimeUnits.add("16fps");
    sTimeUnits.add("20fps");
    sTimeUnits.add("40fps");
    sTimeUnits.add("75fps");
    sTimeUnits.add("80fps");
    sTimeUnits.add("100fps");
    sTimeUnits.add("120fps");
    sTimeUnits.add("150fps");
    sTimeUnits.add("200fps");
    sTimeUnits.add("240fps");
    sTimeUnits.add("250fps");
    sTimeUnits.add("300fps");
    sTimeUnits.add("375fps");
    sTimeUnits.add("400fps");
    sTimeUnits.add("500fps");
    sTimeUnits.add("600fps");
    sTimeUnits.add("750fps");
    sTimeUnits.add("1200fps");
    sTimeUnits.add("1500fps");
    sTimeUnits.add("2000fps");
    sTimeUnits.add("3000fps");
    sTimeUnits.add("6000fps");
  }

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private String pTimeUnit;
  private String pAngularUnit;
  private String pLinearUnit;
}
