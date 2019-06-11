import java.io.*;
import java.util.LinkedList;




public class recognizer {
  /* Class to define a token include an ID and name of the toke */
  public class Token{
    int token_ID; // ID of the token
    String name; // Name of the token

    /* Token ID table
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

  /* LinkedList to store all token got from the file */
  private LinkedList<Token> token_list;

  public void get_tokens() throws IOException{
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
    }
      else {
        for(int i = 0; i< token_list.size(); i++)
          outputWriter.write(token_list.get(i).getSequence());
      }
    /* Close input file and output file */
    outputWriter.close();
  }

  public recognizer() {
    token_list = new LinkedList<Token>();
  }

  public static void main(String[] args) throws IOException{
    recognizer test = new recognizer();
    test.get_tokens();
  }
}
