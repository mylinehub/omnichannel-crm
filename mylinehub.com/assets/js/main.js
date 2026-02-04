(function () {
  "use strict";

  // ==== Preloader
  window.onload = function () {
    window.setTimeout(fadeout, 500);
  };

  function fadeout() {
    const p = document.querySelector(".preloader");
    if (!p) return;
    p.style.opacity = "0";
    p.style.display = "none";
  }

  // ======= Sticky (NO logo swapping)
  window.onscroll = function () {
    const header_navbar = document.querySelector(".navbar-area");
    if (header_navbar) {
      const sticky = header_navbar.offsetTop;

      if (window.pageYOffset > sticky) {
        header_navbar.classList.add("sticky");
      } else {
        header_navbar.classList.remove("sticky");
      }
    }

    // show or hide the back-to-top button
    const backToTop = document.querySelector(".back-to-top");
    if (backToTop) {
      if (document.body.scrollTop > 50 || document.documentElement.scrollTop > 50) {
        backToTop.style.display = "flex";
      } else {
        backToTop.style.display = "none";
      }
    }
  };

  // ==== Smooth scroll ONLY for in-page anchors (#...)
  const pageLink = document.querySelectorAll(".page-scroll");
  pageLink.forEach((elem) => {
    elem.addEventListener("click", (e) => {
      const href = elem.getAttribute("href") || "";

      // Only intercept if it's a real in-page anchor like "#pricing"
      if (!href.startsWith("#")) return;

      const target = document.querySelector(href);
      if (!target) return;

      e.preventDefault();
      target.scrollIntoView({ behavior: "smooth" });
    });
  });

  // ==== section menu active (ONLY if the target exists)
  function onScroll() {
    const sections = document.querySelectorAll(".page-scroll");
    const scrollPos =
      window.pageYOffset ||
      document.documentElement.scrollTop ||
      document.body.scrollTop;

    for (let i = 0; i < sections.length; i++) {
      const currLink = sections[i];
      const val = currLink.getAttribute("href") || "";

      // Only for in-page anchors
      if (!val.startsWith("#")) continue;

      const refElement = document.querySelector(val);
      if (!refElement) continue;

      const scrollTopMinus = scrollPos + 73;

      if (
        refElement.offsetTop <= scrollTopMinus &&
        refElement.offsetTop + refElement.offsetHeight > scrollTopMinus
      ) {
        document.querySelectorAll(".page-scroll.active").forEach((x) => x.classList.remove("active"));
        currLink.classList.add("active");
      } else {
        currLink.classList.remove("active");
      }
    }
  }
  window.document.addEventListener("scroll", onScroll);

  //===== close navbar-collapse when a page-scroll clicked (mobile)
  const navbarToggler = document.querySelector(".navbar-toggler");
  const navbarCollapse = document.querySelector(".navbar-collapse");

  document.querySelectorAll(".page-scroll").forEach((e) =>
    e.addEventListener("click", () => {
      if (navbarToggler) navbarToggler.classList.remove("active");
      if (navbarCollapse) navbarCollapse.classList.remove("show");
    })
  );

  if (navbarToggler) {
    navbarToggler.addEventListener("click", function () {
      navbarToggler.classList.toggle("active");
    });
  }

  // ========= glightbox (your videos)
  if (typeof GLightbox !== "undefined") {
    GLightbox({
      selector: ".video1",
      href: "https://www.youtube.com/watch?v=cA_9uSfwsLg",
      type: "video",
      source: "youtube",
      width: 900,
      autoplayVideos: true,
    });

    GLightbox({ selector: ".video2", href: "https://www.youtube.com/watch?v=yTHPJmd_k-w", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video3", href: "https://www.youtube.com/watch?v=xE9GYn88sOM", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video4", href: "https://www.youtube.com/watch?v=gKCtp7R2Rbo", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video5", href: "https://www.youtube.com/watch?v=3C4KTQoD3Zk", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video6", href: "https://www.youtube.com/watch?v=b8iSfUXtP34", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video7", href: "https://www.youtube.com/watch?v=dWhh2zw6pWY", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video8", href: "https://www.youtube.com/watch?v=7tzY9i2Qrlw", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video9", href: "https://www.youtube.com/watch?v=MAgmsUZE88E", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video10", href: "https://www.youtube.com/watch?v=9hSVMFxjWmg", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video11", href: "https://www.youtube.com/watch?v=0iTdby8PzIg", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video12", href: "https://www.youtube.com/watch?v=jxKxrIqVKgc", type: "video", source: "youtube", width: 900, autoplayVideos: true });
    GLightbox({ selector: ".video13", href: "https://www.youtube.com/watch?v=CVOhF2V6xlA", type: "video", source: "youtube", width: 900, autoplayVideos: true });
  }

  //====== counter up
  if (typeof counterUp !== "undefined") {
    const cu = new counterUp({
      start: 0,
      duration: 2000,
      intvalues: true,
      interval: 100,
      append: "k+",
    });
    cu.start();
  }

  //=====  WOW active
  if (typeof WOW !== "undefined") {
    new WOW().init();
  }

  //=====  particles (FULL configs)
  if (typeof particlesJS !== "undefined") {
    if (document.getElementById("particles-1"))
      particlesJS("particles-1", {
        particles: {
          number: { value: 40, density: { enable: !0, value_area: 4000 } },
          color: { value: ["#FFFFFF", "#FFFFFF", "#FFFFFF"] },
          shape: {
            type: "circle",
            stroke: { width: 0, color: "#fff" },
            polygon: { nb_sides: 5 },
            image: { src: "img/github.svg", width: 33, height: 33 },
          },
          opacity: {
            value: 0.15,
            random: !0,
            anim: { enable: !0, speed: 0.2, opacity_min: 0.15, sync: !1 },
          },
          size: {
            value: 50,
            random: !0,
            anim: { enable: !0, speed: 2, size_min: 5, sync: !1 },
          },
          line_linked: { enable: !1, distance: 150, color: "#ffffff", opacity: 0.4, width: 1 },
          move: {
            enable: !0,
            speed: 1,
            direction: "top",
            random: !0,
            straight: !1,
            out_mode: "out",
            bounce: !1,
            attract: { enable: !1, rotateX: 600, rotateY: 600 },
          },
        },
        interactivity: {
          detect_on: "canvas",
          events: { onhover: { enable: !1, mode: "bubble" }, onclick: { enable: !1, mode: "repulse" }, resize: !0 },
          modes: {
            grab: { distance: 400, line_linked: { opacity: 1 } },
            bubble: { distance: 250, size: 0, duration: 2, opacity: 0, speed: 3 },
            repulse: { distance: 400, duration: 0.4 },
            push: { particles_nb: 4 },
            remove: { particles_nb: 2 },
          },
        },
        retina_detect: !0,
      });

    if (document.getElementById("particles-2"))
      particlesJS("particles-2", {
        particles: {
          number: { value: 40, density: { enable: !0, value_area: 4000 } },
          color: { value: ["#FFFFFF", "#FFFFFF", "#FFFFFF"] },
          shape: {
            type: "circle",
            stroke: { width: 0, color: "#fff" },
            polygon: { nb_sides: 5 },
            image: { src: "img/github.svg", width: 33, height: 33 },
          },
          opacity: {
            value: 0.15,
            random: !0,
            anim: { enable: !0, speed: 0.2, opacity_min: 0.15, sync: !1 },
          },
          size: {
            value: 50,
            random: !0,
            anim: { enable: !0, speed: 2, size_min: 5, sync: !1 },
          },
          line_linked: { enable: !1, distance: 150, color: "#ffffff", opacity: 0.4, width: 1 },
          move: {
            enable: !0,
            speed: 1,
            direction: "top",
            random: !0,
            straight: !1,
            out_mode: "out",
            bounce: !1,
            attract: { enable: !1, rotateX: 600, rotateY: 600 },
          },
        },
        interactivity: {
          detect_on: "canvas",
          events: { onhover: { enable: !1, mode: "bubble" }, onclick: { enable: !1, mode: "repulse" }, resize: !0 },
          modes: {
            grab: { distance: 400, line_linked: { opacity: 1 } },
            bubble: { distance: 250, size: 0, duration: 2, opacity: 0, speed: 3 },
            repulse: { distance: 400, duration: 0.4 },
            push: { particles_nb: 4 },
            remove: { particles_nb: 2 },
          },
        },
        retina_detect: !0,
      });
  }
})();
