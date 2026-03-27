package minesweeper.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

/**
 * Запрос новой игры из API
 * */
@Data
public class NewGameRequest {

    @NonNull private int width;

    @NonNull private int height;

    @JsonProperty("mines_count")
    @NonNull private int minesCount;

}
