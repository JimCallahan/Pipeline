//$Id: Department.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

/**
 * High level path structural component.
 * <p>
 * To change the naming of the directory, simply override the {@link #toString()} method
 * of this enum to return a different name. 
 */
public 
enum Department
{
	model,
	rig,
	shade {
		public String toString() {
			return "material";
		}
	},
	anim,
	lgt,
	render,
	effects,
	comp,
	texture,
	general;
}
