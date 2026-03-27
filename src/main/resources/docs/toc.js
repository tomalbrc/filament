// Populate the sidebar
//
// This is a script, and not included directly in the page, to control the total size of the book.
// The TOC contains an entry for each page, so if each page includes a copy of the TOC,
// the total size of the page becomes O(n**2).
class MDBookSidebarScrollbox extends HTMLElement {
    constructor() {
        super();
    }
    connectedCallback() {
        this.innerHTML = '<ol class="chapter"><li class="chapter-item expanded affix "><a href="index.html">Introduction</a></li><li class="chapter-item expanded "><a href="content/general/general.html"><strong aria-hidden="true">1.</strong> General</a><a class="toggle"><div>❱</div></a></li><li><ol class="section"><li class="chapter-item "><a href="usage.html"><strong aria-hidden="true">1.1.</strong> Using filament</a></li><li class="chapter-item "><a href="developers.html"><strong aria-hidden="true">1.2.</strong> Developers</a></li><li class="chapter-item "><a href="content/creating-content.html"><strong aria-hidden="true">1.3.</strong> Creating content</a></li><li class="chapter-item "><a href="content/general/config.html"><strong aria-hidden="true">1.4.</strong> Config</a></li><li class="chapter-item "><a href="content/general/item-groups.html"><strong aria-hidden="true">1.5.</strong> Item Groups</a></li><li class="chapter-item "><a href="content/general/components.html"><strong aria-hidden="true">1.6.</strong> Components</a></li><li class="chapter-item "><a href="content/general/templates.html"><strong aria-hidden="true">1.7.</strong> Templates</a></li><li class="chapter-item "><a href="content/general/third-party-compat.html"><strong aria-hidden="true">1.8.</strong> Oraxen / ItemsAdder compatibility</a></li></ol></li><li class="chapter-item expanded "><a href="content/item/items.html"><strong aria-hidden="true">2.</strong> Items</a><a class="toggle"><div>❱</div></a></li><li><ol class="section"><li class="chapter-item "><a href="content/item/item-properties.html"><strong aria-hidden="true">2.1.</strong> Properties</a></li><li class="chapter-item "><a href="content/item/item-behaviours.html"><strong aria-hidden="true">2.2.</strong> Behaviours</a></li><li class="chapter-item "><a href="content/item/item-examples.html"><strong aria-hidden="true">2.3.</strong> Item Examples</a></li></ol></li><li class="chapter-item expanded "><a href="content/block/blocks.html"><strong aria-hidden="true">3.</strong> Blocks</a><a class="toggle"><div>❱</div></a></li><li><ol class="section"><li class="chapter-item "><a href="content/block/block-model-types.html"><strong aria-hidden="true">3.1.</strong> Block model types</a></li><li class="chapter-item "><a href="content/block/block-properties.html"><strong aria-hidden="true">3.2.</strong> Properties</a></li><li class="chapter-item "><a href="content/block/block-behaviours.html"><strong aria-hidden="true">3.3.</strong> Behaviours</a></li><li class="chapter-item "><a href="content/block/block-examples.html"><strong aria-hidden="true">3.4.</strong> Block Examples</a></li></ol></li><li class="chapter-item expanded "><a href="content/decoration/decorations.html"><strong aria-hidden="true">4.</strong> Decorations</a><a class="toggle"><div>❱</div></a></li><li><ol class="section"><li class="chapter-item "><a href="content/decoration/decoration-properties.html"><strong aria-hidden="true">4.1.</strong> Properties</a></li><li class="chapter-item "><a href="content/decoration/decoration-behaviours.html"><strong aria-hidden="true">4.2.</strong> Behaviours</a></li><li class="chapter-item "><a href="content/decoration/decoration-blocks.html"><strong aria-hidden="true">4.3.</strong> Blocks</a></li><li class="chapter-item "><a href="content/decoration/decoration-complex.html"><strong aria-hidden="true">4.4.</strong> Simple and complex decorations</a></li><li class="chapter-item "><a href="content/decoration/decoration-examples.html"><strong aria-hidden="true">4.5.</strong> Decoration Examples</a></li></ol></li><li class="chapter-item expanded "><a href="content/entity/entities.html"><strong aria-hidden="true">5.</strong> Entities</a><a class="toggle"><div>❱</div></a></li><li><ol class="section"><li class="chapter-item "><a href="content/entity/entity-properties.html"><strong aria-hidden="true">5.1.</strong> Properties</a></li><li class="chapter-item "><a href="content/entity/entity-goals.html"><strong aria-hidden="true">5.2.</strong> Goals</a></li></ol></li><li class="chapter-item expanded "><a href="tutorial/index.html"><strong aria-hidden="true">6.</strong> Tutorials</a><a class="toggle"><div>❱</div></a></li><li><ol class="section"><li class="chapter-item "><a href="tutorial/armor.html"><strong aria-hidden="true">6.1.</strong> Armor</a></li><li class="chapter-item "><a href="tutorial/cosmetic.html"><strong aria-hidden="true">6.2.</strong> Cosmetic</a></li><li class="chapter-item "><a href="tutorial/simple-block.html"><strong aria-hidden="true">6.3.</strong> Simple block</a></li><li class="chapter-item "><a href="tutorial/simple-item.html"><strong aria-hidden="true">6.4.</strong> Simple item</a></li><li class="chapter-item "><a href="tutorial/sword.html"><strong aria-hidden="true">6.5.</strong> Sword</a></li><li class="chapter-item "><a href="tutorial/tree.html"><strong aria-hidden="true">6.6.</strong> Tree</a></li></ol></li></ol>';
        // Set the current, active page, and reveal it if it's hidden
        let current_page = document.location.href.toString().split("#")[0].split("?")[0];
        if (current_page.endsWith("/")) {
            current_page += "index.html";
        }
        var links = Array.prototype.slice.call(this.querySelectorAll("a"));
        var l = links.length;
        for (var i = 0; i < l; ++i) {
            var link = links[i];
            var href = link.getAttribute("href");
            if (href && !href.startsWith("#") && !/^(?:[a-z+]+:)?\/\//.test(href)) {
                link.href = path_to_root + href;
            }
            // The "index" page is supposed to alias the first chapter in the book.
            if (link.href === current_page || (i === 0 && path_to_root === "" && current_page.endsWith("/index.html"))) {
                link.classList.add("active");
                var parent = link.parentElement;
                if (parent && parent.classList.contains("chapter-item")) {
                    parent.classList.add("expanded");
                }
                while (parent) {
                    if (parent.tagName === "LI" && parent.previousElementSibling) {
                        if (parent.previousElementSibling.classList.contains("chapter-item")) {
                            parent.previousElementSibling.classList.add("expanded");
                        }
                    }
                    parent = parent.parentElement;
                }
            }
        }
        // Track and set sidebar scroll position
        this.addEventListener('click', function(e) {
            if (e.target.tagName === 'A') {
                sessionStorage.setItem('sidebar-scroll', this.scrollTop);
            }
        }, { passive: true });
        var sidebarScrollTop = sessionStorage.getItem('sidebar-scroll');
        sessionStorage.removeItem('sidebar-scroll');
        if (sidebarScrollTop) {
            // preserve sidebar scroll position when navigating via links within sidebar
            this.scrollTop = sidebarScrollTop;
        } else {
            // scroll sidebar to current active section when navigating via "next/previous chapter" buttons
            var activeSection = document.querySelector('#sidebar .active');
            if (activeSection) {
                activeSection.scrollIntoView({ block: 'center' });
            }
        }
        // Toggle buttons
        var sidebarAnchorToggles = document.querySelectorAll('#sidebar a.toggle');
        function toggleSection(ev) {
            ev.currentTarget.parentElement.classList.toggle('expanded');
        }
        Array.from(sidebarAnchorToggles).forEach(function (el) {
            el.addEventListener('click', toggleSection);
        });
    }
}
window.customElements.define("mdbook-sidebar-scrollbox", MDBookSidebarScrollbox);
