# 外业调查记录 (FieldSurvey)

一个Android应用，用于现场数据采集和管理，包括拍照、输入信息、导出Excel等功能。

## 功能特性

- 📷 **拍照记录** - 使用相机拍摄现场照片，支持双指捏合缩放
- 📝 **数据输入** - 输入线别、里程、深度等信息
- 📊 **数据管理** - 按日期、里程、线别筛选记录
- 📥 **数据导出** - 导出为Excel格式（包含图片）
- 🎨 **现代UI** - 使用Jetpack Compose构建

## 系统要求

- Java 11 或更高版本
- Android SDK 26+ (minSdk)
- Gradle 8.11.1

## 开发环境设置

### 1. 克隆项目

```bash
git clone <repository-url>
cd FieldSurvey
```

### 2. 安装依赖

项目使用Gradle进行依赖管理，首次构建时会自动下载所有依赖。

### 3. 生成签名密钥（Release打包）

为了进行Release打包，需要生成签名密钥。使用以下命令在项目根目录生成keystore文件：

```bash
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias fieldsurvey-key -storepass fieldsurvey123 -keypass fieldsurvey123 -dname "CN=FieldSurvey,O=FieldSurvey,C=CN"
```

**重要**: `release.keystore` 文件已添加到 `.gitignore` 中，不会被提交到版本控制。每个开发人员需要生成自己的keystore文件或从团队安全存储中获取。

### 4. 编译项目

#### Debug模式（开发测试）
```bash
./gradlew clean build
```

#### Release模式（发布）
```bash
./gradlew clean bundleRelease
```

输出文件位置：
- **APK**: `app/build/outputs/apk/release/app-release.apk`
- **AAB**: `app/build/outputs/bundle/release/app-release.aab`

## 项目结构

```
FieldSurvey/
├── app/                          # 应用模块
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/fieldsurvey/
│   │   │   │   ├── MainActivity.kt          # 主活动
│   │   │   │   ├── ui/
│   │   │   │   │   ├── RecordScreens.kt    # 拍照和列表页面
│   │   │   │   │   ├── CameraCapture.kt    # 相机功能
│   │   │   │   │   ├── SurveyViewModel.kt  # 数据管理
│   │   │   │   │   └── theme/              # UI主题
│   │   │   │   ├── data/
│   │   │   │   │   ├── dao/                # 数据库访问
│   │   │   │   │   └── database/           # 数据库配置
│   │   │   │   └── export/
│   │   │   │       └── ExcelExporter.kt    # Excel导出
│   │   │   └── res/                        # 资源文件
│   │   ├── test/                           # 单元测试
│   │   └── androidTest/                    # 仪器测试
│   ├── build.gradle.kts                    # 应用构建配置
│   └── proguard-rules.pro
├── gradle/                       # Gradle包装器
├── build.gradle.kts             # 根项目构建配置
├── settings.gradle.kts          # 项目设置
├── gradle.properties            # Gradle属性
├── local.properties             # 本地开发属性（不提交）
├── .gitignore
└── README.md

```

## 主要依赖

- **Jetpack Compose** - UI框架
- **CameraX** - 相机功能
- **Room** - 本地数据库
- **Coil** - 图片加载
- **Apache POI** - Excel导出
- **Material Design 3** - UI设计系统

## 构建配置

### 编译SDK版本
- compileSdk: 35
- targetSdk: 35
- minSdk: 26

### Java/Kotlin版本
- sourceCompatibility: Java 11
- targetCompatibility: Java 11
- jvmTarget: 11

## 开发工作流程

1. **创建分支**
   ```bash
   git checkout -b feature/your-feature
   ```

2. **进行开发**

3. **测试**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

4. **提交代码**
   ```bash
   git add .
   git commit -m "Add your feature"
   git push origin feature/your-feature
   ```

5. **创建Pull Request**

## 注意事项

- ⚠️ **不要**将 `release.keystore` 提交到Git仓库
- ⚠️ **不要**将 `local.properties` 提交到Git仓库
- ✅ **确保** `.gradle/` 和 `build/` 目录在 `.gitignore` 中
- 每个开发环境需要自己的 `local.properties` 文件

## 故障排除

### 构建失败

1. 清除缓存
   ```bash
   ./gradlew clean
   ```

2. 重新同步Gradle
   ```bash
   ./gradlew --refresh-dependencies
   ```

3. 检查SDK版本
   - 确保安装了SDK 35

### 运行时错误

- 检查 `AndroidManifest.xml` 中的权限声明
- 确保已授予相机和存储权限

## 许可证

[添加你的许可证]

## 联系方式

[添加联系方式]

