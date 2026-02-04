<!--====== OFFERS PART START ======-->
<section id="offers" style="padding:50px 0; background:linear-gradient(135deg,#f9fafc,#e9f5ff); font-family:'Segoe UI',sans-serif;">
  <div style="max-width:1200px; margin:auto; text-align:center;">

    <!-- Section Title -->
    <div style="margin-bottom:35px;">
      <div style="width:60px; height:4px; background:#119bd2; margin:0 auto 12px auto; border-radius:4px;"></div>
      <h3 style="font-size:26px; font-weight:700; color:#0b4b8c; margin-bottom:10px;">
        Automate Sales Flow,
        <span style="font-weight:400; color:#333;"> Let AI Speed Up Your Work!</span>
      </h3>
    </div>

    <!-- Dual Banner Section -->
    <div class="offers-flex" style="display:flex; flex-wrap:wrap; gap:25px; justify-content:center; align-items:stretch;">

      <!-- Banner 1 -->
      <div class="offer-box" style="
        flex:1 1 48%;
        min-width:300px;
        background:#fff;
        border-radius:15px;
        overflow:hidden;
        box-shadow:0 8px 22px rgba(0,0,0,0.08);
        transition:all 0.4s ease;
        animation:floatA 5s ease-in-out infinite alternate;
      "
        onmouseover="this.style.transform='translateY(-8px) scale(1.02)'; this.style.boxShadow='0 12px 30px rgba(0,0,0,0.15)';"
        onmouseout="this.style.transform='translateY(0px) scale(1)'; this.style.boxShadow='0 8px 22px rgba(0,0,0,0.08)';">

        <img src="<?= BASE_URL ?>assets/images/mylinehubsuite.JPG" alt="MyLineHub Suite"
          style="width:100%; height:240px; object-fit:cover; border-bottom:4px solid #119bd2;">
        <div style="padding:18px 15px;">
          <h4 style="font-size:19px; color:#0b4b8c; font-weight:600; margin-bottom:8px;">MyLineHub Suite</h4>
          <p style="font-size:15px; color:#333; line-height:1.6; margin:0;">
            End-to-end AI automation — streamline your customer journey from chat to sale. Integrate WhatsApp, IVR, and more with zero complexity.
          </p>
        </div>
      </div>

      <!-- Banner 2 -->
      <div class="offer-box" style="
        flex:1 1 48%;
        min-width:300px;
        background:#fff;
        border-radius:15px;
        overflow:hidden;
        box-shadow:0 8px 22px rgba(0,0,0,0.08);
        transition:all 0.4s ease;
        animation:floatB 6s ease-in-out infinite alternate;
      "
        onmouseover="this.style.transform='translateY(-8px) scale(1.02)'; this.style.boxShadow='0 12px 30px rgba(0,0,0,0.15)';"
        onmouseout="this.style.transform='translateY(0px) scale(1)'; this.style.boxShadow='0 8px 22px rgba(0,0,0,0.08)';">

        <img src="<?= BASE_URL ?>assets/images/firstflowfree.JPG" alt="First Flow Free"
          style="width:100%; height:240px; object-fit:cover; border-bottom:4px solid #33c8c1;">
        <div style="padding:18px 15px;">
          <h4 style="font-size:19px; color:#0b4b8c; font-weight:600; margin-bottom:8px;">First Flow Free</h4>
          <p style="font-size:15px; color:#333; line-height:1.6; margin:0;">
            Start instantly with your first WhatsApp flow absolutely free — test, scale, and grow with AI assistance built for speed and efficiency.
          </p>
        </div>
      </div>

    </div>

    <!-- Inline Animations & Responsive Fix -->
    <style>
      @keyframes floatA {
        0% { transform: translateY(0px); }
        100% { transform: translateY(-10px); }
      }
      @keyframes floatB {
        0% { transform: translateY(-8px); }
        100% { transform: translateY(6px); }
      }

      @media(max-width:768px){
        #offers { padding:40px 15px; }
        #offers h3 { font-size:22px; }

        .offers-flex { gap:20px; }

        .offer-box { flex:1 1 100%; margin:0 10px; }

        #offers img {
          height:200px !important;
          object-fit:contain !important;
          background-color:#fff;
        }

        #offers h4 { font-size:17px !important; }

        #offers p {
          font-size:14px !important;
          line-height:1.5;
        }
      }

      @media(max-width:480px){
        .offer-box { margin:0 12px; }
      }
    </style>

  </div>
</section>
<!--====== OFFERS PART ENDS ======-->
