package com.mydomain.mymode;

import processing.app.syntax.TextAreaDefaults;
import processing.mode.java.JavaEditor;
import processing.mode.java.JavaInputHandler;
import processing.mode.java.pdex.JavaTextAreaPainter;
import processing.mode.java.pdex.JavaTextArea;


public class JavadocTextArea extends JavaTextArea {
	 public JavadocTextArea(TextAreaDefaults defaults, JavaEditor editor) {
//		    super(defaults, new JavaInputHandler(editor));
		 super(defaults, editor);
	 }
	@Override
	protected JavadocTextAreaPainter createPainter(final TextAreaDefaults defaults) {
	    return new JavadocTextAreaPainter(this, defaults);
	  }
}