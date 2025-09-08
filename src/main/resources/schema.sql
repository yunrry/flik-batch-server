-- flik-batch-server 테이블 초기화 SQL
-- 파일 위치: src/main/resources/schema.sql
USE flik_batch_db;
-- 1. 관광지 데이터 테이블 (content_type_id: 12)
CREATE TABLE IF NOT EXISTS fetched_tourist_attractions (
                                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                           content_id VARCHAR(50) NOT NULL UNIQUE,
                                                           content_type_id VARCHAR(10) NOT NULL,
                                                           content_type_name VARCHAR(50) NOT NULL,
                                                           title VARCHAR(500) NOT NULL,
                                                           addr1 VARCHAR(500),
                                                           addr2 VARCHAR(500),
                                                           first_image TEXT,
                                                           first_image2 TEXT,
                                                           map_x VARCHAR(50),
                                                           map_y VARCHAR(50),
                                                           area_code VARCHAR(10),
                                                           sigungu_code VARCHAR(10),
                                                           cat1 VARCHAR(10),
                                                           cat2 VARCHAR(10),
                                                           cat3 VARCHAR(10),
                                                           created_time VARCHAR(50),
                                                           modified_time VARCHAR(50),
                                                           tel TEXT,
                                                           zipcode VARCHAR(20),
                                                           overview TEXT,
                                                           source VARCHAR(255) DEFAULT 'http://apis.data.go.kr/B551011/KorService2',
                                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                           usetime TEXT,
                                                           restdate TEXT,
                                                           parking TEXT,
                                                           parkingfee VARCHAR(100) DEFAULT '',
                                                           infocenter VARCHAR(200) DEFAULT '',
                                                           chkbabycarriage VARCHAR(50) DEFAULT '',
                                                           chkpet VARCHAR(50) DEFAULT '',
                                                           chkcreditcard VARCHAR(50) DEFAULT '',
                                                           heritage1 VARCHAR(10) DEFAULT '',
                                                           heritage2 VARCHAR(10) DEFAULT '',
                                                           heritage3 VARCHAR(10) DEFAULT '',
                                                           opendate VARCHAR(50) DEFAULT '',
                                                           expguide TEXT,
                                                           expagerange VARCHAR(100) DEFAULT '',
                                                           accomcount VARCHAR(50) DEFAULT '',
                                                           useseason VARCHAR(200) DEFAULT '',

                                                           INDEX idx_content_type (content_type_id),
                                                           INDEX idx_area (area_code, sigungu_code),
                                                           INDEX idx_title (title(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 문화시설 데이터 테이블 (content_type_id: 14)
CREATE TABLE IF NOT EXISTS fetched_cultural_facilities (
                                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                           content_id VARCHAR(50) NOT NULL UNIQUE,
                                                           content_type_id VARCHAR(10) NOT NULL,
                                                           content_type_name VARCHAR(50) NOT NULL,
                                                           title VARCHAR(500) NOT NULL,
                                                           addr1 VARCHAR(500),
                                                           addr2 VARCHAR(500),
                                                           first_image TEXT,
                                                           first_image2 TEXT,
                                                           map_x VARCHAR(50),
                                                           map_y VARCHAR(50),
                                                           area_code VARCHAR(10),
                                                           sigungu_code VARCHAR(10),
                                                           cat1 VARCHAR(10),
                                                           cat2 VARCHAR(10),
                                                           cat3 VARCHAR(10),
                                                           created_time VARCHAR(50),
                                                           modified_time VARCHAR(50),
                                                           tel TEXT,
                                                           zipcode VARCHAR(20),
                                                           overview TEXT,
                                                           source VARCHAR(255) DEFAULT 'http://apis.data.go.kr/B551011/KorService2',
                                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                           usetime TEXT,
                                                           restdate TEXT,
                                                           parking TEXT,
                                                           parkingfee VARCHAR(100) DEFAULT '',
                                                           infocenter VARCHAR(200) DEFAULT '',
                                                           chkbabycarriage VARCHAR(50) DEFAULT '',
                                                           chkpet VARCHAR(50) DEFAULT '',
                                                           chkcreditcard VARCHAR(50) DEFAULT '',
                                                           scale VARCHAR(100) DEFAULT '',
                                                           usefee VARCHAR(200) DEFAULT '',
                                                           discountinfo VARCHAR(200) DEFAULT '',
                                                           spendtime VARCHAR(100) DEFAULT '',

                                                           INDEX idx_content_type (content_type_id),
                                                           INDEX idx_area (area_code, sigungu_code),
                                                           INDEX idx_title (title(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 축제공연행사 데이터 테이블 (content_type_id: 15)
CREATE TABLE IF NOT EXISTS fetched_festivals_events (
                                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                        content_id VARCHAR(50) NOT NULL UNIQUE,
                                                        content_type_id VARCHAR(10) NOT NULL,
                                                        content_type_name VARCHAR(50) NOT NULL,
                                                        title VARCHAR(500) NOT NULL,
                                                        addr1 VARCHAR(500),
                                                        addr2 VARCHAR(500),
                                                        first_image TEXT,
                                                        first_image2 TEXT,
                                                        map_x VARCHAR(50),
                                                        map_y VARCHAR(50),
                                                        area_code VARCHAR(10),
                                                        sigungu_code VARCHAR(10),
                                                        cat1 VARCHAR(10),
                                                        cat2 VARCHAR(10),
                                                        cat3 VARCHAR(10),
                                                        created_time VARCHAR(50),
                                                        modified_time VARCHAR(50),
                                                        tel TEXT,
                                                        zipcode VARCHAR(20),
                                                        overview TEXT,
                                                        source VARCHAR(255) DEFAULT 'http://apis.data.go.kr/B551011/KorService2',
                                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                        usetime TEXT,
                                                        restdate TEXT,
                                                        parking TEXT,
                                                        parkingfee VARCHAR(100) DEFAULT '',
                                                        infocenter VARCHAR(200) DEFAULT '',
                                                        chkbabycarriage VARCHAR(50) DEFAULT '',
                                                        chkpet VARCHAR(50) DEFAULT '',
                                                        chkcreditcard VARCHAR(50) DEFAULT '',
                                                        sponsor1 VARCHAR(200) DEFAULT '',
                                                        sponsor1tel TEXT,
                                                        sponsor2 VARCHAR(200) DEFAULT '',
                                                        sponsor2tel VARCHAR(50) DEFAULT '',
                                                        eventenddate VARCHAR(20) DEFAULT '',
                                                        playtime VARCHAR(100) DEFAULT '',
                                                        eventplace VARCHAR(500) DEFAULT '',
                                                        eventhomepage VARCHAR(500) DEFAULT '',
                                                        agelimit VARCHAR(100) DEFAULT '',
                                                        bookingplace VARCHAR(200) DEFAULT '',
                                                        placeinfo TEXT,
                                                        subevent TEXT,
                                                        program TEXT,
                                                        eventstartdate VARCHAR(20) DEFAULT '',
                                                        usetimefestival VARCHAR(200) DEFAULT '',
                                                        discountinfofestival VARCHAR(200) DEFAULT '',
                                                        spendtimefestival VARCHAR(100) DEFAULT '',
                                                        festivalgrade VARCHAR(50) DEFAULT '',
                                                        progresstype VARCHAR(50) DEFAULT '',
                                                        festivaltype VARCHAR(50) DEFAULT '',

                                                        INDEX idx_content_type (content_type_id),
                                                        INDEX idx_area (area_code, sigungu_code),
                                                        INDEX idx_title (title(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 레포츠 데이터 테이블 (content_type_id: 28)
CREATE TABLE IF NOT EXISTS fetched_sports_recreation (
                                                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                         content_id VARCHAR(50) NOT NULL UNIQUE,
                                                         content_type_id VARCHAR(10) NOT NULL,
                                                         content_type_name VARCHAR(50) NOT NULL,
                                                         title VARCHAR(500) NOT NULL,
                                                         addr1 VARCHAR(500),
                                                         addr2 VARCHAR(500),
                                                         first_image TEXT,
                                                         first_image2 TEXT,
                                                         map_x VARCHAR(50),
                                                         map_y VARCHAR(50),
                                                         area_code VARCHAR(10),
                                                         sigungu_code VARCHAR(10),
                                                         cat1 VARCHAR(10),
                                                         cat2 VARCHAR(10),
                                                         cat3 VARCHAR(10),
                                                         created_time VARCHAR(50),
                                                         modified_time VARCHAR(50),
                                                         tel TEXT,
                                                         zipcode VARCHAR(20),
                                                         overview TEXT,
                                                         source VARCHAR(255) DEFAULT 'http://apis.data.go.kr/B551011/KorService2',
                                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                         usetime TEXT,
                                                         restdate TEXT,
                                                         parking TEXT,
                                                         parkingfee VARCHAR(100) DEFAULT '',
                                                         infocenter VARCHAR(200) DEFAULT '',
                                                         chkbabycarriage VARCHAR(50) DEFAULT '',
                                                         chkpet VARCHAR(50) DEFAULT '',
                                                         chkcreditcard VARCHAR(50) DEFAULT '',
                                                         openperiod VARCHAR(200) DEFAULT '',
                                                         reservation VARCHAR(200) DEFAULT '',
                                                         scaleleports VARCHAR(100) DEFAULT '',
                                                         accomcountleports VARCHAR(50) DEFAULT '',
                                                         usefeeleports VARCHAR(200) DEFAULT '',
                                                         expagerangeleports VARCHAR(100) DEFAULT '',

                                                         INDEX idx_content_type (content_type_id),
                                                         INDEX idx_area (area_code, sigungu_code),
                                                         INDEX idx_title (title(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 쇼핑 데이터 테이블 (content_type_id: 38)
CREATE TABLE IF NOT EXISTS fetched_shopping (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                content_id VARCHAR(50) NOT NULL UNIQUE,
                                                content_type_id VARCHAR(10) NOT NULL,
                                                content_type_name VARCHAR(50) NOT NULL,
                                                title VARCHAR(500) NOT NULL,
                                                addr1 VARCHAR(500),
                                                addr2 VARCHAR(500),
                                                first_image TEXT,
                                                first_image2 TEXT,
                                                map_x VARCHAR(50),
                                                map_y VARCHAR(50),
                                                area_code VARCHAR(10),
                                                sigungu_code VARCHAR(10),
                                                cat1 VARCHAR(10),
                                                cat2 VARCHAR(10),
                                                cat3 VARCHAR(10),
                                                created_time VARCHAR(50),
                                                modified_time VARCHAR(50),
                                                tel TEXT,
                                                zipcode VARCHAR(20),
                                                overview TEXT,
                                                source VARCHAR(255) DEFAULT 'http://apis.data.go.kr/B551011/KorService2',
                                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                usetime TEXT,
                                                restdate TEXT,
                                                parking TEXT,
                                                parkingfee VARCHAR(100) DEFAULT '',
                                                infocenter VARCHAR(200) DEFAULT '',
                                                chkbabycarriage VARCHAR(50) DEFAULT '',
                                                chkpet VARCHAR(50) DEFAULT '',
                                                chkcreditcard VARCHAR(50) DEFAULT '',
                                                saleitem TEXT,
                                                saleitemcost VARCHAR(200) DEFAULT '',
                                                fairday VARCHAR(200) DEFAULT '',
                                                opendateshopping VARCHAR(50) DEFAULT '',
                                                shopguide TEXT,
                                                culturecenter VARCHAR(200) DEFAULT '',
                                                restroom VARCHAR(50) DEFAULT '',
                                                scaleshopping VARCHAR(100) DEFAULT '',

                                                INDEX idx_content_type (content_type_id),
                                                INDEX idx_area (area_code, sigungu_code),
                                                INDEX idx_title (title(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 음식점 데이터 테이블 (content_type_id: 39)
CREATE TABLE IF NOT EXISTS fetched_restaurants (
                                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                   content_id VARCHAR(50) NOT NULL UNIQUE,
                                                   content_type_id VARCHAR(10) NOT NULL,
                                                   content_type_name VARCHAR(50) NOT NULL,
                                                   title VARCHAR(500) NOT NULL,
                                                   addr1 VARCHAR(500),
                                                   addr2 VARCHAR(500),
                                                   first_image TEXT,
                                                   first_image2 TEXT,
                                                   map_x VARCHAR(50),
                                                   map_y VARCHAR(50),
                                                   area_code VARCHAR(10),
                                                   sigungu_code VARCHAR(10),
                                                   cat1 VARCHAR(10),
                                                   cat2 VARCHAR(10),
                                                   cat3 VARCHAR(10),
                                                   created_time VARCHAR(50),
                                                   modified_time VARCHAR(50),
                                                   tel TEXT,
                                                   zipcode VARCHAR(20),
                                                   overview TEXT,
                                                   source VARCHAR(255) DEFAULT 'http://apis.data.go.kr/B551011/KorService2',
                                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                   usetime TEXT,
                                                   restdate TEXT,
                                                   parking TEXT,
                                                   parkingfee VARCHAR(100) DEFAULT '',
                                                   infocenter VARCHAR(200) DEFAULT '',
                                                   chkbabycarriage VARCHAR(50) DEFAULT '',
                                                   chkpet VARCHAR(50) DEFAULT '',
                                                   chkcreditcard VARCHAR(50) DEFAULT '',
                                                   seat VARCHAR(50) DEFAULT '',
                                                   kidsfacility VARCHAR(10) DEFAULT '',
                                                   firstmenu TEXT,
                                                   treatmenu TEXT,
                                                   smoking VARCHAR(50) DEFAULT '',
                                                   packing VARCHAR(50) DEFAULT '',
                                                   scalefood VARCHAR(100) DEFAULT '',
                                                   opendatefood VARCHAR(50) DEFAULT '',
                                                   discountinfofood VARCHAR(200) DEFAULT '',
                                                   reservationfood VARCHAR(200) DEFAULT '',
                                                   lcnsno VARCHAR(50) DEFAULT '',

                                                   INDEX idx_content_type (content_type_id),
                                                   INDEX idx_area (area_code, sigungu_code),
                                                   INDEX idx_title (title(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



-- flik_db.fetched_accommodations definition

CREATE TABLE IF NOT EXISTS `fetched_accommodations` (
                                          `id` bigint NOT NULL AUTO_INCREMENT,
                                          `content_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
                                          `content_type_id` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
                                          `content_type_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
                                          `title` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
                                          `addr1` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `addr2` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `first_image` text COLLATE utf8mb4_unicode_ci,
                                          `first_image2` text COLLATE utf8mb4_unicode_ci,
                                          `map_x` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `map_y` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `area_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `sigungu_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `cat1` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `cat2` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `cat3` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `created_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `modified_time` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `tel` text COLLATE utf8mb4_unicode_ci,
                                          `zipcode` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                          `overview` text COLLATE utf8mb4_unicode_ci,
                                          `source` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT 'http://apis.data.go.kr/B551011/KorService2',
                                          `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                          `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          `usetime` text COLLATE utf8mb4_unicode_ci,
                                          `restdate` text COLLATE utf8mb4_unicode_ci,
                                          `parking` text COLLATE utf8mb4_unicode_ci,
                                          `parkingfee` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `infocenter` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `chkbabycarriage` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `chkpet` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `chkcreditcard` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `roomcount` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `roomtype` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `refundregulation` text COLLATE utf8mb4_unicode_ci,
                                          `checkintime` text COLLATE utf8mb4_unicode_ci,
                                          `checkouttime` text COLLATE utf8mb4_unicode_ci,
                                          `chkcooking` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `seminar` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `sports` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `sauna` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `beauty` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `beverage` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `karaoke` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `barbecue` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `campfire` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `bicycle` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `fitness` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `publicpc` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `publicbath` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `subfacility` text COLLATE utf8mb4_unicode_ci,
                                          `foodplace` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `reservationurl` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `pickup` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `reservationlodging` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `scalelodging` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          `accomcountlodging` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '',
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `content_id` (`content_id`),
                                          KEY `idx_content_type` (`content_type_id`),
                                          KEY `idx_area` (`area_code`,`sigungu_code`),
                                          KEY `idx_title` (`title`(100))
) ENGINE=InnoDB AUTO_INCREMENT=126 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;






-- 7. 배치 작업 실행 로그 테이블
CREATE TABLE IF NOT EXISTS batch_execution_log (
                                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                   job_name VARCHAR(100) NOT NULL COMMENT '배치 작업명',
                                                   execution_date DATE NOT NULL COMMENT '실행날짜',
                                                   start_time TIMESTAMP NOT NULL COMMENT '시작시간',
                                                   end_time TIMESTAMP COMMENT '종료시간',
                                                   status ENUM('RUNNING', 'COMPLETED', 'FAILED', 'STOPPED') NOT NULL COMMENT '실행상태',
                                                   total_count INT DEFAULT 0 COMMENT '전체 처리 건수',
                                                   success_count INT DEFAULT 0 COMMENT '성공 건수',
                                                   fail_count INT DEFAULT 0 COMMENT '실패 건수',
                                                   skip_count INT DEFAULT 0 COMMENT '스킵 건수',
                                                   api_call_count INT DEFAULT 0 COMMENT 'API 호출 횟수',
                                                   error_message TEXT COMMENT '에러 메시지',
                                                   execution_params JSON COMMENT '실행 파라미터',
                                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                                   UNIQUE KEY uk_job_date (job_name, execution_date),
                                                   INDEX idx_status (status),
                                                   INDEX idx_execution_date (execution_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='배치 실행 로그';

-- 8. API 호출 제한 관리 테이블 (Redis 백업용)
CREATE TABLE IF NOT EXISTS api_rate_limit_log (
                                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                  api_name VARCHAR(50) NOT NULL COMMENT 'API 명',
                                                  call_date DATE NOT NULL COMMENT '호출날짜',
                                                  call_count INT NOT NULL DEFAULT 0 COMMENT '호출횟수',
                                                  limit_count INT NOT NULL DEFAULT 1000 COMMENT '제한횟수',
                                                  last_call_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '마지막 호출시간',

                                                  UNIQUE KEY uk_api_date (api_name, call_date),
                                                  INDEX idx_call_date (call_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 호출 제한 로그';










ALTER TABLE fetched_tourist_attractions ADD COLUMN google_place_id VARCHAR(255);
ALTER TABLE fetched_tourist_attractions ADD COLUMN google_rating DECIMAL(2,1);
ALTER TABLE fetched_tourist_attractions ADD COLUMN google_review_count INT;
ALTER TABLE fetched_tourist_attractions ADD COLUMN google_reviews TEXT;

ALTER TABLE fetched_restaurants ADD COLUMN google_place_id VARCHAR(255);
ALTER TABLE fetched_restaurants ADD COLUMN google_rating DECIMAL(2,1);
ALTER TABLE fetched_restaurants ADD COLUMN google_review_count INT;
ALTER TABLE fetched_restaurants ADD COLUMN google_reviews TEXT;

ALTER TABLE fetched_sports_recreation ADD COLUMN google_place_id VARCHAR(255);
ALTER TABLE fetched_sports_recreation ADD COLUMN google_rating DECIMAL(2,1);
ALTER TABLE fetched_sports_recreation ADD COLUMN google_review_count INT;
ALTER TABLE fetched_sports_recreation ADD COLUMN google_reviews TEXT;


ALTER TABLE fetched_cultural_facilities ADD COLUMN google_place_id VARCHAR(255);
ALTER TABLE fetched_cultural_facilities ADD COLUMN google_rating DECIMAL(2,1);
ALTER TABLE fetched_cultural_facilities ADD COLUMN google_review_count INT;
ALTER TABLE fetched_cultural_facilities ADD COLUMN google_reviews TEXT;

ALTER TABLE fetched_shopping ADD COLUMN google_place_id VARCHAR(255);
ALTER TABLE fetched_shopping ADD COLUMN google_rating DECIMAL(2,1);
ALTER TABLE fetched_shopping ADD COLUMN google_review_count INT;
ALTER TABLE fetched_shopping ADD COLUMN google_reviews TEXT;

ALTER TABLE fetched_accommodations ADD COLUMN google_place_id VARCHAR(255);
ALTER TABLE fetched_accommodations ADD COLUMN google_rating DECIMAL(2,1);
ALTER TABLE fetched_accommodations ADD COLUMN google_review_count INT;
ALTER TABLE fetched_accommodations ADD COLUMN google_reviews TEXT;