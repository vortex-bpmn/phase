import {pages} from "./nav_index.js"

const NAV_ACTIVE_ENTRY_CLASS = "nav-entry-active"

let navItems = []

async function loadPage(filename) {
    const html = await fetch(filename)
    document.getElementById("viewer").innerHTML = await html.text()
}

async function prepareNavbar() {
    const navbar = document.getElementById("navbar")

    navItems = []

    for (let page of pages) {
        const title = page.title
        const path = page.path

        const navElement = document.createElement("div")
        navElement.classList.add("nav-entry")
        navElement.innerHTML = title
        navElement.onclick = () => {
            navItems.forEach(item => {
                item.classList.remove(NAV_ACTIVE_ENTRY_CLASS)
            })
            navElement.classList.add(NAV_ACTIVE_ENTRY_CLASS)
            loadPage(path)
        }
        navItems.push(navElement)
        navbar.appendChild(navElement)
    }
}

document.addEventListener('DOMContentLoaded', () => {
    prepareNavbar()
    loadPage("page_0.html")
})