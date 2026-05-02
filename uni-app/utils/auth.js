import http from './http.js';

const TOKEN_KEY = 'wechat_token';
const USER_INFO_KEY = 'wechat_user_info';

/**
 * 微信登录
 * @param {String} code 微信登录凭证
 * @param {Object} userInfo 用户信息
 * @param {Object} deviceInfo 设备信息（可选）
 * @returns {Promise}
 */
export function login(code, userInfo, deviceInfo = {}) {
	const loginData = {
		code: code,
		nickName: userInfo.nickName,
		avatarUrl: userInfo.avatarUrl,
		gender: userInfo.gender,
		country: userInfo.country,
		province: userInfo.province,
		city: userInfo.city
	};
	
	// 如果提供了设备信息，则合并到请求数据中
	if (deviceInfo && Object.keys(deviceInfo).length > 0) {
		Object.assign(loginData, deviceInfo);
	}
	
	return http.post('/wechat/login', loginData);
}

/**
 * 保存用户信息到本地存储
 * @param {String} token JWT Token
 * @param {Object} userInfo 用户信息
 */
export function saveUserInfo(token, userInfo) {
	uni.setStorageSync(TOKEN_KEY, token);
	uni.setStorageSync(USER_INFO_KEY, JSON.stringify(userInfo));
}

/**
 * 获取本地存储的Token
 * @returns {String|null}
 */
export function getToken() {
	return uni.getStorageSync(TOKEN_KEY) || null;
}

/**
 * 获取本地存储的用户信息
 * @returns {Object|null}
 */
export function getUserInfo() {
	const userInfoStr = uni.getStorageSync(USER_INFO_KEY);
	if (userInfoStr) {
		try {
			return JSON.parse(userInfoStr);
		} catch (error) {
			console.error('解析用户信息失败:', error);
			return null;
		}
	}
	return null;
}

/**
 * 清除本地存储的用户信息
 */
export function clearUserInfo() {
	uni.removeStorageSync(TOKEN_KEY);
	uni.removeStorageSync(USER_INFO_KEY);
}

/**
 * 验证Token是否有效
 * @returns {Promise<Boolean>}
 */
export async function validateToken() {
	const token = getToken();
	if (!token) {
		return false;
	}
	
	try {
		const result = await http.post('/wechat/validateToken', { token });
		return result.valid === true;
	} catch (error) {
		console.error('Token验证失败:', error);
		return false;
	}
}

/**
 * 刷新Token
 * @returns {Promise<String>} 新的Token
 */
export async function refreshToken() {
	const oldToken = getToken();
	if (!oldToken) {
		throw new Error('没有Token可刷新');
	}
	
	try {
		const result = await http.post('/wechat/refreshToken', { token: oldToken });
		const newToken = result.token;
		
		// 保存新Token
		uni.setStorageSync(TOKEN_KEY, newToken);
		
		return newToken;
	} catch (error) {
		console.error('Token刷新失败:', error);
		throw error;
	}
}

/**
 * 自动登录（验证本地Token）
 * @returns {Promise<Boolean>} 是否登录成功
 */
export async function autoLogin() {
	const token = getToken();
	const userInfo = getUserInfo();
	
	// 没有Token或用户信息，无法自动登录
	if (!token || !userInfo) {
		return false;
	}
	
	try {
		// 验证Token是否有效
		const isValid = await validateToken();
		
		if (isValid) {
			// Token有效，自动登录成功
			return true;
		} else {
			// Token无效，尝试刷新
			try {
				await refreshToken();
				return true;
			} catch (refreshError) {
				// 刷新失败，清除本地信息
				clearUserInfo();
				return false;
			}
		}
	} catch (error) {
		console.error('自动登录失败:', error);
		clearUserInfo();
		return false;
	}
}

/**
 * 快速检查本地登录状态（不进行网络验证）
 * @returns {Boolean}
 */
export function checkLoginLocal() {
	const token = getToken();
	const userInfo = getUserInfo();
	return !!(token && userInfo);
}

/**
 * 检查登录状态，未登录则跳转到登录页
 * @param {Boolean} redirect 是否跳转到登录页，默认true
 * @param {Boolean} validate 是否进行网络验证，默认false（仅检查本地）
 * @returns {Promise<Boolean>}
 */
export async function checkLogin(redirect = true, validate = false) {
	// 优先进行快速的本地检查
	const hasLocalAuth = checkLoginLocal();
	
	if (!hasLocalAuth) {
		// 本地没有登录信息，直接跳转
		if (redirect) {
			uni.reLaunch({
				url: '/pages/login/login'
			});
		}
		return false;
	}
	
	// 如果需要网络验证（通常不需要）
	if (validate) {
		const isLoggedIn = await autoLogin();
		if (!isLoggedIn && redirect) {
			uni.reLaunch({
				url: '/pages/login/login'
			});
		}
		return isLoggedIn;
	}
	
	// 默认情况：有本地登录信息即认为已登录
	return true;
}

/**
 * 登出
 */
export async function logout() {
	try {
		const token = getToken();
		if (token) {
			// 调用后端登出接口
			await http.post('/wechat/logout', { token });
		}
	} catch (error) {
		console.error('登出请求失败:', error);
	} finally {
		// 无论是否成功，都清除本地信息
		clearUserInfo();
		
		// 跳转到登录页
		uni.reLaunch({
			url: '/pages/login/login'
		});
	}
}

/**
 * 更新用户信息
 * @param {Object} userInfo 要更新的用户信息
 * @returns {Promise}
 */
export async function updateUserInfo(userInfo) {
	try {
		const result = await http.put('/wechat/updateUserInfo', userInfo);
		
		// 更新本地存储的用户信息
		const localUserInfo = getUserInfo();
		if (localUserInfo) {
			const updatedUserInfo = { ...localUserInfo, ...userInfo };
			uni.setStorageSync(USER_INFO_KEY, JSON.stringify(updatedUserInfo));
		}
		
		return result;
	} catch (error) {
		console.error('更新用户信息失败:', error);
		throw error;
	}
}

/**
 * 获取用户ID
 * @returns {Number|null}
 */
export function getUserId() {
	const userInfo = getUserInfo();
	return userInfo ? userInfo.userId : null;
}

export default {
	login,
	saveUserInfo,
	getToken,
	getUserInfo,
	clearUserInfo,
	validateToken,
	refreshToken,
	autoLogin,
	checkLogin,
	logout,
	updateUserInfo,
	getUserId
};
