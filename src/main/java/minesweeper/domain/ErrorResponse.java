package minesweeper.domain;

import lombok.Data;

/**
 * Ответ с ошибкой из API
 * */
@Data
public class ErrorResponse {

    String error;

}
