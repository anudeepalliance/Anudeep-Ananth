(function () {
  var lightbox = document.getElementById("proof-lightbox");
  var lightboxImage = document.getElementById("proof-lightbox-image");
  var lightboxCaption = document.getElementById("proof-lightbox-caption");
  if (!lightbox || !lightboxImage) return;

  var proofButtons = Array.prototype.slice.call(
    document.querySelectorAll("[data-lightbox-src]"),
  );
  var openIndex = 0;

  function openLightbox(index) {
    if (!proofButtons.length) return;
    openIndex = (index + proofButtons.length) % proofButtons.length;
    var button = proofButtons[openIndex];
    lightboxImage.src = button.getAttribute("data-lightbox-src");
    lightboxImage.alt = button.getAttribute("data-lightbox-alt") || "";
    if (lightboxCaption) lightboxCaption.textContent = lightboxImage.alt;
    lightbox.hidden = false;
    document.body.classList.add("lightbox-open");
  }

  function closeLightbox() {
    lightbox.hidden = true;
    lightboxImage.removeAttribute("src");
    document.body.classList.remove("lightbox-open");
  }

  proofButtons.forEach(function (button, index) {
    button.addEventListener("click", function (event) {
      event.preventDefault();
      openLightbox(index);
    });
  });

  lightbox.addEventListener("click", function (event) {
    if (event.target === lightbox) closeLightbox();
  });

  var closeButton = lightbox.querySelector(".lightbox-close");
  if (closeButton) closeButton.addEventListener("click", closeLightbox);

  var prevButton = lightbox.querySelector(".lightbox-prev");
  if (prevButton) {
    prevButton.addEventListener("click", function (event) {
      event.stopPropagation();
      openLightbox(openIndex - 1);
    });
  }

  var nextButton = lightbox.querySelector(".lightbox-next");
  if (nextButton) {
    nextButton.addEventListener("click", function (event) {
      event.stopPropagation();
      openLightbox(openIndex + 1);
    });
  }

  document.addEventListener("keydown", function (event) {
    if (lightbox.hidden) return;
    if (event.key === "Escape") closeLightbox();
    if (event.key === "ArrowLeft") openLightbox(openIndex - 1);
    if (event.key === "ArrowRight") openLightbox(openIndex + 1);
  });
})();
