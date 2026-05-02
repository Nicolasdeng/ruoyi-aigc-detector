/**
 * 全局请求拦截器
 * 功能：
 * 1. 自动添加Token到请求头
 * 2. 统一处理Token过期
 * 3. 统一处理错误响应
 * 4. 自动显示Loading和错误提示
 */

import config from '@/config/config.js'
import { getToken, clearUserInfo } from '@/utils/auth.js'

/**
 * 发起HTTP请求
 * @param {Object} options 请求配置
 * @returns {Promise} 请求Promise
 */
function request(options) {
	return new Promise((resolve, reject) => {
		// 显示Loading
		if (options.showLoading !== false) {
			uni.showLoading({
				title: options.loadingText || '加载中...',
				mask: true
			})
		}

		// 获取Token
		const token = getToken()
		
		// 构建请求头
		const header = {
			'Content-Type': 'application/json',
			...options.header
		}
		
		// 添加Token
		if (token) {
			header['Authorization'] = 'Bearer ' + token
		}

		// 发起请求
		uni.request({
			url: config.baseUrl + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: header,
			timeout: options.timeout || 30000,
			success: (res) => {
				// 隐藏Loading
				if (options.showLoading !== false) {
					uni.hideLoading()
				}

				// 处理响应
				handleResponse(res, options, resolve, reject)
			},
			fail: (err) => {
				// 隐藏Loading
				if (options.showLoading !== false) {
					uni.hideLoading()
				}

				// 显示错误提示
				if (options.showError !== false) {
					uni.showToast({
						title: '网络请求失败',
						icon: 'none',
						duration: 2000
					})
				}

				reject({
					code: -1,
					message: '网络请求失败',
					error: err
				})
			}
		})
	})
}

/**
 * 处理响应数据
 * @param {Object} res 响应对象
 * @param {Object} options 请求配置
 * @param {Function} resolve Promise resolve
 * @param {Function} reject Promise reject
 */
function handleResponse(res, options, resolve, reject) {
	const data = res.data

	// 请求成功
	if (res.statusCode === 200) {
		// 业务成功
		if (data.code === 200) {
			resolve(data)
		}
		// Token过期或无效 (401)
		else if (data.code === 401) {
			handleTokenExpired(options, reject)
		}
		// 权限不足 (403)
		else if (data.code === 403) {
			if (options.showError !== false) {
				uni.showToast({
					title: data.msg || '权限不足',
					icon: 'none',
					duration: 2000
				})
			}
			reject(data)
		}
		// 限流 (429)
		else if (data.code === 429) {
			if (options.showError !== false) {
				uni.showToast({
					title: data.msg || '请求过于频繁，请稍后再试',
					icon: 'none',
					duration: 2000
				})
			}
			reject(data)
		}
		// 其他业务错误
		else {
			if (options.showError !== false) {
				uni.showToast({
					title: data.msg || '操作失败',
					icon: 'none',
					duration: 2000
				})
			}
			reject(data)
		}
	}
	// Token黑名单或未认证
	else if (res.statusCode === 401) {
		handleTokenExpired(options, reject)
	}
	// 权限不足
	else if (res.statusCode === 403) {
		if (options.showError !== false) {
			uni.showToast({
				title: '权限不足',
				icon: 'none',
				duration: 2000
			})
		}
		reject({
			code: 403,
			message: '权限不足'
		})
	}
	// 限流
	else if (res.statusCode === 429) {
		if (options.showError !== false) {
			uni.showToast({
				title: '请求过于频繁，请稍后再试',
				icon: 'none',
				duration: 2000
			})
		}
		reject({
			code: 429,
			message: '请求过于频繁'
		})
	}
	// 服务器错误
	else if (res.statusCode >= 500) {
		if (options.showError !== false) {
			uni.showToast({
				title: '服务器错误，请稍后再试',
				icon: 'none',
				duration: 2000
			})
		}
		reject({
			code: res.statusCode,
			message: '服务器错误'
		})
	}
	// 其他错误
	else {
		if (options.showError !== false) {
			uni.showToast({
				title: data.msg || '请求失败',
				icon: 'none',
				duration: 2000
			})
		}
		reject(data)
	}
}

/**
 * 处理Token过期
 * @param {Object} options 请求配置
 * @param {Function} reject Promise reject
 */
function handleTokenExpired(options, reject) {
	// 清除Token和用户信息
	clearUserInfo()

	// 显示提示
	if (options.showError !== false) {
		uni.showToast({
			title: '登录已过期，请重新登录',
			icon: 'none',
			duration: 2000
		})
	}

	// 延迟跳转到登录页
	setTimeout(() => {
		uni.reLaunch({
			url: '/pages/login/login'
		})
	}, 1500)

	reject({
		code: 401,
		message: '登录已过期'
	})
}

/**
 * GET请求
 * @param {String} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} options 其他配置
 * @returns {Promise} 请求Promise
 */
export function get(url, data = {}, options = {}) {
	return request({
		url: url,
		method: 'GET',
		data: data,
		...options
	})
}

/**
 * POST请求
 * @param {String} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} options 其他配置
 * @returns {Promise} 请求Promise
 */
export function post(url, data = {}, options = {}) {
	return request({
		url: url,
		method: 'POST',
		data: data,
		...options
	})
}

/**
 * PUT请求
 * @param {String} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} options 其他配置
 * @returns {Promise} 请求Promise
 */
export function put(url, data = {}, options = {}) {
	return request({
		url: url,
		method: 'PUT',
		data: data,
		...options
	})
}

/**
 * DELETE请求
 * @param {String} url 请求地址
 * @param {Object} data 请求参数
 * @param {Object} options 其他配置
 * @returns {Promise} 请求Promise
 */
export function del(url, data = {}, options = {}) {
	return request({
		url: url,
		method: 'DELETE',
		data: data,
		...options
	})
}

/**
 * 文件上传
 * @param {String} url 上传地址
 * @param {String} filePath 文件路径
 * @param {Object} formData 额外表单数据
 * @param {Object} options 其他配置
 * @returns {Promise} 上传Promise
 */
export function upload(url, filePath, formData = {}, options = {}) {
	return new Promise((resolve, reject) => {
		// 显示Loading
		if (options.showLoading !== false) {
			uni.showLoading({
				title: options.loadingText || '上传中...',
				mask: true
			})
		}

		// 获取Token
		const token = getToken()
		
		// 构建请求头
		const header = {
			...options.header
		}
		
		// 添加Token
		if (token) {
			header['Authorization'] = 'Bearer ' + token
		}

		// 上传文件
		uni.uploadFile({
			url: config.baseUrl + url,
			filePath: filePath,
			name: options.name || 'file',
			formData: formData,
			header: header,
			success: (res) => {
				// 隐藏Loading
				if (options.showLoading !== false) {
					uni.hideLoading()
				}

				try {
					const data = JSON.parse(res.data)
					
					if (data.code === 200) {
						resolve(data)
					} else if (data.code === 401) {
						handleTokenExpired(options, reject)
					} else {
						if (options.showError !== false) {
							uni.showToast({
								title: data.msg || '上传失败',
								icon: 'none',
								duration: 2000
							})
						}
						reject(data)
					}
				} catch (e) {
					if (options.showError !== false) {
						uni.showToast({
							title: '上传失败',
							icon: 'none',
							duration: 2000
						})
					}
					reject({
						code: -1,
						message: '上传失败',
						error: e
					})
				}
			},
			fail: (err) => {
				// 隐藏Loading
				if (options.showLoading !== false) {
					uni.hideLoading()
				}

				if (options.showError !== false) {
					uni.showToast({
						title: '上传失败',
						icon: 'none',
						duration: 2000
					})
				}

				reject({
					code: -1,
					message: '上传失败',
					error: err
				})
			}
		})
	})
}

export default {
	get,
	post,
	put,
	del,
	upload
}
