package minesweeper.service;

import lombok.extern.slf4j.Slf4j;
import minesweeper.domain.*;

import org.assertj.core.util.Throwables;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;


import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static minesweeper.service.MinesweeperService.HIDDEN;
import static minesweeper.service.MinesweeperService.MINE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@SpringBootTest
public class MinesweeperTest {

    @Autowired
    MinesweeperService minesweeperService;


    @Test
    public void mainTest(){
        try {
            //получаем мапу с играми
            Field mapField = MinesweeperService.class.getDeclaredField("minesweeperGameMap");
            mapField.setAccessible(true);
            Map<UUID, MinesweeperGame> minesweeperGameMap =
                    (Map<UUID, MinesweeperGame>) mapField.get(minesweeperService);

            //начинаем игру
            NewGameRequest newGameRequest = new NewGameRequest(10, 10, 10);

            //получаем все данные о ней
            GameInfoResponse response = minesweeperService.newGame(newGameRequest);

            UUID gameId = response.getGameId();
            String[][] openField = minesweeperGameMap.get(gameId).getOpenField();
            String[][] hiddenField = minesweeperGameMap.get(gameId).getHiddenField();

            //зная, где мины, открываем всё остальное
            for(int i = 0; i < openField.length; i++){
                for(int j = 0; j < openField[i].length; j++){
                    if(HIDDEN.equals(hiddenField[i][j]) && !MINE.equals(openField[i][j])){
                        log.info("i = " + i + ", j =" + j + ", hiddenField=" + hiddenField[i][j] + ", openField=" + openField[i][j]);
                        response =  minesweeperService.turnGame(new GameTurnRequest(gameId, j, i));
                    }
                }
            }

            //проверяем, что игра окончена
            assertTrue(response.isCompleted());
        } catch (Exception e) {
            log.error(Throwables.getStackTrace(e));
            fail();
        }



    }
}
