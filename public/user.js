function getProfile(name) {
	fetch(`http://157.230.233.218:8080/api/account/${name}`)
		.then(res => res.json())
		.then(function (json) { loadProfile(json) });
}

let user = window.location.href.split("?")[1]

getProfile(user);


function formatText(text) {
	// let response = await fetch('https://api.github.com/markdown', {method:"POST", body: JSON.stringify({"text": text}) } );//.then(res => res.text()).then(function(json) {return (json)})
	// let json = await response.text();

	// return json;
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


async function loadProfile(json) {
	if (json.profile_picture_link !== null && json.profile_picture_link !== "null") {
		document.getElementById("userPhoto").src = json.profile_picture_link;
	}

	document.getElementById("username").innerHTML = json.username;

	if(json.biography != null) {
		document.getElementById("userBio").innerHTML = await formatText(json.biography);
	}

	let badges = [...new Set(json.badge_ids)];
	badges.sort();

	for(let i = 0; i < badges.length; i++) {
		document.getElementById("badges").innerHTML += `<img class="badges" src="/badges/${getImg(badges[i])}" alt="${getTitle(badges[i])}" title="${getTitle(badges[i])}" width="30px" height="30px">`;
	}
}

function getImg(id) {
	switch(id) {
		case -1:
			return "developer0.png"
		case 0:
			return "nature1.png";
		case 1:
			return "exercise1.png";
		case 2:
			return "cheese0.png"
	}
}

function getTitle(id) {
	switch(id) {
		case -1:
			return "Developer";
		case 0:
			return "Walked 30 Minutes";
		case 1:
			return "Squats, Situps, and Pushups";
		case 2:
			return "Cheese"
	}
}