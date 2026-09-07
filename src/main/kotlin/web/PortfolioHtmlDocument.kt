package web

import knowledge.ContactDetails
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
import web.seo.PortfolioSeo
import web.seo.buildPortfolioJsonLdScript

private const val STATIC_ASSET_VERSION = "20260908b"

/**
 * Full HTML document shell for the portfolio homepage.
 * Content is visible on first byte — no opacity:0 preloader.
 */
fun HTML.renderPortfolioHtmlDocument() {
    attributes["lang"] = PortfolioSeo.LANGUAGE_CODE
    attributes["prefix"] = "og: https://ogp.me/ns# profile: https://ogp.me/ns/profile#"
    head {
        meta(charset = "UTF-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        title(PortfolioSeo.PAGE_TITLE)
        meta(name = "description", content = PortfolioSeo.META_DESCRIPTION)
        meta(name = "keywords", content = PortfolioSeo.KEYWORDS)
        meta(name = "author", content = ContactDetails.FULL_NAME)
        meta(name = "robots", content = PortfolioSeo.ROBOTS_CONTENT)
        meta(name = "googlebot", content = PortfolioSeo.ROBOTS_CONTENT)
        meta(name = "theme-color", content = PortfolioSeo.THEME_COLOR)
        meta(name = "color-scheme", content = PortfolioSeo.COLOR_SCHEME)
        meta(name = "application-name", content = ContactDetails.FULL_NAME)
        meta(name = "apple-mobile-web-app-title", content = "Anudeep")
        meta(name = "format-detection", content = "telephone=no")
        link(rel = "canonical", href = PortfolioSeo.homepageUrl())
        link(rel = "alternate", href = PortfolioSeo.homepageUrl()) {
            attributes["hreflang"] = "en"
        }
        link(rel = "alternate", href = PortfolioSeo.homepageUrl()) {
            attributes["hreflang"] = "x-default"
        }
        link(rel = "alternate", type = "text/plain", href = PortfolioSeo.absoluteUrl("/llms.txt")) {
            attributes["title"] = "LLM-readable overview"
        }
        link(rel = "alternate", type = "text/plain", href = PortfolioSeo.absoluteUrl("/llms-full.txt")) {
            attributes["title"] = "LLM-readable full overview"
        }
        link(rel = "me", href = ContactDetails.LINKEDIN_URL)
        link(rel = "author", href = ContactDetails.LINKEDIN_URL)
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
        meta(content = PortfolioSeo.OG_TITLE) {
            attributes["property"] = "og:title"
        }
        meta(content = PortfolioSeo.OG_DESCRIPTION) {
            attributes["property"] = "og:description"
        }
        meta(content = PortfolioSeo.OG_TYPE) {
            attributes["property"] = "og:type"
        }
        meta(content = PortfolioSeo.homepageUrl()) {
            attributes["property"] = "og:url"
        }
        meta(content = PortfolioSeo.SITE_NAME) {
            attributes["property"] = "og:site_name"
        }
        meta(content = PortfolioSeo.OG_LOCALE) {
            attributes["property"] = "og:locale"
        }
        meta(content = PortfolioSeo.ogImageUrl()) {
            attributes["property"] = "og:image"
        }
        meta(content = PortfolioSeo.ogImageUrl()) {
            attributes["property"] = "og:image:secure_url"
        }
        meta(content = PortfolioSeo.OG_IMAGE_MIME_TYPE) {
            attributes["property"] = "og:image:type"
        }
        meta(content = PortfolioSeo.OG_IMAGE_WIDTH.toString()) {
            attributes["property"] = "og:image:width"
        }
        meta(content = PortfolioSeo.OG_IMAGE_HEIGHT.toString()) {
            attributes["property"] = "og:image:height"
        }
        meta(content = PortfolioSeo.OG_IMAGE_ALT) {
            attributes["property"] = "og:image:alt"
        }
        meta(content = "Anudeep") {
            attributes["property"] = "profile:first_name"
        }
        meta(content = "Ananth") {
            attributes["property"] = "profile:last_name"
        }
        meta(content = "anudeepananth") {
            attributes["property"] = "profile:username"
        }
        meta(name = "twitter:card", content = "summary_large_image")
        meta(name = "twitter:title", content = PortfolioSeo.OG_TITLE)
        meta(name = "twitter:description", content = PortfolioSeo.OG_DESCRIPTION)
        meta(name = "twitter:image", content = PortfolioSeo.ogImageUrl())
        meta(name = "twitter:image:alt", content = PortfolioSeo.OG_IMAGE_ALT)
        script(type = "application/ld+json") {
            unsafe { +buildPortfolioJsonLdScript() }
        }
        link(rel = "preconnect", href = "https://fonts.googleapis.com")
        link(rel = "preconnect", href = "https://fonts.gstatic.com") {
            attributes["crossorigin"] = "anonymous"
        }
        link(rel = "preconnect", href = "https://unpkg.com")
        link(rel = "preconnect", href = "https://www.reaptive.in")
        link(rel = "dns-prefetch", href = "https://fonts.googleapis.com")
        link(rel = "dns-prefetch", href = "https://unpkg.com")
        link(
            rel = "stylesheet",
            href = "https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Caveat:wght@500;700&family=Manrope:wght@400;600;700;800&display=swap",
        )
        link(rel = "stylesheet", href = "/static/css/lab.css?v=$STATIC_ASSET_VERSION")
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
            src = "/static/js/voice.js?v=$STATIC_ASSET_VERSION"
            attributes["defer"] = "true"
        }
        script {
            src = "/static/js/lightbox.js?v=$STATIC_ASSET_VERSION"
            attributes["defer"] = "true"
        }
        script {
            src = "/static/js/hero-video.js?v=$STATIC_ASSET_VERSION"
            attributes["defer"] = "true"
        }
        script {
            src = "/static/js/motion.js?v=$STATIC_ASSET_VERSION"
            attributes["defer"] = "true"
        }
    }
    body {
        a(href = "#main", classes = "skip-link") { +"Skip to content" }
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
                  var emailLink = event.target.closest('a[data-gmail-compose]');
                  if (emailLink) {
                    var gmailComposeUrl = emailLink.getAttribute('data-gmail-compose');
                    if (gmailComposeUrl) {
                      window.open(gmailComposeUrl, '_blank', 'noopener,noreferrer');
                    }
                    return;
                  }
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
                  if (window.matchMedia('(max-width: 980px)').matches) {
                    var liveDesk = document.getElementById('agent-live');
                    if (liveDesk) {
                      var prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
                      liveDesk.scrollIntoView({
                        behavior: prefersReducedMotion ? 'auto' : 'smooth',
                        block: 'start',
                      });
                    }
                  }
                });
                """.trimIndent()
            }
        }
    }
}
