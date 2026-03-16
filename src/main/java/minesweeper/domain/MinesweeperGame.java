package minesweeper.domain;

import lombok.Data;

/**
 * Игра
 * */
@Data
public class MinesweeperGame {

    //ширина поля
    int width;

    //высота поля
    int height;

    //кол-во мин
    int minesCount;

    //открытое поле
    String[][] openField;

    //скрытое поле
    String[][] hiddenField;

}
