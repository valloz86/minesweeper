package minesweeper.exception;

/**
 * Ошибка в процессе игры или при попытке её начать
 * */
public class MinesweeperRuntimeException extends RuntimeException{

    public MinesweeperRuntimeException(String msg) {
        super(msg);
    }

}
