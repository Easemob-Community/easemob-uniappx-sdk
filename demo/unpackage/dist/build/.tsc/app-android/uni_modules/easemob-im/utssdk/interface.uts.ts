/**
 * 环信IM SDK - 简化版接口定义
 */

// 回调类型
export type SuccessCallback = () => void
export type FailCallback = (code: number, msg: string) => void
export type MessageCallback = (msg: any) => void
