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
		document.getElementById("userPhoto").src = json.profile_picture_link;
	}

	document.getElementById("username").innerHTML = json.username;

	if(json.biography != null) {
		document.getElementById("userBio").innerHTML = formatText(json.biography);
	}

	let badges = [...new Set(json.badge_ids)];
	badges.sort();

	for(let i = 0; i < badges.length; i++) {
		document.getElementById("badges").innerHTML += `<img src="${getImg(badges[i])}" alt="${getTitle(badges[i])}" title="${getTitle(badges[i])}" width="30px" height="30px">`;
	}
}

function getImg(id) {
	switch(id) {
		case 0:
			return "/badges/nature1.png";
		case 1:
			return "/badges/exercise1.png";
	}
}

function getTitle(id) {
	switch(id) {
		case 0:
			return "Walked 30 Minutes";
		case 1:
			return "Squats, Situps, and Pushups";
	}
}