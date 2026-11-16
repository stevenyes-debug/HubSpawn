# HubSpawn

玩家进服时自动传送至指定大厅位置。管理员站在目标地点执行 `/sethubspawn`；死亡后的重生仍由游戏处理。

## 安装

使用 Minecraft 1.21.11、Fabric Loader 和 Java 21。将 Release 中的 `hubspawn-1.0.1.jar` 放入服务端 `mods/` 并重启。同时安装匹配的 Fabric API。

## 构建

在仓库根目录运行 `gradle build`，输出位于 `build/libs/`。
