package com.mydomain.mymode;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;

import processing.app.Base;
import processing.app.SketchCode;
import processing.mode.java.JavaEditor;
import processing.mode.java.pdex.ASTNodeWrapper;
import processing.mode.java.pdex.ErrorCheckerService;
import processing.mode.java.pdex.ASTGenerator;
import processing.mode.java.pdex.OffsetMatcher;
import org.eclipse.jdt.core.dom.Name;


public class JavadocASTGenerator extends ASTGenerator {
	public JavadocASTGenerator(ErrorCheckerService ecs) {
		super(ecs);
	}

	/**
	 * Given a word(identifier) in pde code, finds its location in the ASTNode
	 * 
	 * @param lineNumber
	 * @param name
	 * @param offset
	 *            - line start nonwhitespace offset
	 * @param scrollOnly
	 * @return
	 */
	@Override
	public ASTNodeWrapper getASTNodeAt(int lineNumber, String name, int offset,
			boolean scrollOnly) {

		System.out.println("Here");
		// Convert tab based pde line number to actual line number
		int pdeLineNumber = lineNumber + errorCheckerService.mainClassOffset;
		// log("----getASTNodeAt---- CU State: "
		// + errorCheckerService.compilationUnitState);
		if (errorCheckerService != null) {
			editor = errorCheckerService.getEditor();
			int codeIndex = editor.getSketch().getCodeIndex(
					editor.getCurrentTab());
			if (codeIndex > 0) {
				for (int i = 0; i < codeIndex; i++) {
					SketchCode sc = editor.getSketch().getCode(i);
					int len = Base.countLines(sc.getProgram()) + 1;
					pdeLineNumber += len;
				}
			}

		}

		// Find closest ASTNode to the linenumber
		// log("getASTNodeAt: Node line number " + pdeLineNumber);
		ASTNode lineNode = findLineOfNode(compilationUnit, pdeLineNumber,
				offset, name);
		
		System.out.println(lineNode.toString());

		// log("Node text +> " + lineNode);
		ASTNode decl = null;
		String nodeLabel = null;
		String nameOfNode = null; // The node name which is to be scrolled to

		// Obtain corresponding java code at that line, match offsets
		if (lineNode != null) {
			String pdeCodeLine = errorCheckerService.getPDECodeAtLine(editor
					.getSketch().getCurrentCodeIndex(), lineNumber);
			String javaCodeLine = getJavaSourceCodeLine(pdeLineNumber);

			// log(lineNumber + " Original Line num.\nPDE :" + pdeCodeLine);
			// log("JAVA:" + javaCodeLine);
			// log("Clicked on: " + name + " start offset: " + offset);
			// Calculate expected java offset based on the pde line
			OffsetMatcher ofm = new OffsetMatcher(pdeCodeLine, javaCodeLine);
			int javaOffset = ofm.getJavaOffForPdeOff(offset, name.length())
					+ lineNode.getStartPosition();
			// log("JAVA ast offset: " + (javaOffset));

			// Find the corresponding node in the AST
			ASTNode simpName = dfsLookForASTNode(
					errorCheckerService.getLatestCU(), name, javaOffset,
					javaOffset + name.length());

			// If node wasn't found in the AST, lineNode may contain something
			if (simpName == null && lineNode instanceof SimpleName) {
				switch (lineNode.getParent().getNodeType()) {
				case ASTNode.TYPE_DECLARATION:

				case ASTNode.METHOD_DECLARATION:

				case ASTNode.FIELD_DECLARATION:

				case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
					decl = lineNode.getParent();
					return new ASTNodeWrapper(decl, "");
				default:
					break;
				}
			}

			// SimpleName instance found, now find its declaration in code
			if (simpName instanceof SimpleName) {
				nameOfNode = simpName.toString();
				// log(getNodeAsString(simpName));
				decl = findDeclaration((SimpleName) simpName);
				if (decl != null) {
					// Base.loge("DECLA: " + decl.getClass().getName());
					nodeLabel = getLabelIfType(new ASTNodeWrapper(decl),
							(SimpleName) simpName);
					// retLabelString = getNodeAsString(decl);
				} else {
					// Base.loge("null");
					if (scrollOnly) {
						editor.statusMessage(simpName
								+ " is not defined in this sketch",
								JavaEditor.STATUS_ERR);
					} else {
						return new ASTNodeWrapper(lineNode);
					}
				}

				// log(getNodeAsString(decl));

				/*
				 * // - findDecl3 testing
				 * 
				 * ASTNode nearestNode = findClosestNode(lineNumber, (ASTNode)
				 * compilationUnit.types() .get(0)); ClassMember cmem =
				 * resolveExpression3rdParty(nearestNode, (SimpleName) simpName,
				 * false); if (cmem != null) { log("CMEM-> " + cmem); } else
				 * log("CMEM-> null");
				 */
			}
		}

		if (decl != null && scrollOnly) {
			/*
			 * For scrolling, we highlight just the name of the node, i.e., a
			 * SimpleName instance. But the declared node always points to the
			 * declared node itself, like TypeDecl, MethodDecl, etc. This is
			 * important since it contains all the properties.
			 */
			ASTNode simpName2 = getNodeName(decl, nameOfNode);
			// Base.loge("FINAL String decl: " + getNodeAsString(decl));
			// Base.loge("FINAL String label: " + getNodeAsString(simpName2));
			// errorCheckerService.highlightNode(simpName2);
			ASTNodeWrapper declWrap = new ASTNodeWrapper(simpName2, nodeLabel);
			// errorCheckerService.highlightNode(declWrap);
			if (!declWrap.highlightNode(this)) {
				Base.loge("Highlighting failed.");
			}
		}

		// Return the declaration wrapped as ASTNodeWrapper
		return new ASTNodeWrapper(decl, nodeLabel);
	}
	
	protected static ASTNode findDeclaration(Name findMe){
		return ASTGenerator.findDeclaration(findMe);
	}

}