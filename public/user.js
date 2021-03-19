function getProfile(name) {
	fetch(`http://157.230.233.218:8080/api/account/${name}`)
		.then(res => res.json())
		.then(function (json) { console.log(json) });
}

let user = window.location.href.split("?")[1]

getProfile(user);


function formatText(text) {
	text = text.split("**");

	for(let i = 0; i < text.length; i++) {
		if(i % 2 != 0) {
	  text[i] = "<b>" + text[i] + "</b>"
	}
	}

	text = text.join("").split("*");

	for (let i = 0; i < text.length; i++) {
		if (i % 2 != 0) {
			text[i] = "<i>" + text[i] + "</i>"
		}
	}

	text = text.join("").split(" ");
	text.join("&nbsp;");
	
	return text.join("");
}