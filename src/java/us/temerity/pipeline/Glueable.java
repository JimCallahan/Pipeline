// $Id: Glueable.java,v 1.2 2004/02/14 17:41:57 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E A B L E                                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Support for the bidirectional translation between instantiated objects and a human 
 * readable text representation called Glue. The Glue format is designed for maximum forward
 * and backward compatibility between versions of classes which support the 
 * <CODE>Glueable</CODE> interface. <P>
 *
 * The primitive types (<CODE>boolean</CODE>, <CODE>byte</CODE>, <CODE>short</CODE>, 
 * <CODE>int</CODE>, <CODE>long</CODE>, <CODE>float</CODE>, <CODE>double</CODE> and 
 * <CODE>char</CODE>) are supported implicitly by the 
 * {@link GlueEncoder#encode(String, Object) GlueEncoder.encode} and
 * {@link GlueDecoder#decode(String) GlueDecoder.decode} methods. 
 * The primitive wrapper classes (<CODE>Boolean</CODE>, <CODE>Byte</CODE>, <CODE>Short</CODE>,
 * <CODE>Integer</CODE>, <CODE>Long</CODE>, <CODE>Float</CODE>, <CODE>Double</CODE>, and 
 * <CODE>Character</CODE>) as well as the <CODE>String</CODE> and <CODE>Enum</CODE> classes 
 * are also supported without needing to implement the <CODE>Glueable</CODE> interface. <P>
 * 
 * As a convience, any class which implements either the 
 * {@link Collection java.util.Collection} or {@link Map java.util.Map} interface is also 
 * supported without the need to implement the <CODE>Glueable</CODE> interface by a default 
 * mechanism. If a class implements one of these common interfaces but also implements 
 * the <CODE>Glueable</CODE> interface, the default encoding/decoding will be ignored in 
 * favor of the custom implementation of the <CODE>Glueable</CODE> interface. <P>
 * 
 * In order to implement the <CODE>Glueable</CODE> interface, a class must provide 
 * definitions for the {@link #toGlue(GlueEncoder) toGlue} and 
 * {@link #fromGlue(GlueDecoder) fromGlue} methods.  Typically, the bodies of these methods 
 * will call <CODE>GlueEncoder.encode</CODE> and <CODE>GlueDecoder.decode</CODE> methods 
 * respectively for each field which should be written to and read from Glue format. <P> 
 * 
 * The implementing class must also have an accessible no-arg constructor. When decoded 
 * from Glue, the class will be instantiated with this no-arg constructor and then 
 * initialized by calling the <CODE>fromGlue</CODE> method.  It is the reponsibility of the 
 * implementing class to properly initialize all fields through the combination of these 
 * two methods. Typically, the no-arg constructor will setup reasonable default values for 
 * fields and the <CODE>fromGlue</CODE> method will override these defaults with values 
 * obtained using the <CODE>GlueDecoder.decode</CODE> method for each field. <P>
 * 
 * For example: 
 * <pre>
 * package us.temerity.pipeline;
 *
 * public class Dude 
 *   implements Glueable 
 * {
 *   private String myName;
 *   private int    myAge;
 *
 *   public Dude()
 *   {
 *     myName = "Nobody";
 *     myAge  = 0;
 *   }
 *
 *   public Dude(String name, int age) 
 *   {
 *     myName = name;
 *     myAge  = age;
 *   }
 *
 *   public void toGlue(GlueEncoder ge) 
 *     throws GlueException
 *   {
 *     ge.encode("Name", myName);
 *     ge.encode("Age",  myAge);
 *   }
 *
 *   public void fromGlue(GlueDecoder gd) 
 *     throws GlueException
 *   {
 *     myName = (String)  gd.decode("Name");
 *     myAge  = (Integer) gd.decode("Age");
 *   }
 * }
 * </pre>
 * 
 * @see GlueEncoder
 * @see GlueDecoder
 * @see java.util.Collection
 * @see java.util.Map
 */
public 
interface Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Saves the non-transient fields of this object as Glue.  Typically, the implementation
   * will call {@link GlueEncoder#encode(String, Object) GlueEncoder.encode} for each 
   * non-transient field of the parent object.
   * 
   * @param encoder  [<B>in,out</B>]
   *   The object used to encode this object's fields into Glue format.
   */
  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  )
    throws GlueException;

  /**
   * Restores the non-transient fields of the class from Glue. Typically, the implementation
   * will call {@link GlueDecoder#decode(String) GlueDecoder.decode} for each non-transient 
   * field of the parent object.
   *
   * @param decoder  [<B>in,out</B>]
   *   The object used to decode this object's fields from Glue format.
   */
  public void 
  fromGlue
  (
   GlueDecoder decoder 
  )
    throws GlueException;
  
}



