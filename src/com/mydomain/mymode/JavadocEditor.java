package com.mydomain.mymode;

import processing.app.Base;
import processing.app.EditorState;
import processing.app.Mode;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.PdeTextAreaDefaults;
import processing.mode.java.JavaEditor;
import processing.mode.java.pdex.ErrorCheckerService;
import processing.mode.java.pdex.JavaTextArea;
import processing.mode.java.JavaMode;


public class JavadocEditor extends JavaEditor {

	ErrorCheckerService ecs;
	protected JavadocEditor(Base base, String path, EditorState state, Mode mode) {
		super(base, path, state, mode);
		initializeErrorChecker();

	    getJavaTextArea().setECSandThemeforTextArea(errorCheckerService, (JavaMode)mode);
	}

	@Override
	protected JEditTextArea createTextArea() {
		return new JavadocTextArea(new PdeTextAreaDefaults(mode), this);
	}

	/**
	 * Initializes and starts Error Checker Service
	 */
	private void initializeErrorChecker() {
		Thread errorCheckerThread = null;

		if (errorCheckerThread == null) {
			errorCheckerService = new JavadocErrorCheckerService(this);
			errorCheckerThread = new Thread(errorCheckerService);
			try {
				errorCheckerThread.start();
			} catch (Exception e) {
				Base.loge("Error Checker Service not initialized", e);
			}
		}
	}
}
