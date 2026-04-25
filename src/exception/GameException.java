
package exception;

/*
 OWNER: Thea

 PURPOSE:
 - Custom error handling

 TASKS:
 - Use for file errors, etc.
*/

/**
 * Small custom exception type for game-specific failures.
 */
public class GameException extends Exception {
    // Small wrapper exception for game-specific failures that deserve a clearer type than plain Exception.
    public GameException(String msg){
        super(msg);
    }
}
