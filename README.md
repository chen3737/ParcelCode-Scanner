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
└── settings.gradle.kts                # 项目设置
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

## 安装和运行

### 环境要求
- Android Studio Giraffe 或更高版本
- Android SDK 34
- Java 17

### 构建步骤
1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接Android设备或启动模拟器
5. 点击运行按钮（或按Shift+F10）

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

- 项目主页：https://github.com/yourusername/mashangqujian
- 问题反馈：请使用 GitHub Issues
- 功能建议：欢迎提交 Pull Request 或 Issue