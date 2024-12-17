DELIMITER $$

CREATE PROCEDURE add_movie(
    IN p_movie_id INT,
    IN p_title VARCHAR(255),
    IN p_year INT,
    IN p_director VARCHAR(255),
    IN p_price DECIMAL(4, 2),
    IN p_star_name VARCHAR(255),
    IN p_genre_name VARCHAR(255)
)
BEGIN
    -- Declare variables for the star and genre ids
    DECLARE star_id INT;
    DECLARE genre_id INT;

    -- Insert the movie into the movies table
INSERT INTO movies (id, title, year, director, price)
VALUES (p_movie_id, p_title, p_year, p_director, p_price);

-- Check if the star already exists, if not insert into the stars table
SELECT id INTO star_id
FROM stars
WHERE name = p_star_name;

IF star_id IS NULL THEN
        INSERT INTO stars (name)
        VALUES (p_star_name);
        -- Get the newly inserted star's id
SELECT LAST_INSERT_ID() INTO star_id;
END IF;

    -- Check if the genre already exists, if not insert into the genres table
SELECT id INTO genre_id
FROM genres
WHERE name = p_genre_name;

IF genre_id IS NULL THEN
        INSERT INTO genres (name)
        VALUES (p_genre_name);
        -- Get the newly inserted genre's id
SELECT LAST_INSERT_ID() INTO genre_id;
END IF;

    -- Link the movie to the star in the movie_stars table
INSERT INTO movie_stars (movie_id, star_id)
VALUES (p_movie_id, star_id);

-- Link the movie to the genre in the movie_genres table
INSERT INTO movie_genres (movie_id, genre_id)
VALUES (p_movie_id, genre_id);

END $$

DELIMITER ;
