package cop5556fa17;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.Scanner.Kind.*;
import cop5556fa17.AST.*;

import static cop5556fa17.Scanner.Kind.*;

public class Parser 
{

	private static List<Kind> functionList = new ArrayList<Kind>(Arrays.asList(KW_sin, KW_cos, KW_atan, KW_abs, KW_cart_x, KW_cart_y, KW_polar_a, KW_polar_r));
	private static List<Kind> unaryList = new ArrayList<Kind>(Arrays.asList(KW_x, KW_y, KW_r, KW_a, KW_X, KW_Y, KW_Z, KW_A, KW_R, KW_DEF_X, KW_DEF_Y));
	private static List<Kind> declarationList = new ArrayList<Kind>(Arrays.asList(KW_int, KW_boolean, KW_url, KW_file, KW_image));
	
	@SuppressWarnings("serial")
	
	public class SyntaxException extends Exception 
	{
		Token t;

		public SyntaxException(Token t, String message) 
		{
			super(message);
			this.t = t;
		}

	}

	Scanner scanner;
	Token t;
	
	public Program parse() throws SyntaxException {

		Program p = program();
		matchEOF();
		return p;
	}

	Parser(Scanner scanner)
	{
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	Token consume() 
	{
		t = scanner.nextToken();
		return t;
	}
	
	void match(Kind kind) throws SyntaxException
	{
	       if(t.kind == kind) {
	    	   		consume();
	       } else {
	    	   		throw new SyntaxException(t, "Found "+t.kind+" but expected " +kind);
	       } 	  
	 }
	
	Index selector() throws SyntaxException
	{
		Token firstToken = t;
		Expression e = expression();
		match(COMMA);
		Expression e1 = expression();
		return new Index(firstToken,e,e1);
	}
	
	Index raSelector() throws SyntaxException
	{
		Token firstToken = t;
		Expression_PredefinedName e = new Expression_PredefinedName(firstToken,firstToken.kind);
		match(KW_r);
		match(COMMA);
		Expression_PredefinedName e1 = new Expression_PredefinedName(t,t.kind);
		match(KW_a);
		return new Index(firstToken,e,e1);
	}
	
	Index xySelector() throws SyntaxException
	{
		Token firstToken = t;
		Expression_PredefinedName e = new Expression_PredefinedName(firstToken,firstToken.kind);
		match(KW_x);
		match(COMMA);
		Expression_PredefinedName e1 = new Expression_PredefinedName(t,t.kind);
		match(KW_y);
		return new Index(firstToken, e,e1);
	}
	
	Index lhsSelector() throws SyntaxException
	{
		Index index;
		match(LSQUARE);
		if(t.kind == KW_x) {
			index = xySelector();
		} else if(t.kind == KW_r) {
			index = raSelector();
		} else {
			throw new SyntaxException(t, "Error at Lhs Selector on line" +t.line);
		}
		match(RSQUARE);
		return index;
	}
	
	void functionName() throws SyntaxException
	{
		if(functionList.contains(t.kind)) {
			consume();
		} else {
			throw new SyntaxException(t, "Error at Function Name on line" +t.line);
		}
	}
	
	Expression functionApplication() throws SyntaxException
	{
		Token firstToken = t;
		functionName();
		if(t.kind == LPAREN) {
			consume();
			Expression e = expression();
			match(RPAREN);
			return new Expression_FunctionAppWithExprArg(firstToken, firstToken.kind,e ); 
			}
		else if(t.kind == LSQUARE) {
			consume();
			Index index = selector();
			match(RSQUARE);
			return new Expression_FunctionAppWithIndexArg(firstToken, firstToken.kind, index); 
		} else {
			throw new SyntaxException(t, "Error at Function Application on line" +t.line);
		}
	}
	
	LHS lhs() throws SyntaxException
	{
		
		Token firstToken = t;
		match(IDENTIFIER);
		Index index = null;
		if(t.kind == LSQUARE) {
			consume();
			index = lhsSelector();
			match(RSQUARE);
		}	
		return new LHS(firstToken,firstToken,index);
	}
	
	Expression identOrPixelSelectorExpression () throws SyntaxException
	{
		Token firstToken = t;
		match(IDENTIFIER);
		if(t.kind==LSQUARE)
		{
			consume();
			Index index = selector();
			match(RSQUARE);
			return new Expression_PixelSelector(firstToken, firstToken, index);
		}
		return new Expression_Ident(firstToken, firstToken);
	}
	
	Expression primary() throws SyntaxException
	{
		Token firstToken = t;
		boolean value = true;
		int val;
		Expression e; 
		if(t.kind == BOOLEAN_LITERAL)
		{	if(firstToken.getText().equals("false"))
			value = false;
			consume();
			return new Expression_BooleanLit(firstToken, value);
		}
			
		else if(t.kind == INTEGER_LITERAL)
		{
			val = t.intVal();
			consume();
			return new Expression_IntLit(firstToken,val);
		} 
		else if(t.kind == LPAREN) {
			consume();
			e = expression();
			match(RPAREN);
			return e;
			
		} 
		else if(functionList.contains(t.kind)) {
			
			e = functionApplication();
			return e;
		} else {
			throw new SyntaxException(t, "Error at Primary on line" +t.line);
		}		
	}
	
	Expression unaryExpressionNotPlusMinus() throws SyntaxException
	{
		Token firstToken = t;
		if(t.kind == OP_EXCL) {
			consume();
			Expression e = unaryExpression();
			return new Expression_Unary(firstToken, firstToken,e);
		} else if(unaryList.contains(t.kind)) {
			consume();
			return new Expression_PredefinedName(firstToken,firstToken.kind);
			
		} else if (t.kind == INTEGER_LITERAL || t.kind == LPAREN || functionList.contains(t.kind) || t.kind == BOOLEAN_LITERAL) {
			Expression e = primary();
			return e;
		} else if(t.kind == IDENTIFIER) {
			Expression e = identOrPixelSelectorExpression();
			return e;
		} else {
			throw new SyntaxException(t, "Error at Unary Expression Not Plus Minus on line" +t.line);
		}
	}
	
	Expression unaryExpression() throws SyntaxException
	{
		Token firstToken = t;	
		if(t.kind == OP_PLUS || t.kind == OP_MINUS ||t.kind == OP_EXCL) {
			consume();
			Expression e = unaryExpression();
			return new Expression_Unary(firstToken, firstToken, e);
		
		} else if(unaryList.contains(t.kind)) {
			consume();
			return new Expression_PredefinedName(firstToken, firstToken.kind);
		} else if (t.kind == INTEGER_LITERAL || t.kind == LPAREN || functionList.contains(t.kind) || t.kind == BOOLEAN_LITERAL) {
			Expression e = primary();
			return e;
		} else if(t.kind == IDENTIFIER) {
			Expression e = identOrPixelSelectorExpression();
			return e;
		} else {
			throw new SyntaxException(t, "Error at Unary Expression on line" +t.line);
		}	
	}
	
	Expression multExpression() throws SyntaxException
	{
		Token firstToken = t;
		Expression e = unaryExpression();
		
		while(t.kind == OP_TIMES|| t.kind == OP_DIV || t.kind == OP_MOD)
		{
			Token op = t;
			consume();
			Expression e1= unaryExpression();
			e = new Expression_Binary(firstToken, e, op, e1);
		}
		return e;	
	}
	
	Expression addExpression() throws SyntaxException
	{
		Token firstToken = t;
		Expression e = multExpression();
		while(t.kind == OP_PLUS|| t.kind == OP_MINUS)
		{	
			Token op = t;
			consume();
			Expression e1= multExpression();
			e = new Expression_Binary(firstToken, e, op, e1);
		}
		return e;
	}
	
	Expression relExpression() throws SyntaxException
	{
		Token firstToken = t;
		Expression e = addExpression();
		while(t.kind == OP_LT|| t.kind == OP_GT || t.kind == OP_LE || t.kind == OP_GE)
		{
			Token op = t;
			consume();
			Expression e1 = addExpression();
			e = new Expression_Binary(firstToken, e, op, e1);
		}
		return e;
	}
	
	Expression eqExpression() throws SyntaxException
	{ 
		Token firstToken = t;
		Expression e = relExpression();
		while(t.kind == OP_EQ|| t.kind == OP_NEQ)
		{
			Token op = t;
			consume();
			Expression e1 = relExpression();
			e = new Expression_Binary(firstToken, e, op, e1);
		}
		return e;
	}
	
	Expression andExpression() throws SyntaxException
	{
		Token firstToken = t;
		Expression e = eqExpression();
		while(t.kind == OP_AND)
		{
			Token op = t;
			consume();
			Expression e1 = eqExpression();
			e = new Expression_Binary(firstToken, e, op, e1);
		}
		return e;
	}
	
	Expression orExpression() throws SyntaxException
	{
		Token firstToken = t;
		Expression e =andExpression();
		while(t.kind == OP_OR)
		{
			Token op = t;
			consume();
			Expression e1 = andExpression();
			e = new Expression_Binary(firstToken, e, op, e1);
		}
		return e;
	}
	
//	public void parse() throws SyntaxException 
//	{
//		program();
//		matchEOF();
//	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException
	{
		Token firstToken = t;
		ArrayList <ASTNode> astList = new ArrayList<ASTNode>();
		match(IDENTIFIER);
		while(declarationList.contains(t.kind) || t.kind == IDENTIFIER)
		{
			if(declarationList.contains(t.kind))
			{
				astList.add(declaration());
				match(SEMI);
			}
			else if(t.kind == IDENTIFIER)
			{	astList.add(statement());
				match(SEMI);
			}
		}
		return new Program(firstToken, firstToken, astList);
	}

	public Expression expression() throws SyntaxException
	{
		Token firstToken = t;
		Expression e2 = null;
		Expression e3 = null;
		Expression e1 = orExpression();
		if(t.kind == OP_Q)
		{
			consume();
			e2 =expression();
			match(OP_COLON);
			e3 = expression();
			return new Expression_Conditional(firstToken, e1, e2, e3);
		}
		return e1;
	}
	
	Statement_Assign assignmentStatement() throws SyntaxException
	{
		Token firstToken = t;
		LHS l = lhs();
		match(OP_ASSIGN);
		Expression e = expression();	
		return new Statement_Assign(firstToken, l, e);
	}
	
	Statement_In imageInStatement() throws SyntaxException
	{
		Token firstToken = t;
		match(IDENTIFIER);
		match(OP_LARROW);
		Source s = source();
		return new Statement_In(firstToken, firstToken, s);
	}
	
	Sink sink() throws SyntaxException
	{
		Token firstToken = t;
		if(t.kind ==IDENTIFIER){
			consume();
			return new Sink_Ident(firstToken, firstToken);
			}
		else if(t.kind == KW_SCREEN) {
			consume();
			return new Sink_SCREEN(firstToken);
			}
		 else {
			throw new SyntaxException(t, "Error at Sink on line" +t.line);
		}
	}
	
	Statement_Out imageOutStatement() throws SyntaxException
	{
		Token firstToken = t;
		match(IDENTIFIER);
		match(OP_RARROW);
		Sink s = sink();
		return new Statement_Out(firstToken, firstToken, s);
	}
	
	Statement statement() throws SyntaxException
	{
		Token firstToken = t;
		match(IDENTIFIER);
		if(t.kind == OP_LARROW)
		{
			consume();
			Source s = source();
			return new Statement_In(firstToken, firstToken, s);
		} else if(t.kind == OP_RARROW) {
			consume();
			Sink s = sink();
			return new Statement_Out(firstToken, firstToken, s);
		} else if(t.kind == LSQUARE) {
			consume();
			Index index = lhsSelector();
			match(RSQUARE);
			match(OP_ASSIGN);
			Expression e = expression();
			return new Statement_Assign(firstToken, new LHS(firstToken, firstToken,index), e);
		} else if(t.kind == OP_ASSIGN) {
			consume();
			Expression e = expression();	
			return new Statement_Assign(firstToken, new LHS(firstToken, firstToken, null), e);
		} else {
			throw new SyntaxException(t, "Error at statement on line" + t.line);
		}
	}
	
	Declaration_Image imageDeclaration() throws SyntaxException
	{
		Token firstToken = t;
		match(KW_image);
		Expression e = null;
		Expression e1 = null;
		Source s = null;
		if(t.kind == LSQUARE) {
			consume();
			e = expression();
			match(COMMA);
			e1 = expression();
			match(RSQUARE);
		}
		Token tokenName = t;
		match(IDENTIFIER); 

		if(t.kind == OP_LARROW) {
			consume();
			s = source();
		} 
		return new Declaration_Image(firstToken, e, e1,tokenName, s);
		
	}
	
	void sourceSinkType() throws SyntaxException
	{
		if(t.kind == KW_url || t.kind == KW_file) {
			consume();
		} else {
			throw new SyntaxException(t, "Error at Source Sink type on line" +t.line);
		}
	}
	
	Source source() throws SyntaxException
	{
		Token firstToken = t;
		if(t.kind == STRING_LITERAL) {
			String str = t.getText();
			consume();
			return new Source_StringLiteral(firstToken, str);
		} else if(t.kind == OP_AT) {
			consume();
			Expression e = expression();
			return new Source_CommandLineParam(firstToken, e);
		} else if(t.kind == IDENTIFIER) {
			consume();
			return new Source_Ident(firstToken, firstToken);
		} else {
			throw new SyntaxException(t, "Error at Source on line" +t.line);
		}
	}
	
	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException
	{
		Token firstToken = t;
		sourceSinkType();
		Token ident = t;
		match(IDENTIFIER);
		match(OP_ASSIGN);
		Source s = source();	
		return new Declaration_SourceSink(firstToken, firstToken, ident, s);
	}
	
	void varType() throws SyntaxException
	{
		if(t.kind == KW_int || t.kind == KW_boolean) {
			consume();
		} else {
			throw new SyntaxException(t, "Error at Var Type on line" +t.line);
		}
	}
	
	Declaration_Variable variableDeclaration() throws SyntaxException
	{
		Token firstToken = t;
		varType();
		Token ident = t;
		Expression e = null;
		match(IDENTIFIER);
		if(t.kind==OP_ASSIGN) {
			consume();
			e = expression();
		} 
		return new Declaration_Variable(firstToken, firstToken, ident, e);
	}
	
	Declaration declaration() throws SyntaxException
	{
		Declaration d = null;
		if(t.kind == KW_int || t.kind == KW_boolean) {
			 d = variableDeclaration();
		} else if(t.kind == KW_image) {
			d = imageDeclaration();
		} else if (t.kind == KW_url || t.kind == KW_file) {
			 d = sourceSinkDeclaration();
		} else {
			throw new SyntaxException(t, "Error at Declaration on line" +t.line);
		}
		return d; 
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException 
	{
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}