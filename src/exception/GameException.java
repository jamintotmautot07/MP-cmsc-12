
package exception;

/*
 OWNER: Thea

 PURPOSE:
 - Custom error handling

 TASKS:
 - Use for file errors, etc.
*/

public class GameException extends Exception {
    public GameException(String msg){
        super(msg);
    }
}
