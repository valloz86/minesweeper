package minesweeper.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

/**
 * Ответ о текущем состоянии игры из API
 * */
@Data
public class GameInfoResponse {

    @JsonProperty("game_id")
    UUID gameId;

    int width;

    int height;

    @JsonProperty("mines_count")
    int minesCount;

    boolean completed;

    String [][] field;

}
