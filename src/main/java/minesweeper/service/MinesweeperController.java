package minesweeper.service;

import minesweeper.domain.ErrorResponse;
import minesweeper.domain.GameInfoResponse;
import minesweeper.domain.GameTurnRequest;
import minesweeper.domain.NewGameRequest;
import minesweeper.exception.MinesweeperRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
* Контроллер для запросов c фронтенда
* */

@RestController
@RequestMapping("/api")
public class MinesweeperController {

    @Autowired
    private MinesweeperService service;

    /**
     * Новая игра
     * */
    @PostMapping("/new")
    public GameInfoResponse newGame(@RequestBody NewGameRequest request){
        return service.newGame(request);
    }

    /**
     * Ход в игре
     * */
    @PostMapping("/turn")
    public GameInfoResponse turnGame(@RequestBody GameTurnRequest request){
        return service.turnGame(request);
    }

    /**
     * Обработчик ошибок
     * */
    @ExceptionHandler(MinesweeperRuntimeException.class)
    public ResponseEntity<ErrorResponse> error(MinesweeperRuntimeException e){
        ErrorResponse response = new ErrorResponse();
        response.setError(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
