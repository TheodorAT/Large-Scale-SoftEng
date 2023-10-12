-- This is the schema file that the database is initialized with. It is specific to the H2 SQL dialect.
-- Author: Rasmus Ros, rasmus.ros@cs.lth.se


-- User roles describe what each user can do on a generic level.
CREATE TABLE user_role(role_id TINYINT,
                       role VARCHAR(10) NOT NULL UNIQUE,
                       PRIMARY KEY (role_id));

CREATE TABLE users(user_id INT AUTO_INCREMENT NOT NULL,
                  role_id TINYINT NOT NULL,
                  username VARCHAR_IGNORECASE NOT NULL UNIQUE, -- username should be unique
                  salt BIGINT NOT NULL,
                  password_hash UUID NOT NULL,

                  -- user info
                  -- shoud be NOT NULL (required), but all test cases + the user inserts below will fail
                  -- architects should decide what to do here
                  first_name VARCHAR(255),-- NOT NULL,
                  last_name VARCHAR(255),-- NOT NULL,
                  email VARCHAR(255) UNIQUE,-- NOT NULL,
                  phone_number VARCHAR(20),-- NOT NULL,
                  -- end user info

                  PRIMARY KEY (user_id),
                  FOREIGN KEY (role_id) REFERENCES user_role (role_id),
                  CHECK (LENGTH(username) >= 4)); -- ensures that username have 4 or more characters

-- Sessions are indexed by large random numbers instead of a sequence of integers, because they could otherwise
-- be guessed by a malicious user.
CREATE TABLE session(session_uuid UUID DEFAULT RANDOM_UUID(),
                     user_id INT NOT NULL,
                     last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
                     PRIMARY KEY(session_uuid),
                     FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE);


-- LOCATIONS
-- this might have to be edited depending on the info retrieved from the .txt file
-- i haven't seen it myself
CREATE TABLE locations (
    location_id INT AUTO_INCREMENT NOT NULL,
    municipality VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,

    PRIMARY KEY (location_id)
);

-- TRIPS

CREATE TABLE trip_status(status_id TINYINT,
                       status VARCHAR(20) NOT NULL UNIQUE,
                       PRIMARY KEY (status_id));

CREATE TABLE trips (
    trip_id INT AUTO_INCREMENT NOT NULL,
    driver_id INT,
    from_location_id INT NOT NULL,
    to_location_id INT NOT NULL,
    -- using TIMESTAMP for both, allowing us to skip the date
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status_id TINYINT NOT NULL DEFAULT 1,

    -- TODO: this seat_capacity will later be changed to a FOREIGN ref to vehicles
    seat_capacity INT NOT NULL, -- not to be changed, when checking seat availability, query trip_passengers. Excluding driver

    PRIMARY KEY (trip_id),

    CHECK (seat_capacity > 0),
    CHECK (start_time < end_time),

    FOREIGN KEY (driver_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (from_location_id) REFERENCES locations (location_id) ON DELETE SET NULL,
    FOREIGN KEY (to_location_id) REFERENCES locations (location_id) ON DELETE SET NULL,
    FOREIGN KEY (status_id) REFERENCES trip_status (status_id) ON DELETE SET NULL
);

CREATE TABLE trip_passengers(
    trip_id INT NOT NULL,
    user_id INT NOT NULL,

    PRIMARY KEY (trip_id, user_id),
    FOREIGN KEY (trip_id) REFERENCES trips (trip_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);



-- INSERTS

-- Kept 2 user for backwards compatability w/ tests. 
-- Admin sees all tabs, most funcitonality
-- Driver sees all tabs except for admin tab
-- User sees all tabs except register trip as driver and admin tab
-- "both", i.e., driver and passenger, therefore should be a "driver"
INSERT INTO user_role VALUES (1, 'ADMIN'), (2, 'USER'), (3, 'DRIVER');

INSERT INTO users(role_id, username, salt, password_hash, first_name, last_name, email, phone_number)
VALUES 
    (1, 'Admin', -2883142073796788660, '8dc0e2ab-4bf1-7671-c0c4-d22ffb55ee59', 'Admin_first_name',
     'Admin_last_name', 'Admin_email', 'Admin_phone_number'),
    (2, 'Test', 5336889820313124494, '144141f3-c868-85e8-0243-805ca28cdabd', 'Test_first_name', 
    'Test_last_name', 'Test_email', 'Test_phone_number'),
    (3, 'Driver', -6993485440425371837, '9041ed00-4e07-e0bf-d6ba-4bb84d631770', 'Driver_first_name',
    'Driver_last_name', 'Driver_email', 'Driver_phone_number');

INSERT INTO trip_status VALUES (1, 'ACTIVE'), (2, 'CANCELLED'), (3, 'REQUESTED');