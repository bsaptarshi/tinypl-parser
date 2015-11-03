# tinypl-parser
This is a recursive descent parser to generate JVM bytecode for a minimally defined programming language grammar called TinyPL using Java.

Grammar for TinyPL (using EBNF notation) is as follows:

 program ->  decls stmts end <br />
 decls   ->  int idlist ;<br />
 idlist  ->  id { , id } <br />
 stmts   ->  stmt [ stmts ]<br />
 cmpdstmt->  '{' stmts '}'<br />
 stmt    ->  assign | cond | loop<br />
 assign  ->  id = expr ;<br />
 cond    ->  if '(' rexp ')' cmpdstmt [ else cmpdstmt ]<br />
 loop    ->  while '(' rexp ')' cmpdstmt  <br />
 rexp    ->  expr (< | > | =) expr<br />
 expr    ->  term   [ (+ | -) expr ]<br />
 term    ->  factor [ (* | /) term ]<br />
 factor  ->  int_lit | id | '(' expr ')'<br />

Lexical:   id is a single character; <br />
          int_lit is an unsigned integer;<br />
         equality operator is =, not ==
