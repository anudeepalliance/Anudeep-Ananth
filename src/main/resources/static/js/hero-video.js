(function () {
  var videoFrame = document.querySelector(".hero-video-frame");
  if (!videoFrame) return;
  var introVideo = videoFrame.querySelector("video");
  var muteToggleButton = videoFrame.querySelector(".hero-video-mute");
  var seekBar = videoFrame.querySelector(".hero-video-seek");
  if (!introVideo || !muteToggleButton || !seekBar) return;

  introVideo.muted = true;
  muteToggleButton.classList.add("is-muted");
  muteToggleButton.setAttribute("aria-label", "Unmute intro video");

  function seekBarMaximum() {
    return Number(seekBar.max) || 1000;
  }

  function syncSeekBarToPlayback() {
    if (!introVideo.duration) return;
    if (seekBar.dataset.isDragging === "true") return;
    seekBar.value = String(
      Math.round((introVideo.currentTime / introVideo.duration) * seekBarMaximum()),
    );
  }

  function applySeekBarToVideo() {
    if (!introVideo.duration) return;
    introVideo.currentTime =
      (Number(seekBar.value) / seekBarMaximum()) * introVideo.duration;
  }

  muteToggleButton.addEventListener("click", function () {
    introVideo.muted = !introVideo.muted;
    muteToggleButton.classList.toggle("is-muted", introVideo.muted);
    muteToggleButton.setAttribute(
      "aria-label",
      introVideo.muted ? "Unmute intro video" : "Mute intro video",
    );
    var playPromise = introVideo.play();
    if (playPromise && playPromise.catch) playPromise.catch(function () {});
  });

  seekBar.addEventListener("pointerdown", function () {
    seekBar.dataset.isDragging = "true";
  });
  window.addEventListener("pointerup", function () {
    seekBar.dataset.isDragging = "false";
  });
  seekBar.addEventListener("input", applySeekBarToVideo);
  seekBar.addEventListener("change", applySeekBarToVideo);

  introVideo.addEventListener("timeupdate", syncSeekBarToPlayback);
  introVideo.addEventListener("loadedmetadata", syncSeekBarToPlayback);
})();
