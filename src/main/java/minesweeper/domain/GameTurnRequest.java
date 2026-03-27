package minesweeper.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

/**
 * Запрос хода из API
 * */
@Data
public class GameTurnRequest {

    @JsonProperty("game_id")
    @NonNull private UUID gameId;

    @NonNull private int col;

    @NonNull private int row;
}
