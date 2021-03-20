function getProfile(name) {
	fetch(`http://157.230.233.218:8080/api/account/${name}`)
		.then(res => res.json())
		.then(function (json) { loadProfile(json) });
}

let user = window.location.href.split("?")[1]

getProfile(user);


function formatText(text) {
	text = text.split(" ");
	text = text.join("&nbsp;")
	text = text.split("**");

	for (let i = 0; i < text.length; i++) {
		if (i % 2 != 0) {
			text[i] = "<b>" + text[i] + "</b>"
		}
	}

	text = text.join("").split("*");

	for (let i = 0; i < text.length; i++) {
		if (i % 2 != 0) {
			text[i] = "<i>" + text[i] + "</i>"
		}
	}

	return text.join("");
}


function loadProfile(json) {
	if (json.profile_picture_link !== null && json.profile_picture_link !== "null") {
		document.getElementById("userPhoto").src = profile_picture_link;
	}

	document.getElementById("username") = json.username;
	
	if(json.biography != null) {
		document.getElementById("userBio") = formatText(json.biography);
	}
}