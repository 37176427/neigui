# neigui
为了五黑玩LOL极地大乱斗时增加些乐趣 每局可以选出一个内鬼
内鬼负责带输队伍同时不被发现
其他人正常玩的同时猜谁是内鬼

规则：
1 胜利时 平民每人+1 内鬼-4
  失败时 平民每人-1 内鬼+4 （简单好记）
2 评分最低的-1给评分最高的+1 （为了防止演的太过分 且鼓励carry的玩家惩罚摆烂的玩家）
3 投中内鬼+1同时内鬼-1 内鬼投票仅迷惑平民用 无实际意义 （防止内鬼不演了 当然也可以选择开局明牌）

然后发红包

接口：
1 注册一个身份 会返回这局你是不是内鬼 name后接你想要的昵称
/neigui/whoami?name=   
2 重开下一盘游戏 只有上局第一个注册身份的人有权限 防止其他人调用
/neigui/restart
3 查看上局内鬼 用于重开后核实上局内鬼是谁
/neigui/who
4 查看当前局用户和其序号 (内容在其他接口也有)
/neigui/list
5 内鬼投票 投出你认为的内鬼 (必须所有人都投票过后 管理者才可以重开下一盘)
/neigui/vote?neigui=
6 获取规则说明
/neigui/rule