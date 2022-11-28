package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.Types.Type;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.CodeGenUtils.GenClass;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	final String packageName;
	final String className;
	final String sourceFileName;
	final String fullyQualifiedClassName;
	final String classDesc;

	ClassWriter classWriter;

	public CodeGenVisitor(String className, String packageName, String sourceFileName) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.sourceFileName = sourceFileName;
		this.fullyQualifiedClassName = packageName + "/" + className;
		this.classDesc = "L" + this.fullyQualifiedClassName + ';';
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws PLPException {
		MethodVisitor methodVisitor = (MethodVisitor) arg;
		methodVisitor.visitCode();
		for (ConstDec constDec : block.constDecs) {
			constDec.visit(this, null);
		}
		for (VarDec varDec : block.varDecs) {
			varDec.visit(this, methodVisitor);
		}
		for (ProcDec procDec : block.procedureDecs) {
			procDec.visit(this, null);
		}
		// add instructions from statement to method
		block.statement.visit(this, arg);
		methodVisitor.visitInsn(RETURN);
		methodVisitor.visitMaxs(0, 0);
		methodVisitor.visitEnd();
		return null;

	}

	@Override
	public Object visitProgram(Program program, Object arg) throws PLPException {
		// call the JVMNameVisitor to annotate the JVM nams for all the procedures
		JVMNameVisitor jvmNameVisitor = new JVMNameVisitor(this.fullyQualifiedClassName);
		program.visit(jvmNameVisitor, null);

		// create a classWriter and visit it
		classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		// Hint: if you get failures in the visitMaxs, try creating a ClassWriter with 0
		// instead of ClassWriter.COMPUTE_FRAMES. The result will not be a valid
		// classfile,
		// but you will be able to print it so you can see the instructions. After
		// fixing,
		// restore ClassWriter.COMPUTE_FRAMES
		classWriter.visit(V18, ACC_PUBLIC | ACC_SUPER, fullyQualifiedClassName, null, "java/lang/Object", null);

		// get a method visitor for the main method.
		MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V",
				null, null);
		// visit the block, passing it the methodVisitor
		program.block.visit(this, methodVisitor);
		// finish up the class
		classWriter.visitEnd();
		// return the bytes making up the classfile
		List<GenClass> genClasses = new ArrayList<>();
		genClasses.add(new GenClass(CodeGenUtils.toJMVClassName(this.packageName + '/' + this.className),
				classWriter.toByteArray()));

		return genClasses;
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		statementOutput.expression.visit(this, arg);
		Type etype = statementOutput.expression.getType();
		String JVMType = (etype.equals(Type.NUMBER) ? "I" : (etype.equals(Type.BOOLEAN) ? "Z" : "Ljava/lang/String;"));
		String printlnSig = "(" + JVMType + ")V";
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", printlnSig, false);
		return null;
	}

	@Override
	public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
		for (Statement statement : statementBlock.statements) {
			statement.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
		Expression expression = statementIf.expression;
		Statement statement = statementIf.statement;
		MethodVisitor mv = (MethodVisitor) arg;
		// the bool value will be stored on the top of the stack
		expression.visit(this, arg);
		Label labelPostIf = new Label();

		mv.visitJumpInsn(IFEQ, labelPostIf);
		statement.visit(this, arg);
		mv.visitLabel(labelPostIf);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		Type argType = expressionBinary.e0.getType();
		Kind op = expressionBinary.op.getKind();
		switch (argType) {
			case NUMBER -> {
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case PLUS -> mv.visitInsn(IADD);
					case MINUS -> mv.visitInsn(ISUB);
					case TIMES -> mv.visitInsn(IMUL);
					case DIV -> mv.visitInsn(IDIV);
					case MOD -> mv.visitInsn(IREM);
					case EQ, NEQ, LT, LE, GT, GE -> {
						compareNumberOrBoolean(mv, op);

					}

					default -> {
						throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
					}
				}

			}
			case BOOLEAN -> {
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case PLUS -> {
						// OR
						mv.visitInsn(IOR);

					}
					case TIMES -> {
						// AND
						mv.visitInsn(IAND);
					}
					case EQ, NEQ, LT, LE, GT, GE -> {
						compareNumberOrBoolean(mv, op);

					}
					default -> {
						throw new IllegalStateException("code gen bug in visitExpressionBinary BOOLEAN");
					}
				}

			}
			case STRING -> {
				expressionBinary.e0.visit(this, arg);
				expressionBinary.e1.visit(this, arg);
				switch (op) {
					case PLUS -> {
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
								"(Ljava/lang/String;)Ljava/lang/String;", false);
					}
					case EQ -> {
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals",
								"(Ljava/lang/Object;)Z", false);

					}
					case NEQ -> {
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals",
								"(Ljava/lang/Object;)Z", false);
						this.logicalNotTopStack(mv);

					}
					case LT -> {
						// s0 < s1
						// s1.startsWith(s0) && ! s0.equals(s1)

						mv.visitInsn(DUP2);
						// stack: ... s0, s1, s0, s1
						mv.visitInsn(SWAP);
						// stack: ... s0, s1, s1, s0

						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith",
								"(Ljava/lang/String;)Z", false); // s1.startsWith(s0)
						// stack: ... s0, s1, (s1.startsWith(s0))
						mv.visitInsn(DUP_X2);
						// stack: ... (s1.startsWith(s0)), s0, s1, (s1.startsWith(s0))
						mv.visitInsn(POP);
						// stack: ... (s1.startsWith(s0)), s0, s1

						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals",
								"(Ljava/lang/Object;)Z", false); // s0.equals(s1)
						// stack: ... (s1.startsWith(s0)), (s0.equals(s1))
						this.logicalNotTopStack(mv); // ! s0.equals(s1)
						// stack: ... (s1.startsWith(s0)), (!s1.equals(s0))
						mv.visitInsn(IAND); // s1.startsWith(s0) && ! s0.equals(s1)
						// stack: ... ((s1.startsWith(s0)) && (!s1.equals(s0)))
					}
					case LE -> {
						mv.visitInsn(SWAP);
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith",
								"(Ljava/lang/String;)Z", false); // s1.startsWith(s0)
					}
					case GT -> {
						// s0 > s1
						// s0.endswith(s1) && ! s0.equals(s1)

						mv.visitInsn(DUP2);
						// stack: ... s0, s1, s0, s1
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith",
								"(Ljava/lang/String;)Z", false); // s0.endsWith(s1)
						// stack: ... s0, s1, (s0.endsWith(s1))
						mv.visitInsn(DUP_X2);
						// stack: ...(s0.endsWith(s1)), s0, s1, (s0.endsWith(s1))
						mv.visitInsn(POP);
						// stack: ... (s0.endsWith(s1)), s0, s1
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals",
								"(Ljava/lang/Object;)Z", false); // s0.equals(s1)
						// stack: ... (s0.endsWith(s1)), (s0.equals(s1))
						this.logicalNotTopStack(mv); // !s0.equals(s1)
						// stack: ...(s0.endsWith(s1)), (!s0.equals(s1))
						mv.visitInsn(IAND);
						// stack: ... ((s0.endsWith(s1)) && (!s0.equals(s1)))

					}
					case GE -> {
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "endsWith",
								"(Ljava/lang/String;)Z", false); // s0.endsWith(s1)
					}
					default -> {
						throw new UnsupportedOperationException();
					}
				}
			}
			default -> {
				throw new IllegalStateException("code gen bug in visitExpressionBinary");
			}
		}
		return null;
	}

	/**
	 * negate the top element on the stack.
	 * The stack must be not empty and top element must be a boolean, but this
	 * method won't check it.
	 *
	 * if stack.pop() == 0, GOTO pushTrue
	 * push 0
	 * GOTO end
	 * label: pushTrue
	 * push 1
	 * label: end
	 *
	 * @param mv
	 */
	private void logicalNotTopStack(MethodVisitor mv) {
		Label pushTrue = new Label();
		Label end = new Label();
		mv.visitJumpInsn(IFEQ, pushTrue);
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, end);
		mv.visitLabel(pushTrue);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(end);
	}

	/**
	 * pop and compare the top two number or boolean on the stack, push back the
	 * result in boolean
	 *
	 * @param mv method visitor
	 * @param op operation type, can only be EQ, NEQ, LT, LE, GT, GE
	 */
	private void compareNumberOrBoolean(MethodVisitor mv, Kind op) {
		int comparOpcodes = switch (op) {
			case EQ -> {
				yield IF_ICMPEQ;
			}
			case NEQ -> {
				yield IF_ICMPNE;
			}
			case LT -> {
				yield IF_ICMPLT;
			}
			case LE -> {
				yield IF_ICMPLE;
			}
			case GT -> {
				yield IF_ICMPGT;
			}
			case GE -> {
				yield IF_ICMPGE;
			}
			default -> {
				throw new IllegalStateException("code gen bug in visitExpressionBinary NUMBER");
			}
		};
		Label labelNumCompareTrueBranch = new Label();
		Label labelPostNumCompareBranch = new Label();
		// if the top two number on the stack satisfy the corresponding comparison, jump
		// to compare true branch and push 1, other wise, push 0and jump to the end.
		mv.visitJumpInsn(comparOpcodes, labelNumCompareTrueBranch);

		// compare failed branch
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, labelPostNumCompareBranch);

		// compare true branch
		mv.visitLabel(labelNumCompareTrueBranch);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(labelPostNumCompareBranch);
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitLdcInsn(expressionNumLit.getFirstToken().getIntValue());
		return null;
	}

	@Override
	public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
		// put the string on the top of the stack
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitLdcInsn(expressionStringLit.getFirstToken().getStringValue());
		return null;
	}

	@Override
	public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
		// put the boolean on the top of the stack
		MethodVisitor mv = (MethodVisitor) arg;
		mv.visitLdcInsn(expressionBooleanLit.getFirstToken().getBooleanValue());
		return null;
	}

	@Override
	public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
		// nothing to do at constant declarations, values will be passed when ever the
		// const variable is used.
		return null;
	}

	@Override
	public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
		return null;
	}

	@Override
	public Object visitIdent(Ident ident, Object arg) throws PLPException {
		// assume a value is on the top of the stack, now store the value to the
		// variabled indicated by the ident
		// use the nesting level to go up chain of this$n vars for non-local variable.
		throw new UnsupportedOperationException();
	}

}