(function () {
  function setStatus(message) {
    var statusNode = document.getElementById("voice-status");
    if (statusNode) statusNode.textContent = message || "";
  }

  async function recordOnce() {
    var stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    var mediaRecorder = new MediaRecorder(stream);
    var chunks = [];
    mediaRecorder.ondataavailable = function (event) {
      if (event.data.size > 0) chunks.push(event.data);
    };
    return new Promise(function (resolve, reject) {
      mediaRecorder.onerror = reject;
      mediaRecorder.onstop = function () {
        stream.getTracks().forEach(function (track) {
          track.stop();
        });
        resolve(new Blob(chunks, { type: mediaRecorder.mimeType || "audio/webm" }));
      };
      mediaRecorder.start();
      setTimeout(function () {
        if (mediaRecorder.state === "recording") mediaRecorder.stop();
      }, 8000);
      window.__stopVoiceRecording = function () {
        if (mediaRecorder.state === "recording") mediaRecorder.stop();
      };
    });
  }

  async function transcribeInto(textarea) {
    setStatus("Listening… tap Voice again to stop, or wait 8s.");
    try {
      var blob = await recordOnce();
      setStatus("Transcribing…");
      var formData = new FormData();
      formData.append("file", blob, "voice.webm");
      var activeAgent = document.querySelector("[data-agent-id].is-active");
      if (activeAgent) formData.append("agentId", activeAgent.getAttribute("data-agent-id"));
      var response = await fetch("/htmx/voice/stt", { method: "POST", body: formData });
      if (!response.ok) {
        setStatus("Voice input failed — type instead.");
        return;
      }
      var transcript = await response.text();
      textarea.value = (textarea.value ? textarea.value + " " : "") + transcript.trim();
      setStatus("Transcript added.");
    } catch (error) {
      setStatus("Voice input failed — type instead.");
    }
  }

  async function speakText(text) {
    if (!text) {
      setStatus("Nothing to speak yet.");
      return;
    }
    setStatus("Generating speech…");
    var params = new URLSearchParams();
    params.set("text", text);
    var activeAgent = document.querySelector("[data-agent-id].is-active");
    if (activeAgent) params.set("agentId", activeAgent.getAttribute("data-agent-id"));
    try {
      var response = await fetch("/htmx/voice/tts", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: params.toString(),
      });
      if (!response.ok) {
        setStatus("Spoken reply unavailable — read the text instead.");
        return;
      }
      var audioBlob = await response.blob();
      var audioUrl = URL.createObjectURL(audioBlob);
      var audio = new Audio(audioUrl);
      audio.play();
      setStatus("Playing spoken reply.");
    } catch (error) {
      setStatus("Spoken reply unavailable — read the text instead.");
    }
  }

  document.addEventListener("click", function (event) {
    var voiceJd = event.target.closest("[data-voice-jd]");
    if (voiceJd) {
      var textarea = document.querySelector("#job-matching-form textarea");
      if (textarea) transcribeInto(textarea);
    }
    var voiceChat = event.target.closest("[data-voice-chat]");
    if (voiceChat) {
      var textarea = document.querySelector("#agent-chat-form textarea");
      if (textarea) transcribeInto(textarea);
    }
    var speakReply = event.target.closest("[data-speak-reply]");
    if (speakReply) {
      var output = document.querySelector("#agent-chat-output") || document.querySelector("#job-match-output");
      speakText(output ? output.innerText : "");
    }
  });
})();
