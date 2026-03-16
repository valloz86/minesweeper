package minesweeper.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

/**
 * Запрос хода из API
 * */
@Data
public class GameTurnRequest {

    @JsonProperty("game_id")
    UUID gameId;

    int col;

    int row;
}
