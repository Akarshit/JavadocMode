package com.mydomain.mymode;

import processing.mode.java.JavaEditor;
import processing.mode.java.pdex.ASTGenerator;
import processing.mode.java.pdex.ErrorCheckerService;

public class JavadocErrorCheckerService extends ErrorCheckerService {
	
	public JavadocErrorCheckerService(JavaEditor debugEditor) {
		super(debugEditor);
		astGenerator = new JavadocASTGenerator(this);
	}
	
}