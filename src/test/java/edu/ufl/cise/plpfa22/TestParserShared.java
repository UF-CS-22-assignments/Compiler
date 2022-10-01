package edu.ufl.cise.plpfa22;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
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
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.ast.ConstDec;

class TestParserShared {

    ASTNode getAST(String input) throws PLPException {
        IParser parser = CompilerComponentFactory.getParser(CompilerComponentFactory.getLexer(input));
        return parser.parse();
    }

    @Test
    void coleTest1() throws PLPException {
        String input = """
                VAR x,y;
                PROCEDURE random;
                    CALL random_int;
                BEGIN
                x := 10;
                y := random;
                IF (x > y) THEN ! \"Number 1 is larger than number 2\";
                IF (x < y) THEN ! \"Number 2 is larger than number 1\";
                IF (x = y) THEN ! \"Number 1 is equal to number 2\"
                END
                .
                """;
        ASTNode ast = getAST(input);
        assertThat("", ast, instanceOf(Program.class));
        Block v0 = ((Program) ast).block;
        assertThat("", v0, instanceOf(Block.class));
        List<VarDec> v1 = ((Block) v0).varDecs;
        assertEquals(2, v1.size());
        IToken v2 = ((VarDec) v1.get(0)).ident;
        assertEquals("x", String.valueOf(v2.getText()));
        IToken v3 = ((VarDec) v1.get(1)).ident;
        assertEquals("y", String.valueOf(v3.getText()));
        List<ProcDec> v4 = ((Block) v0).procedureDecs;
        assertEquals(1, v4.size());
        IToken v5 = ((ProcDec) v4.get(0)).ident;
        assertEquals("random", String.valueOf(v5.getText()));
        Block v6 = ((ProcDec) v4.get(0)).block;
        Statement v7 = ((Block) v6).statement;
        assertThat("", v7, instanceOf(StatementCall.class));
        Ident v8 = ((StatementCall) v7).ident;
        assertEquals("random_int", String.valueOf(v8.getText()));
        Statement v9 = ((Block) v0).statement;
        assertThat("", v9, instanceOf(StatementBlock.class));
        Statement v10 = ((StatementBlock) v9).statements.get(0);
        assertThat("", v10, instanceOf(StatementAssign.class));
        Ident v11 = ((StatementAssign) v10).ident;
        assertEquals("x", String.valueOf(v11.getText()));
        Expression v12 = ((StatementAssign) v10).expression;
        assertThat("", v12, instanceOf(ExpressionNumLit.class));
        Statement v13 = ((StatementBlock) v9).statements.get(1);
        assertThat("", v13, instanceOf(StatementAssign.class));
        Ident v14 = ((StatementAssign) v13).ident;
        assertEquals("y", String.valueOf(v14.getText()));
        Expression v15 = ((StatementAssign) v13).expression;
        assertThat("", v15, instanceOf(ExpressionIdent.class));
        Statement v16 = ((StatementBlock) v9).statements.get(2);
        assertThat("", v16, instanceOf(StatementIf.class));
        Expression v17 = ((StatementIf) v16).expression;
        assertEquals("x", String.valueOf(((ExpressionBinary) v17).e0.getFirstToken().getText()));
        assertEquals(">", String.valueOf(((ExpressionBinary) v17).op.getText()));
        assertEquals("y", String.valueOf(((ExpressionBinary) v17).e1.getFirstToken().getText()));
        Statement v18 = ((StatementIf) v16).statement;
        assertThat("", v18, instanceOf(StatementOutput.class));
        Expression v19 = ((StatementOutput) v18).expression;
        assertThat("", v19, instanceOf(ExpressionStringLit.class));
        assertEquals("\"Number 1 is larger than number 2\"", String.valueOf(v19.getFirstToken().getText()));
        Statement v20 = ((StatementBlock) v9).statements.get(3);
        assertThat("", v20, instanceOf(StatementIf.class));
        Expression v21 = ((StatementIf) v20).expression;
        assertEquals("x", String.valueOf(((ExpressionBinary) v21).e0.getFirstToken().getText()));
        assertEquals("<", String.valueOf(((ExpressionBinary) v21).op.getText()));
        assertEquals("y", String.valueOf(((ExpressionBinary) v21).e1.getFirstToken().getText()));
        Statement v22 = ((StatementIf) v20).statement;
        assertThat("", v22, instanceOf(StatementOutput.class));
        Expression v23 = ((StatementOutput) v22).expression;
        assertThat("", v23, instanceOf(ExpressionStringLit.class));
        assertEquals("\"Number 2 is larger than number 1\"", String.valueOf(v23.getFirstToken().getText()));
        Statement v24 = ((StatementBlock) v9).statements.get(4);
        assertThat("", v24, instanceOf(StatementIf.class));
        Expression v25 = ((StatementIf) v24).expression;
        assertEquals("x", String.valueOf(((ExpressionBinary) v25).e0.getFirstToken().getText()));
        assertEquals("=", String.valueOf(((ExpressionBinary) v25).op.getText()));
        assertEquals("y", String.valueOf(((ExpressionBinary) v25).e1.getFirstToken().getText()));
        Statement v26 = ((StatementIf) v24).statement;
        assertThat("", v26, instanceOf(StatementOutput.class));
        Expression v27 = ((StatementOutput) v26).expression;
        assertThat("", v27, instanceOf(ExpressionStringLit.class));
        assertEquals("\"Number 1 is equal to number 2\"",
                String.valueOf(v27.getFirstToken().getText()));
    }

    @Test
    void coleTest2() throws PLPException {
        String input = """
                ! x = y # 10 > 7 <= 45 < 50 >= 56
                .
                """;
        ASTNode ast = getAST(input);
        assertThat("", ast, instanceOf(Program.class));
        Block v0 = ((Program) ast).block;
        assertThat("", v0, instanceOf(Block.class));
        Statement v1 = ((Block) v0).statement;
        assertThat("", v1, instanceOf(StatementOutput.class));
        Expression v2 = ((StatementOutput) v1).expression;
        assertThat("", v2, instanceOf(ExpressionBinary.class));
        assertEquals(">=", String.valueOf(((ExpressionBinary) v2).op.getText()));
        assertEquals("56", String.valueOf(((ExpressionBinary) v2).e1.getFirstToken().getText()));
        Expression v3 = ((ExpressionBinary) v2).e0;
        assertThat("", v3, instanceOf(ExpressionBinary.class));
        assertEquals("<", String.valueOf(((ExpressionBinary) v3).op.getText()));
        assertEquals("50", String.valueOf(((ExpressionBinary) v3).e1.getFirstToken().getText()));
        Expression v4 = ((ExpressionBinary) v3).e0;
        assertThat("", v4, instanceOf(ExpressionBinary.class));
        assertEquals("<=", String.valueOf(((ExpressionBinary) v4).op.getText()));
        assertEquals("45", String.valueOf(((ExpressionBinary) v4).e1.getFirstToken().getText()));
        Expression v5 = ((ExpressionBinary) v4).e0;
        assertThat("", v5, instanceOf(ExpressionBinary.class));
        assertEquals(">", String.valueOf(((ExpressionBinary) v5).op.getText()));
        assertEquals("7", String.valueOf(((ExpressionBinary) v5).e1.getFirstToken().getText()));
        Expression v6 = ((ExpressionBinary) v5).e0;
        assertThat("", v6, instanceOf(ExpressionBinary.class));
        assertEquals("#", String.valueOf(((ExpressionBinary) v6).op.getText()));
        assertEquals("10", String.valueOf(((ExpressionBinary) v6).e1.getFirstToken().getText()));
        Expression v7 = ((ExpressionBinary) v6).e0;
        assertThat("", v7, instanceOf(ExpressionBinary.class));
        assertEquals("=", String.valueOf(((ExpressionBinary) v7).op.getText()));
        assertEquals("y", String.valueOf(((ExpressionBinary) v7).e1.getFirstToken().getText()));
        assertEquals("x", String.valueOf(((ExpressionBinary) v7).e0.getFirstToken().getText()));
    }

    @Test
    void test18() throws PLPException {
        String input = """
                ! ((2-a)+4)+3
                .
                """;
        ASTNode ast = getAST(input);
        assertThat("", ast, instanceOf(Program.class));
        Block v0 = ((Program) ast).block;
        Statement v1 = ((Block) v0).statement;
        assertThat("", v1, instanceOf(StatementOutput.class));
        Expression v2 = ((StatementOutput) v1).expression;
        assertThat("", v2, instanceOf(ExpressionBinary.class));
        Expression v3 = ((ExpressionBinary) v2).e0;
        assertThat("", v3, instanceOf(ExpressionBinary.class));
        Expression v4 = ((ExpressionBinary) v3).e0;
        assertThat("", v4, instanceOf(ExpressionBinary.class));
        Expression v5 = ((ExpressionBinary) v4).e0;
        assertThat("", v5, instanceOf(ExpressionNumLit.class));
        assertEquals("2", String.valueOf(v5.firstToken.getText()));
        Expression v6 = ((ExpressionBinary) v4).e1;
        assertThat("", v6, instanceOf(ExpressionIdent.class));
        assertEquals("a", String.valueOf(v6.firstToken.getText()));
        Expression v7 = ((ExpressionBinary) v3).e1;
        assertThat("", v7, instanceOf(ExpressionNumLit.class));
        assertEquals("4", String.valueOf(v7.firstToken.getText()));
        Expression v8 = ((ExpressionBinary) v2).e1;
        assertThat("", v8, instanceOf(ExpressionNumLit.class));
        assertEquals("3", String.valueOf(v8.firstToken.getText()));
        IToken v9 = ((ExpressionBinary) v2).op;
        assertEquals("+", String.valueOf(v9.getText()));
        IToken v10 = ((ExpressionBinary) v3).op;
        assertEquals("+", String.valueOf(v10.getText()));
        IToken v11 = ((ExpressionBinary) v4).op;
        assertEquals("-", String.valueOf(v11.getText()));
    }

    @Test
    void InvalidEndChar() throws PLPException {
        String input = """
                VAR abc;
                . bruh
                """;
        assertThrows(SyntaxException.class, () -> {
            @SuppressWarnings("unused")
            ASTNode ast = getAST(input);
        });
    }

    @Test
    // Test the expression with parenthesis
    void shr_test1() throws PLPException {
        String input = """
                ! 40/((4+1)*2)
                .
                """;
        ASTNode ast = getAST(input);
        assertThat("", ast, instanceOf(Program.class));
        Block v0 = ((Program) ast).block;
        assertThat("", v0, instanceOf(Block.class));
        List<ConstDec> v1 = ((Block) v0).constDecs;
        assertEquals(0, v1.size());
        List<VarDec> v2 = ((Block) v0).varDecs;
        assertEquals(0, v2.size());
        List<ProcDec> v3 = ((Block) v0).procedureDecs;
        assertEquals(0, v3.size());
        Statement v4 = ((Block) v0).statement;
        assertThat("", v4, instanceOf(StatementOutput.class));
        Expression v5 = ((StatementOutput) v4).expression;
        assertThat("", v5, instanceOf(ExpressionBinary.class));
        Expression v6 = ((ExpressionBinary) v5).e0;
        assertThat("", v6, instanceOf(ExpressionNumLit.class));
        IToken v7 = ((ExpressionNumLit) v6).firstToken;
        assertEquals("40", String.valueOf(v7.getText()));
        IToken v8 = ((ExpressionBinary) v5).op;
        assertEquals("/", String.valueOf(v8.getText()));
        Expression v9 = ((ExpressionBinary) v5).e1;
        assertThat("", v9, instanceOf(ExpressionBinary.class));
        Expression v10 = ((ExpressionBinary) v9).e0;
        assertThat("", v10, instanceOf(ExpressionBinary.class));
        Expression v11 = ((ExpressionBinary) v10).e0;
        assertThat("", v11, instanceOf(ExpressionNumLit.class));
        IToken v12 = ((ExpressionNumLit) v11).firstToken;
        assertEquals("4", String.valueOf(v12.getText()));
        IToken v13 = ((ExpressionBinary) v10).op;
        assertEquals("+", String.valueOf(v13.getText()));
        Expression v14 = ((ExpressionBinary) v10).e1;
        assertThat("", v14, instanceOf(ExpressionNumLit.class));
        IToken v15 = ((ExpressionNumLit) v14).firstToken;
        assertEquals("1", String.valueOf(v15.getText()));
        IToken v16 = ((ExpressionBinary) v9).op;
        assertEquals("*", String.valueOf(v16.getText()));
        Expression v17 = ((ExpressionBinary) v9).e1;
        assertThat("", v14, instanceOf(ExpressionNumLit.class));
        IToken v18 = ((ExpressionNumLit) v17).firstToken;
        assertEquals("2", String.valueOf(v18.getText()));
    }

    @Test
    // Test the expression with parenthesis
    void shr_test2() throws PLPException {
        String input = """
                			! 2+3*4
                			.
                """;
        ASTNode ast = getAST(input);
        assertThat("", ast, instanceOf(Program.class));
        Block v0 = ((Program) ast).block;
        assertThat("", v0, instanceOf(Block.class));
        List<ConstDec> v1 = ((Block) v0).constDecs;
        assertEquals(0, v1.size());
        List<VarDec> v2 = ((Block) v0).varDecs;
        assertEquals(0, v2.size());
        List<ProcDec> v3 = ((Block) v0).procedureDecs;
        assertEquals(0, v3.size());
        Statement v4 = ((Block) v0).statement;
        assertThat("", v4, instanceOf(StatementOutput.class));
        Expression v5 = ((StatementOutput) v4).expression;
        assertThat("", v5, instanceOf(ExpressionBinary.class));
        Expression v6 = ((ExpressionBinary) v5).e0;
        assertThat("", v6, instanceOf(ExpressionNumLit.class));
        IToken v7 = ((ExpressionNumLit) v6).firstToken;
        assertEquals("2", String.valueOf(v7.getText()));
        IToken v8 = ((ExpressionBinary) v5).op;
        assertEquals("+", String.valueOf(v8.getText()));
        Expression v9 = ((ExpressionBinary) v5).e1;
        assertThat("", v9, instanceOf(ExpressionBinary.class));
        Expression v10 = ((ExpressionBinary) v9).e0;
        assertThat("", v10, instanceOf(ExpressionNumLit.class));
        IToken v11 = ((ExpressionNumLit) v10).firstToken;
        assertEquals("3", String.valueOf(v11.getText()));
        IToken v12 = ((ExpressionBinary) v9).op;
        assertEquals("*", String.valueOf(v12.getText()));
        Expression v13 = ((ExpressionBinary) v9).e1;
        assertThat("", v13, instanceOf(ExpressionNumLit.class));
        IToken v14 = ((ExpressionNumLit) v13).firstToken;
        assertEquals("4", String.valueOf(v14.getText()));
    }

    @Test
    // Test the Procedure keyword
    void shr_test3() throws PLPException {
        String input = """
                    PROCEDURE m;
                       ! "Procedure is working." ;
                    .
                """;
        // According to Phrase Structure, Should have “;” at end of procedure.
        // DS - 2022-09-30
        ASTNode ast = getAST(input);
        assertThat("", ast, instanceOf(Program.class));
        Block v0 = ((Program) ast).block;
        assertThat("", v0, instanceOf(Block.class));
        List<ConstDec> v1 = ((Block) v0).constDecs;
        assertEquals(0, v1.size());
        List<VarDec> v2 = ((Block) v0).varDecs;
        assertEquals(0, v2.size());
        List<ProcDec> v3 = ((Block) v0).procedureDecs;
        assertEquals(1, v3.size());
        assertThat("", v3.get(0), instanceOf(ProcDec.class));
        Statement v4 = ((Block) v0).statement;
        assertThat("", v4, instanceOf(StatementEmpty.class));
        IToken v5 = ((ProcDec) v3.get(0)).ident;
        assertEquals("m", String.valueOf(v5.getText()));
        Block v6 = ((ProcDec) v3.get(0)).block;
        assertThat("", v6, instanceOf(Block.class));
        List<ConstDec> v7 = ((Block) v6).constDecs;
        assertEquals(0, v7.size());
        List<VarDec> v8 = ((Block) v6).varDecs;
        assertEquals(0, v8.size());
        List<ProcDec> v9 = ((Block) v6).procedureDecs;
        assertEquals(0, v9.size());
        Block v11 = ((ProcDec) v3.get(0)).block;
        Statement v12 = ((Block) v11).statement;
        assertThat("", v12, instanceOf(StatementOutput.class));
        Expression v13 = ((StatementOutput) v12).expression;
        assertThat("", v13, instanceOf(ExpressionStringLit.class));
        assertEquals("Procedure is working.", v13.getFirstToken().getStringValue());
    }

    // Test the expression with parenthesis and incorrect semicolon
    @Test
    void shr_test4() {
        String input = """
                    ! 40/((4+1)*2);
                    .
                """;
        assertThrows(SyntaxException.class, () -> {
            @SuppressWarnings("unused")
            ASTNode ast = getAST(input);
        });
    }

    @Test
    void shr_test5() throws PLPException {
        String input = """
                    WHILE (x > y) DO ! \"Number 1 is larger than number 2\"
                    .
                """;
        // <program> <statement (WHILE)> <statement (!)> <expression> . . . does not
        // have a terminal “;” DS 2022-09-30
        ASTNode ast = getAST(input);
        assertThat("", ast, instanceOf(Program.class));
        Block v0 = ((Program) ast).block;
        assertThat("", v0, instanceOf(Block.class));
        List<ConstDec> v1 = ((Block) v0).constDecs;
        assertEquals(0, v1.size());
        List<VarDec> v2 = ((Block) v0).varDecs;
        assertEquals(0, v2.size());
        List<ProcDec> v3 = ((Block) v0).procedureDecs;
        assertEquals(0, v3.size());
        Statement v4 = ((Block) v0).statement;
        assertThat("", v4, instanceOf(StatementWhile.class));
        Expression v5 = ((StatementWhile) v4).expression;
        assertEquals("x", String.valueOf(((ExpressionBinary) v5).e0.getFirstToken().getText()));
        assertEquals(">", String.valueOf(((ExpressionBinary) v5).op.getText()));
        assertEquals("y", String.valueOf(((ExpressionBinary) v5).e1.getFirstToken().getText()));
        Statement v6 = ((StatementWhile) v4).statement;
        assertThat("", v6, instanceOf(StatementOutput.class));
        Expression v7 = ((StatementOutput) v6).expression;
        assertThat("", v7, instanceOf(ExpressionStringLit.class));
        assertEquals("\"Number 1 is larger than number 2\"", String.valueOf(v7.getFirstToken().getText()));
    }

}
