以下是优化后的 README.md 文档，对内容进行了结构调整、信息补充和格式优化，使其更清晰易读：

# U+ 教育平台作业自动化处理系统

## 项目概述
本项目是针对 U+ 教育平台开发的作业自动化处理系统，能够帮助用户自动登录教育平台，获取课程列表、作业列表以及题目详情。同时，借助 AI 服务获取题目答案，并自动提交作业答案。系统支持多种常见题型，如单选题、多选题、判断题和填空题。

## 功能特性
1. **自动登录**：用户可使用指定的手机号和密码自动登录 U+ 教育平台。
2. **课程与作业获取**：系统能够获取用户的课程列表，并展示指定课程的作业列表。
3. **题目详情获取**：可以获取作业中每个题目的详细信息，包括题目内容、选项和题目类型。
4. **AI 辅助答题**：利用 AI 服务为用户提供题目的答案。
5. **答案提交**：系统会自动提交每个题目的答案，还可根据设置自动提交整个作业。

## 代码结构

### 主要类及其功能
1. **`Main` 类**：作为程序的入口，负责整个流程的控制，涵盖登录操作、获取课程和作业信息、调用 AI 进行答题以及提交答案等功能。
2. **`CoursesStudy` 类**：用于获取课程列表，并对课程数据进行解析。
3. **`HomeworkFetcher` 类**：获取作业列表，并解析作业数据。
4. **`QuestionFetcher` 类**：获取作业中的题目列表。
5. **`QuestionDetailFetcher` 类**：获取题目的详细信息。
6. **`AnswerSubmitter` 类**：提交单个题目的答案。
7. **`HomeworkAnswerSubmitter` 类**：提交整个作业的答案。
8. **`RsaEncryptor` 类**：模拟前端的 RSA 公钥加密。

### 辅助类及其功能
1. **`HttpService` 类**：处理 HTTP 请求和响应。
2. **`LoginModule` 类**：负责用户登录以及处理登录相关的操作。
3. **`ArkService` 类**：与 AI 服务进行交互。

## 安装和配置

### 依赖安装
本项目使用 Maven 进行依赖管理。请确保你已经安装了 Maven，然后在项目根目录下运行以下命令来安装依赖：
```bash
mvn clean install
```

### 配置 API 密钥
在 `Main` 类中，你需要配置 AI 服务的 API 密钥。你可以通过以下步骤获取 API 密钥：
1. 注册/登录火山引擎账户：[https://console.volcengine.com/user/basics/](https://console.volcengine.com/user/basics/)
2. 获取 API Key：[https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey?apikey={}](https://console.volcengine.com/ark/region:ark+cn-beijing/apiKey?apikey={})

获取到 API 密钥后，将其替换代码中的占位符：
```java
// 从环境变量中获取API密钥
String apiKey = "your-actual-api-key";
```

### 对接 DeepSeek、豆包模型
在 `pom.xml` 文件中添加以下依赖：
```xml
<dependency>
  <groupId>com.volcengine</groupId>
  <artifactId>volcengine-java-sdk-ark-runtime</artifactId>
  <version>LATEST</version>
</dependency>
```

### 配置用户信息
在 `Main` 类中，你需要配置用户的手机号和密码：
```java
UserInfo userInfo = loginModule.getUserInfo("your-phone-number");
if (userInfo == null) {
    userInfo = loginModule.login("your-phone-number", "your-password");
}
```
请将 `your-phone-number` 和 `your-password` 替换为你自己的有效信息。

## 使用方法
1. 确保你已经完成了上述的安装和配置步骤。
2. 在项目根目录下运行以下命令来启动程序：
```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```
3. 程序将自动登录教育平台，并显示课程列表。输入你要选择的课程索引，然后按回车键。
4. 程序将显示所选课程的作业列表。输入你要选择的作业索引，然后按回车键。
5. 程序将获取作业中的题目列表，并依次显示每个题目的详细信息和 AI 答案。如果有答案，程序将自动提交答案。
6. 根据全局变量 `AUTO_SUBMIT` 的设置，程序将决定是否自动提交整个作业。

## 贡献指南
如果你想为这个项目做出贡献，请遵循以下步骤：
1. Fork 这个项目。
2. 创建一个新的分支：`git checkout -b feature/your-feature-name`。
3. 提交你的更改：`git commit -m 'Add some feature'`。
4. 推送你的分支：`git push origin feature/your-feature-name`。
5. 打开一个 Pull Request。

## 许可证
本项目采用 [MIT 许可证](LICENSE)。

## 注意事项
- 请确保你使用的 API 密钥和用户信息是有效的。
- 本项目中的 AI 模型 ID `deepseek-r1-250120` 需要替换为你自己的有效模型 ID。
- 请遵守教育平台的使用规则，不要滥用本系统进行不正当的行为。 