/* 		OO PARSER AND BYTE-CODE GENERATOR FOR TINY PL

Grammar for TinyPL (using EBNF notation) is as follows:

 program ->  decls stmts end
 decls   ->  int idlist ;
 idlist  ->  id { , id } 
 stmts   ->  stmt [ stmts ]
 cmpdstmt->  '{' stmts '}'
 stmt    ->  assign | cond | loop
 assign  ->  id = expr ;
 cond    ->  if '(' rexp ')' cmpdstmt [ else cmpdstmt ]
 loop    ->  while '(' rexp ')' cmpdstmt  
 rexp    ->  expr (< | > | =) expr
 expr    ->  term   [ (+ | -) expr ]
 term    ->  factor [ (* | /) term ]
 factor  ->  int_lit | id | '(' expr ')'

Lexical:   id is a single character; 
	      int_lit is an unsigned integer;
		 equality operator is =, not ==

Sample Program: Factorial

int n, i, f;
n = 4;
i = 1;
f = 1;
while (i < n) {
  i = i + 1;
  f= f * i;
}
end

   Sample Program:  GCD

int x, y;
x = 121;
y = 132;
while (x != y) {
  if (x > y) 
       { x = x - y; }
  else { y = y - x; }
}
end


Sample Bytecode

int n, i, f;
n = 4;
i = 1;
f = 1;
while (i < n) {
	i = i + 1;
	f= f * i;
}
end

Java Byte Codes are:

0: iconst_4
1: istore_0
2: iconst_1
3: istore_1
4: iconst_1
5: istore_2
6: iload_1
7: iload_0
8: if_icmpge 22
11: iload_1
12: iconst_1
13: iadd
14: istore_1
15: iload_2
16: iload_1
17: imul
18: istore_2
19: goto 6
22: return
 */

public class Parser {
	static Program p;
	static Code c;
	public static void main(String[] args)  {
		System.out.println("Enter program and terminate with 'end'!\n");
		Lexer.lex();
		p = new Program();
		c = new Code();
		c.output();
	}
}

class Program {
	Decls d;
	Stmts s;
	Code c;
	Program() {
		d = new Decls();
		s = new Stmts();
		c = new Code();
		if (Lexer.nextToken == Token.KEY_END) {
			c.bytecode[c.step] = c.step + ": return";
			c.step++;
			return;
		}
	}
}

class Decls {
	Idlist id;
	Decls() {
		if (Lexer.nextToken == Token.KEY_INT) {
			Lexer.lex();
			id = new Idlist();
		}
		if (Lexer.nextToken == Token.SEMICOLON) {
			Lexer.lex();
		}
	}
}

class Idlist {
	Code c;
	Idlist() {
		c = new Code();
		while(Lexer.nextToken != Token.SEMICOLON) {
			if(Lexer.nextToken == Token.ID) {
				c.insertid(Lexer.ident);
				Lexer.lex();
			}
			if(Lexer.nextToken == Token.COMMA) {
				Lexer.lex();
			}
		}
		if (Lexer.nextToken == Token.SEMICOLON) {
			return;
		}
	}
}

class Stmt {
	Assign a;
	Loop l;
	Cond c;
	Stmt() {
		if (Lexer.nextToken == Token.ID) {
			a = new Assign();
		} else if (Lexer.nextToken == Token.KEY_WHILE){
			l = new Loop();
		} else if ((Lexer.nextToken == Token.KEY_IF)) {
			c = new Cond();
		}
	}
} 

class Stmts {
	Stmts st;
	Stmt s;
	Stmts() {
		s = new Stmt();
		if ((Lexer.nextToken != Token.KEY_END) && (Lexer.nextToken != Token.RIGHT_BRACE)) {
			st = new Stmts();
		}
	}
}

class Assign {
	char id;
	Expr e;
	Code c;
	Assign(){
		c = new Code();
		if (Lexer.nextToken == Token.ID) {
			id = Lexer.ident;
			Lexer.lex();
		}
		if (Lexer.nextToken == Token.ASSIGN_OP) {
			Lexer.lex();
		}
		e = new Expr();
		if (Lexer.nextToken == Token.SEMICOLON) {
			c.genbyte_assign(id);
			Lexer.lex();
		}
	}
}

class Cond {
	Rexp r;
	Cmpdstmt c1;
	Cmpdstmt c2;
	Code c;
	int if_step = Code.step;
	int else_step = Code.step;
	int goto_step = Code.step;
	int z = Code.step;
	static int k = 0;
	Cond() {
		c = new Code();
		switch (Lexer.nextToken){
		case Token.KEY_IF:
			Lexer.lex();
			if(Lexer.nextToken == Token.LEFT_PAREN){
				Lexer.lex();
				r = new Rexp();
				else_step = Code.step;
				Code.step += 3;
				Lexer.lex();
				c2 = new Cmpdstmt();
				if_step = Code.step;
				if(Lexer.nextToken == Token.KEY_ELSE) {
					Lexer.lex();
					Code.bytecode[if_step] = if_step+": goto";
					goto_step = if_step;
					if_step = Code.step + 3;
					Code.step = Code.step + 3;
					c1 = new Cmpdstmt();
					Code.bytecode[goto_step] += " " + Code.step;
				}
				Code.bytecode[else_step] += " " + if_step;
				
			}
			break;
		default:
			break;
		} 
	}
}

class Loop {
	Rexp r;
	Cmpdstmt c2;
	Loop() {
		int y=Code.step;
		int s_loop = 0;
		int s = Code.step;
		if (Lexer.nextToken == Token.KEY_WHILE) {
			Lexer.lex();
			Code.i = Code.i + 1;
			if(Lexer.nextToken == Token.LEFT_PAREN){ 
				Lexer.lex();
				r = new Rexp();
				s_loop = Code.step;
				Code.step += 3;
				Lexer.lex(); 
				c2 = new Cmpdstmt();
				s = Code.step;
				Code.bytecode[s] = s+": goto " + y;
				Code.step = Code.step + 3;
				s = s+3;
			} 
		}
		Code.bytecode[s_loop] += " " + s;
		Code.i = Code.i - 1;
	}
}

class Cmpdstmt {
	Stmts st;
	Cmpdstmt() {
		if (Lexer.nextToken == Token.LEFT_BRACE) {
			Lexer.lex();
			st = new Stmts();
			Lexer.lex();
		}
	}
}

class Rexp {
	Expr e;
	Expr e1;
	Code c;
	Rexp() {
		e = new Expr();
		int op = Lexer.nextToken;
		if ((Lexer.nextToken == Token.GREATER_OP) || (Lexer.nextToken == Token.LESSER_OP) || (Lexer.nextToken == Token.ASSIGN_OP) || (Lexer.nextToken == Token.NOT_EQ)) {
			Lexer.lex();
			e1 = new Expr();
			c.genbyte_rexp(op);
		}
	}
}

class Expr {
	Term t;
	Expr e;
	Code c;
	Expr() {
		t = new Term();
		if (Lexer.nextToken == Token.ADD_OP || 
				Lexer.nextToken == Token.SUB_OP) {
			int op = Lexer.nextToken;
			Lexer.lex();
			e = new Expr();
			c.genbyte_addsub(op);
		}
	}
}

class Term {
	Term t1;
	Factor f;
	Code c;
	Term() {
		f = new Factor();
		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
			int op = Lexer.nextToken;
			Lexer.lex();
			t1 = new Term();
			c.genbyte_muldiv(op);
		}
	}
}

class Factor {
	Expr e;
	Code c;
	Factor() {
		int i;
		switch (Lexer.nextToken) {
		case Token.INT_LIT: // number
			i = Lexer.intValue;
			c.genbyte_factor(Lexer.nextToken);
			Lexer.lex();
			break;
		case Token.LEFT_PAREN: // '('
			Lexer.lex();
			e = new Expr();
			Lexer.lex(); // skip over ')'
			break;
		case Token.ID:
			c.genbyte_loader(Lexer.ident);
			Lexer.lex();
			break;
		default:
			break;
		}
	}
}

class Code {
	static String bytecode[] = new String[100];
	static int step = 0;
	static int n = 0;
	static char[] idlist = new char[10];
	static int i = 0;

	public static void insertid(char ch) {
		idlist[n++] = ch;
	}

	public static void output() {
		for(int i=0;i<100;i++) {
			if (bytecode[i] != null)
				System.out.println(bytecode[i]);
		}
	}

	public static void genbyte_factor(int type) {
		String code = "";
		switch(type) {
		case Token.INT_LIT:
			int lit = Lexer.intValue;
			if (lit >= -128 && lit <= 127) {
				if (lit == -1) {
					code = step+": "+"const_m1";
					bytecode[step] = code;
					step++;
				} else if (lit >= 0 && lit <= 5) {
					code = step+": "+"iconst_"+lit;
					bytecode[step] = code;
					step++;
				} else {
					code = step+": "+"bipush "+lit;
					bytecode[step] = code;
					step+=2;
				}
			} else if (lit < -128 || lit > 127) {
				code = step+": "+"sipush "+lit;
				bytecode[step] = code;
				step+=3;
			}
			break;
		case Token.ID:
			int index = new String(idlist).indexOf(Lexer.ch);
			code = step+":"+"iload_"+index;
			step++;
			break;
		default:
			break;
		}
	}

	public static void genbyte_assign(char id) {
		String code = "";
		int index = new String(idlist).indexOf(id);
		code = step+": "+"istore_"+index;
		bytecode[step] = code;
		step++;
	}

	public static void genbyte_loader(char id) {
		String code = "";
		int index = new String(idlist).indexOf(id);
		code = step+": "+"iload_"+index;
		bytecode[step] = code;
		step++;
	}

	public static void genbyte_addsub(int op) {
		String code = "";
		if (op == Token.ADD_OP)
			code = step+": "+"iadd";
		else
			code = step+": "+"isub";
		bytecode[step] = code;
		step++;
	}

	public static void genbyte_muldiv(int op) {
		String code = "";
		if (op == Token.MULT_OP)
			code = step+": "+"imul";
		else
			code = step+": "+"idiv";
		bytecode[step] = code;
		step++;
	}

	public static void genbyte_rexp(int op) {
		String code = "";
		if (op == Token.LESSER_OP)
			code = step+": "+"if_icmpge";
		else if (op == Token.GREATER_OP)
			code = step+": "+"if_icmple";
		else if (op == Token.ASSIGN_OP)
			code = step+": "+"if_icmpne";
		else if (op == Token.NOT_EQ)
			code = step+": "+"if_icmpeq";
		bytecode[step] = code;
	}
}


