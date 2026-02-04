<section>
  <div id="home"
    style="
      width: 100%;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      background: linear-gradient(135deg,#33c8c1,#66d1e0);
      position: relative;
      overflow: hidden;
      color: #fff;
      text-align: center;
      padding: 60px 15px;
    "
  >
    <!-- Pulsing circles animation behind text -->
    <div style="position:absolute; top:50%; left:50%; width:400px; height:400px; transform:translate(-50%,-50%); z-index:0;">
      <div style="position:absolute; width:100%; height:100%; border-radius:50%; background:rgba(255,255,255,0.1); animation:pulseCircle 4s infinite ease-in-out;"></div>
      <div style="position:absolute; width:70%; height:70%; top:15%; left:15%; border-radius:50%; background:rgba(255,255,255,0.08); animation:pulseCircle 5s infinite ease-in-out;"></div>
    </div>

    <div style="max-width:900px; width:100%; position:relative; z-index:1;">
      <br>
      <h1 style="font-size:36px; font-weight:700; line-height:1.3; margin-bottom:20px;">
        Automate Hiring, Marketing &amp; Sales Flow
      </h1>
      <p style="font-size:18px; font-weight:400; line-height:1.6; margin-bottom:0px;">
        Streamline customer engagement, track leads, and close deals faster — <strong>completely free to start</strong>, scalable for your team.
      </p>

      <div style="text-align:center; color:#fff; margin-top:26px; background:rgba(0,0,0,0.10); backdrop-filter:blur(6px); padding:10px 0;">
        <div style="font-weight:700; font-size:13px; opacity:0.9; letter-spacing:1px; margin-bottom:4px;">
          AI Call Demo Line
        </div>

        <div style="font-weight:700; font-size:20px; margin-bottom:10px; position:relative; display:inline-block; padding-bottom:4px;">
          📞 +91 7042149841
          <span style="position:absolute; bottom:0; left:0; width:100%; height:3px; background:linear-gradient(90deg,#33c8c1,#119bd2); border-radius:2px;"></span>
        </div>

        <div style="font-size:13px; opacity:0.95; line-height:1.4;">
          Talk to our <strong>AI call agent</strong> live and see it in action.
        </div>
      </div>

      <br><br><br>
      <a href="https://app.mylinehub.com"
        style="
          display:inline-block;
          padding:14px 30px;
          font-size:16px;
          font-weight:600;
          color:#fff;
          border-radius:8px;
          background: linear-gradient(90deg,#33c8c1,#119bd2);
          text-decoration:none;
          box-shadow:0 6px 15px rgba(0,0,0,0.2);
          transition: all 0.3s;
        "
        onmouseover="this.style.transform='scale(1.05)'; this.style.background='linear-gradient(90deg,#119bd2,#33c8c1)'"
        onmouseout="this.style.transform='scale(1)'; this.style.background='linear-gradient(90deg,#33c8c1,#119bd2)'"
      >
        Start Free Now
      </a>
    </div>

    <!-- Moving arrows -->
    <div style="position:absolute; bottom:40px; width:100%; display:flex; justify-content:center; gap:40px; z-index:1;">
      <div style="position:relative; width:60px; height:60px; overflow:visible;">
        <div style="position:absolute; width:20px; height:20px; background:#fff; clip-path:polygon(0% 50%,100% 0%,100% 100%); animation:moveArrow1 4s linear infinite;"></div>
      </div>
      <div style="position:relative; width:60px; height:60px; overflow:visible;">
        <div style="position:absolute; width:20px; height:20px; background:#fff; clip-path:polygon(0% 50%,100% 0%,100% 100%); animation:moveArrow2 5s linear infinite;"></div>
      </div>
      <div style="position:relative; width:60px; height:60px; overflow:visible;">
        <div style="position:absolute; width:20px; height:20px; background:#fff; clip-path:polygon(0% 50%,100% 0%,100% 100%); animation:moveArrow3 6s linear infinite;"></div>
      </div>
    </div>

    <style>
      @keyframes pulseCircle {
        0% { transform: scale(0.8); opacity:0.4; }
        50% { transform: scale(1); opacity:0.1; }
        100% { transform: scale(0.8); opacity:0.4; }
      }
      @keyframes moveArrow1 {
        0% { transform: translate(0,0) rotate(0deg);}
        25% { transform: translate(40px,-20px) rotate(45deg);}
        50% { transform: translate(80px,0) rotate(90deg);}
        75% { transform: translate(40px,20px) rotate(135deg);}
        100% { transform: translate(0,0) rotate(180deg);}
      }
      @keyframes moveArrow2 {
        0% { transform: translate(0,0) rotate(0deg);}
        25% { transform: translate(50px,-25px) rotate(45deg);}
        50% { transform: translate(100px,0) rotate(90deg);}
        75% { transform: translate(50px,25px) rotate(135deg);}
        100% { transform: translate(0,0) rotate(180deg);}
      }
      @keyframes moveArrow3 {
        0% { transform: translate(0,0) rotate(0deg);}
        25% { transform: translate(60px,-30px) rotate(45deg);}
        50% { transform: translate(120px,0) rotate(90deg);}
        75% { transform: translate(60px,30px) rotate(135deg);}
        100% { transform: translate(0,0) rotate(180deg);}
      }
      @media(max-width:768px){
        #home h1 { font-size:28px; }
        #home p { font-size:16px; }
      }
    </style>

  </div>
</section>
