package cop5556fa17;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import java.net.*;
import cop5556fa17.SymbolTable;

public class TypeCheckVisitor implements ASTVisitor {
	
	SymbolTable sym = new SymbolTable();

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		if(declaration_Variable.e!=null)
			declaration_Variable.e.visit(this, null);
	if(sym.lookup(declaration_Variable.name) !=null)
	{
		throw new SemanticException(declaration_Variable.firstToken,"Declaration variable is not null");
	}
		sym.insert(declaration_Variable.name, declaration_Variable);
		declaration_Variable.Type = TypeUtils.getType(declaration_Variable.type);
	
	if(declaration_Variable.e!=null)
	{
		if(declaration_Variable.Type == declaration_Variable.e.Type){
			
		}
		else
			throw new SemanticException(declaration_Variable.firstToken,"Declaration variable is incorrect as the expression type and the declaration type don't match.");
	}
	return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		if(expression_Binary.e0!=null)
		expression_Binary.e0.visit(this, null);
		if(expression_Binary.e1!=null)
		expression_Binary.e1.visit(this, null);
		
	
	
		if(expression_Binary.op == Kind.OP_EQ || expression_Binary.op == Kind.OP_NEQ){
			expression_Binary.Type = Type.BOOLEAN;
		}
		else if((expression_Binary.op == Kind.OP_GE || expression_Binary.op == Kind.OP_GT||expression_Binary.op == Kind.OP_LT 
				|| expression_Binary.op == Kind.OP_LE) && 
				expression_Binary.e0.Type == Type.INTEGER){
			expression_Binary.Type = Type.BOOLEAN;
		}
		else if((expression_Binary.op == Kind.OP_AND || expression_Binary.op == Kind.OP_OR) && 
				(expression_Binary.e0.Type == Type.INTEGER || expression_Binary.e0.Type == Type.BOOLEAN)){
			expression_Binary.Type = expression_Binary.e0.Type;
		}
		else if((expression_Binary.op == Kind.OP_DIV || expression_Binary.op == Kind.OP_MINUS|| expression_Binary.op == Kind.OP_MOD||
				expression_Binary.op == Kind.OP_PLUS || expression_Binary.op == Kind.OP_POWER || expression_Binary.op == Kind.OP_TIMES)
				&& expression_Binary.e0.Type == Type.INTEGER){
			expression_Binary.Type = Type.INTEGER;
		}
		else{
			expression_Binary.Type = null;
		}
		
	if(expression_Binary.e0.Type == expression_Binary.e1.Type && expression_Binary.Type != null){
			
		}
		else{
			throw new SemanticException(expression_Binary.firstToken, "Binary expression is incorrect as the types aren't matching.");
		}
		
	
		return null;
		
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		if(expression_Unary.e!=null)
		{
			expression_Unary.e.visit(this, null);
		}
		
		if(expression_Unary.op==Kind.OP_EXCL && (expression_Unary.e.Type==Type.BOOLEAN || expression_Unary.e.Type == Type.INTEGER))
				expression_Unary.Type = expression_Unary.e.Type;
		else if((expression_Unary.op==Kind.OP_PLUS || expression_Unary.op==Kind.OP_MINUS) && (expression_Unary.e.Type==Type.INTEGER))
			expression_Unary.Type= Type.INTEGER;
		else
			expression_Unary.Type= null;
		if(expression_Unary.Type==null)
			return new SemanticException(expression_Unary.firstToken,"Expression unary should not be null");
		
		return null;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		if(index.e0!=null){
			index.e0.visit(this, null);
		}
		if(index.e1!=null){
			index.e1.visit(this, null);
		}
	if(index.e0.Type == Type.INTEGER && index.e1.Type == Type.INTEGER){
//			if(index.e0.getClass() == Expression_PredefinedName.class && index.e1.getClass() == Expression_PredefinedName.class){
//				Expression_PredefinedName ep1 = (Expression_PredefinedName)index.e0;
//				Expression_PredefinedName ep2 = (Expression_PredefinedName)index.e1;
//				index.setCartesian(!(ep1.kind == Kind.KW_r && ep2.kind == Kind.KW_a));
//			} //Not needed now
			index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_a));
			
		}
		else
			throw new SemanticException(index.firstToken,"E0 and E1 not of Integer type");
		
		return null;

	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		Type typename;
		if(expression_PixelSelector.index!=null)
			expression_PixelSelector.index.visit(this, null);
		Declaration dec = sym.lookup(expression_PixelSelector.name);
		if(dec==null)
			throw new SemanticException(expression_PixelSelector.firstToken,"Pixel selector is null and can't find "+expression_PixelSelector.toString());
		typename = dec.Type;
		if(typename==Type.IMAGE)
			expression_PixelSelector.Type= Type.INTEGER;
		else if(expression_PixelSelector.index==null)
			expression_PixelSelector.Type= typename;
		else
			expression_PixelSelector.Type= null;
		if(expression_PixelSelector.Type==null)
			throw new SemanticException(expression_PixelSelector.firstToken,"Expression Pixel selector should not be null");
		return null;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		if(expression_Conditional.condition!=null)
		{
			expression_Conditional.condition.visit(this, null);
		}
		if(expression_Conditional.falseExpression!=null){
			expression_Conditional.falseExpression.visit(this, null);
		}
		if(expression_Conditional.trueExpression!=null){
			expression_Conditional.trueExpression.visit(this, null);
		}
		if(expression_Conditional.condition.Type == Type.BOOLEAN && expression_Conditional.trueExpression.Type == expression_Conditional.falseExpression.Type){
			
		}
		else
		throw new SemanticException(expression_Conditional.firstToken, "Expression conditional isn't boolean or not equal to false");
		expression_Conditional.Type = expression_Conditional.trueExpression.Type;
		return null;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		if(declaration_Image.source!=null)
			declaration_Image.source.visit(this, null);
		if(declaration_Image.xSize!=null)
			declaration_Image.xSize.visit(this,null);
		if(declaration_Image.ySize!=null)
			declaration_Image.ySize.visit(this,null);
		if(sym.lookup(declaration_Image.name)!=null)
			throw new SemanticException(declaration_Image.firstToken,"Declaration Image already exists");
		sym.insert(declaration_Image.name, declaration_Image);
		declaration_Image.Type = Type.IMAGE;
		if(declaration_Image.xSize!=null){
			if(declaration_Image.ySize!=null&& declaration_Image.xSize.Type==Type.INTEGER && declaration_Image.ySize.Type == Type.INTEGER){
			}
			else{
				throw new SemanticException(declaration_Image.firstToken, "Declaration Image's xsize and ysize is not the same.");
				
			}
		}				
		return null;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		boolean flag = true;
	
		try{
			URL url = new URL(source_StringLiteral.fileOrUrl);
			//url.toURI();
			
		}
		catch(Exception e){
			flag =false;
		}
		if(flag){
			source_StringLiteral.Type = Type.URL;
		}
		else{
			source_StringLiteral.Type = Type.FILE;
		}
		return null;
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		if(source_CommandLineParam.paramNum!=null)
			source_CommandLineParam.paramNum.visit(this, null);
//		source_CommandLineParam.Type = source_CommandLineParam.paramNum.Type;
			if(source_CommandLineParam.paramNum.Type!=Type.INTEGER)
		throw new SemanticException(source_CommandLineParam.firstToken,"Source command line parameter is not integer");
		source_CommandLineParam.Type = null;
	
	return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		Declaration dec;
		dec = sym.lookup(source_Ident.name);
		if(dec == null)
			throw new SemanticException(source_Ident.firstToken,"Source Ident is null and can't find the type"+source_Ident.toString()																																		);
		source_Ident.Type=dec.Type;
		if(source_Ident.Type==Type.FILE || source_Ident.Type==Type.URL)
		{
			
		}
		else
		throw new SemanticException(source_Ident.firstToken,"Source Ident is not of file or url type");
		return null;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		if(declaration_SourceSink.source!=null)
			declaration_SourceSink.source.visit(this, null);
		if(sym.lookup(declaration_SourceSink.name)!=null)
				throw new SemanticException(declaration_SourceSink.firstToken,"Declaration source sink is invalid");
		sym.insert(declaration_SourceSink.name, declaration_SourceSink);
			if(declaration_SourceSink.type == Kind.KW_file)
		declaration_SourceSink.Type = Type.FILE;
		else if(declaration_SourceSink.type == Kind.KW_url)
			declaration_SourceSink.Type = Type.URL;
		else
			throw new SemanticException(declaration_SourceSink.firstToken,"Source sink is not of FILE or URL type");
		
		if(declaration_SourceSink.source.Type == null || declaration_SourceSink.Type==declaration_SourceSink.source.Type ){
				
		}
		else
			throw new SemanticException(declaration_SourceSink.firstToken,"Source sink is not of type source or not null");
		
		return null;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		expression_IntLit.Type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		if(expression_FunctionAppWithExprArg.arg!=null)
			expression_FunctionAppWithExprArg.arg.visit(this, null);
	if(expression_FunctionAppWithExprArg.arg.Type!=Type.INTEGER)
		throw new SemanticException(expression_FunctionAppWithExprArg.firstToken,"Function with Expression Arg not an integer");
	expression_FunctionAppWithExprArg.Type = Type.INTEGER;
	return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		if(expression_FunctionAppWithIndexArg.arg!=null)
			expression_FunctionAppWithIndexArg.arg.visit(this, null);
		expression_FunctionAppWithIndexArg.Type=Type.INTEGER;
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		expression_PredefinedName.Type = Type.INTEGER;
		return null;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		
		if(statement_Out.sink!=null)
		statement_Out.sink.visit(this, null);
		
		Declaration dec= sym.lookup(statement_Out.name);
		if(dec==null)
			throw new SemanticException(statement_Out.firstToken,"Statement out is null and cannot find the type"+statement_Out.toString());
		statement_Out.setDec(dec);
		if(((dec.Type ==Type.INTEGER || dec.Type==Type.BOOLEAN) && statement_Out.sink.Type==Type.SCREEN) || (dec.Type==Type.IMAGE && (statement_Out.sink.Type ==Type.FILE || statement_Out.sink.Type == Type.SCREEN)))
		{
			
		}
		else
		throw new SemanticException(statement_Out.firstToken,"Visit statement out incorrect because it's not of type integer or boolean");
			
		return null;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		if(statement_In.source!=null)
			statement_In.source.visit(this, null);
		Declaration dec = sym.lookup(statement_In.name);
		if(dec==null)
			throw new SemanticException(statement_In.firstToken,"Statement out is null and cannot find the type "+statement_In.toString());
		statement_In.setDec(dec);
//		if(dec != null && dec.Type==statement_In.source.Type)
//		{
//			
//		}
//		else
//			throw new SemanticException(statement_In.firstToken,"Statement in source type is not of declaration type");
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		if(statement_Assign.e!=null)
			statement_Assign.e.visit(this, null);
		if(statement_Assign.lhs!=null)
			statement_Assign.lhs.visit(this, null);
		
		if((statement_Assign.lhs.Type == Type.IMAGE && statement_Assign.e.Type == Type.INTEGER) || (statement_Assign.lhs.Type == statement_Assign.e.Type)){
		}
		else
			throw new SemanticException(statement_Assign.firstToken, "Either LHS is not an image or expression type isn't an integer");	
		
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian);
		
		return null;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		if(lhs.index!=null)
			lhs.index.visit(this, null);
		Declaration d = sym.lookup(lhs.name);
		if(d==null)
			throw new SemanticException(lhs.firstToken,"Visit LHS is null and cannot find the type " +lhs.toString());
		lhs.Declaration = d;
		lhs.Type=lhs.Declaration.Type;
		if(lhs.index!=null)
		lhs.isCartesian = lhs.index.isCartesian();
		else
			lhs.isCartesian = false;
		return null;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
	sink_SCREEN.Type = Type.SCREEN;
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		Declaration dec;
		dec = sym.lookup(sink_Ident.name);
		if(dec==null)
			throw new SemanticException(sink_Ident.firstToken,"Statement out is null");
		sink_Ident.Type = dec.Type;
	
		if(sink_Ident.Type!=Type.FILE)
			throw new SemanticException(sink_Ident.firstToken,"Declaration is not of file type");
			return null;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.Type = Type.BOOLEAN;
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if(sym.lookup(expression_Ident.name) == null)
			throw new SemanticException(expression_Ident.firstToken,"Expression Ident is wrong and can't find the type "+expression_Ident.name);
		expression_Ident.Type = sym.lookup(expression_Ident.name).Type;
		return null;
		
	}

}
