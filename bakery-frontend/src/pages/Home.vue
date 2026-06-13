<template>
  <div class="home-page">
    <!-- Scroll progress bar -->
    <div class="scroll-progress" :style="{ width: scrollProgress + '%' }"></div>

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
              <span :style="{ color: isHeroBgDark ? '#FFFFFF' : '#2D1B0E' }" style="white-space: nowrap; font-size: min(52px, 8vw); display: block; transition: color 0.3s ease;">Bánh ngọt & Đồ uống</span>
              <span :style="{ color: isHeroBgDark ? '#FFA07A' : '#C8502A' }" style="font-size: min(52px, 8vw); display: block; transition: color 0.3s ease;">tươi mới mỗi ngày</span>
            </h1>
            
            <!-- Description -->
            <p class="fs-5 text-secondary mb-0 pe-lg-5" style="line-height: 1.6;">
              Giao tận nơi trong 30 phút.<br/>
              Làm từ nguyên liệu tươi sạch mỗi sáng.
            </p>
            
            <!-- CTAs -->
            <div class="d-flex flex-wrap gap-3 mt-2">
              <router-link to="/products" class="sparkle-button btn-lg shadow-sm" style="--btn-glow-color: hsla(25, 80%, 50%, 0.75);">
                <div class="dots_border"></div>
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" class="sparkle">
                  <path class="path" stroke-linejoin="round" stroke-linecap="round"
                    stroke="#E07340" fill="#E07340"
                    d="M14.187 8.096L15 5.25L15.813 8.096C16.0231 8.83114 16.4171 9.50062
                    16.9577 10.0413C17.4984 10.5819 18.1679 10.9759 18.903 11.186L21.75 12L18.904
                    12.813C18.1689 13.0231 17.4994 13.4171 16.9587 13.9577C16.4181 14.4984 16.0241
                    15.1679 15.814 15.903L15 18.75L14.187 15.904C13.9769 15.1689 13.5829 14.4994
                    13.0423 13.9587C12.5016 13.4181 11.8321 13.0241 11.097 12.814L8.25 12L11.096
                    11.187C11.8311 10.9769 12.5006 10.5829 13.0413 10.0423C13.5819 9.50162 13.9759
                    8.83214 14.186 8.097L14.187 8.096Z"/>
                  <path class="path" stroke-linejoin="round" stroke-linecap="round"
                    stroke="#E07340" fill="#E07340"
                    d="M6 14.25L5.741 15.285C5.59267 15.8785 5.28579 16.4206 4.85319 16.8532C4.42059
                    17.2858 3.87853 17.5927 3.285 17.741L2.25 18L3.285 18.259C3.87853 18.4073 4.42059
                    18.7142 4.85319 19.1468C5.28579 19.5794 5.59267 20.1215 5.741 20.715L6 21.75L6.259
                    20.715C6.40725 20.1216 6.71398 19.5796 7.14639 19.147C7.5788 18.7144 8.12065 18.4075
                    8.714 18.259L9.75 18L8.714 17.741C8.12065 17.5925 7.5788 17.2856 7.14639 16.853C6.71398
                    16.4204 6.40725 15.8784 6.259 15.285L6 14.25Z"/>
                  <path class="path" stroke-linejoin="round" stroke-linecap="round"
                    stroke="#E07340" fill="#E07340"
                    d="M6.5 4L6.303 4.5915C6.24777 4.75718 6.15472 4.90774 6.03123 5.03123C5.90774
                    5.15472 5.75718 5.24777 5.5915 5.303L5 5.5L5.5915 5.697C5.75718 5.75223 5.90774
                    5.84528 6.03123 5.96877C6.15472 6.09226 6.24777 6.24282 6.303 6.4085L6.5 7L6.697
                    6.4085C6.75223 6.24282 6.84528 6.09226 6.96877 5.96877C7.09226 5.84528 7.24282
                    5.75223 7.4085 5.697L8 5.5L7.4085 5.303C7.24282 5.24777 7.09226 5.15472 6.96877
                    5.03123C6.84528 4.90774 6.75223 4.75718 6.697 4.5915L6.5 4Z"/>
                </svg>
                <span class="text_button">Đặt hàng ngay</span>
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
            <div class="hero-image-wrapper position-relative mx-auto"
                 @mousemove="handleMouseMove" 
                 @mouseleave="resetTilt"
                 :style="{ transform: 'perspective(1000px) rotateX(' + tiltY + 'deg) rotateY(' + tiltX + 'deg) translateY(' + (scrollY * 0.05) + 'px)' }">
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

    <!-- 2b. STATS COUNTER STRIP -->
    <section class="container py-4 reveal-on-scroll">
      <div ref="statStrip" class="d-flex flex-wrap gap-4 justify-content-center align-items-center py-4">
        <div class="stat-magic-card animate-stagger" style="transition-delay: 0.1s">
          <div class="stat-magic-card-info">
            <div class="stat-value">{{ stats.orders.toLocaleString('vi-VN') }}+</div>
            <div class="stat-label">Tổng đơn hàng</div>
          </div>
        </div>
        <div class="stat-magic-card animate-stagger" style="transition-delay: 0.2s">
          <div class="stat-magic-card-info">
            <div class="stat-value">{{ stats.revenue }}M+</div>
            <div class="stat-label">Doanh thu</div>
          </div>
        </div>
        <div class="stat-magic-card animate-stagger" style="transition-delay: 0.3s">
          <div class="stat-magic-card-info">
            <div class="stat-value">{{ stats.customers.toLocaleString('vi-VN') }}+</div>
            <div class="stat-label">Khách hàng</div>
          </div>
        </div>
        <div class="stat-magic-card animate-stagger" style="transition-delay: 0.4s">
          <div class="stat-magic-card-info">
            <div class="stat-value">{{ (stats.rating / 10).toFixed(1) }}★</div>
            <div class="stat-label">Đánh giá</div>
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
    <section class="py-5 py-lg-6 reveal-on-scroll" style="background: var(--bg-surface)">
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
    <section class="py-5 py-lg-6 reveal-on-scroll" style="background-color: var(--primary-soft);">
      <div class="container">
        <div class="text-center mb-5">
          <h2 class="fw-bold">Khách hàng nói gì? 💬</h2>
        </div>
        
        <div class="testimonial-slider overflow-hidden position-relative">
          <div class="d-flex transition-transform" :style="{ transform: `translateX(-${currentSlide * slideWidth}%)` }" style="transition: transform 0.5s ease-in-out;">
            <div v-for="(review, index) in testimonials" :key="index" class="testimonial-slide px-2">
              <div class="bg-white p-4 rounded-4 shadow-sm h-100 d-flex flex-column" style="background: var(--bg-surface) !important; border: 1px solid var(--border-light); transition: background var(--duration-slow) var(--ease-smooth), border-color var(--duration-slow) var(--ease-smooth);">
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
    <section class="py-5 py-lg-6 reveal-on-scroll" style="background: var(--bg-surface)">
      <div class="container text-center">
        <h2 class="fw-bold mb-5">Tại sao chọn Brevery? ✨</h2>
        <div class="row g-4">
          <div class="col-md-4">
            <div class="feature-magic-card feature-green h-100">
              <div class="feature-magic-card-info">
                <div class="icon-box bg-white text-success rounded-circle shadow-sm d-flex align-items-center justify-content-center mx-auto mb-4">
                  <PhLeaf size="40" weight="duotone" />
                </div>
                <h5 class="fw-bold mb-3" style="color: var(--card-dark-text); transition: color var(--duration-slow) var(--ease-smooth);">Nguyên liệu tự nhiên</h5>
                <p class="fst-italic small mb-0" style="color: var(--card-dark-sub); transition: color var(--duration-slow) var(--ease-smooth);">"Không chất bảo quản — không lo sức khỏe"</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="feature-magic-card feature-orange h-100">
              <div class="feature-magic-card-info">
                <div class="icon-box bg-white text-primary rounded-circle shadow-sm d-flex align-items-center justify-content-center mx-auto mb-4">
                  <PhRocket size="40" weight="duotone" />
                </div>
                <h5 class="fw-bold mb-3" style="color: var(--card-dark-text); transition: color var(--duration-slow) var(--ease-smooth);">Giao hàng cực nhanh</h5>
                <p class="fst-italic small mb-0" style="color: var(--card-dark-sub); transition: color var(--duration-slow) var(--ease-smooth);">"Giao trễ? Hoàn phí ship ngay lập tức"</p>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="feature-magic-card feature-yellow h-100">
              <div class="feature-magic-card-info">
                <div class="icon-box bg-white text-danger rounded-circle shadow-sm d-flex align-items-center justify-content-center mx-auto mb-4">
                  <PhHeart size="40" weight="duotone" />
                </div>
                <h5 class="fw-bold mb-3" style="color: var(--card-dark-text); transition: color var(--duration-slow) var(--ease-smooth);">Đảm bảo hài lòng</h5>
                <p class="fst-italic small mb-0" style="color: var(--card-dark-sub); transition: color var(--duration-slow) var(--ease-smooth);">"Không hài lòng? Hoàn 100% không hỏi"</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 8. GALLERY -->
    <section class="py-5 py-lg-6 reveal-on-scroll" style="background: var(--bg-muted)">
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

    <!-- 9. FAQ -->
    <section class="container py-5 py-lg-6 reveal-on-scroll">
      <div class="text-center mb-5">
        <h2 class="section-title fw-bold">Câu hỏi thường gặp</h2>
      </div>
      <div class="mx-auto" style="max-width: 760px;">
        <div v-for="(f, i) in faqs" :key="i" class="faq-item" :class="{ open: openFaq === i }">
          <button class="faq-question" @click="toggleFaq(i)">
            <span>{{ f.q }}</span>
            <PhCaretDown class="faq-chevron" size="20" weight="bold" />
          </button>
          <div class="faq-answer"><p class="mb-0 pt-1">{{ f.a }}</p></div>
        </div>
      </div>
    </section>

    <!-- Back to top -->
    <button class="back-to-top" :class="{ show: showBackToTop }" @click="scrollToTop" aria-label="Lên đầu trang" title="Lên đầu trang">
      <PhArrowUp size="22" weight="bold" />
    </button>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { productApi } from '@/api/product.api'
import ProductCard from '@/components/ProductCard.vue'
import { 
  PhStorefront, PhShoppingCart, PhList, PhClock, PhLeaf, PhShieldCheck, 
  PhFire, PhCake, PhCookie, PhCoffee, PhSparkle, PhMapPin, PhMoped, 
  PhStar, PhRocket, PhHeart, PhInstagramLogo, PhBag, PhBookOpen, PhCheckCircle, PhArrowRight,
  PhCaretDown, PhArrowUp
} from '@phosphor-icons/vue'

// --- Stats counter strip ---
const statStrip = ref(null)
const stats = ref({ orders: 0, revenue: 0, customers: 0, rating: 0 })
let statsAnimated = false
function animateStats() {
  if (statsAnimated) return
  statsAnimated = true
  const targets = { orders: 25800, revenue: 990, customers: 15400, rating: 49 }
  const duration = 1600
  const start = performance.now()
  function tick(now) {
    const p = Math.min((now - start) / duration, 1)
    const ease = 1 - Math.pow(1 - p, 3)
    stats.value = {
      orders: Math.round(targets.orders * ease),
      revenue: Math.round(targets.revenue * ease),
      customers: Math.round(targets.customers * ease),
      rating: Math.round(targets.rating * ease),
    }
    if (p < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

// --- FAQ ---
const openFaq = ref(0)
function toggleFaq(i) { openFaq.value = openFaq.value === i ? -1 : i }
const faqs = [
  { q: 'Brevery giao hàng trong bao lâu?', a: 'Đơn nội thành thường được giao trong vòng 30 phút. Vào giờ cao điểm có thể lâu hơn một chút, nhưng chúng tôi luôn cập nhật trạng thái đơn theo thời gian thực.' },
  { q: 'Tôi có thể đặt bánh sinh nhật theo yêu cầu không?', a: 'Có. Bạn có thể để lại ghi chú khi đặt hàng hoặc liên hệ trực tiếp; đội ngũ của chúng tôi sẽ tư vấn mẫu bánh, hương vị và thông điệp trên bánh.' },
  { q: 'Nguyên liệu có tươi mỗi ngày không?', a: 'Tất cả bánh được làm mới mỗi sáng từ nguyên liệu chọn lọc. Chúng tôi không bán bánh tồn của ngày hôm trước.' },
  { q: 'Chính sách hoàn tiền như thế nào?', a: 'Nếu bạn chưa hài lòng về chất lượng sản phẩm, chúng tôi hoàn tiền 100% hoặc đổi sản phẩm mới — không cần lý do phức tạp.' },
]

// --- Dynamic Contrast & 3D Parallax Tilt ---
const isHeroBgDark = ref(false)
const tiltX = ref(0)
const tiltY = ref(0)
const scrollY = ref(0)

function handleMouseMove(e) {
  const el = e.currentTarget
  const rect = el.getBoundingClientRect()
  const x = e.clientX - rect.left - rect.width / 2
  const y = e.clientY - rect.top - rect.height / 2
  tiltX.value = (x / rect.width) * 12
  tiltY.value = -(y / rect.height) * 12
}

function resetTilt() {
  tiltX.value = 0
  tiltY.value = 0
}

function checkHeroBrightness() {
  const heroEl = document.querySelector('.hero-section')
  if (!heroEl) return
  const bg = window.getComputedStyle(heroEl).backgroundColor
  const match = bg.match(/\d+/g)
  if (match && match.length >= 3) {
    const r = parseInt(match[0], 10)
    const g = parseInt(match[1], 10)
    const b = parseInt(match[2], 10)
    const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
    isHeroBgDark.value = luminance < 0.5
  }
}

// --- Scroll progress + back-to-top ---
const scrollProgress = ref(0)
const showBackToTop = ref(false)
function handleScroll() {
  const h = document.documentElement
  const scrolled = h.scrollTop
  const height = h.scrollHeight - h.clientHeight
  scrollProgress.value = height > 0 ? (scrolled / height) * 100 : 0
  showBackToTop.value = scrolled > 500
  scrollY.value = scrolled
}
function scrollToTop() { window.scrollTo({ top: 0, behavior: 'smooth' }) }

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

  // Stats count-up when strip enters viewport
  if (statStrip.value) {
    const statObserver = new IntersectionObserver((entries, obs) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) { animateStats(); obs.disconnect() }
      })
    }, { threshold: 0.4 })
    statObserver.observe(statStrip.value)
  }

  // Scroll progress + back-to-top
  window.addEventListener('scroll', handleScroll, { passive: true })
  handleScroll()

  // Dynamic Contrast Detection
  let themeMutationObserver = null;
  nextTick(() => {
    checkHeroBrightness()
    // Observe theme attribute mutation
    const observerTarget = document.documentElement
    themeMutationObserver = new MutationObserver(() => {
      checkHeroBrightness()
    })
    themeMutationObserver.observe(observerTarget, { attributes: true, attributeFilter: ['data-theme'] })
  })
  
  onUnmounted(() => {
    if (themeMutationObserver) themeMutationObserver.disconnect()
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('scroll', handleScroll)
  clearInterval(slideInterval)
  if (observer) observer.disconnect()
})
</script>

<style scoped>
/* GENERAL ANIMATIONS */
.reveal-on-scroll {
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.8s var(--ease-spring), transform 0.8s var(--ease-spring);
}
.reveal-on-scroll.visible {
  opacity: 1;
  transform: translateY(0);
}

.animate-fade-up {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s var(--ease-spring), transform 0.6s var(--ease-spring);
}
.animate-fade-up.visible {
  opacity: 1;
  transform: translateY(0);
}

/* HERO SECTION */
.hero-section {
  background: var(--primary-soft);
  min-height: 80vh;
  display: flex;
  align-items: center;
  transition: background-color var(--duration-slow) var(--ease-smooth);
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
  background: var(--primary);
  color: #FFFFFF;
  border: none;
  transition: all var(--duration-base) var(--ease-out);
}
.btn-hero-primary:hover {
  background: var(--primary-dark);
  color: #FFFFFF;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(200, 90, 50, 0.4) !important;
}

.btn-hero-secondary {
  background: transparent;
  border: 2px solid var(--primary);
  color: var(--primary);
  transition: all var(--duration-base) var(--ease-out);
}
.btn-hero-secondary:hover {
  background: var(--primary);
  color: #FFFFFF;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(200, 90, 50, 0.3) !important;
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

/* Floating Cards with Orbit animations */
.float-card {
  z-index: 4;
  background: var(--bg-surface) !important;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-soft);
  transition: all 0.3s ease;
}
.float-card:hover {
  transform: scale(1.1) !important;
  box-shadow: var(--shadow-hover);
}
.card-1 { 
  top: 10%; left: -5%; 
  animation: orbit1 8s ease-in-out infinite; 
}
.card-2 { 
  bottom: 20%; right: -5%; 
  animation: orbit2 9s ease-in-out infinite; 
}
.card-3 { 
  bottom: 5%; left: 10%; 
  animation: orbit3 10s ease-in-out infinite; 
}

@keyframes orbit1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(12px, -8px) scale(1.02); }
  66% { transform: translate(-8px, 12px) scale(0.98); }
}
@keyframes orbit2 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  40% { transform: translate(-10px, -12px) scale(0.97); }
  70% { transform: translate(12px, 8px) scale(1.03); }
}
@keyframes orbit3 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  30% { transform: translate(15px, 10px) scale(1.04); }
  60% { transform: translate(-12px, -8px) scale(0.96); }
}

/* Stagger animation for Stats count-up */
.animate-stagger {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.8s var(--ease-spring), transform 0.8s var(--ease-spring);
}
.reveal-on-scroll.visible .animate-stagger {
  opacity: 1;
  transform: translateY(0);
}

/* CATEGORY TABS */
.btn-category {
  background: var(--bg-surface);
  border: 1.5px solid var(--border-color);
  color: var(--bakery-text);
  transition: all 0.35s var(--ease-smooth);
}
.btn-category:hover {
  border-color: var(--bakery-primary);
  background: var(--primary-light);
  color: var(--primary);
  transform: translateY(-1px);
}
.btn-category.active {
  background: var(--bakery-primary);
  color: white;
  border-color: var(--bakery-primary);
  box-shadow: 0 4px 16px -4px rgba(200, 90, 50, 0.4);
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

/* SPARKLE BUTTON */
.sparkle-button {
  --black-75: rgba(0, 0, 0, 0.75);
  --border-color: rgba(255, 255, 255, 0.1);
  --btn-glow-color: hsla(25, 80%, 50%, 0.75);
  --bg-color: #1a0f0a;
  
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.75rem;
  border-radius: 9999px;
  background-color: var(--bg-color);
  color: #fff;
  border: 1px solid var(--border-color);
  position: relative;
  overflow: visible;
  cursor: pointer;
  font-weight: bold;
  text-decoration: none;
  transition: transform 0.2s, background-color 0.3s, box-shadow 0.3s;
  z-index: 1;
}
.sparkle-button:hover {
  background-color: #261710;
  transform: translateY(-2px);
  box-shadow: 0 0 20px var(--btn-glow-color);
  color: #fff;
}
.sparkle-button:active {
  transform: scale(0.98);
}
.sparkle-button .text_button {
  color: #FAF2E9;
  letter-spacing: 0.05em;
  z-index: 2;
}
.sparkle-button .sparkle {
  width: 18px;
  height: 18px;
  z-index: 2;
  transition: transform 0.3s ease;
}
.sparkle-button:hover .sparkle {
  transform: scale(1.2) rotate(15deg);
  animation: sparkle-pulse-hero 1.5s infinite alternate;
}
@keyframes sparkle-pulse-hero {
  0% { transform: scale(1.1) rotate(0deg); }
  100% { transform: scale(1.3) rotate(30deg); }
}
.sparkle-button .dots_border {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  border-radius: 9999px;
  border: 1px dashed rgba(255, 255, 255, 0.4);
  pointer-events: none;
  opacity: 0.6;
  z-index: 0;
  animation: rotate-border-hero 20s linear infinite;
}
@keyframes rotate-border-hero {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* STAT MAGIC CARDS */
.stat-magic-card {
  --background: linear-gradient(135deg, var(--primary) 0%, #f5e6d3 100%);
  width: 220px;
  height: 140px;
  padding: 3px;
  border-radius: 1rem;
  overflow: visible;
  background: var(--background);
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.45s var(--ease-spring), box-shadow 0.45s var(--ease-smooth);
}
.stat-magic-card::after {
  position: absolute;
  content: "";
  top: 15px; left: 0; right: 0;
  z-index: -1;
  height: 100%; width: 100%;
  transform: scale(0.85);
  filter: blur(20px);
  background: var(--background);
  opacity: 0.5;
  transition: opacity 0.5s var(--ease-smooth);
}
.stat-magic-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: var(--shadow-hover);
}
.stat-magic-card:hover::after {
  opacity: 0;
}
.stat-magic-card-info {
  background: var(--card-dark-bg);
  color: var(--card-dark-text);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  border-radius: 0.7rem;
  padding: 1rem;
  border: 1px solid var(--card-dark-border);
  transition: background-color var(--duration-slow) var(--ease-smooth),
              border-color var(--duration-slow) var(--ease-smooth);
}
.stat-magic-card .stat-value {
  font-size: 2rem;
  font-weight: 800;
  color: var(--card-dark-accent);
  margin-bottom: 0.25rem;
  transition: color var(--duration-slow) var(--ease-smooth);
}
.stat-magic-card .stat-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--card-dark-sub);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  transition: color var(--duration-slow) var(--ease-smooth);
}

/* FEATURE MAGIC CARDS */
.feature-magic-card {
  padding: 3px;
  border-radius: 1rem;
  overflow: visible;
  position: relative;
  z-index: 1;
  display: flex;
  transition: transform 0.45s var(--ease-spring), box-shadow 0.45s var(--ease-smooth);
}
.feature-green {
  --background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  --feature-bg: #E3F0E4;
  --feature-text: #1a5c2a;
  --feature-sub: #3d7c4c;
  background: var(--background);
}
.feature-orange {
  --background: linear-gradient(135deg, #ff9f43 0%, #ee5253 100%);
  --feature-bg: #FFF0E6;
  --feature-text: #8B3210;
  --feature-sub: #b54f2a;
  background: var(--background);
}
.feature-yellow {
  --background: linear-gradient(135deg, #f1c40f 0%, #f39c12 100%);
  --feature-bg: #FEF9E7;
  --feature-text: #7d6608;
  --feature-sub: #a38a10;
  background: var(--background);
}
.feature-magic-card::after {
  position: absolute;
  content: "";
  top: 15px; left: 0; right: 0;
  z-index: -1;
  height: 100%; width: 100%;
  transform: scale(0.85);
  filter: blur(20px);
  background: var(--background);
  opacity: 0;
  transition: opacity 0.45s var(--ease-smooth);
}
.feature-magic-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: var(--shadow-hover);
}
.feature-magic-card:hover::after {
  opacity: 0.65;
}
.feature-magic-card-info {
  background: var(--card-dark-bg);
  width: 100%;
  height: 100%;
  border-radius: 0.75rem;
  padding: 2.5rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid var(--card-dark-border);
  transition: background-color var(--duration-slow) var(--ease-smooth),
              border-color var(--duration-slow) var(--ease-smooth);
}
</style>
