package minesweeper.service;

import minesweeper.domain.*;
import minesweeper.exception.MinesweeperRuntimeException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Логика обработки запросов
 * */
@Service
public class MinesweeperService {

    public static final int MIN = 2; //минимальный лимит для ширины и высоты
    public static final int MAX = 50; //максимальный лимит для ширины и высоты

    public static final String MINE = "M"; //мина
    public static final String FAIL = "X"; //подорванная мина
    public static final String HIDDEN = " "; //скрытое поле
    public static final String ZERO = "0"; //нулевое поле

    public static final String INCORRECT_DATA = "Некорректные входные данные";
    public static final String GAME_OVER = "Игра закончена или не начата";
    public static final String REPEAT = "Попытка повторного открытия";

    //мапа с играми
    private Map<UUID, MinesweeperGame> minesweeperGameMap = new ConcurrentHashMap<>();

    //генератор случайных мин
    private Random random = new Random();

    //создаём новую игру
    private UUID generateMinesweeper(NewGameRequest request){
        int height = request.getHeight();
        int width = request.getWidth();
        int minesCount = request.getMinesCount();

        String[][] openField = new String[height][width];

        //генерируем случайные мины
        for(int i = 0; i < minesCount; i++) {
            int mineHeight = random.nextInt(height);
            int mineWidth = random.nextInt(width);

            while(MINE.equals(openField[mineHeight][mineWidth])){
                mineHeight = random.nextInt(height);
                mineWidth = random.nextInt(width);
            }

            openField[mineHeight][mineWidth] = MINE;
        }

        //заполняем остальные поля кол-вом мин-соседей
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                if(!MINE.equals(openField[i][j])){
                    int localMinesCount = 0;
                    for(int k = i - 1; k <= i + 1; k++){
                        if( k < 0 || k == height){
                            continue;
                        }

                        for(int l = j - 1; l <= j + 1; l++){
                            if( l < 0 || l == width || (k == i && l == j)){
                                continue;
                            }

                            if(MINE.equals(openField[k][l])){
                                localMinesCount++;
                            }
                        }
                    }

                    openField[i][j] = String.valueOf(localMinesCount);
                }
            }
        }

        UUID gameId = UUID.randomUUID();

        //создаём скрытое поле
        String[][] hiddenField = new String[height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                hiddenField[i][j] = HIDDEN;
            }
        }

        //создаём игру и кладём в мапу
        MinesweeperGame game = new MinesweeperGame();
        game.setWidth(width);
        game.setHeight(height);
        game.setMinesCount(minesCount);
        game.setOpenField(openField);
        game.setHiddenField(hiddenField);
        minesweeperGameMap.put(gameId, game);

        return gameId;
    }

    //создаём новую игру в случае успешной валидации
    public GameInfoResponse newGame(NewGameRequest request){
        validate(request);

        UUID gameId = generateMinesweeper(request);

        return createGameInfoResponse(gameId,
                request.getWidth(),
                request.getHeight(),
                request.getMinesCount(),
                false,
                minesweeperGameMap.get(gameId).getHiddenField());
    }

    //валидируем запрос на создание новой игры
    private void validate(NewGameRequest request){
        int height = request.getHeight();
        int width = request.getWidth();

        if(height < MIN || height > MAX || width < MIN || width > MAX ||
                height*width - 1 < request.getMinesCount()){
            throw new MinesweeperRuntimeException(INCORRECT_DATA);
        }
    }

    //делаем новый ход
    public GameInfoResponse turnGame(GameTurnRequest request){
        GameInfoResponse response;

        UUID gameId = request.getGameId();
        int row = request.getRow();
        int col = request.getCol();

        MinesweeperGame game = minesweeperGameMap.get(gameId);

        //Проверяем наличие игры
        if(game == null){
            throw new MinesweeperRuntimeException(GAME_OVER);
        }

        //Проверяем, что поле пока не открыто
        String[][] hiddenField = game.getHiddenField();
        if(!HIDDEN.equals(hiddenField[row][col])){
            throw new MinesweeperRuntimeException(REPEAT);
        }

        String[][] openField = game.getOpenField();

        //Если наступили на мину - фиксируем проигрыш и завершаем игру
        if(MINE.equals(openField[row][col])){
            for(int i = 0; i < openField.length; i++){
                for(int j = 0; j < openField[i].length; j++){
                    if(MINE.equals(openField[i][j])){
                        openField[i][j] = FAIL;
                    }
                }
            }

            response = createGameInfoResponse(gameId,
                    game.getWidth(),
                    game.getHeight(),
                    game.getMinesCount(),
                    true,
                    openField);

            minesweeperGameMap.remove(gameId);
        } else {
            hiddenField[row][col] = openField[row][col];

            //Если 0 - открываем все прилегающие числовые поля
            if(ZERO.equals(hiddenField[row][col])){
                openZero(hiddenField, openField, row, col);
            }

            //Если всё, кроме мин открыто - фиксируем победу и завершаем игру
            if(hiddenOnlyMines(hiddenField, openField)){
                response = createGameInfoResponse(gameId,
                        game.getWidth(),
                        game.getHeight(),
                        game.getMinesCount(),
                        true,
                        openField);

                minesweeperGameMap.remove(gameId);
            } else {

                //если игра не завершается - просто возвращаем частично скрытое поле
                response = createGameInfoResponse(gameId,
                        game.getWidth(),
                        game.getHeight(),
                        game.getMinesCount(),
                        false,
                        hiddenField);
            }
        }

        return response;
    }

    //проверка, что скрыты только мины
    private boolean hiddenOnlyMines(String[][] hidden, String[][] open) {
        for(int i = 0; i < hidden.length; i++){
            for(int j = 0; j < hidden[i].length; j++){
                if(HIDDEN.equals(hidden[i][j]) && !MINE.equals(open[i][j])){
                    return false;
                }
            }
        }

        return true;
    }

    //Открытие всех прилегающих к 0 числовых полей
    private void openZero(String[][] hidden, String[][] open, int row, int col) {
        for(int k = row - 1; k <= row + 1; k++){
            if( k < 0 || k == open.length){
                continue;
            }

            for(int l = col - 1; l <= col + 1; l++){
                if( l < 0 || l == open[k].length || (k == row && l == col) ||
                    !HIDDEN.equals(hidden[k][l])){
                    continue;
                }

                hidden[k][l] = open[k][l];

                if(ZERO.equals(hidden[k][l])){
                    openZero(hidden, open, k, l);
                }
            }
        }
    }

    //Создание ответа
    private GameInfoResponse createGameInfoResponse(UUID gameId,
                                                    int width,
                                                    int height,
                                                    int minesCount,
                                                    boolean completed,
                                                    String[][] field) {
        GameInfoResponse response = new GameInfoResponse();
        response.setGameId(gameId);

        response.setWidth(width);
        response.setHeight(height);
        response.setMinesCount(minesCount);

        response.setCompleted(completed);
        response.setField(field);

        return response;
    }

}
