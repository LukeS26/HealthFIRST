let username;
let usernameOutput = document.getElementById("usernameOutput");
let profileHeaderStuff = document.getElementById("profileHeaderStuff");
let headerImage = document.getElementById("headerImage");
let optionsOpen = false;

if (getCookie("username") === null) {
	username = "Username";
} else {
	username = getCookie("username");
}

if (getCookie("imgUrl") !== null && getCookie("imgUrl") !== "null") {
	headerImage.src = getCookie("imgUrl");
}

usernameOutput.innerHTML = username;
document.getElementById("profileHeader").style.width = (username.length * 1.5 - (username.length * 2)) + "em";
document.getElementById("profileHeaderStuff").style.width = document.getElementById("profileHeader").offsetWidth - 4 + "px";

function getCookie(name) {
	let cookieArr = document.cookie.split(";");

	for (let i = 0; i < cookieArr.length; i++) {
		let cookiePair = cookieArr[i].split("=");

		if (name === cookiePair[0].trim()) {
			return cookiePair[1];
		}
	}

	return null;
}

function toggleOptions() {
	if (optionsOpen) {
		profileHeaderStuff.style.top = "-103%";
	} else {
		profileHeaderStuff.style.top = "95%";
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

function logout() {
	document.cookie = "username=; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
	document.cookie = "token=; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
	document.cookie = "accepted=; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
	document.cookie = "imgUrl=; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
	window.location.href = "/login.html";
}

if( navigator.userAgent.match(/Android/i)
 || navigator.userAgent.match(/webOS/i)
 || navigator.userAgent.match(/iPhone/i)
 || navigator.userAgent.match(/iPad/i)
 || navigator.userAgent.match(/iPod/i)
 || navigator.userAgent.match(/BlackBerry/i)
 || navigator.userAgent.match(/Windows Phone/i) {
	alert("You're using Mobile Device!!")
}