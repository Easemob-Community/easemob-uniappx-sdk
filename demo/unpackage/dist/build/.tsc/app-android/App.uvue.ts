
import { initChat } from '@/uni_modules/easemob-unappx-sdk'


let firstBackTime = 0


const __sfc__ = defineApp({
	onLaunch() {
		console.log('App Launch')
		
		// 初始化环信 SDK
		this.initEasemobSDK()
	},
	onShow() {
		console.log('App Show')
	},
	onHide() {
		console.log('App Hide')
	},

	onLastPageBackPress() {
		console.log('App LastPageBackPress')
		if (firstBackTime == 0) {
			uni.showToast({
				title: '再按一次退出应用',
				position: 'bottom',
			})
			firstBackTime = Date.now()
			setTimeout(() => {
				firstBackTime = 0
			}, 2000)
		} else if (Date.now() - firstBackTime < 2000) {
			firstBackTime = Date.now()
			uni.exit()
		}
	},

	onExit() {
		console.log('App Exit')
	},
	methods: {
		/**
		 * 初始化环信 SDK
		 */
		initEasemobSDK() {
			console.log('[App] 开始初始化环信 SDK...')
			
			initChat({
				appKey: 'easemob-demo#support',
				autoLogin: false,
				debugMode: true,
				success: (res) => {
					console.log('[App] 环信 SDK 初始化成功:', JSON.stringify(res))
					uni.showToast({
						title: 'SDK 初始化成功',
						icon: 'success',
						duration: 2000
					})
				},
				fail: (err) => {
					console.error('[App] 环信 SDK 初始化失败:', JSON.stringify(err))
					uni.showToast({
						title: 'SDK 初始化失败',
						icon: 'none',
						duration: 2000
					})
				}
			})
		}
	}
})

export default __sfc__
const GenAppStyles = [_uM([["uni-row", _pS(_uM([["flexDirection", "row"]]))], ["uni-column", _pS(_uM([["flexDirection", "column"]]))]])]
