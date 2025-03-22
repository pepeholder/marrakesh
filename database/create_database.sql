CREATE TABLE games (
                       game_id SERIAL PRIMARY KEY,
                       status VARCHAR(20) CHECK (status IN ('waiting', 'in_progress', 'finished')) NOT NULL,
                       turn_order INT[] NOT NULL,
                       assam_position_x INT NOT NULL,
                       assam_position_y INT NOT NULL,
                       assam_direction VARCHAR(10) CHECK (assam_direction IN ('up', 'down', 'left', 'right')) NOT NULL
);

CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password_hash TEXT NOT NULL,
                       is_playing BOOLEAN DEFAULT FALSE,
                       current_game_id INT REFERENCES games(game_id) ON DELETE SET NULL,
                       total_coins INT DEFAULT 0
);

CREATE TABLE game_turns (
                            game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
                            current_turn INT REFERENCES users(user_id) ON DELETE CASCADE,
                            PRIMARY KEY (game_id, current_turn)
);


CREATE TABLE game_players (
                              game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
                              user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
                              player_color VARCHAR(20) NOT NULL,
                              coins INT DEFAULT 30,
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
                                  PRIMARY KEY (carpet_id, position_x, position_y)
);

CREATE TABLE moves (
                       move_id SERIAL PRIMARY KEY,
                       game_id INT REFERENCES games(game_id) ON DELETE CASCADE,
                       user_id INT REFERENCES users(user_id) ON DELETE CASCADE,
                       turn_number INT NOT NULL,
                       move_description TEXT NOT NULL,
                       timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);