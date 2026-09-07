package web

import knowledge.ContactDetails
import knowledge.HomepageSalesPitch
import kotlinx.html.ButtonType
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.html.unsafe
import web.seo.personAndWebsiteJsonLdScript

/**
 * Full HTML document shell for the portfolio homepage.
 * Content is visible on first byte — no opacity:0 preloader.
 */
fun HTML.renderPortfolioHtmlDocument() {
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        meta(name = "description", content = HomepageSalesPitch.PARAGRAPH.take(160))
        meta(name = "robots", content = "index, follow")
        link(rel = "canonical", href = "${ContactDetails.PORTFOLIO_URL}/")
        link(rel = "icon", href = "/static/favicon-32.png") {
            attributes["type"] = "image/png"
            attributes["sizes"] = "32x32"
        }
        link(rel = "icon", href = "/static/favicon-16.png") {
            attributes["type"] = "image/png"
            attributes["sizes"] = "16x16"
        }
        link(rel = "apple-touch-icon", href = "/static/apple-touch-icon.png") {
            attributes["sizes"] = "180x180"
        }
        meta(content = "${ContactDetails.FULL_NAME} — AI-native lifecycle & demand gen") {
            attributes["property"] = "og:title"
        }
        meta(content = HomepageSalesPitch.HEADLINE) {
            attributes["property"] = "og:description"
        }
        meta(content = "website") {
            attributes["property"] = "og:type"
        }
        meta(content = "${ContactDetails.PORTFOLIO_URL}/") {
            attributes["property"] = "og:url"
        }
        meta(content = "${ContactDetails.PORTFOLIO_URL}/static/images/anudeep-hero.jpg") {
            attributes["property"] = "og:image"
        }
        meta(name = "twitter:card", content = "summary_large_image")
        meta(name = "twitter:title", content = "${ContactDetails.FULL_NAME} — AI-native lifecycle & demand gen")
        meta(name = "twitter:description", content = HomepageSalesPitch.HEADLINE)
        title("${ContactDetails.FULL_NAME} — AI-native lifecycle & demand gen")
        script(type = "application/ld+json") {
            unsafe { +personAndWebsiteJsonLdScript() }
        }
        link(
            rel = "stylesheet",
            href = "https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Caveat:wght@500;700&family=Manrope:wght@400;600;700;800&display=swap",
        )
        link(rel = "stylesheet", href = "/static/css/lab.css")
        script {
            unsafe {
                +"""
                if (!window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
                  document.documentElement.classList.add("motion-on");
                }
                """.trimIndent()
            }
        }
        script {
            src = "https://unpkg.com/htmx.org@2.0.4"
            attributes["defer"] = "true"
        }
        script {
            src = "/static/js/voice.js"
            attributes["defer"] = "true"
        }
        script {
            src = "/static/js/lightbox.js"
            attributes["defer"] = "true"
        }
        script {
            src = "/static/js/hero-video.js"
            attributes["defer"] = "true"
        }
        script {
            src = "/static/js/motion.js"
            attributes["defer"] = "true"
        }
    }
    body {
        a(href = "#hook", classes = "skip-link") { +"Skip to content" }
        div(classes = "site-shell") {
            renderPortfolioPageBody()
        }
        div(classes = "lightbox") {
            id = "proof-lightbox"
            attributes["hidden"] = "hidden"
            attributes["role"] = "dialog"
            attributes["aria-modal"] = "true"
            attributes["aria-label"] = "Achievement image"
            button(classes = "lightbox-close") {
                type = ButtonType.button
                attributes["aria-label"] = "Close image"
                +"×"
            }
            button(classes = "lightbox-nav lightbox-prev") {
                type = ButtonType.button
                attributes["aria-label"] = "Previous image"
                +"‹"
            }
            img {
                id = "proof-lightbox-image"
                alt = ""
            }
            button(classes = "lightbox-nav lightbox-next") {
                type = ButtonType.button
                attributes["aria-label"] = "Next image"
                +"›"
            }
            p(classes = "lightbox-caption") {
                id = "proof-lightbox-caption"
            }
        }
        script {
            // Roster click updates active card, hidden agentId, and chat title in the Agents section.
            unsafe {
                +"""
                document.addEventListener('DOMContentLoaded', function () {
                  document.querySelectorAll('video[autoplay]').forEach(function (videoNode) {
                    videoNode.muted = true;
                    var playPromise = videoNode.play();
                    if (playPromise && playPromise.catch) playPromise.catch(function () {});
                  });
                });
                document.addEventListener('click', function (event) {
                  var card = event.target.closest('#agents [data-agent-id]');
                  if (!card) return;
                  var agentId = card.getAttribute('data-agent-id');
                  if (!agentId) return;
                  document.querySelectorAll('#agents [data-agent-id]').forEach(function (node) {
                    var isActive = node === card;
                    node.classList.toggle('is-active', isActive);
                    node.setAttribute('aria-checked', isActive ? 'true' : 'false');
                  });
                  var agentIdInput = document.getElementById('active-agent-id');
                  if (agentIdInput) agentIdInput.value = agentId;
                  var displayName = card.getAttribute('data-agent-name') || agentId;
                  var chatTitle = document.getElementById('active-agent-chat-title');
                  if (chatTitle) chatTitle.textContent = displayName;
                  var chatHint = document.getElementById('active-agent-chat-hint');
                  if (chatHint) {
                    chatHint.textContent = card.getAttribute('data-agent-use') ||
                      ('Ask my ' + displayName + ' agent something I use it for.');
                  }
                  var chatInput = document.querySelector('#agent-chat-form textarea');
                  var inputHint = card.getAttribute('data-agent-hint');
                  if (chatInput && inputHint) chatInput.placeholder = inputHint;
                });
                """.trimIndent()
            }
        }
    }
}
