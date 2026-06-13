<template>
  <div class="chat-widget">
    <!-- Sparkle Toggle Button -->
    <button class="sparkle-button chat-toggle-sparkle" @click="isOpen = !isOpen" id="chat-widget-toggle" style="--btn-glow-color: hsla(340, 80%, 60%, 0.75);">
      <div class="dots_border"></div>
      <div class="d-flex align-items-center gap-2">
        <img v-if="!isOpen" src="/images/logoAI.png" alt="Yuni" class="toggle-avatar" />
        <span v-else class="toggle-close-icon">✕</span>
        <span class="text_button" style="font-size: 0.85rem; font-weight: bold; color: #FAF2E9;">{{ isOpen ? 'Đóng' : '✨ Chat với Yuni' }}</span>
      </div>
    </button>

    <!-- Chat Panel -->
    <transition name="slide-up">
      <div v-if="isOpen" class="chat-panel">
        <div class="chat-header">
          <div class="d-flex align-items-center gap-2">
            <img src="/images/logoAI.png" alt="Yuni" class="header-avatar" />
            <div class="d-flex flex-column align-items-start">
              <span class="fw-bold" style="color: #FAF2E9; font-size: 0.95rem; line-height: 1.2;">Yuni</span>
              <small style="color: #dcd0c0; font-size: 0.75rem;">Trợ lý của Brevery</small>
            </div>
          </div>
          <button class="btn btn-sm text-white-50 hover-white border-0" @click="isOpen = false">✕</button>
        </div>

        <div class="chat-messages" ref="messagesRef">
          <div class="chat-msg bot">
            <img src="/images/logoAI.png" alt="Yuni" class="msg-avatar" />
            <div class="msg-bubble">Xin chào! Mình là Yuni, trợ lý của Brevery 🌸 Mình có thể giúp gì cho bạn?</div>
          </div>
          <div v-for="(msg, i) in messages" :key="i" :class="['chat-msg', msg.role]">
            <img v-if="msg.role === 'bot'" src="/images/logoAI.png" alt="Yuni" class="msg-avatar" />
            <div class="msg-bubble" v-html="msg.content"></div>
          </div>
          <div v-if="loading" class="chat-msg bot">
            <img src="/images/logoAI.png" alt="Yuni" class="msg-avatar" />
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
.chat-widget { position: fixed; bottom: 24px; right: 24px; z-index: 1000; }

/* SPARKLE TOGGLE BUTTON */
.chat-toggle-sparkle {
  --black-75: rgba(0, 0, 0, 0.75);
  --border-color: rgba(255, 255, 255, 0.15);
  --btn-glow-color: hsla(340, 80%, 60%, 0.75);
  --bg-color: #1a0f0a;
  
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.6rem 1.2rem;
  border-radius: 9999px;
  background-color: var(--bg-color);
  color: #fff;
  border: 1px solid var(--border-color);
  position: relative;
  overflow: visible;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(0,0,0,0.25);
  transition: transform 0.2s, background-color 0.3s, box-shadow 0.3s;
  z-index: 1001;
}
.chat-toggle-sparkle:hover {
  background-color: #261710;
  transform: translateY(-2px);
  box-shadow: 0 0 15px var(--btn-glow-color);
}
.chat-toggle-sparkle:active {
  transform: scale(0.98);
}
.chat-toggle-sparkle .toggle-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #FAF2E9;
}
.chat-toggle-sparkle .toggle-close-icon {
  font-size: 1rem;
  color: #FAF2E9;
  font-weight: bold;
}
.chat-toggle-sparkle .dots_border {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  border-radius: 9999px;
  border: 1px dashed rgba(255, 255, 255, 0.4);
  pointer-events: none;
  opacity: 0.6;
  z-index: 0;
  animation: rotate-border-toggle 20s linear infinite;
}
@keyframes rotate-border-toggle {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* CHAT PANEL */
.chat-panel {
  position: absolute; bottom: 70px; right: 0;
  width: 360px; height: 500px;
  background: #FAF2E9; border-radius: 16px;
  box-shadow: 0 10px 40px rgba(26, 15, 10, 0.25);
  display: flex; flex-direction: column;
  overflow: hidden;
  border: 1px solid #e0d0c0;
}
.chat-header {
  background: #1a0f0a;
  color: #FAF2E9; padding: 12px 16px;
  display: flex; justify-content: space-between; align-items: center;
  border-bottom: 2px solid #E07340;
}
.header-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 1.5px solid #E07340;
}
.chat-messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; }
.chat-msg { margin-bottom: 16px; display: flex; gap: 8px; align-items: flex-start; }
.chat-msg.user { justify-content: flex-end; }
.chat-msg.bot { justify-content: flex-start; }
.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #E07340;
  flex-shrink: 0;
}
.msg-bubble {
  max-width: 75%; padding: 10px 14px;
  border-radius: 16px; font-size: .9rem; line-height: 1.4;
  box-shadow: 0 2px 5px rgba(0,0,0,0.03);
}
.chat-msg.bot .msg-bubble { background: #f5e6d3; color: #1a0f0a; border-top-left-radius: 4px; }
.chat-msg.user .msg-bubble { background: #E07340; color: #fff; border-top-right-radius: 4px; }

.chat-quick { padding: 8px 16px; display: flex; flex-wrap: wrap; gap: 6px; }
.quick-btn {
  background: #f5e6d3; border: 1px solid #e0d0c0;
  border-radius: 20px; padding: 6px 14px;
  font-size: .8rem; color: #1a0f0a;
  cursor: pointer; transition: all .2s;
  font-weight: 500;
}
.quick-btn:hover { background: #E07340; color: #fff; border-color: #E07340; }
.chat-input { padding: 12px; border-top: 1px solid #e0d0c0; display: flex; background: #FAF2E9; }

.typing { animation: blink 1s infinite; }
@keyframes blink { 0%,100% { opacity: 1; } 50% { opacity: .3; } }

.hover-white:hover {
  color: #fff !important;
}

.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(20px); }

@media (max-width: 480px) {
  .chat-panel { width: calc(100vw - 32px); height: calc(100vh - 100px); bottom: 70px; right: -8px; }
}
</style>
