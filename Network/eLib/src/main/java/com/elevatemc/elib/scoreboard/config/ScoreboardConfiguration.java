package com.elevatemc.elib.scoreboard.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.elevatemc.elib.scoreboard.construct.ScoreGetter;
import com.elevatemc.elib.scoreboard.construct.TitleGetter;

/**
 * Scoreboard Configuration class. This class can be used to
 * create scoreboard objects. This configuration object provides
 * the title/scores, along with some other settings. This should be passed to
 * FrozenScoreboardHandler#setConfiguration.
 */
@NoArgsConstructor
public final class ScoreboardConfiguration {

    @Getter @Setter private TitleGetter titleGetter;
    @Getter @Setter private ScoreGetter scoreGetter;

}