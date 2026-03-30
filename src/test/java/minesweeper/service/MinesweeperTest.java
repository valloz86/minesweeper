package minesweeper.service;

import lombok.extern.slf4j.Slf4j;
import minesweeper.domain.*;

import minesweeper.exception.MinesweeperRuntimeException;
import org.assertj.core.util.Throwables;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import static minesweeper.service.MinesweeperService.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class MinesweeperTest {

    @Autowired
    MinesweeperService minesweeperService;

    /**
    * Сценарий победы
    * */
    @Test
    public void winTest(){
        try {
            //получаем мапу с играми
            Map<UUID, MinesweeperGame> minesweeperGameMap = getMinesweeperGameMap();

            //начинаем игру и получаем все данные о ней
            NewGameRequest newGameRequest = new NewGameRequest(10, 10, 10);

            GameInfoResponse response = minesweeperService.newGame(newGameRequest);

            UUID gameId = response.getGameId();
            String[][] openField = minesweeperGameMap.get(gameId).getOpenField();
            String[][] hiddenField = minesweeperGameMap.get(gameId).getHiddenField();

            //зная, где мины, открываем всё остальное
            for(int i = 0; i < openField.length; i++){
                for(int j = 0; j < openField[i].length; j++){
                    if(HIDDEN.equals(hiddenField[i][j]) && !MINE.equals(openField[i][j])){
                        response =  minesweeperService.turnGame(new GameTurnRequest(gameId, j, i));
                    }
                }
            }

            //проверяем, что игра окончена
            assertTrue(response.isCompleted());

            //проверяем, что мы не наступили на мины и победили
            int minesCount = 0;
            for(int i = 0; i < openField.length; i++) {
                for (int j = 0; j < openField[i].length; j++) {
                    if(FAIL.equals(openField[i][j])){
                        fail();
                    } else if(MINE.equals(openField[i][j])){
                        minesCount++;
                    }
                }
            }

            //проверяем, что кол-во мин сошлось
            assertEquals(10, minesCount);

            //проверяем, что игра удалилась
            minesweeperService.turnGame(new GameTurnRequest(gameId, 0, 0));
            fail();
        }  catch (MinesweeperRuntimeException e){
            assertEquals(GAME_OVER, e.getMessage());
        } catch (Exception e) {
            log.error(Throwables.getStackTrace(e));
            fail();
        }
    }

    private Map<UUID, MinesweeperGame> getMinesweeperGameMap() throws NoSuchFieldException, IllegalAccessException {
        Field mapField = MinesweeperService.class.getDeclaredField("minesweeperGameMap");
        mapField.setAccessible(true);
        Map<UUID, MinesweeperGame> minesweeperGameMap =
                (Map<UUID, MinesweeperGame>) mapField.get(minesweeperService);
        return minesweeperGameMap;
    }

    /**
     * Сценарий поражения
     * */
    @Test
    public void failTest(){
        try {
            //получаем мапу с играми
            Map<UUID, MinesweeperGame> minesweeperGameMap = getMinesweeperGameMap();

            //начинаем игру и получаем все данные о ней
            NewGameRequest newGameRequest = new NewGameRequest(10, 10, 10);

            GameInfoResponse response = minesweeperService.newGame(newGameRequest);

            UUID gameId = response.getGameId();
            String[][] openField = minesweeperGameMap.get(gameId).getOpenField();

            //ищем и подрываем мину
            for(int i = 0; i < openField.length; i++){
                for(int j = 0; j < openField[i].length; j++){
                    if(MINE.equals(openField[i][j])){
                        response =  minesweeperService.turnGame(new GameTurnRequest(gameId, j, i));
                        break;
                    }
                }
            }

            //проверяем, что игра окончена
            assertTrue(response.isCompleted());

            //проверяем, что мы наступили на мины и проиграли
            int minesCount = 0;
            for(int i = 0; i < openField.length; i++) {
                for (int j = 0; j < openField[i].length; j++) {
                    if(MINE.equals(openField[i][j])){
                        fail();
                    } else if(FAIL.equals(openField[i][j])){
                        minesCount++;
                    }
                }
            }

            //проверяем, что кол-во мин сошлось
            assertEquals(10, minesCount);

            //проверяем, что игра удалилась
            minesweeperService.turnGame(new GameTurnRequest(gameId, 0, 0));
            fail();
        } catch (MinesweeperRuntimeException e){
            assertEquals(GAME_OVER, e.getMessage());
        } catch (Exception e) {
            log.error(Throwables.getStackTrace(e));
            fail();
        }
    }

    /**
     * Сценарий хода в несуществующей игре
     * */
    @Test
    public void noGameTest(){
        try {
            minesweeperService.turnGame(new GameTurnRequest(UUID.randomUUID(), 0, 0));

            fail();
        } catch (MinesweeperRuntimeException e){
            //проверяем, что случилась нужная ошибка с нужным сообщением
            assertEquals(GAME_OVER, e.getMessage());
        } catch (Exception e){
            log.error(Throwables.getStackTrace(e));
            fail();
        }
    }

    /**
     * Сценарий повторного открытия клетки
     * */
    @Test
    public void repeatTest(){
        try {
            //получаем мапу с играми
            Map<UUID, MinesweeperGame> minesweeperGameMap = getMinesweeperGameMap();

            //начинаем игру и получаем все данные о ней
            NewGameRequest newGameRequest = new NewGameRequest(10, 10, 10);

            GameInfoResponse response = minesweeperService.newGame(newGameRequest);

            UUID gameId = response.getGameId();
            String[][] openField = minesweeperGameMap.get(gameId).getOpenField();

            for(int i = 0; i < openField.length; i++){
                for(int j = 0; j < openField[i].length; j++){
                    if(!MINE.equals(openField[i][j])){
                        minesweeperService.turnGame(new GameTurnRequest(gameId, j, i));
                        minesweeperService.turnGame(new GameTurnRequest(gameId, j, i));
                        fail();
                    }
                }
            }
        } catch (MinesweeperRuntimeException e){
            //проверяем, что случилась нужная ошибка с нужным сообщением
            assertEquals(REPEAT, e.getMessage());
        } catch (Exception e){
            log.error(Throwables.getStackTrace(e));
            fail();
        }
    }

    /**
     * Невалидные входные данные
     * */
    @Test
    public void invalidTest() {
        try {
            minesweeperService.newGame(new NewGameRequest(MIN - 1, 10, 1));
            fail();
        } catch (MinesweeperRuntimeException e){
            //проверяем, что случилась нужная ошибка с нужным сообщением
            assertEquals(INCORRECT_DATA, e.getMessage());
        }

        try {
            minesweeperService.newGame(new NewGameRequest(MAX + 1, 10, 1));
            fail();
        } catch (MinesweeperRuntimeException e){
            //проверяем, что случилась нужная ошибка с нужным сообщением
            assertEquals(INCORRECT_DATA, e.getMessage());
        }

        try {
            minesweeperService.newGame(new NewGameRequest(10, MIN - 1, 1));
            fail();
        } catch (MinesweeperRuntimeException e){
            //проверяем, что случилась нужная ошибка с нужным сообщением
            assertEquals(INCORRECT_DATA, e.getMessage());
        }

        try {
            minesweeperService.newGame(new NewGameRequest(10, MAX + 1, 1));
            fail();
        } catch (MinesweeperRuntimeException e){
            //проверяем, что случилась нужная ошибка с нужным сообщением
            assertEquals(INCORRECT_DATA, e.getMessage());
        }

        try {
            minesweeperService.newGame(new NewGameRequest(10, 10, 100));
            fail();
        } catch (MinesweeperRuntimeException e){
            //проверяем, что случилась нужная ошибка с нужным сообщением
            assertEquals(INCORRECT_DATA, e.getMessage());
        }
    }
}
