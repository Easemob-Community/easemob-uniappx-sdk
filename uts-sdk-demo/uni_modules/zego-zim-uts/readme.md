# zego-zim-uts

### 开发文档

#### Native App 开发
- 制作自定义基座

#### Web、小程序开发
- 切换到项目 uni_modules/zego-zim-uts 目录下，安装 npm sdk 库，安装成功后可以在该目录下看到 node_modules 目录
``` bash
cd uni_modules/zego-zim-uts/
npm run install
```

#### 使用 SDK
``` typescript
// <script>
import { ZIM, ZIMAppConfig, ZIMLoginConfig, ZIMMessage, ZIMMessageSendConfig, ZIMMessageSendNotification, ZIMConversationQueryConfig, ZIMMessageQueryConfig } from '@/uni_modules/zego-zim-uts';

export default {
    onShow() {
        this.useZIM();
    },
    methods: {
        useZIM() {
            // 创建实例
            const appConfig: ZIMAppConfig = { appID: 0, appSign: '' };
            ZIM.create(appConfig);
            const zim = ZIM.getInstance();
            
            // 注册回调事件
            zim.onConnectionStateChanged((data) => console.log(`state: ${data.state}, event: ${data.event}`)));
            
            // 登录
            const userID = '';
            const loginConfig: ZIMLoginConfig = { token: '', userName: '', customStatus: '', isOfflineLogin: false };
            zim.login(userID, loginConfig)
                .then(() => {
                    // 操作成功
                })
                .catch((err) => {
                    // 操作失败
                    console.error(err);
                });
                
            // 发送消息
            const sendConfig: ZIMMessageSendConfig = { priority: 1 };
            
            const notification: ZIMMessageSendNotification = {
                onMessageAttached: (msg) => {},
            };
            
            const textMsg: ZIMMessage = {
                type: 1,
                message: 'test',
            };
            
            zim.sendMessage(textMsg, 'toUserID', 0, sendConfig, notification)
                .then((res) => {
                    // 操作成功
                })
                .catch((err) => {
                    // 操作失败
                    console.error(err);
                });
            
            // 分页查询会话列表
            zim.queryConversationList({ count: 1, nextConversation: null } as ZIMConversationQueryConfig, null).then((res) => {
                console.log('queryConversationList-1', res);
                const conv = res.conversationList[res.conversationList.length - 1];
                const nextFlag = conv.type.toString() + conv.conversationID;
                zim.queryConversationList({ count: 1, nextConversation: nextFlag } as ZIMConversationQueryConfig, null).then((res) => {
                    console.log('queryConversationList-2', res);  
                });             
            });
                  
            // 分页查询历史消息  
            zim.queryHistoryMessage(用户ID, 0, { count: 1, reverse: true, nextMessage: null } as ZIMMessageQueryConfig).then((res) => {
                console.log('queryHistoryMessage-1', res);
                const nextFlag = res.messageList[0]?.localMessageID;
                zim.queryHistoryMessage(用户ID, 0, { count: 1, reverse: true, nextMessage: nextFlag } as ZIMMessageQueryConfig).then((res) => {
                    console.log('queryHistoryMessage-2', res);  
                });                
            });
        }
    }
}
// </script>
```

### 参考文档

- [UTS 语法](https://uniapp.dcloud.net.cn/tutorial/syntax-uts.html)
- [UTS API插件](https://uniapp.dcloud.net.cn/plugin/uts-plugin.html)
