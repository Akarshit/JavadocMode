package com.mydomain.mymode;

import java.awt.event.MouseEvent;
import java.io.File;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import processing.app.Base;
import processing.app.SketchCode;
import processing.app.syntax.TextAreaDefaults;
import processing.mode.java.JavaMode;
import processing.mode.java.pdex.ASTGenerator;
import processing.mode.java.pdex.ErrorCheckerService;
import processing.mode.java.pdex.JavaTextArea;
import processing.mode.java.pdex.JavaTextAreaPainter;
import processing.mode.java.pdex.JavadocHelper;
import processing.mode.java.JavaEditor;

public class JavadocTextAreaPainter extends JavaTextAreaPainter {
	public TreeMap<String, String> jdocMap;

	public JavadocTextAreaPainter(JavaTextArea textArea,
			TextAreaDefaults defaults) {
		super(textArea, defaults);
		loadJdocMap();
	}

	public void loadJdocMap() {
		jdocMap = new TreeMap<String, String>();
		// presently loading only p5 reference for PApplet
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() { // put your address for javadoc here
				JavadocLoader
						.loadJavaDoc(
								jdocMap,
								new File(
										"/home/akarshit/Documents/processing/build/javadoc/everything/processing"));
			}
		});
		t.start();
	}

	String methodName;

	String className;

	int parameters;

	@Override
	public String getToolTipText(MouseEvent event) {
		// if (!getEditor().hasJavaTabs()) {
		int off = textArea.xyToOffset(event.getX(), event.getY());
		if (off < 0) {
			setToolTipText(null);
			return super.getToolTipText(event);
		}
		int line = textArea.getLineOfOffset(off);
		if (line < 0) {
			setToolTipText(null);
			return super.getToolTipText(event);
		}
		String s = textArea.getLineText(line);
		if (s == "") {
			return event.toString();

		} else if (s.length() == 0) {
			setToolTipText(null);
			return super.getToolTipText(event);

		} else {
			int x = textArea.xToOffset(line, event.getX()), x2 = x + 1, x1 = x - 1;
			int xLS = off - textArea.getLineStartNonWhiteSpaceOffset(line);
			if (x < 0 || x >= s.length()) {
				setToolTipText(null);
				return super.getToolTipText(event);
			}
			String word = s.charAt(x) + "";
			if (s.charAt(x) == ' ') {
				setToolTipText(null);
				return super.getToolTipText(event);
			}
			if (!(Character.isLetterOrDigit(s.charAt(x)) || s.charAt(x) == '_' || s
					.charAt(x) == '$')) {
				setToolTipText(null);
				return super.getToolTipText(event);
			}
			int i = 0;
			while (true) {
				i++;
				if (x1 >= 0 && x1 < s.length()) {
					if (Character.isLetter(s.charAt(x1)) || s.charAt(x1) == '_') {
						word = s.charAt(x1--) + word;
						xLS--;
					} else
						x1 = -1;
				} else
					x1 = -1;

				if (x2 >= 0 && x2 < s.length()) {
					if (Character.isLetterOrDigit(s.charAt(x2))
							|| s.charAt(x2) == '_' || s.charAt(x2) == '$')
						word = word + s.charAt(x2++);
					else
						x2 = -1;
				} else
					x2 = -1;

				if (x1 < 0 && x2 < 0)
					break;
				if (i > 200) {
					// time out!
					// System.err.println("Whoopsy! :P");
					break;
				}
			}
			if (Character.isDigit(word.charAt(0))) {
				setToolTipText(null);
				return super.getToolTipText(event);
			}
			String tooltipText = "";
			// String tooltipText = errorCheckerService.getASTGenerator()
			// .getLabelForASTNode(line, word, xLS);
			System.out.println("line = " + line + " word = " + word + " xLS = "
					+ xLS);

			// // Find closest ASTNode to the linenumber
			// // log("getASTNodeAt: Node line number " + pdeLineNumber);
			// ASTNode temp =
			// errorCheckerService.getASTGenerator().findLineOfNode(compilationUnit,
			// pdeLineNumber, offset,
			// name);

			ASTNode temp = errorCheckerService.getASTGenerator()
					.getASTNodeAt(line, word, xLS, false).getNode();
			className = "PApplet";
			methodName = null;
			findRecursively(temp, word);// This function gets the name of
										// function and no. of arguments
			findReturnTypeRecur(temp, word);// This function gets the
											// expected return type
			System.out.println(className + "." + methodName + "(" + parameters
					+ ")");
			// temp = "asd";
			// log(errorCheckerService.mainClassOffset + " MCO "
			// + "|" + line + "| offset " + xLS + word + " <= offf: "+off+
			// "\n");
			if (tooltipText != null) {
				return jdocMap.get(className + methodName + "(" + parameters
						+ ")");
			}
		}
		// }
		// Used when there are Java tabs, but also the fall-through case from
		// above
		setToolTipText(null);
		return super.getToolTipText(event);
	}

	void findRecursively(ASTNode temp, String word) {
		if (temp == null) {
			System.out.println("temp is mull");
		}
		System.out.println(temp.toString());
		switch (temp.getNodeType()) {
		case ASTNode.ASSIGNMENT:
			if (((Assignment) temp).getLeftHandSide().toString().contains(word)) {
				temp = ((Assignment) temp).getLeftHandSide();
			} else {
				temp = ((Assignment) temp).getRightHandSide();
			}
			findRecursively(temp, word);
			break;
		case ASTNode.EXPRESSION_STATEMENT:
			temp = ((ExpressionStatement) temp).getExpression();
			findRecursively(temp, word);
			break;
		case ASTNode.METHOD_INVOCATION:
			// negativeOffset += ((MethodInvocation)
			// temp).getExpression().toString().length()+1;
			if (((MethodInvocation) temp).getName().toString().contains(word)) {
				methodName = ((MethodInvocation) temp).getName()
						.getFullyQualifiedName();
				parameters = ((MethodInvocation) temp).arguments().size();
			} else {
				temp = ((MethodInvocation) temp).getExpression();
				findRecursively(temp, word);
			}
			break;
		}
	}

	void findReturnTypeRecur(ASTNode temp, String word) {
		if (temp == null) {
			return;
		}
		switch (temp.getNodeType()) {
		case ASTNode.ASSIGNMENT:
			if (((Assignment) temp).getLeftHandSide().toString().contains(word)) {
				temp = ((Assignment) temp).getLeftHandSide();
			} else {
				temp = ((Assignment) temp).getRightHandSide();
			}
			findReturnTypeRecur(temp, word);
			break;
		case ASTNode.EXPRESSION_STATEMENT:
			temp = ((ExpressionStatement) temp).getExpression();
			findReturnTypeRecur(temp, word);
			break;
		case ASTNode.METHOD_INVOCATION:
			temp = ((MethodInvocation) temp).getExpression();
			findReturnTypeRecur(temp, word);
			break;
		case ASTNode.SIMPLE_NAME:
			temp = JavadocASTGenerator.findDeclaration((Name) temp);// find the
																// declaration
																// of this.
			findReturnTypeRecur(temp, word);
			break;
		case ASTNode.FIELD_DECLARATION:
			className = ((SimpleName) (((SimpleType) (((FieldDeclaration) temp)
					.getType())).getName())).getFullyQualifiedName();
			break;
		case ASTNode.VARIABLE_DECLARATION_STATEMENT:
			className = ((SimpleName) (((SimpleType) (((VariableDeclarationStatement) temp)
					.getType())).getName())).getFullyQualifiedName();
			break;
		}
	}

}