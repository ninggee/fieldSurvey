# 贡献指南

感谢你对FieldSurvey项目的贡献！本文档说明如何正确地贡献代码。

## 开发工作流程

### 1. Fork并克隆项目

```bash
# 克隆项目
git clone https://github.com/your-username/FieldSurvey.git
cd FieldSurvey

# 添加上游仓库
git remote add upstream https://github.com/original-repo/FieldSurvey.git
```

### 2. 创建特性分支

```bash
# 更新主分支
git fetch upstream
git checkout main
git merge upstream/main

# 创建特性分支
git checkout -b feature/your-feature-name
```

### 3. 进行开发

遵循以下代码规范：

#### Kotlin代码规范
- 使用官方Kotlin代码风格（`kotlin.code.style=official`）
- 遵循 [Google Kotlin Style Guide](https://android.github.io/kotlin-guides/style.html)
- 使用有意义的变量和函数名
- 添加必要的KDoc注释

#### Composable函数规范
- 函数名使用PascalCase
- 将`Modifier`作为最后一个参数
- 对复杂的Composables添加`@Preview`注释

示例：
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // ...
}

@Preview
@Composable
fun MyScreenPreview() {
    MyScreen(
        viewModel = MyViewModel(),
        onNavigate = {}
    )
}
```

### 4. 提交代码

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```bash
# 好的提交信息示例
git commit -m "feat: add photo filtering by date"
git commit -m "fix: resolve camera zoom issue"
git commit -m "docs: update README with setup instructions"
git commit -m "style: format code according to Kotlin style guide"
git commit -m "test: add unit tests for ExcelExporter"
git commit -m "refactor: simplify SurveyViewModel logic"

# 提交信息前缀：
# feat:  新功能
# fix:   修复Bug
# docs:  文档更新
# style: 代码格式化
# test:  添加或修改测试
# refactor: 代码重构
# perf:  性能优化
# ci:    CI/CD相关
# chore: 其他（构建、依赖等）
```

### 5. Push并创建Pull Request

```bash
# Push到你的fork
git push origin feature/your-feature-name

# 在GitHub上创建Pull Request
```

**Pull Request模板：**

```markdown
## 描述
简要描述你的更改内容。

## 类型
- [ ] Bug修复
- [ ] 新功能
- [ ] 破坏性更改
- [ ] 文档更新

## 关联Issue
关闭 #123

## 测试方法
描述如何测试你的更改。

## 检查清单
- [ ] 代码遵循项目的代码规范
- [ ] 我已自测此更改
- [ ] 添加了必要的注释
- [ ] 更新了相关文档
- [ ] 没有创建新的警告信息
```

## 代码审查指南

在审查他人代码时：

1. ✅ **要做：**
   - 审查代码逻辑和结构
   - 提出建设性建议
   - 确保代码遵循规范
   - 验证功能是否正确

2. ❌ **不要：**
   - 评论个人风格问题（除非违反规范）
   - 拒绝没有理由的更改
   - 提出超出范围的需求

## Bug报告

发现Bug时，请创建Issue并包含：

```markdown
## Bug描述
清晰简洁的Bug描述。

## 复现步骤
1. ...
2. ...
3. ...

## 预期行为
应该发生什么。

## 实际行为
实际发生了什么。

## 环境信息
- Android版本：
- 设备型号：
- 应用版本：
- 日志输出（如果有）：
```

## 功能请求

提出功能请求时，请说明：

```markdown
## 功能描述
新功能的简要描述。

## 使用场景
为什么需要这个功能？

## 建议的解决方案
你建议如何实现这个功能。

## 替代方案
其他可能的解决方案。
```

## 提交建议

### 好的提交：
- 职责单一
- 有清晰的提交信息
- 通过所有测试
- 包含相关的文档更新

### 避免：
- 一次提交太多改动
- 提交无关联的更改
- 代码注释不足
- 未通过测试的提交

## 开发最佳实践

### 1. 编写可测试的代码

使用依赖注入、解耦模块，便于单元测试。

### 2. 添加测试

为新功能添加相应的单元测试：

```bash
./gradlew test              # 运行单元测试
./gradlew connectedAndroidTest  # 运行集成测试
```

### 3. 更新文档

- 在代码中添加KDoc注释
- 更新README或SETUP文档
- 为复杂逻辑添加解释

### 4. 性能考虑

- 避免在主线程进行重操作
- 使用协程处理异步任务
- 优化数据库查询

### 5. 内存管理

- 及时释放资源
- 使用`remember`优化Composable性能
- 避免内存泄漏

## 版本号规范

遵循 [Semantic Versioning](https://semver.org/):

```
MAJOR.MINOR.PATCH
例如: 1.2.3

- MAJOR: 不兼容的API更改
- MINOR: 新增功能（向后兼容）
- PATCH: Bug修复（向后兼容）
```

## 发布流程

1. 更新版本号（`build.gradle.kts`）
2. 更新CHANGELOG
3. 创建Release分支
4. 生成Release包
5. 创建GitHub Release
6. 发布到应用市场

## 问题排查

### 开发环境问题

如遇到环境问题，请参考：
- [SETUP.md](./SETUP.md) - 环境配置指南
- [README.md](./README.md) - 故障排除部分

### 构建问题

```bash
# 清理构建缓存
./gradlew clean

# 同步依赖
./gradlew sync

# 详细日志
./gradlew build --info
```

## 联系方式

- 📧 Email: [your-email@example.com]
- 💬 讨论: [项目讨论区]
- 🐛 Bug追踪: GitHub Issues

## 许可证

贡献代码即表示你同意你的贡献在项目许可证（MIT/Apache 2.0等）下发布。

---

感谢你的贡献！🎉

