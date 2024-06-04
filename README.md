# JCode - Simple IDE based on Swing

JCode 是一个以 Java 语言编写, 完全基于 Swing 图形用户界面框架的简单 IDE. 目前, JCode 支持以下功能:

- [x] 文本文件的多窗口编辑
- [x] 集成终端模拟器 (支持 Unix shell 或 Windows cmd)
- [ ] 自动补全
- [ ] 查找替换
- [x] 语法高亮
- [x] 打开目录及目录预览
- [ ] 自定义字体、主题颜色、快捷键
- [ ] 集成 compiler、debugger
- [ ] 集成 git
- [ ] 外文字符支持

## Usage

如果支持 GNU make, 可以运行
```
make run
```
如果不支持 make, 则可以手动编译运行:
```
javac Main.java
java Main
```
