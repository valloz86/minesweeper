package minesweeper.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Запрос новой игры из API
 * */
@Data
public class NewGameRequest {

    int width;

    int height;

    @JsonProperty("mines_count")
    int minesCount;

}
