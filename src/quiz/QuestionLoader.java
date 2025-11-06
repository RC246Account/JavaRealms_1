package quiz;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class QuestionLoader {

    private static JSONObject questionsJson;

    static {
        try {
            InputStream is = QuestionLoader.class.getClassLoader().getResourceAsStream("all_questions_chap1.json");
            if (is == null) {
                System.err.println("[QuestionLoader] ERROR: all_questions_chap1.json not found in resources!");
                questionsJson = null;
            } else {
                JSONParser parser = new JSONParser();
                InputStreamReader reader = new InputStreamReader(is);
                questionsJson = (JSONObject) parser.parse(reader);
                System.out.println("[QuestionLoader] Loaded all_questions_chap1.json successfully.");
            }
        } catch (Exception e) {
            System.err.println("[QuestionLoader] ERROR loading questions JSON:");
            e.printStackTrace();
            questionsJson = null;
        }
    }

    public static List<Question> loadQuestions(String chapter, String difficulty) {
        List<Question> questions = new ArrayList<>();

        System.out.println("[QuestionLoader] loadQuestions called: chapter=" + chapter + ", difficulty=" + difficulty);

        if (questionsJson == null) {
            System.err.println("[QuestionLoader] questionsJson is null! Returning fallback question.");
            questions.add(new Question("What is 2 + 2?", List.of("A. 3", "B. 4", "C. 5", "D. 6"), "B. 4"));
            return questions;
        }

        try {
            JSONObject chapterObj = (JSONObject) questionsJson.get("questions");
            if (chapterObj == null) {
                System.err.println("[QuestionLoader] 'questions' key not found in JSON!");
            } else {
                JSONObject chapterData = (JSONObject) chapterObj.get(chapter);
                if (chapterData == null) {
                    System.err.println("[QuestionLoader] Chapter '" + chapter + "' not found in JSON!");
                } else {
                    JSONArray questionArray = (JSONArray) chapterData.get(difficulty);
                    if (questionArray == null) {
                        System.err.println("[QuestionLoader] Difficulty '" + difficulty + "' not found for chapter '" + chapter + "' in JSON!");
                    } else {
                        for (Object obj : questionArray) {
                            JSONObject q = (JSONObject) obj;
                            String questionText = (String) q.get("question");
                            JSONArray choicesArray = (JSONArray) q.get("choices");
                            List<String> choices = new ArrayList<>();
                            for (Object choice : choicesArray) {
                                choices.add((String) choice);
                            }
                            String answer = (String) q.get("answer");
                            questions.add(new Question(questionText, choices, answer));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[QuestionLoader] Exception while parsing questions:");
            e.printStackTrace();
        }
        
        // Fallback: if no questions loaded, add a hardcoded one
        if (questions.isEmpty()) {
            System.err.println("[QuestionLoader] No questions loaded for chapter=" + chapter + ", difficulty=" + difficulty + ". Adding fallback question.");
            questions.add(new Question("What is 2 + 2?", List.of("A. 3", "B. 4", "C. 5", "D. 6"), "B. 4"));
        } else {
            // Shuffle the questions for randomization
            Collections.shuffle(questions);
        }

        System.out.println("[QuestionLoader] Returning " + questions.size() + " question(s).");
        return questions;
    }
}