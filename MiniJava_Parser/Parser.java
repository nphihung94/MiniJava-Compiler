import java.io.*;
import java.util.LinkedList;
import java.lang.*;

public class Parser {

  /* Class to define a token include an ID and name of the toke */
  public class Token{
    int token_ID; // ID of the token
    String name; // Name of the token

    /* Token ID table
          -1: EOF
          1: {
          2: }
          3: System.out.println
          4: (
          5: )
          6: ;
          7: if
          8: else
          9: while
          10: true
          11: false
          12: !
    */


    public Token(int token, String name) {
      this.token_ID = token;
      this.name = name;
    }

    public int get_token_ID(){
      return this.token_ID;
    }

    public String getSequence(){
      return this.name;
    }
  }

/* lookahead store the next token to parse */
private Token lookahead;

/* token_input_list store the list of input got from tokenize */
LinkedList<Token> token_input_list;

  /* recognizer to get list of token */
  public class recognizer {

    /* LinkedList to store all token got from the file */
    private LinkedList<Token> token_list;

    public LinkedList<Token> get_tokens() throws IOException{
      /* get input from System.in */
      BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

      /* open file to write for debug read */
      BufferedWriter outputWriter = new BufferedWriter( new FileWriter("debug_output.txt"));


      /* read until reach EOF or no byte left in file */
      int c;
      /* error_token = true if found any token not expected in grammar */
      boolean error_token = false;
      while ( ((c = bufferRead.read()) != -1) && (!error_token))
      {

      /* Code to get token */
      /* If input character is white space keep going */
      if (Character.isWhitespace(c)){
        continue;
      }
        else {
          /* Initialize Expected input for each token */
          String expected_token;
          switch(c) {
            case '{':
              token_list.add(new Token(1,"{"));
              break;
            case '}':
              token_list.add(new Token(2, "}"));
              break;
            case 'S':
              expected_token = "System.out.println";
              for (int i = 1; i < expected_token.length(); i++){
                if (!error_token){
                  /* If reach end of file before finish reading token or get a
                  character not suppose to be in this token. Make error_token = true */
                    if (((c = bufferRead.read()) == -1) || (c != expected_token.charAt(i))) {
                      error_token = true;
                      break;
                    }
                  }
              }
              /* finish for mean finish reading this token, add token to the list */
              token_list.add(new Token(3,expected_token));
              break;
            case '(':
              token_list.add(new Token(4, "("));
              break;
            case ')':
              token_list.add(new Token(5, ")"));
              break;
            case ';':
              token_list.add(new Token(6, ";"));
              break;
            case 'i':
              expected_token = "if";
              for (int i = 1; i < expected_token.length(); i++){
                if (!error_token){
                  /* If reach end of file before finish reading token or get a
                  character not suppose to be in this token. Make error_token = true */
                    if (((c = bufferRead.read()) == -1) || (c != expected_token.charAt(i))) {
                      error_token = true;
                      break;
                    }
                  }
                }
                /* finish for mean finish reading this token, add token to the list */
                if (!error_token)
                  token_list.add(new Token(7,expected_token));
                break;
            case 'e':
              expected_token = "else";
              for (int i = 1; i < expected_token.length(); i++){
                if (!error_token){
                  /* If reach end of file before finish reading token or get a
                  character not suppose to be in this token. Make error_token = true */
                    if (((c = bufferRead.read()) == -1) || (c != expected_token.charAt(i))) {
                      error_token = true;
                      break;
                    }
                  }
              }
              /* finish for mean finish reading this token, add token to the list */
              if (!error_token)
                token_list.add(new Token(8,expected_token));
              break;
            case 'w':
              expected_token = "while";
              for (int i = 1; i < expected_token.length(); i++){
                if (!error_token){
                  /* If reach end of file before finish reading token or get a
                  character not suppose to be in this token. Make error_token = true */
                    if (((c = bufferRead.read()) == -1) || (c != expected_token.charAt(i))) {
                      error_token = true;
                      break;
                    }
                  }
              }
              /* finish for mean finish reading this token, add token to the list */
              if (!error_token)
                token_list.add(new Token(9,expected_token));
              break;
            case 't':
              expected_token = "true";
              for (int i = 1; i < expected_token.length(); i++){
                if (!error_token){
                  /* If reach end of file before finish reading token or get a
                  character not suppose to be in this token. Make error_token = true */
                    if (((c = bufferRead.read()) == -1) || (c != expected_token.charAt(i))) {
                      error_token = true;
                      break;
                    }
                  }
              }
              /* finish for mean finish reading this token, add token to the list */
              if (!error_token)
                token_list.add(new Token(10,expected_token));
              break;
            case 'f':
              expected_token = "false";
              for (int i = 1; i < expected_token.length(); i++){
                if (!error_token){
                  /* If reach end of file before finish reading token or get a
                  character not suppose to be in this token. Make error_token = true */
                    if (((c = bufferRead.read()) == -1) || (c != expected_token.charAt(i))) {
                      error_token = true;
                      break;
                    }
                  }
              }
              /* finish for mean finish reading this token, add token to the list */
              if (!error_token)
                token_list.add(new Token(11,expected_token));
              break;
            case '!':
              token_list.add(new Token(12, "!"));
              break;

          }
        }
      }

      /* If there is error in tokenize then print to System.out error message
        Otherwise print token_list to file */
      if (error_token) {
        System.out.println("Error while tokeneize \n");
        for(int i = 0; i< token_list.size(); i++)
          outputWriter.write(token_list.get(i).getSequence());
        /* Close input file and output file */
        outputWriter.close();
        System.exit(-1);
      }
        else {
          for(int i = 0; i< token_list.size(); i++)
            outputWriter.write(token_list.get(i).getSequence());
          /* Close input file and output file */
          outputWriter.close();

        }
      return this.token_list;
    }

    public recognizer() {
      token_list = new LinkedList<Token>();
    }
  }


  /* get next token by pop first element in the list and reset lookahead */
  public void get_next_token() {
    /*
    System.out.println("Token remaining: ");
    for (int j = 0; j< token_input_list.size(); j++)
      System.out.println(token_input_list.get(j).getSequence());

    */
    token_input_list.pop();
    /* If token is isEmpty then return lookahead as EOF */
    if (token_input_list.isEmpty()) {
      lookahead = new Token(-1,"EOF");
    }
    /* Otherwise return next token in the list */
    else {
      lookahead = token_input_list.getFirst();
    }
  }

  /* compare token and move forward */
  public void eat(int token) {
    /* If right token, move to next token */
    if (lookahead.get_token_ID() == token){
        get_next_token();
      }
      else {
        /* Otherwise printout error and exit */
        System.out.println("Parse error");
        System.exit(-1);
      }
  }

  public void start_parsing() {
    /* Get first lookahead as first element in LinkedList */
    lookahead = token_input_list.getFirst();
    /* Start parse with start Symbol S */
    S();
  }

  public void S() {
    switch (lookahead.get_token_ID()) {
      /* case { */
      case 1:
        eat(1);L();eat(2);
        break;
      /* case System.out.printout */
      case 3:
        eat(3);eat(4);E();eat(5);eat(6);
        break;
      /* case If else */
      case 7:
        eat(7);eat(4);E();eat(5);S();eat(8);S();
        break;
      /* case while */
      case 9:
        eat(9); eat(4); E(); eat(5); S();
        break;
      default:
        /* Otherwise printout error and exit */
        System.out.println("Parse error");
        System.exit(-1);
    }
  }
  public void L() {

  /* if lookahead is S then do SL otherwise donothing */
    switch (lookahead.get_token_ID()) {
      case 1:
      case 3:
      case 7:
      case 9: S(); L(); break;
      default: break;
    }
  }

  public void E() {
    switch(lookahead.get_token_ID()) {
      /* case true */
      case 10: eat(10); break;
      /* case false */
      case 11: eat(11); break;
      /* case !E */
      case 12: eat(12); E(); break;
      default:
        /* Otherwise printout error and exit */
        System.out.println("Parse error");
        System.exit(-1);
    }
  }

  public Parser() throws IOException{
    recognizer tokenize = new recognizer();
    this.token_input_list = tokenize.get_tokens();

  }

  public static void main(String[] args) throws IOException{
    Parser mini_parser = new Parser();
    mini_parser.start_parsing();
    /* If finish parsing without any exit, printout successfully */
    System.out.println("Program parsed successfully");
  }
}
