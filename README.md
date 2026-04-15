# 码上取件 - Android应用

一个自动识别取件码短信并整合管理的Android应用。

## 功能特点

- **自动识别**：自动扫描短信中的取件码和地址信息
- **智能分类**：按快递公司、时间等分类管理取件记录
- **手动输入**：支持复制粘贴短信内容进行识别
- **一键复制**：快速复制取件码到剪贴板
- **状态管理**：标记取件状态（已取件/未取件）
- **搜索功能**：按取件码、地址、快递公司搜索
- **自定义规则**：支持添加自定义的取件码识别规则
- **数据导出**：支持分享和导出取件记录

## 技术栈

- **语言**：Kotlin
- **UI框架**：Jetpack Compose
- **数据库**：Room
- **架构模式**：MVVM
- **依赖注入**：Dagger Hilt（规划中）
- **权限管理**：Android权限系统

## 环境信息

### 开发环境要求
- **Android Studio**: Giraffe (2022.3.1) 或更高版本
- **Android SDK**: 34 (Android 14)
- **Java版本**: 17
- **Gradle版本**: 8.3
- **Kotlin版本**: 1.9.20

### 运行环境要求
- **最小Android版本**: API 24 (Android 7.0 Nougat)
- **目标Android版本**: API 34 (Android 14)
- **编译Android版本**: API 34 (Android 14)

### 项目配置
- **应用包名**: `com.mashangqujian`
- **版本号**: 1.0
- **版本代码**: 1
- **支持架构**: ARM, ARM64, x86, x86_64

## 依赖信息

### 核心依赖

#### Android基础库
```kotlin
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
implementation("androidx.activity:activity-compose:1.8.2")
implementation("androidx.activity:activity-ktx:1.8.2")
implementation("androidx.fragment:fragment-ktx:1.6.2")
```

#### Jetpack Compose UI框架
```kotlin
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-graphics")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
```

#### 数据库和存储
```kotlin
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")
```

#### 架构组件
```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
implementation("androidx.navigation:navigation-compose:2.7.6")
```

#### 协程
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

### 测试依赖
```kotlin
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-tooling")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

### 插件配置
- **Android应用插件**: `com.android.application` version `8.2.0`
- **Kotlin插件**: `org.jetbrains.kotlin.android` version `1.9.20`
- **KSP插件**: `com.google.devtools.ksp` version `1.9.20-1.0.14`

## 项目结构

```
码上取件/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mashangqujian/
│   │   │   │   ├── data/              # 数据层
│   │   │   │   │   ├── model/         # 数据模型
│   │   │   │   │   ├── dao/           # 数据访问对象
│   │   │   │   │   └── database/      # 数据库配置
│   │   │   │   ├── sms/               # 短信处理
│   │   │   │   │   ├── SMSParser.kt   # 短信解析器
│   │   │   │   │   └── SMSReader.kt   # 短信读取器
│   │   │   │   ├── ui/                # 界面层
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── MainViewModel.kt
│   │   │   │   │   ├── components/    # UI组件
│   │   │   │   │   └── theme/         # 主题样式
│   │   │   │   └── service/           # 后台服务（规划中）
│   │   │   ├── res/                   # 资源文件
│   │   │   │   └── values/            # 字符串等资源
│   │   │   └── AndroidManifest.xml    # 应用配置文件
│   └── build.gradle.kts               # 应用构建配置
├── build.gradle.kts                   # 项目构建配置
├── gradle.properties                  # Gradle属性配置
├── gradle/wrapper/                    # Gradle包装器
│   └── gradle-wrapper.properties      # Gradle版本配置
├── gradlew                           # Linux/macOS Gradle包装器脚本
├── gradlew.bat                       # Windows Gradle包装器脚本
├── local.properties                  # 本地属性配置
├── settings.gradle.kts               # 项目设置
└── README.md                         # 项目说明文档
```

## 核心模块

### 1. 短信解析器 (SMSParser)
- 使用正则表达式识别不同快递公司的取件码格式
- 支持顺丰、京东、中通、圆通、韵达、菜鸟驿站、邮政等主流快递
- 可扩展的自定义规则系统

### 2. 短信读取器 (SMSReader)
- 读取系统短信内容
- 支持按日期筛选短信
- 权限管理和错误处理

### 3. 数据模型 (Parcel)
```kotlin
@Entity(tableName = "parcels")
data class Parcel(
    val parcelCode: String,      // 取件码
    val address: String,         // 取件地址
    val courierCompany: String,  // 快递公司
    val smsContent: String,      // 原始短信内容
    val smsDate: Long,           // 短信日期
    val isCollected: Boolean = false // 是否已取件
)
```

### 4. 用户界面
- **主界面**：取件记录列表，按状态分组
- **权限提示**：友好的权限申请界面
- **详情页面**：取件记录详情和操作
- **设置页面**：应用配置和自定义规则

## 构建和开发

### 环境设置
1. 确保已安装 **Java 17** 或更高版本
2. 安装 **Android Studio Giraffe** 或更高版本
3. 配置 **Android SDK 34**
4. 克隆项目到本地：
   ```bash
   git clone https://github.com/chen3737/ParcelCode-Scanner.git
   cd ParcelCode-Scanner
   ```

### 构建项目
1. 使用Android Studio打开项目
2. 等待Gradle同步完成（自动下载依赖）
3. 可选：修改配置
   - `gradle.properties`: JVM参数和Gradle配置
   - `local.properties`: 本地SDK路径（Android Studio自动生成）

### 运行应用
1. 连接Android设备或启动模拟器（API 24+）
2. 点击运行按钮（或按 `Shift+F10`）
3. 首次运行会自动下载Gradle依赖

### Gradle配置详情

#### 项目级配置 (`build.gradle.kts`)
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

#### 应用级配置 (`app/build.gradle.kts`)
- 编译SDK: 34
- 最小SDK: 24
- 目标SDK: 34
- Java版本: 17
- Kotlin JVM目标: 17
- Compose编译器版本: 1.5.4

#### Gradle包装器 (`gradle/wrapper/gradle-wrapper.properties`)
- Gradle版本: 8.3
- 分发URL: https://services.gradle.org/distributions/gradle-8.3-bin.zip

### 权限配置
应用需要以下权限：
- `READ_SMS`：读取短信内容
- `RECEIVE_SMS`：接收新短信通知
- `INTERNET`：网络访问（用于后续功能扩展）

## 开发计划

### 已完成
- [x] 项目基础架构
- [x] 数据库设计
- [x] 短信解析器
- [x] 短信读取器
- [x] 主界面UI
- [x] 权限管理

### 待完成
- [ ] 手动输入对话框
- [ ] 设置页面
- [ ] 自定义规则管理
- [ ] 后台短信监控服务
- [ ] 通知提醒
- [ ] 数据导出功能
- [ ] 单元测试
- [ ] 应用图标和主题美化

## 常见问题解决

### 1. Gradle同步失败
- 检查网络连接
- 确认Gradle版本兼容性
- 清理项目并重新同步：`File > Invalidate Caches and Restart`

### 2. 依赖下载缓慢
- 配置国内镜像源（如阿里云镜像）
- 修改 `build.gradle.kts` 中的仓库地址

### 3. 编译错误
- 确认Java版本为17
- 检查Android SDK路径配置
- 清理构建：`./gradlew clean`

### 4. 权限申请问题
- 确保在AndroidManifest.xml中声明了所需权限
- 运行时权限需要在Android 6.0+上动态申请

## 测试短信示例

### 顺丰
```
【顺丰速运】您的快件已到达XXX快递柜，取件码：123456，请及时取件。
```

### 京东
```
【京东物流】您的包裹已到达XXX京东快递站，取件码：7890，请凭码取件。
```

### 菜鸟驿站
```
【菜鸟驿站】取件码：123-4567，请到XXX小区菜鸟驿站取件。
```

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目主页：https://github.com/chen3737/ParcelCode-Scanner
- 问题反馈：请使用 GitHub Issues
- 功能建议：欢迎提交 Pull Request 或 Issue