// $Id: ViewerLabels.java,v 1.3 2004/08/30 14:30:43 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.io.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.geometry.*;

/*------------------------------------------------------------------------------------------*/
/*   V I E W E R   L A B E L S                                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Static helper methods for creating textured Java3D labels for use with 
 * {@link JNodeViewerPanel JNodeViewerPanel}.
 */ 
public
class ViewerLabels
{
  /**
   * Create a 3D label composed of a set of texture mapped quads for each printable 
   * character in the given text string. <P> 
   * 
   * @param text 
   *   The label text.
   * 
   * @param font
   *   The name of the font textures.
   * 
   * @param mode
   *   The selection state of associated the node/link.
   * 
   * @param space
   *   Extra space to add between characters.
   * 
   * @param pos
   *   The label position. 
   *
   * @param align
   *   The text alignment with the label position.
   * 
   * @return 
   *   The transform group which contains the label geometry.
   * 
   * @throws IOException
   *   If unable to find the per-character font textures.
   */ 
  public static TransformGroup
  createLabelGeometry
  (
   String text,      
   String font,     
   SelectionMode mode,
   double space,     
   Point3d pos, 
   TextAlign align
  ) 
    throws IOException
  {
    if(text == null) 
      throw new IllegalArgumentException("The label text cannot be (null)!");

    /* get the per-character appearances, offsets and total width of the label */ 
    Appearance aprs[] = null;
    double offsets[]  = null;
    double width = 0.0;
    {
      AppearanceMgr mgr = AppearanceMgr.getInstance();
      FontGeometry geom = TextureMgr.getInstance().getFontGeometry(font);

      Vector3d size = new Vector3d();
      char codes[]  = text.toCharArray();
      
      aprs    = new Appearance[codes.length];
      offsets = new double[codes.length];

      int wk;
      for(wk=0; wk<codes.length; wk++) {
	if(geom.isPrintable(codes[wk])) {
	  aprs[wk] = mgr.getFontAppearance(font, codes[wk], mode);
	  assert(aprs[wk] != null);

	  Point3d offset = new Point3d(geom.getOrigin(codes[wk]));
	  offset.add(geom.getExtent(codes[wk]));
	  offsets[wk] = offset.x + space;
	}
	else {
	  offsets[wk] = space * 4.0;
	}

	size.x += offsets[wk];
      }

      width = size.x;
    }
    
    /* build geometry for each printable character */ 
    TransformGroup tg = new TransformGroup();
    {
      Vector3d tpos = new Vector3d(pos);
      switch(align) {
      case Center:
	tpos.x -= width*0.5;
	break; 

      case Right:
	tpos.x -= width;
      }

      int wk;
      for(wk=0; wk<aprs.length; wk++) {
	if(aprs[wk] != null) {
	  Point3d pts[] = new Point3d[4];
	  TexCoord2f uvs[] = new TexCoord2f[4];
	  {
	    pts[0] = new Point3d(-0.1, -0.3, 0.0);
	    pts[0].add(tpos);
	    uvs[0] = new TexCoord2f(0.0f, 0.0f);
	    
	    pts[1] = new Point3d(1.0, -0.3, 0.0);
	    pts[1].add(tpos);
	    uvs[1] = new TexCoord2f(1.0f, 0.0f);
	    
	    pts[2] = new Point3d(1.0, 0.8, 0.0);
	    pts[2].add(tpos);
	    uvs[2] = new TexCoord2f(1.0f, 1.0f);
	    
	    pts[3] = new Point3d(-0.1, 0.8, 0.0);
	    pts[3].add(tpos);
	    uvs[3] = new TexCoord2f(0.0f, 1.0f);
	  }

	  GeometryInfo gi = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
	  gi.setCoordinates(pts);
	  gi.setTextureCoordinateParams(1, 2);
	  gi.setTextureCoordinates(0, uvs);
	  
	  NormalGenerator ng = new NormalGenerator();
	  ng.generateNormals(gi);
	  
	  Stripifier st = new Stripifier();
	  st.stripify(gi);
	  
	  GeometryArray ga = gi.getGeometryArray();
	  
	  Shape3D shape = new Shape3D(ga, aprs[wk]);
	  tg.addChild(shape);
	}

	tpos.x += offsets[wk];
      }
    }

    return tg;
  }

}
