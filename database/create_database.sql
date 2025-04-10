CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash TEXT NOT NULL,
                       is_playing BOOLEAN DEFAULT FALSE,
                       current_game_id INT
);

CREATE TABLE games (
                       game_id SERIAL PRIMARY KEY,
                       status VARCHAR(20) CHECK (status IN ('waiting', 'in_progress', 'finished')) NOT NULL,
                       assam_position_x INT NOT NULL DEFAULT 3,
                       assam_position_y INT NOT NULL DEFAULT 3,
                       assam_direction VARCHAR(10) CHECK (assam_direction IN ('up', 'down', 'left', 'right')) NOT NULL,
                       current_turn INT,
                       current_move_number INT NOT NULL DEFAULT 1
);

ALTER TABLE users
    ADD CONSTRAINT fk_current_game
        FOREIGN KEY (current_game_id) REFERENCES games(game_id) ON DELETE SET NULL;

ALTER TABLE games
    ADD CONSTRAINT fk_current_turn
        FOREIGN KEY (current_turn) REFERENCES users(user_id) ON DELETE SET NULL;

CREATE TABLE game_players (
                              game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
                              user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
                              player_color VARCHAR(20) NOT NULL,
                              coins INT NOT NULL DEFAULT 30,
                              turn_order INT,
                              PRIMARY KEY (game_id, user_id)
);

CREATE TABLE carpets (
                         carpet_id SERIAL PRIMARY KEY,
                         game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
                         owner_id INT REFERENCES users(user_id) ON DELETE CASCADE,
                         color VARCHAR(20) NOT NULL
);

CREATE TABLE carpet_positions (
                                  carpet_id INT REFERENCES carpets(carpet_id) ON DELETE CASCADE,
                                  position_x INT NOT NULL,
                                  position_y INT NOT NULL,
                                  placement_turn INT NOT NULL DEFAULT 1,
                                  PRIMARY KEY (carpet_id, position_x, position_y)
);
