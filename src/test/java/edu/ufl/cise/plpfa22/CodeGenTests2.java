/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import edu.ufl.cise.plpfa22.CodeGenUtils.DynamicClassLoader;
import edu.ufl.cise.plpfa22.CodeGenUtils.GenClass;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.PrettyPrintVisitor;

public class CodeGenTests2 {

	/**
	 * Generates classfiles for the given source program. The classfile containing
	 * the main method has the given name and package.
	 * Other classfiles are synthetic inner classes that correspond to procedures.
	 * 
	 * @param input
	 * @param className
	 * @param packageName
	 * @return List of CodeGenUtils.GenClass records
	 * @throws Exception
	 */
	List<GenClass> compile(String input, String className, String packageName) throws Exception {
		show("*****************");
		show(input);
		ILexer lexer = CompilerComponentFactory.getLexer(input);
		ASTNode ast = CompilerComponentFactory.getParser(lexer).parse();
		ast.visit(CompilerComponentFactory.getScopeVisitor(), null);
		ast.visit(CompilerComponentFactory.getTypeInferenceVisitor(), null);
		show(ast);
		List<GenClass> classes = (List<GenClass>) ast
				.visit(CompilerComponentFactory.getCodeGenVisitor(className, packageName, ""), null);
		show(classes);
		show("----------------");
		return classes;
	}

	Object loadClassesAndRunMain(List<GenClass> classes, String className) throws Exception {
		DynamicClassLoader loader = new DynamicClassLoader();
		Class<?> mainClass = loader.define(classes);
		Object[] args = new Object[1];
		return runMethod(mainClass, "main", args);
	}

	private Method findMethod(String name, Method[] methods) {
		for (Method m : methods) {
			String methodName = m.getName();
			if (name.equals(methodName))
				return m;
		}
		throw new RuntimeException("Method " + name + " not found in generated bytecode");
	}

	Object runMethod(Class<?> testClass, String methodName, Object[] args) throws Exception {
		Method[] methods = testClass.getDeclaredMethods();
		Method m = findMethod(methodName, methods);
		return m.invoke(null, args);
	}

	static boolean VERBOSE = true;

	void show(Object o) {
		if (VERBOSE) {
			System.out.println(o);
		}
	}

	void show(byte[] bytecode) {
		show(CodeGenUtils.bytecodeToString(bytecode));
	}

	void show(GenClass genClass) {
		show(genClass.className());
		show(genClass.byteCode());
	}

	void show(List<GenClass> classes) {
		for (GenClass aClass : classes)
			show(aClass);
	}

	void show(ASTNode ast) throws PLPException {
		if (VERBOSE) {
			if (ast != null) {
				System.out.println(PrettyPrintVisitor.AST2String(ast));
			} else {
				System.out.println("ast = null");
			}
		}
	}

	@DisplayName("numOut")
	@Test
	public void numout(TestInfo testInfo) throws Exception {
		String input = """
				! 3
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);

	}

	@DisplayName("stringOut")
	@Test
	public void stringout(TestInfo testInfo) throws Exception {
		String input = """
				! "hello world"
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("booleanOut")
	@Test
	public void booleanOut(TestInfo testInfo) throws Exception {
		String input = """
				! TRUE
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("statementBlock")
	@Test
	public void statementBlock(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! 3;
				! FALSE;
				! "hey, it works!"
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("intOps")
	@Test
	public void intOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! 1+3;
				! 7-3;
				! 2*2;
				! 16/4;
				! 20%8;
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("intEqOps")
	@Test
	public void intEqOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! 3 = 4;
				! 3 = 3;
				! 3 # 4;
				! 3 # 3
				END
				.
				""";

		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);

	}

	@DisplayName("boolEqOps")
	@Test
	public void boolEqOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! TRUE = TRUE;
				! TRUE # TRUE;
				! FALSE = FALSE;
				! FALSE # FALSE;
				! TRUE = FALSE;
				! TRUE # FALSE;
				! FALSE = TRUE;
				! FALSE # TRUE;
				END
				.
				""";

		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);

	}

	@DisplayName("intRelOps")
	@Test
	public void intRelOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! 3 < 4;
				! 3 <= 4;
				! 3 > 4;
				! 3 >= 4;
				! 4 < 4;
				! 4 <= 4;
				! 4 > 4;
				! 4 >= 4;
				! 4 < 3;
				! 4 <= 3;
				! 4 > 3;
				! 4 >= 3
				END
				.
				""";

		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("boolRelOps")
	@Test
	public void boolRelOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! FALSE < TRUE;
				! FALSE <= TRUE;
				! FALSE > TRUE;
				! FALSE >= TRUE;
				! TRUE < TRUE;
				! TRUE <= TRUE;
				! TRUE > TRUE;
				! TRUE >= TRUE;
				! TRUE < FALSE;
				! TRUE <= FALSE;
				! TRUE > FALSE;
				! TRUE >= FALSE	;
				! FALSE < FALSE;
				! FALSE <= FALSE;
				! FALSE > FALSE;
				! FALSE >= FALSE
				END
				.
				""";

		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("stringEqOps")
	@Test
	public void stringEqOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! "red" = "blue";
				! "red"= "red";
				! "red" # "blue";
				! "red" # "red"
				END
				.
				""";

		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);

	}

	@DisplayName("stringRelOps")
	@Test
	public void stringRelOps(TestInfo testInfo) throws Exception {
		String input = """
				BEGIN
				! "FA" < "FALSE";
				! "FA" <= "FALSE";
				! "FA" > "FALSE";
				! "FA" >= "FALSE";
				! "FALSE" < "FALSE";
				! "FALSE" <= "FALSE";
				! "FALSE" > "FALSE";
				! "FALSE" >= "FALSE";
				! "FALSE" < "FA";
				! "FALSE" <= "FA";
				! "FALSE" > "FA";
				! "FALSE" >= "FA"	;
				! "FA" < "FA";
				! "FA" <= "FA";
				! "FA" > "FA";
				! "FA" >= "FA"
				END
				.
				""";

		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("constants")
	@Test
	public void constants(TestInfo testInfo) throws Exception {
		String input = """
				CONST a=33, b="HELLO", c=FALSE;
				BEGIN
				!a;
				!b;
				!c
				END.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("template")
	@Test
	public void template(TestInfo testInfo) throws Exception {
		String input = """
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("const1")
	@Test
	public void const1(TestInfo testInfo) throws Exception {
		String input = """
				CONST a = 3;
				VAR x;
				BEGIN
				x := a;
				!x
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("const2")
	@Test
	public void const2(TestInfo testInfo) throws Exception {
		String input = """
				CONST a = 3;
				PROCEDURE p;
					!a
				;
				CALL p
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("const3")
	@Test
	public void const3(TestInfo testInfo) throws Exception {
		String input = """
				CONST a = 3;
				PROCEDURE p;
					CONST a = 4;
					!a
				;
				CALL p
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("const4")
	@Test
	public void const4(TestInfo testInfo) throws Exception {
		String input = """
				CONST a = 3;
				PROCEDURE p;
					PROCEDURE q;
						!a
					;
					CALL q
				;
				CALL p
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("procStructure")
	@Test
	public void procStructure(TestInfo testInfo) throws Exception {
		String input = """
				PROCEDURE p;
					PROCEDURE r;
						PROCEDURE t;;
					;
					PROCEDURE s;;
				;
				PROCEDURE q;;

				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("procCalls")
	@Test
	public void procCalls(TestInfo testInfo) throws Exception {
		String input = """
				PROCEDURE p;
					PROCEDURE r;
						PROCEDURE t;
							! "in proc t"
						;
						BEGIN
						! "in proc r";
						CALL q
						END
					;
					PROCEDURE s;;
					BEGIN
					! "in proc p";
					CALL r
					END
				;
				PROCEDURE q;
					! "in proc q"
				;
				BEGIN
				CALL p
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("proc1")
	@Test
	public void proc1(TestInfo testInfo) throws Exception {
		String input = """
				CONST a = 22, b = "HELLO", c = FALSE;
				PROCEDURE p1;
				BEGIN
				!a;
				!b;
				!c
				END;
				CALL p1
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("var1")
	@Test
	public void var1(TestInfo testInfo) throws Exception {
		String input = """
				VAR a,b,c;
				BEGIN
				a := 42;
				b := "hello";
				c := TRUE;
				!a;
				!b;
				!c
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("var2")
	@Test
	public void var2(TestInfo testInfo) throws Exception {
		String input = """
				VAR a,b,c;
				PROCEDURE p;
				BEGIN
				a := 42;
				b := "hello";
				c := TRUE;
				!a;
				!b;
				!c
				END;
				CALL p
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("var3")
	@Test
	public void var3(TestInfo testInfo) throws Exception {
		String input = """
				VAR a,b,c;
				PROCEDURE p;
					PROCEDURE q;
						BEGIN
						a := 42;
						b := "hello";
						c := TRUE;
						!a;
						!b;
						!c
						END;
					CALL q;
				CALL p
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("proc2")
	@Test
	public void proc2(TestInfo testInfo) throws Exception {
		String input = """
				VAR a,b,c;
					PROCEDURE p;
						PROCEDURE q;
							BEGIN
							! "in q, calling q1";
							//! "debug run, no CALL q1";
							CALL q1;
							END;
							//end of q
					BEGIN
					  ! "in p";
					  IF a > 0 THEN BEGIN ! "calling q"; CALL q END
					END;
					//end of p
					PROCEDURE q1;
						PROCEDURE r;
						BEGIN
						   a := 0;
						   ! " in r, calling p";
						   CALL p
						END;
						//end of r
					BEGIN
					    ! "in q1 calling r";
					    CALL r;
					END;
					// end of q
				BEGIN  //main
				   a := 1;
				   ! "in main calling p, a=1";
				   CALL p ;
				   ! "terminating back in main"
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("rec2")
	@Test
	public void rec2(TestInfo testInfo) throws Exception {
		String input = """
				VAR a;
				PROCEDURE p;
				BEGIN
				  ! "in p, a=";
				  ! a;
				  a := a-1;
				  IF a >= 0 THEN CALL q
				END;//end of p

				PROCEDURE q;
				BEGIN
				  ! "in q, a=";
				  ! a;
				  a := a-1;
				  IF a >= 0 THEN CALL p
				END;//end of q
				BEGIN
				a := 6;
				CALL p
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("rec1")
	@Test
	public void rec1(TestInfo testInfo) throws Exception {
		String input = """
				VAR a;
				PROCEDURE p;
				BEGIN
				  ! "in p, a=";
				  ! a;
				  a := a-1;
				  IF a >= 0 THEN CALL p
				END;//end of p

				BEGIN
				a := 6;
				CALL p
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

	@DisplayName("while0")
	@Test
	public void while0(TestInfo testInfo) throws Exception {
		String input = """
				VAR a, minus1;

				BEGIN
				a := 6;
				minus1 := 0-1;
				WHILE a > minus1 DO BEGIN !a; a := a-1 END
				END
				.
				""";
		String shortClassName = "prog";
		String JVMpackageName = "edu/ufl/cise/plpfa22";
		List<GenClass> classes = compile(input, shortClassName, JVMpackageName);
		Object[] args = new Object[1];
		String className = "edu.ufl.cise.plpfa22.prog";
		loadClassesAndRunMain(classes, className);
	}

}
