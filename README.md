# 欢迎访问本项目!

示例站点：https://shadowsocks-share.herokuapp.com   

本项目不具备代理及账号管理能力。只负责汇集目标站点信息，便于大家查看、订阅。账号来之不易，请支持源站点。

> **注意：**
> - 项目仅限编程学习、讨论使用，**请在适用法律允许的范围内使用**；
> - 项目提供的信息，来自于其他共享站点，无法保证内容的准确性、可靠性、可用性、安全性；
> - 关于高可用性：项目发布于免费的 Heroku 云平台，受限于免费时间有限，项目可能会自动休眠或免费时间耗尽，无法提供稳定服务；
> - 关于捐赠：项目只是收集信息而非服务提供者，顾不接受捐赠。请有能力的朋友直接支持源站点的发展。


## 使用说明

### Windows

1. [下载](https://github.com/shadowsocksr-backup/shadowsocksr-csharp/releases)客户端，安装、启动
1. 鼠标右键客户端任务栏图标
1. 服务器订阅 -> SSR 服务器订阅设置
1. 添加订阅地址 -> 确定
1. 服务器订阅 -> 更新订阅

### Android

1. [下载](https://github.com/shadowsocksrr/shadowsocksr-android/releases)客户端，安装、启动
1. 添加订阅地址
1. 确定并升级

> **注意：**
> - 账号状态分为“有效”、“无效”两种：
>   - 有效：获取账号或定时检查可用性时，应用所在服务器到 SS 服务，网络可用
>   - 无效：获取账号或定时检查可用性时，应用所在服务器到 SS 服务，网络异常
> - 此状态为瞬时（而非实时）的应用服务器与 SS 服务间的网络状态。不具备准确性。请自行选择订阅有效或是全部。
> - 随机 JSON 订阅：随机返回一条有效状态的账号信息


## 系统简介

### 工作流程如下

1. 项目启动后扫描一次目标站点信息
1. 信息保存至数据库
1. 前台展示、订阅信息
1. 定时扫描目标站点并更新数据[（扫描间隔如下）](#收集信息网站列表如下)
1. 每小时检测所有账号可用性，并更新账号状态

### 收集信息网站列表如下

地址 | 启动时扫描 | 扫描间隔
---- | ---- | ----
https://global.ishadowx.net/ | 是 | 从 0 点 10 分开始，每 3 小时
https://doub.io/sszhfx/ | 是 | 从 0 点 10 分开始，每 6 小时
https://freess.cx/ | 是 | 从 0 点 10 分开始，每 12 小时
https://en.ss8.fun/ | 是 | 从 0 点 10 分开始，每 4 小时
https://freessr.win/ | 是 | 从 0 点 10 分开始，每 6 小时
https://free-ss.site/ | 是 | 从 0 点 10 分开始，每 3 小时
https://www.52ssr.cn/ | 是 | 从 0 点 10 分开始，每 3 小时
https://free.yitianjianss.com/ | 是 | 从 0 点 10 分开始，每 3 小时
https://cloudfra.com/ | 是 | 从 0 点 10 分开始，每 3 小时


## 系统部署

### 项目部署（推荐）

1. 运行环境：JAVA 8 或更高版本（[官方下载](http://www.oracle.com/technetwork/java/javase/downloads/index.html)）
1. [下载](https://github.com/zc-zh-001/ShadowSocks-Share/releases)最新包，并解压
1. 软件配置（一般不需要修改。配置文件路径：config\application-dev.yml）
1. 执行 run.bat （Windows）
1. 浏览器访问：[http://localhost:8080](http://localhost:8080)
1. 初次扫描约 3 分钟，出现 “初始扫描完成...”即可使用

> **注意：**
> - 项目会自动打开 Edge 请勿手动关闭，读取数据后，系统会自动关闭。
> - 项目启动后新增产两个文件夹：data（数据文件）、logs（日志文件），都可以删除。

### Heroku 部署方法

请参考：
ShadowSocksShare-OpenShift [Heroku 部署方法](https://github.com/the0demiurge/ShadowSocksShare-OpenShift#heroku-%E9%83%A8%E7%BD%B2%E6%96%B9%E6%B3%95) 

### 软件配置

系统有多种参数配置方式，在此介绍配置文件、系统环境变量两种：
1. 优先级：两者配置了相同参数时，系统环境变量优先级高于配置文件
1. Heroku 配置环境变量：Settings > Config Variables > Config Vars
1. 配置文件路径：[/src/main/resources/application-prod.yml（YAML 格式）](https://github.com/zc-zh-001/ShadowSocks-Share/blob/master/src/main/resources/application-prod.yml)
1. Heroku 部署时需要配置PROXY_FREE-SS_ENABLE、PROXY_FREE-SS_HOST、PROXY_FREE-SS_PORT、PROXY_FREE-SS_SOCKS（自行寻找可以访问 free-ss 的代理） 及 HEALTH_URL（自己的项目地址）

### 配置参数说明

类型 | 环境变量 | 配置文件 | 参数类型 | 默认值 | 说明
---- | ---- | ---- | ---- | ---- | ----
访问代理 | PROXY_ENABLE | proxy.enable | boolean | false | 访问目标网站是否启动代理（除 free-ss）
访问代理 | PROXY_HOST | proxy.host | string | | 启动代理时（proxy.enable为true），配置代理IP
访问代理 | PROXY_PORT | proxy.port | int | | 启动代理时（proxy.enable为true），配置代理端口
free-ss 代理 | PROXY_FREE-SS_ENABLE | proxy.free-ss.enable | boolean | true | 部分云服务器无法访问 free-ss，需要开启代理访问
free-ss 代理 | PROXY_FREE-SS_HOST | proxy.free-ss.host | string | | 访问 free-ss 代理IP
free-ss 代理 | PROXY_FREE-SS_PORT | proxy.free-ss.port | int | | 访问 free-ss 端口
free-ss 代理 | PROXY_FREE-SS_SOCKS | proxy.free-ss.socks | boolean | false | 是否为 socks 代理

> **注意：**
> - Heroku 免费资源，内存太少，限制太多，造成部分网站账号抓取失败，推荐本地运行项目。
> - 为避免爬虫太多对源站点产生影响及账号安全。**本站提供的服务能够满足需求时，请避免自行部署项目。**


## TO DO

1. 计划支持下列站点：
    1. https://github.com/max2max/freess/wiki/%E5%85%8D%E8%B4%B9ss%E8%B4%A6%E5%8F%B7