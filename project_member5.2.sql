-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- 主機： 127.0.0.1
-- 產生時間： 2025-06-30 07:54:10
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
-- 資料表結構 `answer`
--

CREATE TABLE `answer` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `quiz_id` bigint(20) DEFAULT NULL,
  `selected_option` int(11) DEFAULT NULL,
  `is_correct` tinyint(1) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `answered_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `source` varchar(10) DEFAULT 'local',
  `video_id` bigint(20) DEFAULT NULL,
  `attempt_id` bigint(20) NOT NULL,
  `submitted_at` datetime DEFAULT NULL,
  `answer_index` int(11) DEFAULT NULL,
  `answer_text` text DEFAULT NULL,
  `difficulty` varchar(10) DEFAULT NULL,
  `explanation` text DEFAULT NULL,
  `option1` text DEFAULT NULL,
  `option2` text DEFAULT NULL,
  `option3` text DEFAULT NULL,
  `option4` text DEFAULT NULL,
  `question` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- 傾印資料表的資料 `answer`
--

INSERT INTO `answer` (`id`, `user_id`, `quiz_id`, `selected_option`, `is_correct`, `created_at`, `answered_at`, `source`, `video_id`, `attempt_id`, `submitted_at`, `answer_index`, `answer_text`, `difficulty`, `explanation`, `option1`, `option2`, `option3`, `option4`, `question`) VALUES
(496, 1, 767, 0, 0, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(497, 1, 768, 1, 0, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(498, 1, 769, 2, 0, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(499, 1, 770, 1, 1, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(500, 1, 771, 0, 0, '2025-06-21 22:34:54', '2025-06-21 22:34:54', 'local', 1, 1750574094424, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(501, 1, 782, 0, 0, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(502, 1, 783, 1, 0, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(503, 1, 784, 2, 1, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(504, 1, 785, 1, 0, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(505, 1, 786, 0, 0, '2025-06-21 22:38:14', '2025-06-21 22:38:14', 'local', 2, 1750574294917, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- 資料表結構 `chat_messages`
--

CREATE TABLE `chat_messages` (
  `id` bigint(20) NOT NULL,
  `message` text NOT NULL,
  `role` enum('ASSISTANT','USER') NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `member_id` bigint(20) NOT NULL,
  `session_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `chat_sessions`
--

CREATE TABLE `chat_sessions` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `title` varchar(100) DEFAULT NULL,
  `member_id` bigint(20) NOT NULL,
  `video_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `members`
--

CREATE TABLE `members` (
  `id` bigint(20) NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `coin` bigint(20) NOT NULL DEFAULT 0,
  `current_theme` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'default'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- 傾印資料表的資料 `members`
--

INSERT INTO `members` (`id`, `email`, `password`, `username`, `coin`, `current_theme`) VALUES
(1, 'test@example.com', '123456', 'test', 0, 'default'),
(11, 'nonameve6@gmail.com', '$2a$10$wyUq4/t64Wnvm6TOtsAHu.kYnd8dGieJ/Cn/RF6Yy.yibPbhncnKC', 'brad', 0, 'default'),
(15, 'asd60661144@gmail.com', '$2a$10$BunS6DVdkou2IXjQsTnWO.f4zH2Zro/.xlefK9TwtI1c5PdMI4x86', 'CSC-Andrew', 0, 'default'),
(19, 'comtw8651@gmail.com', 'google', '狗狗之二', 0, 'default'),
(23, 'comtw8651@yahoo.com.tw', '$2a$10$3XrvGdICSxTmNwZl/3FqGeXIcullSqOx1EbQQOjtPTKZtqV61wp/C', 'dog', 90, 'default');

-- --------------------------------------------------------

--
-- 資料表結構 `quiz`
--

CREATE TABLE `quiz` (
  `quiz_id` bigint(20) NOT NULL,
  `correct_index` int(11) DEFAULT NULL,
  `difficulty` varchar(10) DEFAULT NULL,
  `explanation` text DEFAULT NULL,
  `option1` text DEFAULT NULL,
  `option2` text DEFAULT NULL,
  `option3` text DEFAULT NULL,
  `option4` text DEFAULT NULL,
  `question` text DEFAULT NULL,
  `source` varchar(10) DEFAULT NULL,
  `type` enum('single','multi','text') DEFAULT NULL,
  `video_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `quiz_results`
--

CREATE TABLE `quiz_results` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `video_id` bigint(20) NOT NULL,
  `total_questions` int(11) DEFAULT NULL,
  `correct_answers` int(11) DEFAULT NULL,
  `accuracy` decimal(5,2) DEFAULT NULL,
  `submitted_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `source` varchar(50) NOT NULL DEFAULT 'local',
  `attempt_id` bigint(20) NOT NULL,
  `difficulty` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- 傾印資料表的資料 `quiz_results`
--

INSERT INTO `quiz_results` (`id`, `user_id`, `video_id`, `total_questions`, `correct_answers`, `accuracy`, `submitted_at`, `source`, `attempt_id`, `difficulty`) VALUES
(40, 1, 1, 5, 1, 20.00, '2025-06-21 22:34:54', 'local', 1750574094424, NULL),
(41, 1, 2, 5, 1, 20.00, '2025-06-21 22:38:14', 'local', 1750574294917, NULL);

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
  `theme_name` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
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
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `theme_id` bigint(20) NOT NULL,
  `purchased_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 傾印資料表的資料 `user_themes`
--

INSERT INTO `user_themes` (`id`, `user_id`, `theme_id`, `purchased_at`) VALUES
(17, 23, 5, '2025-06-25 07:45:34');

-- --------------------------------------------------------

--
-- 資料表結構 `verification_code`
--

CREATE TABLE `verification_code` (
  `email` varchar(100) NOT NULL,
  `code` varchar(10) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `videos`
--

CREATE TABLE `videos` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text DEFAULT NULL,
  `duration_seconds` int(11) DEFAULT NULL,
  `published_at` datetime(6) DEFAULT NULL,
  `subject` varchar(100) DEFAULT NULL,
  `thumbnail_url` text DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `video_url` varchar(500) DEFAULT NULL,
  `youtube_id` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `video_session_links`
--

CREATE TABLE `video_session_links` (
  `id` bigint(20) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `end_time_seconds` int(11) DEFAULT NULL,
  `last_viewed_time_seconds` int(11) DEFAULT NULL,
  `start_time_seconds` int(11) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `session_id` bigint(20) NOT NULL,
  `video_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- 資料表結構 `watch_progress`
--

CREATE TABLE `watch_progress` (
  `id` bigint(20) NOT NULL,
  `current_time_sec` int(11) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `video_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- 已傾印資料表的索引
--

--
-- 資料表索引 `answer`
--
ALTER TABLE `answer`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `quiz_id` (`quiz_id`);

--
-- 資料表索引 `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK7a2wawc6k2qfgy936egdy32wl` (`member_id`),
  ADD KEY `FK3cpkdtwdxndrjhrx3gt9q5ux9` (`session_id`);

--
-- 資料表索引 `chat_sessions`
--
ALTER TABLE `chat_sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKj4unxb93q7ctlha6qa5a9rpgi` (`member_id`),
  ADD KEY `FKmntetc288q1nkqhjdgi7wlhtp` (`video_id`);

--
-- 資料表索引 `members`
--
ALTER TABLE `members`
  ADD PRIMARY KEY (`id`);

--
-- 資料表索引 `quiz`
--
ALTER TABLE `quiz`
  ADD PRIMARY KEY (`quiz_id`);

--
-- 資料表索引 `quiz_results`
--
ALTER TABLE `quiz_results`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

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
  ADD UNIQUE KEY `user_id` (`user_id`,`theme_id`) USING BTREE,
  ADD KEY `theme_id` (`theme_id`);

--
-- 資料表索引 `verification_code`
--
ALTER TABLE `verification_code`
  ADD PRIMARY KEY (`email`);

--
-- 資料表索引 `videos`
--
ALTER TABLE `videos`
  ADD PRIMARY KEY (`id`);

--
-- 資料表索引 `video_session_links`
--
ALTER TABLE `video_session_links`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FKmwq1ngrig3dw0mpgdmsmjkq83` (`session_id`),
  ADD KEY `FK2t3qy9c88jke6r42asl0e924w` (`video_id`);

--
-- 資料表索引 `watch_progress`
--
ALTER TABLE `watch_progress`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UKkbc7510yij53hq2bt7dcd7dnv` (`user_id`,`video_id`),
  ADD KEY `FK87rd1859t7ak0s5af9jjqycxa` (`video_id`);

--
-- 在傾印的資料表使用自動遞增(AUTO_INCREMENT)
--

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `answer`
--
ALTER TABLE `answer`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=506;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `chat_messages`
--
ALTER TABLE `chat_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `chat_sessions`
--
ALTER TABLE `chat_sessions`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `quiz`
--
ALTER TABLE `quiz`
  MODIFY `quiz_id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `quiz_results`
--
ALTER TABLE `quiz_results`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

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
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `videos`
--
ALTER TABLE `videos`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `video_session_links`
--
ALTER TABLE `video_session_links`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `watch_progress`
--
ALTER TABLE `watch_progress`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 已傾印資料表的限制式
--

--
-- 資料表的限制式 `answer`
--
ALTER TABLE `answer`
  ADD CONSTRAINT `fk_answer_user_id` FOREIGN KEY (`user_id`) REFERENCES `members` (`id`);

--
-- 資料表的限制式 `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD CONSTRAINT `FK3cpkdtwdxndrjhrx3gt9q5ux9` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`),
  ADD CONSTRAINT `FK7a2wawc6k2qfgy936egdy32wl` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`);

--
-- 資料表的限制式 `chat_sessions`
--
ALTER TABLE `chat_sessions`
  ADD CONSTRAINT `FKj4unxb93q7ctlha6qa5a9rpgi` FOREIGN KEY (`member_id`) REFERENCES `members` (`id`),
  ADD CONSTRAINT `FKmntetc288q1nkqhjdgi7wlhtp` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`);

--
-- 資料表的限制式 `quiz_results`
--
ALTER TABLE `quiz_results`
  ADD CONSTRAINT `fk_quiz_results_user_id` FOREIGN KEY (`user_id`) REFERENCES `members` (`id`);

--
-- 資料表的限制式 `user_themes`
--
ALTER TABLE `user_themes`
  ADD CONSTRAINT `fk_user_id_members_id` FOREIGN KEY (`user_id`) REFERENCES `members` (`id`),
  ADD CONSTRAINT `user_themes_ibfk_2` FOREIGN KEY (`theme_id`) REFERENCES `themes` (`id`);

--
-- 資料表的限制式 `video_session_links`
--
ALTER TABLE `video_session_links`
  ADD CONSTRAINT `FK2t3qy9c88jke6r42asl0e924w` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`),
  ADD CONSTRAINT `FKmwq1ngrig3dw0mpgdmsmjkq83` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions` (`id`);

--
-- 資料表的限制式 `watch_progress`
--
ALTER TABLE `watch_progress`
  ADD CONSTRAINT `FK87rd1859t7ak0s5af9jjqycxa` FOREIGN KEY (`video_id`) REFERENCES `videos` (`id`),
  ADD CONSTRAINT `FKen3690gvb8dw44u1duk2w9g8w` FOREIGN KEY (`user_id`) REFERENCES `members` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
