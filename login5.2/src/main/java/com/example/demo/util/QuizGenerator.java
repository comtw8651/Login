package com.example.demo.util;

import com.example.demo.entity.Quiz;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ✅ QuizGenerator：從影片字幕文字自動產生選擇題（非 GPT），
 * 利用關鍵字替換方式產生干擾選項，模擬出選擇題結構。
 */
public class QuizGenerator {

    /**
     * ✅ 從字幕內容中產生指定數量的選擇題
     *
     * @param transcript 字幕內容（文字已斷句，例如抓自 YouTube 字幕）
     * @param videoId 對應影片的 ID（會轉成字串存入 Quiz 中）
     * @param num 產生題目的數量（例如：5 題）
     * @return 回傳包含 Quiz 題目的 List
     */
    public static List<Quiz> generateMultipleChoice(String transcript, int videoId, int num) {
        List<Quiz> result = new ArrayList<>();             // ✅ 最終回傳的題目列表
        Set<String> usedSentences = new HashSet<>();       // ✅ 已用過的正確句子，避免重複出題

        // ✅ 先依照中文的句號/驚嘆號/問號斷句
        String[] sentences = transcript.split("[。！？]");

        List<String> candidates = new ArrayList<>();
        for (String s : sentences) {
            s = s.trim(); // 去除前後空白
            // 條件：句子長度 > 20 且包含中文字元
            if (s.length() > 20 && Pattern.compile("[\u4e00-\u9fa5]").matcher(s).find()) {
                candidates.add(s); // 加入候選清單
            }
        }

        Random random = new Random();
        int attempts = 0; // 最多嘗試 100 次，避免死循環

        // ✅ 進入出題迴圈，直到達到 num 題或超過嘗試次數限制
        while (result.size() < num && attempts++ < 100) {
            if (candidates.isEmpty()) break;

            // 隨機選一個句子當作正解
            String correct = candidates.get(random.nextInt(candidates.size()));
            if (usedSentences.contains(correct)) continue; // 避免重複出題

            // ✅ 製造錯誤選項（干擾選項）：替換 Java 主題中的關鍵詞
            String fake1 = correct.replaceFirst("Java", "Python");
            String fake2 = correct.replaceFirst("JDK", "JRE");
            String fake3 = correct.replaceFirst("VS ?Code", "Notepad");

            // ✅ 利用 Set 確保所有選項唯一
            Set<String> optionsSet = new LinkedHashSet<>(Arrays.asList(correct, fake1, fake2, fake3));
            if (optionsSet.size() < 4) continue; // 若選項不夠 4 個不同則略過

            // ✅ 洗牌選項順序
            List<String> options = new ArrayList<>(optionsSet);
            Collections.shuffle(options);

            int correctIndex = options.indexOf(correct);
            if (correctIndex == -1) continue;

            // ✅ 建立 Quiz 題目物件
            Quiz quiz = new Quiz();
            quiz.setVideoId(Long.valueOf(videoId));
            quiz.setQuestion("下列哪一項敘述正確？"); // 固定題目

            // 設定選項（注意 index 對應）
            quiz.setOption1(options.get(0));
            quiz.setOption2(options.get(1));
            quiz.setOption3(options.get(2));
            quiz.setOption4(options.get(3));

            quiz.setCorrectIndex(correctIndex + 1); // ✔️ 資料庫使用 1-based（1 ~ 4）
            quiz.setExplanation("根據影片內容，正確敘述為：「" + correct + "」");

            result.add(quiz);               // 加入結果
            usedSentences.add(correct);     // 標記此句已使用
        }

        return result;
    }

    /**
     * ✅ 提供簡易使用版本（預設產生 5 題）
     *
     * @param transcript 字幕文字
     * @param videoId 對應影片 ID
     * @return 回傳 5 題選擇題
     */
    public static List<Quiz> generateFromText(String transcript, int videoId) {
        return generateMultipleChoice(transcript, videoId, 5);
    }
}
