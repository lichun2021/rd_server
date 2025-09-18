CREATE TABLE `activity_deep_treasure`
(
    `id`                varchar(64) COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '',
    `playerId`          varchar(64) COLLATE utf8mb4_unicode_ci  NOT NULL DEFAULT '',
    `termId`            int                                     NOT NULL DEFAULT 0,
    `achieveItems`      text COLLATE utf8mb4_unicode_ci         NOT NULL,
    `nineBoxStr`        text COLLATE utf8mb4_unicode_ci         NOT NULL,
    `loginDays`         varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '',
    `nextFree`          bigint                                  NOT NULL DEFAULT 0,
    `purchaseItemTimes` bigint                                  NOT NULL DEFAULT 0,
    `exchangeMsg`       text COLLATE utf8mb4_unicode_ci         NOT NULL,
    `refreshtimes`      bigint                                  NOT NULL DEFAULT 0,
    `lottoryCount`      int                                     NOT NULL DEFAULT 0,
    `createTime`        bigint                                  NOT NULL DEFAULT 0,
    `updateTime`        bigint                                  NOT NULL DEFAULT 0,
    `invalid`           tinyint(1) NOT NULL DEFAULT 0,
    `lotteryBuff`       text COLLATE utf8mb4_unicode_ci         NOT NULL,
    PRIMARY KEY (`id`),
    KEY `playerId_index` (`playerId`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;