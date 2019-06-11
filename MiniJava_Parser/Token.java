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
