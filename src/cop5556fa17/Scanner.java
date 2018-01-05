/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
  */

package cop5556fa17;


import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;






public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception
	{
		
		int pos;

		public LexicalException(String message, int pos) 
		{
			super(message);
			this.pos = pos;
		}
		
		public int getPos() 
		{ 
			return pos;
		}

	}
	public static enum State //Enum for the states 
	{
		START, AFTER_EQ, IN_DIGIT, IN_IDENT, AFTER_OR, AFTER_NOT, AFTER_LESS, 
		AFTER_GREAT, AFTER_SLASH, AFTER_MINUS, AFTER_STAR, AFTER_MUL, AFTER_COMMENT, AFTER_DIV, IN_COMMENT, AFTER_CR, IN_STRING, IN_ESCAPE_SEQ;
	}
	
	public static enum Kind 
	{
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	
	

	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() 
		{
			if (kind == Kind.STRING_LITERAL) 
			{
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) 
			{// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  
	
static HashMap<String, Kind> keywords = new HashMap<String, Kind>(); //HashMap for the reserved words
	
	static {
	
		keywords.put("x", Kind.KW_x);
		keywords.put("y", Kind.KW_y);
		keywords.put("X", Kind.KW_X);
		keywords.put("Y", Kind.KW_Y);
		keywords.put("r", Kind.KW_r);
		keywords.put("R", Kind.KW_R);
		keywords.put("a", Kind.KW_a);
		keywords.put("A", Kind.KW_A);
		keywords.put("Z", Kind.KW_Z);
		keywords.put("DEF_X", Kind.KW_DEF_X);
		keywords.put("DEF_Y", Kind.KW_DEF_Y);
		keywords.put("SCREEN", Kind.KW_SCREEN);
		keywords.put("cart_x", Kind.KW_cart_x);
		keywords.put("cart_y", Kind.KW_cart_y);
		keywords.put("polar_a", Kind.KW_polar_a);
		keywords.put("polar_r", Kind.KW_polar_r);
		keywords.put("abs", Kind.KW_abs);
		keywords.put("sin", Kind.KW_sin);
		keywords.put("cos", Kind.KW_cos);
		keywords.put("atan", Kind.KW_atan);
		keywords.put("log", Kind.KW_log);
		keywords.put("image", Kind.KW_image);
		keywords.put("int", Kind.KW_int);
		keywords.put("boolean", Kind.KW_boolean);
		keywords.put("url", Kind.KW_url);
		keywords.put("file", Kind.KW_file);
		keywords.put("true", Kind.BOOLEAN_LITERAL);
		keywords.put("false", Kind.BOOLEAN_LITERAL);
		
	}
	
static HashSet<Character> escape = new HashSet<Character>(); //Hashset for escape characters
	static	
	{
	escape.add('b');
	escape.add('t');
	escape.add('n');
	escape.add('f');
	escape.add('r');
	escape.add('\"');
	escape.add('\'');
	escape.add('\\');
	}



	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
	}


	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException 
	{
		/* TODO  Replace this with a correct and complete implementation!!! */
		int pos = 0;
		State state = State.START;
		int startingPos =0;
		int line = 1;
		int posInLine = 1;
	while(pos<chars.length)
		{

			char ch = chars[pos];
			
			if(ch>=128 || ch<0) //Check for the Unicode of the characters in the input
			{
				throw new LexicalException("Illegal Unicode for character encountered at line:"+line+"at position: "+pos, pos);
                
			}
			switch(state)
			{
				case START : 
					ch = (char) (pos < chars.length? chars[pos]:-1);
					startingPos = pos;
					switch(ch)
					{
						case EOFchar:		//When it reaches EOF
						{
							
							
							tokens.add(new Token(Kind.EOF, pos, 0, line,posInLine));
							return this; 
							//pos++;
							//posInLine++;
					
						}
						
						
						
						case '+': 		//No state after +	
						{
							tokens.add(new Token(Kind.OP_PLUS,startingPos,1,line, posInLine)); 
							pos++;
							posInLine++;
						}
						break;
						
						case '*': 		//Check for other states after *
						{
							state = State.AFTER_MUL;
							pos++;
		
						
						}
						
						break;
						
						case '=': //Check for other states after =
						{
							state = State.AFTER_EQ;
							pos++;

						}
						break;
						
						case '/': //Check for other states after /
						{
							state = State.AFTER_DIV;
							pos++;
							
							
						}
						break;
						
						case '&': //Check for other states after &
						{
							tokens.add(new Token(Kind.OP_AND, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case ';':
						{
							tokens.add(new Token(Kind.SEMI, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case ',':
						{
							tokens.add(new Token(Kind.COMMA, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case '(':
						{
							tokens.add(new Token(Kind.LPAREN, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case ')':
						{
							tokens.add(new Token(Kind.RPAREN, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case '[':
						{
							tokens.add(new Token(Kind.LSQUARE, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case ']':
						{
							tokens.add(new Token(Kind.RSQUARE, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case '%':
						{
							tokens.add(new Token(Kind.OP_MOD, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case '>':
						{
							state = State.AFTER_GREAT;
							pos++;
						}
						break;
						
						case '<':
						{
							state = State.AFTER_LESS;
							pos++;
						}
						break;
						
						case '!':
						{
							state = State.AFTER_NOT;
							pos++;
							
						}
						break;
						
						case '?':
						{
							tokens.add(new Token(Kind.OP_Q, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case ':' :
						{
							tokens.add(new Token(Kind.OP_COLON, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case '|':
						{
							tokens.add(new Token(Kind.OP_OR, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						case '-':
						{
							state = State.AFTER_MINUS;
							pos++;
							
						}
						break;
						
						case '@':
						{
							tokens.add(new Token(Kind.OP_AT, startingPos, 1, line, posInLine));
							pos++;
							posInLine++;
						}
						break;
						
						
						case '0':		//Check specifically if the character is 0 only.
	            			{
	            				tokens.add(new Token(Kind.INTEGER_LITERAL,startingPos, 1, line, posInLine));
	            				pos++;
	            				posInLine++;
							}
	            			break;
	            			
						case '"':	//To check for validity of String literals
						{
							
							state = State.IN_STRING;
							pos++;
							posInLine++;
					
						}
						break;
						
						case '\n':		//Checking for newline
						{
							line++;
		    				posInLine=1;
		            		pos++;
						}
						break;
						
						case '\r':			//If \r is encountered we check if \n is also there and if there only one line is incremented.
						{
							state = State.AFTER_CR;
		            		pos++;
						}
						break;
						
	
						default:
	            			if (Character.isDigit(ch))
	            			{
	            				state = State.IN_DIGIT;
	            				pos++;
	            			} 
	                        else if (Character.isJavaIdentifierStart(ch))
	                        {
	                             state = State.IN_IDENT;
	                             pos++;
	                        } 
	                        else if((Character.isWhitespace(ch)))	//For tab characters
	                        {
	                        	
	                        	pos++; 
	                        	posInLine++;
	                        }
	                       
	                        else
	                        {
	                    		throw new LexicalException("Illegal character encountered at line:"+line+"at position: "+pos, pos);
	                        }
	            			
	            			break;
	                       
	        		}
					
	        	break;
	        	
				case IN_IDENT:
	                if ((ch>=65 && ch<=90) || (ch>=97 && ch<=122)||(ch>=48 && ch<=57)||(ch=='$')||(ch=='_') && ch!=EOFchar) 
	                {
	                	pos++;
	                	
	                } 
	                else 
	                {
	                	
	                	String ident =  new String (chars, startingPos, pos - startingPos);
	                	
	                	if(keywords.containsKey(ident))
	                		
	                		tokens.add(new Token(keywords.get(ident), startingPos, pos - startingPos, line, posInLine));
	                	else
	                		tokens.add(new Token(Kind.IDENTIFIER, startingPos, pos - startingPos, line, posInLine));
	                	
	                    	posInLine= posInLine + pos - startingPos;
	                    	state = State.START;
	                }
	                break;
	                
				case IN_DIGIT:   
	                if (Character.isDigit(ch) && ch!=EOFchar) 
	                {
	                	pos++;
	                } 
	                else 
	                {
	                	
	                	Token int_lit_token = new Token(Kind.INTEGER_LITERAL, startingPos, pos - startingPos, line, posInLine);
	                	 posInLine = posInLine+pos-startingPos;
	                	try 
	                	{
	                		
	                		int int_val = int_lit_token.intVal();

	                		
	                	}
	                	catch(NumberFormatException e) 
	                	{
	                		
	                		throw new LexicalException(int_lit_token.getText(),pos);
	                		
	                	}
	                	
                		tokens.add(int_lit_token);
                		state = State.START;

	                }
	                break;
	                
				 case AFTER_EQ:

		            	if(ch == '=') 
		            	{
		            		tokens.add(new Token(Kind.OP_EQ, startingPos, 2, line, posInLine));
		            		state = State.START;
		            		pos++;
		            		posInLine=posInLine+2;
		            	}

		            	else
		            	{
		            		tokens.add(new Token(Kind.OP_ASSIGN, startingPos, 1, line, posInLine));
		            		state = State.START;
		                    posInLine++;
		            	}
		            	
		            	break;
		            	
				  case AFTER_LESS:
					  	
		                if (ch == '=') 
		                {
		                    tokens.add(new Token(Kind.OP_LE, startingPos, 2, line, posInLine));
		                    state = State.START;
		                	pos++;
		                	posInLine=posInLine+2;
		                } 
		                else if (ch == '-') 
		                {
		                    tokens.add(new Token(Kind.OP_LARROW, startingPos, 2, line, posInLine));
		                    state = State.START;
		                	pos++;
		                	posInLine=posInLine+2;
		                }
		                else 
		                {	           
		                	tokens.add(new Token(Kind.OP_LT, startingPos, 1, line, posInLine));
		                    state = State.START;
		                    posInLine++;
		                }
		                break;
		                
				  case AFTER_GREAT:
					  if (ch == '=') 
					  {
		                    tokens.add(new Token(Kind.OP_GE, startingPos, 2, line, posInLine));
		                    state = State.START;
		                	pos++;
		                	posInLine=posInLine+2;
		                }
		                else 
		                {	             
		                	tokens.add(new Token(Kind.OP_GT, startingPos, 1, line, posInLine));
		                	state = State.START;
		                    posInLine++;
		                }
		                break;
		                
				  case AFTER_NOT:  
					  
					  if(ch == '=') 
		            	{
		            		tokens.add(new Token(Kind.OP_NEQ, startingPos, 2, line, posInLine));
		            		state = State.START;
		            		pos++;
		            		posInLine=posInLine+2;
		            	}
		            	
		            	
		            	else
		            	{
		            		tokens.add(new Token(Kind.OP_EXCL, startingPos, 1, line, posInLine));
		            		state = State.START;
		                    posInLine++;
		            	}
		            	
		            	break;
		            	
				  case AFTER_MINUS: 
		                if (ch == '>') 
		                {
		                    tokens.add(new Token(Kind.OP_RARROW, startingPos, 2, line, posInLine));
		                    state = State.START;
		                	pos++;
		                	posInLine=posInLine+2;
		                }
		                else
		                {
		                	tokens.add(new Token(Kind.OP_MINUS, startingPos, 1, line, posInLine));
		                    state = State.START;
		                    posInLine++;
		                }
		                break;	
		                
				  case AFTER_MUL:
					  if(ch == '*') 
		            	{
		            		tokens.add(new Token(Kind.OP_POWER, startingPos, 2, line, posInLine));
		            		state = State.START;
		            		pos++;
		            		posInLine=posInLine+2;
		            	}
		            	
		            	
		            	else
		            	{
		            		tokens.add(new Token(Kind.OP_TIMES, startingPos, 1, line, posInLine));
		            		state = State.START;
		                    posInLine++;
		            	}
		            	
		            	break;
		            	
		            case AFTER_DIV:
							  if(ch == '/') 
				            	{
								  state = State.IN_COMMENT;
				                	pos++;
				                	posInLine++;
				            	}
				            else
				            	{
				            		tokens.add(new Token(Kind.OP_DIV, startingPos, 1, line, posInLine));
				            		state = State.START;
				            	
				                    posInLine++;
				            	}
				            	
				            	break;
				            	
		            case IN_COMMENT: 
		            	
		            	
		            	 if(ch == '\n') 
		    			{
		    				state = State.START;
		            		line++;
		    				posInLine=1;
		            		pos++;
		    			}
		            	
		            	else if (ch =='\r')
		            	{
		            		state = State.AFTER_CR;
		            		pos++;
		            	}
		            	 

		            	else if(ch == EOFchar) 
			            	{
			            		tokens.add(new Token(Kind.EOF, startingPos, 0, line, posInLine));
			            		pos++;
			            		posInLine++;
			            	}
			            	
		            	
						else
								pos++;
		            	 
		            	 
						
				
		            	break;
		            	
		            case AFTER_CR:
		            	
		            	if(ch == '\n')
		            	{
		            		state = State.START;
		            		line++;
		    				posInLine=1;
		            		pos++;
		            	}
		            	else
		            	{
		            		
		            		line++;
		    				posInLine=1;
		            		state = State.START;
		            		
						}
		            	break;
		            	
		            case IN_STRING:
		            	
		            	if(ch == '"')
		            	{
		            		pos++;
		            		posInLine++;
		            		state=State.START;
		            		tokens.add(new Token(Kind.STRING_LITERAL, startingPos, pos-startingPos, line, posInLine - (pos-startingPos)));
                        	
		            	}
		            	
		            	else if(ch=='\n')
		            	{
		            		throw new LexicalException("The String literal encountered a new line at line:"+line+" at position: "+pos,pos);
		            		
		            	}
		            	else if(ch=='\r')
		            	{
		            		throw new LexicalException("The String literal encountered a new line at line:"+line+" at position: "+posInLine,pos);
		            		
		            	}
		            	else if(ch=='\\')
		            	{
		            		pos++;
		            		posInLine++;
		            		state = State.IN_ESCAPE_SEQ;
		            	}
		            	else if(ch==EOFchar)
		            	{
		            			throw new LexicalException("String literal unclosed at line:"+line+" at position: "+pos,pos);
		            	}
		            	else
		            	{
		            		pos++;
		            		posInLine++;
		            	}
		            	break;
		            	
		            case IN_ESCAPE_SEQ:
		            {
		            	
		            	if(escape.contains(ch))
		            	{
		            		pos++;
		            		posInLine++;
		            		state= State.IN_STRING;
		            		
		            	}
		            	else
		            	{
		            		throw new LexicalException("Illegal Escape sequence character encountered at line:"+line+" at position: "+pos, pos);
		            	}
		            }
		            break;
		            	
		            default:  assert false;     			
		          
			}
		}
	
		
		return this; 

		}

						
						
						
						
					
					
			
		
		
	



	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
