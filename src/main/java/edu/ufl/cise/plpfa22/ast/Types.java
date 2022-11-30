/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22.ast;

public class Types {

	public static enum Type {
		NUMBER, BOOLEAN, STRING, PROCEDURE;

		public String getDataJVMType() {
			switch (this) {
				case BOOLEAN:
					return "Z";
				case NUMBER:
					return "I";
				case STRING:
					return "Ljava/lang/String;";
				default:
					// the type of a procedure is depended on it's name, so we can't get it here.
					assert false;
					return "";

			}
		}
	};

}
