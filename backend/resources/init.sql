	-- =========================================
	-- RESET DATABASE (DEV MODE)
	-- =========================================
	SET FOREIGN_KEY_CHECKS = 0;
	
	DROP TABLE IF EXISTS parking_slots;
	DROP TABLE IF EXISTS cars;
	
	SET FOREIGN_KEY_CHECKS = 1;
	
	-- =========================================
	-- CREATE TABLE: cars
	-- =========================================
	CREATE TABLE cars (
	    id BIGINT AUTO_INCREMENT PRIMARY KEY,
	    car_number VARCHAR(20) NOT NULL UNIQUE
	);
	
	-- =========================================
	-- CREATE TABLE: parking_slots
	-- =========================================
	CREATE TABLE parking_slots (
	    id BIGINT AUTO_INCREMENT PRIMARY KEY,
	    slot_number INT NOT NULL UNIQUE,
	    status VARCHAR(20) NOT NULL,
	    car_id BIGINT,
	    CONSTRAINT fk_car
	        FOREIGN KEY (car_id)
	        REFERENCES cars(id)
	        ON DELETE SET NULL
	);
	
	-- =========================================
	-- INSERT TEST DATA
	-- =========================================
	
	-- Cars
	INSERT INTO cars (car_number) VALUES
	('B-AB1234'),
	('M-CD5678'),
	('HH-EF9012'),
	('F-GH3456'),
	('S-IJ7890');
	
	-- Parking Slots (50 slots)
	INSERT INTO parking_slots (slot_number, status, car_id) VALUES
	(1, 'OCCUPIED', 1),
	(2, 'OCCUPIED', 2),
	(3, 'IN_PROCESS', 3),
	(4, 'FREE', NULL),
	(5, 'FREE', NULL),
	(6, 'FREE', NULL),
	(7, 'FREE', NULL),
	(8, 'FREE', NULL),
	(9, 'FREE', NULL),
	(10, 'FREE', NULL),
	
	(11, 'FREE', NULL),
	(12, 'FREE', NULL),
	(13, 'FREE', NULL),
	(14, 'FREE', NULL),
	(15, 'FREE', NULL),
	(16, 'FREE', NULL),
	(17, 'FREE', NULL),
	(18, 'FREE', NULL),
	(19, 'FREE', NULL),
	(20, 'FREE', NULL),
	
	(21, 'FREE', NULL),
	(22, 'FREE', NULL),
	(23, 'FREE', NULL),
	(24, 'FREE', NULL),
	(25, 'FREE', NULL),
	(26, 'FREE', NULL),
	(27, 'FREE', NULL),
	(28, 'FREE', NULL),
	(29, 'FREE', NULL),
	(30, 'FREE', NULL),
	
	(31, 'FREE', NULL),
	(32, 'FREE', NULL),
	(33, 'FREE', NULL),
	(34, 'FREE', NULL),
	(35, 'FREE', NULL),
	(36, 'FREE', NULL),
	(37, 'FREE', NULL),
	(38, 'FREE', NULL),
	(39, 'FREE', NULL),
	(40, 'FREE', NULL),
	
	(41, 'FREE', NULL),
	(42, 'FREE', NULL),
	(43, 'FREE', NULL),
	(44, 'FREE', NULL),
	(45, 'FREE', NULL),
	(46, 'FREE', NULL),
	(47, 'FREE', NULL),
	(48, 'FREE', NULL),
	(49, 'FREE', NULL),
	(50, 'FREE', NULL);
}

