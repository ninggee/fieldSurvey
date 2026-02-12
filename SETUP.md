# 开发环境初始化指南

本文档详细说明如何在新的电脑上完整地初始化和编译本项目。

## 前置条件检查

### 1. 安装Java 11+

```bash
java -version
```

如果未安装，请从 [Oracle官网](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) 或 [OpenJDK](https://adoptopenjdk.net/) 下载安装。

### 2. 安装Android SDK

方式一：使用Android Studio（推荐）
- 下载 [Android Studio](https://developer.android.com/studio)
- 安装过程中会自动安装SDK

方式二：仅安装SDK
- 下载 [Android SDK Command-line Tools](https://developer.android.com/studio#command-tools)
- 设置 `ANDROID_SDK_ROOT` 环境变量

### 3. 检查SDK版本

```bash
# 列出已安装的SDK
${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --list
```

确保安装了 SDK 35：

```bash
${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager "platforms;android-35"
```

## 新电脑初始化步骤

### Step 1: 克隆项目

```bash
git clone <your-repository-url>
cd FieldSurvey
```

### Step 2: 配置本地开发环境

创建 `local.properties` 文件（如果不存在）：

```bash
# Windows
echo sdk.dir=C:\Users\YourUsername\AppData\Local\Android\Sdk > local.properties

# macOS/Linux
echo sdk.dir=$HOME/Library/Android/sdk > local.properties
```

或者手动创建 `local.properties` 文件，内容为：

```properties
sdk.dir=/path/to/Android/Sdk
```

### Step 3: 生成签名密钥（Release打包）

如果需要进行Release打包，需要生成签名密钥。在项目根目录执行：

**Windows:**
```batch
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias fieldsurvey-key -storepass fieldsurvey123 -keypass fieldsurvey123 -dname "CN=FieldSurvey,O=FieldSurvey,C=CN"
```

**macOS/Linux:**
```bash
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias fieldsurvey-key -storepass fieldsurvey123 -keypass fieldsurvey123 -dname "CN=FieldSurvey,O=FieldSurvey,C=CN"
```

**重要**: 
- `release.keystore` 已添加到 `.gitignore`，不会上传到Git
- 每个开发人员需要自己生成keystore
- 如果有团队共用的keystore，请从安全存储中获取（如加密的团队驱动）

### Step 4: 验证环境

```bash
# 检查Gradle版本
./gradlew --version

# 同步Gradle依赖
./gradlew sync
```

### Step 5: 构建项目

**Debug构建（用于开发和测试）:**
```bash
./gradlew clean build
```

**Release构建（用于发布）:**
```bash
./gradlew clean bundleRelease
```

### Step 6: 在模拟器或真机上测试

```bash
./gradlew installDebug
```

## 自动化脚本

### Windows批处理脚本 (setup.bat)

创建 `setup.bat` 文件在项目根目录：

```batch
@echo off
REM 设置Android SDK路径（根据实际路径修改）
set ANDROID_SDK_ROOT=%USERPROFILE%\AppData\Local\Android\Sdk

REM 创建local.properties
(
  echo sdk.dir=%ANDROID_SDK_ROOT%
) > local.properties

REM 验证Java
java -version || (
  echo Error: Java not found. Please install Java 11 or higher.
  exit /b 1
)

REM 下载并同步依赖
call gradlew sync

REM 清理并构建
call gradlew clean build

echo Setup completed successfully!
```

### Bash脚本 (setup.sh)

创建 `setup.sh` 文件在项目根目录：

```bash
#!/bin/bash

# 设置Android SDK路径
export ANDROID_SDK_ROOT=$HOME/Library/Android/Sdk

# 创建local.properties
cat > local.properties << EOF
sdk.dir=$ANDROID_SDK_ROOT
EOF

# 检查Java
java -version || {
  echo "Error: Java not found. Please install Java 11 or higher."
  exit 1
}

# 下载并同步依赖
./gradlew sync

# 清理并构建
./gradlew clean build

echo "Setup completed successfully!"
```

## 常见问题

### Q: 缺少Android SDK
```
错误: Could not determine the path to the Android SDK
```

**解决方案:**
1. 安装Android Studio或Android SDK
2. 创建 `local.properties` 文件并设置 `sdk.dir` 路径
3. 重新运行构建

### Q: Java版本不匹配
```
错误: java version "1.8" does not match JDK 11
```

**解决方案:**
1. 安装Java 11+
2. 设置 `JAVA_HOME` 环境变量
3. 验证: `java -version` 应显示Java 11+

### Q: Gradle同步失败
```
错误: Failed to resolve dependency
```

**解决方案:**
```bash
./gradlew --refresh-dependencies clean sync
```

### Q: 编译失败 - minSdkVersion问题
```
错误: Increase the minSdkVersion to 26 or above
```

**解决方案:** 
已在 `build.gradle.kts` 中设置为 `minSdk = 26`，如仍有问题，请清理并重建：
```bash
./gradlew clean build
```

## Release打包完整流程

### 1. 确保有keystore文件

```bash
# 如果没有，生成新的
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias fieldsurvey-key -storepass fieldsurvey123 -keypass fieldsurvey123 -dname "CN=FieldSurvey,O=FieldSurvey,C=CN"
```

### 2. 更新版本号

编辑 `app/build.gradle.kts`：

```kotlin
defaultConfig {
    versionCode = 2          // 增加此数字
    versionName = "1.1"      // 更新版本名
    // ...
}
```

### 3. 构建Release AAB

```bash
./gradlew clean bundleRelease
```

输出位置: `app/build/outputs/bundle/release/app-release.aab`

### 4. 上传到Google Play（可选）

使用 [Android App Bundle Upload](https://play.google.com/console) 或其他发布渠道。

## 环境变量配置

为了方便快速设置，建议配置以下环境变量：

### Windows

1. 打开 "环境变量" 设置
2. 添加新的系统变量：
   - `ANDROID_SDK_ROOT`: `C:\Users\YourUsername\AppData\Local\Android\Sdk`
   - `JAVA_HOME`: `C:\Program Files\Java\jdk-11` (或你的Java安装路径)

### macOS/Linux

编辑 `~/.bash_profile` 或 `~/.zshrc`：

```bash
export ANDROID_SDK_ROOT=$HOME/Library/Android/Sdk
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export PATH=$PATH:$ANDROID_SDK_ROOT/tools:$ANDROID_SDK_ROOT/platform-tools
```

然后执行: `source ~/.bash_profile`

## 验证安装成功

运行以下命令确认所有环境已正确配置：

```bash
# 1. 检查Java
java -version

# 2. 检查Gradle
./gradlew --version

# 3. 检查Android SDK
${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager --list_installed

# 4. 构建项目
./gradlew clean build
```

如果所有命令都执行成功，说明开发环境已正确初始化。

## 获取帮助

如遇到问题，请：

1. 查看 [README.md](./README.md)
2. 检查 [故障排除](#常见问题) 部分
3. 查看Gradle详细日志: `./gradlew build --info`
4. 检查Android SDK是否完整安装

---

祝编码愉快！

