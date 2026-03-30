// API 基础路径
const API_BASE = '/api';

// DOM 加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initTabs();       // 初始化标签页
    initChat();       // 初始化聊天功能
    initPreferences();// 初始化用户偏好功能
    initHistory();    // 初始化历史记录功能
    initAgents();     // 初始化 Agent 列表功能
});

/**
 * 初始化标签页切换功能
 */
function initTabs() {
    const tabBtns = document.querySelectorAll('.tab-btn');
    tabBtns.forEach(btn => {
        btn.addEventListener('click', function() {
            const tabName = this.dataset.tab;
            
            // 移除所有标签页的激活状态
            tabBtns.forEach(b => b.classList.remove('active'));
            // 激活当前标签页
            this.classList.add('active');
            
            // 隐藏所有内容区域
            document.querySelectorAll('.tab-content').forEach(content => {
                content.classList.remove('active');
            });
            // 显示当前标签页对应的内容
            document.getElementById(tabName + '-tab').classList.add('active');
        });
    });
}

/**
 * 初始化聊天功能
 */
function initChat() {
    const sendBtn = document.getElementById('sendBtn');
    const clearBtn = document.getElementById('clearSessionBtn');
    const userInput = document.getElementById('userInput');
    
    // 绑定发送按钮事件
    sendBtn.addEventListener('click', sendMessage);
    // 绑定清除会话按钮事件
    clearBtn.addEventListener('click', clearSession);
    // 绑定回车键发送消息（Shift+Enter换行）
    userInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
}

/**
 * 发送消息到 Agent
 */
async function sendMessage() {
    // 获取表单数据
    const agentName = document.getElementById('agentSelect').value;
    const userId = document.getElementById('userId').value;
    const sessionId = document.getElementById('sessionId').value;
    const message = document.getElementById('userInput').value.trim();
    
    // 消息为空时不发送
    if (!message) {
        return;
    }
    
    // 添加用户消息到聊天界面
    addMessage('user', message);
    // 清空输入框
    document.getElementById('userInput').value = '';
    
    // 显示加载中消息
    const loadingId = addLoadingMessage();
    
    try {
        // 调用带记忆的 Agent 接口
        const response = await fetch(`${API_BASE}/memory/agent/invoke`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                agentName: agentName,
                message: message,
                userId: userId,
                sessionId: sessionId
            })
        });
        
        const data = await response.json();
        
        // 移除加载中消息
        removeLoadingMessage(loadingId);
        
        // 根据响应结果显示消息
        if (data.success) {
            addMessage('agent', data.content || data.result);
        } else {
            addMessage('system', '错误: ' + data.message);
        }
    } catch (error) {
        // 出错时移除加载中消息并显示错误
        removeLoadingMessage(loadingId);
        addMessage('system', '请求失败: ' + error.message);
    }
}

/**
 * 添加消息到聊天界面
 * @param {string} type - 消息类型: user/agent/system
 * @param {string} content - 消息内容
 */
function addMessage(type, content) {
    const messagesDiv = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}-message`;
    
    // 根据消息类型设置图标
    let icon = '💬';
    if (type === 'user') icon = '👤';
    if (type === 'agent') icon = '🤖';
    if (type === 'system') icon = '⚠️';
    
    // 构建消息 HTML
    messageDiv.innerHTML = `
        <span class="message-icon">${icon}</span>
        <div class="message-content">
            <p>${escapeHtml(content)}</p>
        </div>
    `;
    
    // 添加到消息列表并滚动到底部
    messagesDiv.appendChild(messageDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

/**
 * 添加加载中消息
 * @returns {string} 加载消息元素的 ID
 */
function addLoadingMessage() {
    const messagesDiv = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message agent-message';
    messageDiv.id = 'loading-' + Date.now();
    
    messageDiv.innerHTML = `
        <span class="message-icon">🤖</span>
        <div class="message-content">
            <p><span class="loading"></span> 正在思考...</p>
        </div>
    `;
    
    messagesDiv.appendChild(messageDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
    
    return messageDiv.id;
}

/**
 * 移除加载中消息
 * @param {string} id - 加载消息元素的 ID
 */
function removeLoadingMessage(id) {
    const el = document.getElementById(id);
    if (el) {
        el.remove();
    }
}

/**
 * 清除会话记忆
 */
async function clearSession() {
    const sessionId = document.getElementById('sessionId').value;
    
    // 确认对话框
    if (!confirm('确定要清除会话 "' + sessionId + '" 的所有记忆吗？')) {
        return;
    }
    
    try {
        // 调用清除会话接口
        const response = await fetch(`${API_BASE}/memory/session/${sessionId}`, {
            method: 'DELETE'
        });
        
        const data = await response.json();
        
        if (data.success) {
            alert('会话记忆已清除');
            // 清空聊天界面
            const messagesDiv = document.getElementById('chatMessages');
            messagesDiv.innerHTML = `
                <div class="message system-message">
                    <span class="message-icon">💬</span>
                    <div class="message-content">
                        <p>会话已清除，请开始新的对话。</p>
                    </div>
                </div>
            `;
        }
    } catch (error) {
        alert('清除失败: ' + error.message);
    }
}

/**
 * 初始化用户偏好功能
 */
function initPreferences() {
    document.getElementById('getPrefsBtn').addEventListener('click', getPreferences);
    document.getElementById('setPrefsBtn').addEventListener('click', setPreferences);
}

/**
 * 获取用户偏好
 */
async function getPreferences() {
    const userId = document.getElementById('prefUserId').value;
    
    try {
        const response = await fetch(`${API_BASE}/memory/preferences/${userId}`);
        const data = await response.json();
        
        // 显示结果
        const resultDiv = document.getElementById('prefResult');
        resultDiv.innerHTML = '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
        
        // 如果成功，填充偏好到文本框
        if (data.success && data.preferences) {
            document.getElementById('preferencesInput').value = data.preferences.join('\n');
        }
    } catch (error) {
        document.getElementById('prefResult').innerHTML = '<p style="color: red;">请求失败: ' + error.message + '</p>';
    }
}

/**
 * 保存用户偏好
 */
async function setPreferences() {
    const userId = document.getElementById('prefUserId').value;
    const prefsText = document.getElementById('preferencesInput').value;
    // 按行分割并过滤空行
    const preferences = prefsText.split('\n').map(p => p.trim()).filter(p => p);
    
    try {
        const response = await fetch(`${API_BASE}/memory/preferences/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(preferences)
        });
        
        const data = await response.json();
        document.getElementById('prefResult').innerHTML = '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
    } catch (error) {
        document.getElementById('prefResult').innerHTML = '<p style="color: red;">请求失败: ' + error.message + '</p>';
    }
}

/**
 * 初始化历史记录功能
 */
function initHistory() {
    document.getElementById('getHistoryBtn').addEventListener('click', getHistory);
}

/**
 * 获取对话历史
 */
async function getHistory() {
    const sessionId = document.getElementById('histSessionId').value;
    const maxMessages = document.getElementById('maxMessages').value;
    
    try {
        const response = await fetch(`${API_BASE}/memory/session/${sessionId}/history?maxMessages=${maxMessages}`);
        const data = await response.json();
        
        const resultDiv = document.getElementById('historyResult');
        
        // 如果有历史记录，格式化显示
        if (data.success && data.history && data.history.length > 0) {
            resultDiv.innerHTML = data.history.map(item => `
                <div class="history-item ${item.role}">
                    <div class="role">${item.role === 'user' ? '👤 用户' : '🤖 Agent'}</div>
                    <p>${escapeHtml(item.content)}</p>
                </div>
            `).join('');
        } else {
            resultDiv.innerHTML = '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
        }
    } catch (error) {
        document.getElementById('historyResult').innerHTML = '<p style="color: red;">请求失败: ' + error.message + '</p>';
    }
}

/**
 * 初始化 Agent 列表功能
 */
function initAgents() {
    document.getElementById('refreshAgentsBtn').addEventListener('click', listAgents);
    // 页面加载时自动获取 Agent 列表
    listAgents();
}

/**
 * 获取 Agent 列表
 */
async function listAgents() {
    try {
        const response = await fetch(`${API_BASE}/v1/agents`);
        const data = await response.json();
        
        const resultDiv = document.getElementById('agentsResult');
        
        // 如果有 Agent，显示为卡片
        if (data.agents && data.agents.length > 0) {
            resultDiv.innerHTML = data.agents.map(agent => `
                <div class="agent-card">
                    <h3>${escapeHtml(agent.name)}</h3>
                    <p>${escapeHtml(agent.description || '')}</p>
                    <div class="agent-tags">
                        ${(agent.capabilities || []).map(cap => `<span class="agent-tag">${escapeHtml(cap)}</span>`).join('')}
                    </div>
                </div>
            `).join('');
        } else {
            resultDiv.innerHTML = '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
        }
    } catch (error) {
        document.getElementById('agentsResult').innerHTML = '<p style="color: red;">请求失败: ' + error.message + '</p>';
    }
}

/**
 * HTML 转义，防止 XSS 攻击
 * @param {string} text - 原始文本
 * @returns {string} 转义后的文本
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
