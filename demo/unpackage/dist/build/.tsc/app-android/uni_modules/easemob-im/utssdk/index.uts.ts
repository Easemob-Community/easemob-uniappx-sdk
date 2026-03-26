/**
 * 环信IM SDK - 简化版入口
 */


import {
  init as androidInit,
  login as androidLogin,
  logout as androidLogout,
  sendText as androidSendText,
  onMessage as androidOnMessage,
  isLoggedIn as androidIsLoggedIn,
  getVersion as androidGetVersion
} from './app-android/index.uts'














// 导出API
export const im = {
  /**
   * 初始化
   */
  init: (appKey: string): boolean => {

    return androidInit(appKey)




    return false
  },
  
  /**
   * 登录
   */
  login: (username: string, password: string, onSuccess?: any, onFail?: any): void => {

    androidLogin(username, password, onSuccess, onFail)




  },
  
  /**
   * 登出
   */
  logout: (onSuccess?: any): void => {

    androidLogout(onSuccess)




  },
  
  /**
   * 发送文本消息
   */
  sendText: (to: string, content: string, onSuccess?: any, onFail?: any): void => {

    androidSendText(to, content, onSuccess, onFail)




  },
  
  /**
   * 监听消息
   */
  onMessage: (listener: any): void => {

    androidOnMessage(listener)




  },
  
  /**
   * 是否已登录
   */
  isLoggedIn: (): boolean => {

    return androidIsLoggedIn()




    return false
  },
  
  /**
   * 获取版本
   */
  getVersion: (): string => {

    return androidGetVersion()




    return ''
  }
}

export default { im }
