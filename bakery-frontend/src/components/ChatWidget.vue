<template>
  <div class="chat-widget">
    <!-- Toggle Button -->
    <button class="chat-toggle" @click="isOpen = !isOpen" id="chat-widget-toggle">
      <span v-if="!isOpen">💬</span>
      <span v-else>✕</span>
    </button>

    <!-- Chat Panel -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-panel">
        <div class="chat-header">
          <span>🤖 Tư vấn Brevery</span>
          <button class="btn btn-sm text-white" @click="isOpen = false">✕</button>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <div class="chat-msg bot">
            <div class="msg-bubble">Chào bạn! Mình có thể giúp gì? 🍰</div>
          </div>
          <div v-for="(msg, i) in messages" :key="i" :class="['chat-msg', msg.role]">
            <div class="msg-bubble" v-html="msg.content"></div>
          </div>
          <div v-if="loading" class="chat-msg bot">
            <div class="msg-bubble"><span class="typing">...</span></div>
          </div>
        </div>

        <!-- Quick Replies -->
        <div v-if="messages.length === 0" class="chat-quick">
          <button v-for="q in quickReplies" :key="q" class="quick-btn" @click="sendMessage(q)">{{ q }}</button>
        </div>

        <!-- Input -->
        <div class="chat-input">
          <input
            v-model="input"
            class="bakery-input flex-grow-1"
            placeholder="Nhập tin nhắn..."
            @keyup.enter="sendMessage(input)"
            :disabled="loading"
          />
          <button class="btn btn-bakery ms-2" @click="sendMessage(input)" :disabled="loading || !input.trim()">Gửi</button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { aiApi } from '@/api/ai.api'

const isOpen = ref(false)
const messages = ref([])
const input = ref('')
const loading = ref(false)
const messagesRef = ref(null)

const quickReplies = [
  'Hôm nay có món gì mới?',
  'Bánh nào ít ngọt?',
  'Đặt hàng bao lâu giao?',
]

async function sendMessage(text) {
  if (!text?.trim() || messages.value.length >= 40) return
  const msg = text.trim()
  input.value = ''
  messages.value.push({ role: 'user', content: msg })
  scrollToBottom()

  loading.value = true
  try {
    const { data } = await aiApi.chat(msg)
    messages.value.push({ role: 'bot', content: data.data?.reply || data.data || 'Xin lỗi, mình chưa hiểu câu hỏi.' })
  } catch {
    messages.value.push({ role: 'bot', content: 'Rất tiếc, đã có lỗi xảy ra. Bạn thử lại nhé!' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  nextTick(() => { if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight })
}
</script>

<style scoped>
.chat-widget { position: fixed; bottom: 24px; right: 24px; z-index: 9999; }
.chat-toggle {
  width: 56px; height: 56px; border-radius: 50%;
  background: var(--bakery-primary); color: #fff;
  border: none; font-size: 1.4rem;
  box-shadow: 0 4px 20px rgba(212,135,90,.3);
  cursor: pointer; transition: transform .2s;
}
.chat-toggle:hover { transform: scale(1.1); }

.chat-panel {
  position: absolute; bottom: 70px; right: 0;
  width: 360px; height: 500px;
  background: #fff; border-radius: var(--radius-card);
  box-shadow: 0 8px 40px rgba(0,0,0,.15);
  display: flex; flex-direction: column;
  overflow: hidden;
}

.chat-header {
  background: var(--bakery-primary);
  color: #fff; padding: 14px 16px;
  display: flex; justify-content: space-between; align-items: center;
  font-weight: 600;
}

.chat-messages { flex: 1; overflow-y: auto; padding: 16px; }
.chat-msg { margin-bottom: 12px; display: flex; }
.chat-msg.user { justify-content: flex-end; }
.msg-bubble {
  max-width: 80%; padding: 10px 14px;
  border-radius: 16px; font-size: .9rem; line-height: 1.4;
}
.chat-msg.bot .msg-bubble { background: var(--bakery-primary-light); color: var(--bakery-text); }
.chat-msg.user .msg-bubble { background: var(--bakery-primary); color: #fff; }

.chat-quick { padding: 8px 16px; display: flex; flex-wrap: wrap; gap: 6px; }
.quick-btn {
  background: var(--bakery-primary-light); border: none;
  border-radius: var(--radius-badge); padding: 6px 14px;
  font-size: .8rem; color: var(--bakery-primary);
  cursor: pointer; transition: all .15s;
}
.quick-btn:hover { background: var(--bakery-primary); color: #fff; }

.chat-input { padding: 12px; border-top: 1px solid #f0e8e0; display: flex; }

.typing { animation: blink 1s infinite; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: .3; } }

@media (max-width: 480px) {
  .chat-panel { width: calc(100vw - 32px); height: calc(100vh - 100px); bottom: 70px; right: -8px; }
}
</style>
