function getProfile(name) {
	fetch(`http://157.230.233.218:8080/api/account/${name}`)
		.then(res => res.json())
		.then(function (json) { console.log(json) });
}

let user = window.location.href.split("?")[1]

getProfile(user);


function formatText(text) {
	text = text.split("**");

	for (let i = 0; i < text.length; i++) {
		if (text[i] == "") {
			if (isOpen) {
				isOpen = false;
				text[i] = "<i>"
			} else {
				text[i] = "</i>"
				isOpen = true;
			}
		}
	}

	text = text.join("").split("*");

	for (let i = 0; i < text.length; i++) {
		if (text[i] == "") {
			if (isOpen) {
				isOpen = false;
				text[i] = "<b>"
			} else {
				text[i] = "</b>"
				isOpen = true;
			}
		}
	}

	return text.join("");
}