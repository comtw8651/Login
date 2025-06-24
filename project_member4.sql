-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- 主機： 127.0.0.1
-- 產生時間： 2025-06-24 04:35:19
-- 伺服器版本： 10.4.32-MariaDB
-- PHP 版本： 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 資料庫： `project_member`
--

-- --------------------------------------------------------

--
-- 資料表結構 `member`
--

CREATE TABLE `member` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `coin` bigint(20) NOT NULL DEFAULT 0,
  `current_theme` varchar(255) NOT NULL DEFAULT 'default'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `member`
--

INSERT INTO `member` (`id`, `email`, `password`, `username`, `coin`, `current_theme`) VALUES
(11, 'nonameve6@gmail.com', '$2a$10$wyUq4/t64Wnvm6TOtsAHu.kYnd8dGieJ/Cn/RF6Yy.yibPbhncnKC', 'brad', 0, 'default'),
(15, 'asd60661144@gmail.com', '$2a$10$BunS6DVdkou2IXjQsTnWO.f4zH2Zro/.xlefK9TwtI1c5PdMI4x86', 'CSC-Andrew', 0, 'default'),
(19, 'comtw8651@gmail.com', 'google', '狗狗之二', 0, 'default'),
(22, 'comtw8651@yahoo.com.tw', '$2a$10$qQlb9HRSuYDvrFTP.RDSs.po0hBjEFzel7FcuvSUFfOKF2p/SXkmK', 'dog', 9700, 'default');

-- --------------------------------------------------------

--
-- 資料表結構 `reset_password_tokens`
--

CREATE TABLE `reset_password_tokens` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) NOT NULL,
  `token` varchar(255) NOT NULL,
  `expiry_date` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `themes`
--

CREATE TABLE `themes` (
  `id` bigint(20) NOT NULL,
  `theme_name` varchar(50) NOT NULL,
  `display_name` varchar(50) NOT NULL,
  `price` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `themes`
--

INSERT INTO `themes` (`id`, `theme_name`, `display_name`, `price`, `created_at`) VALUES
(1, 'green', '森林綠', 50, '2025-06-13 06:30:57'),
(2, 'purple', '夢幻紫', 80, '2025-06-13 06:30:57'),
(3, 'gold', '奢華金', 150, '2025-06-13 06:30:57'),
(4, 'dark', '墨黑', 10, '2025-06-13 06:43:41'),
(5, 'blue', '海洋藍', 10, '2025-06-13 06:43:41'),
(6, 'default', '預設風格', 0, '2025-06-13 06:45:00');

-- --------------------------------------------------------

--
-- 資料表結構 `user_themes`
--

CREATE TABLE `user_themes` (
  `id` int(11) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `theme_id` bigint(20) NOT NULL,
  `purchased_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `user_themes`
--

INSERT INTO `user_themes` (`id`, `user_id`, `theme_id`, `purchased_at`) VALUES
(10, 22, 6, '2025-06-19 02:35:29'),
(11, 22, 1, '2025-06-19 06:50:42'),
(12, 22, 2, '2025-06-19 06:50:43'),
(13, 22, 3, '2025-06-19 06:50:44'),
(14, 22, 4, '2025-06-19 06:50:46'),
(15, 22, 5, '2025-06-19 06:50:48');

-- --------------------------------------------------------

--
-- 資料表結構 `verification_code`
--

CREATE TABLE `verification_code` (
  `email` varchar(100) NOT NULL,
  `code` varchar(10) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 已傾印資料表的索引
--

--
-- 資料表索引 `member`
--
ALTER TABLE `member`
  ADD PRIMARY KEY (`id`);

--
-- 資料表索引 `reset_password_tokens`
--
ALTER TABLE `reset_password_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- 資料表索引 `themes`
--
ALTER TABLE `themes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `theme_name` (`theme_name`);

--
-- 資料表索引 `user_themes`
--
ALTER TABLE `user_themes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`,`theme_id`),
  ADD KEY `theme_id` (`theme_id`);

--
-- 資料表索引 `verification_code`
--
ALTER TABLE `verification_code`
  ADD PRIMARY KEY (`email`);

--
-- 在傾印的資料表使用自動遞增(AUTO_INCREMENT)
--

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `member`
--
ALTER TABLE `member`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `reset_password_tokens`
--
ALTER TABLE `reset_password_tokens`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `themes`
--
ALTER TABLE `themes`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `user_themes`
--
ALTER TABLE `user_themes`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- 已傾印資料表的限制式
--

--
-- 資料表的限制式 `user_themes`
--
ALTER TABLE `user_themes`
  ADD CONSTRAINT `user_themes_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `member` (`id`),
  ADD CONSTRAINT `user_themes_ibfk_2` FOREIGN KEY (`theme_id`) REFERENCES `themes` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
