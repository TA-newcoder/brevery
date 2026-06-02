<template>
  <div class="home-page">
    
    <!-- 2. HERO SECTION (2 Cột) -->
    <section class="hero-section position-relative overflow-hidden">
      <!-- Decorative background blobs -->
      <div class="bg-blob blob-1"></div>
      <div class="bg-blob blob-2"></div>
      
      <div class="container position-relative z-1 py-5 py-lg-6">
        <div class="row align-items-center g-5">
          <!-- Left Column -->
          <div class="col-lg-6 hero-content d-flex flex-column animate-fade-up" style="gap: 1.5rem;">
            <!-- Badge -->
            <div class="badge-pill d-inline-flex align-items-center gap-2 px-3 py-2 bg-warning-subtle text-warning-emphasis rounded-pill shadow-sm align-self-start">
              <PhClock size="14" weight="bold" color="#C8502A" />
              <span class="fw-bold small" style="color: #C8502A;">Mở cửa T2–CN · 7:00–22:00</span>
            </div>
            
            <!-- Heading -->
            <h1 class="fw-bold mb-0 hero-title" style="letter-spacing: -1px; line-height: 1.2;">
              <span style="color: #2D1B0E; white-space: nowrap; font-size: min(52px, 8vw); display: block;">Bánh ngọt & Đồ uống</span>
              <span style="color: #C8502A; font-size: min(52px, 8vw); display: block;">tươi mới mỗi ngày</span>
            </h1>
            
            <!-- Description -->
            <p class="fs-5 text-secondary mb-0 pe-lg-5" style="line-height: 1.6;">
              Giao tận nơi trong 30 phút.<br/>
              Làm từ nguyên liệu tươi sạch mỗi sáng.
            </p>
            
            <!-- CTAs -->
            <div class="d-flex flex-wrap gap-3 mt-2">
              <router-link to="/products" class="btn btn-hero-primary btn-lg px-4 fw-bold shadow-sm d-flex align-items-center justify-content-center">
                <PhBag size="18" weight="bold" class="me-2" /> Đặt hàng ngay
              </router-link>
              <router-link to="/products" class="btn btn-hero-secondary btn-lg px-4 fw-bold shadow-sm d-flex align-items-center justify-content-center">
                <PhBookOpen size="18" weight="bold" class="me-2" /> Xem thực đơn
              </router-link>
            </div>
            
            <!-- Trust badges -->
            <div class="d-flex flex-wrap gap-4 mt-2">
              <div class="trust-badge d-flex align-items-center gap-2 text-secondary fw-semibold small">
                <PhCheckCircle size="16" weight="fill" color="#16A34A" /> Giao trong 30 phút
              </div>
              <div class="trust-badge d-flex align-items-center gap-2 text-secondary fw-semibold small">
                <PhLeaf size="16" weight="fill" color="#16A34A" /> Nguyên liệu tươi
              </div>
              <div class="trust-badge d-flex align-items-center gap-2 text-secondary fw-semibold small">
                <PhShieldCheck size="16" weight="fill" color="#16A34A" /> Hoàn tiền 100%
              </div>
            </div>
          </div>
          
          <!-- Right Column -->
          <div class="col-lg-6 hero-image-col position-relative d-none d-lg-block">
            <div class="hero-image-wrapper position-relative mx-auto">
              <!-- Decorative ring -->
              <div class="hero-ring"></div>
              
              <!-- Dots -->
              <div class="hero-dot dot-12"></div>
              <div class="hero-dot dot-2"></div>
              <div class="hero-dot dot-4"></div>
              <div class="hero-dot dot-6"></div>
              <div class="hero-dot dot-8"></div>
              <div class="hero-dot dot-10"></div>
              
              <!-- Main Image -->
              <transition name="fade-image" mode="out-in">
                <img :key="currentHeroImage" :src="currentHeroImage" alt="Sản phẩm tươi" class="hero-main-img shadow-lg cursor-pointer" @click="cycleHeroImage" title="Click để xem hình khác" />
              </transition>
              
              <!-- Floating Cards -->
              <div class="float-card card-1 shadow-sm d-flex align-items-center gap-2 px-3 py-2 rounded-pill fw-bold text-dark position-absolute">
                <PhCake size="20" weight="fill" color="#C8502A" /> Bánh tươi
              </div>
              <div class="float-card card-2 shadow-sm d-flex align-items-center gap-2 px-3 py-2 rounded-pill fw-bold text-dark position-absolute">
                <PhCoffee size="20" weight="fill" color="#C8502A" /> Đồ uống
              </div>
              <div class="float-card card-3 shadow-sm d-flex align-items-center gap-2 px-3 py-2 rounded-pill fw-bold text-dark position-absolute">
                <PhMoped size="20" weight="fill" color="#C8502A" /> 30 phút
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. CATEGORY TABS + SẢN PHẨM -->
    <section class="container py-5 py-lg-6 reveal-on-scroll">
      <div class="text-center mb-5">
        <h2 class="fw-bold mb-4">Khám phá Thực Đơn</h2>
        
        <!-- Category Tabs -->
        <div class="d-flex justify-content-center flex-wrap gap-2 gap-md-3">
          <button class="btn btn-category rounded-pill px-4 py-2 fw-semibold shadow-sm d-flex align-items-center gap-2" :class="{ 'active': activeTab === 'all' }" @click="activeTab = 'all'">
            <PhFire size="20" weight="duotone" :color="activeTab === 'all' ? '#fff' : 'var(--bakery-primary)'" /> Tất cả
          </button>
          <button class="btn btn-category rounded-pill px-4 py-2 fw-semibold shadow-sm d-flex align-items-center gap-2" :class="{ 'active': activeTab === 'cake' }" @click="activeTab = 'cake'">
            <PhCake size="20" weight="duotone" :color="activeTab === 'cake' ? '#fff' : 'var(--bakery-primary)'" /> Bánh kem
          </button>
          <button class="btn btn-category rounded-pill px-4 py-2 fw-semibold shadow-sm d-flex align-items-center gap-2" :class="{ 'active': activeTab === 'pastry' }" @click="activeTab = 'pastry'">
            <PhCookie size="20" weight="duotone" :color="activeTab === 'pastry' ? '#fff' : 'var(--bakery-primary)'" /> Bánh ngọt
          </button>
          <button class="btn btn-category rounded-pill px-4 py-2 fw-semibold shadow-sm d-flex align-items-center gap-2" :class="{ 'active': activeTab === 'drink' }" @click="activeTab = 'drink'">
            <PhCoffee size="20" weight="duotone" :color="activeTab === 'drink' ? '#fff' : 'var(--bakery-primary)'" /> Đồ uống
          </button>
          <button class="btn btn-category rounded-pill px-4 py-2 fw-semibold shadow-sm d-flex align-items-center gap-2" :class="{ 'active': activeTab === 'new' }" @click="activeTab = 'new'">
            <PhSparkle size="20" weight="duotone" :color="activeTab === 'new' ? '#fff' : 'var(--bakery-primary)'" /> Mới nhất
          </button>
        </div>
      </div>

      <div class="row g-4">
        <div v-for="p in displayedProducts" :key="p.productId" class="col-6 col-md-4 col-lg-3">
          <ProductCard :product="p" />
        </div>
        <div v-if="displayedProducts.length === 0" v-for="i in 4" :key="'sk'+i" class="col-6 col-md-4 col-lg-3">
          <div class="skeleton" style="height: 320px; border-radius: 16px;"></div>
        </div>
      </div>
      
      <div class="text-center mt-5">
        <router-link to="/products" class="btn btn-outline-dark rounded-pill px-5 py-2 fw-bold shadow-sm">Xem toàn bộ Menu →</router-link>
      </div>
    </section>

    <!-- 4. BANNER DANH MỤC -->
    <section class="container py-5 reveal-on-scroll">
      <div class="row g-4 banner-grid" style="height: 320px;">
        <!-- Banner Trái (60%) -->
        <div class="col-md-7 h-100">
          <div class="promo-banner w-100 h-100 rounded-4 overflow-hidden position-relative shadow-sm" style="background: url('/images/banner.png') center/cover;">
            <div class="banner-overlay position-absolute w-100 h-100" style="background: linear-gradient(to top, rgba(0,0,0,0.8), transparent); bottom: 0;"></div>
            <div class="position-absolute bottom-0 p-4 w-100 text-white z-1">
              <h3 class="fw-bold mb-2 d-flex align-items-center">
                <PhCake size="24" weight="fill" color="#fff" class="me-2" /> Bánh Sinh Nhật
              </h3>
              <p class="mb-3 text-white-50">Đặt trước 24h — Miễn phí giao hàng</p>
              <router-link to="/products?categoryId=1" class="btn btn-light rounded-pill px-4 fw-bold btn-sm shadow d-inline-flex align-items-center gap-1">
                Đặt ngay <PhArrowRight size="16" weight="bold" />
              </router-link>
            </div>
          </div>
        </div>
        
        <!-- Banner Phải (40%) -->
        <div class="col-md-5 h-100 d-flex flex-column gap-4">
          <!-- Banner Trên -->
          <div class="promo-banner w-100 rounded-4 overflow-hidden position-relative shadow-sm flex-grow-1" style="background: url('/images/cake.png') center/cover;">
            <div class="banner-overlay position-absolute w-100 h-100" style="background: rgba(0,0,0,0.5);"></div>
            <div class="position-absolute w-100 h-100 d-flex flex-column justify-content-center align-items-center text-white z-1">
              <h4 class="fw-bold mb-1 d-flex align-items-center">
                <PhCookie size="20" weight="fill" color="#fff" class="me-2" /> Bánh Ngọt
              </h4>
              <p class="small mb-0 text-white-50">Tươi làm mỗi sáng</p>
            </div>
          </div>
          <!-- Banner Dưới -->
          <div class="promo-banner w-100 rounded-4 overflow-hidden position-relative shadow-sm flex-grow-1" style="background: url('/images/drink.png') center/cover;">
            <div class="banner-overlay position-absolute w-100 h-100" style="background: rgba(0,0,0,0.5);"></div>
            <div class="position-absolute w-100 h-100 d-flex flex-column justify-content-center align-items-center text-white z-1">
              <h4 class="fw-bold mb-1 d-flex align-items-center">
                <PhCoffee size="20" weight="fill" color="#fff" class="me-2" /> Đồ Uống
              </h4>
              <p class="small mb-0 text-white-50">Trà sữa · Cà phê · Sinh tố</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. HOW IT WORKS -->
    <section class="py-5 py-lg-6 bg-white reveal-on-scroll">
      <div class="container text-center">
        <h2 class="fw-bold mb-5 d-flex align-items-center justify-content-center">
          Đặt hàng chỉ 3 bước <PhRocket size="32" weight="fill" color="#C8502A" class="ms-2" />
        </h2>
        <div class="row position-relative">
          <!-- Line connecting steps (Desktop only) -->
          <div class="d-none d-md-block position-absolute" style="top: 40px; left: 16%; right: 16%; height: 2px; border-top: 2px dashed #ddd; z-index: 0;"></div>
          
          <div class="col-md-4 mb-4 mb-md-0 position-relative z-1">
            <div class="step-icon-wrapper bg-white mx-auto shadow-sm d-flex align-items-center justify-content-center mb-3 position-relative">
              <PhShoppingCart size="32" color="var(--bakery-primary)" weight="duotone" />
              <div class="step-number bg-bakery text-white rounded-circle position-absolute fw-bold">1</div>
            </div>
            <h5 class="fw-bold">Chọn sản phẩm</h5>
            <p class="text-secondary small mx-auto" style="max-width: 200px;">Duyệt menu, thêm vào giỏ hàng</p>
          </div>
          
          <div class="col-md-4 mb-4 mb-md-0 position-relative z-1">
            <div class="step-icon-wrapper bg-white mx-auto shadow-sm d-flex align-items-center justify-content-center mb-3 position-relative">
              <PhMapPin size="32" color="var(--bakery-primary)" weight="duotone" />
              <div class="step-number bg-bakery text-white rounded-circle position-absolute fw-bold">2</div>
            </div>
            <h5 class="fw-bold">Nhập địa chỉ</h5>
            <p class="text-secondary small mx-auto" style="max-width: 200px;">Điền thông tin chỉ 1 phút</p>
          </div>
          
          <div class="col-md-4 position-relative z-1">
            <div class="step-icon-wrapper bg-white mx-auto shadow-sm d-flex align-items-center justify-content-center mb-3 position-relative">
              <PhMoped size="32" color="var(--bakery-primary)" weight="duotone" />
              <div class="step-number bg-bakery text-white rounded-circle position-absolute fw-bold">3</div>
            </div>
            <h5 class="fw-bold">Nhận tại nhà</h5>
            <p class="text-secondary small mx-auto" style="max-width: 200px;">Shipper giao trong 30 phút</p>
          </div>
        </div>
        <div class="mt-5">
          <router-link to="/products" class="btn btn-bakery rounded-pill px-5 py-3 fw-bold shadow-sm fs-5">Bắt đầu đặt hàng →</router-link>
        </div>
      </div>
    </section>

    <!-- 6. TESTIMONIALS -->
    <section class="py-5 py-lg-6 reveal-on-scroll" style="background-color: #FDF6EE;">
      <div class="container">
        <div class="text-center mb-5">
          <h2 class="fw-bold">Khách hàng nói gì? 💬</h2>
        </div>
        
        <div class="testimonial-slider overflow-hidden position-relative">
          <div class="d-flex transition-transform" :style="{ transform: `translateX(-${currentSlide * slideWidth}%)` }" style="transition: transform 0.5s ease-in-out;">
            <div v-for="(review, index) in testimonials" :key="index" class="testimonial-slide px-2">
              <div class="bg-white p-4 rounded-4 shadow-sm h-100 d-flex flex-column">
                <div class="d-flex text-warning mb-3">
                  <PhStar v-for="n in 5" :key="n" size="18" weight="fill" />
                </div>
                <p class="fst-italic text-secondary flex-grow-1">"{{ review.text }}"</p>
                <hr class="my-3 text-black-50">
                <div class="d-flex align-items-center gap-3">
                  <div class="avatar-circle bg-bakery text-white fw-bold d-flex align-items-center justify-content-center flex-shrink-0">
                    {{ review.name.charAt(0) }}
                  </div>
                  <div>
                    <h6 class="fw-bold mb-0" style="font-size: 0.9rem;">{{ review.name }} · {{ review.location }}</h6>
                    <small class="text-muted" style="font-size: 0.8rem;">Đã mua: {{ review.product }}</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Controls -->
          <div class="d-flex justify-content-center gap-2 mt-4">
            <button v-for="(_, idx) in maxSlides" :key="idx" class="dot-btn rounded-circle p-0" :class="{ 'active': currentSlide === idx }" @click="currentSlide = idx"></button>
          </div>
        </div>
      </div>
    </section>

    <!-- 7. TẠI SAO CHỌN BREVERY -->
    <section class="py-5 py-lg-6 bg-white reveal-on-scroll">
      <div class="container text-center">
        <h2 class="fw-bold mb-5">Tại sao chọn Brevery? ✨</h2>
        <div class="row g-4">
          <div class="col-md-4">
            <div class="feature-card bg-success-subtle p-4 p-lg-5 rounded-4 h-100 shadow-sm transition-hover">
              <div class="icon-box bg-white text-success rounded-circle shadow-sm d-flex align-items-center justify-content-center mx-auto mb-4">
                <PhLeaf size="40" weight="duotone" />
              </div>
              <h5 class="fw-bold mb-3">Nguyên liệu tự nhiên</h5>
              <p class="text-secondary fst-italic small">"Không chất bảo quản — không lo sức khỏe"</p>
            </div>
          </div>
          <div class="col-md-4">
            <div class="feature-card bg-primary-subtle p-4 p-lg-5 rounded-4 h-100 shadow-sm transition-hover">
              <div class="icon-box bg-white text-primary rounded-circle shadow-sm d-flex align-items-center justify-content-center mx-auto mb-4">
                <PhRocket size="40" weight="duotone" />
              </div>
              <h5 class="fw-bold mb-3">Giao hàng cực nhanh</h5>
              <p class="text-secondary fst-italic small">"Giao trễ? Hoàn phí ship ngay lập tức"</p>
            </div>
          </div>
          <div class="col-md-4">
            <div class="feature-card bg-danger-subtle p-4 p-lg-5 rounded-4 h-100 shadow-sm transition-hover">
              <div class="icon-box bg-white text-danger rounded-circle shadow-sm d-flex align-items-center justify-content-center mx-auto mb-4">
                <PhHeart size="40" weight="duotone" />
              </div>
              <h5 class="fw-bold mb-3">Đảm bảo hài lòng</h5>
              <p class="text-secondary fst-italic small">"Không hài lòng? Hoàn 100% không hỏi"</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 8. GALLERY -->
    <section class="py-5 py-lg-6 reveal-on-scroll bg-light">
      <div class="container text-center">
        <h3 class="fw-bold mb-4 d-flex align-items-center justify-content-center gap-2">
          <PhInstagramLogo size="32" weight="duotone" color="var(--bakery-primary)" />
          Theo dõi chúng tôi 📸 @brevery.vn
        </h3>
        
        <div class="row g-2 justify-content-center">
          <div v-for="n in 6" :key="n" class="col-4 col-md-2">
            <div class="gallery-item position-relative overflow-hidden rounded-3 shadow-sm ratio ratio-1x1">
              <img :src="n % 2 === 0 ? '/images/cake.png' : (n % 3 === 0 ? '/images/drink.png' : '/images/banner.png')" alt="Instagram Post" class="w-100 h-100 object-fit-cover" />
              <div class="gallery-overlay position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center">
                <PhInstagramLogo size="28" weight="fill" color="#fff" />
              </div>
            </div>
          </div>
        </div>
        <div class="mt-4">
          <a href="#" class="btn btn-outline-dark rounded-pill px-4 fw-bold bg-white">Follow Instagram</a>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { productApi } from '@/api/product.api'
import ProductCard from '@/components/ProductCard.vue'
import { 
  PhStorefront, PhShoppingCart, PhList, PhClock, PhLeaf, PhShieldCheck, 
  PhFire, PhCake, PhCookie, PhCoffee, PhSparkle, PhMapPin, PhMoped, 
  PhStar, PhRocket, PhHeart, PhInstagramLogo, PhBag, PhBookOpen, PhCheckCircle, PhArrowRight
} from '@phosphor-icons/vue'

// --- Hero Image Cycle Logic ---
const heroImages = ['/images/cake.png', '/images/drink.png', '/images/banner.png']
const currentHeroImageIndex = ref(0)
const currentHeroImage = computed(() => heroImages[currentHeroImageIndex.value])

function cycleHeroImage() {
  currentHeroImageIndex.value = (currentHeroImageIndex.value + 1) % heroImages.length
}

// --- Category Tabs Logic ---
const activeTab = ref('all')
const topProducts = ref([])
const displayedProducts = computed(() => {
  // Demo filter logic using the topProducts array
  if (topProducts.value.length === 0) return []
  if (activeTab.value === 'all') return topProducts.value.slice(0, 8)
  if (activeTab.value === 'new') return [...topProducts.value].reverse().slice(0, 4)
  // Just slicing differently for visual effect since we don't fetch full categories in Home
  return topProducts.value.slice(0, 4)
})

// --- Testimonials Logic ---
const currentSlide = ref(0)
const slideWidth = ref(100/3) // Default desktop: 3 slides
const maxSlides = computed(() => testimonials.length - (100 / slideWidth.value) + 1)
let slideInterval = null

const testimonials = [
  { name: 'Nguyễn T.', location: 'Q.1', text: 'Bánh kem siêu ngon, cốt bánh xốp mềm không bị ngọt gắt. Giao hàng cực kỳ cẩn thận không hề bị méo!', product: 'Bánh Sinh Nhật Dâu Tây' },
  { name: 'Trần H.', location: 'Q.Bình Thạnh', text: 'Cà phê thơm lừng, shipper giao đến nơi đá vẫn chưa tan. Mình gọi vào giờ cao điểm nhưng chỉ 20p là có.', product: 'Cà phê Sữa Đá' },
  { name: 'Lê M.', location: 'Q.7', text: 'Bao bì thân thiện môi trường, nhìn rất sang. Bánh sừng trâu thơm mùi bơ, ăn sáng với trà nóng là chuẩn bài.', product: 'Bánh Croissant' },
  { name: 'Phạm Q.', location: 'Q.3', text: 'Đặt bánh kem gấp để tặng sinh nhật sếp mà Brevery vẫn xử lý kịp trong 2 tiếng. Rất cảm ơn shop!', product: 'Bánh Tiramisu Ý' },
  { name: 'Hoàng V.', location: 'Q.Phú Nhuận', text: 'Trà sữa trân châu dẻo mềm, vị trà đậm đà không bị lấn át bởi sữa. Đã trở thành khách ruột của quán.', product: 'Trà Sữa Truyền Thống' }
]

function handleResize() {
  if (window.innerWidth < 768) {
    slideWidth.value = 100 // 1 slide on mobile
  } else {
    slideWidth.value = 100 / 3 // 3 slides on desktop
  }
}

function nextSlide() {
  currentSlide.value = (currentSlide.value + 1) % maxSlides.value
}

// --- Intersection Observer for Scroll Animations ---
let observer = null

onMounted(async () => {
  // Fetch Products
  try {
    const { data } = await productApi.getTopSelling(12)
    topProducts.value = data.data || []
  } catch {
    try {
      const { data } = await productApi.getProducts({ page: 0, size: 12, sortBy: 'totalSold' })
      topProducts.value = data.data?.content || []
    } catch { /* silent */ }
  }

  // Setup Testimonials
  handleResize()
  window.addEventListener('resize', handleResize)
  slideInterval = setInterval(nextSlide, 4000)

  // Setup Scroll Animation Observer
  observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible')
      }
    })
  }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' })

  document.querySelectorAll('.reveal-on-scroll, .animate-fade-up').forEach((el) => {
    observer.observe(el)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  clearInterval(slideInterval)
  if (observer) observer.disconnect()
})
</script>

<style scoped>
/* GENERAL ANIMATIONS */
.reveal-on-scroll {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.8s ease-out, transform 0.8s ease-out;
}
.reveal-on-scroll.visible {
  opacity: 1;
  transform: translateY(0);
}

.animate-fade-up {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s ease-out, transform 0.6s ease-out;
}
.animate-fade-up.visible {
  opacity: 1;
  transform: translateY(0);
}

/* HERO SECTION */
.hero-section {
  background: #FDF6EE;
  min-height: 80vh;
  display: flex;
  align-items: center;
}
.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  z-index: 0;
}
.blob-1 { width: 400px; height: 400px; background: rgba(200, 80, 42, 0.15); top: -100px; right: -50px; }
.blob-2 { width: 300px; height: 300px; background: rgba(255, 200, 100, 0.2); bottom: -50px; left: 10%; }

/* Buttons CTA Hover States */
.btn-hero-primary {
  background: #C8502A;
  color: #FFFFFF;
  border: none;
  transition: all 0.25s ease;
}
.btn-hero-primary:hover {
  background: #8B3210;
  color: #FFFFFF;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(200, 80, 42, 0.4) !important;
}

.btn-hero-secondary {
  background: transparent;
  border: 2px solid #C8502A;
  color: #C8502A;
  transition: all 0.25s ease;
}
.btn-hero-secondary:hover {
  background: #C8502A;
  color: #FFFFFF;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(200, 80, 42, 0.3) !important;
}

/* Hero Right Column Visuals */
.hero-image-wrapper {
  width: 450px;
  height: 450px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-main-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 8px solid white;
  object-fit: cover;
  box-shadow: 0 0 40px rgba(200, 80, 42, 0.25), 0 0 80px rgba(200, 80, 42, 0.10);
  transition: transform 0.4s ease;
  position: relative;
  z-index: 2;
}

.cursor-pointer {
  cursor: pointer;
}

/* Image Fade Transition */
.fade-image-enter-active,
.fade-image-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-image-enter-from,
.fade-image-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.hero-image-wrapper:hover .hero-main-img {
  transform: scale(1.04);
}
.hero-image-wrapper:hover .hero-ring {
  animation-duration: 5s;
}

.hero-ring {
  position: absolute;
  top: -12px; left: -12px; right: -12px; bottom: -12px;
  border: 2px dashed #F4A876;
  border-radius: 50%;
  animation: spin 20s linear infinite;
  z-index: 1;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Dots */
.hero-dot {
  position: absolute;
  width: 10px; height: 10px;
  border-radius: 50%;
  opacity: 0.5;
  z-index: 3;
  animation: pulse-dot 2s infinite ease-in-out;
}
.dot-12 { top: -20px; left: 50%; transform: translateX(-50%); background: #C8502A; animation-delay: 0s; }
.dot-2 { top: 15%; right: -10px; background: #F4A876; animation-delay: 0.3s; }
.dot-4 { bottom: 15%; right: -10px; background: #C8502A; animation-delay: 0.6s; }
.dot-6 { bottom: -20px; left: 50%; transform: translateX(-50%); background: #F4A876; animation-delay: 0.9s; }
.dot-8 { bottom: 15%; left: -10px; background: #C8502A; animation-delay: 1.2s; }
.dot-10 { top: 15%; left: -10px; background: #F4A876; animation-delay: 1.5s; }

@keyframes pulse-dot {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.5); opacity: 1; }
}

/* Floating Cards Glassmorphism */
.float-card {
  z-index: 4;
  animation: float 4s ease-in-out infinite;
  backdrop-filter: blur(8px);
  background: rgba(255, 255, 255, 0.85) !important;
  border: 1px solid rgba(255, 255, 255, 0.6);
}
.card-1 { top: 10%; left: -5%; animation-delay: 0s; }
.card-2 { bottom: 20%; right: -5%; animation-delay: 1s; }
.card-3 { bottom: 5%; left: 10%; animation-delay: 2s; }

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-15px); }
}

/* CATEGORY TABS */
.btn-category {
  background: white;
  border: 1px solid rgba(200, 80, 42, 0.2);
  color: var(--bakery-text);
  transition: all 0.3s;
}
.btn-category:hover {
  border-color: var(--bakery-primary);
}
.btn-category.active {
  background: var(--bakery-primary);
  color: white;
  border-color: var(--bakery-primary);
}

/* PROMO BANNERS */
.promo-banner {
  transition: transform 0.3s ease;
}
.promo-banner:hover {
  transform: scale(1.02);
}

/* HOW IT WORKS */
.step-icon-wrapper {
  width: 80px; height: 80px;
  border-radius: 50%;
}
.step-number {
  width: 24px; height: 24px;
  font-size: 0.8rem;
  display: flex; align-items: center; justify-content: center;
  top: -5px; right: -5px;
}

/* TESTIMONIALS */
.testimonial-slide {
  width: calc(100% / 3);
  flex-shrink: 0;
}
@media (max-width: 768px) {
  .testimonial-slide { width: 100%; }
}
.avatar-circle {
  width: 40px; height: 40px;
  border-radius: 50%;
  font-family: 'Playfair Display', serif;
}
.dot-btn {
  width: 10px; height: 10px;
  background: #ddd;
  border: none;
  transition: background 0.3s, transform 0.3s;
}
.dot-btn.active {
  background: var(--bakery-primary);
  transform: scale(1.3);
}

/* WHY CHOOSE US */
.feature-card {
  border: 1px solid transparent;
}
.icon-box {
  width: 80px; height: 80px;
}
.transition-hover {
  transition: transform 0.3s, box-shadow 0.3s;
}
.transition-hover:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.08) !important;
}

/* GALLERY */
.gallery-item .gallery-overlay {
  background: rgba(200, 80, 42, 0);
  opacity: 0;
  transition: all 0.3s ease;
}
.gallery-item:hover .gallery-overlay {
  background: rgba(200, 80, 42, 0.7);
  opacity: 1;
}
</style>
