function getProfile(name) {
	fetch(`http://157.230.233.218:8080/api/account/${name}`)
		.then(res => res.json())
		.then(function (json) { console.log(json) });
}

let user = window.location.href.split("?")[1]

getProfile(user);


function formatText(text) {
	let isOpen = false;

	text = text.split("**");

	for (let i = 0; i < text.length; i++) {
		if (text[i] == "" || text[i] == "*") {
			if (!isOpen) {
				isOpen = true;
				text[i] += "<i>"
			} else {
				text[i] += "</i>"
				isOpen = false;
			}
		}
	}

	isOpen = false;
	console.log(text);
	text = text.join("").split("*");
	console.log(text);
	for (let i = 0; i < text.length; i++) {
		if (text[i] == "") {
			if (!isOpen) {
				isOpen = true;
				text[i] = "<b>"
			} else {
				text[i] = "</b>"
				isOpen = false;
			}
		}
	}

	return text.join("");
}