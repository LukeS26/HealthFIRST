let username;
let usernameOutput = document.getElementById("usernameOutput");
let profileHeaderStuff = document.getElementById("profileHeaderStuff");
let optionsOpen = false;

if (getCookie("username") === null) {
	username = "Username";
} else {
	username = getCookie("username");
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

let data = {
	title: "text",
	body: "text"
}
let currToken = getCookie("token");

fetch("http://157.230.233.218:8080/api/posts", {
	method: "POST",
	body: JSON.stringify(data),
	headers: {
		'Content-type': 'application/json; charset=UTF-8'
	},
	mode: "cors",
	credentials: "include",
	withCredentials: true,
	headers: {
		'Origin': 'http://157.230.233.218:8080',
		'Authorization': currToken
	}
})
.then(res => {
	let code = res.status;
	console.log(code);
	if (!res.ok) {
		alert("token didn't work!");
	} else {
		alert("It worked!");
	}
})
.catch(err => {
	console.log(err);
	console.log("Request failed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
});