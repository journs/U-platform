package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.example.api.*;
import org.example.model.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    // 全局变量，控制是否自动提交
    private static final boolean AUTO_SUBMIT = true;

    public static void main(String[] args) {
        HttpService httpService = new HttpService();
        LoginModule loginModule = new LoginModule(httpService);
        DoubaoApiClient doubaoClient = new DoubaoApiClient(httpService); // 新增豆包客户端

        // 从环境变量中获取API密钥
        String apiKey = "6c9adf2d-daec-xxxx-xxx9";

        // 创建ArkService实例
        ArkService arkService = ArkService.builder().apiKey(apiKey).build();

        // 1. 检查本地有效用户信息
        UserInfo userInfo = loginModule.getUserInfo("账号");

        if (userInfo == null) {
            System.out.println("userinfo为空");
            // 2. 执行登录（首次或Token失效）
            userInfo = loginModule.login("账号", "密码");
        }

        // 3. 使用用户信息
        System.out.println("登录用户: " + userInfo.getName());
        System.out.println("Access Token: " + userInfo.getAccessToken());

        CoursesStudy coursesStudy = new CoursesStudy(httpService, loginModule);
        try (Scanner scanner = new Scanner(System.in)) {
            String coursesResponse = coursesStudy.fetchCourses();
            JsonArray courseArray = coursesStudy.parseCourseData(coursesResponse);
            if (courseArray != null) {
                System.out.print("输入课程索引（0-" + (courseArray.size() - 1) + "）: ");
                int courseIndex = scanner.nextInt();
                String courseId = getCourseIdByIndex(courseArray, courseIndex);
                courseArray.get(courseIndex).getAsJsonObject();
                if (courseId != null) {
                    HomeworkFetcher homeworkFetcher = new HomeworkFetcher(httpService, loginModule);
                    String homeworkResponse = homeworkFetcher.fetchPublishedHomeworks(courseId);
                    JsonArray homeworkArray = homeworkFetcher.parseHomeworkData(homeworkResponse);
                    if (homeworkArray != null) {
                        System.out.print("输入作业索引（0-" + (homeworkArray.size() - 1) + "）: ");
                        int homeworkIndex = scanner.nextInt();
                        JsonObject homework = getHomeworkIdByIndex(homeworkArray, homeworkIndex);
                        if (homework != null) {
                            String homeworkPublishId = homework.get("homeworkId").getAsString();
                            String SubmitHomeworkPublishId = homework.get("id").getAsString();
                            System.out.println("你选择的作业 ID 是: " + homeworkPublishId);
                            QuestionFetcher questionFetcher = new QuestionFetcher(httpService, loginModule);

                            // 获取题目列表（假设 homeworkQuestionsResponse 包含题目详情）
                            String questionsResponse = questionFetcher.fetchHomeworkQuestionsResponse(
                                    homework,
                                    courseId
                            );
                            JsonArray questionArray = new Gson().fromJson(questionsResponse, JsonObject.class).getAsJsonArray("data");

                            for (int i = 0; i < questionArray.size(); i++) {
                                JsonObject question = questionArray.get(i).getAsJsonObject();
                                String questionId = question.get("id").getAsString();

                                // 获取题目详情
                                QuestionDetailFetcher detailFetcher = new QuestionDetailFetcher(httpService, loginModule);
                                String detailResponse = detailFetcher.fetchQuestionDetail(
                                        questionId,
                                        courseId,
                                        homeworkPublishId
                                );
                                JsonObject detailData = new Gson().fromJson(detailResponse, JsonObject.class).getAsJsonObject("data");

                                // 提取关键信息
                                String titleText = detailData.get("titleText").getAsString();
                                JsonArray options = detailData.getAsJsonArray("options");
                                int qsnType = detailData.get("qsnType").getAsInt();
                                String existingUserAnswer = detailData.has("userAnswer") ? detailData.get("userAnswer").getAsString() : null;

                                System.out.println("题目：" + titleText);
                                System.out.println("选项：" + options);
                                System.out.println("题目类型：" + qsnType);

                                // 判断是否已有答案，有则跳过AI搜题
                                /*
                                 if (existingUserAnswer != null) {
                                    System.out.println("已有答案，跳过AI搜题");
                                    continue;
                                }
                                 */

                                String userAnswer = "";
                                switch (qsnType) {
                                    case 1: // 单选题
                                        userAnswer = getAnswerFromAi(titleText, options, arkService, 1);
                                        break;
                                    case 2: // 多选题
                                        userAnswer = getAnswerFromAi(titleText, options, arkService, 2);
                                        break;
                                    case 3: // 判断题
                                        String aiResultForJudge = getAnswerFromAi(titleText, options, arkService, 3);
                                        userAnswer = aiResultForJudge.equalsIgnoreCase("正确") ? "true" : "false";
                                        break;
                                    case 6: // 填空题
                                        String aiResultForFill = getAnswerFromAi(titleText, null, arkService, 6);
                                        userAnswer = "<p>" + aiResultForFill + "</p>";
                                        break;
                                    default:
                                        System.out.println("不支持的题目类型：" + qsnType);
                                        continue;
                                }

                                // 提交答案
                                if (!userAnswer.isEmpty()) {
                                    AnswerSubmitter submitter = new AnswerSubmitter(httpService, loginModule);
                                    String submitResponse = submitter.submitAnswer(
                                            questionId,
                                            userAnswer,
                                            courseId,
                                            homeworkPublishId
                                    );
                                    System.out.println("答案提交响应：" + submitResponse);
                                }
                            }

                            // 根据全局变量决定是否自动提交作业
                            if (AUTO_SUBMIT) {
                                HomeworkAnswerSubmitter homeworkAnswerSubmitter = new HomeworkAnswerSubmitter(httpService, loginModule);
                                String submitResponse = homeworkAnswerSubmitter.submitAnswer(SubmitHomeworkPublishId);
                                System.out.println("作业提交响应：" + submitResponse);
                            }
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            System.err.println("JSON 解析错误: " + e.getMessage());
        }
    }

    /**
     * 向 AI 提问并获取纯答案（根据题目类型返回不同格式）
     */
    private static String getAnswerFromAi(String titleText, JsonArray options, ArkService arkService, int qsnType) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        // 根据题目类型设置不同的系统提示
        switch (qsnType) {
            case 1:
                chatMessages.add(ChatMessage.builder()
                        .role(ChatMessageRole.SYSTEM)
                        .content("你是一个严格遵循格式的助手，只返回单个选项字母（如：A），无需任何解释。")
                        .build());
                break;
            case 2:
                chatMessages.add(ChatMessage.builder()
                        .role(ChatMessageRole.SYSTEM)
                        .content("你是一个严格遵循格式的助手，返回所有正确选项的字母组合（如：ABC），无需任何解释。")
                        .build());
                break;
            case 3:
                chatMessages.add(ChatMessage.builder()
                        .role(ChatMessageRole.SYSTEM)
                        .content("你是一个严格遵循格式的助手，对于判断题，只返回“正确”或“错误”。")
                        .build());
                break;
            case 6:
                chatMessages.add(ChatMessage.builder()
                        .role(ChatMessageRole.SYSTEM)
                        .content("你是一个严格遵循格式的助手，对于填空题，直接返回答案内容，禁止添加任何引号、括号或解释性文字。")
                        .build());
                break;
            default:
                chatMessages.add(ChatMessage.builder()
                        .role(ChatMessageRole.SYSTEM)
                        .content("你是一个严格遵循格式的助手，只返回选项字母（如：A），无需任何解释。")
                        .build());
        }

        StringBuilder userContent = new StringBuilder(stripHtml(titleText));
        if (options != null) {
            for (int j = 0; j < options.size(); j++) {
                JsonObject option = options.get(j).getAsJsonObject();
                userContent.append("\n").append(option.get("id").getAsString()).append(") ").append(stripHtml(option.get("optionContent").getAsString()));
            }
        }

        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(userContent.toString())
                .build();
        chatMessages.add(userMessage);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("deepseek-r1-250120") // 替换为有效的模型 ID
                .messages(chatMessages)
                .build();

        try {
            String answer = arkService.createChatCompletion(request)
                    .getChoices()
                    .stream()
                    .map(choice -> {
                        Object content = choice.getMessage().getContent();
                        return extractAnswerLetter(content, qsnType);
                    })
                    .findFirst()
                    .orElse("");

            System.out.println("AI 答案：" + answer);
            return answer;
        } catch (Exception e) {
            System.err.println("AI 请求失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 从 AI 响应中提取纯选项字母（如从 "答案：A" 提取 "A"）
     */
    private static String extractAnswerLetter(Object content, int qsnType) {
        // 处理 null 值
        if (content == null) {
            return "";
        }
        String contentStr = content.toString().trim();
        switch (qsnType) {
            case 1: // 单选题
            case 2: // 多选题
                return contentStr.replaceAll("[^A-Za-z]", "").toUpperCase();
            case 3: // 判断题
                return contentStr;
            case 6: // 填空题
                contentStr = contentStr.replaceAll("^[\"“”']|['\"“”]$", ""); // 去除首尾引号
                contentStr = contentStr.replaceAll("\\s+", " "); // 压缩连续空格
                return contentStr;
            default:
                return contentStr.replaceAll("[^A-Za-z]", "").toUpperCase();
        }
    }

    // 原有辅助方法（去除 HTML 标签、索引校验等）保持不变
    private static String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", "");
    }

    private static String getCourseIdByIndex(JsonArray array, int index) {
        if (isValidIndex(index, array)) {
            JsonObject course = array.get(index).getAsJsonObject();
            return course.get("id").getAsString();
        } else {
            System.out.println("无效索引！");
            return null;
        }
    }

    private static JsonObject getHomeworkIdByIndex(JsonArray array, int index) {
        if (isValidIndex(index, array)) {
            JsonObject homework = array.get(index).getAsJsonObject();
            return homework;
        } else {
            System.out.println("无效索引！");
            return null;
        }
    }

    private static boolean isValidIndex(int index, JsonArray array) {
        return index >= 0 && index < array.size();
    }
}