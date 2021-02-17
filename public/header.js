let username;
let usernameOutput = document.getElementById("usernameOutput");
let profileHeaderStuff = document.getElementById("profileHeaderStuff");
let optionsOpen = false;

if (sessionStorage.getItem("username") === null) {
	username = "Username";
} else {
	username = sessionStorage.getItem("username");
}

usernameOutput.innerHTML = username;
document.getElementById("profileHeader").style.width = (username.length * 1.5 - (username.length * 2)) + "em";
document.getElementById("profileHeaderStuff").style.width = document.getElementById("profileHeader").offsetWidth - 4 + "px";

function toggleOptions() {
	if (optionsOpen) {
		profileHeaderStuff.style.top = "-103%";
	} else {
		profileHeaderStuff.style.top = "100%";
	}
}

profileHeaderStuff.ontransitionend = function() {
	if (optionsOpen) {
		optionsOpen = false;
	} else {
		optionsOpen = true;
	}
}

window.onclick = function() {
	if (optionsOpen) {
		profileHeaderStuff.style.top = "-103%";
	}
}