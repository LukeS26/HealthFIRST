if (localStorage.getItem("light-mode") === "dark") {
    let r = document.querySelector(":root");

	r.style.setProperty("--post-text-color", "white");
    r.style.setProperty("--post-background-color", "rgb(50, 50, 50)");
    r.style.setProperty("--body-background-color", "rgb(25, 25, 25)");
    r.style.setProperty("--body-text-color", "white");
    r.style.setProperty("--header-background-color", "#002672");
    r.style.setProperty("--profileHeader-background-color", "#002672");
    r.style.setProperty("--navigation-background-color", "#002672");
    r.style.setProperty("--challenges-header", "white");
    r.style.setProperty("--finished-challenge-background-color", "rgb(50, 50, 50)");
}