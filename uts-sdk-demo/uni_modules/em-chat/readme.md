# em-chat UTS插件集成 环信安卓SDK 示例 （自定义基座运行）

## 前提

注册环信账号，创建自己的应用 https://console.easemob.com/

## 代码示例

index.nvue 文件

```javascript 

<template>
	<view class="content">
		<image class="logo" src="/static/logo.png"></image>
		<view class="text-area">
			<text class="title">{{title}}</text>
		</view>

		<view class="opt-wrap">
			<button type="primary" class="btn" @click="init">
				初始化SDK
			</button>

			<button class="btn" @click="createAccount">
				注册
			</button>

			<button class="btn" @click="login">
				登录
			</button>

			<button class="btn" @click="sendMessage">
				发送消息
			</button>

			<button type="warn" class="btn" @click="logout">
				登出
			</button>
		</view>
	</view>
</template>

<script lang="uts">
	import { initChat, loginEM, logoutEM, sendTextMessage, createUser } from "../../uni_modules/em-chat"
	export default {
		data() {
			return {
				title: 'Eeasemob Android SDK',
			}
		},
		onLoad() {

		},
		methods: {
			init() {
				initChat('你的环信AppKey')
			},
			createAccount() {
				createUser('uniappx', '123')
			},
			login() {
				loginEM('sttest', '123');
			},
			logout() {
				logoutEM();
			},
			sendMessage() {
				sendTextMessage('你好啊', 'yjj');
			}
		}
	}
</script>

<style>
	.content {
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.logo {
		height: 200rpx;
		width: 200rpx;
		margin-top: 200rpx;
		margin-bottom: 50rpx;
	}

	.opt-wrap {
		width: 100%;
	}

	.title {
		font-size: 36rpx;
		color: #8f8f94;
	}

	.btn {
		margin-top: 20rpx;
	}
</style>

```

### 开发文档
[UTS 语法](https://uniapp.dcloud.net.cn/tutorial/syntax-uts.html)
[UTS API插件](https://uniapp.dcloud.net.cn/plugin/uts-plugin.html)
[UTS 组件插件](https://uniapp.dcloud.net.cn/plugin/uts-component.html)
[Hello UTS](https://gitcode.net/dcloud/hello-uts)